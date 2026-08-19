package Replan.AI.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class RecoveryRequest {

    private LocalDate missedDate;

    @NotBlank(message = "Miss reason is required")
    private String reason;

    public RecoveryRequest() {
    }

    public LocalDate getMissedDate() {
        return missedDate;
    }

    public void setMissedDate(LocalDate missedDate) {
        this.missedDate = missedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}