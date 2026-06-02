package com.readus.forum.repository;

import com.readus.forum.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    Optional<Reaction> findByUserIdAndMessageId(UUID userId, UUID messageId);

    @Query("SELECT r.message.id FROM Reaction r WHERE r.user.id = :userId AND r.type = 1")
    Set<UUID> findLikedMessageIdsByUser(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE Discussion d SET d.reactionCount = d.reactionCount + 1 WHERE d.id = :discussionId")
    void incrementReactionCount(@Param("discussionId") UUID discussionId);

    @Modifying
    @Transactional
    @Query("UPDATE Discussion d SET d.reactionCount = d.reactionCount - 1 WHERE d.id = :discussionId")
    void decrementReactionCount(@Param("discussionId") UUID discussionId);

    long countByMessageId(UUID messageId);
}