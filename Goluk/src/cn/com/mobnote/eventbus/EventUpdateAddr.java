package cn.com.mobnote.eventbus;

public class EventUpdateAddr {
    int opCode;
    String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public EventUpdateAddr(int code, String msg) {
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
