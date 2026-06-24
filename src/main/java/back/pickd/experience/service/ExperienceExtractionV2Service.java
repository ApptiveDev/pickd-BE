package back.pickd.experience.service;

import back.pickd.experience.dto.ExperienceExtractionV2Dto.DuplicateGroupResponse;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.GroupSelection;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.PendingBatchResponse;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.PendingBatchesResponse;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.Step2Response;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.Step3Request;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.Step3Response;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.entity.*;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.ExtractionBatchStatus;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.ExperienceExtractionBatchRepository;
import back.pickd.experience.repository.ExperienceTempRepository;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.experience.support.ExperienceConversionUtils;
import back.pickd.experience.support.PresetRegistry;
import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.AiExperienceMergeCheckRequest;
import back.pickd.global.infra.ai.dto.AiStep1Response;
import back.pickd.global.infra.ai.dto.AiStep2Response;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceExtractionV2Service {

    private static final String BATCH_CANDIDATE_PREFIX = "batch:";

    private final AiClient aiClient;
    private final ExperienceTempRepository tempRepository;
    private final UserExperienceRepository experienceRepository;
    private final ExperienceExtractionBatchRepository batchRepository;
    private final UserService userService;
    private final ExperienceMergeService experienceMergeService;
    private final PresetRegistry presetRegistry;
    private final ExperienceConversionUtils conversionUtils;

    @Transactional
    public Step2Response extractStep2(String email, List<Long> selectedTempIds) {
        User user = userService.findByEmail(email);
        List<ExperienceTemp> temps = loadSelectedTemps(user, selectedTempIds);
        String resumeUrl = validateSingleResumeSource(temps);

        List<SelectedExperience> selectedExperiences = temps.stream()
                .map(this::toSelectedExperience)
                .toList();

        List<AiStep1Response.ExperienceSummaryDto> summaries = selectedExperiences.stream()
                .map(selected -> new AiStep1Response.ExperienceSummaryDto(
                        selected.temp().getExperienceName(),
                        conversionUtils.toKoreanGroup(selected.group()),
                        selected.type().getKoreanName()
                ))
                .toList();

        List<AiExperienceMergeCheckRequest.ExperiencePayload> existingExperiences =
                experienceMergeService.buildExistingExperiencePayloads(user);

        AiStep2Response aiResponse = aiClient.extractStep2V2ByUrl(
                resumeUrl,
                summaries,
                existingExperiences,
                presetRegistry.getAiPresetSchemas(
                        selectedExperiences.stream().map(SelectedExperience::type).toList()
                )
        );

        List<AiStep2Response.Step2ExperienceDto> extracted = validateAiResponse(
                aiResponse,
                selectedExperiences
        );

        // UUID를 @PrePersist 이전에 수동 할당 → 루프 내 즉시 참조 가능, save() 지연 가능
        List<UserExperience> toSave = new ArrayList<>();
        Map<Integer, UserExperience> savedByTargetIndex = new HashMap<>();
        Map<String, ExperienceDuplicateGroup> groupsByExistingId = new LinkedHashMap<>();
        ExperienceExtractionBatch batch = null;

        for (int index = 0; index < extracted.size(); index++) {
            AiStep2Response.Step2ExperienceDto dto = extracted.get(index);
            SelectedExperience selected = selectedExperiences.get(index);

            if (!dto.isNeeds_merge()) {
                UserExperience experience = buildExtractedExperience(
                        user,
                        dto,
                        selected.type(),
                        selected.group(),
                        resumeUrl
                );
                toSave.add(experience);
                savedByTargetIndex.put(index, experience);
                continue;
            }

            UserExperience existing = resolveMergeCandidate(
                    user,
                    dto.getMerge_candidate_id(),
                    savedByTargetIndex
            );

            if (batch == null) {
                batch = new ExperienceExtractionBatch(user);
            }

            ExperienceDuplicateGroup group = groupsByExistingId.get(existing.getId());
            if (group == null) {
                group = new ExperienceDuplicateGroup(existing);
                batch.addGroup(group);
                groupsByExistingId.put(existing.getId(), group);
            }

            group.addDraft(ExperienceExtractionDraft.builder()
                    .title(dto.getExperience_name())
                    .experienceType(selected.type())
                    .experienceGroup(selected.group())
                    .status(Status.COMPLETED)
                    .documentContent(dto.getExperience_content())
                    .attributes(presetRegistry.normalizeAttributes(
                            selected.type(),
                            dto.getBasic_info()
                    ))
                    .keywords(dto.getKeywords())
                    .resumeUrl(resumeUrl)
                    .similarity(dto.getMerge_similarity())
                    .build());
        }

        // 경험 + 파일 일괄 저장 (CascadeType.ALL 로 ExperienceFile 함께 INSERT)
        List<UserExperience> savedExperiences = experienceRepository.saveAll(toSave);

        if (batch != null) {
            batchRepository.save(batch);
        }

        tempRepository.deleteByUser(user);

        return new Step2Response(
                savedExperiences.stream().map(ExperienceResponse::new).toList(),
                batch != null ? batch.getId() : null,
                batch != null
                        ? batch.getGroups().stream().map(DuplicateGroupResponse::from).toList()
                        : List.of()
        );
    }

    @Transactional
    public Step3Response confirmStep3(String email, Step3Request request) {
        User user = userService.findByEmail(email);
        ExperienceExtractionBatch batch = batchRepository.findByIdAndUser(
                        request.getDuplicateBatchId(),
                        user
                )
                .orElseThrow(() -> new IllegalArgumentException("중복 경험 batch를 찾을 수 없습니다."));

        if (batch.getStatus() != ExtractionBatchStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 중복 경험 batch입니다.");
        }

        Map<String, GroupSelection> selections = indexSelections(request.getGroups());

        Set<String> expectedGroupIds = batch.getGroups().stream()
                .map(ExperienceDuplicateGroup::getId)
                .collect(Collectors.toSet());

        if (!selections.keySet().equals(expectedGroupIds)) {
            throw new IllegalArgumentException("batch의 모든 중복 그룹에 대한 선택이 필요합니다.");
        }

        List<UserExperience> selectedExperiences = new ArrayList<>();
        List<UserExperience> experiencesToDelete = new ArrayList<>();
        List<String> deletedExperienceIds = new ArrayList<>();

        for (ExperienceDuplicateGroup group : batch.getGroups()) {
            Set<String> selectedItemIds = new LinkedHashSet<>(
                    selections.get(group.getId()).getSelectedItemIds()
            );

            if (selectedItemIds.isEmpty()) {
                throw new IllegalArgumentException("그룹마다 하나 이상의 경험을 선택해야 합니다.");
            }

            if (selectedItemIds.size()
                    != selections.get(group.getId()).getSelectedItemIds().size()) {
                throw new IllegalArgumentException("동일한 경험을 중복 선택할 수 없습니다.");
            }

            Set<String> allowedItemIds = new HashSet<>();
            allowedItemIds.add(group.getExistingExperience().getId());
            group.getDrafts().stream()
                    .map(ExperienceExtractionDraft::getId)
                    .forEach(allowedItemIds::add);

            if (!allowedItemIds.containsAll(selectedItemIds)) {
                throw new IllegalArgumentException("중복 그룹에 속하지 않은 경험이 선택되었습니다.");
            }

            UserExperience existing = group.getExistingExperience();
            if (selectedItemIds.contains(existing.getId())) {
                selectedExperiences.add(existing);
            } else {
                experiencesToDelete.add(existing);
                deletedExperienceIds.add(existing.getId());
            }

            for (ExperienceExtractionDraft draft : group.getDrafts()) {
                if (selectedItemIds.contains(draft.getId())) {
                    selectedExperiences.add(buildDraftExperience(user, draft));
                }
            }
        }

        // Draft에서 생성된 경험 일괄 저장 (CascadeType.ALL 로 ExperienceFile 함께 INSERT)
        experienceRepository.saveAll(selectedExperiences);

        batch.complete();
        batchRepository.saveAndFlush(batch);

        if (!experiencesToDelete.isEmpty()) {
            experienceRepository.deleteAll(experiencesToDelete);
        }

        return new Step3Response(
                selectedExperiences.stream().map(ExperienceResponse::new).toList(),
                deletedExperienceIds
        );
    }

    @Transactional(readOnly = true)
    public PendingBatchesResponse getPendingDuplicates(String email) {
        User user = userService.findByEmail(email);
        return new PendingBatchesResponse(
                batchRepository.findByUserAndStatusOrderByCreatedAtDesc(
                                user,
                                ExtractionBatchStatus.PENDING
                        )
                        .stream()
                        .map(PendingBatchResponse::from)
                        .toList()
        );
    }

    private List<ExperienceTemp> loadSelectedTemps(User user, List<Long> selectedTempIds) {
        if (selectedTempIds == null || selectedTempIds.isEmpty()) {
            throw new IllegalArgumentException("선택된 임시 경험 ID가 없습니다.");
        }
        if (new HashSet<>(selectedTempIds).size() != selectedTempIds.size()) {
            throw new IllegalArgumentException("동일한 임시 경험을 중복 선택할 수 없습니다.");
        }
        Map<Long, ExperienceTemp> tempsById = tempRepository.findAllById(selectedTempIds)
                .stream()
                .collect(Collectors.toMap(ExperienceTemp::getId, Function.identity()));

        if (tempsById.size() != selectedTempIds.size()) {
            throw new IllegalArgumentException("요청한 임시 경험 데이터를 찾을 수 없습니다.");
        }
        List<ExperienceTemp> ordered = selectedTempIds.stream()
                .map(tempsById::get)
                .toList();
        if (ordered.stream().anyMatch(temp -> !isOwnedBy(temp, user))) {
            throw new IllegalArgumentException("다른 사용자의 임시 경험은 선택할 수 없습니다.");
        }
        return ordered;
    }

    private boolean isOwnedBy(ExperienceTemp temp, User user) {
        if (temp.getUser() == user) {
            return true;
        }
        return temp.getUser().getId() != null
                && Objects.equals(temp.getUser().getId(), user.getId());
    }

    private String validateSingleResumeSource(List<ExperienceTemp> temps) {
        Set<String> resumeUrls = temps.stream()
                .map(ExperienceTemp::getResumeUrl)
                .collect(Collectors.toSet());
        if (resumeUrls.size() != 1) {
            throw new IllegalArgumentException("서로 다른 원본 파일의 경험을 함께 추출할 수 없습니다.");
        }
        return temps.get(0).getResumeUrl();
    }

    private SelectedExperience toSelectedExperience(ExperienceTemp temp) {
        ExperienceType type = temp.getExperienceType();
        ExperienceGroup group = temp.getExperienceGroup();
        conversionUtils.validateGroupType(group, type);
        return new SelectedExperience(temp, type, group);
    }

    private List<AiStep2Response.Step2ExperienceDto> validateAiResponse(
            AiStep2Response response,
            List<SelectedExperience> selectedExperiences
    ) {
        if (response == null || response.getExperiences() == null) {
            throw new IllegalStateException("AI 2차 경험 추출 응답이 없습니다.");
        }
        if (response.getExperiences().size() != selectedExperiences.size()) {
            throw new IllegalStateException("AI 2차 경험 추출 결과 수가 선택한 경험 수와 다릅니다.");
        }
        for (int index = 0; index < response.getExperiences().size(); index++) {
            AiStep2Response.Step2ExperienceDto dto = response.getExperiences().get(index);
            ExperienceType responseType = conversionUtils.convertType(dto.getExperience_type());
            ExperienceGroup responseGroup = conversionUtils.convertGroup(dto.getExperience_group());
            SelectedExperience selected = selectedExperiences.get(index);
            if (responseType != selected.type() || responseGroup != selected.group()) {
                throw new IllegalStateException("AI 2차 경험 유형이 선택한 1차 경험과 일치하지 않습니다.");
            }
        }
        return response.getExperiences();
    }

    private UserExperience resolveMergeCandidate(
            User user,
            String candidateId,
            Map<Integer, UserExperience> savedByTargetIndex
    ) {
        if (candidateId == null || candidateId.isBlank()) {
            throw new IllegalStateException("AI 중복 후보 ID가 없습니다.");
        }
        if (candidateId.startsWith(BATCH_CANDIDATE_PREFIX)) {
            int targetIndex;
            try {
                targetIndex = Integer.parseInt(
                        candidateId.substring(BATCH_CANDIDATE_PREFIX.length())
                );
            } catch (NumberFormatException e) {
                throw new IllegalStateException("AI batch 중복 후보 ID가 올바르지 않습니다.", e);
            }
            UserExperience saved = savedByTargetIndex.get(targetIndex);
            if (saved == null) {
                throw new IllegalStateException("AI batch 중복 후보가 저장 대상에 없습니다.");
            }
            return saved;
        }
        return experienceRepository.findByIdAndUser(candidateId, user)
                .orElseThrow(() -> new IllegalStateException(
                        "AI 중복 후보를 사용자 경험에서 찾을 수 없습니다."
                ));
    }

    /**
     * UserExperience 객체를 빌드만 하고 저장은 하지 않습니다.
     * UUID를 @PrePersist 이전에 수동 할당하여 저장 전에도 ID 참조가 가능합니다.
     * 호출부에서 saveAll()로 일괄 저장하세요.
     */
    private UserExperience buildExtractedExperience(
            User user,
            AiStep2Response.Step2ExperienceDto dto,
            ExperienceType type,
            ExperienceGroup group,
            String resumeUrl
    ) {
        UserExperience experience = UserExperience.builder()
                .id(java.util.UUID.randomUUID().toString())
                .user(user)
                .title(dto.getExperience_name())
                .experienceType(type)
                .experienceGroup(group)
                .status(Status.COMPLETED)
                .documentContent(dto.getExperience_content())
                .attributes(presetRegistry.normalizeAttributes(type, dto.getBasic_info()))
                .keywords(dto.getKeywords() != null ? dto.getKeywords() : new ArrayList<>())
                .build();
        attachResumeFile(experience, resumeUrl);
        return experience;
    }

    /**
     * Draft로부터 UserExperience를 빌드만 하고 저장은 하지 않습니다.
     * 호출부에서 saveAll()로 일괄 저장하세요.
     */
    private UserExperience buildDraftExperience(User user, ExperienceExtractionDraft draft) {
        UserExperience experience = UserExperience.builder()
                .id(java.util.UUID.randomUUID().toString())
                .user(user)
                .title(draft.getTitle())
                .experienceType(draft.getExperienceType())
                .experienceGroup(draft.getExperienceGroup())
                .status(draft.getStatus())
                .documentContent(draft.getDocumentContent())
                .attributes(draft.getAttributes())
                .keywords(draft.getKeywords())
                .build();
        attachResumeFile(experience, draft.getResumeUrl());
        return experience;
    }

    private void attachResumeFile(UserExperience experience, String resumeUrl) {
        ExperienceFile resumeFile = ExperienceFile.builder()
                .userExperience(experience)
                .originalFilename("자기소개서 원본.pdf")
                .fileType("application/pdf")
                .fileSize(0L)
                .filePath(resumeUrl)
                .source("RESUME_ORIGINAL")
                .build();
        experience.updateFiles(List.of(resumeFile));
    }

    private Map<String, GroupSelection> indexSelections(List<GroupSelection> selections) {
        Map<String, GroupSelection> indexed = new HashMap<>();
        for (GroupSelection selection : selections) {
            if (indexed.put(selection.getGroupId(), selection) != null) {
                throw new IllegalArgumentException("동일한 중복 그룹을 중복 제출할 수 없습니다.");
            }
        }
        return indexed;
    }

    private record SelectedExperience(
            ExperienceTemp temp,
            ExperienceType type,
            ExperienceGroup group
    ) {
    }
}
