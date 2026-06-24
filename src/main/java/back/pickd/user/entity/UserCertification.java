package back.pickd.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_certifications")
public class UserCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type; // LICENSE, LANGUAGE

    @Column(nullable = false)
    private String name;

    @Column
    private String score; // 점수 또는 급수

    @Column
    private String acquisitionDate; // 취득일 YYYY-MM

    @Builder
    public UserCertification(User user, String type, String name, String score, String acquisitionDate) {
        this.user = user;
        this.type = type;
        this.name = name;
        this.score = score;
        this.acquisitionDate = acquisitionDate;
    }
}
