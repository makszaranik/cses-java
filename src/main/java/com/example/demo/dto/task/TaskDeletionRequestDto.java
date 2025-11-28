package com.example.demo.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskDeletionRequestDto(
        @NotNull @NotBlank String taskId
)
{}