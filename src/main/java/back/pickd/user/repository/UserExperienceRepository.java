package back.pickd.user.repository;

import back.pickd.user.entity.User;
import back.pickd.user.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserExperienceRepository extends JpaRepository<UserExperience, Long> {
    @Modifying
    @Query("delete from UserExperience e where e.user = :user")
    void deleteByUser(User user);
}
