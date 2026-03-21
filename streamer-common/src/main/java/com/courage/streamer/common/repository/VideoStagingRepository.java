package com.courage.streamer.common.repository;

import com.courage.streamer.common.entity.VideoStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoStagingRepository extends JpaRepository<VideoStaging, UUID> {
}
