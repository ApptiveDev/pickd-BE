package back.pickd.user.utils;

import back.pickd.user.entity.enums.ExperienceType;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class PresetRegistry { // 왜 사용되지 않는지 알아보기, 각필드를 어떻게 저장하고 있는지를, 핕터링

    private static final Map<ExperienceType, List<String>> PRESET_MAP = new EnumMap<>(ExperienceType.class);

    static {
        // 상세 서술형 (Narrative) 프리셋 필드 정의
        PRESET_MAP.put(ExperienceType.PROJECT, List.of("프로젝트명", "진행 기간", "역할", "소속/팀", "주요 성과"));
        PRESET_MAP.put(ExperienceType.ACTIVITY, List.of("활동명", "주관기관", "활동 기간", "역할", "주요 성과"));
        PRESET_MAP.put(ExperienceType.INTERN, List.of("회사/기관명", "직무/부서", "근무/참여 기간", "담당 업무", "주요 성과"));
        PRESET_MAP.put(ExperienceType.CONTEST, List.of("공모전명", "주관기관", "참가 기간", "역할", "수상/결과"));
        PRESET_MAP.put(ExperienceType.VOLUNTEER, List.of("활동명", "기관/단체", "활동 기간", "역할"));
        PRESET_MAP.put(ExperienceType.EXCHANGE, List.of("국가/도시", "학교명", "파견 기간", "전공/수강 분야"));
        // 알바
        // 학부 연구생 추가

        // 스펙·증빙 (Spec) 프리셋 필드 정의
        PRESET_MAP.put(ExperienceType.LANGUAGE, List.of("시험명", "점수/등급", "응시일", "유효기간", "성적표")); // 수험 번호 추가
        PRESET_MAP.put(ExperienceType.LICENSE, List.of("자격증명", "발급기관", "취득일", "유효기간", "자격증 사본")); // 자격 번호
        PRESET_MAP.put(ExperienceType.AWARD, List.of("수상명", "수여기관", "수상일", "수상 구분", "수상 증빙"));
        PRESET_MAP.put(ExperienceType.COURSE, List.of("과목명", "이수 학기", "학점", "성적", "관련 분야")); // 과목 설명
        PRESET_MAP.put(ExperienceType.EDUCATION, List.of("교육명", "운영기관", "교육 기간", "수료 여부", "수료증"));
    }

    public List<String> getPresetKeys(ExperienceType type) {
        return PRESET_MAP.getOrDefault(type, Collections.emptyList());
    }

    /**
     * AI가 자의적으로 바꾼 필드명(예: '역할분담', '배정부서')을 백엔드의 표준 키명으로 매핑해주는 보정 유틸리티
     */
    public String normalizeKey(ExperienceType type, String rawKey) {
        if (rawKey == null) return null;
        String cleanKey = rawKey.replace(" ", "").trim();
        List<String> standardKeys = getPresetKeys(type);

        for (String stdKey : standardKeys) {
            String cleanStd = stdKey.replace(" ", "");
            if (cleanStd.contains(cleanKey) || cleanKey.contains(cleanStd)) {
                return stdKey; // 일치하는 표준 프리셋 필드가 감지되면 해당 표준 필드명 리턴
            }
        }
        return rawKey; // 완전 새로운 커스텀 필드라면 그대로 유지시킴
    }
}
