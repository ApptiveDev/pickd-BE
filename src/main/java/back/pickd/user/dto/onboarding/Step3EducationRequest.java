package back.pickd.user.dto.onboarding;

import back.pickd.user.entity.enums.DegreeType;
import back.pickd.user.entity.enums.EnrollmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Step3EducationRequest {
    private String schoolName;
    private String department;
    private String doubleMajor;
    private String minor;
    private DegreeType degreeType;
    private EnrollmentStatus enrollmentStatus;
    private String graduationDate;
    private Double gpa;
    private boolean isTransfer;
    private String campus;
    private String exchangeExperience;
    private String courses;
}
