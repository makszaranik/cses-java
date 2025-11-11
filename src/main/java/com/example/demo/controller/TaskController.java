package com.example.demo.controller;


import com.example.demo.dto.task.*;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.model.user.UserEntity.UserRole;
import com.example.demo.security.PreAuthorize;
import com.example.demo.security.UserContext;
import com.example.demo.service.event.EventService;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskMapper;
import com.example.demo.service.task.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final UserContext userContext;
    private final SubmissionService submissionService;
    private final EventService eventService;

    @PostMapping("submit")
    //@PreAuthorize(roles = {UserRole.ADMIN, UserRole.STUDENT, UserRole.TEACHER})
    public SubmissionEntity submitTask(@RequestBody @Valid TaskSubmissionRequestDto submitDto) {
        return submissionService.createSubmission(submitDto);
    }


    @GetMapping("status")
    public SseEmitter taskStatus(@RequestParam String submissionId) {
        String userId = userContext.get().getId();
        SseEmitter emitter = new SseEmitter(TimeUnit.SECONDS.toMillis(30));
        eventService.createSubmissionStatusEvent(emitter, userId, submissionId);
        return emitter;
    }


    @PostMapping("create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(roles = {UserRole.ADMIN, UserRole.TEACHER})
    public String createTask(@RequestBody @Valid TaskCreateRequestDto createDto) {
        String ownerId = userContext.get().getId();
        TaskEntity task = taskMapper.toEntity(createDto, ownerId);
        return taskService.save(task);
    }


    @DeleteMapping("delete")
    @PreAuthorize(roles = {UserRole.ADMIN, UserRole.TEACHER})
    public void deleteTask(@RequestBody @Valid TaskDeletionRequestDto deleteDto) {
        taskService.removeTaskEntity(deleteDto.taskId());
    }


    @PutMapping("update")
    @PreAuthorize(roles = {UserRole.ADMIN, UserRole.TEACHER})
    public void updateTask(@RequestBody @Valid TaskUpdateRequestDto updateDto) {
        String ownerId = userContext.get().getId();
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
