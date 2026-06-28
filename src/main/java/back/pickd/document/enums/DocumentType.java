package back.pickd.document.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {

    RESUME("이력서"),
    PORTFOLIO("포트폴리오"),
    ETC("기타");

    @JsonValue
    private final String label;

    @JsonCreator
    public static DocumentType from(String label) {
        for (DocumentType t : values()) {
            if (t.label.equals(label) || t.name().equals(label)) return t;
        }
        throw new IllegalArgumentException("지원하지 않는 서류 유형입니다: " + label);
    }
}
