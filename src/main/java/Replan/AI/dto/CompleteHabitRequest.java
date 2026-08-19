package Replan.AI.dto;

import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public class CompleteHabitRequest {

    // Null হলে backend আজকের date ব্যবহার করবে
    private LocalDate scheduledDate;

    @Min(value = 0, message = "Completed value cannot be negative")
    private int completedValue;

    private String note;

    public CompleteHabitRequest() {
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public int getCompletedValue() {
        return completedValue;
    }

    public void setCompletedValue(int completedValue) {
        this.completedValue = completedValue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}