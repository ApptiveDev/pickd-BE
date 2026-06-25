package back.pickd.application.controller;

import back.pickd.application.dto.request.ApplicationRequest;
import back.pickd.application.dto.response.ApplicationResponse;
import back.pickd.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public List<ApplicationResponse> getAll(Authentication auth) {
        return applicationService.getApplications(auth);
    }

    @PostMapping
    public void add(@RequestBody @Valid ApplicationRequest dto, Authentication auth) throws Exception {
        applicationService.addApplication(dto, auth);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) throws Exception {
        applicationService.deleteApplication(id, auth);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id,
                       @RequestBody @Valid ApplicationRequest dto,
                       Authentication auth) throws Exception {
        applicationService.updateApplication(id, dto, auth);
    }
}
