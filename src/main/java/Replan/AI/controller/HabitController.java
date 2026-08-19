package Replan.AI.controller;

import Replan.AI.model.Habit;
import Replan.AI.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Habit createHabit(@Valid @RequestBody Habit habit) {
        return habitService.createHabit(habit);
    }

    @GetMapping("/{habitId}")
    public Habit getHabit(@PathVariable String habitId) {
        return habitService.getHabitById(habitId);
    }

    @GetMapping("/user/{userId}")
    public List<Habit> getUserHabits(@PathVariable String userId) {
        return habitService.getHabitsByUser(userId);
    }

    @PutMapping("/{habitId}")
    public Habit updateHabit(
            @PathVariable String habitId,
            @Valid @RequestBody Habit habit) {

        return habitService.updateHabit(habitId, habit);
    }

    @PatchMapping("/{habitId}/pause")
    public Habit pauseHabit(@PathVariable String habitId) {
        return habitService.pauseHabit(habitId);
    }
}