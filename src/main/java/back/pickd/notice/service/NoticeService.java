package back.pickd.notice.service;

import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.*;
import back.pickd.notice.enums.EmploymentType;
import back.pickd.notice.enums.JobCategory;
import back.pickd.notice.notice.Notice;
import back.pickd.notice.notice.NoticeRepository;
import back.pickd.notice.section.NoticeSection;
import back.pickd.notice.section.NoticeSectionRepository;
import back.pickd.notice.qualification.SectionQualification;
import back.pickd.notice.qualification.SectionQualificationRepository;
import back.pickd.notice.preference.SectionPreference;
import back.pickd.notice.preference.SectionPreferenceRepository;
import back.pickd.notice.process.NoticeProcess;
import back.pickd.notice.process.NoticeProcessRepository;
import back.pickd.notice.document.ApplicationDocument;
import back.pickd.notice.document.ApplicationDocumentRepository;
import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final UserRepository userRepository;

    // URL 채용공고 분석 후 저장
    @Transactional
    public Long analyzeAndSaveNoticeUrl(String email, String url) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        AiJobPostingResponse aiResponse = aiClient.analyzeNoticeUrl(url);
        return saveNotice(user, aiResponse, url);
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
                .headcount(aiResponse.getHeadcount() != null ? String.valueOf(aiResponse.getHeadcount()) : "0")
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

        return savedNotice.getId();
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
