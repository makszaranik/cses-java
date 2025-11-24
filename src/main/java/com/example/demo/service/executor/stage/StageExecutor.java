package com.example.demo.service.executor.stage;

import com.example.demo.model.submission.SubmissionEntity;

public interface StageExecutor {

    void execute(SubmissionEntity submission, StageExecutorChain chain);

    enum Stages {
        BUILD,
        TEST,
        LINTER
    }
}
