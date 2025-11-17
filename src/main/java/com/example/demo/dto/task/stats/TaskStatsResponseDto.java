package com.example.demo.dto.task.stats;

import com.example.demo.repository.SubmissionRepository;

import java.util.List;

public record TaskStatsResponseDto(
        String taskId,
        List<SubmissionRepository.StatusWrapper> statuses
) {}

