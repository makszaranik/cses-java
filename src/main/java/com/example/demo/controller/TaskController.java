package com.example.demo.controller;

import com.example.demo.dto.submission.SubmissionResponseDto;
import com.example.demo.dto.task.*;
import com.example.demo.exceptions.ScoreExceededException;
import com.example.demo.exceptions.SubmissionNotAllowedException;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.event.EventService;
import com.example.demo.service.submission.SubmissionMapper;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskMapper;
import com.example.demo.service.task.TaskService;
import com.example.demo.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final SubmissionService submissionService;
    private final EventService eventService;
    private final UserService userService;
    private final SubmissionMapper submissionMapper;

    @PostMapping("submit")
    @PreAuthorize("isAuthenticated()")
    public SubmissionResponseDto submitTask(@RequestBody @Valid TaskSubmissionRequestDto submitDto) {
        TaskEntity task = taskService.findTaskById(submitDto.taskId());
        String userId = userService.getCurrentUser().getId();
        Integer submissionCount = submissionService.getNumberSubmissionsForTask(userId, submitDto.taskId());

        if (submissionCount >= task.getSubmissionsNumberLimit()) {
            throw new SubmissionNotAllowedException("Number of submissions exceeded");
        }

        SubmissionEntity submission = submissionService.createSubmission(submitDto);
        return submissionMapper.toDto(submission);
    }

    @GetMapping("status")
    @PreAuthorize("isAuthenticated()")
    public SseEmitter taskStatus(@RequestParam String submissionId) {
        String userId = userService.getCurrentUser().getId();
        SseEmitter emitter = new SseEmitter(TimeUnit.SECONDS.toMillis(60));
        eventService.createSubmissionStatusEvent(emitter, userId, submissionId);
        return emitter;
    }

    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public String createTask(@RequestBody @Valid TaskCreateRequestDto createDto) {
        if(createDto.testsPoints() + createDto.lintersPoints() != 100){
            throw new ScoreExceededException("Score sum must be 100");
        }

        String ownerId = userService.getCurrentUser().getId();
        TaskEntity task = taskMapper.toEntity(createDto, ownerId);
        return taskService.save(task);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public void deleteTask(@RequestBody @Valid TaskDeletionRequestDto deleteDto) {
        TaskEntity task = taskService.findTaskById(deleteDto.taskId());
        String ownerId = userService.getCurrentUser().getId();

        if (!ownerId.equals(task.getOwnerId())) {
            throw new AccessDeniedException("You are not allowed to delete this task");
        }

        taskService.removeTaskEntity(deleteDto.taskId());
    }

    @PutMapping("update")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public TaskResponseDto updateTask(@RequestBody @Valid TaskUpdateRequestDto updateDto) {
        TaskEntity task = taskService.findTaskById(updateDto.taskId());
        String ownerId = userService.getCurrentUser().getId();

        if(updateDto.testsPoints() + updateDto.lintersPoints() != 100){
            throw new ScoreExceededException("Score sum must be 100");
        }

        if (!ownerId.equals(task.getOwnerId())) {
            throw new AccessDeniedException("You are not allowed to update this task");
        }

        TaskEntity updatedTask = taskService.updateTask(taskMapper.toEntity(updateDto, ownerId));
        return taskMapper.toResponseDto(updatedTask);
    }

    @GetMapping("{id}")
    public TaskResponseDto findTask(@PathVariable String id) {
        TaskEntity task = taskService.findTaskById(id);
        return taskMapper.toResponseDto(task);
    }

    @GetMapping("owned")
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDto> findMyTasks() {
        String userId = userService.getCurrentUser().getId();
        List<TaskEntity> tasks = taskService.findAllByOwnerId(userId);
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .toList();
    }

    @GetMapping
    public List<TaskResponseDto> findAllTasks() {
        List<TaskEntity> tasks = taskService.findAll();
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .toList();
    }

}
