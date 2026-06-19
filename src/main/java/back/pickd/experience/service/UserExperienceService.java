package back.pickd.experience.service;

import back.pickd.experience.dto.ExperienceCreateDto.Request;
import back.pickd.experience.dto.ExperienceCreateDto.Response;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.entity.ExperienceLink;
import back.pickd.user.entity.User;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.exception.ExperienceMergeConflictException;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.user.repository.UserRepository;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.support.PresetRegistry;
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
    private final PresetRegistry presetRegistry;

    // 경험 수기 생성
    @Transactional
    public Response createExperience(String email, Request request) {
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
                .attributes(presetRegistry.normalizeAttributes(
                        request.getExperienceType(),
                        request.getAttributes()
                ))
                .keywords(request.getKeywords())
                .build();

        experience.updateLinks(toLinks(request));

        UserExperience saved = userExperienceRepository.save(experience);
        return new Response(saved.getId());
    }

    // 경험 단일 조회
    public ExperienceResponse getExperience(String email, String id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserExperience experience = userExperienceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("경험을 찾을 수 없습니다."));
        return new ExperienceResponse(experience);
    }

    // 경험 목록 조회 (필터링 적용)
    public List<ExperienceResponse> getExperiences(String email, ExperienceType type, ExperienceGroup group) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return userExperienceRepository.findByUserWithFilters(user, type, group)
                .stream()
                .map(ExperienceResponse::new)
                .collect(Collectors.toList());
    }
    // 경험 수정/삭제
    @Transactional
    public ExperienceResponse updateExperience(String email, String id, Request request) {
        UserExperience experience = findOwnedExperience(email, id);
        experience.update(
                request.getTitle(),
                request.getExperienceType(),
                request.getExperienceGroup(),
                request.getStatus(),
                request.getDocumentContent(),
                presetRegistry.normalizeAttributes(
                        request.getExperienceType(),
                        request.getAttributes()
                ),
                request.getKeywords()
        );
        experience.updateLinks(toLinks(request));
        return new ExperienceResponse(experience);
    }
    //자기 경험이면 삭제
    @Transactional
    public void deleteExperience(String email, String id) {
        UserExperience experience = findOwnedExperience(email, id);
        userExperienceRepository.delete(experience);
    }

    private UserExperience findOwnedExperience(String email, String id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return userExperienceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("경험을 찾을 수 없습니다."));
    }

    private List<ExperienceLink> toLinks(Request request) {
        if (request.getLinks() == null) {
            return List.of();
        }
        return request.getLinks().stream()
                .map(l -> ExperienceLink.builder()
                        .title(l.getTitle())
                        .url(l.getUrl())
                        .materialType(l.getMaterialType())
                        .documentPosition(l.getDocumentPosition())
                        .build())
                .collect(Collectors.toList());
    }
}
