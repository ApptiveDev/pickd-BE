package back.pickd.document.service;

import back.pickd.application.entity.Application;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.document.dto.DocumentRequest;
import back.pickd.document.dto.DocumentResponse;
import back.pickd.document.entity.Document;
import back.pickd.document.repository.DocumentRepository;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long applicationId, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return documentRepository.findAllByApplicationIdAndUser(applicationId, user)
                .stream().map(DocumentResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return documentRepository.findAllByUserOrderByIdDesc(user)
                .stream().map(DocumentResponse::new).toList();
    }

    @Transactional
    public DocumentResponse addDocument(Long applicationId, DocumentRequest request, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        Application application = applicationRepository.findByIdAndUser(applicationId, user)
                .orElseThrow(() -> new IllegalArgumentException("지원 공고를 찾을 수 없습니다."));

        Document document = Document.builder()
                .user(user)
                .application(application)
                .title(request.getTitle())
                .company(request.getCompany())
                .type(request.getType())
                .status(request.getStatus())
                .progress(request.getProgress() == null ? 0 : request.getProgress())
                .content(request.getContent())
                .build();

        return new DocumentResponse(documentRepository.save(document));
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentRequest request, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("서류를 찾을 수 없습니다."));

        document.update(
                request.getTitle(),
                request.getCompany(),
                request.getType(),
                request.getStatus(),
                request.getProgress(),
                request.getContent()
        );
        return new DocumentResponse(document);
    }

    @Transactional
    public void deleteDocument(Long id, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("서류를 찾을 수 없습니다."));
        documentRepository.delete(document);
    }
}
