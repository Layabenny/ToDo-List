package com.ty.todo.Service;

import com.ty.todo.Model.Task;
import com.ty.todo.Model.User;
import com.ty.todo.Repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, Task taskDetails) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setCompleted(taskDetails.isCompleted());
        existingTask.setDueDate(taskDetails.getDueDate());
        existingTask.setReminderTime(taskDetails.getReminderTime());

        return taskRepository.save(existingTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        taskRepository.delete(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
    }

    public List<Task> getTasksByUser(User user) {
        return taskRepository.findByUser(user);
    }
    
    public List<Task> getDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findByReminderTimeBeforeAndCompletedFalse(now);
    }
}
