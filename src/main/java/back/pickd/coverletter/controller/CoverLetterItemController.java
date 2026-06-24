package back.pickd.coverletter.controller;

import back.pickd.coverletter.dto.request.CoverLetterItemRequest;
import back.pickd.coverletter.dto.response.CoverLetterItemResponse;
import back.pickd.coverletter.service.CoverLetterItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cover-letter")
public class CoverLetterItemController {

    private final CoverLetterItemService coverLetterItemService;

    // GET /api/cover-letter?noticeId=1
    @GetMapping
    public List<CoverLetterItemResponse> getByNotice(
            @RequestParam(required = false) Long noticeId,
            @RequestParam(required = false) Long applicationId,
            Authentication auth) {
        if (noticeId != null) {
            return coverLetterItemService.getByNotice(noticeId, auth);
        } else if (applicationId != null) {
            return coverLetterItemService.getByApplication(applicationId, auth);
        }
        throw new IllegalArgumentException("noticeId 또는 applicationId 파라미터가 필요합니다.");
    }

    // POST /api/cover-letter
    @PostMapping
    public CoverLetterItemResponse create(
            @RequestBody @Valid CoverLetterItemRequest dto,
            Authentication auth) {
        return coverLetterItemService.create(dto, auth);
    }

    // PUT /api/cover-letter/{id}
    @PutMapping("/{id}")
    public CoverLetterItemResponse update(
            @PathVariable Long id,
            @RequestBody @Valid CoverLetterItemRequest dto,
            Authentication auth) {
        return coverLetterItemService.update(id, dto, auth);
    }

    // DELETE /api/cover-letter/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        coverLetterItemService.delete(id, auth);
    }
}
