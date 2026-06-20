package back.pickd.user.photo;

import back.pickd.global.infra.s3.FileUploadType;
import back.pickd.global.infra.s3.S3Service;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPhotoService {

    private final UserService userService;
    private final UserPhotoRepository userPhotoRepository;
    private final S3Service s3Service;

    @Transactional
    public UserPhotoResponse uploadPhoto(
            String email,
            MultipartFile file,
            boolean isPublic,
            boolean isRepresentative
    ) {
        User user = userService.findByEmail(email);
        String imageUrl = s3Service.uploadFile(file, FileUploadType.PROFILE, user.getId());

        if (isRepresentative) {
            clearRepresentative(user);
            user.updatePicture(imageUrl);
        }

        UserPhoto photo = UserPhoto.builder()
                .user(user)
                .imageUrl(imageUrl)
                .originalFilename(file.getOriginalFilename())
                .isPublic(isPublic)
                .isRepresentative(isRepresentative)
                .build();

        return new UserPhotoResponse(userPhotoRepository.save(photo));
    }

    @Transactional(readOnly = true)
    public List<UserPhotoResponse> getPhotos(String email) {
        User user = userService.findByEmail(email);
        return userPhotoRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(UserPhotoResponse::new)
                .toList();
    }

    @Transactional
    public UserPhotoResponse updateRepresentative(String email, Long photoId) {
        User user = userService.findByEmail(email);
        UserPhoto photo = userPhotoRepository.findByIdAndUser(photoId, user)
                .orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));

        clearRepresentative(user);
        photo.updateRepresentative(true);
        user.updatePicture(photo.getImageUrl());

        return new UserPhotoResponse(photo);
    }

    private void clearRepresentative(User user) {
        userPhotoRepository.findByUserAndIsRepresentativeTrue(user)
                .ifPresent(photo -> photo.updateRepresentative(false));
    }
}
