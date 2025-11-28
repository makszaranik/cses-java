package com.example.demo.service.docker;

import com.example.demo.config.DockerConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerClientFacade {

    private final DockerClient dockerClient;
    private final DockerConfig.DockerClientProperties properties;

    public DockerJobResult runJob(String containerName, Long memoryRestriction, String... args) {
        String containerId = null;
        try {
            containerId = createAndRunContainer(containerName, memoryRestriction, args);
            log.debug("container with id {} running.", containerId);
            int statusCode = awaitContainerStatusCode(containerId);
            String logs = collectLogs(containerId);
            log.debug("container with id {} finished with status code {}.", containerId, statusCode);
            return new DockerJobResult(statusCode, logs);

        } catch (InterruptedException e) {
            log.error("container with id {} interrupted.", containerId, e);
            throw new RuntimeException(e);

        } finally {
            if (containerId != null) removeContainer(containerId);
        }
    }

    public DockerJobResult runJobWithVolume(String containerName, String hostDir, String containerDir, Long memoryRestriction, String... args) {
        String containerId = null;
        boolean mkdir = new File(hostDir).mkdirs();
        try {
            containerId = createAndRunContainerWithVolume(containerName, hostDir, containerDir, memoryRestriction, args);
            log.debug("container with id {} running with volume.", containerId);
            int statusCode = awaitContainerStatusCode(containerId);
            String logs = collectLogs(containerId);
            log.debug("container with id {} finished with status code {}.", containerId, statusCode);
            return new DockerJobResult(statusCode, logs);

        } catch (InterruptedException e) {
            log.error("container with id {} interrupted.", containerId, e);
            throw new RuntimeException(e);

        } finally {
            if (containerId != null) removeContainer(containerId);
        }
    }

    public int awaitContainerStatusCode(String containerId) {
        try {
            int exitCode = dockerClient.waitContainerCmd(containerId)
                    .exec(new WaitContainerResultCallback())
                    .awaitStatusCode(properties.container().timeout().getSeconds(), TimeUnit.SECONDS);

            if (isOutOfMemory(containerId)) {
                return ContainerStatusCode.CONTAINER_OUT_OF_MEMORY.getStatusCode();
            }

            return exitCode;

        } catch (DockerClientException e) {
            log.warn("Container {} exceeded time limit or failed to respond.", containerId, e);
            return ContainerStatusCode.CONTAINER_TIME_LIMIT.getStatusCode();
        }
    }

    public Boolean isOutOfMemory(String containerId) {
        return dockerClient.inspectContainerCmd(containerId)
                .exec()
                .getState()
                .getOOMKilled();
    }

    public String createAndRunContainer(String name, Long memoryRestriction, String... args) {
        CreateContainerResponse container = dockerClient.createContainerCmd(properties.container().imageName())
                .withCmd(args)
                .withHostConfig(HostConfig.newHostConfig()
                        .withNetworkMode(properties.container().hostName())
                        .withMemory(memoryRestriction * 1024L * 1024L)) //mb
                .withTty(true)
                .withName(name)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        return container.getId();
    }

    public String createAndRunContainerWithVolume(String containerName, String hostDir, String containerDir, Long memoryRestriction, String... args) {
        CreateContainerResponse container = dockerClient.createContainerCmd(properties.container().imageName())
                .withCmd(args)
                .withHostConfig(HostConfig.newHostConfig()
                        .withNetworkMode(properties.container().hostName())
                        .withBinds(new Bind(hostDir, new Volume(containerDir)))
                        .withMemory(memoryRestriction * 1024L * 1024L)) //mb
                .withTty(true)
                .withName(containerName)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        return container.getId();
    }

    public void removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId)
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
    }

    public String collectLogs(String containerId) throws InterruptedException {
        StringBuilder logs = new StringBuilder();
        dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(false)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        String line = new String(frame.getPayload(), StandardCharsets.UTF_8).trim();
                        logs.append(line).append("\n");
                    }
                })
                .awaitCompletion(properties.container().timeout().getSeconds(), TimeUnit.SECONDS); //await logs

        return logs.toString();
    }

    public record DockerJobResult(Integer statusCode, String logs) {}

}
