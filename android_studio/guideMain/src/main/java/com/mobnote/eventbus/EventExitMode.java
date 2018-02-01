package com.mobnote.eventbus;

/**
 * 小白退出模式
 */
public class EventExitMode {

    public int type;

    public EventExitMode(int type) {
        this.type = type;
    }

    public boolean isSetMode() {
        return type == 1;
    }

    public boolean isPlaybackMode() {
        return type == 2;
    }

}
