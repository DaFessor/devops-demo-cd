package com.via.doc1.devops_demo_cd.service;

import com.via.doc1.devops_demo_cd.model.Task;
import com.via.doc1.devops_demo_cd.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Initialize Mockito annotations
class TaskServiceImplTest {

    @Mock // Create a mock TaskRepository
    private TaskRepository taskRepository;

    @InjectMocks // Create an instance of TaskServiceImpl and inject the mocks
    private TaskServiceImpl taskService;

    private Task task1;
    private Task task2;
    private Task taskInput;

    @BeforeEach
    void setUp() {
        // Initialize common test data
        task1 = new Task("Test Task 1", "Description 1");
        task1.setId(1L);
        task1.setLastModified(LocalDateTime.now().minusDays(1));

        task2 = new Task("Test Task 2", "Description 2");
        task2.setId(2L);
        task2.setLastModified(LocalDateTime.now());

        taskInput = new Task("New Task", "New Desc"); // No ID or lastModified yet
    }

    @Test
    void createTask_shouldSaveAndReturnTask() {
        // Arrange: Configure the mock repository's save method
        // When save is called with any Task object, return that object after setting an ID
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            taskToSave.setId(5L); // Simulate database assigning an ID
            taskToSave.setLastModified(LocalDateTime.now()); // Simulate auditing
            return taskToSave;
        });

        // Act: Call the service method
        Task createdTask = taskService.createTask(taskInput);

        // Assert
        assertNotNull(createdTask);
        assertEquals(5L, createdTask.getId()); // Check if ID was assigned
        assertEquals("New Task", createdTask.getName());
        assertNotNull(createdTask.getLastModified()); // Check if date was set

        // Verify that taskRepository.save was called exactly once
        verify(taskRepository, times(1)).save(any(Task.class));

        // Optional: Use ArgumentCaptor to inspect the object passed to save
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertNull(taskCaptor.getValue().getId(), "Service should ensure ID is null before saving for creation");
        assertEquals("New Task", taskCaptor.getValue().getName());
    }

    @Test
    void getAllTasks_shouldReturnListOfTasks() {
        // Arrange
        List<Task> expectedTasks = Arrays.asList(task1, task2);
        when(taskRepository.findAll()).thenReturn(expectedTasks);

        // Act
        List<Task> actualTasks = taskService.getAllTasks();

        // Assert
        assertNotNull(actualTasks);
        assertEquals(2, actualTasks.size());
        assertEquals(expectedTasks, actualTasks);

        // Verify
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTaskById_whenTaskExists_shouldReturnTask() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task1));

        // Act
        Optional<Task> foundTask = taskService.getTaskById(taskId);

        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals(task1, foundTask.get());
        assertEquals(taskId, foundTask.get().getId());

        // Verify
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_shouldReturnEmptyOptional() {
        // Arrange
        Long taskId = 99L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<Task> foundTask = taskService.getTaskById(taskId);

        // Assert
        assertTrue(foundTask.isEmpty());

        // Verify
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void updateTask_whenTaskExists_shouldUpdateAndReturnTask() {
        // Arrange
        Long taskId = 1L;
        Task updateDetails = new Task("Updated Name", "Updated Desc");
        Task existingTask = task1; // Use the task from setUp

        // Mock findById to return the existing task
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Mock save to return the saved entity (important for verifying the result)
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
             // Simulate auditing updating the timestamp on save
            savedTask.setLastModified(LocalDateTime.now().plusMinutes(1));
            return savedTask;
           });


        // Act
        Optional<Task> updatedTaskOptional = taskService.updateTask(taskId, updateDetails);

        // Assert
        assertTrue(updatedTaskOptional.isPresent());
        Task updatedTask = updatedTaskOptional.get();
        assertEquals(taskId, updatedTask.getId()); // ID should remain the same
        assertEquals("Updated Name", updatedTask.getName()); // Name should be updated
        assertEquals("Updated Desc", updatedTask.getDescription()); // Description should be updated
        assertNotEquals(task1.getLastModified(), updatedTask.getLastModified()); // Timestamp should change

        // Verify findById and save were called
        verify(taskRepository, times(1)).findById(taskId);

        // Verify save was called with the updated details merged into the existing task
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertEquals(taskId, savedTask.getId());
        assertEquals("Updated Name", savedTask.getName());
        assertEquals("Updated Desc", savedTask.getDescription());
    }

    @Test
    void updateTask_whenTaskDoesNotExist_shouldReturnEmptyOptional() {
        // Arrange
        Long taskId = 99L;
        Task updateDetails = new Task("Updated Name", "Updated Desc");
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<Task> updatedTaskOptional = taskService.updateTask(taskId, updateDetails);

        // Assert
        assertTrue(updatedTaskOptional.isEmpty());

        // Verify findById was called, but save was not
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_whenTaskExists_shouldDeleteAndReturnTrue() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);
        // Mock void method deleteById - doNothing is default, but explicit is clearer
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        boolean deleted = taskService.deleteTask(taskId);

        // Assert
        assertTrue(deleted);

        // Verify existsById and deleteById were called
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void deleteTask_whenTaskDoesNotExist_shouldReturnFalse() {
        // Arrange
        Long taskId = 99L;
        when(taskRepository.existsById(taskId)).thenReturn(false);

        // Act
        boolean deleted = taskService.deleteTask(taskId);

        // Assert
        assertFalse(deleted);

        // Verify existsById was called, but deleteById was not
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskRepository, never()).deleteById(taskId);
    }
}