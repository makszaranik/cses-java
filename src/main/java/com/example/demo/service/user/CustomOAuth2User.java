package com.example.demo.service.user;

import com.example.demo.model.user.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


@Getter
public class CustomOAuth2User implements OAuth2User {

    private final UserEntity userEntity;

    private final OAuth2User oAuth2User;

    public CustomOAuth2User(UserEntity userEntity, OAuth2User oAuth2User) {
        this.userEntity = userEntity;
        this.oAuth2User = oAuth2User;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + userEntity.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
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