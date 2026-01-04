package com.sleekydz86.domain.message.domain.service;

import com.sleekydz86.domain.message.entity.MessageEntity;
import com.sleekydz86.domain.user.entity.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class MessageDomainService {

    public void validateMessageSend(UserEntity sender, UserEntity receiver) {
        if (sender == null) {
            throw new IllegalArgumentException("발신자는 필수입니다.");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("수신자는 필수입니다.");
        }
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("자기 자신에게 메시지를 보낼 수 없습니다.");
        }
    }

    public boolean canReadMessage(MessageEntity message, Long userId) {
        return message.isSentBy(userId) || message.isReceivedBy(userId);
    }

    public boolean canDeleteMessage(MessageEntity message, Long userId) {
        return message.isSentBy(userId) || message.isReceivedBy(userId);
    }
}

