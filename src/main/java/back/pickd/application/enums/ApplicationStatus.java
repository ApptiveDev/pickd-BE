package back.pickd.application.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {

    PREPARING("지원 예정"),
    WRITING("작성중"),
    SUBMITTED("제출 완료"),
    WAITING("결과 대기"),
    INTERVIEW("면접 전형"),
    FINAL("최종 결과");

    private final String label;

    public boolean needsApplyEvent() {
        return this == PREPARING || this == WRITING;
    }

    public boolean needsInterviewEvent() {
        return this == SUBMITTED || this == WAITING || this == INTERVIEW;
    }

    public boolean needsDeadlineEvent() {
        return this == FINAL;
    }
}
