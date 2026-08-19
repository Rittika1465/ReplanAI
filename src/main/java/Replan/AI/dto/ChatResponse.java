package Replan.AI.dto;

import java.util.List;

public record ChatResponse(

        ChatIntent intent,
        String reply,
        boolean requiresConfirmation,
        List<String> missingFields,
        HabitProposal habit

) {
}