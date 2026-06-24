package back.pickd.document.dto;

import back.pickd.document.enums.DocumentStatus;
import back.pickd.document.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {

    private String title;
    private String company;

    @NotNull
    private DocumentType type;      // RESUME, PORTFOLIO, ETC

    private DocumentStatus status;  // nullable

    private Integer progress;       // 글자수 기반 계산값

    private String content;         // 수정 가능
}
