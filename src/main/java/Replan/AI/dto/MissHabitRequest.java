package Replan.AI.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class MissHabitRequest {

    // Null হলে backend আজকের date ব্যবহার করবে
    private LocalDate scheduledDate;

    @NotBlank(message = "Miss reason is required")
    private String missReason;

    private String note;

    public MissHabitRequest() {
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
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
}