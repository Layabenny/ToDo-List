package com.ty.todo.Controller;

import com.ty.todo.Model.Task;
import com.ty.todo.Model.User;
import com.ty.todo.Service.TaskService;
import com.ty.todo.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    // Test endpoint to check if controller is working
    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint() {
        return "TaskController is working!";
    }

    @GetMapping("/")
    public String showHomePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            // Get current user from database
            User currentUser = userService.findById(user.getId());
            List<Task> tasks = taskService.getTasksByUser(currentUser);
            
            model.addAttribute("tasks", tasks);
            model.addAttribute("newTask", new Task());
            model.addAttribute("currentDate", LocalDateTime.now());
            return "home";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading tasks: " + e.getMessage());
            model.addAttribute("newTask", new Task());
            return "home";
        }
    }

    @GetMapping("/tasks")
    public String showAllTasks(HttpSession session, Model model) {
        System.out.println("=== /tasks endpoint called ===");

        User user = (User) session.getAttribute("user");
        if (user == null) {
            System.out.println("No user in session - redirecting to login");
            return "redirect:/auth/login";
        }

        System.out.println("User in session: " + user.getUsername());

        try {
            User currentUser = userService.findById(user.getId());
            List<Task> tasks = taskService.getTasksByUser(currentUser);
            model.addAttribute("tasks", tasks);

            // add same attributes as home so the tasks-view template can use them
            model.addAttribute("newTask", new Task());
            model.addAttribute("currentDate", LocalDateTime.now());
            model.addAttribute("isEditing", false);
            model.addAttribute("task", new Task()); // empty task for safety

            System.out.println("Loaded " + tasks.size() + " tasks from database");
            return "tasks-view";
        } catch (Exception e) {
            System.out.println("Error in /tasks: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading tasks: " + e.getMessage());
            model.addAttribute("newTask", new Task());
            model.addAttribute("currentDate", LocalDateTime.now());
            model.addAttribute("isEditing", false);
            return "tasks-view";
        }
    }


    @PostMapping("/tasks")
    public String createTask(@ModelAttribute("newTask") Task task,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        System.out.println("=== CREATE TASK CALLED ===");
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            // Debug: Print task details
            System.out.println("Task title: " + task.getTitle());
            System.out.println("Task description: " + task.getDescription());
            System.out.println("Task dueDate: " + task.getDueDate());
            System.out.println("Task reminderTime: " + task.getReminderTime());

            // Get current user from database
            User currentUser = userService.findById(user.getId());
            task.setUser(currentUser);
            
            // Save the task
            Task savedTask = taskService.createTask(task);
            System.out.println("Task saved with ID: " + savedTask.getId());
            
            redirectAttributes.addFlashAttribute("success", "Task '" + task.getTitle() + "' created successfully!");
            
        } catch (Exception e) {
            System.out.println("Error creating task: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating task: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/complete")
    public String toggleTaskCompletion(@PathVariable Long id,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Task task = taskService.getTaskById(id);
            
            // Verify the task belongs to the current user
            if (!task.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/";
            }
            
            task.setCompleted(!task.isCompleted());
            taskService.updateTask(id, task);
            
            String message = task.isCompleted() ? 
                "Task marked as completed!" : "Task marked as pending!";
            redirectAttributes.addFlashAttribute("success", message);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating task: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Task task = taskService.getTaskById(id);
            
            // Verify the task belongs to the current user
            if (!task.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/";
            }
            
            String taskTitle = task.getTitle();
            taskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("success", "Task '" + taskTitle + "' deleted successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting task: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    @GetMapping("/tasks/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Task task = taskService.getTaskById(id);

            // verify ownership
            if (!task.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/tasks";
            }

            model.addAttribute("task", task);
            return "task-edit"; // separate Thymeleaf template
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Task not found");
            return "redirect:/tasks";
        }
    }

    @PostMapping("/tasks/{id}/edit")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute("task") Task taskDetails,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            Task existingTask = taskService.getTaskById(id);

            if (!existingTask.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/tasks";
            }

            // update fields
            existingTask.setTitle(taskDetails.getTitle());
            existingTask.setDescription(taskDetails.getDescription());
            existingTask.setDueDate(taskDetails.getDueDate());
            existingTask.setReminderTime(taskDetails.getReminderTime());
            existingTask.setCompleted(taskDetails.isCompleted());

            taskService.updateTask(id, existingTask);

            redirectAttributes.addFlashAttribute("success", "Task updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating task: " + e.getMessage());
        }

        return "redirect:/tasks";
    }


    // Debug endpoint to check tasks in database
    @GetMapping("/debug-tasks")
    @ResponseBody
    public String debugTasks(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "No user in session";
        }
        
        try {
            User currentUser = userService.findById(user.getId());
            List<Task> tasks = taskService.getTasksByUser(currentUser);
            
            StringBuilder result = new StringBuilder();
            result.append("User: ").append(currentUser.getUsername()).append("<br>");
            result.append("Total tasks: ").append(tasks.size()).append("<br><br>");
            
            for (Task task : tasks) {
                result.append("Task: ").append(task.getTitle())
                      .append(" (ID: ").append(task.getId())
                      .append(", Completed: ").append(task.isCompleted())
                      .append(")<br>");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/reminders/due")
    @ResponseBody
    public List<Task> getDueReminders() {
        return taskService.getDueReminders();
    }

   
}