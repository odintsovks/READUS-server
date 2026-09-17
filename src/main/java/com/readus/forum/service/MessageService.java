package com.readus.forum.service;

import com.readus.forum.dto.CreateMessageRequest;
import com.readus.forum.dto.DiscussionResponse;
import com.readus.forum.dto.MessageResponse;
import com.readus.forum.dto.MessageUpdateRequest;
import com.readus.forum.entity.Discussion;
import com.readus.forum.entity.Message;
import com.readus.forum.entity.Reaction;
import com.readus.forum.entity.User;
import com.readus.forum.exception.BadRequestException;
import com.readus.forum.exception.ForbiddenException;
import com.readus.forum.exception.NotFoundException;
import com.readus.forum.repository.DiscussionRepository;
import com.readus.forum.repository.MessageRepository;
import com.readus.forum.repository.ReactionRepository;
import com.readus.forum.repository.ReactionRepository.MessageReactionCount;
import com.readus.forum.repository.UserRepository;
import com.readus.forum.util.MarkdownProcessor;
import com.readus.forum.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private static final short LIKE = 1;
    private static final short DISLIKE = 2;
    private static final short REJECTED = 2;

    private final MessageRepository messageRepository;
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final MarkdownProcessor markdownProcessor;

    @Transactional
    public void approveMessage(UUID messageId) {
        messageRepository.updateModerationStatus(messageId, 1);
        log.info("Message {} approved", messageId);
    }

    @Transactional
    public void rejectMessage(UUID messageId) {
        messageRepository.updateModerationStatus(messageId, 2);
        log.info("Message {} rejected", messageId);
    }

    /** All visible messages (rejected ones hidden), ascending, with author + reaction counts. */
    public List<MessageResponse> listByDiscussion(UUID discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new NotFoundException("Discussion not found"));
        if (discussion.getDeletedAt() != null) {
            throw new NotFoundException("Discussion not found");
        }

        List<Message> messages = messageRepository
                .findAllByDiscussionIdAndModerationStatusNotOrderByCreatedAtAsc(discussionId, REJECTED);
        if (messages.isEmpty()) {
            return List.of();
        }

        Set<UUID> userIds = messages.stream().map(m -> m.getUser().getId()).collect(Collectors.toSet());
        Map<UUID, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<UUID> ids = messages.stream().map(Message::getId).toList();
        Map<UUID, int[]> counts = new HashMap<>();
        for (MessageReactionCount row : reactionRepository.countByMessageIdIn(ids, List.of(LIKE, DISLIKE))) {
            int[] pair = counts.computeIfAbsent(row.getMessageId(), k -> new int[2]);
            pair[row.getType() == LIKE ? 0 : 1] = row.getCnt().intValue();
        }

        UUID viewer = SecurityUtils.getOptionalCurrentUserId();
        Map<UUID, List<Short>> myReactions = viewer == null ? Map.of()
                : reactionRepository.findByUserIdAndMessageIdIn(viewer, ids).stream()
                        .collect(Collectors.groupingBy(Reaction::getMessageId,
                                Collectors.mapping(Reaction::getType, Collectors.toList())));

        return messages.stream().map(m -> {
            int[] pair = counts.getOrDefault(m.getId(), new int[2]);
            User author = users.get(m.getUser().getId());
            return build(m, pair[0], pair[1], myReactions.getOrDefault(m.getId(), List.of()), author);
        }).toList();
    }

    @Transactional
    public MessageResponse create(UUID userId, CreateMessageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Discussion discussion = discussionRepository.findById(request.getDiscussionId())
                .orElseThrow(() -> new NotFoundException("Discussion not found: " + request.getDiscussionId()));
        if (discussion.getDeletedAt() != null) {
            throw new NotFoundException("Discussion not found");
        }

        Message parent = null;
        if (request.getParentMessageId() != null) {
            parent = messageRepository.findById(request.getParentMessageId())
                    .orElseThrow(() -> new BadRequestException(
                            "Parent message not found: " + request.getParentMessageId()));
            if (!parent.getDiscussion().getId().equals(discussion.getId())) {
                throw new BadRequestException("Parent message belongs to a different discussion");
            }
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setContentHtml(markdownProcessor.toSafeHtml(request.getContent()));
        message.setDiscussion(discussion);
        message.setUser(user);
        message.setParentMessage(parent);
        message.setModerationStatus((short) 0); // pending
        message = messageRepository.save(message);

        discussion.setLastMessage(message);
        // Flush the last_message_id association before the bulk counter update.
        discussionRepository.saveAndFlush(discussion);
        discussionRepository.incrementReplyCount(discussion.getId());

        return toResponse(message);
    }

    @Transactional
    public MessageResponse update(UUID userId, UUID id, MessageUpdateRequest request) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        if (!message.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Only the author can edit this message");
        }

        message.setContent(request.getContent());
        message.setContentHtml(markdownProcessor.toSafeHtml(request.getContent()));
        return toResponse(messageRepository.saveAndFlush(message));
    }

    /** Single-message response (used for previews and create/update replies). */
    public MessageResponse toResponse(Message message) {
        long likes = reactionRepository.countByMessageIdAndType(message.getId(), LIKE);
        long dislikes = reactionRepository.countByMessageIdAndType(message.getId(), DISLIKE);
        UUID viewer = SecurityUtils.getOptionalCurrentUserId();
        List<Short> myReactions = viewer == null ? List.of()
                : reactionRepository.findByUserIdAndMessageId(viewer, message.getId()).stream()
                        .map(Reaction::getType).toList();
        return build(message, likes, dislikes, myReactions, message.getUser());
    }

    private MessageResponse build(Message m, long likeCount, long dislikeCount,
                                  List<Short> myReactions, User author) {
        return MessageResponse.builder()
                .id(m.getId())
                .discussionId(m.getDiscussion() != null ? m.getDiscussion().getId() : null)
                .parentMessageId(m.getParentMessage() != null ? m.getParentMessage().getId() : null)
                .content(m.getContent())
                .contentHtml(m.getContentHtml())
                .createdAt(m.getCreatedAt())
                .author(toUserInfo(author))
                .likeCount((int) likeCount)
                .dislikeCount((int) dislikeCount)
                .myReactions(myReactions)
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
}
