package com.sleekydz86.core.security.masking.service;

import com.sleekydz86.core.security.masking.annotation.Sensitive;
import com.sleekydz86.core.security.masking.util.DataMaskingUtil;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.Map;

import static com.sleekydz86.core.security.masking.annotation.Sensitive.MaskingType.*;

@Service
public class DataMaskingService {

    public void maskSensitiveFields(Object obj, boolean applyMasking) {
        if (obj == null || !applyMasking) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Sensitive.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value != null && value instanceof String) {
                        Sensitive annotation = field.getAnnotation(Sensitive.class);
                        String maskedValue = maskValue((String) value, annotation.type());
                        field.set(obj, maskedValue);
                    }
                } catch (IllegalAccessException e) {
                    // 필드 접근 실패 시 무시기능 설계예정
                }
            }
        }
    }

    public void maskMapValue(Map<String, Object> map, String key, Sensitive.MaskingType type) {
        if (map == null || !map.containsKey(key)) {
            return;
        }
        Object value = map.get(key);
        if (value instanceof String) {
            map.put(key, maskValue((String) value, type));
        }
    }

    private String maskValue(String value, Sensitive.MaskingType type) {
        return switch (type) {
            case RRN -> DataMaskingUtil.maskRRN(value);
            case PHONE -> DataMaskingUtil.maskPhone(value);
            case EMAIL -> DataMaskingUtil.maskEmail(value);
            case ACCOUNT -> DataMaskingUtil.maskAccount(value);
            case NAME -> DataMaskingUtil.maskName(value);
            case ADDRESS -> DataMaskingUtil.maskAddress(value);
            default -> DataMaskingUtil.maskDefault(value);
        };
    }
}
