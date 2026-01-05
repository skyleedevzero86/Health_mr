package com.sleekydz86.domain.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.message.entity.MessageEntity;
import com.sleekydz86.domain.message.type.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long messageId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String subject;
    private String content;
    private MessageStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    private Boolean senderDeleted;
    private Boolean receiverDeleted;

    public static MessageResponse from(MessageEntity entity) {
        return new MessageResponse(
                entity.getMessageId(),
                entity.getSender().getId(),
                entity.getSender().getName(),
                entity.getReceiver().getId(),
                entity.getReceiver().getName(),
                entity.getMessageContent().getSubject(),
                entity.getMessageContent().getContent(),
                entity.getStatus(),
                entity.getSentAt(),
                entity.getReadAt(),
                entity.getSenderDeleted(),
                entity.getReceiverDeleted()
        );
    }
}

