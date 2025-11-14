package com.example.demo.config;

import com.example.demo.model.user.UserEntity;
import com.example.demo.model.user.UserEntity.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GitHubOAuth2Filter extends OncePerRequestFilter {

    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String username = oAuth2User.getAttribute("login");
            UserEntity user = userService.findByUsername(username)
                    .orElseGet(() -> {
                        UserEntity newUser = new UserEntity();
                        newUser.setUsername(username);
                        newUser.setEmail(oAuth2User.getAttribute("email"));
                        newUser.setRole(UserRole.STUDENT);
                        return userService.save(newUser);
                    });

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    user, null, List.of(new SimpleGrantedAuthority(user.getRole().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
        filterChain.doFilter(request, response);
    }
}
