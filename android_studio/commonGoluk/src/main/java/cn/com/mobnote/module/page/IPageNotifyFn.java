package cn.com.mobnote.module.page;

import cn.com.mobnote.logic.IGolukCommFn;

public interface IPageNotifyFn extends IGolukCommFn {

    public static final int PAGE_RESULT_SUCESS = 1;

    /**
     * 页面访问类事件声明,主要用于处理各种页面数据的访问接口及其回调通知等
     * */

    /**
     * 上传视频param1:(上传接口json串信息)，param2:0
     */
    public static int PageType_UploadVideo = 1;
    /**
     * 分享param1:（分析成功结果json串信息），param2:0
     */
    public static int PageType_Share = 2;
    /**
     * 获取评论
     */
    public static int PageType_GetComments = 5;
    /**
     * 评论
     */
    public static int PageType_Comment = 6;
    /**
     * 请求大头针数据
     */
    public static int PageType_GetPinData = 7;
    /**
     * 下载图片
     */
    public static int PageType_GetPictureByURL = 8;
    /**
     * 获取视频详情
     */
    public static int PageType_GetVideoDetail = 9;
    /**
     * 检测升级
     */
    public static int PageType_CheckUpgrade = 10;
    /**
     * 登录
     */
    public static int PageType_Login = 11;
    /**
     * 自动登录
     */
    public static int PageType_AutoLogin = 12;
    /**
     * 获取用户信息
     */
    public static int PageType_GetUserInfo = 13;
    /**
     * 注销
     */
    public static int PageType_SignOut = 14;
    public static int PageType_GetVCode = 15;
    public static int PageType_Register = 16;
    /**
     * 修改密码
     */
    public static int PageType_ModifyPwd = 17;
    /**
     * 主动开启直播
     */
    public static int PageType_LiveStart = 18;
    /**
     * 直播结束
     */
    public static int PageType_LiveStop = 19;
    /**
     * 看别人直播
     */
    public static int PageType_PlayStart = 20;
    /**
     * 停止观看别人直播
     */
    public static int PageType_PlayStop = 21;

    /**
     * 上传视频第一帧图片
     */
    public static final int PageType_LiveUploadPic = 26;
    /**
     * 下载ipc文件
     **/
    public static final int PageType_CommDownloadFile = 27;
    /**
     * 意见反馈
     **/
    public static final int PageType_FeedBack = 28;
    /**
     * 推送注册
     */
    public static final int PageType_PushReg = 29;
    /**
     * 获取推送配置 (是否允许点赞)
     */
    public static final int PageType_GetPushCfg = 30;

    public static final int PageType_SetPushCfg = 31;
    /**
     * 下载IPC文件
     **/
    public static final int PageType_DownloadIPCFile = 32;

    /**
     * 上传用户头像
     **/
    public static final int PageType_ModifyHeadPic = 35;

    public static final int PageType_ModifyNickName = 33;

    public static final int PageType_ModifySignature = 34;
    /**
     * 添加通用头
     **/
    public static final int PageType_AddCommHeader = 36;
    /**
     * 第三方登录
     **/
    public static final int PageType_OauthLogin = 37;
    public static final int PageType_BindInfo = 38;
    public static final int PageType_GetPromotion = 39;
    /**
     * 设置登录应答信息
     */
    public static final int PageType_SetLoginRespInfo = 39;

    public static final int PageType_TagGet = 40;

    /**
     * 我的收益
     **/
    public static final int PageType_MyProfit = 41;
    /**
     * 收益明细
     **/
    public static final int PageType_ProfitDetail = 42;

    /**
     * 活动聚合 推荐
     **/
    public static final int PageType_ClusterRecommend = 43;
    /**
     * 活动聚合 最新
     **/
    public static final int PageType_ClusterNews = 44;
    /**
     * 聚合分享地址
     **/
    public static final int PageType_ClusterShareUrl = 45;
    /* Banner */
    public static final int PageType_BannerGet = 46;
    /**
     * 最新视频点击次数上报
     **/
    public static final int PageType_VideoClick = 47;

