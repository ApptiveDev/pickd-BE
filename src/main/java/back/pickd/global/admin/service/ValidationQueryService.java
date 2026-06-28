package back.pickd.global.admin.service;

import back.pickd.application.enums.ApplicationStatus;
import back.pickd.document.enums.DocumentStatus;
import back.pickd.document.enums.DocumentType;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.ExtractionBatchStatus;
import back.pickd.experience.enums.Status;
import back.pickd.global.admin.dto.EnumColumnResult;
import back.pickd.notice.enums.EmploymentType;
import back.pickd.notice.enums.JobCategory;
import back.pickd.user.entity.enums.AuthProvider;
import back.pickd.user.entity.enums.DegreeType;
import back.pickd.user.entity.enums.EnrollmentStatus;
import back.pickd.user.entity.enums.OnboardingStep;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DB에 저장된 ENUM 컬럼 값이 Java Enum 정의와 일치하는지 검증한다.
 *
 * 네이티브 쿼리로 raw 문자열을 직접 조회하므로,
 * Hibernate 매핑 오류가 있는 잘못된 값도 탐지 가능하다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ValidationQueryService {

    @PersistenceContext
    private EntityManager em;

    public List<EnumColumnResult> validateAll() {
        return List.of(
                // applications
                validate("applications",  "status",          "ApplicationStatus",    ApplicationStatus.class),

                // documents
                validate("documents",     "type",            "DocumentType",         DocumentType.class),
                validate("documents",     "status",          "DocumentStatus",       DocumentStatus.class),

                // notices
                validate("notices",       "category",        "JobCategory",          JobCategory.class),
                validate("notices",       "employment_type", "EmploymentType",       EmploymentType.class),

                // user_experiences
                validate("user_experiences", "experience_type",  "ExperienceType",   ExperienceType.class),
                validate("user_experiences", "experience_group", "ExperienceGroup",  ExperienceGroup.class),
                validate("user_experiences", "status",           "Status",           Status.class),

                // experience_temps
                validate("experience_temps", "experience_type",  "ExperienceType",   ExperienceType.class),
                validate("experience_temps", "experience_group", "ExperienceGroup",  ExperienceGroup.class),

                // experience_extraction_drafts
                validate("experience_extraction_drafts", "experience_type",  "ExperienceType",  ExperienceType.class),
                validate("experience_extraction_drafts", "experience_group", "ExperienceGroup", ExperienceGroup.class),
                validate("experience_extraction_drafts", "status",           "Status",          Status.class),

                // experience_extraction_batches
                validate("experience_extraction_batches", "status", "ExtractionBatchStatus", ExtractionBatchStatus.class),

                // users
                validate("users", "provider",        "AuthProvider",  AuthProvider.class),
                validate("users", "onboarding_step", "OnboardingStep", OnboardingStep.class),

                // user_educations
                validate("user_educations", "degree_type",       "DegreeType",       DegreeType.class),
                validate("user_educations", "enrollment_status", "EnrollmentStatus", EnrollmentStatus.class)
        );
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> EnumColumnResult validate(
            String table, String column, String enumClassName, Class<E> enumClass) {

        // Java Enum에 정의된 합법 값 목록
        List<String> validValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
        Set<String> validSet = Set.copyOf(validValues);

        List<String> dbValues;
        long totalRows;

        try {
            // raw 문자열로 조회 - 잘못된 값도 탐지 가능
            dbValues = em.createNativeQuery(
                            "SELECT DISTINCT " + column + " FROM " + table +
                            " WHERE " + column + " IS NOT NULL ORDER BY " + column)
                    .getResultList();

            totalRows = ((Number) em.createNativeQuery(
                            "SELECT COUNT(*) FROM " + table)
                    .getSingleResult()).longValue();

        } catch (Exception e) {
            log.warn("검증 쿼리 실패: {}.{} — {}", table, column, e.getMessage());
            dbValues = List.of("⚠ 쿼리 오류: " + e.getMessage());
            totalRows = -1;
        }

        List<String> invalidValues = dbValues.stream()
                .filter(v -> !validSet.contains(v))
                .collect(Collectors.toList());

        return new EnumColumnResult(table, column, enumClassName, validValues, dbValues, invalidValues, totalRows);
    }
}
