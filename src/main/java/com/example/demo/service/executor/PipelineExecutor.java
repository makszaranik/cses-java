package com.example.demo.service.executor;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.executor.stage.StageExecutorChain;
import com.example.demo.service.submission.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PipelineExecutor {

    private final SubmissionService submissionService;
    private final StageExecutorChain chain;

    @Scheduled(fixedRate = 5000)
    void execute() {
        submissionService.getAllSubmitted().forEach(submitted -> {
            SubmissionEntity submission = submissionService.findSubmissionById(submitted.getId());
            submission.setStatus(SubmissionEntity.Status.COMPILING);
            submissionService.save(submission);
            chain.startChain(submission);
        });
    }
}