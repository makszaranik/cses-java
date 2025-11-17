package com.example.demo.service.executor.stage;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.docker.DockerClientFacade;
import com.example.demo.service.submission.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("build")
@RequiredArgsConstructor
public class BuildStageExecutor implements StageExecutor {

    private final DockerClientFacade dockerClientFacade;
    private final SubmissionService submissionService;

    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {

        log.info("Build stage for submission {}.", submission.getId());
        String downloadPath = "http://host.docker.internal:8080/files/download/%s";
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());

        String cmd = String.format(
                buildDockerCommand(solutionUri),
                solutionUri
        );

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJob(
                "build_container",
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        SubmissionEntity.Status status = determineStatus(statusCode);
        String logs = jobResult.logs();

        log.info("Status code is {}", statusCode);
        submission.setStatus(status);
        submission.setLogs(logs);

        submissionService.save(submission);
        chain.doNext(submission, chain);
    }

    private String buildDockerCommand(String solutionUri) {
        return String.format("""
                wget -O solution.zip %s && unzip solution.zip -d solution_dir &&
                SOLUTION_DIR_NAME=$(find solution_dir -mindepth 1 -maxdepth 1 -type d | head -n 1) &&
                cd $SOLUTION_DIR_NAME && mvn clean compile -q
                """, solutionUri);
    }

    private SubmissionEntity.Status determineStatus(Integer statusCode) {
        return statusCode == 0 ? SubmissionEntity.Status.COMPILATION_SUCCESS
                : SubmissionEntity.Status.COMPILATION_ERROR;
    }


}