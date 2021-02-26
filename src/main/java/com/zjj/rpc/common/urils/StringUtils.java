package com.zjj.rpc.common.urils;

public class StringUtils {
    private StringUtils() {
    }

    public static String classNameToCamelName(String name) {
        if (Character.isLowerCase(name.charAt(0))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public static String camelToSplitName(String camelName, String split) {
        if (camelName == null || camelName.isEmpty()) {
            return camelName;
        }
        camelName = classNameToCamelName(camelName);
        StringBuilder buf = new StringBuilder();
        for (char c : camelName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                buf.append(split).append(Character.toLowerCase(c));
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }


    public static String calculateAttributeFromGetter(String getter) {
        int i = getter.startsWith("get") ? 3 : 2;
        return getter.substring(i, i + 1).toLowerCase() + getter.substring(i + 1);
    }

    public static void main(String[] args) {
        System.out.println(camelToSplitName("stringUtils", "_"));
    }
}
