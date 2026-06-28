package back.pickd.notice.repository;

import back.pickd.notice.entity.NoticeSection;
import back.pickd.notice.entity.SectionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionPreferenceRepository extends JpaRepository<SectionPreference, Long> {
    List<SectionPreference> findAllBySection(NoticeSection section);
}
