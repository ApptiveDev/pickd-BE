package back.pickd.user.dto;

import back.pickd.user.entity.User;
import back.pickd.user.entity.enums.OnboardingStep;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "사용자 프로필 및 온보딩 상태 응답")
@Getter
@Builder
public class UserResponseDto {
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "OAuth 프로필 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 닉네임", example = "길동")
    private String nickname;

    @Schema(description = "OAuth 프로필 이미지 URL", example = "https://lh3.googleusercontent.com/a/example")
    private String picture;

    @Schema(description = "온보딩 진행 단계", example = "COMPLETED")
    private OnboardingStep onboardingStep;
    
    @Schema(description = "현재 거주지", example = "서울특별시 강남구")
    private String currentResidence;

    @Schema(description = "학교명", example = "픽디대학교")
    private String schoolName;

    @Schema(description = "전공", example = "컴퓨터공학과")
    private String major;

    @Schema(description = "목표 취업 준비 기간", example = "2026 상반기")
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
