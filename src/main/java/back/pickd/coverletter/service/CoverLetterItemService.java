package back.pickd.coverletter.service;

import back.pickd.application.entity.Application;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.coverletter.dto.request.CoverLetterItemRequest;
import back.pickd.coverletter.dto.response.CoverLetterItemResponse;
import back.pickd.coverletter.entity.CoverLetterItem;
import back.pickd.coverletter.repository.CoverLetterItemRepository;
import back.pickd.notice.entity.Notice;
import back.pickd.notice.repository.NoticeRepository;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoverLetterItemService {

    private final CoverLetterItemRepository coverLetterItemRepository;
    private final NoticeRepository noticeRepository;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<CoverLetterItemResponse> getByNotice(Long noticeId, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return coverLetterItemRepository
                .findAllByNoticeIdAndUserOrderByOrderIndexAsc(noticeId, user)
                .stream()
                .map(CoverLetterItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CoverLetterItemResponse> getByApplication(Long applicationId, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return coverLetterItemRepository
                .findAllByApplicationIdAndUserOrderByOrderIndexAsc(applicationId, user)
                .stream()
                .map(CoverLetterItemResponse::from)
                .toList();
    }

    @Transactional
    public CoverLetterItemResponse create(CoverLetterItemRequest dto, Authentication auth) {
        User user = userService.findByEmail(auth.getName());

        Notice notice = null;
        Application application = null;

        if (dto.getNoticeId() != null) {
            notice = noticeRepository.findByIdAndUser(dto.getNoticeId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        } else if (dto.getApplicationId() != null) {
            application = applicationRepository.findByIdAndUser(dto.getApplicationId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("지원 공고를 찾을 수 없습니다."));
        } else {
            throw new IllegalArgumentException("noticeId 또는 applicationId 중 하나는 필수입니다.");
        }

        CoverLetterItem item = CoverLetterItem.builder()
                .user(user)
                .notice(notice)
                .application(application)
                .question(dto.getQuestion())
                .answer(dto.getAnswer())
                .maxLength(dto.getMaxLength())
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .aiGenerated(dto.isAiGenerated())
                .build();

        return CoverLetterItemResponse.from(coverLetterItemRepository.save(item));
    }

    @Transactional
    public CoverLetterItemResponse update(Long id, CoverLetterItemRequest dto, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        CoverLetterItem item = coverLetterItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("자소서 문항을 찾을 수 없습니다."));

        item.update(dto.getQuestion(), dto.getAnswer(), dto.getMaxLength(),
                dto.getOrderIndex() != null ? dto.getOrderIndex() : item.getOrderIndex());

        return CoverLetterItemResponse.from(item);
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        CoverLetterItem item = coverLetterItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("자소서 문항을 찾을 수 없습니다."));
        coverLetterItemRepository.delete(item);
    }
}
