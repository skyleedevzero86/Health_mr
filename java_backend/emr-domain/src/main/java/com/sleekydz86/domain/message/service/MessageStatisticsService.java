package com.sleekydz86.domain.message.service;

import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.message.dto.MessageStatisticsResponse;
import com.sleekydz86.domain.message.entity.MessageEntity;
import com.sleekydz86.domain.message.repository.MessageRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageStatisticsService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageStatisticsResponse getDailyStatistics(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<MessageEntity> messages = messageRepository.findAll().stream()
                .filter(msg -> !msg.getSentAt().isBefore(start) && !msg.getSentAt().isAfter(end))
                .filter(msg -> !msg.isDeleted())
                .collect(Collectors.toList());

        return buildStatisticsResponse(messages, date, null, null, null);
    }

    public MessageStatisticsResponse getWeeklyStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<MessageEntity> messages = messageRepository.findAll().stream()
                .filter(msg -> !msg.getSentAt().isBefore(start) && !msg.getSentAt().isAfter(end))
                .filter(msg -> !msg.isDeleted())
                .collect(Collectors.toList());

        return buildStatisticsResponse(messages, null, startDate, endDate, null);
    }

    public MessageStatisticsResponse getMonthlyStatistics(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<MessageEntity> messages = messageRepository.findAll().stream()
                .filter(msg -> !msg.getSentAt().isBefore(start) && !msg.getSentAt().isAfter(end))
                .filter(msg -> !msg.isDeleted())
                .collect(Collectors.toList());

        return buildStatisticsResponse(messages, null, startDate, endDate, null);
    }

    public MessageStatisticsResponse getUserStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<MessageEntity> sentMessages = messageRepository.findBySender_Id(userId).stream()
                .filter(msg -> !msg.getSentAt().isBefore(start) && !msg.getSentAt().isAfter(end))
                .filter(msg -> !msg.isDeleted())
                .collect(Collectors.toList());

        List<MessageEntity> receivedMessages = messageRepository.findByReceiver_Id(userId).stream()
                .filter(msg -> !msg.getSentAt().isBefore(start) && !msg.getSentAt().isAfter(end))
                .filter(msg -> !msg.isDeleted())
                .collect(Collectors.toList());

        List<MessageEntity> allMessages = new java.util.ArrayList<>(sentMessages);
        allMessages.addAll(receivedMessages);

        return buildStatisticsResponse(allMessages, null,
                startDate != null ? startDate : start.toLocalDate(),
                endDate != null ? endDate : end.toLocalDate(),
                userId);
    }

    public MessageStatisticsResponse getAdminStatistics(LocalDate startDate, LocalDate endDate) {
        if (!TenantContext.isAdmin()) {
            throw new IllegalArgumentException("관리자만 전체 통계를 조회할 수 있습니다.");
        }

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<MessageEntity> messages = messageRepository.findAll().stream()
                .filter(msg -> !msg.getSentAt().isBefore(start) && !msg.getSentAt().isAfter(end))
                .filter(msg -> !msg.isDeleted())
                .collect(Collectors.toList());

        return buildStatisticsResponse(messages, null,
                startDate != null ? startDate : start.toLocalDate(),
                endDate != null ? endDate : end.toLocalDate(),
                null);
    }

    private MessageStatisticsResponse buildStatisticsResponse(
            List<MessageEntity> messages, LocalDate date,
            LocalDate startDate, LocalDate endDate, Long userId) {

        long totalMessages = messages.size();
        long totalSent = messages.stream()
                .filter(msg -> userId == null || msg.getSender().getId().equals(userId))
                .count();
        long totalReceived = messages.stream()
                .filter(msg -> userId == null || msg.getReceiver().getId().equals(userId))
                .count();
        long totalRead = messages.stream()
                .filter(MessageEntity::isRead)
                .count();
        long totalUnread = totalMessages - totalRead;

        long uniqueUsers = messages.stream()
                .flatMap(msg -> java.util.stream.Stream.of(msg.getSender().getId(), msg.getReceiver().getId()))
                .distinct()
                .count();
        long avgMessagesPerUser = uniqueUsers > 0 ? totalMessages / uniqueUsers : 0;

        long avgReadRate = totalMessages > 0 ? (totalRead * 100) / totalMessages : 0;

        Map<Long, Long> senderCounts = messages.stream()
                .collect(Collectors.groupingBy(
                        msg -> msg.getSender().getId(),
                        Collectors.counting()
                ));

        Map<Long, Long> receiverCounts = messages.stream()
                .collect(Collectors.groupingBy(
                        msg -> msg.getReceiver().getId(),
                        Collectors.counting()
                ));

        Long mostActiveSenderId = senderCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Long mostActiveReceiverId = receiverCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String userName = null;
        String mostActiveSenderName = null;
        String mostActiveReceiverName = null;

        if (userId != null) {
            UserEntity user = userRepository.findById(userId).orElse(null);
            userName = user != null ? user.getName() : null;
        }

        if (mostActiveSenderId != null) {
            UserEntity sender = userRepository.findById(mostActiveSenderId).orElse(null);
            mostActiveSenderName = sender != null ? sender.getName() : null;
        }

        if (mostActiveReceiverId != null) {
            UserEntity receiver = userRepository.findById(mostActiveReceiverId).orElse(null);
            mostActiveReceiverName = receiver != null ? receiver.getName() : null;
        }

        return MessageStatisticsResponse.builder()
                .date(date)
                .startDate(startDate)
                .endDate(endDate)
                .userId(userId)
                .userName(userName)
                .totalMessages(totalMessages)
                .totalSent(totalSent)
                .totalReceived(totalReceived)
                .totalRead(totalRead)
                .totalUnread(totalUnread)
                .averageMessagesPerUser(avgMessagesPerUser)
                .averageReadRate(avgReadRate)
                .mostActiveSenderId(mostActiveSenderId)
                .mostActiveSenderName(mostActiveSenderName)
                .mostActiveReceiverId(mostActiveReceiverId)
                .mostActiveReceiverName(mostActiveReceiverName)
                .build();
    }
}

