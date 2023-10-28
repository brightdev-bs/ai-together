package com.example.aitogetherbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AiTogetherBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTogetherBackendApplication.class, args);
    }

}
