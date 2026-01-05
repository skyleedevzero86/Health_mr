package com.sleekydz86.domain.message.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity(name = "MessageRead")
@Table(name = "message_read")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageReadEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "read_id", nullable = false)
    private Long readId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @Builder
    private MessageReadEntity(
            Long readId,
            MessageEntity message,
            UserEntity user,
            LocalDateTime readAt
    ) {
        this.readId = readId;
        this.message = message;
        this.user = user;
        this.readAt = readAt != null ? readAt : LocalDateTime.now();
    }
}

