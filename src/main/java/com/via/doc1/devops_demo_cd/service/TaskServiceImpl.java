package com.via.doc1.devops_demo_cd.service;

import com.via.doc1.devops_demo_cd.model.Task; // Importing the Task entity
import com.via.doc1.devops_demo_cd.repository.TaskRepository; // Importing the Task repository
import lombok.RequiredArgsConstructor; // Lombok: Generates constructor for final fields
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.List;
import java.util.Optional;

@Service // Marks this class as a Spring service component
@RequiredArgsConstructor // Lombok: Generates a constructor injecting final fields (TaskRepository)
@Transactional(readOnly = true) // Default transactionality for read operations
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository; // Injected by Spring via constructor

    @Override
    @Transactional // Override readOnly for write operation
    public Task createTask(Task task) {
        // ID and lastModified are handled by JPA/Auditing
        task.setId(null); // Ensure we are creating a new one, not updating
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    @Transactional // Override readOnly for write operation
    public Optional<Task> updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setName(taskDetails.getName());
                    existingTask.setDescription(taskDetails.getDescription());
                    // lastModified will be updated automatically by JPA Auditing on save
                    return taskRepository.save(existingTask);
                });
        // If findById returns empty Optional, map() won't execute, and empty Optional is returned
    }

    @Override
    @Transactional // Override readOnly for write operation
    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false; // Task not found
    }
}