package com.learnforlearning.web;

import com.learnforlearning.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin-only moderation area. Access is restricted to {@code ROLE_ADMIN} in
 * {@link com.learnforlearning.config.SecurityConfig}. Every action is a POST.
 */
@Controller
@RequestMapping("/manage")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String manage(Model model) {
        model.addAttribute("disputes", adminService.disputes());
        model.addAttribute("pendingTeachers", adminService.pendingTeachers());
        model.addAttribute("pendingSubjects", adminService.pendingSubjects());
        return "manage";
    }

    @PostMapping("/changeActivity")
    public String changeActivity(@RequestParam Long teacherId,
                                 @RequestParam Long subjectId,
                                 @RequestParam(defaultValue = "false") boolean active,
                                 RedirectAttributes redirect) {
        boolean ok = adminService.setTeacherActivity(teacherId, subjectId, active);
        flash(redirect, ok, "A változtatás mentésre került!", "A változtatás sikertelen volt.");
        return "redirect:/manage";
    }

    @PostMapping("/addTeacher")
    public String acceptTeacher(@RequestParam Long teacherId, RedirectAttributes redirect) {
        boolean ok = adminService.acceptTeacher(teacherId);
        flash(redirect, ok, "Az oktató elfogadásra került!", "Az oktató nem található.");
        return "redirect:/manage";
    }

    @PostMapping("/addSubject")
    public String acceptSubject(@RequestParam Long subjectId, RedirectAttributes redirect) {
        boolean ok = adminService.acceptSubject(subjectId);
        flash(redirect, ok, "A tárgy elfogadásra került!", "A tárgy nem található.");
        return "redirect:/manage";
    }

    @PostMapping("/resetAgainstActivity")
    public String resetDispute(@RequestParam Long teacherId,
                               @RequestParam Long subjectId,
                               RedirectAttributes redirect) {
        boolean ok = adminService.resetDispute(teacherId, subjectId);
        flash(redirect, ok, "Az észrevétel elutasításra került!", "A művelet sikertelen volt.");
        return "redirect:/manage";
    }

    @PostMapping("/deleteTeacher")
    public String deleteTeacher(@RequestParam Long teacherId, RedirectAttributes redirect) {
        boolean ok = adminService.deleteTeacher(teacherId);
        flash(redirect, ok, "Az oktató törlésre került!", "Az oktató nem található.");
        return "redirect:/manage";
    }

    @PostMapping("/deleteSubject")
    public String deleteSubject(@RequestParam Long subjectId, RedirectAttributes redirect) {
        boolean ok = adminService.deleteSubject(subjectId);
        flash(redirect, ok, "A tárgy törlésre került!", "A tárgy nem található.");
        return "redirect:/manage";
    }

    private static void flash(RedirectAttributes redirect, boolean ok, String success, String error) {
        redirect.addFlashAttribute(ok ? "success" : "error", ok ? success : error);
    }
}
