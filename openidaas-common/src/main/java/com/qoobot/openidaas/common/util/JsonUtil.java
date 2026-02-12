package com.qoobot.openidaas.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 *
 * @author QooBot
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 注册JavaTime模块以支持LocalDateTime等时间类型
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        // 忽略未知属性
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败", e);
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", json, e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * JSON字符串转泛型对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", json, e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * JSON字符串转List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("JSON转List失败: {}", json, e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * JSON字符串转Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON转Map失败: {}", json, e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * 对象深度复制
     */
    public static <T> T deepCopy(T obj, Class<T> clazz) {
        return fromJson(toJson(obj), clazz);
    }

    /**
     * 判断是否为有效的JSON字符串
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 格式化JSON字符串
     */
    public static String formatJson(String json) {
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON格式化失败: {}", json, e);
            return json;
        }
    }
}