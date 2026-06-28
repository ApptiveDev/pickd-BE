package back.pickd.notice.repository;

import back.pickd.notice.entity.NoticeSection;
import back.pickd.notice.entity.SectionQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionQualificationRepository extends JpaRepository<SectionQualification, Long> {
    List<SectionQualification> findAllBySection(NoticeSection section);
}
