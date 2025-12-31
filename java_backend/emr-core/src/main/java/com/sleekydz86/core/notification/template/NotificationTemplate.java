package com.sleekydz86.core.notification.template;

import com.sleekydz86.core.notification.type.TemplateType;
import java.util.Map;

public class NotificationTemplate {

    private String templateId;
    private String subject;
    private String body;
    private TemplateType type;

    public NotificationTemplate(String templateId, String subject, String body, TemplateType type) {
        this.templateId = templateId;
        this.subject = subject;
        this.body = body;
        this.type = type;
    }

    public String render(Map<String, Object> variables) {
        String renderedSubject = renderString(subject, variables);
        String renderedBody = renderString(body, variables);

        return renderedBody;
    }

    public String renderSubject(Map<String, Object> variables) {
        return renderString(subject, variables);
    }

    private String renderString(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    // Getters
    public String getTemplateId() {
        return templateId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public TemplateType getType() {
        return type;
    }

}

