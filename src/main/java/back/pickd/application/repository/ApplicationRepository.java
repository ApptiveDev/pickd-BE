package back.pickd.application.repository;

import back.pickd.application.entity.Application;
import back.pickd.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findAllByUserOrderByIdDesc(User user);

    Optional<Application> findByIdAndUser(Long id, User user);
}
