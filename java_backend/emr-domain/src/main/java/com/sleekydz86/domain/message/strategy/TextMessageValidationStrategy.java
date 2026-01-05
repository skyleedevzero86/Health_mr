package com.sleekydz86.domain.message.strategy;

import com.sleekydz86.domain.message.valueobject.MessageContent;
import org.springframework.stereotype.Component;

@Component
public class TextMessageValidationStrategy implements MessageValidationStrategy {

    @Override
    public boolean supports(String content, String subject) {
        return content != null && subject != null;
    }

    @Override
    public void validate(MessageContent messageContent) {
        if (messageContent == null) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다.");
        }
        if (messageContent.getSubject() == null || messageContent.getSubject().isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (messageContent.getContent() == null || messageContent.getContent().isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        if (messageContent.getSubject().length() > 200) {
            throw new IllegalArgumentException("제목은 200자를 초과할 수 없습니다.");
        }
    }
}

