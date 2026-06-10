package back.pickd.user.utils;

import back.pickd.user.entity.enums.ExperienceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
