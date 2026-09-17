package com.readus.forum.service;

import com.readus.forum.dto.CursorPage;
import com.readus.forum.dto.FeedItemResponse;
import com.readus.forum.entity.Discussion;
import com.readus.forum.repository.DiscussionRepository;
import com.readus.forum.repository.ReactionRepository;
import com.readus.forum.util.CursorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int CANDIDATE_LIMIT = 500;
    /** Half-life window for the recency factor, in seconds (7 days). */
    private static final double RECENCY_WINDOW_SECONDS = 7.0 * 24 * 3600;

    private final DiscussionRepository discussionRepository;
    private final ReactionRepository reactionRepository;

    /**
     * Personalized feed: active discussions scored by recency + engagement, with a bonus
     * for discussions the user already liked. Cursor is an opaque base64 offset.
     */
    @Cacheable(value = "feeds", key = "#userId + '_' + #cursor + '_' + #limit", unless = "#result.items.isEmpty()")
    public CursorPage<FeedItemResponse> getPersonalizedFeed(UUID userId, String cursor, int limit) {
        int size = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        int offset = CursorUtil.decodeOffset(cursor);

        Set<UUID> likedDiscussions = reactionRepository.findLikedDiscussionIdsByUser(userId);
        List<Discussion> candidates = discussionRepository.findActive(PageRequest.of(0, CANDIDATE_LIMIT));

        List<Scored> scored = candidates.stream()
                .map(d -> new Scored(d, calculateScore(d, likedDiscussions.contains(d.getId()))))
                .sorted(Comparator.comparingDouble((Scored s) -> s.score()).reversed())
                .toList();

        int end = Math.min(offset + size, scored.size());
        if (offset >= scored.size()) {
            return new CursorPage<>(List.of(), null);
        }

        List<FeedItemResponse> items = scored.subList(offset, end).stream()
                .map(s -> toItem(s.discussion(), s.score()))
                .toList();

        String nextCursor = end < scored.size() ? CursorUtil.encodeOffset(end) : null;
        return new CursorPage<>(items, nextCursor);
    }

    private FeedItemResponse toItem(Discussion d, double score) {
        return FeedItemResponse.builder()
                .type("DISCUSSION")
                .id(d.getId())
                .title(d.getTitle())
                .contentHtml(d.getContentHtml())
                .relevanceScore(score)
                .author(FeedItemResponse.Author.builder()
                        .id(d.getUser().getId())
                        .username(d.getUser().getUsername())
                        .build())
                .createdAt(d.getCreatedAt())
                .viewCount(d.getViewCount())
                .replyCount(d.getReplyCount())
                .build();
    }

    private double calculateScore(Discussion d, boolean isLiked) {
        long ageSeconds = Math.max(0, Instant.now().getEpochSecond() - d.getCreatedAt().getEpochSecond());
        double recency = Math.exp(-ageSeconds / RECENCY_WINDOW_SECONDS);
        double engagement = d.getViewCount() * 0.3 + d.getReplyCount() * 0.5 + d.getReactionCount() * 0.8;
        double personalBonus = isLiked ? 20 : 0;
        return (engagement * recency) + personalBonus;
    }

    private record Scored(Discussion discussion, double score) {}
}
