package back.pickd.experience.dto;

import back.pickd.experience.entity.ExperienceFile;
import back.pickd.experience.entity.ExperienceLink;
import back.pickd.experience.entity.UserExperience;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ExperienceResponse {

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

    public ExperienceResponse(UserExperience experience) {
        this.id = experience.getId();
        this.userId = experience.getUser().getId();
        this.title = experience.getTitle();
        this.experienceType = experience.getExperienceType() != null
                ? experience.getExperienceType().name()
                : null;
        this.experienceGroup = experience.getExperienceGroup() != null
                ? experience.getExperienceGroup().name()
                : null;
        this.status = experience.getStatus() != null ? experience.getStatus().name() : null;
        this.documentContent = experience.getDocumentContent();
        this.attributes = experience.getAttributes();
        this.keywords = experience.getKeywords();
        this.files = experience.getFiles().stream().map(FileInfo::new).collect(Collectors.toList());
        this.links = experience.getLinks().stream().map(LinkInfo::new).collect(Collectors.toList());
        this.createdAt = experience.getCreatedAt();
        this.updatedAt = experience.getUpdatedAt();
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
