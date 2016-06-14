package com.mobnote.util;

public class GolukConfig {
	/** 腾讯云 appid */
	public static final int QQWNS_APPID = 202066;

	/** Request code for message box */
	public final static int REQUEST_CODE_MSG_LOGIN_PRAISE = 1009;
	public final static int REQUEST_CODE_MSG_LOGIN_COMMENT = 1010;
	public final static int REQUEST_CODE_MSG_LOGIN_SYSTEM = 1011;
	public final static int REQUEST_CODE_MSG_LOGIN_SETTING = 1012;

	public final static int REQUEST_CODE_VIDEO_SYNC_SETTING = 1013;
	/**
	 * Important, this code for facebook share callback, used in BaseActivity
	 */
	public final static int REQUEST_CODE_FACEBOOK_SHARE = 1111;

	public final static String STRING_VIDEO_SYNC_SETTING_VALUE = "video_sync_setting_value";

	/** 微信 appid secret **/
	public static final String WX_APPID = "wxd437f1e0f67dfac1";
	public static final String WX_APPSECRET = "90d4bc4ae1b3dba372dd72d03cc8e82d";
	/** QQ appid secret **/
	public static final String QQ_APPID = "1104418156";
	public static final String QQ_APPKEY = "G7OfQ0qbqe5OJlUP";
	public static final String SHARE_DESCRIPTOR = "com.umeng.share";
	public static final String LOGIN_DESCRIPTOR = "com.umeng.login";

	public static final String WEB_TYPE = "web_type";
	public static final String NEED_H5_TITLE = "need_h5_title";
	public static final String URL_OPEN_PATH = "url_open_path";
	public static final String NEED_SHARE = "need_share";
	public static final String H5_URL = "url";
	public static final String NEED_SHARE_PICTURE = "need_share_picture";
	public static final String NEED_SHARE_INTRO = "need_share_intro";
	public static final String NEED_SHARE_ID = "need_share_id";

	/** instagram包名 **/
	public final static String INSTAGRAM_PACKAGE = "com.instagram.android";
	/** instagram类名 **/
	public final static String INSTAGRAM_CLASS = "com.instagram.android.activity.ShareHandlerActivity";

	/** Whatsapp package name **/
	public final static String WTATSAPP_PACKAGE = "com.whatsapp";

	/** Line package name **/
	public final static String LINE_PACKAGE = "jp.naver.line.android";
	
	/**第三方登陆默认密码**/
	public final static String OTHER_PASSWORD = "lovegoluk@2016";

	/**跳转登陆时的timer**/
	public final static int CLOSE_ACTIVITY_TIMER = 300;

}
