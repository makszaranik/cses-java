package com.example.demo.security;

import com.example.demo.model.user.UserEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final ThreadLocal<UserEntity> user = new ThreadLocal<>();

    public void set(UserEntity user) {
        this.user.set(user);
    }

    public UserEntity get() {
        return user.get();
    }

    public void remove() {
        user.remove();
    }

}