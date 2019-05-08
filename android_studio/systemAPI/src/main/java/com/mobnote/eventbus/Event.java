package com.mobnote.eventbus;

public class Event<T> {

    /* 消息类型 */
    public int type;
    /* 消息数据 */
    public T data;

    private Event(int type, T data) {
        this.type = type;
        this.data = data;
    }

    public static <T> Event create(int type) {
        return new Event(type, null);
    }

    public static <T> Event create(int type, T data) {
        return new Event(type, data);
    }

}
