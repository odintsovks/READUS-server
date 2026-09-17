package com.readus.forum.service;

import com.readus.forum.dto.ReactionResponse;
import com.readus.forum.entity.Discussion;
import com.readus.forum.entity.Reaction;
import com.readus.forum.exception.NotFoundException;
import com.readus.forum.repository.DiscussionRepository;
import com.readus.forum.repository.MessageRepository;
import com.readus.forum.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionService {

    private static final String TARGET_MESSAGE = "MESSAGE";
    private static final String TARGET_DISCUSSION = "DISCUSSION";

    private final ReactionRepository reactionRepository;
    private final DiscussionRepository discussionRepository;
    private final MessageRepository messageRepository;

    /** Upsert: posting the same (user, type) twice is a no-op returning the existing row. */
    @Transactional
    public ReactionResponse reactToDiscussion(UUID userId, UUID discussionId, Short type) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new NotFoundException("Discussion not found"));
        if (discussion.getDeletedAt() != null) {
            throw new NotFoundException("Discussion not found");
        }

        Optional<Reaction> existing = reactionRepository
                .findByUserIdAndTargetTypeAndDiscussionIdAndType(userId, TARGET_DISCUSSION, discussionId, type);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Reaction reaction = new Reaction();
        reaction.setType(type);
        reaction.setUserId(userId);
        reaction.setTargetType(TARGET_DISCUSSION);
        reaction.setDiscussionId(discussionId);
        reaction = reactionRepository.saveAndFlush(reaction);
        reactionRepository.incrementReactionCount(discussionId);

        return toResponse(reaction);
    }

    /** Upsert: posting the same (user, type) twice is a no-op returning the existing row. */
    @Transactional
    public ReactionResponse reactToMessage(UUID userId, UUID messageId, Short type) {
        if (!messageRepository.existsById(messageId)) {
            throw new NotFoundException("Message not found");
        }

        Optional<Reaction> existing = reactionRepository
                .findByUserIdAndTargetTypeAndMessageIdAndType(userId, TARGET_MESSAGE, messageId, type);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Reaction reaction = new Reaction();
        reaction.setType(type);
        reaction.setUserId(userId);
        reaction.setTargetType(TARGET_MESSAGE);
        reaction.setMessageId(messageId);
        return toResponse(reactionRepository.save(reaction));
    }

    /** Idempotent: removing a reaction the user never made is a no-op. */
    @Transactional
    public void removeDiscussionReaction(UUID userId, UUID discussionId, Short type) {
        Optional<Reaction> existing = reactionRepository
                .findByUserIdAndTargetTypeAndDiscussionIdAndType(userId, TARGET_DISCUSSION, discussionId, type);
        if (existing.isEmpty()) {
            return;
        }
        reactionRepository.delete(existing.get());
        reactionRepository.flush();
        reactionRepository.decrementReactionCount(discussionId);
    }

    private ReactionResponse toResponse(Reaction r) {
        return ReactionResponse.builder()
                .id(r.getId())
                .type(r.getType())
                .targetType(r.getTargetType())
                .messageId(r.getMessageId())
                .discussionId(r.getDiscussionId())
                .build();
    }
}
