package back.pickd.application.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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

    @JsonValue
    private final String label;

    @JsonCreator
    public static ApplicationStatus from(String label) {
        for (ApplicationStatus status : values()) {
            if (status.label.equals(label) || status.name().equals(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 지원 상태입니다: " + label);
    }

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
