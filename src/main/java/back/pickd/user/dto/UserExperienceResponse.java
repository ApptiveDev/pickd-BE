package back.pickd.user.dto;

import back.pickd.user.entity.ExperienceFile;
import back.pickd.user.entity.ExperienceLink;
import back.pickd.user.entity.UserExperience;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = "사용자 경험 카드 상세 응답")
@Getter
public class UserExperienceResponse {

    @Schema(description = "경험 ID", example = "exp_7f8a9b2c")
    private final String id;

    @Schema(description = "경험 소유 사용자 ID", example = "12")
    private final Long userId;

    @Schema(description = "경험 제목", example = "Apptive 24기 안드로이드 토이 프로젝트")
    private final String title;

    @Schema(description = "경험 유형", example = "PROJECT")
    private final String experienceType;

    @Schema(description = "경험 그룹", example = "NARRATIVE")
    private final String experienceGroup;

    @Schema(description = "경험 진행 상태", example = "COMPLETED")
    private final String status;

    @Schema(description = "경험 수기 본문 또는 STAR-L 정리 내용")
    private final String documentContent;

    @Schema(description = "경험 유형별 추가 속성 JSON")
    private final Map<String, Object> attributes;

    @Schema(description = "경험 핵심 키워드 목록", example = "[\"Spring Boot\",\"협업\",\"API 설계\"]")
    private final List<String> keywords;

    @Schema(description = "경험에 첨부된 파일 목록")
    private final List<FileInfo> files;

    @Schema(description = "경험에 연결된 외부 자료 링크 목록")
    private final List<LinkInfo> links;

    @Schema(description = "경험 생성 시각")
    private final OffsetDateTime createdAt;

    @Schema(description = "경험 수정 시각")
    private final OffsetDateTime updatedAt;

    public UserExperienceResponse(UserExperience exp) {
        this.id = exp.getId();
        this.userId = exp.getUser().getId();
        this.title = exp.getTitle();
        this.experienceType = exp.getExperienceType() != null ? exp.getExperienceType().name() : null;
        this.experienceGroup = exp.getExperienceGroup() != null ? exp.getExperienceGroup().name() : null;
        this.status = exp.getStatus() != null ? exp.getStatus().name() : null;
        this.documentContent = exp.getDocumentContent();
        this.attributes = exp.getAttributes();
        this.keywords = exp.getKeywords();
        this.files = exp.getFiles().stream().map(FileInfo::new).collect(Collectors.toList());
        this.links = exp.getLinks().stream().map(LinkInfo::new).collect(Collectors.toList());
        this.createdAt = exp.getCreatedAt();
        this.updatedAt = exp.getUpdatedAt();
    }

    @Schema(description = "경험 첨부 파일 정보")
    @Getter
    public static class FileInfo {
        @Schema(description = "파일 ID", example = "file_123")
        private final String id;

        @Schema(description = "원본 파일명", example = "resume.pdf")
        private final String originalFilename;

        @Schema(description = "MIME 타입", example = "application/pdf")
        private final String fileType;

        @Schema(description = "파일 크기(byte)", example = "102400")
        private final Long fileSize;

        @Schema(description = "파일 접근 URL 또는 저장 경로", example = "https://cdn.pickd.co.kr/experience/general/1/resume.pdf")
        private final String filePath;

        @Schema(description = "파일 출처", example = "RESUME_ORIGINAL")
        private final String source;

        public FileInfo(ExperienceFile file) {
            this.id = file.getId();
            this.originalFilename = file.getOriginalFilename();
            this.fileType = file.getFileType();
            this.fileSize = file.getFileSize();
            this.filePath = file.getFilePath();
            this.source = file.getSource();
        }
    }

    @Schema(description = "경험 외부 자료 링크 정보")
    @Getter
    public static class LinkInfo {
        @Schema(description = "링크 ID", example = "link_123")
        private final String id;

        @Schema(description = "링크 제목", example = "GitHub Repository")
        private final String title;

        @Schema(description = "링크 URL", example = "https://github.com/example/project")
        private final String url;

        @Schema(description = "자료 유형", example = "GITHUB")
        private final String materialType;

        public LinkInfo(ExperienceLink link) {
            this.id = link.getId();
            this.title = link.getTitle();
            this.url = link.getUrl();
            this.materialType = link.getMaterialType();
        }
    }
}
