package com.example.demo.exceptions;

public class ScoreExceededException extends RuntimeException {
    public ScoreExceededException(String message) {
        super(message);
    }
}
