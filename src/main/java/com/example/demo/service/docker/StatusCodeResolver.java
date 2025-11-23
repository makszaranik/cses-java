package com.example.demo.service.docker;

import lombok.Getter;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.Arrays;

@Getter
public enum StatusCodeResolver {

    CONTAINER_SUCCESS(0),
    CONTAINER_TIME_LIMIT(124),
    CONTAINER_OUT_OF_MEMORY(137),
    CONTAINER_FAILED(1);

    private final int statusCode;

    StatusCodeResolver(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public static StatusCodeResolver resolve(Integer statusCode) {
        return Arrays.stream(StatusCodeResolver.values())
                .filter(v -> v.getStatusCode() == statusCode)
                .findFirst()
                .orElse(CONTAINER_FAILED);
    }

}
