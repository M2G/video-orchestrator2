package com.example.video_orchestrator.repository;

import com.example.video_orchestrator.model.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class JpaVideoJobRepository implements VideoJobRepository {

    private static final int MAX_RETRY = 5;
    private final VideoJobJpaRepository jpa;

    public JpaVideoJobRepository(VideoJobJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void upsert(String filename, String status) {
        var target = VideoJobStatus.valueOf(status);
        jpa.findByFilename(filename).ifPresentOrElse(
                job -> job.setStatus(target),
                () -> jpa.save(new VideoJobEntity(filename, target)));
    }

    @Override
    public void markProcessing(long id) { load(id).setStatus(VideoJobStatus.PROCESSING); }

    @Override
    public void markDone(long id) { load(id).setStatus(VideoJobStatus.DONE); }

    @Override
    public void markFailed(long id) { load(id).setStatus(VideoJobStatus.FAILED); }

    @Override
    public void markRetry(long id, int delaySeconds) {
        var job = load(id);
        job.setRetryCount(job.getRetryCount() + 1);
        job.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
        job.setStatus(VideoJobStatus.PENDING);
    }

    @Override
    public void resetStuckJobs() {
        jpa.findByStatusAndUpdatedAtBefore(
                        VideoJobStatus.PROCESSING, LocalDateTime.now().minusMinutes(10))
                .forEach(job -> job.setStatus(VideoJobStatus.PENDING));
    }

    @Override
    public List<VideoJob> lockAndMarkProcessing(int limit) {
        var jobs = jpa.findByStatusAndRetryCountLessThanAndNextRetryAtLessThanEqual(
                VideoJobStatus.PENDING, MAX_RETRY, LocalDateTime.now(),
                PageRequest.of(0, limit, Sort.by("createdAt")));

        jobs.forEach(job -> job.setStatus(VideoJobStatus.PROCESSING));

        return jobs.stream()
                .map(j -> new VideoJob(j.getId(), j.getFilename(), j.getRetryCount()))
                .toList();
    }

    private VideoJobEntity load(long id) {
        return jpa.findById(id).orElseThrow();
    }
}