package com.readus.forum.service;

import com.readus.forum.dto.*;
import com.readus.forum.entity.Discussion;
import com.readus.forum.entity.Message;
import com.readus.forum.entity.User;
import com.readus.forum.exception.BadRequestException;
import com.readus.forum.exception.ForbiddenException;
import com.readus.forum.exception.NotFoundException;
import com.readus.forum.repository.BranchRepository;
import com.readus.forum.repository.DiscussionRepository;
import com.readus.forum.repository.MessageRepository;
import com.readus.forum.repository.UserRepository;
import com.readus.forum.util.CursorUtil;
import com.readus.forum.util.MarkdownProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final DiscussionRepository discussionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final MarkdownProcessor markdownProcessor;
    private final MessageService messageService;

    public CursorPage<DiscussionResponse> getAll(UUID branchId, String cursor, int limit) {
        int size = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        CursorUtil.KeysetCursor keyset = CursorUtil.decodeKeyset(cursor);

        List<Discussion> rows;
        if (branchId != null) {
            if (keyset == null) {
                rows = discussionRepository.findActiveByBranch(branchId, PageRequest.of(0, size + 1));
            } else {
                rows = discussionRepository.findActiveByBranchAfter(branchId,
                        Instant.ofEpochMilli(keyset.epochMillis()), keyset.id(), PageRequest.of(0, size + 1));
            }
        } else if (keyset == null) {
            rows = discussionRepository.findActive(PageRequest.of(0, size + 1));
        } else {
            rows = discussionRepository.findActiveAfter(Instant.ofEpochMilli(keyset.epochMillis()),
                    keyset.id(), PageRequest.of(0, size + 1));
        }

        boolean hasNext = rows.size() > size;
        List<Discussion> page = hasNext ? rows.subList(0, size) : rows;
        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            Discussion last = page.get(page.size() - 1);
            nextCursor = CursorUtil.encodeKeyset(last.getCreatedAt(), last.getId());
        }
        return new CursorPage<>(page.stream().map(this::toResponse).toList(), nextCursor);
    }

    @Transactional
    public DiscussionDetailResponse getByIdOrSlug(String idOrSlug) {
        Discussion discussion = findEntityByIdOrSlug(idOrSlug);
        if (discussion.getDeletedAt() != null) {
            throw new NotFoundException("Discussion not found");
        }

        // clearAutomatically: the bulk update evicts the persistence context, so reload afterwards.
        discussionRepository.incrementViewCount(discussion.getId());
        discussion = discussionRepository.findById(discussion.getId())
                .orElseThrow(() -> new NotFoundException("Discussion not found"));

        Message last = messageRepository.findFirstByDiscussionIdOrderByCreatedAtDesc(discussion.getId())
                .orElse(null);

        return DiscussionDetailResponse.builder()
                .id(discussion.getId())
                .branchId(discussion.getBranchId())
                .title(discussion.getTitle())
                .slug(discussion.getSlug())
                .content(discussion.getContent())
                .contentHtml(discussion.getContentHtml())
                .viewCount(discussion.getViewCount())
                .replyCount(discussion.getReplyCount())
                .reactionCount(discussion.getReactionCount())
                .createdAt(discussion.getCreatedAt())
                .updatedAt(discussion.getUpdatedAt())
                .user(toUserInfo(discussion.getUser()))
                .lastMessage(last == null ? null : messageService.toResponse(last))
                .build();
    }

    @Transactional
    public DiscussionResponse create(UUID userId, CreateDiscussionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!branchRepository.existsById(request.getBranchId())) {
            throw new BadRequestException("Branch not found: " + request.getBranchId());
        }

        Discussion discussion = new Discussion();
        discussion.setTitle(request.getTitle());
        discussion.setSlug(generateSlug(request.getTitle()));
        discussion.setContent(request.getContent());
        discussion.setContentHtml(markdownProcessor.toSafeHtml(request.getContent()));
        discussion.setUser(user);
        discussion.setBranchId(request.getBranchId());

        discussion = discussionRepository.save(discussion);

        if (request.getMessageContent() != null && !request.getMessageContent().isBlank()) {
            Message first = new Message();
            first.setContent(request.getMessageContent());
            first.setContentHtml(markdownProcessor.toSafeHtml(request.getMessageContent()));
            first.setDiscussion(discussion);
            first.setUser(user);
            first.setModerationStatus((short) 0); // pending
            first = messageRepository.save(first);

            discussion.setLastMessage(first);
            // Opening post: no reply_count increment.
            discussionRepository.saveAndFlush(discussion);
        }

        return toResponse(discussion);
    }

    @Transactional
    public DiscussionResponse update(UUID userId, UUID id, DiscussionUpdateRequest request) {
        Discussion discussion = findActiveById(id);
        if (!discussion.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Only the author can edit this discussion");
        }

        // Slug stays stable across edits.
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            discussion.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            discussion.setContent(request.getContent());
            discussion.setContentHtml(markdownProcessor.toSafeHtml(request.getContent()));
        }

        return toResponse(discussionRepository.saveAndFlush(discussion));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Discussion discussion = findActiveById(id);
        if (!discussion.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Only the author can delete this discussion");
        }
        discussion.setDeletedAt(Instant.now());
        discussionRepository.saveAndFlush(discussion);
    }

    Discussion findEntityByIdOrSlug(String idOrSlug) {
        UUID id = parseUuid(idOrSlug);
        if (id != null) {
            return discussionRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Discussion not found"));
        }
        return discussionRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new NotFoundException("Discussion not found"));
    }

    Discussion findActiveById(UUID id) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Discussion not found"));
        if (discussion.getDeletedAt() != null) {
            throw new NotFoundException("Discussion not found");
        }
        return discussion;
    }

    DiscussionResponse toResponse(Discussion discussion) {
        return DiscussionResponse.builder()
                .id(discussion.getId())
                .branchId(discussion.getBranchId())
                .title(discussion.getTitle())
                .slug(discussion.getSlug())
                .content(discussion.getContent())
                .viewCount(discussion.getViewCount())
                .replyCount(discussion.getReplyCount())
                .reactionCount(discussion.getReactionCount())
                .createdAt(discussion.getCreatedAt())
                .user(toUserInfo(discussion.getUser()))
                .build();
    }

    private DiscussionResponse.UserInfo toUserInfo(User user) {
        if (user == null) return null;
        return DiscussionResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
