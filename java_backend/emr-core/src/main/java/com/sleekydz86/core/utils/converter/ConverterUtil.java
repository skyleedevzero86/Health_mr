package com.sleekydz86.core.utils.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConverterUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            log.error("Map to Object 변환 실패", e);
            return null;
        }
    }

    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            log.error("Object to Map 변환 실패", e);
            return null;
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON to Object 변환 실패", e);
            return null;
        }
    }

    public static String objectToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Object to JSON 변환 실패", e);
            return null;
        }
    }

    public static <T, R> List<R> convertList(List<T> sourceList, Class<R> targetClass) {
        if (sourceList == null) {
            return null;
        }
        List<R> result = new ArrayList<>();
        for (T source : sourceList) {
            try {
                R target = objectMapper.convertValue(source, targetClass);
                result.add(target);
            } catch (Exception e) {
                log.error("리스트 변환 실패", e);
            }
        }
        return result;
    }

    public static <T> T deepCopy(T obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("깊은 복사 실패", e);
            return null;
        }
    }
}

