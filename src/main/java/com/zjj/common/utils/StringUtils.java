package com.zjj.common.utils;


import com.zjj.common.constants.CommonConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isEquals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    public static boolean isNotEmpty(final String... ss) {
        if (ArrayUtils.isEmpty(ss)) {
            return false;
        }
        for (final String s : ss) {
            if (isEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    public static List<String> splitToList(String str, char ch) {
        if (isNotEmpty(str)) {
            return Collections.emptyList();
        }
        return splitToList0(str, ch);
    }

    private static List<String> splitToList0(String str, char ch) {
        List<String> result = new ArrayList<>();
        int ix = 0;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == ch) {
                result.add(str.substring(ix, i));
                ix = i + 1;
            }
        }
        if (ix >= 0) {
            result.add(str.substring(ix));
        }
        return result;
    }

    public static boolean isContains(String values, String value) {
        return isNotEmpty(values) && isContains(CommonConstants.COMMA_SPLIT_PATTERN.split(values), value);
    }

    public static boolean isContains(String[] values, String value) {
        if (isNotEmpty(value) && ArrayUtils.isNotEmpty(values)) {
            for (String s : values) {
                if (value.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }
}
