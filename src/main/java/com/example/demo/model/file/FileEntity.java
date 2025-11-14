package com.example.demo.model.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@Document("files")
@NoArgsConstructor
@AllArgsConstructor
public abstract class FileEntity {

    @Id
    private String id;
    private String ownerId;
    private String gridFSFileId;

    @CreatedBy
    protected String createdBy;

    @CreatedDate
    protected LocalDateTime createdAt;

    @LastModifiedDate
    protected LocalDateTime lastModifiedAt;

    public enum FileType {
        LINTER,
        SOLUTION,
        SOLUTION_TEMPLATE,
        TEST
    }
}

