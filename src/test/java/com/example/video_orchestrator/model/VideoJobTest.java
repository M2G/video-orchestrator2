package com.example.video_orchestrator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VideoJobTest {

    @Test
    void retryCountCannotBeNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new VideoJob(1L, "video.mp4", -1));
    }
}
