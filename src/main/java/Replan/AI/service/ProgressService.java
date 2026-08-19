package Replan.AI.service;

import Replan.AI.dto.ProgressResponse;
import Replan.AI.model.HabitLog;
import Replan.AI.model.HabitLogStatus;
import Replan.AI.repository.HabitLogRepository;
import Replan.AI.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

@Service
public class ProgressService {

    private final HabitLogRepository habitLogRepository;
    private final UserProfileRepository profileRepository;

    public ProgressService(
            HabitLogRepository habitLogRepository,
            UserProfileRepository profileRepository) {

        this.habitLogRepository = habitLogRepository;
        this.profileRepository = profileRepository;
    }

    public ProgressResponse getWeeklyProgress(String userId) {
        validateUser(userId);

        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDate weekEnd = today.with(
                TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
        );

        List<HabitLog> weeklyLogs =
                habitLogRepository
                        .findByUserIdAndScheduledDateBetweenOrderByScheduledDateAsc(
                                userId,
                                weekStart,
                                weekEnd
                        );

        int completedSessions = (int) weeklyLogs.stream()
                .filter(log ->
                        log.getStatus() == HabitLogStatus.COMPLETED)
                .count();

        int missedSessions = (int) weeklyLogs.stream()
                .filter(log ->
                        log.getStatus() == HabitLogStatus.MISSED)
                .count();

        int totalTrackedSessions =
                completedSessions + missedSessions;

        double weeklyCompletionRate =
                totalTrackedSessions == 0
                        ? 0.0
                        : (completedSessions * 100.0)
                        / totalTrackedSessions;

        int currentStreak = calculateCurrentStreak(userId);

        return new ProgressResponse(
                userId,
                weekStart,
                weekEnd,
                completedSessions,
                missedSessions,
                totalTrackedSessions,
                weeklyCompletionRate,
                currentStreak
        );
    }

    private int calculateCurrentStreak(String userId) {
        List<HabitLog> allLogs =
                habitLogRepository.findByUserId(userId);

        allLogs.sort(
                Comparator.comparing(HabitLog::getScheduledDate)
                        .reversed()
        );

        int streak = 0;

        for (HabitLog log : allLogs) {
            if (log.getStatus() == HabitLogStatus.COMPLETED) {
                streak++;
            } else if (log.getStatus() == HabitLogStatus.MISSED) {
                break;
            }
        }

        return streak;
    }

    private void validateUser(String userId) {
        if (!profileRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User profile not found"
            );
        }
    }
}