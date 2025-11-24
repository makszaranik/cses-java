package com.example.demo.dto.submission;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.executor.stage.StageExecutor;

import java.time.LocalDateTime;
import java.util.Map;

public record SubmissionResponseDto(
        String id,
        String taskId,
        String userId,
        String sourceCodeFileId,
        Map<StageExecutor.Stages, String> logs,
        SubmissionEntity.Status status,
        Integer score,
        LocalDateTime createdAt
) {}
