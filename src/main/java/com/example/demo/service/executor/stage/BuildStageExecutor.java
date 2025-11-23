package com.example.demo.service.executor.stage;

import com.example.demo.config.DockerConfig;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.docker.DockerClientFacade;
import com.example.demo.service.docker.StatusCodeResolver;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.example.demo.model.submission.SubmissionEntity.*;

@Slf4j
@Component("build")
@RequiredArgsConstructor
public class BuildStageExecutor implements StageExecutor {

    private final DockerClientFacade dockerClientFacade;
    private final SubmissionService submissionService;
    private final TaskService taskService;
    private final DockerConfig.DockerClientProperties properties;

    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {

        log.debug("Build stage for submission {}.", submission.getId());
        TaskEntity task = taskService.findTaskById(submission.getTaskId());
        Long memoryRestriction = task.getMemoryRestriction();

        String downloadPath = properties.container().downloadUriTemplate();
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());

        String cmd = String.format(properties.scripts().build(), solutionUri);

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJob(
                properties.containers().build(),
                memoryRestriction,
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        submission.setLogs(jobResult.logs());

        log.info("Status code is {}", statusCode);
        StatusCodeResolver containerStatus = StatusCodeResolver.resolve(statusCode);

        SubmissionEntity.Status submissionStatus = switch (containerStatus) {
            case CONTAINER_SUCCESS -> Status.COMPILATION_SUCCESS;
            case CONTAINER_FAILED,
                 CONTAINER_TIME_LIMIT,
                 CONTAINER_OUT_OF_MEMORY -> Status.COMPILATION_ERROR;
        };

        submission.setStatus(submissionStatus);
        submissionService.save(submission);

        if (submissionStatus == Status.COMPILATION_SUCCESS) {
            chain.doNext(submission, chain);
        }
    }
}