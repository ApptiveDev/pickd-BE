package back.pickd.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;
    private String jobTitle;
    private String position;
    private String industry;
    private String status;
    private String memo;
    private LocalDateTime applyDate;
    private LocalDateTime deadlineDate;

    private String applyEventId;
    private String deadlineEventId;
}