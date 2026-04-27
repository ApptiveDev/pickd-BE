package back.pickd.user.service;

import back.pickd.user.dto.onboarding.*;
import back.pickd.user.entity.*;
import back.pickd.user.entity.enums.OnboardingStep;
import back.pickd.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final UserEducationRepository educationRepository;
    private final UserLocationRepository locationRepository;
    private final UserInterestRepository interestRepository;
    private final UserPrepStatusRepository prepStatusRepository;
    private final UserExperienceRepository experienceRepository;
    private final UserCertificationRepository certificationRepository;

    @Transactional
    public void saveStep1Terms(User user, Step1TermsRequest request) {
        User targetUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        validateStep(targetUser, OnboardingStep.NONE);
        updateTerms(targetUser, request);
        targetUser.updateOnboardingStep(OnboardingStep.TERMS);
        userRepository.save(targetUser);
    }

    @Transactional
    public void saveVerification(User user, StepVerificationRequest request) {
        User targetUser = userRepository.findById(user.getId()).orElseThrow();
        validateStep(targetUser, OnboardingStep.TERMS);
        targetUser.verify(request.getName(), request.getBirthDate(), request.getPhone());
        targetUser.updateOnboardingStep(OnboardingStep.VERIFICATION);
    }

    @Transactional
    public void saveStep2BasicInfo(User user, Step2BasicInfoRequest request) {
        User targetUser = userRepository.findById(user.getId()).orElseThrow();
        validateStep(targetUser, OnboardingStep.VERIFICATION);
        targetUser.updateNickname(request.getNickname());
        targetUser.updateIntro(request.getIntro());
        
        UserLocation location = targetUser.getLocation();
        if (location == null) {
            location = UserLocation.builder().user(targetUser).build();
        }
        location.update(request.getCurrentResidence(), request.getDesiredLocations(), request.getDetailedAddress());
        locationRepository.save(location);
        
        targetUser.updateOnboardingStep(OnboardingStep.BASIC);
    }

    @Transactional
    public void saveStep3Education(User user, Step3EducationRequest request) {
        User targetUser = userRepository.findById(user.getId()).orElseThrow();
        validateStep(targetUser, OnboardingStep.BASIC);
        
        UserEducation education = targetUser.getEducation();
        if (education == null) {
            education = UserEducation.builder().user(targetUser).build();
        }
        education.update(request.getSchoolName(), request.getDepartment(), request.getDoubleMajor(), request.getMinor(),
                request.getDegreeType(), request.getEnrollmentStatus(), request.getGraduationDate(),
                request.getGpa(), request.isTransfer(), request.getCampus(), request.getExchangeExperience(), request.getCourses());
        educationRepository.save(education);
        
        targetUser.updateOnboardingStep(OnboardingStep.EDUCATION);
    }

    @Transactional
    public void saveStep4Interest(User user, Step4InterestRequest request) {
        User targetUser = userRepository.findById(user.getId()).orElseThrow();
        validateStep(targetUser, OnboardingStep.EDUCATION);
        
        UserInterest interest = targetUser.getInterest();
        if (interest == null) {
            interest = UserInterest.builder().user(targetUser).build();
        }
        interest.update(request.getIndustries(), request.getJobGroups(), request.getEmploymentType(),
                request.getCompanyTypes(), request.getKeywords(), request.getSpecificJob(),
                request.getTargetCompany(), request.getSalaryRange(), request.getJobPriority(),
                request.getIndustryPriority(), request.getWorkType(), request.getApplyTypes());
        interestRepository.save(interest);
        
        targetUser.updateOnboardingStep(OnboardingStep.INTERESTS);
    }

    @Transactional
    public void saveStep5PrepStatus(User user, Step5PrepStatusRequest request) {
        User targetUser = userRepository.findById(user.getId()).orElseThrow();
        validateStep(targetUser, OnboardingStep.INTERESTS);
        
        UserPrepStatus prepStatus = targetUser.getPrepStatus();
        if (prepStatus == null) {
            prepStatus = UserPrepStatus.builder().user(targetUser).build();
        }
        prepStatus.update(request.getTargetPeriod(), request.getCurrentStage(), request.getFocusItems(),
                request.isHasResume(), request.isHasBaseEssay(), request.isHasPortfolio(),
                request.getPreparingExams(), request.getTargetApplyCount());
        prepStatusRepository.save(prepStatus);

        // 경험/자격증은 N개이므로 기존 것을 지우고 새로 저장 (Update 시 중복 방지)
        experienceRepository.deleteByUser(targetUser);
        if (request.getExperiences() != null) {
            request.getExperiences().forEach(exp -> {
                experienceRepository.save(UserExperience.builder()
                        .user(targetUser).type(exp.getType()).title(exp.getTitle())
                        .description(exp.getDescription()).startDate(exp.getStartDate()).endDate(exp.getEndDate()).build());
            });
        }

        certificationRepository.deleteByUser(targetUser);
        if (request.getCertifications() != null) {
            request.getCertifications().forEach(cert -> {
                certificationRepository.save(UserCertification.builder()
                        .user(targetUser).type(cert.getType()).name(cert.getName())
                        .score(cert.getScore()).acquisitionDate(cert.getAcquisitionDate()).build());
            });
        }
        
        targetUser.updateOnboardingStep(OnboardingStep.COMPLETED);
    }

    @Transactional
    public void resetOnboarding(User user) {
        User targetUser = userRepository.findById(user.getId()).orElseThrow();
        targetUser.updateOnboardingStep(OnboardingStep.NONE);
        userRepository.save(targetUser);
    }

    private void updateTerms(User user, Step1TermsRequest request) {
        user.updateTerms(request.isServiceAgreed(), request.isPrivacyAgreed(), request.isMarketingAgreed(), request.isPushAgreed());
    }

    private void validateStep(User user, OnboardingStep requiredStep) {
        if (user.getOnboardingStep().ordinal() < requiredStep.ordinal()) {
            throw new IllegalStateException("잘못된 온보딩 단계입니다. (현재: " + user.getOnboardingStep() + ", 기대: " + requiredStep + ")");
        }
    }
}