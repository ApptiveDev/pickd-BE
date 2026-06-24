package back.pickd.experience.service;

import back.pickd.experience.dto.ExperienceExtractionTestDto.BatchState;
import back.pickd.experience.dto.ExperienceExtractionTestDto.StateResponse;
import back.pickd.experience.dto.ExperienceExtractionTestDto.TempState;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.repository.ExperienceExtractionBatchRepository;
import back.pickd.experience.repository.ExperienceTempRepository;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("local")
@RequiredArgsConstructor
public class ExperienceExtractionTestService {

    private final UserRepository userRepository;
    private final UserExperienceRepository experienceRepository;
    private final ExperienceTempRepository tempRepository;
    private final ExperienceExtractionBatchRepository batchRepository;

    @Transactional(readOnly = true)
    public StateResponse getState(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new StateResponse(
                email,
                experienceRepository.findByUserOrderByCreatedAtDesc(user).stream()
                        .map(ExperienceResponse::new)
                        .toList(),
                tempRepository.findByUser(user).stream()
                        .map(TempState::from)
                        .toList(),
                batchRepository.findByUserOrderByCreatedAtDesc(user).stream()
                        .map(BatchState::from)
                        .toList()
        );
    }
}
