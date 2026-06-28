package back.pickd.experience.controller;

import back.pickd.auth.jwt.JwtTokenProvider;
import back.pickd.experience.entity.ExperienceTemp;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.repository.ExperienceTempRepository;
import back.pickd.user.entity.User;
import back.pickd.user.entity.enums.AuthProvider;
import back.pickd.user.entity.enums.OnboardingStep;
import back.pickd.user.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 로컬 E2E 테스트 전용 시드 컨트롤러.
 *
 * <p><b>local 프로파일에서만 활성화됩니다. 절대 운영에 배포하지 마세요.</b>
 *
 * <ul>
 *   <li>POST /internal/seed/user         — 테스트 유저 생성 + JWT 반환</li>
 *   <li>POST /internal/seed/temp         — ExperienceTemp 주입 + ID 목록 반환</li>
 *   <li>DELETE /internal/seed/temp       — 해당 유저의 temp 전체 삭제</li>
 * </ul>
 */
@RestController
@RequestMapping("/internal/seed")
@Profile("local")
@RequiredArgsConstructor
public class LocalSeedController {

    private final UserRepository userRepository;
    private final ExperienceTempRepository tempRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /internal/seed/user
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/user")
    public UserSeedResponse seedUser(@RequestBody UserSeedRequest req) {
        String email = req.getEmail() != null ? req.getEmail() : "test@pickd.local";
        String name  = req.getName()  != null ? req.getName()  : "테스트유저";

        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .picture(null)
                                .provider(AuthProvider.GOOGLE)
                                .onboardingStep(OnboardingStep.COMPLETED)
                                .serviceAgreed(true)
                                .privacyAgreed(true)
                                .marketingAgreed(false)
                                .pushAgreed(false)
                                .build()
                )
        );

        var auth = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(), "", List.of(new SimpleGrantedAuthority("ROLE_USER"))
                ),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.createToken(auth);
        return new UserSeedResponse(user.getId(), user.getEmail(), user.getName(), token);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /internal/seed/temp
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * ExperienceTemp 레코드를 주입합니다.
     * Step1 S3 업로드 없이 Step2/Step3 E2E를 검증할 때 사용합니다.
     *
     * <p>요청 예시:
     * <pre>
     * {
     *   "email": "test@pickd.local",
     *   "resumeUrl": "https://example.com/resume.pdf",
     *   "experiences": [
     *     { "name": "금융 AI 프로젝트", "type": "PROJECT",  "group": "NARRATIVE" },
     *     { "name": "TOEIC 930점",     "type": "LANGUAGE", "group": "SPEC"      }
     *   ]
     * }
     * </pre>
     */
    @PostMapping("/temp")
    public TempSeedResponse seedTemp(@RequestBody TempSeedRequest req) {
        String email     = req.getEmail()     != null ? req.getEmail()     : "test@pickd.local";
        String resumeUrl = req.getResumeUrl() != null ? req.getResumeUrl() : "https://example.com/dummy-resume.pdf";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + email + " — 먼저 /internal/seed/user 를 호출하세요."));

        List<TempSeedRequest.TempEntry> entries = req.getExperiences() != null
                ? req.getExperiences()
                : defaultEntries();

        List<Long> ids = entries.stream()
                .map(e -> tempRepository.save(
                        ExperienceTemp.builder()
                                .user(user)
                                .experienceName(e.getName())
                                .experienceType(ExperienceType.valueOf(e.getType()))
                                .experienceGroup(ExperienceGroup.valueOf(e.getGroup()))
                                .resumeUrl(resumeUrl)
                                .build()
                ).getId())
                .toList();

        return new TempSeedResponse(email, ids, resumeUrl);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /internal/seed/temp
    // ──────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/temp")
    public Map<String, Object> deleteTemp(@RequestParam(defaultValue = "test@pickd.local") String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + email));
        List<ExperienceTemp> temps = tempRepository.findByUser(user);
        tempRepository.deleteAll(temps);
        return Map.of("deleted", temps.size(), "email", email);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // defaults
    // ──────────────────────────────────────────────────────────────────────────

    private List<TempSeedRequest.TempEntry> defaultEntries() {
        return List.of(
                new TempSeedRequest.TempEntry("금융 AI Agent 프로젝트", "PROJECT", "NARRATIVE"),
                new TempSeedRequest.TempEntry("TOEIC 930점", "LANGUAGE", "SPEC")
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DTOs
    // ──────────────────────────────────────────────────────────────────────────

    @Getter
    public static class UserSeedRequest {
        private String email;
        private String name;
    }

    @Getter
    public static class UserSeedResponse {
        private final Long   id;
        private final String email;
        private final String name;
        private final String accessToken;

        public UserSeedResponse(Long id, String email, String name, String accessToken) {
            this.id = id; this.email = email; this.name = name; this.accessToken = accessToken;
        }
    }

    @Getter
    public static class TempSeedRequest {
        private String email;
        private String resumeUrl;
        private List<TempEntry> experiences;

        @Getter
        public static class TempEntry {
            private String name;
            private String type;
            private String group;

            public TempEntry() {}
            public TempEntry(String name, String type, String group) {
                this.name = name; this.type = type; this.group = group;
            }
        }
    }

    @Getter
    public static class TempSeedResponse {
        private final String     email;
        private final List<Long> tempIds;
        private final String     resumeUrl;

        public TempSeedResponse(String email, List<Long> tempIds, String resumeUrl) {
            this.email = email; this.tempIds = tempIds; this.resumeUrl = resumeUrl;
        }
    }
}
