package back.pickd.notice.repository;

import back.pickd.notice.entity.ApplicationDocument;
import back.pickd.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {
    List<ApplicationDocument> findAllByNotice(Notice notice);
}
