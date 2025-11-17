package com.example.demo.controller;


import com.example.demo.model.user.UserEntity;
import com.example.demo.service.github.GithubService;
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

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("{userId}/grant-teacher")
    public void grantTeacherRole(@PathVariable String userId) {
        userService.grantRole(userId, UserEntity.UserRole.TEACHER);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("repos")
    public List<GithubService.RepoData> getAllGithubRepoIds(){
        return githubService.getAllUserReposNames();
    }

}
