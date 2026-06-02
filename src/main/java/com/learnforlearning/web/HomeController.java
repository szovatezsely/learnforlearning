package com.learnforlearning.web;

import com.learnforlearning.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final StatsService statsService;

    public HomeController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("stats", statsService.forHomePage());
        return "main";
    }

    @GetMapping("/offline")
    public String offline() {
        return "offline";
    }
}
