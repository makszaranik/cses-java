package com.example.demo.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.dockerjava.okhttp.OkDockerHttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(DockerConfig.DockerClientProperties.class)
public class DockerConfig {

    @Bean
    public DefaultDockerClientConfig defaultDockerClientConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();
    }

    @Bean
    public DockerHttpClient dockerHttpClient(DefaultDockerClientConfig config) {
        return new OkDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
    }

    @Bean
    public DockerClient dockerClient(DefaultDockerClientConfig config, DockerHttpClient httpClient) {
        return DockerClientImpl.getInstance(config, httpClient);
    }

    @ConfigurationProperties(prefix = "spring.docker-client")
    public record DockerClientProperties(
            Container container,
            Containers containers,
            Scripts scripts
    ) {

        public record Container(
                String imageName,
                String hostName,
                String downloadUriTemplate,
                Duration timeout
        ) {}

        public record Containers(
                String build,
                String test,
                String linter
        ) {}

        public record Scripts(
                String build,
                String test,
                String linter
        ) {}
    }
}
