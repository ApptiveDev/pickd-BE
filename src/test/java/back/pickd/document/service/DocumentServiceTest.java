package back.pickd.document.service;

import back.pickd.application.entity.Application;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.document.dto.DocumentRequest;
import back.pickd.document.dto.DocumentResponse;
import back.pickd.document.entity.Document;
import back.pickd.document.enums.DocumentStatus;
import back.pickd.document.enums.DocumentType;
import back.pickd.document.repository.DocumentRepository;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService 단위 테스트")
class DocumentServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock UserService userService;
    @Mock Authentication authentication;

    @InjectMocks DocumentService documentService;

    User user;
    Application application;

    @BeforeEach
    void setUp() {
        user = User.builder().email("test@test.com").name("테스터").build();
        application = Application.builder()
                .user(user).company("카카오").jobTitle("백엔드")
                .status(ApplicationStatus.WRITING).important(false).build();

        when(authentication.getName()).thenReturn("test@test.com");
        when(userService.findByEmail("test@test.com")).thenReturn(user);
    }

    private DocumentRequest buildRequest() {
        DocumentRequest req = new DocumentRequest();
        req.setTitle("자기소개서");
        req.setCompany("카카오");
        req.setType(DocumentType.RESUME);
        req.setStatus(DocumentStatus.DRAFT);
        req.setProgress(30);
        req.setContent("내용...");
        return req;
    }

    private Document buildDocument() {
        return Document.builder()
                .user(user)
                .application(application)
                .title("자기소개서")
                .company("카카오")
                .type(DocumentType.RESUME)
                .status(DocumentStatus.DRAFT)
                .progress(30)
                .content("내용...")
                .build();
    }

    // ── getAllDocuments ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllDocuments")
    class GetAllDocuments {

        @Test
        @DisplayName("사용자의 전체 서류를 최신순으로 반환한다")
        void returnsAllDocumentsForUser() {
            Document doc1 = buildDocument();
            Document doc2 = buildDocument();
            when(documentRepository.findAllByUserOrderByIdDesc(user)).thenReturn(List.of(doc2, doc1));

            List<DocumentResponse> result = documentService.getAllDocuments(authentication);

            assertThat(result).hasSize(2);
            verify(documentRepository).findAllByUserOrderByIdDesc(user);
        }
    }

    // ── getDocuments (by applicationId) ───────────────────────────────────────

    @Nested
    @DisplayName("getDocuments")
    class GetDocuments {

        @Test
        @DisplayName("특정 공고에 속한 서류 목록을 반환한다")
        void returnsDocumentsByApplicationId() {
            when(documentRepository.findAllByApplicationIdAndUser(1L, user))
                    .thenReturn(List.of(buildDocument()));

            List<DocumentResponse> result = documentService.getDocuments(1L, authentication);

            assertThat(result).hasSize(1);
            verify(documentRepository).findAllByApplicationIdAndUser(1L, user);
        }
    }

    // ── addDocument ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addDocument")
    class AddDocument {

        @Test
        @DisplayName("소유권 검증 후 서류를 저장한다")
        void savesDocumentAfterOwnershipCheck() {
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(application));
            ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
            when(documentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            documentService.addDocument(1L, buildRequest(), authentication);

            Document saved = captor.getValue();
            assertThat(saved.getUser()).isEqualTo(user);
            assertThat(saved.getApplication()).isEqualTo(application);
            assertThat(saved.getType()).isEqualTo(DocumentType.RESUME);
        }

        @Test
        @DisplayName("progress가 null이면 0으로 저장된다")
        void savesProgressAsZeroWhenNull() {
            DocumentRequest req = buildRequest();
            req.setProgress(null);
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(application));
            ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
            when(documentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            documentService.addDocument(1L, req, authentication);

            assertThat(captor.getValue().getProgress()).isEqualTo(0);
        }

        @Test
        @DisplayName("다른 사용자의 Application에 서류 추가 시 예외가 발생한다")
        void throwsWhenApplicationNotOwnedByUser() {
            when(applicationRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.addDocument(99L, buildRequest(), authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원 공고를 찾을 수 없습니다");
        }
    }

    // ── updateDocument ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateDocument")
    class UpdateDocument {

        @Test
        @DisplayName("소유자가 맞으면 서류 내용을 수정한다")
        void updatesDocumentContent() {
            Document doc = buildDocument();
            when(documentRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(doc));

            DocumentRequest req = buildRequest();
            req.setTitle("수정된 자소서");
            req.setStatus(DocumentStatus.COMPLETED);
            req.setProgress(100);

            documentService.updateDocument(1L, req, authentication);

            assertThat(doc.getTitle()).isEqualTo("수정된 자소서");
            assertThat(doc.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
            assertThat(doc.getProgress()).isEqualTo(100);
        }

        @Test
        @DisplayName("다른 사용자의 서류 수정 시 예외가 발생한다")
        void throwsWhenUpdatingOtherUsersDocument() {
            when(documentRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.updateDocument(99L, buildRequest(), authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("서류를 찾을 수 없습니다");
        }
    }

    // ── deleteDocument ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteDocument")
    class DeleteDocument {

        @Test
        @DisplayName("소유자가 맞으면 서류를 삭제한다")
        void deletesDocumentForOwner() {
            Document doc = buildDocument();
            when(documentRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(doc));

            documentService.deleteDocument(1L, authentication);

            verify(documentRepository).delete(doc);
        }

        @Test
        @DisplayName("다른 사용자의 서류 삭제 시 예외가 발생한다")
        void throwsWhenDeletingOtherUsersDocument() {
            when(documentRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.deleteDocument(99L, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("서류를 찾을 수 없습니다");
        }
    }
}
