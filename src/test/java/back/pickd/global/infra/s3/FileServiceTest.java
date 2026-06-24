package back.pickd.global.infra.s3;

import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private UserService userService;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private FileService fileService;

    @Test
    void uploadFileStoresUploadedFileLink() {
        User user = createUser();
        MultipartFile file = createFile();
        String fileUrl = "https://cdn.pickd.com/temp/resume/resume.pdf";

        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(s3Service.uploadFile(file, FileUploadType.TEMP_RESUME, user.getId())).thenReturn(fileUrl);
        when(uploadedFileRepository.save(any(UploadedFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UploadedFileResponse response =
                fileService.uploadFile("user@example.com", file, FileUploadType.TEMP_RESUME);

        ArgumentCaptor<UploadedFile> captor = ArgumentCaptor.forClass(UploadedFile.class);
        verify(uploadedFileRepository).save(captor.capture());
        UploadedFile saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals("resume.pdf", saved.getFileName());
        assertEquals(fileUrl, saved.getFileUrl());
        assertEquals(FileUploadType.TEMP_RESUME, saved.getUploadType());
        assertEquals(file.getSize(), saved.getFileSize());
        assertEquals("application/pdf", saved.getContentType());
        assertEquals(fileUrl, response.fileUrl());
    }

    @Test
    void getFilesReturnsLinksFilteredByType() {
        User user = createUser();
        UploadedFile uploadedFile = UploadedFile.builder()
                .id(1L)
                .user(user)
                .fileName("resume.pdf")
                .fileUrl("https://cdn.pickd.com/temp/resume/resume.pdf")
                .uploadType(FileUploadType.TEMP_RESUME)
                .fileSize(100L)
                .contentType("application/pdf")
                .build();

        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(uploadedFileRepository.findByUserAndUploadTypeOrderByCreatedAtDesc(
                user,
                FileUploadType.TEMP_RESUME
        )).thenReturn(List.of(uploadedFile));

        List<UploadedFileResponse> responses =
                fileService.getFiles("user@example.com", FileUploadType.TEMP_RESUME);

        assertEquals(1, responses.size());
        assertEquals("resume.pdf", responses.get(0).fileName());
        assertEquals(FileUploadType.TEMP_RESUME, responses.get(0).uploadType());
    }

    private User createUser() {
        return User.builder()
                .email("user@example.com")
                .name("테스트")
                .build();
    }

    private MultipartFile createFile() {
        return new MockMultipartFile("file", "resume.pdf", "application/pdf", "pdf".getBytes());
    }
}
