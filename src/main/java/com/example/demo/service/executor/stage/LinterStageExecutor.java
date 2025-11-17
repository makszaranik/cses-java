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


    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {
        log.info("Linter stage for submission {}.", submission.getId());
        TaskEntity task = taskService.findTaskById(submission.getTaskId());
        String lintersFileId = task.getLintersFileId();

        String downloadPath = "http://host.docker.internal:8080/files/download/%s";
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());
        String linterUri = String.format(downloadPath, lintersFileId);

        String hostReportsDir = "/tmp/linter-results/" + submission.getId();
        String containerReportsDir = "/app/solution_dir";

        String cmd = String.format(
                linterDockerCommand(solutionUri, linterUri),
                solutionUri,
                linterUri
        );

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJobWithVolume(
                "linter_container",
                hostReportsDir,
                containerReportsDir,
                "/bin/bash", "-c",
                cmd
        );

        Integer statusCode = jobResult.statusCode();
        String logs = jobResult.logs();

        SubmissionEntity.Status status = determineStatus(statusCode);
        boolean pmdPassed = isPmdPassed(hostReportsDir);
        Integer pmdScore = (pmdPassed) ? task.getLintersPoints() : 0;

        if (pmdPassed) {
            Integer score = submission.getScore();
            submission.setScore(score + pmdScore);
        }

        log.debug("Status code is {}", statusCode);
        log.debug("Score is {}", pmdScore);

        log.info("Status code is {}", statusCode);
        submission.setStatus(status);
        submission.setLogs(logs);

        submissionService.save(submission);
        chain.doNext(submission, chain);
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

    private SubmissionEntity.Status determineStatus(Integer statusCode) {
        if (statusCode == 0) {
            return SubmissionEntity.Status.LINTER_PASSED;
        } else {
            return SubmissionEntity.Status.LINTER_FAILED;
        }
    }

    private String linterDockerCommand(String solutionUri, String linterUri) {
        return String.format("""
                wget -O solution.zip %s && unzip solution.zip -d solution_dir &&
                wget -O linter.zip %s && unzip linter.zip -d linter_dir &&
                SOLUTION_DIR_NAME=$(find solution_dir -mindepth 1 -maxdepth 1 -type d | head -n 1) &&
                mv linter_dir/* $SOLUTION_DIR_NAME/src/main/resources &&
                cd $SOLUTION_DIR_NAME && mvn pmd:check -q
                """, solutionUri, linterUri);
    }
}
