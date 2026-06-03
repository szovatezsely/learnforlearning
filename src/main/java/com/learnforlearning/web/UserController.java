package com.learnforlearning.web;

import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherVote;
import com.learnforlearning.domain.User;
import com.learnforlearning.security.UserPrincipal;
import com.learnforlearning.service.TeacherService;
import com.learnforlearning.service.UserService;
import com.learnforlearning.web.dto.CommentForm;
import com.learnforlearning.web.dto.SpecializationForm;
import com.learnforlearning.web.view.PersonalVoteView;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final TeacherService teacherService;

    public UserController(UserService userService, TeacherService teacherService) {
        this.userService = userService;
        this.teacherService = teacherService;
    }

    @GetMapping("/personal")
    public String personal(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = userService.getById(principal.getId());
        List<PersonalVoteView> votes = userService.votesOf(user.getId()).stream()
                .map(v -> new PersonalVoteView(v.getTeacher().getId(), v.getTeacher().getName(),
                        v.getComment(), v.getPositiveVote()))
                .toList();
        model.addAttribute("user", user);
        model.addAttribute("votes", votes);
        return "personal";
    }

    @GetMapping("/personal/{id}/edit")
    public String editSpecialization(@PathVariable Long id,
                                     @AuthenticationPrincipal UserPrincipal principal,
                                     Model model,
                                     RedirectAttributes redirect) {
        if (!id.equals(principal.getId())) {
            redirect.addFlashAttribute("error", "Csak a saját adataidat szerkesztheted.");
            return "redirect:/personal";
        }
        User user = userService.getById(id);
        SpecializationForm form = new SpecializationForm();
        form.setSpecialization(user.getSpecialization());
        model.addAttribute("user", user);
        model.addAttribute("specializationForm", form);
        return "edit_specialization";
    }

    @PostMapping("/personal/{id}/update")
    public String updateSpecialization(@PathVariable Long id,
                                       @Valid @ModelAttribute("specializationForm") SpecializationForm form,
                                       BindingResult binding,
                                       @AuthenticationPrincipal UserPrincipal principal,
                                       Model model,
                                       RedirectAttributes redirect) {
        if (!id.equals(principal.getId())) {
            redirect.addFlashAttribute("error", "Csak a saját adataidat szerkesztheted.");
            return "redirect:/personal";
        }
        if (binding.hasErrors()) {
            model.addAttribute("user", userService.getById(id));
            return "edit_specialization";
        }
        userService.updateSpecialization(id, form.getSpecialization());
        redirect.addFlashAttribute("success", "A specializáció frissítve!");
        return "redirect:/personal";
    }

    // --- Votes ------------------------------------------------------------------

    @PostMapping("/subject/vote")
    public String vote(@RequestParam Long teacherId,
                       @RequestParam Long subjectId,
                       @RequestParam boolean positive,
                       @AuthenticationPrincipal UserPrincipal principal,
                       RedirectAttributes redirect) {
        userService.vote(principal.getId(), teacherId, positive);
        redirect.addFlashAttribute("success", "A szavazatod rögzítésre került!");
        return "redirect:/subjects/" + subjectId;
    }

    // --- Comments ---------------------------------------------------------------

    @GetMapping("/subject/comment")
    public String commentForm(@RequestParam Long teacherId,
                              @RequestParam Long subjectId,
                              @AuthenticationPrincipal UserPrincipal principal,
                              Model model) {
        Teacher teacher = teacherService.findById(teacherId).orElseThrow();
        TeacherVote existing = userService.findVote(principal.getId(), teacherId).orElse(null);
        CommentForm form = new CommentForm();
        form.setTeacherId(teacherId);
        form.setSubjectId(subjectId);
        form.setComment(existing != null ? existing.getComment() : null);
        model.addAttribute("commentForm", form);
        model.addAttribute("teacher", teacher);
        return "make_comment";
    }

    @PostMapping("/subject/comment/update")
    public String saveComment(@Valid @ModelAttribute("commentForm") CommentForm form,
                              BindingResult binding,
                              @AuthenticationPrincipal UserPrincipal principal,
                              Model model,
                              RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("teacher", teacherService.findById(form.getTeacherId()).orElseThrow());
            return "make_comment";
        }
        userService.addComment(principal.getId(), form.getTeacherId(), form.getComment());
        redirect.addFlashAttribute("success", "A megjegyzésed mentésre került!");
        return "redirect:/subjects/" + form.getSubjectId();
    }

    @PostMapping("/subject/comment/delete")
    public String deleteSubjectComment(@RequestParam Long teacherId,
                                       @RequestParam Long subjectId,
                                       @AuthenticationPrincipal UserPrincipal principal,
                                       RedirectAttributes redirect) {
        userService.deleteComment(principal.getId(), teacherId);
        redirect.addFlashAttribute("success", "A megjegyzésed törlésre került!");
        return "redirect:/subjects/" + subjectId;
    }

    @PostMapping("/personal/comment/delete")
    public String deletePersonalComment(@RequestParam Long teacherId,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteComment(principal.getId(), teacherId);
        return "redirect:/personal";
    }
}
