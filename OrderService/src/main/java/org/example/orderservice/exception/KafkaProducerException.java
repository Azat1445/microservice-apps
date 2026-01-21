package org.example.orderservice.exception;


public class KafkaProducerException extends RuntimeException {
    public KafkaProducerException(String msg) {
        super(msg);
    }

    public KafkaProducerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
