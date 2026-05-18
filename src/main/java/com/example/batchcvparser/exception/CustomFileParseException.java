package com.example.batchcvparser.exception;

public class CustomFileParseException extends RuntimeException {
    public CustomFileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
