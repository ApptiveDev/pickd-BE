package back.pickd.notice.repository;

import back.pickd.notice.entity.Notice;
import back.pickd.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByUserOrderByIdDesc(User user);

    Optional<Notice> findByIdAndUser(Long id, User user);
}
