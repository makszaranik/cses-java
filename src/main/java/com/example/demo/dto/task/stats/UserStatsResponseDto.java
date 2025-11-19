package com.example.demo.dto.task.stats;

import java.util.List;

public record UserStatsResponseDto(
        String userId,
        List<TaskStatsResponseDto> tasks
) {}
