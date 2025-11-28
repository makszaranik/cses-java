package com.example.demo.service.executor.stage;

import com.example.demo.model.submission.SubmissionEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("chain")
@RequiredArgsConstructor
public class StageExecutorChain {

    private final BuildStageExecutor buildStageExecutor;
    private final TestStageExecutor testStageExecutor;
    private final LinterStageExecutor linterStageExecutor;

    private List<StageExecutor> stages;
    private final ThreadLocal<Integer> currentStageIndex = ThreadLocal.withInitial(() -> 0);

    @PostConstruct
    private void initExecutorChain() {
        stages = List.of(
                buildStageExecutor,
                linterStageExecutor,
                testStageExecutor
        );
    }

    public void doNext(SubmissionEntity submission, StageExecutorChain chain) {
        if (currentStageIndex.get() < stages.size()) {
            StageExecutor currentStage = stages.get(currentStageIndex.get());
            currentStageIndex.set(currentStageIndex.get() + 1); //next stage in chain
            currentStage.execute(submission, chain);
        } else {
            log.debug("All stages completed.");
            currentStageIndex.set(0);
        }
    }

    public void startChain(SubmissionEntity submission) {
        this.currentStageIndex.set(0);
        this.doNext(submission, this);
    }

}
