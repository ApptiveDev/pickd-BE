package back.pickd.notice.repository;

import back.pickd.notice.entity.Notice;
import back.pickd.notice.entity.NoticeProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeProcessRepository extends JpaRepository<NoticeProcess, Long> {
    List<NoticeProcess> findAllByNotice(Notice notice);
}
