package back.pickd.experience.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experience_files")
@Getter

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceFile {

    @Id
    @Column(length = 36)
    private String id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id", nullable = false)
    private UserExperience userExperience;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(nullable = false, length = 50)
    private String source;

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
