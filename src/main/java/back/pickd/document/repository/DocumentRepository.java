package back.pickd.document.repository;

import back.pickd.document.entity.Document;
import back.pickd.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByUserOrderByIdDesc(User user);

    List<Document> findAllByApplicationIdAndUser(Long applicationId, User user);

    Optional<Document> findByIdAndUser(Long id, User user);
}
