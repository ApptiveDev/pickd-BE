package back.pickd.document.controller;

import back.pickd.document.dto.DocumentRequest;
import back.pickd.document.dto.DocumentResponse;
import back.pickd.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public List<DocumentResponse> getAllDocuments(Authentication auth) {
        return documentService.getAllDocuments(auth);
    }

    @GetMapping("/{applicationId}")
    public List<DocumentResponse> getDocuments(@PathVariable Long applicationId, Authentication auth) {
        return documentService.getDocuments(applicationId, auth);
    }

    @PostMapping("/{applicationId}")
    public DocumentResponse addDocument(@PathVariable Long applicationId,
                                        @RequestBody DocumentRequest request,
                                        Authentication auth) {
        return documentService.addDocument(applicationId, request, auth);
    }

    @PutMapping("/{id}")
    public DocumentResponse updateDocument(@PathVariable Long id,
                                           @RequestBody DocumentRequest request,
                                           Authentication auth) {
        return documentService.updateDocument(id, request, auth);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id, Authentication auth) {
        documentService.deleteDocument(id, auth);
    }
}
