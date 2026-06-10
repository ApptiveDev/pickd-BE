package back.pickd.experience.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experience_links")
@Getter

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceLink {

    @Id
    @Column(length = 36)
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id", nullable = false)
    private UserExperience userExperience;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "material_type", nullable = false)
    private String materialType;

    @Column(name = "document_position")
    private Integer documentPosition;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
    public void setUserExperience(UserExperience userExperience) {
        this.userExperience = userExperience;
    }
}
