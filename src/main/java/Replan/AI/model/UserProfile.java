package Replan.AI.model;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "user_profiles")
public class UserProfile {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;


    @NotEmpty(message = "At least one available day is required")
    private List<DayOfWeek> availableDays;

    @NotNull(message = "Preferred start time is required")
    private LocalTime preferredStartTime;

    @NotNull(message = "Preferred end time is required")
    private LocalTime preferredEndTime;

    @Min(value = 1, message = "Daily workload must be at least 1 minute")
    @Max(value = 1440, message = "Daily workload cannot exceed 1440 minutes")
    private int maximumDailyMinutes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfile(){

    }

    public void prepareForCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void prepareForUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<DayOfWeek> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<DayOfWeek> availableDays) {
        this.availableDays = availableDays;
    }

    public LocalTime getPreferredStartTime() {
        return preferredStartTime;
    }

    public void setPreferredStartTime(LocalTime preferredStartTime) {
        this.preferredStartTime = preferredStartTime;
    }

    public LocalTime getPreferredEndTime() {
        return preferredEndTime;
    }

    public void setPreferredEndTime(LocalTime preferredEndTime) {
        this.preferredEndTime = preferredEndTime;
    }

    public int getMaximumDailyMinutes() {
        return maximumDailyMinutes;
    }

    public void setMaximumDailyMinutes(int maximumDailyMinutes) {
        this.maximumDailyMinutes = maximumDailyMinutes;
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
