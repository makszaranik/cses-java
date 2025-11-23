package com.example.demo.service.docker;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ContainerStatusCode {

    CONTAINER_SUCCESS(0),
    CONTAINER_TIME_LIMIT(124),
    CONTAINER_OUT_OF_MEMORY(137),
    CONTAINER_FAILED(1);

    private final int statusCode;

    ContainerStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public static ContainerStatusCode resolve(Integer statusCode) {
        return Arrays.stream(ContainerStatusCode.values())
                .filter(v -> v.getStatusCode() == statusCode)
                .findFirst()
                .orElse(CONTAINER_FAILED);
    }

}
