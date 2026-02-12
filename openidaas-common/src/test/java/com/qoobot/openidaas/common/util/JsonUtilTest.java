package com.qoobot.openidaas.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON工具类测试
 *
 * @author QooBot
 */
class JsonUtilTest {

    @Test
    void testToJson() {
        // 测试对象转JSON
        TestUser user = new TestUser(1L, "张三", 25, LocalDateTime.now());
        String json = JsonUtil.toJson(user);
        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"张三\""));
    }

    @Test
    void testFromJson() {
        // 测试JSON转对象
        String json = "{\"id\":1,\"name\":\"张三\",\"age\":25}";
        TestUser user = JsonUtil.fromJson(json, TestUser.class);
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("张三", user.getName());
        assertEquals(25, user.getAge());
    }

    @Test
    void testFromJsonToList() {
        // 测试JSON转List
        String json = "[{\"id\":1,\"name\":\"张三\",\"age\":25},{\"id\":2,\"name\":\"李四\",\"age\":30}]";
        List<TestUser> users = JsonUtil.fromJsonToList(json, TestUser.class);
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("张三", users.get(0).getName());
        assertEquals("李四", users.get(1).getName());
    }

    @Test
    void testFromJsonToMap() {
        // 测试JSON转Map
        String json = "{\"name\":\"张三\",\"age\":25,\"active\":true}";
        Map<String, Object> map = JsonUtil.fromJsonToMap(json);
        assertNotNull(map);
        assertEquals("张三", map.get("name"));
        assertEquals(25, map.get("age"));
        assertEquals(true, map.get("active"));
    }

    @Test
    void testIsValidJson() {
        // 测试JSON有效性验证
        assertTrue(JsonUtil.isValidJson("{\"name\":\"张三\"}"));
        assertFalse(JsonUtil.isValidJson("{invalid json}"));
        assertFalse(JsonUtil.isValidJson(""));
        assertFalse(JsonUtil.isValidJson(null));
    }

    @Test
    void testFormatJson() {
        // 测试JSON格式化
        String json = "{\"name\":\"张三\",\"age\":25}";
        String formatted = JsonUtil.formatJson(json);
        assertNotNull(formatted);
        assertNotEquals(json, formatted);
        assertTrue(formatted.contains("\n"));
    }

    @Test
    void testDeepCopy() {
        // 测试对象深度复制
        TestUser original = new TestUser(1L, "张三", 25, LocalDateTime.now());
        TestUser copy = JsonUtil.deepCopy(original, TestUser.class);
        assertNotNull(copy);
        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getAge(), copy.getAge());
        assertNotSame(original, copy); // 确保是不同的对象实例
    }

    /**
     * 测试用户类
     */
    public static class TestUser {
        private Long id;
        private String name;
        private Integer age;
        private LocalDateTime createTime;

        public TestUser() {}

        public TestUser(Long id, String name, Integer age, LocalDateTime createTime) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.createTime = createTime;
        }

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
}