package com.example.video_orchestrator.runner;

import com.example.video_orchestrator.repository.VideoJobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRecoveryRunner implements CommandLineRunner {

    private final VideoJobRepository repository;

    public StartupRecoveryRunner(VideoJobRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.resetStuckJobs();
    }
}