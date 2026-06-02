package com.learnforlearning.web;

import com.learnforlearning.domain.Teacher;
import com.learnforlearning.service.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    /** All comments written about a teacher. {@code id} is the teacher id. */
    @GetMapping("/subject/{id}/comments")
    public String comments(@PathVariable Long id,
                           @RequestParam(value = "subjectId", required = false) Long subjectId,
                           Model model,
                           RedirectAttributes redirect) {
        Teacher teacher = teacherService.findById(id).orElse(null);
        if (teacher == null) {
            redirect.addFlashAttribute("error", "A keresett oktató nem található.");
            return subjectId == null ? "redirect:/subjects" : "redirect:/subjects/" + subjectId;
        }
        model.addAttribute("teacher", teacher);
        model.addAttribute("comments", teacherService.commentsOf(teacher));
        model.addAttribute("subjectId", subjectId);
        return "comments";
    }
}
