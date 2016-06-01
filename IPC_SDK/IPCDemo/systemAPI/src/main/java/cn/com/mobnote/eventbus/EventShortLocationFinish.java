package cn.com.mobnote.eventbus;

public class EventShortLocationFinish {

	String shortAddress;
	public EventShortLocationFinish(String address){
		this.shortAddress = address;
	}
	public String getShortAddress() {
		return shortAddress;
	}
	public void setShortAddress(String shortAddress) {
		this.shortAddress = shortAddress;
	}
	
}
