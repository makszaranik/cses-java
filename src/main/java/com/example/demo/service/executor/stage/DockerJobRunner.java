package com.example.demo.service.executor.stage;

import com.example.demo.model.submission.SubmissionEntity;
import com.example.demo.service.submission.SubmissionService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


@Slf4j
@RequiredArgsConstructor
public abstract class DockerJobRunner {

    protected final DockerClient dockerClient;

    public JobResult runJob(String containerName, SubmissionEntity submission, String... args) {
        Integer statusCode;
        String containerLogs = "";
        String containerId = "";

        try {
            CreateContainerResponse container = dockerClient.createContainerCmd("java-maven-ci")
                    .withCmd(args)
                    .withHostConfig(HostConfig.newHostConfig().withNetworkMode("demo_default"))
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .withName(containerName)
                    .exec();

            containerId = container.getId();
            dockerClient.startContainerCmd(containerId).exec();

            log.info("container with id {} running.", containerId);

            statusCode = dockerClient
                    .waitContainerCmd(containerId)
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(60, TimeUnit.SECONDS); //await build for 60 sec

            log.info("container with id {} finished.", containerId);

        } catch (Exception e) {
            statusCode = -1;
            log.error(e.getMessage(), e);

        } finally {
            containerLogs = collectLogs(containerId, submission);
            removeContainer(containerId);
        }
        return new JobResult(statusCode, containerLogs);
    }

    @SneakyThrows
    private String collectLogs(String containerId, SubmissionEntity submission) {
        StringBuilder logs = new StringBuilder();
        dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(false)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        String line = new String(frame.getPayload(), StandardCharsets.UTF_8).trim();
                        logs.append(line).append(System.lineSeparator());
                    }
                })
                .awaitCompletion(60, TimeUnit.SECONDS); //await logs for 60 sec

        log.info("container {} logs", logs);
        return logs.toString();
    }

    private void removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId)
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
    }

    public record JobResult(Integer statusCode, String logs){}

}