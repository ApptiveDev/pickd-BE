package back.pickd.document.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {
    private String title;
    private String company;
    private String type; // 포트 폴리오인지 이력서인지
    private Integer progress; // 글자수로 계산해서 응답 
    private String status; // nullable 
    private String content; // 수정 가능해야함. 
}
