package com.example.demo.security;

import com.example.demo.exceptions.InvalidTokenException;
import com.example.demo.model.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Component
@RequiredArgsConstructor
public class PreAuthorizeAspect {

    private final UserContext userContext;

    @Before("@annotation(preAuthorize)")
    public void checkRole(PreAuthorize preAuthorize) {
        UserEntity.UserRole[] allowedRoles = preAuthorize.roles();
        UserEntity.UserRole userRole = userContext.get().getRole();
        boolean hasPermissions = Arrays.asList(allowedRoles).contains(userRole);
        if(!hasPermissions){
            throw new InvalidTokenException("No permission");
        }
    }
}
