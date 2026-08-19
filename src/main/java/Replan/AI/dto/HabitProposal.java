package Replan.AI.dto;

import Replan.AI.model.FrequencyType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record HabitProposal(

        String name,
        String description,
        FrequencyType frequencyType,
        List<DayOfWeek> daysOfWeek,
        int targetValue,
        String unit,
        int durationMinutes,
        LocalTime preferredTime,
        LocalDate startDate

) {
}