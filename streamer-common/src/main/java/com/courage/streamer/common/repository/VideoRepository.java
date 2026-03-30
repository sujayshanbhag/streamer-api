package com.courage.streamer.common.repository;

import com.courage.streamer.common.dto.VideoDto;
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
    SELECT new com.courage.streamer.common.dto.VideoDto(
        v.id, v.title, v.description, v.thumbnailKey, v.status, v.createdBy, u.name, v.createdAt
    )
    FROM Video v
    JOIN User u ON v.createdBy = u.id
    WHERE v.status = 'LIVE'
    AND (CAST(:cursor AS timestamp) IS NULL OR v.createdAt < :cursor)
    ORDER BY v.createdAt DESC
""")
    List<VideoDto> findLiveWithCursor(
            @Param("cursor") Instant cursor,
            @Param("size") int size
    );

    @Query(value = """
        SELECT new com.courage.streamer.common.dto.VideoDto(
            v.id, v.title, v.description, v.thumbnailKey, v.status, v.createdBy, u.name, v.createdAt
        )
        FROM Video v
        JOIN User u ON v.createdBy = u.id
        WHERE v.createdBy = :userId
        AND (CAST(:cursor AS timestamp) IS NULL OR v.createdAt < :cursor)
        ORDER BY v.createdAt DESC
    """)
    List<VideoDto> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursor") Instant cursor,
            @Param("size") int size
    );

    @Query("""
    SELECT new com.courage.streamer.common.dto.VideoDto(
        v.id, v.title, v.description, v.thumbnailKey, v.status, v.createdBy, u.name, v.createdAt
    )
    FROM Video v
    JOIN User u ON v.createdBy = u.id
    WHERE v.createdBy = :userId
    AND v.status = 'LIVE'
    AND (CAST(:cursor AS timestamp) IS NULL OR v.createdAt < :cursor)
    ORDER BY v.createdAt DESC
""")
    List<VideoDto> findLiveByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursor") Instant cursor,
            @Param("size") int size
    );

    @Query("""
    SELECT new com.courage.streamer.common.dto.VideoDto(
        v.id, v.title, v.description, v.thumbnailKey, v.status, v.createdBy, u.name, v.createdAt
    )
    FROM Video v
    JOIN User u ON v.createdBy = u.id
    WHERE v.id = :id
""")
    Optional<VideoDto> findVideoById(@Param("id") UUID id);
}
