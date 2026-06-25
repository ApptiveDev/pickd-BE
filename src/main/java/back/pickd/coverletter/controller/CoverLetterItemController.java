package back.pickd.coverletter.controller;

import back.pickd.coverletter.dto.request.CoverLetterItemRequest;
import back.pickd.coverletter.dto.response.CoverLetterItemResponse;
import back.pickd.coverletter.service.CoverLetterItemService;
import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cover-letter")
@Tag(name = "CoverLetter", description = "자기소개서 문항 관리 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class CoverLetterItemController {

    private final CoverLetterItemService coverLetterItemService;

    @GetMapping
    @Operation(
            summary = "자기소개서 문항 조회",
            description = "noticeId 또는 applicationId 중 하나를 쿼리 파라미터로 전달합니다. 둘 다 없으면 400을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CoverLetterItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "noticeId / applicationId 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<CoverLetterItemResponse> getByNotice(
            @RequestParam(required = false) Long noticeId,
            @RequestParam(required = false) Long applicationId,
            @Parameter(hidden = true) Authentication auth) {
        if (noticeId != null) {
            return coverLetterItemService.getByNotice(noticeId, auth);
        } else if (applicationId != null) {
            return coverLetterItemService.getByApplication(applicationId, auth);
        }
        throw new IllegalArgumentException("noticeId 또는 applicationId 파라미터가 필요합니다.");
    }

    @PostMapping
    @Operation(summary = "자기소개서 문항 생성", description = "새 자기소개서 문항을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CoverLetterItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CoverLetterItemResponse create(
            @RequestBody @Valid CoverLetterItemRequest dto,
            @Parameter(hidden = true) Authentication auth) {
        return coverLetterItemService.create(dto, auth);
    }

    @PutMapping("/{id}")
    @Operation(summary = "자기소개서 문항 수정", description = "문항 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = CoverLetterItemResponse.class))),
            @ApiResponse(responseCode = "403", description = "소유권 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CoverLetterItemResponse update(
            @PathVariable Long id,
            @RequestBody @Valid CoverLetterItemRequest dto,
            @Parameter(hidden = true) Authentication auth) {
        return coverLetterItemService.update(id, dto, auth);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "자기소개서 문항 삭제", description = "문항을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "소유권 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@PathVariable Long id, @Parameter(hidden = true) Authentication auth) {
        coverLetterItemService.delete(id, auth);
    }
}
