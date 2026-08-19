package Replan.AI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmHabitRequest(

        @NotBlank(message = "User ID is required")
        String userId,

        @NotNull(message = "Habit proposal is required")
        HabitProposal habit

) {
}