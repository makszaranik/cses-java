package com.example.demo.service.executor.stage;

import com.example.demo.config.DockerConfig;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.docker.DockerClientFacade;
import com.example.demo.service.docker.ContainerStatusCode;
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
        submission.getLogs().put(SubmissionEntity.LogType.BUILD, jobResult.logs());

        log.debug("Status code is {}", statusCode);
        ContainerStatusCode containerStatus = ContainerStatusCode.resolve(statusCode);

        SubmissionEntity.Status submissionStatus = switch (containerStatus) {
            case CONTAINER_SUCCESS -> SubmissionEntity.Status.COMPILATION_SUCCESS;
            case CONTAINER_TIME_LIMIT -> SubmissionEntity.Status.TIME_LIMIT_EXCEEDED;
            case CONTAINER_OUT_OF_MEMORY -> SubmissionEntity.Status.OUT_OF_MEMORY_ERROR;
            case CONTAINER_FAILED -> SubmissionEntity.Status.TIME_LIMIT_EXCEEDED;
        };

        submission.setStatus(submissionStatus);
        submissionService.save(submission);

        if (submissionStatus == Status.COMPILATION_SUCCESS) {
            chain.doNext(submission, chain);
        }
    }
}