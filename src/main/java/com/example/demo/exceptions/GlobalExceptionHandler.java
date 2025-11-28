package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail("Resource with id " + e.getMessage() + " not found");
        return problemDetail;
    }

    @ExceptionHandler(ScoreExceededException.class)
    public ProblemDetail handleScoreExceeded(ScoreExceededException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Invalid Score Sum");
        problemDetail.setDetail(e.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(SubmissionNotAllowedException.class)
    public ProblemDetail handleSubmissionNotAllowed(SubmissionNotAllowedException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("Submission Not Allowed");
        problemDetail.setDetail(e.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(SubmissionNotFoundException.class)
    public ProblemDetail handleSubmissionNotFound(SubmissionNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Submission Not Found");
        problemDetail.setDetail("Submission with id " + e.getMessage() + " not found");
        return problemDetail;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("User Not Found");
        problemDetail.setDetail("User with id " + e.getMessage() + " not found");
        return problemDetail;
    }

    @ExceptionHandler(RoleChangeNotAllowedException.class)
    public ProblemDetail handleRoleChangeNotAllowedException(RoleChangeNotAllowedException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("Role Change Not Allowed");
        problemDetail.setDetail(e.getMessage());
        return problemDetail;
    }
}
