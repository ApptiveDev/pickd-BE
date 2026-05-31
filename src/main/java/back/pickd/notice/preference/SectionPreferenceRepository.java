package back.pickd.notice.preference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionPreferenceRepository extends JpaRepository<SectionPreference, Long> {
}
