package com.example.demo.dto.task;


public record TaskResponseDto(
        String id,
        String title,
        String statement,
        Long memoryRestriction,
        String solutionTemplateFileId,
        int testsPoints,
        int lintersPoints,
        int submissionsNumberLimit,
        String ownerId
) {}
