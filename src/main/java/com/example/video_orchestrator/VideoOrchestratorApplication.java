package com.example.video_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VideoOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoOrchestratorApplication.class, args);
    }
}