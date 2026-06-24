package back.pickd.coverletter.entity;

import back.pickd.application.entity.Application;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.notice.entity.Notice;
import back.pickd.notice.enums.JobCategory;
import back.pickd.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CoverLetterItem 엔티티 단위 테스트")
class CoverLetterItemTest {

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
    }

    // @PrePersist onCreate() 메서드를 리플렉션으로 직접 호출
    private void invokeOnCreate(CoverLetterItem item) throws Exception {
        Method method = CoverLetterItem.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        try {
            method.invoke(item);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        }
    }

    @Nested
    @DisplayName("@PrePersist 검증")
    class PrePersistValidation {

        @Test
        @DisplayName("notice만 있으면 정상 저장된다")
        void passesWhenNoticeIsSet() throws Exception {
            CoverLetterItem item = CoverLetterItem.builder()
                    .user(user).notice(notice).question("q").orderIndex(0).build();

            assertThatCode(() -> invokeOnCreate(item)).doesNotThrowAnyException();
            assertThat(item.getCreatedAt()).isNotNull();
            assertThat(item.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("application만 있으면 정상 저장된다")
        void passesWhenApplicationIsSet() throws Exception {
            CoverLetterItem item = CoverLetterItem.builder()
                    .user(user).application(application).question("q").orderIndex(0).build();

            assertThatCode(() -> invokeOnCreate(item)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("notice와 application 모두 null이면 IllegalStateException이 발생한다")
        void throwsWhenBothNotiiceAndApplicationAreNull() throws Exception {
            CoverLetterItem item = CoverLetterItem.builder()
                    .user(user).question("q").orderIndex(0).build();

            assertThatThrownBy(() -> invokeOnCreate(item))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Notice 또는 Application 중 하나에 반드시 연결되어야 합니다");
        }
    }
}
