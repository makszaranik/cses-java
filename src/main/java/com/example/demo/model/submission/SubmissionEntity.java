package com.example.demo.model.submission;


import com.example.demo.service.executor.stage.StageExecutor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@Document("submissions")
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionEntity {

    @Id
    private String id;
    private String taskId;
    private String userId;
    private String sourceCodeFileId; //user uploaded sourceCodeId
    private Map<StageExecutor.Stages, String> logs;
    private Status status;
    private Integer score;

    @CreatedDate
    private LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    public enum Status {
        SUBMITTED,
        COMPILING,
        COMPILATION_SUCCESS,
        COMPILATION_ERROR,
        WRONG_ANSWER,
        ACCEPTED,
        TIME_LIMIT_EXCEEDED,
        OUT_OF_MEMORY_ERROR,
        LINTER_PASSED,
        LINTER_FAILED,
    }
}
