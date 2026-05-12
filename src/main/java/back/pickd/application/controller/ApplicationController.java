package back.pickd.application.controller;

import back.pickd.application.entity.Application;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.application.dto.request.ApplicationRequest;
import back.pickd.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/application")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    @GetMapping
    public List<Application> getAll() {
        return applicationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @PostMapping
    public void add(@RequestBody ApplicationRequest dto, Authentication auth) throws Exception {
        applicationService.addApplication(dto, auth);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) throws Exception {
        applicationService.deleteApplication(id, auth);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id,
                       @RequestBody ApplicationRequest dto,
                       Authentication auth) throws Exception {
        applicationService.updateApplication(id, dto, auth);
    }
}