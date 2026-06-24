package back.pickd.experience.controller;

import back.pickd.experience.dto.ExperienceExtractionV2Dto.Step2Request;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.Step2Response;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.Step3Request;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.Step3Response;
import back.pickd.experience.dto.ExperienceExtractionV2Dto.PendingBatchesResponse;
import back.pickd.experience.service.ExperienceExtractionV2Service;
import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/experiences/extract")
@RequiredArgsConstructor
@Tag(
        name = "Experience Extraction V2",
        description = "경험 상세 추출, 중복 후보 저장 및 최종 선택 API"
)
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class ExperienceExtractionV2Controller {

    private final ExperienceExtractionV2Service extractionService;

    @GetMapping("/duplicates/pending")
    @Operation(
            summary = "미처리 중복 경험 조회",
            description = """
                    현재 로그인 사용자의 Step2 미처리 중복 batch를 최신순으로 조회합니다.

                    - Step2 응답을 잃었거나 화면을 새로고침한 경우 Step3 선택 화면을 복구할 수 있습니다.
                    - 각 batch는 Step3에 전달할 duplicateBatchId와 전체 duplicateGroups를 포함합니다.
                    - 다른 사용자의 batch와 이미 완료된 batch는 반환하지 않습니다.
                    - 미처리 batch가 없으면 batches는 빈 배열입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "미처리 중복 경험 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = PendingBatchesResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "batches": [
                                                {
                                                  "duplicateBatchId": "batch-uuid",
                                                  "createdAt": "2026-06-22T19:30:00+09:00",
                                                  "duplicateGroups": [
                                                    {
                                                      "groupId": "group-uuid",
                                                      "items": [
                                                        {
                                                          "itemId": "existing-experience-id",
                                                          "source": "EXISTING",
                                                          "similarity": null,
                                                          "experience": {
                                                            "title": "기존 프로젝트",
                                                            "experienceType": "PROJECT",
                                                            "experienceGroup": "NARRATIVE",
                                                            "status": "COMPLETED",
                                                            "documentContent": "기존 본문",
                                                            "attributes": {},
                                                            "keywords": []
                                                          }
                                                        },
                                                        {
                                                          "itemId": "draft-uuid",
                                                          "source": "EXTRACTED",
                                                          "similarity": 0.91,
                                                          "experience": {
                                                            "title": "새 추출 프로젝트",
                                                            "experienceType": "PROJECT",
                                                            "experienceGroup": "NARRATIVE",
                                                            "status": "COMPLETED",
                                                            "documentContent": "새 본문",
                                                            "attributes": {},
                                                            "keywords": []
                                                          }
                                                        }
                                                      ]
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PendingBatchesResponse> getPendingDuplicates(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(extractionService.getPendingDuplicates(
                authentication.getName()
        ));
    }

    @PostMapping("/step2")
    @Operation(
            summary = "선택 경험 상세 추출 및 중복 후보 생성",
            description = """
                    Step1에서 선택한 임시 경험을 선택 순서대로 상세 추출합니다.

                    - Spring PresetRegistry에서 선택된 경험 유형의 필드 스키마만 AI 서버에 전달합니다.
                    - 기존 저장 경험과 같은 요청에서 앞서 저장된 신규 경험을 대상으로 중복을 판정합니다.
                    - 비중복 경험은 즉시 저장합니다.
                    - 중복 경험은 서버 draft로 저장하고 duplicateBatchId와 duplicateGroups를 반환합니다.
                    - 중복 후보가 없으면 duplicateBatchId는 null이고 duplicateGroups는 빈 배열입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상세 추출 및 중복 분류 성공",
                    content = @Content(
                            schema = @Schema(implementation = Step2Response.class),
                            examples = @ExampleObject(
                                    name = "중복 후보가 있는 응답",
                                    value = """
                                            {
                                              "savedExperiences": [
                                                {
                                                  "id": "saved-experience-id",
                                                  "userId": 1,
                                                  "title": "비중복 프로젝트",
                                                  "experienceType": "PROJECT",
                                                  "experienceGroup": "NARRATIVE",
                                                  "status": "COMPLETED",
                                                  "documentContent": "프로젝트 본문",
                                                  "attributes": {
                                                    "project_name": "비중복 프로젝트"
                                                  },
                                                  "keywords": ["실행력"],
                                                  "files": [],
                                                  "links": []
                                                }
                                              ],
                                              "duplicateBatchId": "batch-uuid",
                                              "duplicateGroups": [
                                                {
                                                  "groupId": "group-uuid",
                                                  "items": [
                                                    {
                                                      "itemId": "existing-experience-id",
                                                      "source": "EXISTING",
                                                      "similarity": null,
                                                      "experience": {
                                                        "title": "기존 프로젝트",
                                                        "experienceType": "PROJECT",
                                                        "experienceGroup": "NARRATIVE",
                                                        "status": "COMPLETED",
                                                        "documentContent": "기존 본문",
                                                        "attributes": {},
                                                        "keywords": []
                                                      }
                                                    },
                                                    {
                                                      "itemId": "draft-uuid",
                                                      "source": "EXTRACTED",
                                                      "similarity": 0.91,
                                                      "experience": {
                                                        "title": "새로 추출한 프로젝트",
                                                        "experienceType": "PROJECT",
                                                        "experienceGroup": "NARRATIVE",
                                                        "status": "COMPLETED",
                                                        "documentContent": "새 본문",
                                                        "attributes": {},
                                                        "keywords": []
                                                      }
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "임시 경험 누락, 소유권 오류, 그룹·유형 불일치 또는 AI 응답 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Step2Response> extractStep2(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid Step2Request request
    ) {
        return ResponseEntity.ok(extractionService.extractStep2(
                authentication.getName(),
                request.getSelectedTempIds()
        ));
    }

    @PostMapping("/step3")
    @Operation(
            summary = "중복 경험 최종 선택 반영",
            description = """
                    Step2가 반환한 중복 batch의 모든 그룹에 대해 남길 itemId를 제출합니다.

                    - 기존 경험만 선택: 기존 경험 유지, 추출 draft 삭제
                    - 추출 draft만 선택: 기존 경험 삭제, 선택 draft 신규 저장
                    - 기존 경험과 draft를 함께 선택: 둘 다 유지
                    - 여러 draft 선택: 선택한 draft를 모두 신규 경험으로 저장
                    - 선택되지 않은 기존 경험과 draft는 삭제
                    - 모든 그룹을 한 요청에 포함해야 하며 하나의 Spring 트랜잭션으로 처리합니다.
                    - AI 서버는 호출하지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "선택 반영 성공",
                    content = @Content(
                            schema = @Schema(implementation = Step3Response.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "selectedExperiences": [
                                                {
                                                  "id": "selected-experience-id",
                                                  "userId": 1,
                                                  "title": "최종 선택 경험",
                                                  "experienceType": "PROJECT",
                                                  "experienceGroup": "NARRATIVE",
                                                  "status": "COMPLETED",
                                                  "documentContent": "경험 본문",
                                                  "attributes": {},
                                                  "keywords": [],
                                                  "files": [],
                                                  "links": []
                                                }
                                              ],
                                              "deletedExperienceIds": ["unselected-existing-id"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "batch 누락, 그룹 누락, 빈 선택, 잘못된 itemId 또는 이미 처리된 batch",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Step3Response> confirmStep3(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid Step3Request request
    ) {
        return ResponseEntity.ok(extractionService.confirmStep3(
                authentication.getName(),
                request
        ));
    }
}
