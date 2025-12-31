package com.sleekydz86.core.security.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        EncryptedStringConverter.applicationContext = applicationContext;
    }

    private EncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext가 초기화되지 않았습니다.");
        }
        return applicationContext.getBean(EncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        try {
            return getEncryptionService().encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        try {
            return getEncryptionService().decrypt(dbData);
        } catch (Exception e) {
            // 복호화 실패 시 원본 반환 처리예정
            // 이미 암호화되지 않은 데이터 확인하기
            return dbData;
        }
    }
}

