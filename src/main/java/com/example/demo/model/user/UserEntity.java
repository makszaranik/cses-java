package com.example.demo.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Builder
@Document("users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private String id;
    private String username;
    private String email;
    private UserRole role;

    public enum UserRole {
        STUDENT,
        TEACHER,
        ADMIN
    }
}

