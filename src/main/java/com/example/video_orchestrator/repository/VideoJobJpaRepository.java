package com.example.video_orchestrator.repository;

import com.example.video_orchestrator.model.VideoJobEntity;
import com.example.video_orchestrator.model.VideoJobStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoJobJpaRepository extends JpaRepository<VideoJobEntity, Long> {

    Optional<VideoJobEntity> findByFilename(String filename);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    List<VideoJobEntity> findByStatusAndRetryCountLessThanAndNextRetryAtLessThanEqual(
            VideoJobStatus status, int maxRetry, LocalDateTime now, Pageable page);

    List<VideoJobEntity> findByStatusAndUpdatedAtBefore(
            VideoJobStatus status, LocalDateTime before);
}