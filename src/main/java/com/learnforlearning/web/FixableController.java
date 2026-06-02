package com.learnforlearning.web;

import com.learnforlearning.domain.Subject;
import com.learnforlearning.service.SubjectService;
import com.learnforlearning.service.TeacherService;
import com.learnforlearning.web.dto.SubjectSuggestionForm;
import com.learnforlearning.web.dto.TeacherSuggestionForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * The "javítási észrevételek" (correction suggestions) area where any logged-in user
 * can dispute a teacher's activity or suggest a new teacher/subject for admin review.
 */
@Controller
public class FixableController {

    private final SubjectService subjectService;
    private final TeacherService teacherService;

    public FixableController(SubjectService subjectService, TeacherService teacherService) {
        this.subjectService = subjectService;
        this.teacherService = teacherService;
    }

    @GetMapping("/fixable")
    public String fixable(Model model) {
        model.addAttribute("subjects", subjectService.listAccepted());
        if (!model.containsAttribute("teacherForm")) {
            model.addAttribute("teacherForm", new TeacherSuggestionForm());
        }
        if (!model.containsAttribute("subjectForm")) {
            model.addAttribute("subjectForm", new SubjectSuggestionForm());
        }
        return "fixable";
    }

    /** Active teachers of a subject — used to populate the dispute dropdown via AJAX. */
    @GetMapping("/fixable/teachers")
    @ResponseBody
    public List<TeacherOption> teachersForSubject(@RequestParam Long subjectId) {
        Subject subject = subjectService.findById(subjectId).orElse(null);
        if (subject == null) {
            return List.of();
        }
        return teacherService.activeAssignmentsFor(subject).stream()
                .map(a -> new TeacherOption(a.getTeacher().getId(), a.getTeacher().getName()))
                .toList();
    }

    @PostMapping("/fixable/activity")
    public String dispute(@RequestParam Long teacherId,
                          @RequestParam Long subjectId,
                          @RequestParam(defaultValue = "false") boolean active,
                          RedirectAttributes redirect) {
        boolean ok = teacherService.dispute(teacherId, subjectId, active);
        redirect.addFlashAttribute(ok ? "success" : "error",
                ok ? "Köszönjük az észrevételt!" : "Az észrevétel rögzítése sikertelen volt.");
        return "redirect:/fixable";
    }

    @PostMapping("/fixable/newTeacher")
    public String suggestTeacher(@Valid @ModelAttribute("teacherForm") TeacherSuggestionForm form,
                                 BindingResult binding,
                                 RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.teacherForm", binding);
            redirect.addFlashAttribute("teacherForm", form);
            return "redirect:/fixable";
        }
        teacherService.suggestTeacher(form.getName(), form.getSubjectId());
        redirect.addFlashAttribute("success", "Az oktató javaslatát rögzítettük!");
        return "redirect:/fixable";
    }

    @PostMapping("/fixable/newSubject")
    public String suggestSubject(@Valid @ModelAttribute("subjectForm") SubjectSuggestionForm form,
                                 BindingResult binding,
                                 RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.subjectForm", binding);
            redirect.addFlashAttribute("subjectForm", form);
            return "redirect:/fixable";
        }
        subjectService.suggestSubject(form);
        redirect.addFlashAttribute("success", "A tárgy javaslatát rögzítettük!");
        return "redirect:/fixable";
    }

    /** Lightweight JSON option for the teacher dropdown. */
    public record TeacherOption(Long id, String name) {
    }
}
