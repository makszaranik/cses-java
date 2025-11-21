package com.example.demo.service.executor.stage;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.model.task.TaskEntity;
import com.example.demo.service.docker.DockerClientFacade;
import com.example.demo.service.submission.SubmissionService;
import com.example.demo.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("build")
@RequiredArgsConstructor
public class BuildStageExecutor implements StageExecutor {

    private final DockerClientFacade dockerClientFacade;
    private final SubmissionService submissionService;
    private final TaskService taskService;

    @Override
    public void execute(SubmissionEntity submission, StageExecutorChain chain) {

        log.info("Build stage for submission {}.", submission.getId());
        TaskEntity task = taskService.findTaskById(submission.getTaskId());
        Long memoryRestriction = task.getMemoryRestriction();

        String downloadPath = "http://host.docker.internal:8080/files/download/%s";
        String solutionUri = String.format(downloadPath, submission.getSourceCodeFileId());

        String cmd = String.format("""
                wget -O solution.zip %s && unzip solution.zip -d solution_dir &&
                SOLUTION_DIR_NAME=$(find solution_dir -mindepth 1 -maxdepth 1 -type d | head -n 1) &&
                cd $SOLUTION_DIR_NAME && mvn clean compile -q
                """, solutionUri
        );

        DockerClientFacade.DockerJobResult jobResult = dockerClientFacade.runJob(
                "build_container",
                memoryRestriction,
                "/bin/bash", "-c", cmd
        );

        Integer statusCode = jobResult.statusCode();
        String logs = jobResult.logs();
        submission.setLogs(logs);

        log.debug("Status code is {}", statusCode);

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