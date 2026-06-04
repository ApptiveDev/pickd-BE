package back.pickd.user.dto;

import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 사용자 경험 수기 입력 요청 DTO
@Schema(description = "사용자 경험 카드 생성 요청")
@Getter
@NoArgsConstructor
public class ExperienceCreateRequestDto {

    @Schema(description = "경험 카드 제목", example = "Apptive 24기 안드로이드 토이 프로젝트")
    @NotBlank(message = "경험 제목은 필수입니다.")
    private String title;

    @Schema(description = "경험 유형", example = "PROJECT")
    @NotNull(message = "경험 유형은 필수입니다.")
    private ExperienceType experienceType;

    @Schema(description = "경험 그룹", example = "NARRATIVE")
    @NotNull(message = "경험 그룹은 필수입니다.")
    private ExperienceGroup experienceGroup;

    @Schema(description = "경험 진행 상태", example = "COMPLETED")
    @NotNull(message = "상태는 필수입니다.")
    private Status status;

    @Schema(description = "경험 수기 본문 또는 STAR-L 정리 내용", example = "[S] 프로젝트 초기 요구사항이 불명확했습니다. [T] ... [A] ... [R] ... [L] ...")
    private String documentContent;

    @Schema(description = "경험 유형별 추가 속성 JSON", example = "{\"role\":\"Backend\",\"teamSize\":4}")
    private Map<String, Object> attributes = new HashMap<>();

    @Schema(description = "경험 핵심 키워드 목록", example = "[\"Spring Boot\",\"협업\",\"API 설계\"]")
    private List<String> keywords = new ArrayList<>();

    @Schema(description = "경험과 연결할 외부 자료 링크 목록")
    private List<LinkRequest> links = new ArrayList<>();

    @Schema(description = "중복 후보가 있어도 강제로 신규 생성할지 여부", example = "false")
    private boolean forceCreate = false;

    @Schema(description = "경험 관련 외부 자료 링크")
    @Getter
    @NoArgsConstructor
    public static class LinkRequest {
        @Schema(description = "링크 제목", example = "GitHub Repository")
        private String title;

        @Schema(description = "링크 URL", example = "https://github.com/example/project")
        private String url;

        @Schema(description = "자료 유형", example = "GITHUB")
        private String materialType;

        @Schema(description = "문서 내 연결 위치", example = "1")
        private Integer documentPosition;
    }
}
