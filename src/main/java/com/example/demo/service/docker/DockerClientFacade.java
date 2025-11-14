package com.example.demo.service.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
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

    public DockerJobResult runJob(String containerName, String... args) {
        String containerId = null;
        try {
            containerId = createAndStartContainer(containerName, args);
            log.debug("container with id {} running.", containerId);
            int status = getStatusCode(containerId);
            String logs = collectLogs(containerId);
            log.debug("container with id {} finished with status code {}.", containerId, status);
            return new DockerJobResult(status, logs);

        } catch (Exception e) {
            log.error("Error running container: {}", e.getMessage(), e);
            return new DockerJobResult(-1, e.getMessage());

        } finally {
            if (containerId != null) removeContainer(containerId);
        }
    }

    public DockerJobResult runJobWithVolume(String containerName, String hostDir, String containerDir, String... args) {
        String containerId = null;
        boolean mkdir = new File(hostDir).mkdirs();
        try {
            var container = dockerClient.createContainerCmd("java-maven-ci")
                    .withCmd(args)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withNetworkMode("demo_default")
                            .withBinds(new Bind(hostDir, new Volume(containerDir))))
                    .withTty(true)
                    .withName(containerName)
                    .exec();

            containerId = container.getId();
            dockerClient.startContainerCmd(containerId).exec();
            log.debug("container with id {} running with volume.", containerId);

            int status = getStatusCode(containerId);
            String logs = collectLogs(containerId);
            log.debug("container with id {} finished with status code {}.", containerId, status);

            return new DockerJobResult(status, logs);

        } catch (Exception e) {
            log.error("Error running container with volume: {}", e.getMessage(), e);
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
                        logs.append(line).append(System.lineSeparator());
                    }
                })
                .awaitCompletion(60, TimeUnit.SECONDS); //await logs for 60 sec

        return logs.toString();
    }

    public record DockerJobResult(Integer statusCode, String logs) {}


}
