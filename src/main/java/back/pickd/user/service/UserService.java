package back.pickd.user.service;

import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Save or Update user info from Google OAuth2 attributes
     */
        @Transactional
        public User saveOrUpdate(String email, String name, String picture) {
            User user = userRepository.findByEmail(email)
                    .map(entity -> entity.update(name, picture))
                    .orElse(User.builder()
                            .email(email)
                            .name(name)
                            .picture(picture)
                            .onboardingStep(back.pickd.user.entity.enums.OnboardingStep.NONE)
                            .build());
    
            return userRepository.save(user);
        }
    
        @Transactional(readOnly = true)
        public User findByEmail(String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. email: " + email));
        }
    }
    