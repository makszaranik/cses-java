package com.example.demo.controller;


import com.example.demo.dto.task.*;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.event.EventService;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskMapper;
import com.example.demo.service.task.TaskService;
import com.example.demo.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final SubmissionService submissionService;
    private final EventService eventService;
    private final UserService userService;

    @PostMapping("submit")
    @PreAuthorize("isAuthenticated()")
    public SubmissionEntity submitTask(@RequestBody @Valid TaskSubmissionRequestDto submitDto) {
        return submissionService.createSubmission(submitDto);
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public String createTask(@RequestBody @Valid TaskCreateRequestDto createDto) {
        String ownerId = userService.getCurrentUser().getId();
        TaskEntity task = taskMapper.toEntity(createDto, ownerId);
        return taskService.save(task);
    }


    @DeleteMapping("delete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public void deleteTask(@RequestBody @Valid TaskDeletionRequestDto deleteDto) {
        taskService.removeTaskEntity(deleteDto.taskId());
    }


    @PutMapping("update")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public void updateTask(@RequestBody @Valid TaskUpdateRequestDto updateDto) {
        String ownerId = userService.getCurrentUser().getId();
        taskService.updateTask(taskMapper.toEntity(updateDto, ownerId));
    }


    @GetMapping("{id}")
    public TaskResponseDto findTask(@PathVariable String id) {
        TaskEntity task = taskService.findTaskById(id);
        return taskMapper.toResponseDto(task);
    }


    @GetMapping
    public List<TaskResponseDto> findAllTasks() {
        List<TaskEntity> tasks = taskService.findAll();
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .toList();
    }

}
