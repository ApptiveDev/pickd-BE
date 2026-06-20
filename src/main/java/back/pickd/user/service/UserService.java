package back.pickd.user.service;

import back.pickd.global.infra.s3.FileUploadType;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.entity.User;
import back.pickd.user.entity.enums.OnboardingStep;
import back.pickd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

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
                        .onboardingStep(OnboardingStep.NONE)
                        .build());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. email: " + email));
    }

    @Transactional
    public String updateProfileImage(String email, MultipartFile file) {
        User user = findByEmail(email);
        String profileImageUrl = s3Service.uploadFile(file, FileUploadType.PROFILE, user.getId());
        user.updatePicture(profileImageUrl);
        return profileImageUrl;
    }

    @Transactional(readOnly = true)
    public String getProfileImage(String email) {
        return findByEmail(email).getPicture();
    }
}
