package com.example.demo.exceptions;

public class RoleChangeNotAllowedException extends RuntimeException {
    public RoleChangeNotAllowedException(String message) {
        super(message);
    }
}
