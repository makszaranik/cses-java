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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component("test")
@RequiredArgsConstructor
public class TestStageExecutor implements StageExecutor {

    private final TaskService taskService;
    private final DockerClientFacade dockerClientFacade;
    private final SubmissionService submissionService;
    private final DockerConfig.DockerClientProperties properties;

    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {
        log.debug("Test stage for submission {}.", submission.getId());

        TaskEntity task = taskService.findTaskById(submission.getTaskId());
        String testsFileId = task.getTestsFileId();
        Long memoryRestriction = task.getMemoryRestriction();

        String downloadPath = properties.container().downloadUriTemplate();
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());
        String testUri = String.format(downloadPath, testsFileId);

        DockerConfig.DockerClientProperties.Path path = properties.stages().test().path();
        String hostReportsDir = path.host() + submission.getId();
        String containerReportsDir = path.container();

        String cmd = String.format(properties.stages().test().script(), solutionUri, testUri);

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJobWithVolume(
                properties.stages().test().containerName(),
                hostReportsDir,
                containerReportsDir,
                memoryRestriction,
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        ContainerStatusCode containerStatus = ContainerStatusCode.resolve(statusCode);
        Optional<TestsResult> testsResult = getTestResult(hostReportsDir);

        int score = 0;
        if (testsResult.isPresent()) {
            int passedTests = testsResult.get().passed();
            int totalTests = testsResult.get().total();
            score = calculateScore(passedTests, totalTests, task.getTestsPoints());
        }

        submission.getLogs().put(SubmissionEntity.LogType.TEST, jobResult.logs());
        submission.setScore(submission.getScore() + score);

        log.debug("Status code is {}", statusCode);
        log.debug("Score is {}", score);

        SubmissionEntity.Status submissionStatus = switch (containerStatus) {
            case CONTAINER_SUCCESS -> SubmissionEntity.Status.ACCEPTED;
            case CONTAINER_TIME_LIMIT -> SubmissionEntity.Status.TIME_LIMIT_EXCEEDED;
            case CONTAINER_OUT_OF_MEMORY -> SubmissionEntity.Status.OUT_OF_MEMORY_ERROR;
            case CONTAINER_FAILED -> SubmissionEntity.Status.WRONG_ANSWER;
        };

        submission.setStatus(submissionStatus);
        submissionService.save(submission);

        if (submissionStatus == SubmissionEntity.Status.ACCEPTED) {
            chain.doNext(submission, chain);
        }
    }

    @SneakyThrows
    private Optional<TestsResult> getTestResult(String pathToFile) {
        Path path = new File(pathToFile).toPath();

        if (!Files.exists(path)) {   //empty if report doesnt exists
            return Optional.empty();
        }

        List<String> report = new ArrayList<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @NonNull
            @Override
            public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".txt")) {
                    String content = Files.readString(file);
                    Arrays.stream(content.split("\n"))
                            .filter(line -> line.contains("Tests run:"))
                            .findFirst().ifPresent(report::add);
                }
                return report.isEmpty() ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
            }
        });
        return report.isEmpty() ? Optional.empty() : Optional.of(getTestResultParams(report.getFirst()));
    }

    private TestsResult getTestResultParams(String line) {
        String[] parts = line.split(", ");
        int totalTests = getValueFromPart(parts[0]);
        int failures = getValueFromPart(parts[1]);
        int errors = getValueFromPart(parts[2]);
        int skipped = getValueFromPart(parts[3]);
        int timeout = line.contains("timeout") ? getValueFromPart(parts[4]) : 0;
        int passedTests = totalTests - failures - errors - skipped - timeout;
        return new TestsResult(totalTests, failures, errors, skipped, timeout, passedTests);
    }

    private int getValueFromPart(String part) {
        return Integer.parseInt(part.split(":")[1].trim());
    }

    private Integer calculateScore(int passed, int total, int points) {
        return passed == 0 ? 0 : (passed / total) * points;
    }

    private record TestsResult(
            Integer total,
            Integer failures,
            Integer errors,
            Integer skipped,
            Integer timeout,
            Integer passed
    ) {
    }
}