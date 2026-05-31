package back.pickd.notice.section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeSectionRepository extends JpaRepository<NoticeSection, Long> {
}
