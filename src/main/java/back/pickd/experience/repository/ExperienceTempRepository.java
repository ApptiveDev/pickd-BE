package back.pickd.experience.repository;

import back.pickd.experience.entity.ExperienceTemp;
import back.pickd.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExperienceTempRepository extends JpaRepository<ExperienceTemp, Long> {
    
    List<ExperienceTemp> findByUser(User user);

    @Modifying
    @Query("delete from ExperienceTemp et where et.user = :user")
    void deleteByUser(@Param("user") User user);
}
