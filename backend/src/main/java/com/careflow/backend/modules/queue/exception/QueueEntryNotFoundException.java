package com.careflow.backend.modules.queue.exception;

public class QueueEntryNotFoundException extends RuntimeException {

    public QueueEntryNotFoundException(String message) {
        super(message);
    }
}