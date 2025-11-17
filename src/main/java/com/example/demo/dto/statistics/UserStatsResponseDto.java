package com.example.demo.dto.statistics;

import java.util.List;

public record UserStatsResponseDto(
        String userId,
        List<TaskStatsRecordDto> tasks
) {}
