package Replan.AI.repository;

import Replan.AI.model.RecoveryPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecoveryPlanRepository
        extends MongoRepository<RecoveryPlan, String> {

    List<RecoveryPlan> findByUserId(String userId);

    List<RecoveryPlan> findByHabitId(String habitId);
}