package com.learnforlearning.web;

import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.User;
import com.learnforlearning.recommendation.RecommendationResult;
import com.learnforlearning.recommendation.RecommendationService;
import com.learnforlearning.recommendation.Semester;
import com.learnforlearning.repository.CalculationRepository;
import com.learnforlearning.security.UserPrincipal;
import com.learnforlearning.service.SubjectService;
import com.learnforlearning.service.UserService;
import com.learnforlearning.web.view.CalculationResponse;
import com.learnforlearning.web.view.CalculationView;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final SubjectService subjectService;
    private final UserService userService;
    private final CalculationRepository calculationRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    SubjectService subjectService,
                                    UserService userService,
                                    CalculationRepository calculationRepository) {
        this.recommendationService = recommendationService;
        this.subjectService = subjectService;
        this.userService = userService;
        this.calculationRepository = calculationRepository;
    }

    @GetMapping("/findsubject")
    public String find(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = userService.getById(principal.getId());

        List<CalculationView> history = calculationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(c -> subjectService.findByCode(c.getSubjectCode())
                        .map(s -> new CalculationView(s.getId(), s.getName(), s.getCode()))
                        .orElse(new CalculationView(null, c.getSubjectCode(), c.getSubjectCode())))
                .toList();

        model.addAttribute("canCalculate", user.hasSpecialization());
        model.addAttribute("optionalSubjects", subjectService.availableOptionalSubjectsFor(user));
        model.addAttribute("history", history);
        return "find";
    }

    @PostMapping("/findsubject/calculate")
    @ResponseBody
    public CalculationResponse calculate(@RequestParam("semester") String semester,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        Semester parsed = Semester.fromFormValue(semester);
        RecommendationResult result = recommendationService.recommend(principal.getId(), parsed);
        if (!result.successful()) {
            return CalculationResponse.failed();
        }
        Subject subject = result.subject();
        return CalculationResponse.of(subject);
    }

    @PostMapping("/findsubject/delete")
    public String deleteHistory(@AuthenticationPrincipal UserPrincipal principal, RedirectAttributes redirect) {
        userService.deleteCalculations(principal.getId());
        redirect.addFlashAttribute("calculationsDeleted", true);
        return "redirect:/findsubject";
    }
}
