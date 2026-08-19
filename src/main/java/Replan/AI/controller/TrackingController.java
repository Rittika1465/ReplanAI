package Replan.AI.controller;

import Replan.AI.dto.CompleteHabitRequest;
import Replan.AI.dto.MissHabitRequest;
import Replan.AI.model.HabitLog;
import Replan.AI.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/{habitId}/complete")
    public HabitLog markComplete(
            @PathVariable String habitId,
            @Valid @RequestBody CompleteHabitRequest request) {

        return trackingService.markComplete(habitId, request);
    }

    @PostMapping("/{habitId}/miss")
    public HabitLog markMissed(
            @PathVariable String habitId,
            @Valid @RequestBody MissHabitRequest request) {

        return trackingService.markMissed(habitId, request);
    }

    @GetMapping("/logs/user/{userId}")
    public List<HabitLog> getUserLogs(
            @PathVariable String userId) {

        return trackingService.getUserLogs(userId);
    }
}