package com.readus.forum.repository;

import com.readus.forum.entity.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {
    Optional<Discussion> findBySlug(String slug);
    Page<Discussion> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Discussion d SET d.viewCount = d.viewCount + 1 WHERE d.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Discussion d SET d.replyCount = d.replyCount + 1 WHERE d.id = :id")
    void incrementReplyCount(@Param("id") UUID id);
}