package com.example.demo.security;

import com.example.demo.exceptions.InvalidTokenException;
import com.example.demo.model.user.UserEntity;
import com.example.demo.service.user.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserContext userContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws InvalidTokenException, ServletException, IOException {
        try {
            Optional<UserEntity> user = Optional.empty();
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
                String token = authorizationHeader.substring(7);
                String username = jwtService.validateTokenAndGetSubject(token);
                user = userService.findByUsername(username);
            }
            userContext.set(user.orElse(UserEntity.builder()  //empty user
                    .role(UserEntity.UserRole.GUEST)
                    .build())
            );
            log.info("filter chain ");
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } finally {
            log.info("context remove");
            userContext.remove();
        }
    }
}