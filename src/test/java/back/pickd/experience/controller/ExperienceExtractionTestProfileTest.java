package back.pickd.experience.controller;

import back.pickd.experience.service.ExperienceExtractionTestService;
import back.pickd.experience.support.PresetRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExperienceExtractionTestProfileTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withBean(PresetRegistry.class)
                    .withBean(ObjectMapper.class)
                    .withBean(
                            ExperienceExtractionTestService.class,
                            () -> mock(ExperienceExtractionTestService.class)
                    )
                    .withUserConfiguration(TestConfiguration.class);

    @Test
    void controllerIsNotCreatedOutsideLocalProfile() {
        contextRunner
                .withPropertyValues("spring.profiles.active=prod")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(ExperienceExtractionTestController.class));
    }

    @Configuration(proxyBeanMethods = false)
    @Import(ExperienceExtractionTestController.class)
    static class TestConfiguration {
    }
}
