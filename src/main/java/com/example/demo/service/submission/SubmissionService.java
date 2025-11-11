package com.example.demo.service.submission;


import com.example.demo.config.RabbitConfig;
import com.example.demo.dto.task.TaskSubmissionRequestDto;
import com.example.demo.exceptions.SubmissionNotFoundException;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final RabbitTemplate rabbitTemplate;

    public SubmissionEntity createSubmission(TaskSubmissionRequestDto submitDto) {
        SubmissionEntity taskSubmission = SubmissionEntity.builder()
                .taskId(submitDto.taskId())
                .userId("userId")
                .sourceCodeFileId(submitDto.sourceCodeFileId())
                .status(SubmissionEntity.Status.SUBMITTED)
                .logs("")
                .score(null)
                .build();

        SubmissionEntity saved = submissionRepository.save(taskSubmission);
        rabbitTemplate.convertAndSend("",  RabbitConfig.SUBMISSION_QUEUE, saved.getId());
        return saved;
    }


    public List<SubmissionEntity> getAllSubmitted() {
        return submissionRepository.findAllByStatus(SubmissionEntity.Status.SUBMITTED);
    }

    public SubmissionEntity findSubmissionById(String id) {
        return submissionRepository.findSubmissionEntityById(id)
                .orElseThrow(() -> new SubmissionNotFoundException(id));
    }

    public void save(SubmissionEntity submissionEntity) {
        submissionRepository.save(submissionEntity);
    }
}
