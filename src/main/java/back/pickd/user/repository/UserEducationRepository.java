package back.pickd.user.repository;

import back.pickd.user.entity.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEducationRepository extends JpaRepository<UserEducation, Long> {
}
