package back.pickd.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_experiences")
public class UserExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String type; // INTERN, PROJECT, ACTIVITY, AWARD

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column
    private String startDate; // YYYY-MM

    @Column
    private String endDate; // YYYY-MM or "진행중"

    @Builder
    public UserExperience(User user, String type, String title, String description, String startDate, String endDate) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
