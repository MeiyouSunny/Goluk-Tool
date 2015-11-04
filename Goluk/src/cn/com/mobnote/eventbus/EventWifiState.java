package cn.com.mobnote.eventbus;

public class EventWifiState {
    int opCode;
    boolean msg;

    public boolean getMsg() {
        return msg;
    }

    public void setMsg(boolean msg) {
        this.msg = msg;
    }

    public EventWifiState(int code, boolean msg) {
        opCode = code;
        this.msg = msg;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }
}
