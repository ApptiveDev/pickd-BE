package back.pickd.document.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {

    RESUME("이력서"),
    PORTFOLIO("포트폴리오"),
    COVER_LETTER("자기소개서"),
    ETC("기타");

    private final String label;
}
