package com.example.demo.dto.statistics;

import java.time.LocalDate;
import java.util.Map;

public record TaskStatsRecordDto(
        String taskName,
        int accepted,
        int wrong,
        int runtimeError,
        Map<LocalDate, Integer> submissionsPerDay
) {}
