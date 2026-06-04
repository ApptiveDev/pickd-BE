package back.pickd.calendar.controller;

import back.pickd.calendar.service.CalendarService;
import back.pickd.global.error.ApiException;
import back.pickd.global.error.ErrorResponse;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.TimeZone;
import java.util.Calendar;

@Tag(name = "캘린더 레거시 (Calendar Legacy)", description = "기존 화면 연동용 Google Calendar API")
@RestController
@RequiredArgsConstructor
public class CalendarViewController {

    private final CalendarService calendarService;

    @Hidden
    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/my-calendar";
        }
        return "index";
    }

    @Operation(
            summary = "캘린더 일정 조회 (레거시)",
            description = "화면 연동용 기존 API입니다. 현재 로그인한 사용자의 Google Calendar 일정 중 Asia/Seoul 기준 최근 14일부터 향후 14일까지의 일정을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레거시 캘린더 일정 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Event.class)))),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요하거나 인증 정보가 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar API 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/calendar")
    public List<Event> getCalendar(Authentication authentication) throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
        Calendar cal = Calendar.getInstance(tz);

        cal.add(Calendar.DAY_OF_YEAR, -14);
        DateTime timeMin = new DateTime(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, 28);
        DateTime timeMax = new DateTime(cal.getTime());

        return calendarService.getEvents(authentication, timeMin, timeMax);
    }

    @Operation(
            summary = "캘린더 일정 생성 (레거시)",
            description = "화면 연동용 기존 API입니다. Google Calendar Event 원본 객체를 받아 현재 로그인한 사용자의 캘린더에 일정을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레거시 캘린더 일정 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요하거나 인증 정보가 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar API 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Google Calendar Event 원본 객체",
            required = true,
            content = @Content(schema = @Schema(implementation = Event.class))
    )
    @PostMapping("/api/calendar")
    public void createEvent(Authentication auth, @RequestBody Event event) {
        try {
            calendarService.createEvent(auth, event);
        } catch (IOException | GeneralSecurityException e) {
            throw ApiException.badGateway("구글 캘린더 일정 생성에 실패했습니다.", e);
        }
    }
}
