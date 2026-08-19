package Replan.AI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "recovery_plans")
public class RecoveryPlan {

    @Id
    private String id;

    private String userId;
    private String habitId;

    // Habit miss করার কারণ
    private String reason;

    // MOVE_SESSION, SPLIT_SESSION ইত্যাদি
    private RecoveryType recoveryType;

    // আগে কখন session হওয়ার কথা ছিল
    private LocalDateTime originalDateTime;

    // RePlan engine যে নতুন সময় suggest করেছে
    private LocalDateTime proposedDateTime;

    // Habit-এর আগের target
    private Integer originalTarget;

    // Temporary ছোট target, যদি প্রয়োজন হয়
    private Integer temporaryTarget;

    // PENDING, CONFIRMED অথবা REJECTED
    private RecoveryStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    public RecoveryPlan() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHabitId() {
        return habitId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RecoveryType getRecoveryType() {
        return recoveryType;
    }

    public void setRecoveryType(RecoveryType recoveryType) {
        this.recoveryType = recoveryType;
    }

    public LocalDateTime getOriginalDateTime() {
        return originalDateTime;
    }

    public void setOriginalDateTime(LocalDateTime originalDateTime) {
        this.originalDateTime = originalDateTime;
    }

    public LocalDateTime getProposedDateTime() {
        return proposedDateTime;
    }

    public void setProposedDateTime(LocalDateTime proposedDateTime) {
        this.proposedDateTime = proposedDateTime;
    }

    public Integer getOriginalTarget() {
        return originalTarget;
    }

    public void setOriginalTarget(Integer originalTarget) {
        this.originalTarget = originalTarget;
    }

    public Integer getTemporaryTarget() {
        return temporaryTarget;
    }

    public void setTemporaryTarget(Integer temporaryTarget) {
        this.temporaryTarget = temporaryTarget;
    }

    public RecoveryStatus getStatus() {
        return status;
    }

    public void setStatus(RecoveryStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}