package com.sleekydz86.domain.message.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.message.domain.service.MessageDomainService;
import com.sleekydz86.domain.message.dto.MessageListResponse;
import com.sleekydz86.domain.message.dto.MessageResponse;
import com.sleekydz86.domain.message.dto.MessageSendRequest;
import com.sleekydz86.domain.message.entity.MessageEntity;
import com.sleekydz86.domain.message.entity.MessageReadEntity;
import com.sleekydz86.domain.message.factory.MessageFactory;
import com.sleekydz86.domain.message.repository.MessageReadRepository;
import com.sleekydz86.domain.message.repository.MessageRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService implements BaseService<MessageEntity, Long> {

    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final MessageFactory messageFactory;
    private final MessageDomainService messageDomainService;
    private final EventPublisher eventPublisher;

    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageSendRequest request) {
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("발신자를 찾을 수 없습니다."));

        UserEntity receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new NotFoundException("수신자를 찾을 수 없습니다."));

        messageDomainService.validateMessageSend(sender, receiver);

        MessageEntity message = messageFactory.create(
                sender,
                receiver,
                request.getSubject(),
                request.getContent()
        );

        MessageEntity saved = messageRepository.save(message);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.MessageSentEvent(
                saved.getMessageId(),
                saved.getSender().getId(),
                saved.getSender().getName(),
                saved.getReceiver().getId(),
                saved.getReceiver().getName(),
                saved.getMessageContent().getSubject(),
                saved.getSentAt()
        ));

        return MessageResponse.from(saved);
    }

    public MessageResponse getMessage(Long messageId, Long userId) {
        MessageEntity message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new NotFoundException("메시지를 찾을 수 없습니다."));

        if (!messageDomainService.canReadMessage(message, userId)) {
            throw new IllegalArgumentException("메시지를 조회할 권한이 없습니다.");
        }

        if (message.isDeleted()) {
            throw new NotFoundException("삭제된 메시지입니다.");
        }

        if (message.isReceivedBy(userId) && !message.isRead()) {
            message.markAsRead();
            messageRepository.save(message);

            MessageReadEntity messageRead = MessageReadEntity.builder()
                    .message(message)
                    .user(userRepository.findById(userId).orElseThrow())
                    .readAt(message.getReadAt())
                    .build();
            messageReadRepository.save(messageRead);

            eventPublisher.publish(new com.sleekydz86.core.event.domain.MessageReadEvent(
                    message.getMessageId(),
                    userId,
                    userRepository.findById(userId).orElseThrow().getName(),
                    message.getReadAt()
            ));
        }

        return MessageResponse.from(message);
    }

    public MessageListResponse getSentMessages(Long userId) {
        List<MessageEntity> messages = messageRepository
                .findBySender_IdAndSenderDeletedFalse(userId);
        List<MessageResponse> responses = messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        return MessageListResponse.of(responses);
    }

    public MessageListResponse getReceivedMessages(Long userId) {
        List<MessageEntity> messages = messageRepository
                .findByReceiver_IdAndReceiverDeletedFalse(userId);
        List<MessageResponse> responses = messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        return MessageListResponse.of(responses);
    }

    public MessageListResponse getAllMessages(Long userId) {
        List<MessageEntity> messages = messageRepository
                .findBySender_IdOrReceiver_Id(userId, userId);
        List<MessageResponse> responses = messages.stream()
                .filter(msg -> !msg.isDeleted())
                .filter(msg -> {
                    if (msg.isSentBy(userId)) {
                        return !msg.getSenderDeleted();
                    } else {
                        return !msg.getReceiverDeleted();
                    }
                })
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        return MessageListResponse.of(responses);
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        MessageEntity message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new NotFoundException("메시지를 찾을 수 없습니다."));

        if (!messageDomainService.canDeleteMessage(message, userId)) {
            throw new IllegalArgumentException("메시지를 삭제할 권한이 없습니다.");
        }

        if (message.isSentBy(userId)) {
            message.deleteBySender();
        } else {
            message.deleteByReceiver();
        }

        messageRepository.save(message);
    }
}

