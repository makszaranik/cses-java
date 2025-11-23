package com.example.demo.service.task;

import com.example.demo.dto.task.TaskCreateRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.dto.task.TaskUpdateRequestDto;
import com.example.demo.model.task.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskEntity toEntity(TaskCreateRequestDto dto, String ownerId) {
        return TaskEntity.builder()
                .title(dto.title())
                .statement(dto.statement())
                .memoryRestriction(dto.memoryRestriction())
                .solutionTemplateFileId(dto.solutionTemplateFileId())
                .testsFileId(dto.testsFileId())
                .lintersFileId(dto.lintersFileId())
                .testsPoints(dto.testsPoints())
                .lintersPoints(dto.lintersPoints())
                .submissionsNumberLimit(dto.submissionsNumberLimit())
                .ownerId(ownerId)
                .build();
    }

    public TaskEntity toEntity(TaskUpdateRequestDto dto, String ownerId) {
        return TaskEntity.builder()
                .id(dto.taskId())
                .title(dto.title())
                .statement(dto.statement())
                .memoryRestriction(dto.memoryRestriction())
                .solutionTemplateFileId(dto.solutionTemplateFileId())
                .testsFileId(dto.testsFileId())
                .lintersFileId(dto.lintersFileId())
                .testsPoints(dto.testsPoints())
                .lintersPoints(dto.lintersPoints())
                .submissionsNumberLimit(dto.submissionsNumberLimit())
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
                entity.getTestsFileId(),
                entity.getLintersFileId(),
                entity.getTestsPoints(),
                entity.getLintersPoints(),
                entity.getSubmissionsNumberLimit(),
                entity.getOwnerId()
        );
    }
}
