package back.pickd.user.photo;

import back.pickd.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {
    List<UserPhoto> findByUserOrderByCreatedAtDesc(User user);

    Optional<UserPhoto> findByIdAndUser(Long id, User user);

    Optional<UserPhoto> findByUserAndIsRepresentativeTrue(User user);
}
