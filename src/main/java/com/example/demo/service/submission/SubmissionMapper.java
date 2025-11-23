package com.example.demo.service.submission;

import com.example.demo.dto.submission.SubmissionResponseDto;
import com.example.demo.model.submission.SubmissionEntity;
import org.springframework.stereotype.Service;

@Service
public class SubmissionMapper {

    public SubmissionResponseDto toDto(SubmissionEntity submission) {
        return new SubmissionResponseDto(
                submission.getId(),
                submission.getTaskId(),
                submission.getUserId(),
                submission.getSourceCodeFileId(),
                submission.getLogs(),
                submission.getStatus() != null ? submission.getStatus().name() : null,
                submission.getScore(),
                submission.getCreatedAt()
        );
    }

}
