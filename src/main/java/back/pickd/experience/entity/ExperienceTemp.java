package back.pickd.experience.entity;

import back.pickd.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "experience_temps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceTemp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String experienceName;

    @Column(nullable = false)
    private String experienceGroup; // 예: "상세 서술형", "스펙·증빙형"

    @Column(nullable = false)
    private String experienceType;  // 예: "프로젝트", "어학", "인턴"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumeUrl; // 1차 업로드 시 S3에 임시 저장된 자소서 원본 CloudFront URL

    private LocalDateTime createdAt;

    @Builder
    public ExperienceTemp(User user, String experienceName, String experienceGroup, String experienceType, String resumeUrl) {
        this.user = user;
        this.experienceName = experienceName;
        this.experienceGroup = experienceGroup;
        this.experienceType = experienceType;
        this.resumeUrl = resumeUrl;
        this.createdAt = LocalDateTime.now();
    }
}
