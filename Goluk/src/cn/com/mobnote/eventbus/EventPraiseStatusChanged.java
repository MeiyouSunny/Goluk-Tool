package cn.com.mobnote.eventbus;

// Praise status sync between video detail and each video list

public class EventPraiseStatusChanged {
	int opCode;
	String videoId;
	// true for praised, false for cancel praised
	boolean status;

	public EventPraiseStatusChanged(int code, String videoId, boolean status) {
		opCode = code;
		this.videoId = videoId;
		this.status = status;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
}
