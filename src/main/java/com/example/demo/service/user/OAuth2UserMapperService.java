package com.example.demo.service.user;

import com.example.demo.model.user.UserEntity;
import com.example.demo.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserMapperService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String username = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");

        UserEntity user = userRepository.findByUsername(username).orElseGet(() -> {
            UserEntity userEntity = UserEntity.builder()
                    .username(username)
                    .email(email)
                    .role(UserEntity.UserRole.STUDENT)
                    .build();
            return userRepository.save(userEntity);
        });

        log.info("user={}", user);

        return new CustomOAuth2User(user, oAuth2User);
    }

}
