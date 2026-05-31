package back.pickd.notice.question;

import back.pickd.notice.enums.QuestionType;
import back.pickd.notice.notice.Notice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice; // 부모 공고 (1:N)

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType; // 문항 유형 (STRING 저장)

    @Column(name = "question_number")
    private Integer questionNumber;

    @Column(name = "question_content", columnDefinition = "TEXT", nullable = false)
    private String questionContent;

    @Column(name = "character_limit")
    private String characterLimit;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public ApplicationQuestion(Notice notice, QuestionType questionType,
                               Integer questionNumber, String questionContent,
                               String characterLimit) {
        this.notice = notice;
        this.questionType = questionType;
        this.questionNumber = questionNumber;
        this.questionContent = questionContent;
        this.characterLimit = characterLimit;
    }

    public void assignToNotice(Notice notice) {
        this.notice = notice;
    }
}