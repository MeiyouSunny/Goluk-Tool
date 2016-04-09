package com.mobnote.eventbus;

public class EventIPCUpdate {
    int opCode;

    public EventIPCUpdate(int code) {
        opCode = code;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }
}