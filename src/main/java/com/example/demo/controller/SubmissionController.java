package com.example.demo.controller;

import com.example.demo.dto.submission.SubmissionResponseDto;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.submission.SubmissionMapper;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserService userService;
    private final SubmissionMapper submissionMapper;

    @GetMapping("{taskId}/history")
    @PreAuthorize("isAuthenticated()")
    public List<SubmissionResponseDto> getUserSubmissions(@PathVariable String taskId) {
        String userId = userService.getCurrentUser().getId();
        List<SubmissionEntity> submissionsByUser = submissionService.getSubmissionsByUser(userId);
        return submissionsByUser.stream()
                .filter(submission -> submission.getTaskId().equals(taskId))
                .map(submissionMapper::toDto)
                .toList();
    }

}
