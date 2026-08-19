package Replan.AI.controller;

import Replan.AI.dto.ChatRequest;
import Replan.AI.dto.ChatResponse;
import Replan.AI.dto.ConfirmHabitRequest;
import Replan.AI.dto.HabitProposal;
import Replan.AI.model.Habit;
import Replan.AI.model.UserProfile;
import Replan.AI.service.AIChatService;
import Replan.AI.service.HabitService;
import Replan.AI.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AIChatService aiChatService;
    private final ProfileService profileService;
    private final HabitService habitService;

    public ChatController(
            AIChatService aiChatService,
            ProfileService profileService,
            HabitService habitService
    ) {
        this.aiChatService = aiChatService;
        this.profileService = profileService;
        this.habitService = habitService;
    }

    /*
     * User-এর message Groq-এ পাঠিয়ে
     * structured AI response ফেরত দেয়।
     */
    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request
    ) {
        UserProfile profile =
                profileService.getProfileById(
                        request.userId()
                );

        return aiChatService.analyseMessage(
                profile,
                request.message(),
                request.history()
        );
    }

    /*
     * User Confirm চাপার পর AI proposal-কে
     * Habit object-এ বদলে MongoDB-তে save করে।
     */
    @PostMapping("/confirm")
    public Habit confirmHabit(
            @Valid @RequestBody ConfirmHabitRequest request
    ) {
        HabitProposal proposal = request.habit();

        Habit habit = new Habit();

        habit.setUserId(request.userId());
        habit.setName(proposal.name());
        habit.setDescription(proposal.description());
        habit.setFrequencyType(proposal.frequencyType());
        habit.setDaysOfWeek(proposal.daysOfWeek());
        habit.setTargetValue(proposal.targetValue());
        habit.setUnit(proposal.unit());
        habit.setDurationMinutes(
                proposal.durationMinutes()
        );
        habit.setPreferredTime(
                proposal.preferredTime()
        );
        habit.setStartDate(proposal.startDate());

        return habitService.createHabit(habit);
    }
}