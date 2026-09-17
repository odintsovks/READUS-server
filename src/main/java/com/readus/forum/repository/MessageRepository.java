package com.readus.forum.repository;

import com.readus.forum.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findAllByDiscussionIdAndModerationStatusNotOrderByCreatedAtAsc(UUID discussionId, Short status);

    Optional<Message> findFirstByDiscussionIdOrderByCreatedAtDesc(UUID discussionId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.moderationStatus = :status WHERE m.id = :id")
    void updateModerationStatus(@Param("id") UUID id, @Param("status") int status);
}
