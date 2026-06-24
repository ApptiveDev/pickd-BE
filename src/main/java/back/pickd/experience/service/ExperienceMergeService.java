package back.pickd.experience.service;

import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.AiExperienceMergeCheckRequest;
import back.pickd.global.infra.ai.dto.AiExperienceMergeCheckResponse;
import back.pickd.global.infra.ai.dto.AiStep2Response;
import back.pickd.experience.dto.ExperienceCreateDto.Request;
import back.pickd.experience.dto.ExperienceMergeDto.Candidate;
import back.pickd.experience.dto.ExperienceMergeDto.Conflict;
import back.pickd.experience.dto.ExperienceMergeDto.Draft;
import back.pickd.experience.support.ExperienceConversionUtils;
import back.pickd.user.entity.User;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.UserExperienceRepository;
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
    private final ExperienceConversionUtils conversionUtils;

    public List<AiExperienceMergeCheckRequest.ExperiencePayload> buildExistingExperiencePayloads(User user) {
        return userExperienceRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    public Optional<Conflict> findCreateMergeCandidate(
            User user,
            Request request
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

        Optional<Conflict> conflict = buildConflictResponse(
                user,
                result.getMergeCandidateId(),
                result.getSimilarity(),
                Draft.fromCreateRequest(request)
        );
        if (conflict.isEmpty()) {
            throw new RuntimeException("AI 병합 후보를 사용자 경험에서 찾을 수 없습니다.");
        }
        return conflict;
    }

    public Optional<Conflict> buildStep2MergeCandidate(
            User user,
            AiStep2Response.Step2ExperienceDto dto,
            ExperienceType type,
            ExperienceGroup group
    ) {
        if (!dto.isNeeds_merge() || dto.getMerge_candidate_id() == null) {
            return Optional.empty();
        }

        Optional<Conflict> conflict = buildConflictResponse(
                user,
                dto.getMerge_candidate_id(),
                dto.getMerge_similarity(),
                Draft.fromStep2(dto, type, group, Status.COMPLETED)
        );
        if (conflict.isEmpty()) {
            throw new RuntimeException("AI 병합 후보를 사용자 경험에서 찾을 수 없습니다.");
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
            throw new RuntimeException("AI 병합 검사 응답이 없습니다.");
        }
        return response.getResults().get(0);
    }

    private Optional<Conflict> buildConflictResponse(
            User user,
            String mergeCandidateId,
            Double similarity,
            Draft draft
    ) {
        if (mergeCandidateId == null) {
            return Optional.empty();
        }

        return userExperienceRepository.findByIdAndUser(mergeCandidateId, user)
                .map(candidate -> {
                    Candidate candidateResponse = Candidate.from(candidate, similarity);
                    return new Conflict(
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
                .experienceGroup(conversionUtils.toKoreanGroup(experience.getExperienceGroup()))
                .experienceType(conversionUtils.toKoreanType(experience.getExperienceType()))
                .keywords(experience.getKeywords() != null ? experience.getKeywords() : new ArrayList<>())
                .attributes(experience.getAttributes() != null ? experience.getAttributes() : new HashMap<>())
                .documentContent(experience.getDocumentContent())
                .build();
    }

    private AiExperienceMergeCheckRequest.ExperiencePayload toPayload(Request request) {
        return AiExperienceMergeCheckRequest.ExperiencePayload.builder()
                .title(request.getTitle())
                .experienceName(request.getTitle())
                .experienceGroup(conversionUtils.toKoreanGroup(request.getExperienceGroup()))
                .experienceType(conversionUtils.toKoreanType(request.getExperienceType()))
                .keywords(request.getKeywords() != null ? request.getKeywords() : new ArrayList<>())
                .attributes(request.getAttributes() != null ? request.getAttributes() : new HashMap<>())
                .documentContent(request.getDocumentContent())
                .build();
    }

}
