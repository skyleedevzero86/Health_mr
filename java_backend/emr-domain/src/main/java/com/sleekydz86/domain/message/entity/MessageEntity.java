package com.sleekydz86.domain.message.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.message.valueobject.MessageContent;
import com.sleekydz86.domain.message.type.MessageStatus;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "Message")
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "subject", column = @Column(name = "subject", nullable = false, length = 200)),
            @AttributeOverride(name = "content", column = @Column(name = "content", nullable = false, columnDefinition = "TEXT"))
    })
    private MessageContent messageContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sender_deleted", nullable = false)
    private Boolean senderDeleted;

    @Column(name = "receiver_deleted", nullable = false)
    private Boolean receiverDeleted;

    @Builder
    private MessageEntity(
            Long messageId,
            UserEntity sender,
            UserEntity receiver,
            MessageContent messageContent,
            MessageStatus status,
            LocalDateTime sentAt
    ) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.messageContent = messageContent;
        this.status = status != null ? status : MessageStatus.SENT;
        this.sentAt = sentAt != null ? sentAt : LocalDateTime.now();
        this.senderDeleted = false;
        this.receiverDeleted = false;
    }

    public void markAsRead() {
        if (this.status == MessageStatus.SENT) {
            this.status = MessageStatus.READ;
            this.readAt = LocalDateTime.now();
        }
    }

    public void deleteBySender() {
        this.senderDeleted = true;
        if (this.receiverDeleted) {
            this.status = MessageStatus.DELETED;
        }
    }

    public void deleteByReceiver() {
        this.receiverDeleted = true;
        if (this.senderDeleted) {
            this.status = MessageStatus.DELETED;
        }
    }

    public boolean isSentBy(Long userId) {
        return this.sender != null && this.sender.getId().equals(userId);
    }

    public boolean isReceivedBy(Long userId) {
        return this.receiver != null && this.receiver.getId().equals(userId);
    }

    public boolean isDeleted() {
        return this.status == MessageStatus.DELETED;
    }

    public boolean isRead() {
        return this.status == MessageStatus.READ;
    }
}

