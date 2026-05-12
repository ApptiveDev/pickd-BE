package back.pickd.application.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
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

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    private String company;
    private String jobTitle;
    private String position;
    private String industry;
    private String status;
    private String memo;
    private LocalDateTime applyDate;
    private LocalDateTime interviewDate;
    private LocalDateTime deadlineDate;

    private String applyEventId;
    private String interviewEventId;
    private String deadlineEventId;
}