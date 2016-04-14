package com.mobnote.eventbus;

public class EventRefreshUserInfo {
    int opCode;

    public EventRefreshUserInfo(int code) {
        opCode = code;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }
}
