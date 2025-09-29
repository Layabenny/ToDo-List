package com.ty.todo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    private boolean completed = false;

    private LocalDateTime dueDate;

    private LocalDateTime reminderTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Task() {}

    public Task(String title, String description, LocalDateTime dueDate, LocalDateTime reminderTime, User user) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.reminderTime = reminderTime;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
 // Add these methods to your existing Task.java class

 // For handling datetime-local form inputs
 public void setDueDate(String dueDateString) {
     if (dueDateString != null && !dueDateString.isEmpty()) {
         try {
             this.dueDate = LocalDateTime.parse(dueDateString);
         } catch (DateTimeParseException e) {
             // If parsing fails, try with different format
             try {
                 this.dueDate = LocalDateTime.parse(dueDateString.replace(" ", "T"));
             } catch (DateTimeParseException e2) {
                 System.err.println("Failed to parse dueDate: " + dueDateString);
             }
         }
     }
 }

 public void setReminderTime(String reminderTimeString) {
     if (reminderTimeString != null && !reminderTimeString.isEmpty()) {
         try {
             this.reminderTime = LocalDateTime.parse(reminderTimeString);
         } catch (DateTimeParseException e) {
             // If parsing fails, try with different format
             try {
                 this.reminderTime = LocalDateTime.parse(reminderTimeString.replace(" ", "T"));
             } catch (DateTimeParseException e2) {
                 System.err.println("Failed to parse reminderTime: " + reminderTimeString);
             }
         }
     }
 }
}