package back.pickd.global.infra.s3;

import back.pickd.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FileUploadType uploadType;

    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
