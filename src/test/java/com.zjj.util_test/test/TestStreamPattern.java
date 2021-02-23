package com.zjj.util_test.test;

import com.zjj.common.constants.CommonConstants;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TestStreamPattern {
    public static void main(String[] args) {
        Pattern pattern = CommonConstants.COMMA_SPLIT_PATTERN;
        System.out.println(Arrays.toString(pattern.split("dubbo,,,,,  d dasda,ds, ")));
    }
}
