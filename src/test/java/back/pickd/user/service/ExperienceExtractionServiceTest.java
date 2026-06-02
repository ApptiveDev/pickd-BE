package back.pickd.user.service;

import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.dto.ExperienceStep3Request;
import back.pickd.user.dto.ExperienceStep3Response;
import back.pickd.user.entity.User;
import back.pickd.user.entity.UserExperience;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import back.pickd.user.repository.ExperienceTempRepository;
import back.pickd.user.repository.UserExperienceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperienceExtractionServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private S3Service s3Service;

    @Mock
    private ExperienceTempRepository tempRepository;

    @Mock
    private UserExperienceRepository experienceRepository;

    @Mock
    private UserService userService;

    @Mock
    private ExperienceMergeService experienceMergeService;

    @InjectMocks
    private ExperienceExtractionService experienceExtractionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void confirmStep3CreatesSelectedDraftAndSkipsIgnoredDraft() throws Exception {
        User user = User.builder()
                .email("user@example.com")
                .name("테스트")
                .build();
        String requestJson = """
                {
                  "decisions": [
                    {
                      "action": "CREATE_NEW",
                      "draft": {
                        "title": "FIn-agent",
                        "experienceType": "PROJECT",
                        "experienceGroup": "NARRATIVE",
                        "status": "COMPLETED",
                        "documentContent": "미래에셋 AI Agent 프로젝트에서 데이터 전처리와 툴 개발을 담당했습니다.",
                        "attributes": {
                          "project_name": "FIn-agent",
                          "organization": "미래에셋",
                          "period": "2025.06.31 ~07.31"
                        },
                        "keywords": ["문제 해결", "분석력", "실행력"]
                      }
                    },
                    {
                      "action": "SKIP",
                      "draft": {
                        "title": "중복으로 저장하지 않을 경험",
                        "experienceType": "PROJECT",
                        "experienceGroup": "NARRATIVE",
                        "status": "COMPLETED",
                        "documentContent": "저장하지 않는 후보입니다.",
                        "attributes": {},
                        "keywords": []
                      }
                    }
                  ]
                }
                """;
        ExperienceStep3Request request = objectMapper.readValue(requestJson, ExperienceStep3Request.class);

        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(experienceRepository.save(any(UserExperience.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExperienceStep3Response response = experienceExtractionService.confirmStep3("user@example.com", request);

        assertEquals(1, response.getSavedExperiences().size());
        assertEquals(1, response.getSkippedCount());

        ArgumentCaptor<UserExperience> captor = ArgumentCaptor.forClass(UserExperience.class);
        verify(experienceRepository, times(1)).save(captor.capture());

        UserExperience saved = captor.getValue();
        assertEquals("FIn-agent", saved.getTitle());
        assertEquals(ExperienceType.PROJECT, saved.getExperienceType());
        assertEquals(ExperienceGroup.NARRATIVE, saved.getExperienceGroup());
        assertEquals(Status.COMPLETED, saved.getStatus());
        assertEquals("미래에셋", saved.getAttributes().get("organization"));
        assertEquals(List.of("문제 해결", "분석력", "실행력"), saved.getKeywords());
    }
}
