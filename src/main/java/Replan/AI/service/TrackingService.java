package Replan.AI.service;

import Replan.AI.dto.CompleteHabitRequest;
import Replan.AI.dto.MissHabitRequest;
import Replan.AI.model.Habit;
import Replan.AI.model.HabitLog;
import Replan.AI.model.HabitLogStatus;
import Replan.AI.repository.HabitLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrackingService {

    private final HabitLogRepository habitLogRepository;
    private final HabitService habitService;

    public TrackingService(
            HabitLogRepository habitLogRepository,
            HabitService habitService) {

        this.habitLogRepository = habitLogRepository;
        this.habitService = habitService;
    }

    public HabitLog markComplete(
            String habitId,
            CompleteHabitRequest request) {

        Habit habit = habitService.getHabitById(habitId);
        LocalDate date = getDateOrToday(request.getScheduledDate());

        HabitLog log = getOrCreateLog(habit, date);

        log.setStatus(HabitLogStatus.COMPLETED);

        int completedValue = request.getCompletedValue() > 0
                ? request.getCompletedValue()
                : habit.getTargetValue();

        log.setCompletedValue(completedValue);
        log.setMissReason(null);
        log.setNote(request.getNote());
        log.setCompletedAt(LocalDateTime.now());

        return saveLog(log);
    }

    public HabitLog markMissed(
            String habitId,
            MissHabitRequest request) {

        Habit habit = habitService.getHabitById(habitId);
        LocalDate date = getDateOrToday(request.getScheduledDate());

        HabitLog log = getOrCreateLog(habit, date);

        log.setStatus(HabitLogStatus.MISSED);
        log.setCompletedValue(0);
        log.setMissReason(request.getMissReason());
        log.setNote(request.getNote());
        log.setCompletedAt(null);

        return saveLog(log);
    }

    public List<HabitLog> getUserLogs(String userId) {
        return habitLogRepository.findByUserId(userId);
    }

    private HabitLog getOrCreateLog(Habit habit, LocalDate date) {
        return habitLogRepository
                .findByHabitIdAndScheduledDate(habit.getId(), date)
                .orElseGet(() -> {
                    HabitLog newLog = new HabitLog();
                    newLog.setHabitId(habit.getId());
                    newLog.setUserId(habit.getUserId());
                    newLog.setScheduledDate(date);
                    newLog.setScheduledTime(habit.getPreferredTime());
                    return newLog;
                });
    }

    private HabitLog saveLog(HabitLog log) {
        if (log.getId() == null) {
            log.prepareForCreate();
        } else {
            log.prepareForUpdate();
        }

        return habitLogRepository.save(log);
    }

    private LocalDate getDateOrToday(LocalDate scheduledDate) {
        return scheduledDate != null
                ? scheduledDate
                : LocalDate.now();
    }
}