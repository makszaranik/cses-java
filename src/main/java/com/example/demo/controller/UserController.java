package com.example.demo.controller;


import com.example.demo.dto.repository.GithubRepositoryResponseDto;
import com.example.demo.dto.task.stats.UserStatsResponseDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.model.user.UserEntity;
import com.example.demo.service.github.GithubService;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("users")
public class UserController {

    private final UserService userService;
    private final GithubService githubService;
    private final SubmissionService submissionService;

    @PostMapping("{userId}/grant-teacher")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void grantTeacherRole(@PathVariable String userId) {
        userService.grantRole(userId, UserEntity.UserRole.TEACHER);
    }

    @GetMapping("github-repos")
    @PreAuthorize("isAuthenticated()")
    public List<GithubRepositoryResponseDto> getAllGithubRepos() {
        String userId = userService.getCurrentUser().getId();
        return githubService.getAllUserReposNames(userId);
    }

    @GetMapping("statistics")
    @PreAuthorize("isAuthenticated()")
    public UserStatsResponseDto getUserStats() {
        String userId = userService.getCurrentUser().getId();
        return submissionService.calculateStatisticsForTask(userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponseDto> getAllUsers() {
        return userService.findAllUsers().stream()
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getRole()))
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDto getCurrentUser() {
        UserEntity user = userService.getCurrentUser();
        return new UserResponseDto(user.getId(), user.getUsername(), user.getRole());
    }

}
