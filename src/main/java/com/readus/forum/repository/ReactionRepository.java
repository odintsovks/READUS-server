package com.readus.forum.repository;

import com.readus.forum.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUserIdAndTargetTypeAndMessageIdAndType(UUID userId, String targetType,
                                                                    UUID messageId, Short type);

    Optional<Reaction> findByUserIdAndTargetTypeAndDiscussionIdAndType(UUID userId, String targetType,
                                                                       UUID discussionId, Short type);

    List<Reaction> findByUserIdAndMessageId(UUID userId, UUID messageId);

    List<Reaction> findByUserIdAndMessageIdIn(UUID userId, Collection<UUID> messageIds);

    @Query("""
            SELECT r.discussionId FROM Reaction r
            WHERE r.targetType = 'DISCUSSION' AND r.type = 1 AND r.userId = :userId
            """)
    Set<UUID> findLikedDiscussionIdsByUser(@Param("userId") UUID userId);

    /** Grouped like/dislike counts for a batch of messages. */
    interface MessageReactionCount {
        UUID getMessageId();
        Short getType();
        Long getCnt();
    }

    @Query("""
            SELECT r.messageId AS messageId, r.type AS type, COUNT(r) AS cnt
            FROM Reaction r
            WHERE r.messageId IN :messageIds AND r.type IN :types
            GROUP BY r.messageId, r.type
            """)
    List<MessageReactionCount> countByMessageIdIn(@Param("messageIds") Collection<UUID> messageIds,
                                                  @Param("types") Collection<Short> types);

    long countByMessageIdAndType(UUID messageId, Short type);

    @Modifying
    @Transactional
    @Query("UPDATE Discussion d SET d.reactionCount = d.reactionCount + 1 WHERE d.id = :discussionId")
    void incrementReactionCount(@Param("discussionId") UUID discussionId);

    @Modifying
    @Transactional
    @Query("UPDATE Discussion d SET d.reactionCount = d.reactionCount - 1 WHERE d.id = :discussionId")
    void decrementReactionCount(@Param("discussionId") UUID discussionId);
}
