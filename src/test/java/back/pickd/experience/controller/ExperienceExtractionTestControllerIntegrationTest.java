package back.pickd.experience.controller;

import back.pickd.auth.jwt.JwtTokenProvider;
import back.pickd.experience.entity.ExperienceDuplicateGroup;
import back.pickd.experience.entity.ExperienceExtractionBatch;
import back.pickd.experience.entity.ExperienceExtractionDraft;
import back.pickd.experience.entity.ExperienceTemp;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.dto.ExperienceExtractionTestDto.PresetDefinition;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.ExperienceExtractionBatchRepository;
import back.pickd.experience.repository.ExperienceTempRepository;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ExperienceExtractionTestControllerIntegrationTest {

    private static final String OWNER_EMAIL = "test-owner@example.com";
    private static final String OTHER_EMAIL = "test-other@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserExperienceRepository experienceRepository;

    @Autowired
    private ExperienceTempRepository tempRepository;

    @Autowired
    private ExperienceExtractionBatchRepository batchRepository;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private AiClient aiClient;

    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        clearDatabase();
        owner = userRepository.save(User.builder()
                .email(OWNER_EMAIL)
                .name("테스트 소유자")
                .build());
        other = userRepository.save(User.builder()
                .email(OTHER_EMAIL)
                .name("다른 사용자")
                .build());
    }

    @AfterEach
    void tearDown() {
        clearDatabase();
    }

    @Test
    void localPageIsPublicAndProvidesAllPresetDefinitions() throws Exception {
        MvcResult result = mockMvc.perform(get("/experience-extraction-test"))
                .andExpect(status().isOk())
                .andExpect(view().name("experience-extraction-test"))
                .andExpect(model().attribute("authenticated", false))
                .andExpect(model().attribute("presetDefinitions", hasSize(13)))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("project_name")
                ))
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("미처리 중복 불러오기")
                ))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<PresetDefinition> presets = (List<PresetDefinition>) result
                .getModelAndView()
                .getModel()
                .get("presetDefinitions");
        PresetDefinition research = presets.stream()
                .filter(preset -> "RESEARCH".equals(preset.type()))
                .findFirst()
                .orElseThrow();

        assertEquals("학부연구생", research.koreanName());
        assertEquals("NARRATIVE", research.group());
        assertTrue(research.fields().stream()
                .anyMatch(field -> "lab_name".equals(field.key())));
    }

    @Test
    void stateEndpointReturnsOnlyAuthenticatedUsersData() throws Exception {
        UserExperience ownerExperience = experienceRepository.save(
                experience(owner, "소유자 프로젝트")
        );
        experienceRepository.save(experience(other, "타 사용자 프로젝트"));
        tempRepository.save(temp(owner, "소유자 Temp"));
        tempRepository.save(temp(other, "타 사용자 Temp"));

        ExperienceExtractionBatch ownerBatch =
                new ExperienceExtractionBatch(owner);
        ExperienceDuplicateGroup ownerGroup =
                new ExperienceDuplicateGroup(ownerExperience);
        ownerGroup.addDraft(draft("소유자 Draft"));
        ownerBatch.addGroup(ownerGroup);
        batchRepository.saveAndFlush(ownerBatch);

        UserExperience otherExperience = experienceRepository.findByUserOrderByCreatedAtDesc(other)
                .get(0);
        ExperienceExtractionBatch otherBatch =
                new ExperienceExtractionBatch(other);
        ExperienceDuplicateGroup otherGroup =
                new ExperienceDuplicateGroup(otherExperience);
        otherGroup.addDraft(draft("타 사용자 Draft"));
        otherBatch.addGroup(otherGroup);
        batchRepository.saveAndFlush(otherBatch);

        mockMvc.perform(get("/internal/experience-extraction-test/state")
                        .cookie(accessToken(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value(OWNER_EMAIL))
                .andExpect(jsonPath("$.experiences", hasSize(1)))
                .andExpect(jsonPath("$.experiences[*].title",
                        hasItem("소유자 프로젝트")))
                .andExpect(jsonPath("$.temps", hasSize(1)))
                .andExpect(jsonPath("$.temps[0].experienceName")
                        .value("소유자 Temp"))
                .andExpect(jsonPath("$.batches", hasSize(1)))
                .andExpect(jsonPath("$.batches[0].duplicateGroups", hasSize(1)))
                .andExpect(jsonPath(
                        "$.batches[0].duplicateGroups[0].existingExperience.title"
                ).value("소유자 프로젝트"))
                .andExpect(jsonPath(
                        "$.batches[0].duplicateGroups[0].drafts[0].title"
                ).value("소유자 Draft"))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString("타 사용자")
                        )
                ));
    }

    private UserExperience experience(User user, String title) {
        return UserExperience.builder()
                .user(user)
                .title(title)
                .experienceType(ExperienceType.PROJECT)
                .experienceGroup(ExperienceGroup.NARRATIVE)
                .status(Status.COMPLETED)
                .documentContent(title + " 본문")
                .attributes(Map.of("project_name", title))
                .keywords(List.of("문제 해결"))
                .build();
    }

    private ExperienceTemp temp(User user, String name) {
        return ExperienceTemp.builder()
                .user(user)
                .experienceName(name)
                .experienceGroup(ExperienceGroup.NARRATIVE)
                .experienceType(ExperienceType.PROJECT)
                .resumeUrl("https://cdn.example.com/" + name + ".pdf")
                .build();
    }

    private ExperienceExtractionDraft draft(String title) {
        return ExperienceExtractionDraft.builder()
                .title(title)
                .experienceType(ExperienceType.PROJECT)
                .experienceGroup(ExperienceGroup.NARRATIVE)
                .status(Status.COMPLETED)
                .documentContent(title + " 본문")
                .attributes(Map.of("project_name", title))
                .keywords(List.of("협업"))
                .resumeUrl("https://cdn.example.com/resume.pdf")
                .similarity(0.91)
                .build();
    }

    private Cookie accessToken(User user) {
        String token = jwtTokenProvider.createToken(
                user.getEmail(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return new Cookie("accessToken", token);
    }

    private void clearDatabase() {
        batchRepository.deleteAll();
        batchRepository.flush();
        tempRepository.deleteAll();
        tempRepository.flush();
        experienceRepository.deleteAll();
        experienceRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
    }
}
