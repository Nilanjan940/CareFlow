package com.careflow.backend.modules.queue.exception;

public class InvalidQueueStatusException extends RuntimeException {

    public InvalidQueueStatusException(String message) {
        super(message);
    }
}