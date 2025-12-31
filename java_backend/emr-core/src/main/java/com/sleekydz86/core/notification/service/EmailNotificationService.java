package com.sleekydz86.core.notification.service;

import com.sleekydz86.core.notification.template.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationTemplateService templateService;

    @Override
    public void send(String recipient, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(recipient);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailSender.send(mailMessage);
            log.info("이메일 발송 성공: {}", recipient);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", recipient, e);
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }

    @Override
    public void sendWithTemplate(String recipient, String templateId, Map<String, Object> variables) {
        try {

            NotificationTemplate template = templateService.getTemplate(templateId);

            String renderedSubject = template.renderSubject(variables);
            String renderedBody = template.render(variables);

            send(recipient, renderedSubject, renderedBody);
            log.info("템플릿 기반 이메일 발송 성공: {} (템플릿: {})", recipient, templateId);
        } catch (Exception e) {
            log.error("템플릿 기반 이메일 발송 실패: {} (템플릿: {})", recipient, templateId, e);
            throw new RuntimeException("템플릿 기반 이메일 발송 실패", e);
        }
    }
}

