package back.pickd.user.repository;

import back.pickd.user.entity.User;
import back.pickd.user.entity.UserCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCertificationRepository extends JpaRepository<UserCertification, Long> {
    @Modifying
    @Query("delete from UserCertification c where c.user = :user")
    void deleteByUser(User user);
}
