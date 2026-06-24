package back.pickd.notice.repository;

import back.pickd.notice.entity.NoticeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeSectionRepository extends JpaRepository<NoticeSection, Long> {
}
