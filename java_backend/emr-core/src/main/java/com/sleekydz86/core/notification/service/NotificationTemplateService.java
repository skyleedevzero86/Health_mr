package com.sleekydz86.core.notification.service;

import com.sleekydz86.core.notification.template.NotificationTemplate;
import com.sleekydz86.core.notification.type.TemplateType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationTemplateService {

    private final Map<String, NotificationTemplate> templates = new HashMap<>();

    public NotificationTemplateService() {

        initializeDefaultTemplates();
    }

    private void initializeDefaultTemplates() {

        registerTemplate(new NotificationTemplate(
                "password-reset",
                "비밀번호 재설정",
                "안녕하세요 ${userName}님,\n\n비밀번호 재설정을 요청하셨습니다.\n\n인증 코드: ${code}\n\n이 코드는 10분간 유효합니다.\n\n감사합니다.",
                TemplateType.EMAIL
        ));

        registerTemplate(new NotificationTemplate(
                "email-verification",
                "이메일 인증",
                "안녕하세요 ${userName}님,\n\n이메일 인증을 요청하셨습니다.\n\n인증 코드: ${code}\n\n이 코드는 10분간 유효합니다.\n\n감사합니다.",
                TemplateType.EMAIL
        ));

        registerTemplate(new NotificationTemplate(
                "reservation-created",
                "예약이 생성되었습니다",
                "안녕하세요 ${patientName}님,\n\n예약이 성공적으로 생성되었습니다.\n\n예약 일시: ${reservationDate}\n예약 담당자: ${staffName}\n\n감사합니다.",
                TemplateType.EMAIL
        ));

        registerTemplate(new NotificationTemplate(
                "prescription-completed",
                "처방이 완료되었습니다",
                "안녕하세요 ${patientName}님,\n\n처방이 완료되었습니다.\n\n처방 일시: ${prescriptionDate}\n처방 의사: ${doctorName}\n\n감사합니다.",
                TemplateType.EMAIL
        ));
    }

    public void registerTemplate(NotificationTemplate template) {
        templates.put(template.getTemplateId(), template);
        log.debug("템플릿이 등록되었습니다: {}", template.getTemplateId());
    }

    public NotificationTemplate getTemplate(String templateId) {
        NotificationTemplate template = templates.get(templateId);
        if (template == null) {
            log.warn("템플릿을 찾을 수 없습니다: {}", templateId);
            throw new IllegalArgumentException("템플릿을 찾을 수 없습니다: " + templateId);
        }
        return template;
    }

    public boolean hasTemplate(String templateId) {
        return templates.containsKey(templateId);
    }
}

