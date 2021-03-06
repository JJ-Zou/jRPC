package com.zjj.clutter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestClutter {
    @Test
    public void retailAll() {
        List<Integer> l1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Integer> l2 = new ArrayList<>(Arrays.asList(1, 2, 5, 8, 9));
        l1.removeAll(l2);
        System.out.println(l1);
    }

    @Test
    public void name() {
        ThreadLocal<List<Integer>> threadLocal = ThreadLocal.withInitial(ArrayList::new);
        List<Integer> list = threadLocal.get();
        a(list);
        System.out.println(list);
    }

    private void a(List<Integer> list) {
        list.clear();
        list.add(1);
    }
}
