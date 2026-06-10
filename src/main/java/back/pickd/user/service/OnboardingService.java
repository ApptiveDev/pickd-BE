package back.pickd.user.service;

import back.pickd.user.dto.onboarding.OnboardingRequest;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.experience.repository.UserExperienceRepository;
import back.pickd.user.entity.*;
import back.pickd.user.entity.enums.*;
import java.util.HashMap;
import java.util.Map;
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
        if (dto.getTargetPeriod() != null) user.updateOnboardingStep(OnboardingStep.COMPLETED);
        else if (dto.getIndustries() != null) user.updateOnboardingStep(OnboardingStep.INTERESTS);
        else if (dto.getSchoolName() != null) user.updateOnboardingStep(OnboardingStep.EDUCATION);
        else if (dto.getNickname() != null) user.updateOnboardingStep(OnboardingStep.BASIC);
        else if (dto.getName() != null) user.updateOnboardingStep(OnboardingStep.VERIFICATION);
        else if (dto.getServiceAgreed() != null) user.updateOnboardingStep(OnboardingStep.TERMS);
    }

    private void updateListInfos(User user, OnboardingRequest dto) {
        if (dto.getExperiences() != null) {
            experienceRepository.deleteByUser(user);
            dto.getExperiences().forEach(e -> {
                ExperienceType expType = ExperienceType.PROJECT;
                ExperienceGroup expGroup = ExperienceGroup.NARRATIVE;
                
                String rawType = e.getType();
                if ("AWARD".equals(rawType)) {
                    expType = ExperienceType.AWARD;
                    expGroup = ExperienceGroup.SPEC;
                } else if ("INTERN".equals(rawType)) {
                    expType = ExperienceType.INTERN;
                    expGroup = ExperienceGroup.NARRATIVE;
                } else if ("ACTIVITY".equals(rawType)) {
                    expType = ExperienceType.ACTIVITY;
                    expGroup = ExperienceGroup.NARRATIVE;
                } else if ("PROJECT".equals(rawType)) {
                    expType = ExperienceType.PROJECT;
                    expGroup = ExperienceGroup.NARRATIVE;
                }

                Map<String, Object> attributes = new HashMap<>();
                String periodVal = (e.getStartDate() != null ? e.getStartDate() : "") 
                        + " ~ " 
                        + (e.getEndDate() != null ? e.getEndDate() : "");
                
                if (expType == ExperienceType.AWARD) {
                    attributes.put("award_date", e.getEndDate());
                } else if (expType == ExperienceType.INTERN) {
                    attributes.put("period", periodVal);
                } else if (expType == ExperienceType.ACTIVITY) {
                    attributes.put("period", periodVal);
                } else {
                    attributes.put("period", periodVal);
                }

                experienceRepository.save(
                    UserExperience.builder()
                        .user(user)
                        .title(e.getTitle())
                        .experienceType(expType)
                        .experienceGroup(expGroup)
                        .status(Status.COMPLETED)
                        .attributes(attributes)
                        .documentContent("")
                        .build()
                );
            });
        }
        if (dto.getCertifications() != null) {
            certificationRepository.deleteByUser(user);
            dto.getCertifications().forEach(c -> certificationRepository.save(
                UserCertification.builder()
                    .user(user)
                    .type("LICENSE")
                    .name(c.getName())
                    .score(c.getScore())
                    .acquisitionDate(c.getAcquisitionDate())
                    .build()
            ));
        }
    }

    @Transactional
    public void resetOnboarding(String email) {
        userRepository.findByEmail(email).ifPresent(u -> u.updateOnboardingStep(OnboardingStep.NONE));
    }
}
