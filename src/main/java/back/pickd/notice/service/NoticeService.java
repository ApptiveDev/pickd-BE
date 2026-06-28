package back.pickd.notice.service;

import back.pickd.application.entity.Application;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.*;
import back.pickd.notice.dto.NoticeSaveRequestDto;
import back.pickd.notice.dto.NoticeSectionRequestDto;
import back.pickd.notice.dto.response.*;
import back.pickd.coverletter.repository.CoverLetterItemRepository;
import back.pickd.notice.entity.*;
import back.pickd.notice.enums.*;
import back.pickd.notice.repository.*;
import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 채용공고 AI 분석 결과를 DB에 저장하는 서비스 레이어
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final AiClient aiClient;
    private final NoticeRepository noticeRepository;
    private final NoticeSectionRepository noticeSectionRepository;
    private final SectionQualificationRepository sectionQualificationRepository;
    private final SectionPreferenceRepository sectionPreferenceRepository;
    private final NoticeProcessRepository noticeProcessRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CoverLetterItemRepository coverLetterItemRepository;

    // URL 채용공고 분석 후 저장
    @Transactional
    public Long analyzeAndSaveNoticeUrl(String email, String url) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        AiJobPostingResponse aiResponse = aiClient.analyzeNoticeUrl(url);
        return saveNotice(user, aiResponse, url);
    }

    // 이미지 채용공고 분석 후 저장
    @Transactional
    public Long analyzeAndSaveNoticeImages(String email, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일은 최소 1개 이상 필요합니다.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        AiJobPostingResponse aiResponse = aiClient.analyzeNoticeImages(files);
        return saveNotice(user, aiResponse, null);
    }

    // PDF 채용공고 분석 후 저장
    @Transactional
    public Long analyzeAndSaveNoticePdf(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF 파일은 필수입니다.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        AiJobPostingResponse aiResponse = aiClient.analyzeNoticePdf(file);
        return saveNotice(user, aiResponse, null);
    }

    // 공고 및 하위 연관 엔티티(모집부문, 전형단계, 서류 등) 일괄 영속화
    private Long saveNotice(User user, AiJobPostingResponse aiResponse, String url) {
        Notice notice = Notice.builder()
                .user(user)
                .companyName(aiResponse.getCompanyName())
                .noticeName(aiResponse.getNoticeName())
                .category(convertJobCategory(aiResponse.getCategory()))
                .startedAt(aiResponse.getStartedAt())
                .endedAt(aiResponse.getEndedAt())
                .employmentType(convertEmploymentType(aiResponse.getEmploymentType()))
                .headcount(aiResponse.getHeadcount())
                .region1depth(aiResponse.getRegion1depth())
                .workplaceAddress(aiResponse.getWorkplaceAddress())
                .noticeUrl(url != null ? url : aiResponse.getNoticeUrl())
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        if (aiResponse.getSections() != null) {
            for (AiNoticeSectionDto sectionDto : aiResponse.getSections()) {
                NoticeSection section = NoticeSection.builder()
                        .notice(savedNotice)
                        .sectionName(sectionDto.getSectionName())
                        .jobTitle(sectionDto.getJobTitle())
                        .responsibilities(sectionDto.getResponsibilities())
                        .headcount(sectionDto.getHeadcount())
                        .workplace(sectionDto.getWorkplace())
                        .build();

                NoticeSection savedSection = noticeSectionRepository.save(section);
                savedNotice.addSection(savedSection);

                if (sectionDto.getQualifications() != null) {
                    for (AiSectionQualificationDto qualDto : sectionDto.getQualifications()) {
                        SectionQualification qualification = SectionQualification.builder()
                                .section(savedSection)
                                .generalQualification(qualDto.getGeneralQualification())
                                .mandatoryQualification(qualDto.getMandatoryQualification())
                                .build();
                        sectionQualificationRepository.save(qualification);
                    }
                }

                if (sectionDto.getPreferences() != null) {
                    for (AiSectionPreferenceDto prefDto : sectionDto.getPreferences()) {
                        SectionPreference preference = SectionPreference.builder()
                                .section(savedSection)
                                .generalPreference(prefDto.getGeneralPreference())
                                .additionalPoints(prefDto.getAdditionalPoints())
                                .veteranPreference(prefDto.getVeteranPreference())
                                .disabilityPreference(prefDto.getDisabilityPreference())
                                .certificatePreference(prefDto.getCertificatePreference())
                                .build();
                        sectionPreferenceRepository.save(preference);
                    }
                }
            }
        }

        if (aiResponse.getProcesses() != null) {
            for (AiNoticeProcessDto processDto : aiResponse.getProcesses()) {
                NoticeProcess process = NoticeProcess.builder()
                        .notice(savedNotice)
                        .processName(processDto.getProcessName())
                        .documentScreenSchedule(processDto.getDocumentScreenSchedule())
                        .writtenExamSchedule(processDto.getWrittenExamSchedule())
                        .interviewSchedule(processDto.getInterviewSchedule())
                        .joinDate(processDto.getJoinDate())
                        .applicationPeriod(processDto.getApplicationPeriod())
                        .scheduleNotes(processDto.getScheduleNotes())
                        .build();
                noticeProcessRepository.save(process);
            }
        }

        if (aiResponse.getDocuments() != null) {
            for (AiApplicationDocumentDto docDto : aiResponse.getDocuments()) {
                ApplicationDocument document = ApplicationDocument.builder()
                        .notice(savedNotice)
                        .mandatoryDocuments(docDto.getMandatoryDocuments())
                        .proofDocuments(docDto.getProofDocuments())
                        .applyMethod(docDto.getApplyMethod())
                        .applyUrlOrEmail(docDto.getApplyUrlOrEmail())
                        .submissionNotes(docDto.getSubmissionNotes())
                        .build();
                applicationDocumentRepository.save(document);
            }
        }

        // AI 분석 공고 저장 후 Application 자동 생성
        Application application = Application.builder()
                .user(user)
                .notice(savedNotice)
                .company(savedNotice.getCompanyName())
                .jobTitle(savedNotice.getNoticeName())
                .status(ApplicationStatus.PREPARING)
                .build();
        applicationRepository.save(application);

        return savedNotice.getId();
    }

    // ── 공고 조회 API ──────────────────────────────────────────────────────────

    /** 로그인 사용자의 전체 공고 목록 조회 (최신순) */
    public List<NoticeListResponse> getNotices(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return noticeRepository.findAllByUserOrderByIdDesc(user).stream()
                .map(NoticeListResponse::new)
                .toList();
    }

    /** 공고 상세 조회 (sections + qualifications + preferences + processes + documents) */
    public NoticeDetailResponse getNoticeDetail(String email, Long noticeId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Notice notice = noticeRepository.findByIdAndUser(noticeId, user)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        List<NoticeSectionResponse> sectionResponses = notice.getSections().stream()
                .map(section -> {
                    List<SectionQualification> quals = sectionQualificationRepository.findAllBySection(section);
                    List<SectionPreference> prefs = sectionPreferenceRepository.findAllBySection(section);
                    return new NoticeSectionResponse(section, quals, prefs);
                })
                .toList();

        List<NoticeProcessResponse> processResponses = noticeProcessRepository.findAllByNotice(notice).stream()
                .map(NoticeProcessResponse::new)
                .toList();

        List<ApplicationDocumentResponse> documentResponses = applicationDocumentRepository.findAllByNotice(notice).stream()
                .map(ApplicationDocumentResponse::new)
                .toList();

        List<CoverLetterItemResponse> coverLetterResponses = coverLetterItemRepository
                .findAllByNoticeIdAndUserOrderByOrderIndexAsc(notice.getId(), user).stream()
                .map(CoverLetterItemResponse::new)
                .toList();

        return new NoticeDetailResponse(notice, sectionResponses, processResponses, documentResponses, coverLetterResponses);
    }

    // ── 수기 입력 Notice 수정 API ──────────────────────────────────────────────

    /** 수기 입력으로 생성된 Notice의 기본 정보를 수정합니다. */
    @Transactional
    public void updateNotice(String email, Long noticeId, NoticeSaveRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Notice notice = noticeRepository.findByIdAndUser(noticeId, user)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        notice.update(
                dto.getCompanyName(), dto.getNoticeName(), dto.getCategory(),
                dto.getStartedAt(), dto.getEndedAt(), dto.getEmploymentType(),
                dto.getHeadcount(), dto.getRegion1depth(), dto.getWorkplaceAddress(), dto.getNoticeUrl()
        );
    }

    /** Notice에 모집부문(Section)을 추가합니다. */
    @Transactional
    public Long addSectionToNotice(String email, Long noticeId, NoticeSectionRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Notice notice = noticeRepository.findByIdAndUser(noticeId, user)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        NoticeSection section = NoticeSection.builder()
                .notice(notice)
                .sectionName(dto.getSectionName())
                .jobTitle(dto.getJobTitle())
                .responsibilities(dto.getResponsibilities())
                .headcount(dto.getHeadcount())
                .workplace(dto.getWorkplace())
                .build();

        NoticeSection saved = noticeSectionRepository.save(section);
        notice.addSection(saved);
        return saved.getId();
    }

    // 채용 구분 Enum 변환 및 방어 처리
    private JobCategory convertJobCategory(String category) {
        if (category == null) {
            return JobCategory.FULL_TIME;
        }
        try {
            return JobCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return JobCategory.FULL_TIME;
        }
    }

    // 고용 형태 Enum 변환 및 방어 처리
    private EmploymentType convertEmploymentType(String type) {
        if (type == null) {
            return EmploymentType.OTHER;
        }
        try {
            return EmploymentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (EmploymentType et : EmploymentType.values()) {
                if (et.getDescription().equals(type) || et.name().equalsIgnoreCase(type)) {
                    return et;
                }
            }
            return EmploymentType.OTHER;
        }
    }
}
