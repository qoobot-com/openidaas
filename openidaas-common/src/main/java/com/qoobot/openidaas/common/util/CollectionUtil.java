package com.qoobot.openidaas.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具类
 *
 * @author QooBot
 */
@Slf4j
public class CollectionUtil {

    /**
     * 判断集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return CollectionUtils.isEmpty(collection);
    }

    /**
     * 判断集合是否非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断Map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return CollectionUtils.isEmpty(map);
    }

    /**
     * 判断Map是否非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 安全获取列表的第一个元素
     */
    public static <T> T getFirst(List<T> list) {
        return isEmpty(list) ? null : list.get(0);
    }

    /**
     * 安全获取列表的最后一个元素
     */
    public static <T> T getLast(List<T> list) {
        return isEmpty(list) ? null : list.get(list.size() - 1);
    }

    /**
     * 安全获取列表指定索引的元素
     */
    public static <T> T get(List<T> list, int index) {
        return isEmpty(list) || index < 0 || index >= list.size() ? null : list.get(index);
    }

    /**
     * 将列表转换为Map
     */
    public static <K, V, T> Map<K, V> toMap(List<T> list, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper, (k1, k2) -> k1));
    }

    /**
     * 将列表按指定属性分组
     */
    public static <K, T> Map<K, List<T>> groupBy(List<T> list, Function<T, K> keyMapper) {
        if (isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * 去重
     */
    public static <T> List<T> distinct(List<T> list) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 过滤
     */
    public static <T> List<T> filter(List<T> list, Function<T, Boolean> predicate) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().filter(predicate::apply).collect(Collectors.toList());
    }

    /**
     * 映射转换
     */
    public static <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * 列表转Set
     */
    public static <T> Set<T> toSet(List<T> list) {
        if (isEmpty(list)) {
            return new HashSet<>();
        }
        return new HashSet<>(list);
    }

    /**
     * 合并两个列表
     */
    public static <T> List<T> merge(List<T> list1, List<T> list2) {
        List<T> result = new ArrayList<>();
        if (isNotEmpty(list1)) {
            result.addAll(list1);
        }
        if (isNotEmpty(list2)) {
            result.addAll(list2);
        }
        return result;
    }

    /**
     * 获取两个列表的交集
     */
    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        if (isEmpty(list1) || isEmpty(list2)) {
            return new ArrayList<>();
        }
        Set<T> set = new HashSet<>(list1);
        return list2.stream().filter(set::contains).collect(Collectors.toList());
    }

    /**
     * 获取两个列表的差集
     */
    public static <T> List<T> difference(List<T> list1, List<T> list2) {
        if (isEmpty(list1)) {
            return new ArrayList<>();
        }
        if (isEmpty(list2)) {
            return new ArrayList<>(list1);
        }
        Set<T> set = new HashSet<>(list2);
        return list1.stream().filter(item -> !set.contains(item)).collect(Collectors.toList());
    }

    /**
     * 获取两个列表的并集
     */
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<>();
        if (isNotEmpty(list1)) {
            set.addAll(list1);
        }
        if (isNotEmpty(list2)) {
            set.addAll(list2);
        }
        return new ArrayList<>(set);
    }

    /**
     * 判断列表是否包含指定元素
     */
    public static <T> boolean contains(List<T> list, T item) {
        return isNotEmpty(list) && list.contains(item);
    }

    /**
     * 安全的subList操作
     */
    public static <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        int size = list.size();
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (toIndex > size) {
            toIndex = size;
        }
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return list.subList(fromIndex, toIndex);
    }

    /**
     * 分页获取列表
     */
    public static <T> List<T> getPage(List<T> list, int pageNum, int pageSize) {
        if (isEmpty(list) || pageNum <= 0 || pageSize <= 0) {
            return new ArrayList<>();
        }
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        return safeSubList(list, fromIndex, toIndex);
    }

    /**
     * 计算列表大小
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * 判断两个列表是否相等
     */
    public static <T> boolean equals(List<T> list1, List<T> list2) {
        if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        return list1.containsAll(list2) && list2.containsAll(list1);
    }
}