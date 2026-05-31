package back.pickd.user.dto;

import back.pickd.user.entity.ExperienceFile;
import back.pickd.user.entity.ExperienceLink;
import back.pickd.user.entity.UserExperience;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class UserExperienceResponse {

    private final String id;
    private final Long userId;
    private final String title;
    private final String experienceType;
    private final String experienceGroup;
    private final String status;
    private final String documentContent;
    private final Map<String, Object> attributes;
    private final List<String> keywords;
    private final List<FileInfo> files;
    private final List<LinkInfo> links;
    private final OffsetDateTime createdAt;
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

    @Getter
    public static class FileInfo {
        private final String id;
        private final String originalFilename;
        private final String fileType;
        private final Long fileSize;
        private final String filePath;
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

    @Getter
    public static class LinkInfo {
        private final String id;
        private final String title;
        private final String url;
        private final String materialType;

        public LinkInfo(ExperienceLink link) {
            this.id = link.getId();
            this.title = link.getTitle();
            this.url = link.getUrl();
            this.materialType = link.getMaterialType();
        }
    }
}
