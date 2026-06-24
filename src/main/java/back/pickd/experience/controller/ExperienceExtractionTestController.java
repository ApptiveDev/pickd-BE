package back.pickd.experience.controller;

import back.pickd.experience.dto.ExperienceExtractionTestDto.PresetDefinition;
import back.pickd.experience.dto.ExperienceExtractionTestDto.StateResponse;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.service.ExperienceExtractionTestService;
import back.pickd.experience.support.PresetRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
@Profile("local")
@RequiredArgsConstructor
public class ExperienceExtractionTestController {

    private final PresetRegistry presetRegistry;
    private final ExperienceExtractionTestService testService;
    private final ObjectMapper objectMapper;

    @GetMapping("/experience-extraction-test")
    public String page(Authentication authentication, Model model)
            throws JsonProcessingException {
        List<PresetDefinition> presets = Arrays.stream(ExperienceType.values())
                .map(type -> new PresetDefinition(
                        type.name(),
                        type.getKoreanName(),
                        presetRegistry.getExperienceGroup(type).name(),
                        presetRegistry.getPresetFields(type)
                ))
                .toList();

        model.addAttribute("authenticated",
                authentication != null && authentication.isAuthenticated());
        model.addAttribute("userEmail",
                authentication != null ? authentication.getName() : null);
        model.addAttribute("presetDefinitions", presets);
        model.addAttribute("presetDefinitionsJson",
                objectMapper.writeValueAsString(presets));
        return "experience-extraction-test";
    }

    @GetMapping("/internal/experience-extraction-test/state")
    @ResponseBody
    public StateResponse state(Authentication authentication) {
        return testService.getState(authentication.getName());
    }
}
