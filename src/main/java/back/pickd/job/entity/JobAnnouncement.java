package back.pickd.job.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "job_announcements")
public class JobAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String companyName;
    private String jdUrl;

    @Column(columnDefinition = "TEXT")
    private String opportunities;
    @Column(columnDefinition = "TEXT")
    private String threats;

    // 자격증 가점 정보
    @ElementCollection
    @CollectionTable(name = "job_certification_bonuses", joinColumns = @JoinColumn(name = "job_id"))
    private List<CertificationBonus> bonuses = new ArrayList<>();

    @Builder
    public JobAnnouncement(String title, String companyName, String jdUrl, String opportunities, String threats) {
        this.title = title;
        this.companyName = companyName;
        this.jdUrl = jdUrl;
        this.opportunities = opportunities;
        this.threats = threats;
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationBonus {
        private String certificateName;
        private String score;
        private String targetGroup;
        private Integer evidencePage;
        @Column(columnDefinition = "TEXT")
        private String originalText;
    }
}
