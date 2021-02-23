package com.zjj.common.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionUtils {
    private CollectionUtils() {

    }

    public static Map<String, String> toStringMap(String... pairs) {
        Map<String, String> parameters = new HashMap<>();
        if (ArrayUtils.isEmpty(pairs)) {
            return parameters;
        }
        if (pairs.length > 0) {
            int len = pairs.length;
            if ((len & 1) != 0) {
                throw new IllegalArgumentException("pairs must be even.");
            }
            for (int i = 0; i < len; i += 2) {
                parameters.put(pairs[i], pairs[i + 1]);
            }
        }
        return parameters;
    }

    public static boolean isNotEmptyMap(Map map) {
        return !isEmptyMap(map);
    }

    public static boolean isEmptyMap(Map map) {
        return map == null || map.size() == 0;
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
