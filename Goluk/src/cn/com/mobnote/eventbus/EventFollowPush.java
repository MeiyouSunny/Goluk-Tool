package cn.com.mobnote.eventbus;

public class EventFollowPush {
	int opCode;

	public EventFollowPush(int code) {
		opCode = code;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
