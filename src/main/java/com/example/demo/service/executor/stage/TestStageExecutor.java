package com.example.demo.service.executor.stage;

import com.example.demo.config.DockerConfig;
import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.docker.DockerClientFacade;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
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

        String hostReportsDir = "/tmp/test-results/" + submission.getId();
        String containerReportsDir = "/app/solution_dir";

        String cmd = String.format(properties.scripts().test(), solutionUri, testUri);

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJobWithVolume(
                properties.containers().test(),
                hostReportsDir,
                containerReportsDir,
                memoryRestriction,
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        String logs = jobResult.logs();
        TestsResult testsResult = getTestResult(hostReportsDir);
        Integer score = calculateScore(testsResult.passed(), testsResult.total(), task.getTestsPoints());

        submission.setLogs(logs);
        submission.setScore(submission.getScore() + score);

        log.debug("Status code is {}", statusCode);
        log.debug("Score is {}", score);

        if (statusCode == 0) {
            submission.setStatus(SubmissionEntity.Status.ACCEPTED);
            submissionService.save(submission);
            chain.doNext(submission, chain);
        } else {
            submission.setStatus(SubmissionEntity.Status.WRONG_ANSWER);
            submissionService.save(submission);
        }
    }

    @SneakyThrows
    private TestsResult getTestResult(String pathToFile) {
        Path path = new File(pathToFile).toPath();
        AtomicReference<TestsResult> result = new AtomicReference<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @NonNull
            @Override
            public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".txt")) {
                    String content = Files.readString(file);
                    Arrays.stream(content.split("\n"))
                            .filter(line -> line.contains("Tests run:"))
                            .findFirst()
                            .ifPresent(line -> result.set(getTestResultParams(line)));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result.get();
    }

    private TestsResult getTestResultParams(String line) {
        String[] parts = line.split(", ");
        int totalTests = Integer.parseInt(parts[0].split(":")[1].trim());
        int failures = Integer.parseInt(parts[1].split(":")[1].trim());
        int errors = Integer.parseInt(parts[2].split(":")[1].trim());
        int skipped = Integer.parseInt(parts[3].split(":")[1].trim());
        int timeout = 0; //optional field
        if(line.contains("timeout")){
            timeout = Integer.parseInt(parts[4].split(":")[1].trim());
        }
        int passedTests = totalTests - failures - errors - skipped - timeout;
        return new TestsResult(totalTests, failures, errors, skipped, timeout, passedTests);
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
    ) {}
}