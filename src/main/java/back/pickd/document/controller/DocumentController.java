package back.pickd.document.controller;

import back.pickd.document.dto.DocumentRequest;
import back.pickd.document.entity.Document;
import back.pickd.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/document")
public class DocumentController {
    private final DocumentService documentService;

    @GetMapping("/{applicationId}")
    public List<Document> getDocuments(
            @PathVariable Long applicationId
    ) {
        return documentService.getDocuments(applicationId);
    }

    @PostMapping("/{applicationId}")
    public Document addDocument(
            @PathVariable Long applicationId,
            @RequestBody DocumentRequest request
    ) {
        return documentService.addDocument(applicationId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(
            @PathVariable Long id
    ) {
        documentService.deleteDocument(id);
    }
}