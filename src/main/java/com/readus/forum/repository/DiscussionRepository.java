package com.readus.forum.repository;

import com.readus.forum.entity.Discussion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {
    Optional<Discussion> findBySlug(String slug);

    @Query("""
            SELECT d FROM Discussion d
            WHERE d.deletedAt IS NULL
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    List<Discussion> findActive(Pageable pageable);

    @Query("""
            SELECT d FROM Discussion d
            WHERE d.deletedAt IS NULL AND d.branchId = :branchId
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    List<Discussion> findActiveByBranch(@Param("branchId") UUID branchId, Pageable pageable);

    @Query("""
            SELECT d FROM Discussion d
            WHERE d.deletedAt IS NULL
              AND (d.createdAt < :t OR (d.createdAt = :t AND d.id < :i))
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    List<Discussion> findActiveAfter(@Param("t") Instant t, @Param("i") UUID i, Pageable pageable);

    @Query("""
            SELECT d FROM Discussion d
            WHERE d.deletedAt IS NULL AND d.branchId = :branchId
              AND (d.createdAt < :t OR (d.createdAt = :t AND d.id < :i))
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    List<Discussion> findActiveByBranchAfter(@Param("branchId") UUID branchId,
                                             @Param("t") Instant t, @Param("i") UUID i, Pageable pageable);

    /** clearAutomatically: the caller must reload the entity after this runs. */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Discussion d SET d.viewCount = d.viewCount + 1 WHERE d.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Discussion d SET d.replyCount = d.replyCount + 1 WHERE d.id = :id")
    void incrementReplyCount(@Param("id") UUID id);
}
