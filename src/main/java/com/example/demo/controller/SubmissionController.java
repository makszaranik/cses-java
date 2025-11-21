package com.example.demo.controller;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("history")
    @PreAuthorize("isAuthenticated()")
    public List<SubmissionEntity> getUserSubmissions() {
        String userId = userService.getCurrentUser().getId();
        return submissionService.getSubmissionsByUser(userId);
    }

}
