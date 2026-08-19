package Replan.AI.service;

import Replan.AI.model.FrequencyType;
import Replan.AI.model.Habit;
import Replan.AI.model.HabitStatus;
import Replan.AI.repository.HabitRepository;
import Replan.AI.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserProfileRepository profileRepository;

    public HabitService(
            HabitRepository habitRepository,
            UserProfileRepository profileRepository) {

        this.habitRepository = habitRepository;
        this.profileRepository = profileRepository;
    }

    public Habit createHabit(Habit habit) {
        validateUser(habit.getUserId());
        prepareDays(habit);

        habit.setId(null);
        habit.prepareForCreate();

        return habitRepository.save(habit);
    }

    public Habit getHabitById(String habitId) {
        return habitRepository.findById(habitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Habit not found"
                ));
    }

    public List<Habit> getHabitsByUser(String userId) {
        validateUser(userId);
        return habitRepository.findByUserId(userId);
    }

    public Habit updateHabit(String habitId, Habit updatedHabit) {
        Habit existingHabit = getHabitById(habitId);

        updatedHabit.setUserId(existingHabit.getUserId());
        prepareDays(updatedHabit);

        existingHabit.setName(updatedHabit.getName());
        existingHabit.setDescription(updatedHabit.getDescription());
        existingHabit.setFrequencyType(updatedHabit.getFrequencyType());
        existingHabit.setDaysOfWeek(updatedHabit.getDaysOfWeek());
        existingHabit.setTargetValue(updatedHabit.getTargetValue());
        existingHabit.setUnit(updatedHabit.getUnit());
        existingHabit.setDurationMinutes(updatedHabit.getDurationMinutes());
        existingHabit.setPreferredTime(updatedHabit.getPreferredTime());
        existingHabit.setStartDate(updatedHabit.getStartDate());
        existingHabit.prepareForUpdate();

        return habitRepository.save(existingHabit);
    }

    public Habit pauseHabit(String habitId) {
        Habit habit = getHabitById(habitId);

        habit.setStatus(HabitStatus.PAUSED);
        habit.prepareForUpdate();

        return habitRepository.save(habit);
    }

    private void validateUser(String userId) {
        if (!profileRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User profile not found"
            );
        }
    }

    private void prepareDays(Habit habit) {
        if (habit.getFrequencyType() == FrequencyType.DAILY) {
            habit.setDaysOfWeek(List.of(DayOfWeek.values()));
        }

        else if (habit.getFrequencyType() == FrequencyType.WEEKDAYS) {
            habit.setDaysOfWeek(List.of(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
            ));
        }

        else if (habit.getFrequencyType() == FrequencyType.WEEKENDS) {
            habit.setDaysOfWeek(List.of(
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
            ));
        }

        else if (habit.getFrequencyType() == FrequencyType.SELECTED_DAYS
                && (habit.getDaysOfWeek() == null
                || habit.getDaysOfWeek().isEmpty())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected days are required"
            );
        }
    }
}