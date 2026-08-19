package Replan.AI.service;

import Replan.AI.dto.RecoveryRequest;
import Replan.AI.model.*;
import Replan.AI.repository.RecoveryPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RecoveryService {

    private final RecoveryPlanRepository recoveryPlanRepository;
    private final HabitService habitService;
    private final ProfileService profileService;

    public RecoveryService(
            RecoveryPlanRepository recoveryPlanRepository,
            HabitService habitService,
            ProfileService profileService
    ) {
        this.recoveryPlanRepository = recoveryPlanRepository;
        this.habitService = habitService;
        this.profileService = profileService;
    }

    public RecoveryPlan suggestRecovery(
            String habitId,
            RecoveryRequest request
    ) {
        Habit habit = habitService.getHabitById(habitId);

        UserProfile profile =
                profileService.getProfileById(habit.getUserId());

        LocalDate missedDate = request.getMissedDate() != null
                ? request.getMissedDate()
                : LocalDate.now();

        RecoveryType recoveryType =
                chooseRecoveryType(request.getReason());

        LocalDateTime proposedDateTime =
                findAvailableRecoveryDateTime(
                        habit,
                        profile,
                        missedDate
                );

        int originalTarget = habit.getTargetValue() > 0
                ? habit.getTargetValue()
                : habit.getDurationMinutes();

        Integer temporaryTarget = null;

        if (recoveryType == RecoveryType.REDUCE_TARGET
                || recoveryType == RecoveryType.SPLIT_SESSION) {

            temporaryTarget = Math.max(
                    1,
                    originalTarget / 2
            );
        }

        RecoveryPlan plan = new RecoveryPlan();

        plan.setUserId(habit.getUserId());
        plan.setHabitId(habit.getId());
        plan.setReason(request.getReason());
        plan.setRecoveryType(recoveryType);

        plan.setOriginalDateTime(
                LocalDateTime.of(
                        missedDate,
                        habit.getPreferredTime()
                )
        );

        plan.setProposedDateTime(
                proposedDateTime
        );

        plan.setOriginalTarget(originalTarget);
        plan.setTemporaryTarget(temporaryTarget);
        plan.setStatus(RecoveryStatus.PENDING);
        plan.setCreatedAt(LocalDateTime.now());

        return recoveryPlanRepository.save(plan);
    }

    public RecoveryPlan confirmRecovery(String recoveryId) {
        RecoveryPlan plan = getRecoveryPlan(recoveryId);

        plan.setStatus(RecoveryStatus.CONFIRMED);
        plan.setConfirmedAt(LocalDateTime.now());

        return recoveryPlanRepository.save(plan);
    }

    public RecoveryPlan rejectRecovery(String recoveryId) {
        RecoveryPlan plan = getRecoveryPlan(recoveryId);

        plan.setStatus(RecoveryStatus.REJECTED);
        plan.setConfirmedAt(null);

        return recoveryPlanRepository.save(plan);
    }

    public List<RecoveryPlan> getUserRecoveryPlans(
            String userId
    ) {
        return recoveryPlanRepository.findByUserId(userId);
    }

    private RecoveryPlan getRecoveryPlan(String recoveryId) {
        return recoveryPlanRepository
                .findById(recoveryId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Recovery plan not found"
                        )
                );
    }

    private RecoveryType chooseRecoveryType(String reason) {
        String text = reason.toLowerCase();

        if (text.contains("tired")
                || text.contains("sick")
                || text.contains("low energy")) {

            return RecoveryType.REDUCE_TARGET;
        }

        if (text.contains("too long")
                || text.contains("difficult")
                || text.contains("overwhelming")) {

            return RecoveryType.SPLIT_SESSION;
        }

        return RecoveryType.MOVE_SESSION;
    }

    private LocalDateTime findAvailableRecoveryDateTime(
            Habit missedHabit,
            UserProfile profile,
            LocalDate missedDate
    ) {
        List<Habit> userHabits =
                habitService.getHabitsByUser(
                        missedHabit.getUserId()
                );

        List<RecoveryPlan> recoveryPlans =
                recoveryPlanRepository.findByUserId(
                        missedHabit.getUserId()
                );

        LocalTime startTime =
                profile.getPreferredStartTime() != null
                        ? profile.getPreferredStartTime()
                        : LocalTime.of(9, 0);

        LocalTime endTime =
                profile.getPreferredEndTime() != null
                        ? profile.getPreferredEndTime()
                        : LocalTime.of(21, 0);

        int duration =
                Math.max(
                        1,
                        missedHabit.getDurationMinutes()
                );

        LocalDate candidateDate =
                missedDate.plusDays(1);

        // আগামী 14 দিনের মধ্যে free slot খুঁজবে
        for (int dayCount = 0;
             dayCount < 14;
             dayCount++) {

            if (isAvailableDay(
                    candidateDate,
                    profile.getAvailableDays()
            )) {
                LocalTime candidateTime =
                        startTime;

                while (
                        !candidateTime
                                .plusMinutes(duration)
                                .isAfter(endTime)
                ) {
                    LocalDateTime candidate =
                            LocalDateTime.of(
                                    candidateDate,
                                    candidateTime
                            );

                    if (isSlotFree(
                            candidate,
                            duration,
                            userHabits,
                            recoveryPlans
                    )) {
                        return candidate;
                    }

                    // Conflict হলে 15 মিনিট পরে চেষ্টা করবে
                    candidateTime =
                            candidateTime.plusMinutes(15);
                }
            }

            candidateDate =
                    candidateDate.plusDays(1);
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No free recovery slot was found in the next 14 days"
        );
    }


    private boolean isAvailableDay(
            LocalDate date,
            List<DayOfWeek> availableDays
    ) {
        return availableDays == null
                || availableDays.isEmpty()
                || availableDays.contains(
                date.getDayOfWeek()
        );
    }


    private boolean isSlotFree(
            LocalDateTime candidate,
            int duration,
            List<Habit> habits,
            List<RecoveryPlan> recoveryPlans
    ) {
        LocalDateTime candidateEnd =
                candidate.plusMinutes(duration);

        for (Habit habit : habits) {
            if (habit.getStatus() != HabitStatus.ACTIVE
                    || habit.getDaysOfWeek() == null
                    || !habit.getDaysOfWeek().contains(
                    candidate.getDayOfWeek()
            )
                    || habit.getPreferredTime() == null
                    || (
                    habit.getStartDate() != null
                            && habit.getStartDate()
                            .isAfter(candidate.toLocalDate())
            )) {

                continue;
            }

            LocalDateTime habitStart =
                    LocalDateTime.of(
                            candidate.toLocalDate(),
                            habit.getPreferredTime()
                    );

            LocalDateTime habitEnd =
                    habitStart.plusMinutes(
                            habit.getDurationMinutes()
                    );

            if (timesOverlap(
                    candidate,
                    candidateEnd,
                    habitStart,
                    habitEnd
            )) {
                return false;
            }
        }

        for (RecoveryPlan plan : recoveryPlans) {
            if (
                    plan.getStatus() == RecoveryStatus.REJECTED
                            || plan.getProposedDateTime() == null
            ) {
                continue;
            }

            LocalDateTime recoveryStart =
                    plan.getProposedDateTime();

            LocalDateTime recoveryEnd =
                    recoveryStart.plusMinutes(duration);

            if (timesOverlap(
                    candidate,
                    candidateEnd,
                    recoveryStart,
                    recoveryEnd
            )) {
                return false;
            }
        }

        return true;
    }


    private boolean timesOverlap(
            LocalDateTime firstStart,
            LocalDateTime firstEnd,
            LocalDateTime secondStart,
            LocalDateTime secondEnd
    ) {
        return firstStart.isBefore(secondEnd)
                && secondStart.isBefore(firstEnd);
    }
}