package com.example.video_orchestrator.orchestrator;

import com.example.video_orchestrator.job.JobHandler;
import com.example.video_orchestrator.model.VideoJob;
import com.example.video_orchestrator.repository.VideoJobRepository;
import com.example.video_orchestrator.retry.RetryPolicy;
import com.example.video_orchestrator.services.OrchestratorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestratorServiceTest {

    @Mock VideoJobRepository repository;
    @Mock JobHandler jobHandler;
    @Mock RetryPolicy retryPolicy;

    ThreadPoolExecutor executor;
    OrchestratorService service;

    @BeforeEach
    void setup() {
        executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
        service = new OrchestratorService(
                repository, jobHandler, retryPolicy, executor);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    private void runAndWait() throws InterruptedException {
        service.runOnce();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void shouldProcessJobSuccessfully() throws Exception {
        VideoJob job = new VideoJob(1L, "video.mp4", 0);
        when(repository.lockAndMarkProcessing(anyInt())).thenReturn(List.of(job));

        runAndWait();

        verify(jobHandler).handle(job);
        verify(repository).markDone(1L);
        verify(repository, never()).markRetry(anyLong(), anyInt());
    }

    @Test
    void shouldRetryOnFailure() throws Exception {
        VideoJob job = new VideoJob(1L, "video.mp4", 1);
        when(repository.lockAndMarkProcessing(anyInt())).thenReturn(List.of(job));
        doThrow(new RuntimeException("fail")).when(jobHandler).handle(job);
        when(retryPolicy.nextDelaySeconds(1)).thenReturn(10);

        runAndWait();

        verify(repository).markRetry(1L, 10);
        verify(repository, never()).markDone(anyLong());
    }

    @Test
    void shouldMarkFailedAfterLastRetry() throws Exception {
        VideoJob job = new VideoJob(1L, "video.mp4", 4);
        when(repository.lockAndMarkProcessing(anyInt())).thenReturn(List.of(job));
        doThrow(new RuntimeException("fail")).when(jobHandler).handle(job);

        runAndWait();

        verify(repository).markFailed(1L);
        verify(repository, never()).markRetry(anyLong(), anyInt());
    }
}