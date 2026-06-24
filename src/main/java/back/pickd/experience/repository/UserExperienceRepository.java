package back.pickd.experience.repository;

import back.pickd.user.entity.User;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserExperienceRepository extends JpaRepository<UserExperience, String> {

    // 유저의 경험 목록 조회 (최신순)
    List<UserExperience> findByUserOrderByCreatedAtDesc(User user);

    // 유저의 특정 경험 단일 조회
    Optional<UserExperience> findByIdAndUser(String id, User user);

    // 필터링 적용된 경험 목록 조회
    @Query("select e from UserExperience e where e.user = :user " +
           "and (:type is null or e.experienceType = :type) " +
           "and (:group is null or e.experienceGroup = :group) " +
           "order by e.createdAt desc")
    List<UserExperience> findByUserWithFilters(
        @Param("user") User user,
        @Param("type") ExperienceType type,
        @Param("group") ExperienceGroup group
    );
}
