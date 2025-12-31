package com.sleekydz86.core.notification.service;

public interface NotificationService {

    void send(String recipient, String subject, String message);

    void sendWithTemplate(String recipient, String templateId, java.util.Map<String, Object> variables);
}
