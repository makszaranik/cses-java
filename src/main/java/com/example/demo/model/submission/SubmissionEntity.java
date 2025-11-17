package com.example.demo.model.submission;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Document("submissions")
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionEntity implements Serializable {

    @Id
    private String id;
    private String taskId;
    private String userId;
    private String sourceCodeFileId; //user uploaded sourceCodeId
    private String logs;
    private Status status;
    private Integer score;

    @CreatedDate
    private LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    public enum Status {
        SUBMITTED,
        COMPILATION_SUCCESS,
        COMPILATION_ERROR,
        WRONG_ANSWER,
        ACCEPTED,
        TIMELIMIT_EXCEEDED,
        LINTER_PASSED,
        LINTER_FAILED,
    }
}
