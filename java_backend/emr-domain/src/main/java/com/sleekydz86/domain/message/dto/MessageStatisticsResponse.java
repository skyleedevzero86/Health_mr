package com.sleekydz86.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageStatisticsResponse {

    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId;
    private String userName;

    private Long totalMessages;
    private Long totalSent;
    private Long totalReceived;
    private Long totalRead;
    private Long totalUnread;

    private Long averageMessagesPerUser;
    private Long averageReadRate;

    private Long mostActiveSenderId;
    private String mostActiveSenderName;
    private Long mostActiveReceiverId;
    private String mostActiveReceiverName;
}

