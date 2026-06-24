package back.pickd.coverletter.repository;

import back.pickd.coverletter.entity.CoverLetterItem;
import back.pickd.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoverLetterItemRepository extends JpaRepository<CoverLetterItem, Long> {

    List<CoverLetterItem> findAllByNoticeIdAndUserOrderByOrderIndexAsc(Long noticeId, User user);

    List<CoverLetterItem> findAllByApplicationIdAndUserOrderByOrderIndexAsc(Long applicationId, User user);

    Optional<CoverLetterItem> findByIdAndUser(Long id, User user);
}
