package com.kinghy.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RagAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagAiApplication.class, args);
    }

}
