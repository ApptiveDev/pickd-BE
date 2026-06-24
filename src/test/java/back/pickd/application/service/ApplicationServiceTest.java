package back.pickd.application.service;

import back.pickd.application.dto.request.ApplicationRequest;
import back.pickd.application.dto.response.ApplicationResponse;
import back.pickd.application.entity.Application;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.calendar.service.CalendarAsyncService;
import back.pickd.notice.entity.Notice;
import back.pickd.notice.enums.JobCategory;
import back.pickd.notice.repository.NoticeRepository;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService 단위 테스트")
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock NoticeRepository noticeRepository;
    @Mock CalendarAsyncService calendarAsyncService;
    @Mock UserService userService;
    @Mock Authentication authentication;

    @InjectMocks ApplicationService applicationService;

    User user;
    Notice notice;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .name("테스트유저")
                .build();

        notice = Notice.builder()
                .user(user)
                .companyName("카카오")
                .noticeName("2026 상반기 공채")
                .category(JobCategory.FULL_TIME)
                .startedAt("2026-01-01")
                .build();

        when(authentication.getName()).thenReturn("test@test.com");
        when(userService.findByEmail("test@test.com")).thenReturn(user);
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private Application buildApp(Long id, ApplicationStatus status) {
        return Application.builder()
                .user(user)
                .company("카카오")
                .jobTitle("백엔드 개발")
                .status(status)
                .important(false)
                .build();
    }

    private ApplicationRequest buildRequest(ApplicationStatus status) {
        ApplicationRequest req = new ApplicationRequest();
        req.setCompany("카카오");
        req.setJobTitle("백엔드 개발");
        req.setStatus(status);
        req.setImportant(false);
        return req;
    }

    // ── getApplications ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getApplications")
    class GetApplications {

        @Test
        @DisplayName("현재 사용자 공고 목록을 최신순으로 반환한다")
        void returnsUserApplicationsOrderedByIdDesc() {
            Application app1 = buildApp(1L, ApplicationStatus.PREPARING);
            Application app2 = buildApp(2L, ApplicationStatus.SUBMITTED);
            when(applicationRepository.findAllByUserOrderByIdDesc(user)).thenReturn(List.of(app2, app1));

            List<ApplicationResponse> result = applicationService.getApplications(authentication);

            assertThat(result).hasSize(2);
            verify(applicationRepository).findAllByUserOrderByIdDesc(user);
        }

        @Test
        @DisplayName("공고가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoApplications() {
            when(applicationRepository.findAllByUserOrderByIdDesc(user)).thenReturn(List.of());

            List<ApplicationResponse> result = applicationService.getApplications(authentication);

            assertThat(result).isEmpty();
        }
    }

    // ── addApplication ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addApplication")
    class AddApplication {

        @Test
        @DisplayName("PREPARING 상태이고 applyDate가 있으면 apply 캘린더 이벤트를 생성한다")
        void createsApplyCalendarEventForPreparingStatus() throws Exception {
            ApplicationRequest req = buildRequest(ApplicationStatus.PREPARING);
            req.setApplyDate(LocalDateTime.now().plusDays(3));
            when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            applicationService.addApplication(req, authentication);

            verify(calendarAsyncService).createEventAsync(any(), eq("apply"), eq(authentication), any());
            verify(calendarAsyncService, never()).createEventAsync(any(), eq("interview"), any(), any());
            verify(calendarAsyncService, never()).createEventAsync(any(), eq("deadline"), any(), any());
        }

        @Test
        @DisplayName("SUBMITTED 상태이고 interviewDate가 있으면 interview 캘린더 이벤트를 생성한다")
        void createsInterviewCalendarEventForSubmittedStatus() throws Exception {
            ApplicationRequest req = buildRequest(ApplicationStatus.SUBMITTED);
            req.setInterviewDate(LocalDateTime.now().plusDays(7));
            when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            applicationService.addApplication(req, authentication);

            verify(calendarAsyncService).createEventAsync(any(), eq("interview"), eq(authentication), any());
            verify(calendarAsyncService, never()).createEventAsync(any(), eq("apply"), any(), any());
        }

        @Test
        @DisplayName("FINAL 상태이고 deadlineDate가 있으면 deadline 캘린더 이벤트를 생성한다")
        void createsDeadlineCalendarEventForFinalStatus() throws Exception {
            ApplicationRequest req = buildRequest(ApplicationStatus.FINAL);
            req.setDeadlineDate(LocalDateTime.now().plusDays(14));
            when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            applicationService.addApplication(req, authentication);

            verify(calendarAsyncService).createEventAsync(any(), eq("deadline"), eq(authentication), any());
        }

        @Test
        @DisplayName("날짜가 null이면 캘린더 이벤트를 생성하지 않는다")
        void doesNotCreateCalendarEventWhenDateIsNull() throws Exception {
            ApplicationRequest req = buildRequest(ApplicationStatus.PREPARING);
            // applyDate = null
            when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            applicationService.addApplication(req, authentication);

            verify(calendarAsyncService, never()).createEventAsync(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Application 저장 시 User가 설정된다")
        void savesApplicationWithUser() throws Exception {
            ApplicationRequest req = buildRequest(ApplicationStatus.WRITING);
            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            when(applicationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            applicationService.addApplication(req, authentication);

            assertThat(captor.getValue().getUser()).isEqualTo(user);
            assertThat(captor.getValue().getCompany()).isEqualTo("카카오");
        }

        @Test
        @DisplayName("noticeId가 있으면 Notice와 연결된 Application을 생성한다")
        void createsApplicationLinkedToNotice() throws Exception {
            ApplicationRequest req = buildRequest(ApplicationStatus.PREPARING);
            req.setNoticeId(1L);
            when(noticeRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(notice));
            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            when(applicationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            applicationService.addApplication(req, authentication);

            assertThat(captor.getValue().getNotice()).isEqualTo(notice);
        }

        @Test
        @DisplayName("noticeId가 null이면 notice 없이 Application을 생성한다")
        void createsApplicationWithoutNotice() throws Exception {
            ApplicationRequest req = buildRequest(ApplicationStatus.PREPARING);
            // noticeId = null
            ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
            when(applicationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            applicationService.addApplication(req, authentication);

            assertThat(captor.getValue().getNotice()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 noticeId로 Application 생성 시 예외가 발생한다")
        void throwsWhenNoticeNotFound() {
            ApplicationRequest req = buildRequest(ApplicationStatus.PREPARING);
            req.setNoticeId(99L);
            when(noticeRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.addApplication(req, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공고를 찾을 수 없습니다");
        }
    }

    // ── deleteApplication ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteApplication")
    class DeleteApplication {

        @Test
        @DisplayName("소유자가 맞으면 Application을 삭제한다")
        void deletesApplicationWhenOwnerMatches() throws Exception {
            Application app = buildApp(1L, ApplicationStatus.PREPARING);
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(app));

            applicationService.deleteApplication(1L, authentication);

            verify(applicationRepository).delete(app);
        }

        @Test
        @DisplayName("다른 사용자의 Application 삭제 시 예외가 발생한다")
        void throwsWhenDeletingOtherUsersApplication() {
            when(applicationRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.deleteApplication(99L, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원 공고를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("applyEventId가 있으면 캘린더 이벤트를 삭제한다")
        void deletesCalendarEventWhenApplyEventIdExists() throws Exception {
            Application app = Application.builder()
                    .user(user)
                    .company("카카오")
                    .jobTitle("백엔드")
                    .status(ApplicationStatus.PREPARING)
                    .important(false)
                    .build();
            app.assignApplyEventId("apply-event-id-123");
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(app));

            applicationService.deleteApplication(1L, authentication);

            verify(calendarAsyncService).deleteEventAsync(authentication, "apply-event-id-123");
            verify(applicationRepository).delete(app);
        }

        @Test
        @DisplayName("캘린더 이벤트가 없으면 캘린더 삭제를 호출하지 않는다")
        void doesNotDeleteCalendarEventWhenNoEventIds() throws Exception {
            Application app = buildApp(1L, ApplicationStatus.WRITING);
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(app));

            applicationService.deleteApplication(1L, authentication);

            verify(calendarAsyncService, never()).deleteEventAsync(any(), any());
        }
    }

    // ── updateApplication ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateApplication")
    class UpdateApplication {

        @Test
        @DisplayName("다른 사용자의 Application 수정 시 예외가 발생한다")
        void throwsWhenUpdatingOtherUsersApplication() {
            when(applicationRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());
            ApplicationRequest req = buildRequest(ApplicationStatus.WRITING);

            assertThatThrownBy(() -> applicationService.updateApplication(99L, req, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원 공고를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("상태가 바뀌어 캘린더 이벤트가 필요 없어지면 기존 이벤트를 삭제한다")
        void deletesCalendarEventWhenStatusNoLongerNeedsIt() throws Exception {
            Application app = Application.builder()
                    .user(user)
                    .company("카카오")
                    .jobTitle("백엔드")
                    .status(ApplicationStatus.PREPARING)
                    .important(false)
                    .build();
            app.assignApplyEventId("old-event-id");
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(app));

            // FINAL 상태로 변경 → needsApplyEvent() = false
            ApplicationRequest req = buildRequest(ApplicationStatus.FINAL);
            req.setDeadlineDate(LocalDateTime.now().plusDays(7));
            when(applicationRepository.save(any())).thenReturn(app);

            applicationService.updateApplication(1L, req, authentication);

            verify(calendarAsyncService).deleteEventAsync(authentication, "old-event-id");
        }
    }
}
