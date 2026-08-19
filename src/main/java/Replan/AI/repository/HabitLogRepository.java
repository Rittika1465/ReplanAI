package Replan.AI.repository;

import Replan.AI.model.HabitLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitLogRepository
        extends MongoRepository<HabitLog, String> {

    Optional<HabitLog> findByHabitIdAndScheduledDate(
            String habitId,
            LocalDate scheduledDate
    );

    List<HabitLog> findByUserId(String userId);

    List<HabitLog> findByUserIdAndScheduledDateBetweenOrderByScheduledDateAsc(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    );
}