package Replan.AI.model;

// Missed session recover করার possible strategy
public enum RecoveryType {
    MOVE_SESSION,
    SPLIT_SESSION,
    REDUCE_TARGET,
    INCREASE_FUTURE_DURATION,
    EXTEND_DEADLINE
}