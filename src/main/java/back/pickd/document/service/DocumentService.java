package back.pickd.document.service;

import back.pickd.application.entity.Application;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.document.dto.DocumentRequest;
import back.pickd.document.entity.Document;
import back.pickd.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;

    public List<Document> getDocuments(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId);
    }
    public Document addDocument(
            Long applicationId,
            DocumentRequest request
    ) {
        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow();

        Document document = Document.builder()
                .title(request.getTitle())
                .company(request.getCompany())
                .type(request.getType())
                .progress(
                    request.getProgress() == null
                            ? 0
                            : request.getProgress()
                )
                .status(request.getStatus())
                .content(request.getContent())
                .updatedAt(LocalDateTime.now())
                .application(application)
                .build();
        return documentRepository.save(document);
    }
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}