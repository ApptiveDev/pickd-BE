package back.pickd.application.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ApplicationRequest {

    private String company;
    private String jobTitle;
    private String position;
    private String industry;
    private String status;
    private String memo;

    private LocalDateTime applyDate;
    private LocalDateTime interviewDate;
    private LocalDateTime deadlineDate;
    private boolean important;
}