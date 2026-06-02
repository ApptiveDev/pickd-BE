package back.pickd.user.service;

import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.AiExperienceMergeCheckRequest;
import back.pickd.user.entity.User;
import back.pickd.user.entity.UserExperience;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import back.pickd.user.repository.UserExperienceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperienceMergeServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private UserExperienceRepository userExperienceRepository;

    @InjectMocks
    private ExperienceMergeService experienceMergeService;

    @Test
    void buildExistingExperiencePayloadsConvertsUserExperiencesForAiServer() {
        User user = User.builder()
                .email("user@example.com")
                .name("테스트")
                .build();
        UserExperience experience = UserExperience.builder()
                .id("exp-1")
                .user(user)
                .title("캡스톤 AI 프로젝트")
                .experienceGroup(ExperienceGroup.NARRATIVE)
                .experienceType(ExperienceType.PROJECT)
                .status(Status.COMPLETED)
                .documentContent("추천 모델을 개선했습니다.")
                .attributes(Map.of("role", "backend"))
                .keywords(List.of("문제 해결"))
                .build();
        when(userExperienceRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(experience));

        List<AiExperienceMergeCheckRequest.ExperiencePayload> payloads =
                experienceMergeService.buildExistingExperiencePayloads(user);

        assertEquals(1, payloads.size());
        AiExperienceMergeCheckRequest.ExperiencePayload payload = payloads.get(0);
        assertEquals("exp-1", payload.getId());
        assertEquals("캡스톤 AI 프로젝트", payload.getTitle());
        assertEquals("상세 서술형", payload.getExperienceGroup());
        assertEquals("프로젝트", payload.getExperienceType());
        assertEquals("추천 모델을 개선했습니다.", payload.getDocumentContent());
        assertEquals(List.of("문제 해결"), payload.getKeywords());
        assertEquals("backend", payload.getAttributes().get("role"));
    }
}
