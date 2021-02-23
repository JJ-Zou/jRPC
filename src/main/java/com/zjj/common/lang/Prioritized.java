package com.zjj.common.lang;

import java.util.Comparator;

public interface Prioritized extends Comparable<Prioritized> {
    Comparator<Object> COMPARATOR = (a, b) -> {
        boolean b1 = a instanceof Prioritized;
        boolean b2 = b instanceof Prioritized;
        if (b1 && !b2) {
            return -1;
        } else if (!b1 && b2) {
            return 1;
        } else if (b1 && b2) {
            return ((Prioritized) a).compareTo((Prioritized) b);
        } else {
            return 0;
        }
    };

    int MIN_PRIORITY = Integer.MIN_VALUE;
    int MAX_PRIORITY = Integer.MAX_VALUE;
    int NORMAL_PRIORITY = 0;

    default int getPriority() {
        return NORMAL_PRIORITY;
    }

    @Override
    default int compareTo(Prioritized o) {
        return Integer.compare(this.getPriority(), o.getPriority());
    }
}
