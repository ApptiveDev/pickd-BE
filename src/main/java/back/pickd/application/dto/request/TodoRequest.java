package back.pickd.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoRequest {
    private String title;
    private String dueDateTime;
    private String memo;
    private Long applicationId;

    private String company;
    private String jobTitle;
}