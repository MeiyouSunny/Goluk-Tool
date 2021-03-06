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


    public static final String WEB_TYPE = "web_type";
    public static final String NEED_H5_TITLE = "need_h5_title";
    public static final String URL_OPEN_PATH = "url_open_path";
    public static final String NEED_SHARE = "need_share";
    public static final String H5_URL = "url";
    public static final String NEED_SHARE_PICTURE = "need_share_picture";
    public static final String NEED_SHARE_INTRO = "need_share_intro";
    public static final String NEED_SHARE_ID = "need_share_id";



    /** 国内版sina微博APP_KEY */
    public static final String SINA_APP_KEY_MAINLAND = "3570996032";
    public static final String SINA_APP_SECRET_MAINLAND = "79cd95f1da313901d743de78469861c4";

    /** 国际版sina微博APP_KEY */
    public static final String SINA_APP_KEY_INTERNATIONAL = "3481905571";
    public static final String SINA_APP_SECRET_INTERNATIONAL = "c67e90430b9d8baa581d794431811329";

    /**
     * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
     *
     * <p>
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     * </p>
     */
    public static final String REDIRECT_URL = "http://sns.whalecloud.com/sina2/callback";

    /**
     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
     * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
     * 选择赋予应用的功能。
     *
     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
     * 使用权限，高级权限需要进行申请。
     *
     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
     *
     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
     * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
     */
    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";
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
