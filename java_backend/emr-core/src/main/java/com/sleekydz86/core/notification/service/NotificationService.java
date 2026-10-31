package com.sleekydz86.core.notification.service;

import java.util.Map;

public interface NotificationService {

    void send(String recipient, String subject, String message);

    void sendWithTemplate(String recipient, String templateId, Map<String, Object> variables);
}
