package com.readus.forum.service;

import com.readus.forum.dto.DiscussionResponse;
import com.readus.forum.entity.Discussion;
import com.readus.forum.repository.DiscussionRepository;
import com.readus.forum.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final DiscussionRepository discussionRepository;
    private final ReactionRepository reactionRepository;
    private final DiscussionService discussionService;

    @Cacheable(value = "feeds", key = "#userId + '_' + #pageable.pageNumber", unless = "#result.content.isEmpty()")
    public Page<DiscussionResponse> getPersonalizedFeed(UUID userId, Pageable pageable) {
        Set<UUID> likedDiscussions = reactionRepository.findLikedMessageIdsByUser(userId);

        Page<Discussion> discussions = discussionRepository.findAll(Pageable.ofSize(500));

        List<Discussion> scored = discussions.getContent().stream()
                .sorted((a, b) -> {
                    double scoreA = calculateScore(a, likedDiscussions.contains(a.getId()));
                    double scoreB = calculateScore(b, likedDiscussions.contains(b.getId()));
                    return Double.compare(scoreB, scoreA);
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), scored.size());

        if (start >= scored.size()) {
            return new PageImpl<>(List.of(), pageable, scored.size());
        }

        List<DiscussionResponse> responses = scored.subList(start, end).stream()
                .map(discussionService::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, scored.size());
    }

    private double calculateScore(Discussion d, boolean isLiked) {
        double recency = Math.exp(-d.getCreatedAt().getEpochSecond() / (24.0 * 3600 * 7));
        double engagement = d.getViewCount() * 0.3 + d.getReplyCount() * 0.5 + d.getReactionCount() * 0.8;
        double personalBonus = isLiked ? 20 : 0;
        return (engagement * recency) + personalBonus;
    }
}
