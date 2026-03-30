package com.courage.streamer.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "video_staging")
public class VideoStaging {

    @Id
    private UUID id;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Column(name = "thumbnail_key")
    private String thumbnailKey;

    @Column(name = "created_at")
    private final Instant createdAt = Instant.now();
}

