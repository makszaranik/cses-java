package com.example.demo.service.executor.stage;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.executor.facade.DockerClientFacade;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component("test")
@RequiredArgsConstructor
public class TestStageExecutor implements StageExecutor {

    private final SubmissionService submissionService;
    private final TaskService taskService;
    private final DockerClientFacade dockerClientFacade;

    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {

        log.debug("Test stage for submission {}.", submission.getId());

        TaskEntity task = taskService.findTaskById(submission.getTaskId());
        String testsFileId = task.getTestsFileId();

        String downloadPath = "http://host.docker.internal:8000/api/files/download/%s";
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());
        String testUri = String.format(downloadPath, testsFileId);

        String cmd = String.format(
                "wget -O solution.zip %s && unzip solution.zip" +
                        " && wget -O test.zip %s && unzip test.zip" +
                        " && mv test solution/src/test" +
                        " && cd solution && mvn clean test -q",
                solutionUri, testUri
        );

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJob(
                "test_container",
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.status();
        String logs = jobResult.logs();
        submission.setLogs(logs);
        submissionService.save(submission);

        if (statusCode == 0) {
            submission.setStatus(SubmissionEntity.Status.ACCEPTED);
            chain.doNext(submission, chain);
        } else {
            submission.setStatus(SubmissionEntity.Status.WRONG_ANSWER);
        }
    }
}

