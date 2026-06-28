package back.pickd.application.dto.request;

import back.pickd.application.enums.ApplicationStatus;
import back.pickd.notice.enums.JobCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplicationRequest {

    // 기존 공고 연결 (AI 분석된 공고)
    private Long noticeId;

    // 수기 입력용 공고 정보 (noticeId 없을 때 Notice 자동 생성)
    private String company;
    private String jobTitle;
    private String position;
    private String industry;

    /** 수기 입력 시 공고 카테고리 (미입력 시 FULL_TIME 기본값) */
    private JobCategory category;

    /** 수기 입력 시 공고 시작일 (미입력 시 오늘 날짜, 형식: yyyy-MM-dd) */
    private String startedAt;

    /** 수기 입력 시 공고 마감일 (선택) */
    private String endedAt;

    @NotNull
    private ApplicationStatus status;

    private String memo;

    private LocalDateTime applyDate;
    private LocalDateTime interviewDate;
    private LocalDateTime deadlineDate;
    private boolean important;
}
