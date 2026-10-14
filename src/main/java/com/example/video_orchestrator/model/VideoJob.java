package com.example.video_orchestrator.model;

// Représentation d'un job vidéo
public record VideoJob(
        long id,
        String filename,
        int retryCount
) {
    public VideoJob {
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must be >= 0");
        }
    }
}