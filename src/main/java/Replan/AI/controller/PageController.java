package Replan.AI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/today")
    public String todayPage() {
        return "today";
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/habits")
    public String habitsPage() {
        return "habits";
    }

    @GetMapping("/progress")
    public String progressPage() {
        return "progress";
    }

    @GetMapping("/calendar")
    public String calendarPage() {
        return "calendar";
    }
}