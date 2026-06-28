package back.pickd.global.admin.controller;

import back.pickd.global.admin.dto.EnumColumnResult;
import back.pickd.global.admin.service.ValidationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ValidationQueryService validationQueryService;

    @GetMapping
    public String dashboard() {
        return "redirect:/admin/validation";
    }

    @GetMapping("/validation")
    public String validationDashboard(Model model) {
        List<EnumColumnResult> results = validationQueryService.validateAll();

        long passCount = results.stream().filter(EnumColumnResult::isPassed).count();
        long failCount = results.size() - passCount;

        model.addAttribute("results", results);
        model.addAttribute("passCount", passCount);
        model.addAttribute("failCount", failCount);
        model.addAttribute("total", results.size());
        model.addAttribute("checkedAt", LocalDateTime.now());

        return "admin/dashboard";
    }
}
