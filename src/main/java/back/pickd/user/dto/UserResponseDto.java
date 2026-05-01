package back.pickd.user.dto;

import back.pickd.user.entity.User;
import back.pickd.user.entity.enums.OnboardingStep;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserResponseDto {
    private String email;
    private String name;
    private String nickname;
    private String picture;
    private OnboardingStep onboardingStep;
    
    private String currentResidence;
    private String schoolName;
    private String major;
    private String targetPeriod;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .picture(user.getPicture())
                .onboardingStep(user.getOnboardingStep())
                .currentResidence(user.getLocation() != null ? user.getLocation().getCurrentResidence() : null)
                .schoolName(user.getEducation() != null ? user.getEducation().getSchoolName() : null)
                .major(user.getEducation() != null ? user.getEducation().getDepartment() : null)
                .targetPeriod(user.getPrepStatus() != null ? user.getPrepStatus().getTargetPeriod() : null)
                .build();
    }
}