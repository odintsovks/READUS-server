package com.readus.forum.repository;

import com.readus.forum.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UploadRepository extends JpaRepository<Upload, UUID> {
    List<Upload> findByMessageId(UUID messageId);
}