package com.courage.streamer.common.entity;

import com.courage.streamer.common.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "video")
public class Video {

    @Id
    private UUID id;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    private VideoStatus status;

    @Column(name = "key_360p")
    private String key360p;

    @Column(name = "key_720p")
    private String key720p;

    @Column(name = "key_1080p")
    private String key1080p;

    @Column(name = "created_at")
    private Instant createdAt;
}

