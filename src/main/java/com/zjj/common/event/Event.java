package com.zjj.common.event;

import java.util.EventObject;

public abstract class Event extends EventObject {

    private static final long serialVersionUID = 3440543325214321172L;
    private final long timestamp;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public Event(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
