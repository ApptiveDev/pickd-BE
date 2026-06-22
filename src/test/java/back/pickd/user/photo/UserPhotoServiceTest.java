package back.pickd.user.photo;

import back.pickd.global.infra.s3.FileUploadType;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPhotoServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserPhotoRepository userPhotoRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserPhotoService userPhotoService;

    @Test
    void uploadPhotoSavesPhotoAndUpdatesRepresentative() {
        User user = createUser();
        MockMultipartFile file = createFile();
        UserPhoto currentRepresentative = createPhoto(user, 1L, "old-url", true);
        String imageUrl = "https://cdn.example.com/user/profile/1/new.png";
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(s3Service.uploadFile(file, FileUploadType.PROFILE, user.getId())).thenReturn(imageUrl);
        when(userPhotoRepository.findByUserAndIsRepresentativeTrue(user))
                .thenReturn(Optional.of(currentRepresentative));
        when(userPhotoRepository.save(any(UserPhoto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPhotoResponse response =
                userPhotoService.uploadPhoto("user@example.com", file, true, true);

        assertEquals(imageUrl, response.imageUrl());
        assertEquals("profile.png", response.originalFilename());
        assertTrue(response.isPublic());
        assertTrue(response.isRepresentative());
        assertFalse(currentRepresentative.isRepresentative());
        assertEquals(imageUrl, user.getPicture());
        verify(userPhotoRepository).save(any(UserPhoto.class));
    }

    @Test
    void getPhotosReturnsUserPhotos() {
        User user = createUser();
        UserPhoto photo = createPhoto(user, 1L, "image-url", false);
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(userPhotoRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(photo));

        List<UserPhotoResponse> responses = userPhotoService.getPhotos("user@example.com");

        assertEquals(1, responses.size());
        assertEquals("image-url", responses.get(0).imageUrl());
    }

    @Test
    void updateRepresentativeChangesRepresentativeAndUserPicture() {
        User user = createUser();
        UserPhoto currentRepresentative = createPhoto(user, 1L, "old-url", true);
        UserPhoto targetPhoto = createPhoto(user, 2L, "new-url", false);
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(userPhotoRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(targetPhoto));
        when(userPhotoRepository.findByUserAndIsRepresentativeTrue(user))
                .thenReturn(Optional.of(currentRepresentative));

        UserPhotoResponse response =
                userPhotoService.updateRepresentative("user@example.com", 2L);

        assertFalse(currentRepresentative.isRepresentative());
        assertTrue(targetPhoto.isRepresentative());
        assertEquals("new-url", user.getPicture());
        assertEquals("new-url", response.imageUrl());
    }

    private User createUser() {
        return User.builder().email("user@example.com").name("테스트").build();
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile("file", "profile.png", "image/png", "image".getBytes());
    }

    private UserPhoto createPhoto(User user, Long id, String imageUrl, boolean isRepresentative) {
        return UserPhoto.builder()
                .id(id)
                .user(user)
                .imageUrl(imageUrl)
                .originalFilename("profile.png")
                .isPublic(false)
                .isRepresentative(isRepresentative)
                .build();
    }
}
