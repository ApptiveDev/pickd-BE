package back.pickd.experience.service;

import back.pickd.experience.dto.ExperienceCreateDto.Request;
import back.pickd.experience.dto.ExperienceCreateDto.Response;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.experience.support.PresetRegistry;
import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserExperienceServiceTest {

    @Mock
    private UserExperienceRepository userExperienceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperienceMergeService experienceMergeService;

    @Mock
    private PresetRegistry presetRegistry;

    @InjectMocks
    private UserExperienceService userExperienceService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createExperienceNormalizesAttributesBeforeSaving() throws Exception {
        User user = createUser();
        Request request = objectMapper.readValue("""
                {
                  "title": "Pickd",
                  "experienceType": "PROJECT",
                  "experienceGroup": "NARRATIVE",
                  "status": "COMPLETED",
                  "attributes": {
                    "프로젝트명": "Pickd",
                    "진행 기간": "2026.01 ~ 2026.06"
                  },
                  "keywords": ["백엔드"],
                  "links": [],
                  "forceCreate": true
                }
                """, Request.class);
        Map<String, Object> normalized = Map.of(
                "project_name", "Pickd",
                "period", "2026.01 ~ 2026.06"
        );

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(presetRegistry.normalizeAttributes(
                ExperienceType.PROJECT,
                request.getAttributes()
        )).thenReturn(normalized);
        when(userExperienceRepository.save(any(UserExperience.class)))
                .thenReturn(UserExperience.builder().id("exp-new").build());

        Response response =
                userExperienceService.createExperience("user@example.com", request);

        ArgumentCaptor<UserExperience> captor =
                ArgumentCaptor.forClass(UserExperience.class);
        verify(userExperienceRepository).save(captor.capture());
        UserExperience saved = captor.getValue();

        assertEquals(normalized, saved.getAttributes());
        assertEquals("Pickd", saved.getTitle());
        assertEquals(ExperienceType.PROJECT, saved.getExperienceType());
        assertEquals(ExperienceGroup.NARRATIVE, saved.getExperienceGroup());
        assertEquals(Status.COMPLETED, saved.getStatus());
        assertEquals("exp-new", response.getId());
        verify(presetRegistry).normalizeAttributes(
                eq(ExperienceType.PROJECT),
                eq(request.getAttributes())
        );
    }

    @Test
    void getExperienceReturnsOwnedExperience() {
        User user = createUser();
        UserExperience experience = createExperience(user, "exp-1");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userExperienceRepository.findByIdAndUser("exp-1", user))
                .thenReturn(Optional.of(experience));

        ExperienceResponse response =
                userExperienceService.getExperience("user@example.com", "exp-1");

        assertEquals("exp-1", response.getId());
        assertEquals("Pickd", response.getTitle());
        assertEquals("PROJECT", response.getExperienceType());
    }

    @Test
    void getExperiencesUsesTypeAndGroupFilters() {
        User user = createUser();
        UserExperience experience = createExperience(user, "exp-1");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userExperienceRepository.findByUserWithFilters(
                user,
                ExperienceType.PROJECT,
                ExperienceGroup.NARRATIVE
        )).thenReturn(List.of(experience));

        List<ExperienceResponse> responses = userExperienceService.getExperiences(
                "user@example.com",
                ExperienceType.PROJECT,
                ExperienceGroup.NARRATIVE
        );

        assertEquals(1, responses.size());
        assertEquals("exp-1", responses.get(0).getId());
        verify(userExperienceRepository).findByUserWithFilters(
                user,
                ExperienceType.PROJECT,
                ExperienceGroup.NARRATIVE
        );
    }

    @Test
    void updateExperienceUpdatesOwnedExperience() throws Exception {
        User user = createUser();
        UserExperience experience = createExperience(user, "exp-1");
        Request request = objectMapper.readValue("""
                {
                  "title": "Pickd 개선",
                  "experienceType": "PROJECT",
                  "experienceGroup": "NARRATIVE",
                  "status": "IN_PROGRESS",
                  "documentContent": "경험 수정 API를 구현했습니다.",
                  "attributes": {
                    "프로젝트명": "Pickd 개선"
                  },
                  "keywords": ["API"],
                  "links": []
                }
                """, Request.class);
        Map<String, Object> normalized = Map.of("project_name", "Pickd 개선");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userExperienceRepository.findByIdAndUser("exp-1", user))
                .thenReturn(Optional.of(experience));
        when(presetRegistry.normalizeAttributes(
                ExperienceType.PROJECT,
                request.getAttributes()
        )).thenReturn(normalized);

        ExperienceResponse response =
                userExperienceService.updateExperience("user@example.com", "exp-1", request);

        assertEquals("Pickd 개선", response.getTitle());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals("경험 수정 API를 구현했습니다.", response.getDocumentContent());
        assertEquals(normalized, response.getAttributes());
        verify(presetRegistry).normalizeAttributes(
                eq(ExperienceType.PROJECT),
                eq(request.getAttributes())
        );
    }

    @Test
    void deleteExperienceDeletesOwnedExperience() {
        User user = createUser();
        UserExperience experience = createExperience(user, "exp-1");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userExperienceRepository.findByIdAndUser("exp-1", user))
                .thenReturn(Optional.of(experience));

        userExperienceService.deleteExperience("user@example.com", "exp-1");

        verify(userExperienceRepository).delete(experience);
    }

    private User createUser() {
        return User.builder()
                .email("user@example.com")
                .name("테스트")
                .build();
    }

    private UserExperience createExperience(User user, String id) {
        return UserExperience.builder()
                .id(id)
                .user(user)
                .title("Pickd")
                .experienceType(ExperienceType.PROJECT)
                .experienceGroup(ExperienceGroup.NARRATIVE)
                .status(Status.COMPLETED)
                .attributes(Map.of("project_name", "Pickd"))
                .keywords(List.of("백엔드"))
                .build();
    }
}
