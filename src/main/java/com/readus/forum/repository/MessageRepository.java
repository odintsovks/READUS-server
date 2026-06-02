package com.readus.forum.repository;

import com.readus.forum.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByDiscussionIdOrderByCreatedAtAsc(UUID discussionId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.moderationStatus = :status WHERE m.id = :id")
    void updateModerationStatus(@Param("id") UUID id, @Param("status") int status);
}