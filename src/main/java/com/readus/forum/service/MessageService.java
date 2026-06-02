package com.readus.forum.service;

import com.readus.forum.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

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
}