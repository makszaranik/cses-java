package com.example.demo.service.executor.stage;

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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@Component("linter")
@RequiredArgsConstructor
public class LinterStageExecutor implements StageExecutor {

    private final TaskService taskService;
    private final DockerClientFacade dockerClientFacade;
    private final SubmissionService submissionService;

    @Value("${spring.docker-client.scripts.linter}")
    private String linterCommand;

    @Value("${spring.docker-client.containers.linter}")
    private String containerName;

    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {
        log.debug("Linter stage for submission {}.", submission.getId());
        TaskEntity task = taskService.findTaskById(submission.getTaskId());
        String lintersFileId = task.getLintersFileId();
        Long memoryRestriction = task.getMemoryRestriction();

        String downloadPath = "http://host.docker.internal:8080/files/download/%s";
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());
        String linterUri = String.format(downloadPath, lintersFileId);

        String hostReportsDir = "/tmp/linter-results/" + submission.getId();
        String containerReportsDir = "/app/solution_dir";

        String cmd = String.format(linterCommand, solutionUri, linterUri);

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJobWithVolume(
                containerName,
                hostReportsDir,
                containerReportsDir,
                memoryRestriction,
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        String logs = jobResult.logs();
        Integer pmdScore = isPmdPassed(hostReportsDir) ? task.getLintersPoints() : 0;
        submission.setScore(submission.getScore() + pmdScore);
        submission.setLogs(logs);

        log.debug("Status code is {}", statusCode);
        log.debug("Score is {}", pmdScore);

        if (statusCode == 0) {
            submission.setStatus(SubmissionEntity.Status.LINTER_PASSED);
            submissionService.save(submission);
            chain.doNext(submission, chain);
        } else {
            submission.setStatus(SubmissionEntity.Status.LINTER_FAILED);
            submissionService.save(submission);
        }
    }

    @SneakyThrows
    public boolean isPmdPassed(String pathToDir) {
        String[] content = {""};
        Files.walkFileTree(Path.of(pathToDir), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals("pmd.xml")) {
                    content[0] = Files.readString(file);
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return !content[0].contains("<violation");
    }

}
