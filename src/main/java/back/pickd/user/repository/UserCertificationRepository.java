package back.pickd.user.repository;

import back.pickd.user.entity.User;
import back.pickd.user.entity.UserCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCertificationRepository extends JpaRepository<UserCertification, Long> {
    void deleteByUser(User user);
}
