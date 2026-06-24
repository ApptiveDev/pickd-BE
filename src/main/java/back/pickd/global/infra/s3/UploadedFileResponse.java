package back.pickd.global.infra.s3;

import java.time.LocalDateTime;

public record UploadedFileResponse(
        Long id,
        String fileName,
        String fileUrl,
        FileUploadType uploadType,
        Long fileSize,
        String contentType,
        LocalDateTime createdAt
) {

    public UploadedFileResponse(UploadedFile file) {
        this(
                file.getId(),
                file.getFileName(),
                file.getFileUrl(),
                file.getUploadType(),
                file.getFileSize(),
                file.getContentType(),
                file.getCreatedAt()
        );
    }
}
