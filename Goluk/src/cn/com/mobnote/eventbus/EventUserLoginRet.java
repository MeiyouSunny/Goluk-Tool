package cn.com.mobnote.eventbus;

public class EventUserLoginRet {
	int opCode;
	//Login success or fail
	boolean ret;

	public boolean getRet() {
		return ret;
	}

	public void setRet(boolean ret) {
		this.ret = ret;
	}

	public EventUserLoginRet(int code, boolean ret) {
		opCode = code;
		this.ret = ret;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
