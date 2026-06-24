package back.pickd.application.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ApplicationStatus 단위 테스트")
class ApplicationStatusTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── @JsonValue 직렬화 ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("JSON 직렬화")
    class Serialization {

        @Test
        @DisplayName("PREPARING은 '지원 예정'으로 직렬화된다")
        void serializesPreparing() throws Exception {
            String json = objectMapper.writeValueAsString(ApplicationStatus.PREPARING);
            assertThat(json).isEqualTo("\"지원 예정\"");
        }

        @Test
        @DisplayName("SUBMITTED은 '제출 완료'로 직렬화된다")
        void serializesSubmitted() throws Exception {
            String json = objectMapper.writeValueAsString(ApplicationStatus.SUBMITTED);
            assertThat(json).isEqualTo("\"제출 완료\"");
        }

        @Test
        @DisplayName("모든 enum 값이 label로 직렬화된다")
        void allEnumsSerializeToLabel() throws Exception {
            for (ApplicationStatus status : ApplicationStatus.values()) {
                String json = objectMapper.writeValueAsString(status);
                assertThat(json).isEqualTo("\"" + status.getLabel() + "\"");
            }
        }
    }

    // ── @JsonCreator 역직렬화 ──────────────────────────────────────────────────

    @Nested
    @DisplayName("JSON 역직렬화")
    class Deserialization {

        @Test
        @DisplayName("'지원 예정' 문자열을 PREPARING으로 역직렬화한다")
        void deserializesPreparingLabel() throws Exception {
            ApplicationStatus result = objectMapper.readValue("\"지원 예정\"", ApplicationStatus.class);
            assertThat(result).isEqualTo(ApplicationStatus.PREPARING);
        }

        @Test
        @DisplayName("'면접 전형' 문자열을 INTERVIEW로 역직렬화한다")
        void deserializesInterviewLabel() throws Exception {
            ApplicationStatus result = objectMapper.readValue("\"면접 전형\"", ApplicationStatus.class);
            assertThat(result).isEqualTo(ApplicationStatus.INTERVIEW);
        }

        @Test
        @DisplayName("모든 label이 올바른 enum으로 역직렬화된다")
        void allLabelsDeserializeCorrectly() throws Exception {
            for (ApplicationStatus expected : ApplicationStatus.values()) {
                ApplicationStatus result = objectMapper.readValue(
                        "\"" + expected.getLabel() + "\"", ApplicationStatus.class);
                assertThat(result).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("지원하지 않는 label이면 IllegalArgumentException이 발생한다")
        void throwsForUnknownLabel() {
            assertThatThrownBy(() -> ApplicationStatus.from("없는상태"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원하지 않는 지원 상태입니다");
        }
    }

    // ── 캘린더 이벤트 필요 여부 ────────────────────────────────────────────────

    @Nested
    @DisplayName("needsApplyEvent")
    class NeedsApplyEvent {

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"PREPARING", "WRITING"})
        @DisplayName("PREPARING, WRITING은 apply 이벤트가 필요하다")
        void returnsTrueForPreparingAndWriting(ApplicationStatus status) {
            assertThat(status.needsApplyEvent()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"SUBMITTED", "WAITING", "INTERVIEW", "FINAL"})
        @DisplayName("그 외 상태는 apply 이벤트가 필요하지 않다")
        void returnsFalseForOtherStatuses(ApplicationStatus status) {
            assertThat(status.needsApplyEvent()).isFalse();
        }
    }

    @Nested
    @DisplayName("needsInterviewEvent")
    class NeedsInterviewEvent {

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"SUBMITTED", "WAITING", "INTERVIEW"})
        @DisplayName("SUBMITTED, WAITING, INTERVIEW는 interview 이벤트가 필요하다")
        void returnsTrueForInterviewStatuses(ApplicationStatus status) {
            assertThat(status.needsInterviewEvent()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"PREPARING", "WRITING", "FINAL"})
        @DisplayName("그 외 상태는 interview 이벤트가 필요하지 않다")
        void returnsFalseForOtherStatuses(ApplicationStatus status) {
            assertThat(status.needsInterviewEvent()).isFalse();
        }
    }

    @Nested
    @DisplayName("needsDeadlineEvent")
    class NeedsDeadlineEvent {

        @Test
        @DisplayName("FINAL만 deadline 이벤트가 필요하다")
        void returnsTrueOnlyForFinal() {
            assertThat(ApplicationStatus.FINAL.needsDeadlineEvent()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = ApplicationStatus.class, names = {"PREPARING", "WRITING", "SUBMITTED", "WAITING", "INTERVIEW"})
        @DisplayName("FINAL 외 상태는 deadline 이벤트가 필요하지 않다")
        void returnsFalseForNonFinalStatuses(ApplicationStatus status) {
            assertThat(status.needsDeadlineEvent()).isFalse();
        }
    }
}
