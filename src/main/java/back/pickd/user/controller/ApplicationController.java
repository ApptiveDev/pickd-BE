package back.pickd.user.controller;

import back.pickd.user.dto.ApplicationRequest;
import back.pickd.user.entity.Application;
import back.pickd.user.service.ApplicationService;
import back.pickd.user.repository.ApplicationRepository;

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