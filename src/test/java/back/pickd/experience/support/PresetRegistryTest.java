package back.pickd.experience.support;

import back.pickd.experience.enums.ExperienceType;
import back.pickd.global.infra.ai.dto.AiExperiencePresetSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresetRegistryTest {

    private PresetRegistry presetRegistry;

    @BeforeEach
    void setUp() {
        presetRegistry = new PresetRegistry();
    }

    @Test
    @DisplayName("영문 키가 그대로 들어오면 정상적으로 매칭된다")
    void normalizeKey_withEnglishKey() {
        String result = presetRegistry.normalizeKey(ExperienceType.PROJECT, "project_name");
        assertEquals("project_name", result);
    }

    @Test
    @DisplayName("대소문자 및 언더스코어가 혼합된 키도 정상적으로 정규화된다")
    void normalizeKey_withMixedCaseAndUnderscore() {
        String result = presetRegistry.normalizeKey(ExperienceType.PROJECT, "Project_Name");
        assertEquals("project_name", result);
    }

    @Test
    @DisplayName("한글 라벨이 들어오면 매칭되는 영문 키로 변환된다")
    void normalizeKey_withKoreanLabel() {
        String result = presetRegistry.normalizeKey(ExperienceType.PROJECT, "프로젝트명");
        assertEquals("project_name", result);
    }

    @Test
    @DisplayName("공백이 포함된 한글 라벨도 정상적으로 영문 키로 변환된다")
    void normalizeKey_withSpacedKoreanLabel() {
        String result = presetRegistry.normalizeKey(ExperienceType.PROJECT, "프로젝트 명");
        assertEquals("project_name", result);
    }

    @Test
    @DisplayName("매칭되는 키나 라벨이 없는 커스텀 필드는 원본 키를 그대로 반환한다")
    void normalizeKey_withCustomField() {
        String result = presetRegistry.normalizeKey(ExperienceType.PROJECT, "사용자 정의 필드");
        assertEquals("사용자 정의 필드", result);
    }

    @Test
    @DisplayName("normalizeAttributes는 Map 내의 모든 키를 정규화하여 반환한다")
    void normalizeAttributes() {
        Map<String, Object> input = new HashMap<>();
        input.put("프로젝트 명", "픽드 백엔드");
        input.put("Period", "2023.01 ~ 2023.12");
        input.put("알 수 없는 필드", "커스텀 값");

        Map<String, Object> result = presetRegistry.normalizeAttributes(ExperienceType.PROJECT, input);

        assertEquals("픽드 백엔드", result.get("project_name"));
        assertEquals("2023.01 ~ 2023.12", result.get("period"));
        assertEquals("커스텀 값", result.get("알 수 없는 필드"));
    }

    @Test
    @DisplayName("알바 한글 라벨을 표준 영문 키로 변환한다")
    void normalizeAttributes_withAlbaLabels() {
        Map<String, Object> input = Map.of(
                "근무처명", "카페",
                "업무 유형", "고객 응대",
                "주요 경험", "운영 개선"
        );

        Map<String, Object> result =
                presetRegistry.normalizeAttributes(ExperienceType.ALBA, input);

        assertEquals("카페", result.get("workplace_name"));
        assertEquals("고객 응대", result.get("work_type"));
        assertEquals("운영 개선", result.get("key_experience"));
    }

    @Test
    @DisplayName("학부연구생 한글 라벨을 표준 영문 키로 변환한다")
    void normalizeAttributes_withResearchLabels() {
        Map<String, Object> input = Map.of(
                "연구실명", "데이터 연구실",
                "연구 주제", "추천 시스템",
                "주요 결과물", "학술 포스터"
        );

        Map<String, Object> result =
                presetRegistry.normalizeAttributes(ExperienceType.RESEARCH, input);

        assertEquals("데이터 연구실", result.get("lab_name"));
        assertEquals("추천 시스템", result.get("research_topic"));
        assertEquals("학술 포스터", result.get("deliverables"));
    }

    @Test
    @DisplayName("null 또는 빈 attributes는 빈 Map으로 반환한다")
    void normalizeAttributes_withNullAndEmptyMap() {
        assertTrue(presetRegistry.normalizeAttributes(ExperienceType.PROJECT, null).isEmpty());
        assertTrue(presetRegistry.normalizeAttributes(
                ExperienceType.PROJECT,
                Map.of()
        ).isEmpty());
    }

    @Test
    @DisplayName("선택된 경험 유형만 AI Step2 프리셋 스키마로 변환한다")
    void getAiPresetSchemas_returnsOnlySelectedTypes() {
        List<AiExperiencePresetSchema> schemas = presetRegistry.getAiPresetSchemas(List.of(
                ExperienceType.PROJECT,
                ExperienceType.LANGUAGE,
                ExperienceType.PROJECT
        ));

        assertEquals(2, schemas.size());
        assertEquals("PROJECT", schemas.get(0).getExperienceType());
        assertEquals("상세 서술형", schemas.get(0).getExperienceGroup());
        assertEquals("project_name", schemas.get(0).getFields().get(0).getKey());
        assertEquals("LANGUAGE", schemas.get(1).getExperienceType());
        assertEquals("스펙·증빙형", schemas.get(1).getExperienceGroup());
    }
}
