package org.demo.orderservice.infrastructure.exception;

public class QueueSendException extends RuntimeException{
    public QueueSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
