package back.pickd.user.service;

import back.pickd.experience.entity.ExperienceDuplicateGroup;
import back.pickd.experience.entity.ExperienceExtractionBatch;
import back.pickd.experience.entity.ExperienceExtractionDraft;
import back.pickd.experience.entity.ExperienceFile;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.ExperienceExtractionBatchRepository;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.dto.onboarding.OnboardingRequest;
import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class OnboardingServiceIntegrationTest {

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserExperienceRepository experienceRepository;

    @Autowired
    private ExperienceExtractionBatchRepository batchRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private AiClient aiClient;

    private User user;

    @BeforeEach
    void setUp() {
        clearDatabase();
        user = userRepository.save(User.builder()
                .email("onboarding@example.com")
                .name("온보딩 사용자")
                .build());
    }

    @AfterEach
    void tearDown() {
        clearDatabase();
    }

    @Test
    void replacingOnboardingExperiencesDeletesFilesAndPendingDraftsFirst()
            throws Exception {
        UserExperience existing = UserExperience.builder()
                .user(user)
                .title("기존 경험")
                .experienceType(ExperienceType.PROJECT)
                .experienceGroup(ExperienceGroup.NARRATIVE)
                .status(Status.COMPLETED)
                .documentContent("기존 본문")
                .attributes(Map.of("project_name", "기존 경험"))
                .keywords(List.of("기존"))
                .build();
        existing.updateFiles(List.of(ExperienceFile.builder()
                .originalFilename("resume.pdf")
                .fileType("application/pdf")
                .fileSize(100L)
                .filePath("https://cdn.example.com/resume.pdf")
                .source("RESUME_ORIGINAL")
                .build()));
        existing = experienceRepository.saveAndFlush(existing);

        ExperienceExtractionBatch batch = new ExperienceExtractionBatch(user);
        ExperienceDuplicateGroup group = new ExperienceDuplicateGroup(existing);
        group.addDraft(ExperienceExtractionDraft.builder()
                .title("중복 Draft")
                .experienceType(ExperienceType.PROJECT)
                .experienceGroup(ExperienceGroup.NARRATIVE)
                .status(Status.COMPLETED)
                .documentContent("Draft 본문")
                .attributes(Map.of("project_name", "중복 Draft"))
                .keywords(List.of("중복"))
                .resumeUrl("https://cdn.example.com/resume.pdf")
                .similarity(0.92)
                .build());
        batch.addGroup(group);
        batchRepository.saveAndFlush(batch);

        String existingId = existing.getId();
        OnboardingRequest request = objectMapper.readValue("""
                {
                  "targetPeriod": "2026 하반기",
                  "currentStage": "서류 준비",
                  "focusItems": ["자기소개서"],
                  "hasResume": true,
                  "hasBaseEssay": false,
                  "hasPortfolio": false,
                  "experiences": [
                    {
                      "type": "PROJECT",
                      "title": "새 온보딩 경험",
                      "startDate": "2026-01",
                      "endDate": "2026-03"
                    }
                  ]
                }
                """, OnboardingRequest.class);

        onboardingService.updateOnboarding(user.getEmail(), request);

        assertFalse(experienceRepository.findById(existingId).isPresent());
        assertEquals(0, count("experience_files"));
        assertEquals(0, count("experience_extraction_batches"));
        assertEquals(0, count("experience_duplicate_groups"));
        assertEquals(0, count("experience_extraction_drafts"));
        assertEquals(
                List.of("새 온보딩 경험"),
                experienceRepository.findByUserOrderByCreatedAtDesc(user).stream()
                        .map(UserExperience::getTitle)
                        .toList()
        );
    }

    private int count(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from " + tableName,
                Integer.class
        );
        return count != null ? count : 0;
    }

    private void clearDatabase() {
        batchRepository.deleteAll();
        batchRepository.flush();
        experienceRepository.deleteAll();
        experienceRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
    }
}
