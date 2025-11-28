package com.example.demo.service.user;

import com.example.demo.exceptions.RoleChangeNotAllowedException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.model.user.UserEntity;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity findUserById(String userId) {
        Optional<UserEntity> user = userRepository.findUserEntityById(userId);
        return user.orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
    }

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User principal) {
            return principal.getUserEntity();
        }
        return null;
    }

    public void grantTeacherRole(String userId) {
        UserEntity user = findUserById(userId);
        if(user.getRole() == UserEntity.UserRole.ADMIN){
            throw new RoleChangeNotAllowedException("Can't grant teacher role to admin");
        }
        user.setRole(UserEntity.UserRole.TEACHER);
        userRepository.save(user);
    }

}
