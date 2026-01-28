package com.example.url_system.exceptions;

public class ResponseAlreadyBeingProcessed extends RuntimeException {
    public ResponseAlreadyBeingProcessed(String message) {
        super(message);
    }
}
