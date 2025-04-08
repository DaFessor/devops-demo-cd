package com.via.doc1.devops_demo_cd.controller;

import com.via.doc1.devops_demo_cd.model.Task; // Importing the Task entity
import com.via.doc1.devops_demo_cd.service.TaskService; // Importing the Task service
import com.via.doc1.devops_demo_cd.exception.ResourceNotFoundException; // Custom exception for not found resources
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.List;

@RestController // Combines @Controller and @ResponseBody, handles JSON conversion
@RequestMapping("/api/v1/tasks") // Base path for all endpoints in this controller
@RequiredArgsConstructor // Lombok: Constructor injection for TaskService
public class TaskController {

    private final TaskService taskService;

    // GET /api/v1/tasks - List all tasks
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks); // 200 OK with list in body
    }

    // GET /api/v1/tasks/{id} - Get a single task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return ResponseEntity.ok(task); // 200 OK with task in body
    }

    // POST /api/v1/tasks - Add a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        // Basic validation (could be enhanced with @Valid and validation annotations)
        if (task.getName() == null || task.getName().trim().isEmpty()) {
             return ResponseEntity.badRequest().build(); // Or throw specific validation exception
        }
        Task createdTask = taskService.createTask(task);

        // Build the location URI for the newly created resource
         URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTask); // 201 Created with location header and task body
    }

    // PUT /api/v1/tasks/{id} - Update an existing task
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
         if (taskDetails.getName() == null || taskDetails.getName().trim().isEmpty()) {
             return ResponseEntity.badRequest().build();
         }
        Task updatedTask = taskService.updateTask(id, taskDetails)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return ResponseEntity.ok(updatedTask); // 200 OK with updated task
    }

    // DELETE /api/v1/tasks/{id} - Delete a task by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
