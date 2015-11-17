package cn.com.mobnote.eventbus;

// Event bind finished
public class EventBindFinish {
    int opCode;

    public EventBindFinish(int code) {
        opCode = code;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }

}
