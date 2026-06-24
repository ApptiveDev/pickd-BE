package back.pickd.global.infra.s3;

import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final S3Service s3Service;
    private final UserService userService;
    private final UploadedFileRepository uploadedFileRepository;

    @Transactional
    public UploadedFileResponse uploadFile(String email, MultipartFile file, FileUploadType type) {
        User user = userService.findByEmail(email);
        String fileUrl = s3Service.uploadFile(file, type, user.getId());

        UploadedFile uploadedFile = UploadedFile.builder()
                .user(user)
                .fileName(resolveFileName(file))
                .fileUrl(fileUrl)
                .uploadType(type)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        return new UploadedFileResponse(uploadedFileRepository.save(uploadedFile));
    }

    public List<UploadedFileResponse> getFiles(String email, FileUploadType type) {
        User user = userService.findByEmail(email);
        List<UploadedFile> files = type == null
                ? uploadedFileRepository.findByUserOrderByCreatedAtDesc(user)
                : uploadedFileRepository.findByUserAndUploadTypeOrderByCreatedAtDesc(user, type);

        return files.stream()
                .map(UploadedFileResponse::new)
                .toList();
    }

    private String resolveFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return (originalFilename == null || originalFilename.isBlank()) ? "unknown" : originalFilename;
    }
}
