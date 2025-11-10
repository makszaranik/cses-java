package com.example.demo.service.executor.facade;

import com.example.demo.model.submission.SubmissionEntity;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerClientFacade {
    private final DockerClient dockerClient;

    public DockerJobResult runJob(String containerName, String... args) {
        String containerId = null;
        try {
            containerId = createAndStartContainer(containerName, args);
            int status = getStatusCode(containerId);
            String logs = collectLogs(containerId);
            return new DockerJobResult(status, logs);

        } catch (Exception e) {
            log.error("Error running container: {}", e.getMessage(), e);
            return new DockerJobResult(-1, e.getMessage());

        } finally {
            if (containerId != null) removeContainer(containerId);
        }
    }

    public int getStatusCode(String containerId) {
        return dockerClient.waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode(60, TimeUnit.SECONDS);
    }

    public String createAndStartContainer(String name, String... args) {
        var container = dockerClient.createContainerCmd("java-maven-ci")
                .withCmd(args)
                .withHostConfig(HostConfig.newHostConfig().withNetworkMode("demo_default"))
                .withTty(true)
                .withName(name)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        return container.getId();
    }

    public void removeContainer(String id) {
        dockerClient.removeContainerCmd(id)
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
    }

    public String collectLogs(String containerId) throws InterruptedException {
        StringBuilder logs = new StringBuilder();
        dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        logs.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                    }
                })
                .awaitCompletion(60, TimeUnit.SECONDS);
        return logs.toString();
    }

    public record DockerJobResult(Integer status, String logs) {}


}
