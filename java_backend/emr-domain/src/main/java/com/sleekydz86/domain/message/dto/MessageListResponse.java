package com.sleekydz86.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageListResponse {

    private Long totalCount;
    private List<MessageResponse> messages;

    public static MessageListResponse of(List<MessageResponse> messages) {
        return new MessageListResponse((long) messages.size(), messages);
    }
}

