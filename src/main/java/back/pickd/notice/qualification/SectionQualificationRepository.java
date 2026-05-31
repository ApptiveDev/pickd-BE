package back.pickd.notice.qualification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionQualificationRepository extends JpaRepository<SectionQualification, Long> {
}
