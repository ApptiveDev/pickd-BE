package back.pickd.user.photo;

import java.time.LocalDateTime;

public record UserPhotoResponse(Long id, String imageUrl, String originalFilename,
                                boolean isPublic, boolean isRepresentative, LocalDateTime createdAt) {
    public UserPhotoResponse(UserPhoto photo) {
        this(photo.getId(), photo.getImageUrl(), photo.getOriginalFilename(),
                photo.isPublic(), photo.isRepresentative(), photo.getCreatedAt());
    }
}
