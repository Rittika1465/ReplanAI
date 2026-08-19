package Replan.AI.dto;

import java.time.LocalDate;

// Progress API যে calculated result return করবে
public record ProgressResponse(
        String userId,
        LocalDate weekStart,
        LocalDate weekEnd,
        int completedSessions,
        int missedSessions,
        int totalTrackedSessions,
        double weeklyCompletionRate,
        int currentStreak
) {
}