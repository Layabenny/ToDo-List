package com.ty.todo.Repository;

import com.ty.todo.Model.Task;
import com.ty.todo.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
  
    List<Task> findByUser(User user);
    
    List<Task> findByUserAndCompleted(User user, boolean completed);
    
    List<Task> findByReminderTimeBeforeAndCompleted(LocalDateTime dateTime, boolean completed);
   
    @Query("SELECT t FROM Task t WHERE t.user = :user ORDER BY t.createdAt DESC")
    List<Task> findUserTasksOrdered(@Param("user") User user);
    
   
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate IS NOT NULL ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasks(@Param("user") User user);
    
    List<Task> findByReminderTimeBeforeAndCompletedFalse(LocalDateTime now);

}