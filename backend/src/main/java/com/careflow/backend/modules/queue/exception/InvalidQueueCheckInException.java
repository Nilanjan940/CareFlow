package com.careflow.backend.modules.queue.exception;

public class InvalidQueueCheckInException extends RuntimeException {

    public InvalidQueueCheckInException(String message) {
        super(message);
    }
}