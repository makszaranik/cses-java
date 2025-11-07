package com.example.demo.service.executor.facade;

import com.example.demo.model.submission.SubmissionEntity;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerClientFacade {

    private final DockerClient dockerClient;
    private final ContainerExecutorFacade containerFacade;
    private final ContainerLogFacade logFacade;

    public DockerJobResult runJob(String containerName, String... args) {
        String containerId = null;
        try {
            containerId = containerFacade.createAndStart(containerName, args);
            int status = getStatusCode(containerId);
            String logs = logFacade.collect(containerId);
            return new DockerJobResult(status, logs);

        } catch (Exception e) {
            log.error("Error running container: {}", e.getMessage(), e);
            return new DockerJobResult(-1, e.getMessage());

        } finally {
            if (containerId != null) containerFacade.remove(containerId);
        }
    }

    public int getStatusCode(String containerId) {
        return dockerClient.waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode(60, TimeUnit.SECONDS);
    }

    public record DockerJobResult(Integer status, String logs) {}


}
