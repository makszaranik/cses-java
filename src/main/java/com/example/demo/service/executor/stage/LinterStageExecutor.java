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
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        DockerConfig.DockerClientProperties.Path path = properties.stages().linter().path();
        String hostReportsDir = path.host() + submission.getId();
        String containerReportsDir = path.container();

        String cmd = String.format(properties.stages().linter().script(), solutionUri, linterUri);

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJobWithVolume(
                properties.stages().linter().containerName(),
                hostReportsDir,
                containerReportsDir,
                memoryRestriction,
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        String pmdReportContent = getPmdReport(hostReportsDir)
                .orElseThrow(() -> new IllegalStateException("No PMD report found for submission " + submission.getId()));

        int pmdScore = isPmdPassed(pmdReportContent) ? task.getLintersPoints() : 0;

        submission.setScore(submission.getScore() + pmdScore);
        submission.getLogs().put(SubmissionEntity.LogType.LINTER, pmdReportContent);

        log.debug("Status code is {}", statusCode);
        log.debug("Score is {}", pmdScore);

        ContainerStatusCode containerStatus = ContainerStatusCode.resolve(statusCode);
        SubmissionEntity.Status submissionStatus = switch (containerStatus) {
            case CONTAINER_SUCCESS -> SubmissionEntity.Status.LINTER_PASSED;
            case CONTAINER_OUT_OF_MEMORY -> SubmissionEntity.Status.OUT_OF_MEMORY_ERROR;
            case CONTAINER_TIME_LIMIT -> SubmissionEntity.Status.TIME_LIMIT_EXCEEDED;
            case CONTAINER_FAILED -> SubmissionEntity.Status.LINTER_FAILED;
        };

        submission.setStatus(submissionStatus);
        submissionService.save(submission);

        chain.doNext(submission, chain);

    }

    public boolean isPmdPassed(String pmdReport) {
        return !pmdReport.contains("<violation");
    }

    @SneakyThrows
    public Optional<String> getPmdReport(String pathToDir) {
        Path path = new File(pathToDir).toPath();

        if (!Files.exists(path)) { //empty if report doesnt exists
            return Optional.empty();
        }

        List<String> report = new ArrayList<>();
        Files.walkFileTree(Path.of(pathToDir), new SimpleFileVisitor<>() {
            @NonNull
            @Override
            public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals("pmd.xml")) {
                    report.add(Files.readString(file));
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return report.isEmpty() ? Optional.empty() : Optional.of(report.getFirst());
    }
}
