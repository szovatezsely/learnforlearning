package com.learnforlearning.web;

import com.learnforlearning.service.UserService;
import com.learnforlearning.web.dto.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult binding) {
        if (!form.passwordsMatch()) {
            binding.rejectValue("passwordConfirmation", "passwords.mismatch",
                    "A két jelszó nem egyezik!");
        }
        if (!binding.hasErrors()) {
            try {
                userService.register(form.getName(), form.getEmail(), form.getPassword());
            } catch (IllegalArgumentException ex) {
                binding.rejectValue("email", "email.taken", ex.getMessage());
            }
        }
        if (binding.hasErrors()) {
            return "auth/register";
        }
        return "redirect:/login?registered";
    }
}
