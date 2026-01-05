package com.sleekydz86.domain.message.factory;

import com.sleekydz86.domain.message.entity.MessageEntity;
import com.sleekydz86.domain.message.strategy.MessageValidationStrategy;
import com.sleekydz86.domain.message.type.MessageStatus;
import com.sleekydz86.domain.message.valueobject.MessageContent;
import com.sleekydz86.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageFactory {

    private final List<MessageValidationStrategy> validationStrategies;

    public MessageEntity create(
            UserEntity sender,
            UserEntity receiver,
            String subject,
            String content
    ) {
        MessageContent messageContent = MessageContent.of(subject, content);

        MessageValidationStrategy strategy = findStrategy(content, subject);
        strategy.validate(messageContent);

        return MessageEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .messageContent(messageContent)
                .status(MessageStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();
    }

    private MessageValidationStrategy findStrategy(String content, String subject) {
        return validationStrategies.stream()
                .filter(strategy -> strategy.supports(content, subject))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 메시지 타입입니다."));
    }
}

