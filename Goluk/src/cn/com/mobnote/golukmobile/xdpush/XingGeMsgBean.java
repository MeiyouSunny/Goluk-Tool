package cn.com.mobnote.golukmobile.xdpush;

/**
 * 信鸽下发的消息体
 * 
 * @author jyf
 */
public class XingGeMsgBean {
	/** 通知id, 主要用于在状态栏显示用, 目前的值使用本地的当前时间 */
	public int notifyId;
	/***/
	public int action;
	public String title;
	public String msg;

}
