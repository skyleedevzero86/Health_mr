package com.sleekydz86.core.event.publisher;

import com.sleekydz86.core.event.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(DomainEvent event) {
        validateEvent(event);
        try {
            applicationEventPublisher.publishEvent(event);
            log.debug("이벤트 발행 성공: {} [ID: {}]", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            log.error("이벤트 발행 실패: {} [ID: {}]", event.getEventType(), event.getEventId(), e);
            if (event.isCritical()) {
                throw new EventPublishException("중요 이벤트 발행 실패: " + event.getEventType(), e);
            }
        }
    }

    @Async
    public void publishAsync(DomainEvent event) {
        publish(event);
    }


    private void validateEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("이벤트는 null일 수 없습니다.");
        }
        if (event.getEventId() == null) {
            throw new IllegalArgumentException("이벤트 ID는 필수입니다.");
        }
        if (event.getEventType() == null || event.getEventType().isBlank()) {
            throw new IllegalArgumentException("이벤트 타입은 필수입니다.");
        }
        if (event.getOccurredAt() == null) {
            throw new IllegalArgumentException("이벤트 발생 시간은 필수입니다.");
        }
    }

    public static class EventPublishException extends RuntimeException {
        public EventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

