package back.pickd.user.service;

import back.pickd.user.dto.onboarding.OnboardingRequest;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.ExperienceExtractionBatchRepository;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.user.entity.*;
import back.pickd.user.entity.enums.*;
import java.util.ArrayList;
import java.util.List;
import back.pickd.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final UserLocationRepository locationRepository;
    private final UserEducationRepository educationRepository;
    private final UserInterestRepository interestRepository;
    private final UserPrepStatusRepository prepStatusRepository;
    private final UserExperienceRepository experienceRepository;
    private final ExperienceExtractionBatchRepository extractionBatchRepository;
    private final UserCertificationRepository certificationRepository;

    @Transactional
    public User updateOnboarding(String email, OnboardingRequest dto) {
        User user = userRepository.findByEmail(email).orElseThrow();

        if (dto.getServiceAgreed() != null) {
            user.updateTerms(dto.getServiceAgreed(), dto.getPrivacyAgreed(), dto.getMarketingAgreed(), dto.getPushAgreed());
        }
        if (dto.getName() != null) {
            user.verify(dto.getName(), dto.getBirthDate(), dto.getPhone());
        }
        if (dto.getNickname() != null) {
            user.updateNickname(dto.getNickname());
            user.updateIntro(dto.getIntro());
        }

        updateRelatedEntities(user, dto);
        updateCurrentStep(user, dto);

        return user;
    }

    private void updateRelatedEntities(User user, OnboardingRequest dto) {
        if (dto.getCurrentResidence() != null) {
            UserLocation loc = user.getLocation() != null ? user.getLocation() : UserLocation.builder().user(user).build();
            loc.update(dto.getCurrentResidence(), dto.getDesiredLocations(), dto.getDetailedAddress());
            user.setLocation(loc);
        }
        if (dto.getSchoolName() != null) {
            UserEducation edu = user.getEducation() != null ? user.getEducation() : UserEducation.builder().user(user).build();
            edu.update(dto.getSchoolName(), dto.getDepartment(), dto.getDoubleMajor(), dto.getMinor(), dto.getDegreeType(), 
                       dto.getEnrollmentStatus(), dto.getGraduationDate(), dto.getGpa(), false, dto.getCampus(), null, null);
            user.setEducation(edu);
        }
        if (dto.getIndustries() != null) {
            UserInterest inter = user.getInterest() != null ? user.getInterest() : UserInterest.builder().user(user).build();
            inter.update(dto.getIndustries(), dto.getJobGroups(), dto.getEmploymentType(), dto.getCompanyTypes(), dto.getKeywords(), null, dto.getTargetCompany(), dto.getSalaryRange(), null, null, null, null);
            user.setInterest(inter);
        }
        if (dto.getTargetPeriod() != null) {
            UserPrepStatus prep = user.getPrepStatus() != null ? user.getPrepStatus() : UserPrepStatus.builder().user(user).build();
            prep.update(dto.getTargetPeriod(), dto.getCurrentStage(), dto.getFocusItems(), 
                        dto.getHasResume() != null && dto.getHasResume(), 
                        dto.getHasBaseEssay() != null && dto.getHasBaseEssay(), 
                        dto.getHasPortfolio() != null && dto.getHasPortfolio(), null, null);
            user.setPrepStatus(prep);
            updateListInfos(user, dto);
        }
    }

    private void updateCurrentStep(User user, OnboardingRequest dto) {
        OnboardingStep step = dto.detectStep();
        if (step != null) user.updateOnboardingStep(step);
    }

    private void updateListInfos(User user, OnboardingRequest dto) {
        if (dto.getExperiences() != null) {
            deleteExistingExperiences(user);
            List<UserExperience> experiences = new ArrayList<>();
            for (OnboardingRequest.ExperienceDto e : dto.getExperiences()) {
                ExperienceType expType;
                try {
                    expType = ExperienceType.valueOf(e.getType());  // 영문 enum (INTERN 등)
                } catch (IllegalArgumentException ex) {
                    expType = ExperienceType.fromKoreanName(e.getType());  // 한국어 (인턴 등)
                }
                experiences.add(
                    UserExperience.builder()
                        .user(user)
                        .title(e.getTitle())
                        .experienceType(expType)
                        .experienceGroup(expType.defaultGroup())
                        .status(Status.COMPLETED)
                        .attributes(expType.buildAttributes(e.getStartDate(), e.getEndDate()))
                        .documentContent("")
                        .build()
                );
            }
            experienceRepository.saveAll(experiences);
        }
        if (dto.getCertifications() != null) {
            certificationRepository.deleteByUser(user);
            List<UserCertification> certifications = new ArrayList<>();
            for (OnboardingRequest.CertificationDto c : dto.getCertifications()) {
                certifications.add(
                    UserCertification.builder()
                        .user(user)
                        .type("LICENSE")
                        .name(c.getName())
                        .score(c.getScore())
                        .acquisitionDate(c.getAcquisitionDate())
                        .build()
                );
            }
            certificationRepository.saveAll(certifications);
        }
    }

    private void deleteExistingExperiences(User user) {
        extractionBatchRepository.deleteAll(
                extractionBatchRepository.findByUserOrderByCreatedAtDesc(user)
        );
        extractionBatchRepository.flush();

        experienceRepository.deleteAll(
                experienceRepository.findByUserOrderByCreatedAtDesc(user)
        );
        experienceRepository.flush();
    }

    @Transactional
    public void resetOnboarding(String email) {
        userRepository.findByEmail(email).ifPresent(u -> u.updateOnboardingStep(OnboardingStep.NONE));
    }
}
