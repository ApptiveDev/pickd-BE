package back.pickd.notice.process;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeProcessRepository extends JpaRepository<NoticeProcess, Long> {
}
