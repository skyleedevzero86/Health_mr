package com.sleekydz86.domain.message.strategy;

import com.sleekydz86.domain.message.valueobject.MessageContent;

public interface MessageValidationStrategy {
    boolean supports(String content, String subject);
    void validate(MessageContent messageContent);
}

