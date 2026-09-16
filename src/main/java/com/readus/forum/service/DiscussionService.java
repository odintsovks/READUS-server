package com.readus.forum.service;

import com.readus.forum.dto.*;
import com.readus.forum.entity.Discussion;
import com.readus.forum.entity.Message;
import com.readus.forum.entity.User;
import com.readus.forum.repository.DiscussionRepository;
import com.readus.forum.repository.MessageRepository;
import com.readus.forum.repository.UserRepository;
import com.readus.forum.util.MarkdownProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MarkdownProcessor markdownProcessor;

    public Page<DiscussionResponse> getAll(Pageable pageable) {
        return discussionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public DiscussionDetailResponse getBySlug(String slug) {
        Discussion discussion = discussionRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Discussion not found"));

        discussionRepository.incrementViewCount(discussion.getId());

        Page<Message> messages = messageRepository.findByDiscussionIdOrderByCreatedAtAsc(
                discussion.getId(), Pageable.ofSize(100));

        return DiscussionDetailResponse.builder()
                .id(discussion.getId())
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
                .messages(messages.getContent().stream()
                        .map(this::toMessageResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public DiscussionResponse create(UUID userId, CreateDiscussionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Discussion discussion = new Discussion();
        discussion.setTitle(request.getTitle());
        discussion.setSlug(generateSlug(request.getTitle()));
        discussion.setContent(request.getContent());
        discussion.setContentHtml(markdownProcessor.toSafeHtml(request.getContent()));
        discussion.setUser(user);

        discussion = discussionRepository.save(discussion);

        return toResponse(discussion);
    }

    @Transactional
    public MessageResponse addMessage(UUID discussionId, UUID userId, CreateMessageRequest request) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Discussion not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();
        message.setContent(request.getContent());
        message.setContentHtml(markdownProcessor.toSafeHtml(request.getContent()));
        message.setDiscussion(discussion);
        message.setUser(user);
        message.setModerationStatus((short)0); // pending

        message = messageRepository.save(message);

        discussion.setLastMessage(message);
        discussionRepository.incrementReplyCount(discussionId);

        return toMessageResponse(message);
    }

    DiscussionResponse toResponse(Discussion discussion) {
        return DiscussionResponse.builder()
                .id(discussion.getId())
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

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .contentHtml(message.getContentHtml())
                .createdAt(message.getCreatedAt())
                .user(toUserInfo(message.getUser()))
                .reactionCount(message.getReactions().size())
                .likedByCurrentUser(false)
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
}
