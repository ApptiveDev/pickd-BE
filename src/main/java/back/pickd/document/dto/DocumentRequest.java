package back.pickd.document.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {
    private String title;
    private String company;
    private String type;
    private Integer progress;
    private String status;
    private String content;
}