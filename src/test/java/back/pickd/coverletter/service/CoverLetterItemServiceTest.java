package back.pickd.coverletter.service;

import back.pickd.application.entity.Application;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.coverletter.dto.request.CoverLetterItemRequest;
import back.pickd.coverletter.dto.response.CoverLetterItemResponse;
import back.pickd.coverletter.entity.CoverLetterItem;
import back.pickd.coverletter.repository.CoverLetterItemRepository;
import back.pickd.notice.enums.JobCategory;
import back.pickd.notice.entity.Notice;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoverLetterItemService 단위 테스트")
class CoverLetterItemServiceTest {

    @Mock CoverLetterItemRepository coverLetterItemRepository;
    @Mock NoticeRepository noticeRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock UserService userService;
    @Mock Authentication authentication;

    @InjectMocks CoverLetterItemService coverLetterItemService;

    User user;
    Notice notice;
    Application application;

    @BeforeEach
    void setUp() {
        user = User.builder().email("test@test.com").name("테스터").build();

        notice = Notice.builder()
                .user(user)
                .companyName("카카오")
                .noticeName("2026 상반기 공채")
                .category(JobCategory.FULL_TIME)
                .startedAt("2026-01-01")
                .build();

        application = Application.builder()
                .user(user).company("카카오").jobTitle("백엔드")
                .status(ApplicationStatus.WRITING).important(false).build();

        when(authentication.getName()).thenReturn("test@test.com");
        when(userService.findByEmail("test@test.com")).thenReturn(user);
    }

    private CoverLetterItem buildItem(Notice n, Application app) {
        return CoverLetterItem.builder()
                .user(user)
                .notice(n)
                .application(app)
                .question("지원 동기를 서술하세요.")
                .answer("저는...")
                .maxLength(500)
                .orderIndex(0)
                .build();
    }

    private CoverLetterItemRequest buildRequest(Long noticeId, Long appId) {
        return new CoverLetterItemRequest("지원 동기를 서술하세요.", "저는...", 500, 0, false, noticeId, appId);
    }

    // ── getByNotice ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getByNotice")
    class GetByNotice {

        @Test
        @DisplayName("공고 ID로 자소서 문항 목록을 orderIndex 오름차순으로 반환한다")
        void returnsItemsOrderedByIndex() {
            when(coverLetterItemRepository.findAllByNoticeIdAndUserOrderByOrderIndexAsc(1L, user))
                    .thenReturn(List.of(buildItem(notice, null)));

            List<CoverLetterItemResponse> result = coverLetterItemService.getByNotice(1L, authentication);

            assertThat(result).hasSize(1);
            verify(coverLetterItemRepository).findAllByNoticeIdAndUserOrderByOrderIndexAsc(1L, user);
        }

        @Test
        @DisplayName("문항이 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoItems() {
            when(coverLetterItemRepository.findAllByNoticeIdAndUserOrderByOrderIndexAsc(1L, user))
                    .thenReturn(List.of());

            List<CoverLetterItemResponse> result = coverLetterItemService.getByNotice(1L, authentication);

            assertThat(result).isEmpty();
        }
    }

    // ── getByApplication ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getByApplication")
    class GetByApplication {

