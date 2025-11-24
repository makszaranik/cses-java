package com.example.demo.service.executor.stage;

import com.example.demo.config.DockerConfig;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.docker.DockerClientFacade;
import com.example.demo.service.docker.ContainerStatusCode;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component("linter")
@RequiredArgsConstructor
public class LinterStageExecutor implements StageExecutor {

    private final TaskService taskService;
    private final DockerClientFacade dockerClientFacade;
    private final SubmissionService submissionService;
    private final DockerConfig.DockerClientProperties properties;

    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {
        log.debug("Linter stage for submission {}.", submission.getId());
        TaskEntity task = taskService.findTaskById(submission.getTaskId());
        String lintersFileId = task.getLintersFileId();
        Long memoryRestriction = task.getMemoryRestriction();

        String downloadPath = properties.container().downloadUriTemplate();
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());
        String linterUri = String.format(downloadPath, lintersFileId);

        String hostReportsDir = "/tmp/linter-results/" + submission.getId();
        String containerReportsDir = "/app/solution_dir";

        String cmd = String.format(properties.scripts().linter(), solutionUri, linterUri);

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJobWithVolume(
                properties.containers().linter(),
                hostReportsDir,
                containerReportsDir,
                memoryRestriction,
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        Integer pmdScore = isPmdPassed(hostReportsDir) ? task.getLintersPoints() : 0;
        submission.setScore(submission.getScore() + pmdScore);
        submission.getLogs().put(Stages.LINTER, jobResult.logs());

        log.debug("Status code is {}", statusCode);
        log.debug("Score is {}", pmdScore);

        ContainerStatusCode containerStatus = ContainerStatusCode.resolve(statusCode);
        SubmissionEntity.Status submissionStatus = switch (containerStatus) {
            case CONTAINER_SUCCESS -> SubmissionEntity.Status.LINTER_PASSED;
            case CONTAINER_OUT_OF_MEMORY -> SubmissionEntity.Status.OUT_OF_MEMORY_ERROR;
            case CONTAINER_TIME_LIMIT -> SubmissionEntity.Status.TIME_LIMIT_EXCEEDED;
            case CONTAINER_FAILED ->  SubmissionEntity.Status.LINTER_FAILED;
        };

        submission.setStatus(submissionStatus);
        submissionService.save(submission);

        chain.doNext(submission, chain);

    }

    @SneakyThrows
    public boolean isPmdPassed(String pathToDir) {
        AtomicReference<String> result = new AtomicReference<>();
        Files.walkFileTree(Path.of(pathToDir), new SimpleFileVisitor<>() {
            @NonNull
            @Override
            public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals("pmd.xml")) {
                    result.set(Files.readString(file));
                    log.info(result.toString());
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return !result.get().contains("<violation");
    }
}
