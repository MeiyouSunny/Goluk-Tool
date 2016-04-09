package com.mobnote.eventbus;

public class EventUserLoginRet {
	int opCode;
	//Login success or fail
	boolean ret;
	int followedVideoNum;

	public EventUserLoginRet(int code, boolean ret, int num) {
		opCode = code;
		this.ret = ret;
		followedVideoNum = num;
	}

	public int getFollowedVideoNum() {
		return followedVideoNum;
	}

	public void setFollowedVideoNum(int followedVideoNum) {
		this.followedVideoNum = followedVideoNum;
	}

	public boolean getRet() {
		return ret;
	}

	public void setRet(boolean ret) {
		this.ret = ret;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
