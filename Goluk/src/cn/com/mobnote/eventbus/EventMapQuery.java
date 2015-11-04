package cn.com.mobnote.eventbus;

public class EventMapQuery {
    int opCode;

    public EventMapQuery(int code) {
        opCode = code;
    }

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
