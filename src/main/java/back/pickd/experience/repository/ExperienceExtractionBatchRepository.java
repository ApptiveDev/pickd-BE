package back.pickd.experience.repository;

import back.pickd.experience.entity.ExperienceExtractionBatch;
import back.pickd.experience.enums.ExtractionBatchStatus;
import back.pickd.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ExperienceExtractionBatchRepository
        extends JpaRepository<ExperienceExtractionBatch, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExperienceExtractionBatch> findByIdAndUser(String id, User user);

    List<ExperienceExtractionBatch> findByUserOrderByCreatedAtDesc(User user);

    List<ExperienceExtractionBatch> findByUserAndStatusOrderByCreatedAtDesc(
            User user,
            ExtractionBatchStatus status
    );
}
