package com.example.demo.dto.submission;

import java.time.LocalDateTime;

public record SubmissionResponseDto(
        String id,
        String taskId,
        String userId,
        String sourceCodeFileId,
        String logs,
        String status,
        Integer score,
        LocalDateTime createdAt
) {}
