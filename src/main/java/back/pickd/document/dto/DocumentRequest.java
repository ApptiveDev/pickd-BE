package back.pickd.document.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {
    private String title;
    private String company;
    private String type; // 포트 폴리오인지 이력서인지
    private Integer progress;
    private String status;
    private String content;
}
