package back.pickd.document.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentStatus {

    DRAFT("작성중"),
    COMPLETED("완료");

    private final String label;
}
