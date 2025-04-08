package com.via.doc1.devops_demo_cd.controller;

import com.via.doc1.devops_demo_cd.model.Task;
import com.via.doc1.devops_demo_cd.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper; // For JSON serialization
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class) // Test only the TaskController slice
class TaskControllerTest {

    @Autowired // MockMvc is auto-configured by @WebMvcTest
    private MockMvc mockMvc;

    @MockitoBean // Creates a Mockito mock and adds it to the ApplicationContext
    private TaskService taskService;

    @Autowired // ObjectMapper is usually available in the test context
    private ObjectMapper objectMapper;

    private Task task1;
    private Task task2;
    private Task taskInput;
    private Task taskCreated;

    @BeforeEach
    void setUp() {
        task1 = new Task("Task One", "Desc One");
        task1.setId(1L);
        task1.setLastModified(LocalDateTime.now().minusHours(1));

        task2 = new Task("Task Two", "Desc Two");
        task2.setId(2L);
        task2.setLastModified(LocalDateTime.now());

        taskInput = new Task("New Task", "New Desc"); // For POST/PUT request body

        taskCreated = new Task("New Task", "New Desc"); // For POST response mock
        taskCreated.setId(3L);
        taskCreated.setLastModified(LocalDateTime.now());
    }

    @Test
    void getAllTasks_shouldReturnListOfTasks() throws Exception {
        // Arrange
        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Task One")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Task Two")));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void getTaskById_whenTaskExists_shouldReturnTask() throws Exception {
        // Arrange
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(task1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Task One")))
                .andExpect(jsonPath("$.description", is("Desc One")));

        verify(taskService, times(1)).getTaskById(taskId);
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        Long taskId = 99L;
        // Configure the mock service to throw the exception the controller expects
        when(taskService.getTaskById(taskId)).thenReturn(Optional.empty());
        // Or alternatively:
        // when(taskService.getTaskById(taskId)).thenThrow(new ResourceNotFoundException("Task not found with id: " + taskId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
                // If using GlobalExceptionHandler, you might assert the error message:
                // .andExpect(jsonPath("$.message", containsString("Task not found with id: 99")));

        verify(taskService, times(1)).getTaskById(taskId);
    }

    @Test
    void createTask_withValidInput_shouldReturnCreated() throws Exception {
        // Arrange
        when(taskService.createTask(any(Task.class))).thenReturn(taskCreated);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskInput))) // Serialize input object to JSON
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/tasks/" + taskCreated.getId()))) // Check Location header
                .andExpect(jsonPath("$.id", is(taskCreated.getId().intValue()))) // Check response body
                .andExpect(jsonPath("$.name", is(taskInput.getName())))
                .andExpect(jsonPath("$.description", is(taskInput.getDescription())))
                .andExpect(jsonPath("$.lastModified", notNullValue()));


        // Verify service method was called
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).createTask(taskCaptor.capture());
        assertEquals(taskInput.getName(), taskCaptor.getValue().getName());
        assertEquals(taskInput.getDescription(), taskCaptor.getValue().getDescription());
        assertNull(taskCaptor.getValue().getId()); // Controller should pass the raw input
    }

     @Test
    void createTask_withMissingName_shouldReturnBadRequest() throws Exception {
        // Arrange
        Task invalidTask = new Task(null, "Some Description"); // Invalid input

        // Act & Assert
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request

        // Verify service method was NOT called
        verify(taskService, never()).createTask(any(Task.class));
    }


    @Test
    void updateTask_whenTaskExists_shouldReturnOk() throws Exception {
        // Arrange
        Long taskId = 1L;
        Task updatedDetails = new Task("Updated Task 1", "Updated Desc 1");
        Task returnedUpdatedTask = new Task("Updated Task 1", "Updated Desc 1"); // Task returned by service
        returnedUpdatedTask.setId(taskId);
        returnedUpdatedTask.setLastModified(LocalDateTime.now().plusSeconds(10)); // Simulate updated timestamp

        when(taskService.updateTask(eq(taskId), any(Task.class))).thenReturn(Optional.of(returnedUpdatedTask));

        // Act & Assert
        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.intValue())))
                .andExpect(jsonPath("$.name", is(updatedDetails.getName())))
                .andExpect(jsonPath("$.description", is(updatedDetails.getDescription())))
                .andExpect(jsonPath("$.lastModified", not(equalTo(task1.getLastModified().toString())))); // Check timestamp changed


        // Verify service method was called correctly
         ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).updateTask(eq(taskId), taskCaptor.capture());
        assertEquals(updatedDetails.getName(), taskCaptor.getValue().getName());
        assertEquals(updatedDetails.getDescription(), taskCaptor.getValue().getDescription());
    }

    @Test
    void updateTask_whenTaskDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        Long taskId = 99L;
        Task updatedDetails = new Task("Updated Task 99", "Updated Desc 99");
        when(taskService.updateTask(eq(taskId), any(Task.class))).thenReturn(Optional.empty());
        // Or throw ResourceNotFoundException if service throws it directly
        // when(taskService.updateTask(eq(taskId), any(Task.class))).thenThrow(new ResourceNotFoundException("..."));


        // Act & Assert
        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).updateTask(eq(taskId), any(Task.class));
    }

     @Test
    void updateTask_withMissingName_shouldReturnBadRequest() throws Exception {
        // Arrange
        Long taskId = 1L;
        Task invalidUpdate = new Task(null, "Updated Desc"); // Invalid input

        // Act & Assert
        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        // Verify service method was NOT called
        verify(taskService, never()).updateTask(anyLong(), any(Task.class));
    }

    @Test
    void deleteTask_whenTaskExists_shouldReturnNoContent() throws Exception {
        // Arrange
        Long taskId = 1L;
        when(taskService.deleteTask(taskId)).thenReturn(true); // Service confirms deletion happened

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNoContent()); // 204 No Content

        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    void deleteTask_whenTaskDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        Long taskId = 99L;
        when(taskService.deleteTask(taskId)).thenReturn(false); // Service indicates task wasn't found
        // Or mock service to throw ResourceNotFoundException directly if it does that
        // when(taskService.deleteTask(taskId)).thenThrow(new ResourceNotFoundException("..."));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).deleteTask(taskId);
    }
}