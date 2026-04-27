package back.pickd.user.repository;

import back.pickd.user.entity.UserPrepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrepStatusRepository extends JpaRepository<UserPrepStatus, Long> {
}
