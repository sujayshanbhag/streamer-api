package com.courage.streamer.common.repository;

import com.courage.streamer.common.entity.Video;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Video v WHERE v.id = :id")
    Optional<Video> findByIdForUpdate(@Param("id") UUID id);

    @Query(value = """
    SELECT * FROM video
    WHERE status = 'LIVE'
    AND (CAST(:cursor as timestamp) IS NULL OR created_at < :cursor)
    ORDER BY created_at DESC
    LIMIT :size
    """, nativeQuery = true)
    List<Video> findLiveWithCursor(
            @Param("cursor") Instant cursor,
            @Param("size") int size
    );

    @Query(value = """
    SELECT * FROM video
    WHERE created_by = :userId
    AND (CAST(:cursor AS timestamp) IS NULL OR created_at < :cursor)
    ORDER BY created_at DESC 
    LIMIT :size
    """, nativeQuery = true)
    List<Video> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursor") Instant cursor,
            @Param("size") int size
    );

}
