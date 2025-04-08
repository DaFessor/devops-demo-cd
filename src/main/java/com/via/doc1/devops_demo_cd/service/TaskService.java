package com.via.doc1.devops_demo_cd.service;

import com.via.doc1.devops_demo_cd.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskService {
    Task createTask(Task task);
    List<Task> getAllTasks();
    Optional<Task> getTaskById(Long id);
    Optional<Task> updateTask(Long id, Task taskDetails);
    boolean deleteTask(Long id);
}