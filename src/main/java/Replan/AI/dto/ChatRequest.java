package Replan.AI.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ChatRequest(

        @NotBlank(message = "User ID is required")
        String userId,

        @NotBlank(message = "Message is required")
        String message,

        List<ChatHistoryMessage> history

) {
}