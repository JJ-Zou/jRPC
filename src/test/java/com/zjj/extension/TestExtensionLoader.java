package com.zjj.extension;

import org.junit.Test;

import java.util.List;

public class TestExtensionLoader {
    @Test
    public void loadArrayList() {
        List array = ExtensionLoader.getExtensionLoader(List.class).getExtension("array");
        System.out.println(array);
    }
}
