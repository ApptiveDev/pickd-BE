package back.pickd.user.entity;

import back.pickd.user.entity.enums.AuthProvider;
import back.pickd.user.entity.enums.OnboardingStep;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password; // For EMAIL provider

    @Column(nullable = false)
    private String name;

    @Column
    private String nickname;

    @Column
    private String picture;

    @Column
    private String phone;

    @Column
    private String birthDate; // YYYYMMDD

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column
    private String intro; // 한 줄 소개

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingStep onboardingStep;

    @Column(nullable = false)
    private boolean serviceAgreed;

    @Column(nullable = false)
    private boolean privacyAgreed;

    @Column(nullable = false)
    private boolean marketingAgreed;

    @Column(nullable = false)
    private boolean pushAgreed;

    @Column
    private LocalDateTime lastLoginDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserEducation education;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserLocation location;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserInterest interest;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPrepStatus prepStatus;

    @Builder
    public User(String email, String password, String name, String nickname, String picture, String phone, 
                AuthProvider provider, OnboardingStep onboardingStep,
                boolean serviceAgreed, boolean privacyAgreed, boolean marketingAgreed, boolean pushAgreed) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.picture = picture;
        this.phone = phone;
        this.provider = provider != null ? provider : AuthProvider.GOOGLE;
        this.onboardingStep = onboardingStep != null ? onboardingStep : OnboardingStep.NONE;
        this.serviceAgreed = serviceAgreed;
        this.privacyAgreed = privacyAgreed;
        this.marketingAgreed = marketingAgreed;
        this.pushAgreed = pushAgreed;
        this.lastLoginDate = LocalDateTime.now();
    }

    public void setEducation(UserEducation education) {
        this.education = education;
    }

    public void setLocation(UserLocation location) {
        this.location = location;
    }

    public void setInterest(UserInterest interest) {
        this.interest = interest;
    }

    public void setPrepStatus(UserPrepStatus prepStatus) {
        this.prepStatus = prepStatus;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        this.lastLoginDate = LocalDateTime.now();
        return this;
    }

    public void updatePicture(String picture) {
        this.picture = picture;
    }

    public void updateTerms(boolean serviceAgreed, boolean privacyAgreed, boolean marketingAgreed, boolean pushAgreed) {
        this.serviceAgreed = serviceAgreed;
        this.privacyAgreed = privacyAgreed;
        this.marketingAgreed = marketingAgreed;
        this.pushAgreed = pushAgreed;
    }

    public void updateOnboardingStep(OnboardingStep step) {
        this.onboardingStep = step;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateIntro(String intro) {
        this.intro = intro;
    }

    public void verify(String name, String birthDate, String phone) {
        this.name = name;
        this.birthDate = birthDate;
        this.phone = phone;
        this.isVerified = true;
    }
}
