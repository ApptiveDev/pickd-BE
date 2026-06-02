package back.pickd.user.service;

import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.AiExperienceMergeCheckRequest;
import back.pickd.global.infra.ai.dto.AiStep1Response;
import back.pickd.global.infra.ai.dto.AiStep2Response;
import back.pickd.global.infra.s3.FileUploadType;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.dto.ExperienceMergeConflictResponse;
import back.pickd.user.dto.ExperienceStep3Action;
import back.pickd.user.dto.ExperienceStep3Request;
import back.pickd.user.dto.ExperienceStep2SaveResult;
import back.pickd.user.dto.ExperienceStep3Response;
import back.pickd.user.dto.UserExperienceResponse;
import back.pickd.user.entity.*;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import back.pickd.user.repository.ExperienceTempRepository;
import back.pickd.user.repository.UserExperienceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienceExtractionService {

    private final AiClient aiClient;
    private final S3Service s3Service;
    private final ExperienceTempRepository tempRepository;
    private final UserExperienceRepository experienceRepository;
    private final UserService userService;
    private final ExperienceMergeService experienceMergeService;

    /**
     * 1차 경험 후보 추출 및 임시 캐싱
     */
    @Transactional
    public List<ExperienceTemp> extractStep1(String email, MultipartFile file) {
        User user = userService.findByEmail(email);
        // 1. 자소서 원본을 S3 temp/resume 디렉터리에 임시 저장 (CloudFront URL 획득)
        String resumeUrl = s3Service.uploadFile(file, FileUploadType.TEMP_RESUME, user.getId());

        // 2. AI 서버 1차 분석 호출 (FastAPI step1)
        AiStep1Response aiResponse = aiClient.extractStep1(file);

        // 3. 기존에 쌓여있던 임시 데이터는 초기화
        tempRepository.deleteByUser(user);

        // 4. 새로운 1차 분석 결과를 임시 테이블에 캐싱
        List<ExperienceTemp> temps = new ArrayList<>();
        if (aiResponse.getExperiences() != null) {
            for (AiStep1Response.ExperienceSummaryDto summary : aiResponse.getExperiences()) {
                ExperienceTemp temp = ExperienceTemp.builder()
                        .user(user)
                        .experienceName(summary.getExperience_name())
                        .experienceGroup(summary.getExperience_group())
                        .experienceType(summary.getExperience_type())
                        .resumeUrl(resumeUrl)
                        .build();
                temps.add(tempRepository.save(temp));
            }
        }

        return temps;
    }

    /**
     * 2차 선택형 정밀 분석 및 UserExperience 최종 영구 저장
     */
    @Transactional
    public ExperienceStep2SaveResult extractStep2(String email, List<Long> selectedTempIds) {
        if (selectedTempIds == null || selectedTempIds.isEmpty()) {
            throw new IllegalArgumentException("선택된 임시 경험 ID가 없습니다.");
        }

        User user = userService.findByEmail(email);
        // 1. 선택한 임시 경험 데이터들을 DB에서 로드
        List<ExperienceTemp> temps = tempRepository.findAllById(selectedTempIds);
        if (temps.isEmpty()) {
            throw new IllegalArgumentException("요청한 임시 경험 데이터를 찾을 수 없습니다.");
        }

        // 자소서 URL 추출 (모두 같은 파일이므로 첫 번째 아이템에서 획득)
        String resumeUrl = temps.get(0).getResumeUrl();

        // 2. AI step2 송신용 DTO 리스트 변환 (개행 오류 차단을 위해 객체 생성)
        List<AiStep1Response.ExperienceSummaryDto> selectedSummaries = temps.stream()
                .map(t -> new AiStep1Response.ExperienceSummaryDto(
                        t.getExperienceName(),
                        t.getExperienceGroup(),
                        t.getExperienceType()
                ))
                .collect(Collectors.toList());

        List<AiExperienceMergeCheckRequest.ExperiencePayload> existingExperiences =
                experienceMergeService.buildExistingExperiencePayloads(user);

        // 3. AI 2차 분석 호출 (S3 CloudFront URL을 던져 2차 정밀 분석 수행)
        AiStep2Response aiResponse = aiClient.extractStep2ByUrl(resumeUrl, selectedSummaries, existingExperiences);

        // 4. 2차 상세 분석 결과 영구 저장 및 파일 연동
        List<UserExperience> savedExperiences = new ArrayList<>();
        List<ExperienceMergeConflictResponse> mergeCandidates = new ArrayList<>();
        if (aiResponse.getExperiences() != null) {
            for (AiStep2Response.Step2ExperienceDto dto : aiResponse.getExperiences()) {
                ExperienceGroup group = convertGroup(dto.getExperience_group());
                ExperienceType type = convertType(dto.getExperience_type());

                experienceMergeService.buildStep2MergeCandidate(user, dto, type, group)
                        .ifPresent(mergeCandidates::add);
                if (dto.isNeeds_merge()) {
                    continue;
                }

                // UserExperience 빌드 및 attributes JSONB 통짜 적재
                UserExperience userExperience = UserExperience.builder()
                        .user(user)
                        .title(dto.getExperience_name())
                        .experienceGroup(group)
                        .experienceType(type)
                        .status(Status.COMPLETED)
                        .documentContent(dto.getExperience_content())
                        .attributes(dto.getBasic_info() != null ? dto.getBasic_info() : new HashMap<>())
                        .keywords(dto.getKeywords() != null ? dto.getKeywords() : new ArrayList<>())
                        .build();

                // 첨부 파일 등록 (자소서 원본 출처 명시)
                ExperienceFile resumeFile = ExperienceFile.builder()
                        .userExperience(userExperience)
                        .originalFilename("자기소개서 원본.pdf")
                        .fileType("application/pdf")
                        .fileSize(0L)
                        .filePath(resumeUrl)
                        .source("RESUME_ORIGINAL")
                        .build();
                userExperience.updateFiles(List.of(resumeFile));

                savedExperiences.add(experienceRepository.save(userExperience));
            }
        }

        // 5. 사용 완료된 임시 캐시 데이터 일괄 삭제
        tempRepository.deleteByUser(user);

        return new ExperienceStep2SaveResult(savedExperiences, mergeCandidates);
    }

    /**
     * 2차 추출에서 중복 후보로 보류된 draft 경험을 사용자 결정에 따라 후처리합니다.
     */
    @Transactional
    public ExperienceStep3Response confirmStep3(String email, ExperienceStep3Request request) {
        User user = userService.findByEmail(email);

        List<UserExperienceResponse> savedExperiences = new ArrayList<>();
        int skippedCount = 0;

        for (ExperienceStep3Request.Decision decision : request.getDecisions()) {
            if (decision.getAction() == ExperienceStep3Action.SKIP) {
                skippedCount++;
                continue;
            }

            UserExperience experience = UserExperience.builder()
                    .user(user)
                    .title(decision.getDraft().getTitle())
                    .experienceGroup(decision.getDraft().getExperienceGroup())
                    .experienceType(decision.getDraft().getExperienceType())
                    .status(decision.getDraft().getStatus() != null ? decision.getDraft().getStatus() : Status.COMPLETED)
                    .documentContent(decision.getDraft().getDocumentContent())
                    .attributes(decision.getDraft().getAttributes() != null ? decision.getDraft().getAttributes() : new HashMap<>())
                    .keywords(decision.getDraft().getKeywords() != null ? decision.getDraft().getKeywords() : new ArrayList<>())
                    .build();

            savedExperiences.add(new UserExperienceResponse(experienceRepository.save(experience)));
        }

        return new ExperienceStep3Response(savedExperiences, skippedCount);
    }

    private ExperienceGroup convertGroup(String koreanGroup) {
        if ("상세 서술형".equals(koreanGroup)) {
            return ExperienceGroup.NARRATIVE;
        }
        return ExperienceGroup.SPEC;
    }

    private ExperienceType convertType(String koreanType) {
        try {
            return ExperienceType.fromKoreanName(koreanType);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown experience type '{}', fallback to PROJECT", koreanType);
            return ExperienceType.PROJECT;
        }
    }
}
