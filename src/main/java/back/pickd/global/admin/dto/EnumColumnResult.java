package back.pickd.global.admin.dto;

import java.util.List;
import java.util.Set;

/**
 * 특정 테이블 컬럼의 ENUM 검증 결과를 담는 DTO.
 *
 * @param tableName   DB 테이블명
 * @param columnName  컬럼명
 * @param enumClass   Java Enum 클래스명
 * @param validValues Java Enum에 정의된 합법 값 목록
 * @param dbValues    DB에 실제 저장된 distinct 값 목록
 * @param invalidValues DB에만 존재하고 Java Enum에 없는 값 (비어있으면 PASS)
 * @param totalRows   해당 컬럼의 전체 row 수
 */
public record EnumColumnResult(
        String tableName,
        String columnName,
        String enumClass,
        List<String> validValues,
        List<String> dbValues,
        List<String> invalidValues,
        long totalRows
) {
    public boolean isPassed() {
        return invalidValues.isEmpty();
    }

    public String statusBadge() {
        return isPassed() ? "PASS" : "FAIL";
    }
}
