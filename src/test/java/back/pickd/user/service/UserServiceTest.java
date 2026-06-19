package back.pickd.user.service;

import back.pickd.global.infra.s3.FileUploadType;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.entity.User;
import back.pickd.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserService userService;

    @Test
    void updateProfileImageUploadsFileAndUpdatesPicture() {
        User user = User.builder()
                .email("user@example.com")
                .name("테스트")
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                "image".getBytes()
        );
        String imageUrl = "https://cdn.example.com/user/profile/1/profile.png";

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(s3Service.uploadFile(file, FileUploadType.PROFILE, user.getId()))
                .thenReturn(imageUrl);

        String result = userService.updateProfileImage("user@example.com", file);

        assertEquals(imageUrl, result);
        assertEquals(imageUrl, user.getPicture());
        verify(s3Service).uploadFile(file, FileUploadType.PROFILE, user.getId());
    }

    @Test
    void getProfileImageReturnsUserPicture() {
        User user = User.builder()
                .email("user@example.com")
                .name("테스트")
                .picture("https://cdn.example.com/user/profile/1/profile.png")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        String result = userService.getProfileImage("user@example.com");

        assertEquals("https://cdn.example.com/user/profile/1/profile.png", result);
    }
}
