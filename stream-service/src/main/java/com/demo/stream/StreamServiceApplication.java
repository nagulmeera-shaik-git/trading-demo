package com.demo.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Pushes live prices and order updates to browsers over WebSocket. */
@SpringBootApplication
public class StreamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamServiceApplication.class, args);
    }
}
