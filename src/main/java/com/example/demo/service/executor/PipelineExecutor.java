package com.example.demo.service.executor;

import com.example.demo.config.RabbitConfig;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.executor.stage.StageExecutorChain;
import com.example.demo.service.submission.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PipelineExecutor {

    private final SubmissionService submissionService;
    private final StageExecutorChain chain;

    @RabbitListener(queues = RabbitConfig.SUBMISSION_QUEUE)
    void execute(String submissionId) {
        SubmissionEntity submission = submissionService.findSubmissionById(submissionId);
        if(submission.getStatus() == SubmissionEntity.Status.SUBMITTED){
            chain.startChain(submission);
        }
    }
}