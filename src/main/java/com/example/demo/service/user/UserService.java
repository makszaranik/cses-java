package com.example.demo.service.user;

import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.model.user.UserEntity;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserEntity findUserByUsername(String username) {
        Optional<UserEntity> user = userRepository.findByUsername(username);
        return user.orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));
    }

    public UserEntity findUserById(String userId) {
        Optional<UserEntity> user = userRepository.findUserEntityById(userId);
        return user.orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
    }


    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) authentication.getPrincipal()).getUserEntity();
        }
        return null;
    }

    public void grantRole(String userId, UserEntity.UserRole role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

}
