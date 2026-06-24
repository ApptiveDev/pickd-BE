package back.pickd.experience.service;

import back.pickd.experience.dto.ExperienceExtractionDto.Step3Request;
import back.pickd.experience.dto.ExperienceExtractionDto.Step3Response;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.ExperienceTempRepository;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.experience.support.ExperienceConversionUtils;
import back.pickd.experience.support.PresetRegistry;
import back.pickd.global.infra.ai.AiClient;
import back.pickd.global.infra.ai.dto.AiStep1Response;
import back.pickd.global.infra.s3.FileUploadType;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private PresetRegistry presetRegistry;

    @Mock
    private ExperienceConversionUtils conversionUtils;

    @InjectMocks
    private ExperienceExtractionService experienceExtractionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractStep1RejectsUnsupportedExperienceType() throws Exception {
        User user = User.builder()
                .email("user@example.com")
                .name("테스트")
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[]{1}
        );
        AiStep1Response aiResponse = objectMapper.readValue("""
                {
                  "experiences": [
                    {
                      "experience_name": "지원하지 않는 경험",
                      "experience_group": "상세 서술형",
                      "experience_type": "창업"
                    }
                  ]
                }
                """, AiStep1Response.class);

        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(s3Service.uploadFile(file, FileUploadType.TEMP_RESUME, null))
                .thenReturn("https://cdn/resume.pdf");
        when(aiClient.extractStep1(file)).thenReturn(aiResponse);
        when(conversionUtils.convertType("창업"))
                .thenThrow(new IllegalArgumentException("지원하지 않는 경험 유형입니다: 창업"));

        assertThrows(
                IllegalArgumentException.class,
                () -> experienceExtractionService.extractStep1("user@example.com", file)
        );
        verify(tempRepository, times(0)).save(any());
    }

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
        Step3Request request = objectMapper.readValue(requestJson, Step3Request.class);

        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(presetRegistry.normalizeAttributes(any(ExperienceType.class), any())).thenAnswer(invocation -> invocation.getArgument(1));
        when(experienceRepository.save(any(UserExperience.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Step3Response response = experienceExtractionService.confirmStep3("user@example.com", request);

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
        verify(presetRegistry).normalizeAttributes(eq(ExperienceType.PROJECT), any());
    }
}
