package back.pickd.user.service;

import back.pickd.user.dto.ExperienceCreateRequestDto;
import back.pickd.user.dto.ExperienceCreateResponseDto;
import back.pickd.user.dto.UserExperienceResponse;
import back.pickd.user.entity.ExperienceLink;
import back.pickd.user.entity.User;
import back.pickd.user.entity.UserExperience;
import back.pickd.user.exception.ExperienceMergeConflictException;
import back.pickd.user.repository.UserExperienceRepository;
import back.pickd.user.repository.UserRepository;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 사용자 경험 CR 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserExperienceService {

    private final UserExperienceRepository userExperienceRepository;
    private final UserRepository userRepository;
    private final ExperienceMergeService experienceMergeService;

    // 경험 수기 생성
    @Transactional
    public ExperienceCreateResponseDto createExperience(String email, ExperienceCreateRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!request.isForceCreate()) {
            experienceMergeService.findCreateMergeCandidate(user, request)
                    .ifPresent(conflict -> {
                        throw new ExperienceMergeConflictException(conflict);
                    });
        }

        UserExperience experience = UserExperience.builder()
                .user(user)
                .title(request.getTitle())
                .experienceType(request.getExperienceType())
                .experienceGroup(request.getExperienceGroup())
                .status(request.getStatus())
                .documentContent(request.getDocumentContent())
                .attributes(request.getAttributes())
                .keywords(request.getKeywords())
                .build();

        if (request.getLinks() != null) {
            List<ExperienceLink> links = request.getLinks().stream()
                    .map(l -> ExperienceLink.builder()
                            .title(l.getTitle())
                            .url(l.getUrl())
                            .materialType(l.getMaterialType())
                            .documentPosition(l.getDocumentPosition())
                            .build())
                    .collect(Collectors.toList());
            experience.updateLinks(links);
        }

        UserExperience saved = userExperienceRepository.save(experience);
        return new ExperienceCreateResponseDto(saved.getId());
    }

    // 경험 단일 조회
    public UserExperienceResponse getExperience(String email, String id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserExperience experience = userExperienceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("경험을 찾을 수 없습니다."));
        return new UserExperienceResponse(experience);
    }

    // 경험 목록 조회 (필터링 적용)
    public List<UserExperienceResponse> getExperiences(String email, ExperienceType type, ExperienceGroup group) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return userExperienceRepository.findByUserWithFilters(user, type, group)
                .stream()
                .map(UserExperienceResponse::new)
                .collect(Collectors.toList());
    }
}
