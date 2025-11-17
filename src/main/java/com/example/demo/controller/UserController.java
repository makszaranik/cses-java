package com.example.demo.controller;


import com.example.demo.dto.repo.RepoResponseDto;
import com.example.demo.dto.statistics.TaskStatsRecordDto;
import com.example.demo.dto.statistics.UserStatsResponseDto;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.model.user.UserEntity;
import com.example.demo.service.github.GithubService;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskService;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("users")
public class UserController {

    private final UserService userService;
    private final GithubService githubService;
    private final SubmissionService submissionService;
    private final TaskService taskService;

    @PostMapping("{userId}/grant-teacher")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void grantTeacherRole(@PathVariable String userId) {
        userService.grantRole(userId, UserEntity.UserRole.TEACHER);
    }

    @GetMapping("repos")
    @PreAuthorize("isAuthenticated()")
    public List<RepoResponseDto> getAllGithubRepoIds(){
        return githubService.getAllUserReposNames();
    }

    /*
    @GetMapping("{userId}/stats")
    @PreAuthorize("isAuthenticated()")
    public UserStatsResponseDto getUserStats(@PathVariable String userId) {
        List<SubmissionEntity> submissions = submissionService.findAllSubmittedByUserId(userId);

    }

     */

}
