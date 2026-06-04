package back.pickd.calendar.controller;

import back.pickd.calendar.dto.CalendarEventRequest;
import back.pickd.calendar.service.CalendarService;
import back.pickd.global.error.ErrorResponse;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Tag(name = "캘린더 REST (Calendar)", description = "Google Calendar 일정 조회, 생성, 수정, 삭제 API")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarRestController {

    private final CalendarService calendarService;

    /**
     * 일정 목록 조회
     */
    @Operation(
            summary = "캘린더 일정 목록 조회",
            description = "현재 로그인한 사용자의 Google Calendar 일정을 조회합니다. 현재 구현은 Asia/Seoul 기준 최근 1년부터 향후 1년까지의 일정을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캘린더 일정 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Event.class)))),
            @ApiResponse(responseCode = "400", description = "요청 파라미터가 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요하거나 인증 정보가 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar API 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/events")
    public List<Event> getEvents(Authentication authentication,
                                @Parameter(description = "조회 시작 시각(예약 파라미터, 현재 구현은 기본 범위를 사용)", example = "2026-01-01T00:00:00+09:00")
                                @RequestParam(required = false) String timeMin,
                                @Parameter(description = "조회 종료 시각(예약 파라미터, 현재 구현은 기본 범위를 사용)", example = "2026-12-31T23:59:59+09:00")
                                @RequestParam(required = false) String timeMax)
            throws IOException, GeneralSecurityException {

        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("Asia/Seoul");
        java.util.Calendar cal = java.util.Calendar.getInstance(tz);

        cal.add(java.util.Calendar.YEAR, -1);
        DateTime min = new DateTime(cal.getTime());

        cal.add(java.util.Calendar.YEAR, 2);
        DateTime max = new DateTime(cal.getTime());

        return calendarService.getEvents(authentication, min, max);
    }

    /**
     * 일정 등록
     */
    @Operation(
            summary = "캘린더 일정 등록",
            description = "현재 로그인한 사용자의 Google Calendar에 새 일정을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캘린더 일정 등록 성공",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요하거나 인증 정보가 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar API 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "등록할 캘린더 일정 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = CalendarEventRequest.class))
    )
    @PostMapping("/events")
    public Event createEvent(Authentication authentication, @RequestBody CalendarEventRequest requestDto) throws IOException, GeneralSecurityException {
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

    /**
     * 일정 수정 (부분 업데이트 지원)
     */
    @Operation(
            summary = "캘린더 일정 수정",
            description = "Google Calendar 이벤트 ID에 해당하는 일정을 수정합니다. summary, location, description 값은 전달된 필드만 부분 반영합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캘린더 일정 수정 성공",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "400", description = "이벤트 ID 또는 요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요하거나 인증 정보가 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar API 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 캘린더 일정 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = CalendarEventRequest.class))
    )
    @PutMapping("/events/{eventId}")
    public Event updateEvent(Authentication authentication, 
                            @Parameter(description = "수정할 Google Calendar 이벤트 ID", example = "abc123def456")
                            @PathVariable String eventId,
                            @RequestBody CalendarEventRequest requestDto) throws IOException, GeneralSecurityException {
        
        Event existingEvent = calendarService.getEvent(authentication, eventId);
        
        if (requestDto.getSummary() != null) existingEvent.setSummary(requestDto.getSummary());
        if (requestDto.getLocation() != null) existingEvent.setLocation(requestDto.getLocation());
        if (requestDto.getDescription() != null) existingEvent.setDescription(requestDto.getDescription());
        
        return calendarService.updateEvent(authentication, eventId, existingEvent);
    }

    /**
     * 일정 삭제
     */
    @Operation(
            summary = "캘린더 일정 삭제",
            description = "Google Calendar 이벤트 ID에 해당하는 일정을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캘린더 일정 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "이벤트 ID가 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요하거나 인증 정보가 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar API 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/events/{eventId}")
    public void deleteEvent(Authentication authentication,
                            @Parameter(description = "삭제할 Google Calendar 이벤트 ID", example = "abc123def456")
                            @PathVariable String eventId) throws IOException, GeneralSecurityException {
        calendarService.deleteEvent(authentication, eventId);
    }

    @Operation(
            summary = "캘린더 인증 사용자 확인",
            description = "현재 인증 객체에 저장된 사용자 식별자(email)를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 사용자 식별자 조회 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public String me(Authentication authentication) {
        if (authentication == null) return null;
        return authentication.getName();
    }
}
