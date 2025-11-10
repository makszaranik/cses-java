package com.example.demo.service.executor.stage;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.executor.facade.DockerClientFacade;
import com.example.demo.service.submission.SubmissionService;
import com.github.dockerjava.api.DockerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
                "wget -O solution.zip %s && unzip solution.zip -d solution_dir" +
                        " && SOLUTION_DIR_NAME=$(find solution_dir -mindepth 1 -maxdepth 1 -type d | head -n 1)" +
                        " && cd $SOLUTION_DIR_NAME" +
                        " && mvn clean compile -q",
                solutionUri
        );

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJob(
                "build_container",
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        String logs = jobResult.logs();
        submission.setLogs(logs);

        log.info("Status code is {}", statusCode);
        if (statusCode == 0) {
            submission.setStatus(SubmissionEntity.Status.COMPILATION_SUCCESS);
            submissionService.save(submission);
            chain.doNext(submission, chain);
        } else {
            submission.setStatus(SubmissionEntity.Status.COMPILATION_ERROR);
            submissionService.save(submission);
        }
    }
}