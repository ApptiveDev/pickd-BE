package back.pickd.notice.question;

import back.pickd.notice.enums.QuestionType;
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
@Table(name = "application_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NoticeSection section; // 부모 모집 부문 (FK)

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType; // 문항 유형 (COVER_LETTER, FREE_FORM, JOB_DESCRIPTION, ADDITIONAL)

    @Column(name = "question_number")
    private Integer questionNumber; // 문항 번호

    @Column(name = "question_content", columnDefinition = "TEXT", nullable = false)
    private String questionContent; // 문항 내용

    @Column(name = "character_limit")
    private String characterLimit; // 글자 수 제한 (단위가 기업마다 다르므로 String 사용)

    @Column(name = "question_notes", columnDefinition = "TEXT")
    private String questionNotes; // 문항 관련 유의사항

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

    /** NoticeSection 단방향에서 호출 시 반대편 FK 설정용 */
    public void setSection(NoticeSection section) {
        this.section = section;
    }

    @Builder
    public ApplicationQuestion(NoticeSection section, QuestionType questionType,
                               Integer questionNumber, String questionContent,
                               String characterLimit, String questionNotes) {
        this.section = section;
        this.questionType = questionType;
        this.questionNumber = questionNumber;
        this.questionContent = questionContent;
        this.characterLimit = characterLimit;
        this.questionNotes = questionNotes;
    }
}