    public static final int PageType_VideoDetail = 48;

    public static final int PageType_CommentList = 49;

    public static final int PageType_DelComment = 50;

    public static final int PageType_AddComment = 51;

    public static final int PageType_GetShareURL = 52;

    public static final int PageType_Praise = 53;

    public static final int PageType_VoteShare = 54;

    /**
     * 系统消息
     **/
    public static final int PageType_SystemMsgMain = 55;
    /**
     * 消息盒子——评论
     **/
    public static final int PageType_MsgComment = 56;
    /**
     * 消息盒子——点赞
     **/
    public static final int PageType_MsgPraise = 57;
    /**
     * 消息盒子——消息中心计数
     **/
    public static final int PageType_MsgCounter = 58;
    /**
     * 消息盒子——官方通知
     **/
    public static final int PageType_MsgOfficial = 59;


    public static final int PageType_PraiseCancel = 60;

    public static final int PageType_PraisedList = 61;
    /**
     * 个人主页
     **/
    public static final int PageType_HomeUserInfo = 62;
    /**
     * 个人主页——用户视频分类
     **/
    public static final int PageType_HomeVideoList = 63;
    /**
     * 个人主页——关注 / 取消关注
     **/
    public static final int PageType_HomeAttention = 64;
    /**
     * 分享个人主页
     **/
    public static final int PageType_HomeShare = 65;

    /**
     * 关注内容
     **/
    public static final int PageType_FollowedContent = 66;

    /**
     * 关注
     **/
    public static final int PageType_Follow = 67;

    /**
     * 全部关注
     **/
    public static final int PageType_FollowAll = 68;

    /**
     * 关注的人
     **/
    public static final int PageType_Following = 69;

    /**
     * 粉丝
     */
    public static final int PageType_Fans = 70;

    /**
     * userinfoHome
     */
    public static final int PageType_UserinfoHome = 71;

    /**
     * searchUser
     */
    public static final int PageType_SearchUser = 72;

    /**
     * recommenduser
     */
    public static final int PageType_RecommendUser = 73;

    /**
     * 活动排名列表
     **/
    public static final int PageType_RankingList = 74;

    /**
     * 删除视频
     **/
    public static final int PageType_DeleteVideo = 75;

    /**
     * 用户注册
     **/
    public static final int PageType_InternationalRegister = 76;

    /**
     * 删除视频
     **/
    public static final int PageType_InternationalCheckvcode = 77;

    /**
     * 关注数量
     */
    public static final int PageType_FollowCount = 78;

    /**
     * 获取直播签名
     */
    public static final int PageType_LiveSign = 79;

    /**
     * 上传用户位置
     */
    public static final int PAGE_TYPE_UPLOAD_POSITION = 80;

    /**
     * 请求当前直播是否在线
     */
    public static final int PAGE_TYPE_IS_ALIVE = 81;

    /**
     * 请求直播大头针数据
     */
    public static final int GET_LIVE_POSITION_INFO = 82;

    /**
     * 邮箱注册
     **/
    public static final int REGISTER_BY_EMAIL = 83;

    /**
     * 发送邮箱验证码
     **/
    public static final int SEND_EMAIL_VCODE = 84;

    /**
     * 通过邮箱重置密码
     **/
    public static final int RESET_PWD_BY_EMAIL = 85;

    /**
     * 通过邮箱登录
     */
    public static final int LOGIN_BY_EMAIL = 86;

    /**
     *
     * 以下为同步获取信息标识
     * */

    /**
     * 同步获取登录用户信息命令
     */
    public static final int PageType_GetUserInfo_Get = 0;
    public static final int PageType_GetVersion = 1;
    /**
     * 查询IPC升级文件的存放位置
     **/
    public static final int PageType_GetIPCFile = 2;


    public void pageNotifyCallBack(int type, int success, Object param1, Object param2);

}
