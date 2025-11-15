package com.example.demo.controller;


import com.example.demo.model.user.UserEntity;
import com.example.demo.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("{userId}/grant-teacher")
    public void grantTeacherRole(@PathVariable String userId) {
        userService.grantRole(userId, UserEntity.UserRole.TEACHER);
    }



}
