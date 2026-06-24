package back.pickd.notice.repository;

import back.pickd.notice.entity.SectionQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionQualificationRepository extends JpaRepository<SectionQualification, Long> {
}
