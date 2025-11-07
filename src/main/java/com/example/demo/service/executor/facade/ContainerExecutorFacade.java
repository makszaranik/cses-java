package com.example.demo.service.executor.facade;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContainerExecutorFacade {
    private final DockerClient dockerClient;

    public String createAndStart(String name, String... args) {
        var container = dockerClient.createContainerCmd("java-maven-ci")
                .withCmd(args)
                .withHostConfig(HostConfig.newHostConfig().withNetworkMode("demo_default"))
                .withTty(true)
                .withName(name)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        return container.getId();
    }

    public void remove(String id) {
        dockerClient.removeContainerCmd(id)
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
    }
}
