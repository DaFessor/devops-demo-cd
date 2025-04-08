package com.via.doc1.devops_demo_cd.repository;

import com.via.doc1.devops_demo_cd.model.Task; // Importing the Task entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Marks this interface as a Spring Data repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // JpaRepository provides standard CRUD methods (save, findById, findAll, deleteById, etc.)
    // We don't need to write implementations for these basic operations.
    // Custom query methods can be added here if needed (e.g., findByName(String name)).
}
