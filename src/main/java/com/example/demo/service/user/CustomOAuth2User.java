package com.example.demo.service.user;

import com.example.demo.model.user.UserEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserEntity userEntity;
    private final OAuth2User oAuth2User;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + userEntity.getRole().name();
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public String getName() {
        return userEntity.getUsername();
    }
}