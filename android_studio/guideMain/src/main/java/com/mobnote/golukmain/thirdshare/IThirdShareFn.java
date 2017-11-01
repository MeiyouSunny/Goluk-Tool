package com.mobnote.golukmain.thirdshare;

import android.view.View;

public interface IThirdShareFn {

	/** 新浪微博支持多条消息的值，大于等于字个值，就支持多条消息，否则只支持单条消息 */
	public final int SUPPORT_MUTI_MSG = 10351;
	/** 微信 */
	public static final String TYPE_WEIXIN = "2";
	/** 微博 */
	public static final String TYPE_WEIBO_XINLANG = "3";
	/** QQ */
	public static final String TYPE_QQ = "4";
	/** 微信朋友圈 */
	public static final String TYPE_WEIXIN_CIRCLE = "5";
	/** 短信 */
	public static final String TYPE_SMS = "6";
	/** QQ空间 **/
	public static final String TYPE_QQ_ZONE = "7";
	/** FaceBook **/
	public static final String TYPE_FACEBOOK = "101";
	/** Twitter **/
	public static final String TYPE_TWITTER = "102";
	/** Instagram **/
	public static final String TYPE_INSTAGRAM = "103";
	/** Whatsapp **/
	public static final String TYPE_WHATSAPP = "104";
	/** Line **/
	public static final String TYPE_LINE = "105";
	/** VK **/
	public static final String TYPE_VK = "106";

	public void close();

	// 显示窗口
	public void showAtLocation(View parent, int gravity, int x, int y);

	// 单击 "微信"， “微博 ”, "facebook"
	public void click(String type);

	public void setShareType(String type);

	// 分享回调
	public void CallBack_Share(int event);

}
