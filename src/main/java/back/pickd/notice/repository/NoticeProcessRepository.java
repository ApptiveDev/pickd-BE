package back.pickd.notice.repository;

import back.pickd.notice.entity.NoticeProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeProcessRepository extends JpaRepository<NoticeProcess, Long> {
}
