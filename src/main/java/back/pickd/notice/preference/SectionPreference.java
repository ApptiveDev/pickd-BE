package back.pickd.notice.preference;

import back.pickd.notice.section.NoticeSection;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "section_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SectionPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NoticeSection section; // 부모 모집 부문 (FK)

    @Column(name = "general_pref", columnDefinition = "TEXT")
    private String generalPreference; // 일반 우대사항

    @Column(name = "additional_points", columnDefinition = "TEXT")
    private String additionalPoints; // 가산점 부여 항목

    @Column(name = "veteran_pref")
    private String veteranPreference; // 보훈 대상자 우대

    @Column(name = "disability_pref")
    private String disabilityPreference; // 장애인 우대

    @Column(name = "local_talent_pref")
    private String localTalentPreference; // 지역 인재 우대

    @Column(name = "certificate_pref", columnDefinition = "TEXT")
    private String certificatePreference; // 자격증 우대

    @Column(name = "experience_pref", columnDefinition = "TEXT")
    private String experiencePreference; // 경력 우대

    @Column(name = "other_prefs", columnDefinition = "TEXT")
    private String otherPreferences; // 기타 우대사항

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 일시

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public SectionPreference(NoticeSection section, String generalPreference, String additionalPoints,
                             String veteranPreference, String disabilityPreference, String localTalentPreference,
                             String certificatePreference, String experiencePreference, String otherPreferences) {
        this.section = section;
        this.generalPreference = generalPreference;
        this.additionalPoints = additionalPoints;
        this.veteranPreference = veteranPreference;
        this.disabilityPreference = disabilityPreference;
        this.localTalentPreference = localTalentPreference;
        this.certificatePreference = certificatePreference;
        this.experiencePreference = experiencePreference;
        this.otherPreferences = otherPreferences;
    }
}
