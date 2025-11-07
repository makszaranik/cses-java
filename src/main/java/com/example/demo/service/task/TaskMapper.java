package com.example.demo.service.task;

import com.example.demo.dto.task.TaskCreateRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.dto.task.TaskUpdateRequestDto;
import com.example.demo.model.task.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskEntity toEntity(TaskCreateRequestDto createDto, String ownerId) {
        return TaskEntity.builder()
                .title(createDto.title())
                .statement(createDto.statement())
                .memoryRestriction(createDto.memoryRestriction())
                .solutionTemplateFileId(createDto.solutionTemplateFileId())
                .testsFileId(createDto.testsFileId())
                .lintersFileId(createDto.lintersFileId())
                .testsPoints(createDto.testsPoints())
                .lintersPoints(createDto.lintersPoints())
                .ownerId(ownerId)
                .build();
    }

    public TaskEntity toEntity(TaskUpdateRequestDto createDto, String ownerId) {
        return TaskEntity.builder()
                .title(createDto.title())
                .statement(createDto.statement())
                .memoryRestriction(createDto.memoryRestriction())
                .solutionTemplateFileId(createDto.solutionTemplateFileId())
                .testsFileId(createDto.testsFileId())
                .lintersFileId(createDto.lintersFileId())
                .testsPoints(createDto.testsPoints())
                .lintersPoints(createDto.lintersPoints())
                .ownerId(ownerId)
                .build();
    }

    public TaskResponseDto toResponseDto(TaskEntity entity) {
        return new TaskResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getStatement(),
                entity.getMemoryRestriction(),
                entity.getSolutionTemplateFileId(),
                entity.getTestsPoints(),
                entity.getLintersPoints(),
                entity.getSubmissionsNumberLimit(),
                entity.getOwnerId()
        );
    }
}
