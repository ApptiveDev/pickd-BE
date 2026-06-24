package back.pickd.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "section_qualifications")
public class SectionQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private NoticeSection section;

    @Column(name = "general_qualification", columnDefinition = "TEXT", nullable = false)
    private String generalQualification;

    @Column(name = "mandatory_qualification", columnDefinition = "TEXT")
    private String mandatoryQualification;

    @Builder
    public SectionQualification(NoticeSection section, String generalQualification, String mandatoryQualification) {
        this.section = section;
        this.generalQualification = generalQualification;
        this.mandatoryQualification = mandatoryQualification;
    }
}
