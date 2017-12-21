package com.mobnote.util;

public class GolukConfig {
    /**
     * 腾讯云 appid
     */
    public static final int QQWNS_APPID = 202066;

    /**
     * Request code for message box
     */
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

    /**
     * 微信国内版 appid secret
     **/
    public static final String WX_APPID_MAINLAND = "wxd437f1e0f67dfac1";
    public static final String WX_APPSECRET_MAINLAND = "1badfa5951ca39788aeb58eddfc0db71";

    /**
     * 微信国际版 appid secret
     **/
    public static final String WX_APPID_INTERNATIONAL = "wx493f46bf1a71416f";
    public static final String WX_APPSECRET_INTERNATIONAL = "b572ec9cbd3fac52e138e34eff0b4926";

    /**
     * QQ国内版 appid secret
     **/
    public static final String QQ_APPID_MAINLAND = "1104418156";
    public static final String QQ_APPKEY_MAINLAND = "G7OfQ0qbqe5OJlUP";

    /**
     * QQ国际版 appid secret
     **/
    public static final String QQ_APPID_INTERNATIONAL = "1105266664";
    public static final String QQ_APPKEY_INTERNATIONAL = "atVlUoZ9yo4nH8wn";

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

    /**
     * instagram包名
     **/
    public final static String INSTAGRAM_PACKAGE = "com.instagram.android";
    /**
     * instagram类名
     **/
    public final static String INSTAGRAM_CLASS = "com.instagram.android.activity.ShareHandlerActivity";

    /**
     * Whatsapp package name
     **/
    public final static String WTATSAPP_PACKAGE = "com.whatsapp";

    /**
     * Line package name
     **/
    public final static String LINE_PACKAGE = "jp.naver.line.android";

    /**
     * VK package name
     */
    public final static String VK_PACKAGE = "com.vkontakte.android";

    /**
     * 第三方登陆默认密码
     **/
    public final static String OTHER_PASSWORD = "lovegoluk@2016";

    /**
     * 跳转登陆时的timer
     **/
    public final static int CLOSE_ACTIVITY_TIMER = 300;


    /**
     * common server ret code
     */
    public final static int SERVER_RESULT_OK = 0;
    public final static int SERVER_TOKEN_EXPIRED = 10001;
    public final static int SERVER_TOKEN_INVALID = 10002;
    public final static int SERVER_TOKEN_DEVICE_INVALID = 10003;
    public final static String SERVER_PROTOCOL_V1 = "100";
    public final static String SERVER_PROTOCOL_V2 = "200";

    /* List pull or refresh operation constants */
    public final static String LIST_REFRESH_NORMAL = "0";
    public final static String LIST_REFRESH_PULL_DOWN = "1";
    public final static String LIST_REFRESH_PULL_UP = "2";
}
