package back.pickd.notice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentType {
    FULL_TIME("정규직"),
    CONTRACT("계약직"),
    INTERN("인턴"),
    PART_TIME("아르바이트"),
    FREELANCER("프리랜서"),
    OTHER("기타");

    private final String description;
}
