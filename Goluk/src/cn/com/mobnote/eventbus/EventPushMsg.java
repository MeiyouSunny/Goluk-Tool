package cn.com.mobnote.eventbus;

import cn.com.mobnote.golukmobile.xdpush.XingGeMsgBean;

/**
 * 消息推送EventBus类
 * 
 * @author jyf
 */
public class EventPushMsg {
	/** 消息 */
	private int opCode;
	/** 消息实体类 */
	private XingGeMsgBean xinGeMsgBean;

	public EventPushMsg(int code, XingGeMsgBean bean) {
		opCode = code;
		xinGeMsgBean = bean;
	}

	public int getOpCode() {
		return opCode;
	}

	public XingGeMsgBean getXinGeMsgBean() {
		return xinGeMsgBean;
	}

}
