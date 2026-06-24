package back.pickd.user.entity;

import back.pickd.user.entity.enums.DegreeType;
import back.pickd.user.entity.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_educations")
public class UserEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String schoolName;

    @Column(nullable = false)
    private String department;

    @Column
    private String doubleMajor;

    @Column
    private String minor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DegreeType degreeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus enrollmentStatus;

    @Column(nullable = false)
    private String graduationDate; // "YYYY-MM" format

    @Column
    private Double gpa;

    @Column
    private boolean isTransfer;

    @Column
    private String campus;

    @Column
    private String exchangeExperience;

    @Column
    private String courses;

    public void update(String schoolName, String department, String doubleMajor, String minor,
                       DegreeType degreeType, EnrollmentStatus enrollmentStatus, String graduationDate,
                       Double gpa, boolean isTransfer, String campus, String exchangeExperience, String courses) {
        this.schoolName = schoolName;
        this.department = department;
        this.doubleMajor = doubleMajor;
        this.minor = minor;
        this.degreeType = degreeType;
        this.enrollmentStatus = enrollmentStatus;
        this.graduationDate = graduationDate;
        this.gpa = gpa;
        this.isTransfer = isTransfer;
        this.campus = campus;
        this.exchangeExperience = exchangeExperience;
        this.courses = courses;
    }

    @Builder
    public UserEducation(User user, String schoolName, String department, String doubleMajor, String minor,
                         DegreeType degreeType, EnrollmentStatus enrollmentStatus, String graduationDate,
                         Double gpa, boolean isTransfer, String campus, String exchangeExperience, String courses) {
        this.user = user;
        this.schoolName = schoolName;
        this.department = department;
        this.doubleMajor = doubleMajor;
        this.minor = minor;
        this.degreeType = degreeType;
        this.enrollmentStatus = enrollmentStatus;
        this.graduationDate = graduationDate;
        this.gpa = gpa;
        this.isTransfer = isTransfer;
        this.campus = campus;
        this.exchangeExperience = exchangeExperience;
        this.courses = courses;
    }
}