        @Test
        @DisplayName("지원 공고 ID로 자소서 문항 목록을 반환한다")
        void returnsItemsByApplicationId() {
            when(coverLetterItemRepository.findAllByApplicationIdAndUserOrderByOrderIndexAsc(1L, user))
                    .thenReturn(List.of(buildItem(null, application)));

            List<CoverLetterItemResponse> result = coverLetterItemService.getByApplication(1L, authentication);

            assertThat(result).hasSize(1);
        }
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("noticeId로 Notice에 연결된 문항을 생성한다")
        void createsItemLinkedToNotice() {
            when(noticeRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(notice));
            ArgumentCaptor<CoverLetterItem> captor = ArgumentCaptor.forClass(CoverLetterItem.class);
            when(coverLetterItemRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            coverLetterItemService.create(buildRequest(1L, null), authentication);

            CoverLetterItem saved = captor.getValue();
            assertThat(saved.getNotice()).isEqualTo(notice);
            assertThat(saved.getApplication()).isNull();
            assertThat(saved.getUser()).isEqualTo(user);
            assertThat(saved.getQuestion()).isEqualTo("지원 동기를 서술하세요.");
        }

        @Test
        @DisplayName("applicationId로 Application에 연결된 문항을 생성한다")
        void createsItemLinkedToApplication() {
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(application));
            ArgumentCaptor<CoverLetterItem> captor = ArgumentCaptor.forClass(CoverLetterItem.class);
            when(coverLetterItemRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            coverLetterItemService.create(buildRequest(null, 1L), authentication);

            CoverLetterItem saved = captor.getValue();
            assertThat(saved.getApplication()).isEqualTo(application);
            assertThat(saved.getNotice()).isNull();
        }

        @Test
        @DisplayName("noticeId와 applicationId가 모두 null이면 예외가 발생한다")
        void throwsWhenBothIdsAreNull() {
            assertThatThrownBy(() -> coverLetterItemService.create(buildRequest(null, null), authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("noticeId 또는 applicationId 중 하나는 필수입니다");
        }

        @Test
        @DisplayName("다른 사용자의 Notice에 문항 생성 시 예외가 발생한다")
        void throwsWhenNoticeNotOwnedByUser() {
            when(noticeRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> coverLetterItemService.create(buildRequest(99L, null), authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("공고를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("다른 사용자의 Application에 문항 생성 시 예외가 발생한다")
        void throwsWhenApplicationNotOwnedByUser() {
            when(applicationRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> coverLetterItemService.create(buildRequest(null, 99L), authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원 공고를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("orderIndex가 null이면 0으로 저장된다")
        void savesOrderIndexAsZeroWhenNull() {
            // orderIndex = null 로 생성
            CoverLetterItemRequest req = new CoverLetterItemRequest("지원 동기를 서술하세요.", "저는...", 500, null, false, 1L, null);
            when(noticeRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(notice));
            ArgumentCaptor<CoverLetterItem> captor = ArgumentCaptor.forClass(CoverLetterItem.class);
            when(coverLetterItemRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            coverLetterItemService.create(req, authentication);

            assertThat(captor.getValue().getOrderIndex()).isEqualTo(0);
        }
    }

    // ── update ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("소유자가 맞으면 문항 내용을 수정한다")
        void updatesItemContent() {
            CoverLetterItem item = buildItem(notice, null);
            when(coverLetterItemRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(item));

            CoverLetterItemRequest req = new CoverLetterItemRequest("수정된 문항", "수정된 답변", 1000, 2, false, null, null);

            coverLetterItemService.update(1L, req, authentication);

            assertThat(item.getQuestion()).isEqualTo("수정된 문항");
            assertThat(item.getAnswer()).isEqualTo("수정된 답변");
            assertThat(item.getMaxLength()).isEqualTo(1000);
            assertThat(item.getOrderIndex()).isEqualTo(2);
        }

        @Test
        @DisplayName("다른 사용자의 문항 수정 시 예외가 발생한다")
        void throwsWhenUpdatingOtherUsersItem() {
            when(coverLetterItemRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> coverLetterItemService.update(99L, buildRequest(null, null), authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("자소서 문항을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("update 시 orderIndex가 null이면 기존 값을 유지한다")
        void keepsExistingOrderIndexWhenUpdateOrderIndexIsNull() {
            CoverLetterItem item = buildItem(notice, null);
            // item.orderIndex = 0 (빌더 기본값)
            when(coverLetterItemRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(item));

            CoverLetterItemRequest req = new CoverLetterItemRequest("지원 동기를 서술하세요.", "저는...", 500, null, false, null, null);

            coverLetterItemService.update(1L, req, authentication);

            assertThat(item.getOrderIndex()).isEqualTo(0);
        }
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("소유자가 맞으면 문항을 삭제한다")
        void deletesItemForOwner() {
            CoverLetterItem item = buildItem(notice, null);
            when(coverLetterItemRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(item));

            coverLetterItemService.delete(1L, authentication);

            verify(coverLetterItemRepository).delete(item);
        }

        @Test
        @DisplayName("다른 사용자의 문항 삭제 시 예외가 발생한다")
        void throwsWhenDeletingOtherUsersItem() {
            when(coverLetterItemRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> coverLetterItemService.delete(99L, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("자소서 문항을 찾을 수 없습니다");
        }
    }
}
