package back.pickd.document.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentStatus {

    DRAFT("작성중"),
    COMPLETED("완료");

    @JsonValue
    private final String label;

    @JsonCreator
    public static DocumentStatus from(String label) {
        for (DocumentStatus s : values()) {
            if (s.label.equals(label) || s.name().equals(label)) return s;
        }
        throw new IllegalArgumentException("지원하지 않는 서류 상태입니다: " + label);
    }
}
