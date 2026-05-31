package back.pickd.user.repository;

import back.pickd.user.entity.User;
import back.pickd.user.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserExperienceRepository extends JpaRepository<UserExperience, String> {

    @Modifying
    @Query("delete from UserExperience e where e.user = :user")
    void deleteByUser(User user);

    // 유저의 경험 목록 조회 (최신순)
    List<UserExperience> findByUserOrderByCreatedAtDesc(User user);

    // 유저의 특정 경험 단일 조회
    Optional<UserExperience> findByIdAndUser(String id, User user);
}
