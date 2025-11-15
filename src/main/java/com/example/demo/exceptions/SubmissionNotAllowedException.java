package com.example.demo.exceptions;

public class SubmissionNotAllowedException extends RuntimeException {
    public SubmissionNotAllowedException(String message) {
        super(message);
    }
}
