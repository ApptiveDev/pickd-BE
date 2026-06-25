package back.pickd.calendar.controller;

import back.pickd.calendar.dto.CalendarEventRequest;
import back.pickd.calendar.service.CalendarService;
import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Google Calendar 연동 일정 관리 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class CalendarRestController {

    private final CalendarService calendarService;

    @GetMapping("/events")
    @Operation(
            summary = "일정 목록 조회",
            description = "현재 시점 기준 1년 전 ~ 1년 후 범위의 Google Calendar 일정을 반환합니다. timeMin/timeMax 파라미터는 현재 미적용(서버 내부 범위 고정)입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<Event> getEvents(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "조회 시작 시각 (현재 미적용)") @RequestParam(required = false) String timeMin,
            @Parameter(description = "조회 종료 시각 (현재 미적용)") @RequestParam(required = false) String timeMax)
            throws IOException, GeneralSecurityException {

        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("Asia/Seoul");
        java.util.Calendar cal = java.util.Calendar.getInstance(tz);

        cal.add(java.util.Calendar.YEAR, -1);
        DateTime min = new DateTime(cal.getTime());

        cal.add(java.util.Calendar.YEAR, 2);
        DateTime max = new DateTime(cal.getTime());

        return calendarService.getEvents(authentication, min, max);
    }

    @PostMapping("/events")
    @Operation(summary = "일정 등록", description = "Google Calendar에 새 일정을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Event createEvent(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid CalendarEventRequest requestDto) throws IOException, GeneralSecurityException {
        Event event = new Event()
                .setSummary(requestDto.getSummary())
                .setLocation(requestDto.getLocation())
                .setDescription(requestDto.getDescription());

        if (requestDto.getStart() != null) {
            event.setStart(new EventDateTime()
                    .setDateTime(new DateTime(requestDto.getStart().getDateTime()))
                    .setTimeZone(requestDto.getStart().getTimeZone()));
        }

        if (requestDto.getEnd() != null) {
            event.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(requestDto.getEnd().getDateTime()))
                    .setTimeZone(requestDto.getEnd().getTimeZone()));
        }

        return calendarService.createEvent(authentication, event);
    }

    @PutMapping("/events/{eventId}")
    @Operation(summary = "일정 수정", description = "기존 일정을 부분 업데이트합니다. null 필드는 변경되지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Event updateEvent(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable String eventId,
            @RequestBody @Valid CalendarEventRequest requestDto) throws IOException, GeneralSecurityException {

        Event existingEvent = calendarService.getEvent(authentication, eventId);

        if (requestDto.getSummary() != null) existingEvent.setSummary(requestDto.getSummary());
        if (requestDto.getLocation() != null) existingEvent.setLocation(requestDto.getLocation());
        if (requestDto.getDescription() != null) existingEvent.setDescription(requestDto.getDescription());

        return calendarService.updateEvent(authentication, eventId, existingEvent);
    }

    @DeleteMapping("/events/{eventId}")
    @Operation(summary = "일정 삭제", description = "Google Calendar에서 일정을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void deleteEvent(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable String eventId) throws IOException, GeneralSecurityException {
        calendarService.deleteEvent(authentication, eventId);
    }

    @GetMapping("/me")
    @Operation(summary = "인증 사용자 이메일 확인", description = "현재 인증된 사용자의 이메일을 반환합니다. (디버그용)")
    @ApiResponse(responseCode = "200", description = "이메일 반환")
    public String me(@Parameter(hidden = true) Authentication authentication) {
        if (authentication == null) return null;
        return authentication.getName();
    }
}
