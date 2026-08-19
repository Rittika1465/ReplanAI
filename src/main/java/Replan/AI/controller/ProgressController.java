package Replan.AI.controller;

import Replan.AI.dto.ProgressResponse;
import Replan.AI.service.ProgressService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/{userId}")
    public ProgressResponse getProgress(
            @PathVariable String userId) {

        return progressService.getWeeklyProgress(userId);
    }

    @GetMapping("/progress")
    public String progressPage() {
        return "progress";
    }
}

