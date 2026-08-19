package Replan.AI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// প্রতিদিন Habit করা হয়েছে নাকি miss হয়েছে, তার history
@Document(collection = "habit_logs")
public class HabitLog {

    @Id
    private String id;

    // কোন Habit-এর session
    private String habitId;

    // Habit-এর owner
    private String userId;

    // Session কোন date-এ scheduled ছিল
    private LocalDate scheduledDate;

    // Session কোন সময় scheduled ছিল
    private LocalTime scheduledTime;

    // COMPLETED, MISSED, SCHEDULED অথবা RESCHEDULED
    private HabitLogStatus status;

    // User বাস্তবে কতটা complete করেছে
    private int completedValue;

    // Miss করার কারণ
    private String missReason;

    // Optional user note
    private String note;

    // Completion-এর exact date এবং time
    private LocalDateTime completedAt;

    // Log কখন তৈরি হয়েছিল
    private LocalDateTime createdAt;

    // Log শেষবার কখন বদলেছিল
    private LocalDateTime updatedAt;

    public HabitLog() {
    }

    public void prepareForCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void prepareForUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHabitId() {
        return habitId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public HabitLogStatus getStatus() {
        return status;
    }

    public void setStatus(HabitLogStatus status) {
        this.status = status;
    }

    public int getCompletedValue() {
        return completedValue;
    }

    public void setCompletedValue(int completedValue) {
        this.completedValue = completedValue;
    }

    public String getMissReason() {
        return missReason;
    }

    public void setMissReason(String missReason) {
        this.missReason = missReason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}