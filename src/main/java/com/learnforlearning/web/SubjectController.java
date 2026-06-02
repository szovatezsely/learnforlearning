package com.learnforlearning.web;

import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherVote;
import com.learnforlearning.domain.User;
import com.learnforlearning.security.UserPrincipal;
import com.learnforlearning.service.SubjectService;
import com.learnforlearning.service.TeacherService;
import com.learnforlearning.service.UserService;
import com.learnforlearning.web.dto.GradeForm;
import com.learnforlearning.web.view.TeacherRating;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class SubjectController {

    private final SubjectService subjectService;
    private final TeacherService teacherService;
    private final UserService userService;

    public SubjectController(SubjectService subjectService, TeacherService teacherService, UserService userService) {
        this.subjectService = subjectService;
        this.teacherService = teacherService;
        this.userService = userService;
    }

    @GetMapping("/subjects")
    public String list(@RequestParam(value = "q", required = false) String query, Model model) {
        List<Subject> subjects = (query == null || query.isBlank())
                ? subjectService.listAccepted()
                : subjectService.search(query).stream().filter(Subject::isAccepted).toList();
        model.addAttribute("subjects", subjects);
        model.addAttribute("query", query == null ? "" : query);
        return "subjects";
    }

    @GetMapping("/subjects/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(value = "page", required = false) String page,
                         @AuthenticationPrincipal UserPrincipal principal,
                         Model model,
                         RedirectAttributes redirect) {
        Subject subject = subjectService.findAccepted(id).orElse(null);
        if (subject == null) {
            redirect.addFlashAttribute("error", "A keresett tárgy nem található.");
            return "redirect:/subjects";
        }

        // Map of this user's votes by teacher id, fetched once (no per-teacher queries).
        Map<Long, TeacherVote> myVotes = principal == null
                ? Map.of()
                : userService.votesOf(principal.getId()).stream()
                        .collect(Collectors.toMap(v -> v.getTeacher().getId(), Function.identity(), (a, b) -> a));

        List<TeacherRating> ratings = new ArrayList<>();
        for (var assignment : teacherService.activeAssignmentsFor(subject)) {
            Teacher teacher = assignment.getTeacher();
            TeacherVote mine = myVotes.get(teacher.getId());
            boolean up = mine != null && Boolean.TRUE.equals(mine.getPositiveVote());
            boolean down = mine != null && Boolean.FALSE.equals(mine.getPositiveVote());
            ratings.add(new TeacherRating(teacher, teacher.score(), up, down));
        }

        model.addAttribute("subject", subject);
        model.addAttribute("ratings", ratings);
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("page", page == null ? "subjects" : page);
        return "subject";
    }

    @GetMapping("/newsubject")
    public String gradeBook(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = userService.getById(principal.getId());
        model.addAttribute("gradableSubjects", subjectService.gradableSubjectsFor(user));
        model.addAttribute("grades", subjectService.gradesOf(user));
        if (!model.containsAttribute("gradeForm")) {
            model.addAttribute("gradeForm", new GradeForm());
        }
        return "given_subjects";
    }

    @PostMapping("/subjects/add")
    public String addGrade(@Valid @ModelAttribute("gradeForm") GradeForm form,
                           BindingResult binding,
                           @AuthenticationPrincipal UserPrincipal principal,
                           RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.gradeForm", binding);
            redirect.addFlashAttribute("gradeForm", form);
            return "redirect:/newsubject";
        }
        boolean added = userService.addGrade(principal.getId(), form.getSubjectId(), form.getGrade());
        redirect.addFlashAttribute(added ? "success" : "error",
                added ? "A jegy felvételre került!" : "Ehhez a tárgyhoz már tartozik jegyed!");
        return "redirect:/newsubject";
    }

    @GetMapping("/newsubject/{id}/edit")
    public String editGrade(@PathVariable Long id,
                            @AuthenticationPrincipal UserPrincipal principal,
                            Model model,
                            RedirectAttributes redirect) {
        Subject subject = subjectService.findById(id).orElse(null);
        if (subject == null) {
            redirect.addFlashAttribute("error", "A keresett tárgy nem létezik.");
            return "redirect:/newsubject";
        }
        Integer grade = userService.findGrade(principal.getId(), id).map(g -> g.getValue()).orElse(null);
        model.addAttribute("subject", subject);
        model.addAttribute("grade", grade);
        return "edit_given_subject";
    }

    @PostMapping("/newsubject/{id}/update")
    public String updateGrade(@PathVariable Long id,
                              @RequestParam("grade") Integer grade,
                              @AuthenticationPrincipal UserPrincipal principal,
                              RedirectAttributes redirect) {
        if (grade == null || grade < 1 || grade > 5) {
            redirect.addFlashAttribute("error", "A jegynek értelmesnek kell lennie! (1-5)");
            return "redirect:/newsubject/" + id + "/edit";
        }
        userService.updateGrade(principal.getId(), id, grade);
        redirect.addFlashAttribute("success", "A jegy módosításra került!");
        return "redirect:/newsubject";
    }

    @PostMapping("/newsubject/{id}/delete")
    public String deleteGrade(@PathVariable Long id,
                              @AuthenticationPrincipal UserPrincipal principal,
                              RedirectAttributes redirect) {
        boolean deleted = userService.deleteGrade(principal.getId(), id);
        redirect.addFlashAttribute(deleted ? "success" : "error",
                deleted ? "A jegy törlésre került!" : "A jegy törlése sikertelen volt.");
        return "redirect:/newsubject";
    }
}
