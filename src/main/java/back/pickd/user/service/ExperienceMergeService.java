package back.pickd.user.service;

import back.pickd.global.error.ApiException;
import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.AiExperienceMergeCheckRequest;
import back.pickd.global.infra.ai.dto.AiExperienceMergeCheckResponse;
import back.pickd.global.infra.ai.dto.AiStep2Response;
import back.pickd.user.dto.ExperienceCreateRequestDto;
import back.pickd.user.dto.ExperienceDraftResponse;
import back.pickd.user.dto.ExperienceMergeCandidateResponse;
import back.pickd.user.dto.ExperienceMergeConflictResponse;
import back.pickd.user.entity.User;
import back.pickd.user.entity.UserExperience;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import back.pickd.user.repository.UserExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceMergeService {

    private final AiClient aiClient;
    private final UserExperienceRepository userExperienceRepository;

    public List<AiExperienceMergeCheckRequest.ExperiencePayload> buildExistingExperiencePayloads(User user) {
        return userExperienceRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    public Optional<ExperienceMergeConflictResponse> findCreateMergeCandidate(
            User user,
            ExperienceCreateRequestDto request
    ) {
        List<AiExperienceMergeCheckRequest.ExperiencePayload> existingExperiences = buildExistingExperiencePayloads(user);
        if (existingExperiences.isEmpty()) {
            return Optional.empty();
        }

        AiExperienceMergeCheckResponse.MergeCheckResult result = checkSingleTarget(
                toPayload(request),
                existingExperiences
        );

        if (result == null || !result.isNeedsMerge()) {
            return Optional.empty();
        }

        Optional<ExperienceMergeConflictResponse> conflict = buildConflictResponse(
                user,
                result.getMergeCandidateId(),
                result.getSimilarity(),
                ExperienceDraftResponse.fromCreateRequest(request)
        );
        if (conflict.isEmpty()) {
            throw ApiException.badGateway("AI 병합 후보를 사용자 경험에서 찾을 수 없습니다.");
        }
        return conflict;
    }

    public Optional<ExperienceMergeConflictResponse> buildStep2MergeCandidate(
            User user,
            AiStep2Response.Step2ExperienceDto dto,
            ExperienceType type,
            ExperienceGroup group
    ) {
        if (!dto.isNeeds_merge() || dto.getMerge_candidate_id() == null) {
            return Optional.empty();
        }

        Optional<ExperienceMergeConflictResponse> conflict = buildConflictResponse(
                user,
                dto.getMerge_candidate_id(),
                dto.getMerge_similarity(),
                ExperienceDraftResponse.fromStep2(dto, type, group, Status.COMPLETED)
        );
        if (conflict.isEmpty()) {
            throw ApiException.badGateway("AI 병합 후보를 사용자 경험에서 찾을 수 없습니다.");
        }
        return conflict;
    }

    private AiExperienceMergeCheckResponse.MergeCheckResult checkSingleTarget(
            AiExperienceMergeCheckRequest.ExperiencePayload target,
            List<AiExperienceMergeCheckRequest.ExperiencePayload> existingExperiences
    ) {
        AiExperienceMergeCheckRequest request = AiExperienceMergeCheckRequest.builder()
                .targets(List.of(target))
                .existingExperiences(existingExperiences)
                .topK(1)
                .build();
        AiExperienceMergeCheckResponse response = aiClient.checkExperienceMerge(request);
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw ApiException.badGateway("AI 병합 검사 응답이 없습니다.");
        }
        return response.getResults().get(0);
    }

    private Optional<ExperienceMergeConflictResponse> buildConflictResponse(
            User user,
            String mergeCandidateId,
            Double similarity,
            ExperienceDraftResponse draft
    ) {
        if (mergeCandidateId == null) {
            return Optional.empty();
        }

        return userExperienceRepository.findByIdAndUser(mergeCandidateId, user)
                .map(candidate -> {
                    ExperienceMergeCandidateResponse candidateResponse =
                            ExperienceMergeCandidateResponse.from(candidate, similarity);
                    return new ExperienceMergeConflictResponse(
                            true,
                            candidateResponse,
                            similarity,
                            draft
                    );
                });
    }

    private AiExperienceMergeCheckRequest.ExperiencePayload toPayload(UserExperience experience) {
        return AiExperienceMergeCheckRequest.ExperiencePayload.builder()
                .id(experience.getId())
                .title(experience.getTitle())
                .experienceName(experience.getTitle())
                .experienceGroup(toKoreanGroup(experience.getExperienceGroup()))
                .experienceType(toKoreanType(experience.getExperienceType()))
                .keywords(experience.getKeywords() != null ? experience.getKeywords() : new ArrayList<>())
                .attributes(experience.getAttributes() != null ? experience.getAttributes() : new HashMap<>())
                .documentContent(experience.getDocumentContent())
                .build();
    }

    private AiExperienceMergeCheckRequest.ExperiencePayload toPayload(ExperienceCreateRequestDto request) {
        return AiExperienceMergeCheckRequest.ExperiencePayload.builder()
                .title(request.getTitle())
                .experienceName(request.getTitle())
                .experienceGroup(toKoreanGroup(request.getExperienceGroup()))
                .experienceType(toKoreanType(request.getExperienceType()))
                .keywords(request.getKeywords() != null ? request.getKeywords() : new ArrayList<>())
                .attributes(request.getAttributes() != null ? request.getAttributes() : new HashMap<>())
                .documentContent(request.getDocumentContent())
                .build();
    }

    private String toKoreanGroup(ExperienceGroup group) {
        if (group == null) {
            return null;
        }
        return group == ExperienceGroup.NARRATIVE ? "상세 서술형" : "스펙·증빙형";
    }

    private String toKoreanType(ExperienceType type) {
        return type != null ? type.getKoreanName() : null;
    }
}
