package cn.com.tiros.airtalkee;

import com.airtalkee.sdk.OnAccountListener;
import com.airtalkee.sdk.OnChannelListener;
//import com.airtalkee.sdk.OnChannelSessionListener;
import com.airtalkee.sdk.OnContactListener;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.OnMediaWaveListener;
import com.airtalkee.sdk.OnMessageListener;
import com.airtalkee.sdk.OnSessionIncomingListener;
import com.airtalkee.sdk.OnSessionListener;
import com.airtalkee.sdk.OnSystemListener;
import com.airtalkee.sdk.OnMediaAudioControlRecordListener;

public interface TaxiAirTalkeeFn extends OnSessionListener, OnMediaListener, OnContactListener,
		OnSessionIncomingListener, OnAccountListener, OnMessageListener, OnChannelListener, OnMediaWaveListener,
		OnSystemListener,OnMediaAudioControlRecordListener {

	public static final String JSON_STATE = "state";
	public static final String JSON_ONLINECOUNT = "onlinecount";
	public static final String JSON_MEMBERCOUNT = "allcount";
	public static final String JSON_AID = "aid";
	public static final String JSON_VOLUME = "volume";
	public static final String JSON_SUCCESS = "success";
	public static final String JSON_MSGID = "msgid";
	public static final String JSON_CHANNELID = "channelID";
	public static final String JSON_RESID = "resid";
	public static final String JSON_TIME = "time";
	public static final String JSON_MSG = "msg";
	public static final String JSON_GROUPID = "groupid";
	public static final String JSON_MSGTYPE = "msgtype";
	public static final String JSON_DATE = "date";
	public static final String JSON_REASON = "reason";
	public static final String JSON_MEMBERS = "members";
	public static final String JSON_ISME = "isme";

	public static final int AIRTALKEE_LOGOUT = 8000;//注销
	public static final int AIRTALKEE_LOGIN = 8001;// 登录
	public static final int AIRTALKEE_HEART = 8002; // 心跳
	public static final int AIRTALKEE_ONLINECOUNT = 8003;// 所有群组的在线人数
	public static final int AIRTALKEE_RINGING = 8004;// 刷新会话状态---8004
	public static final int AIRTALKEE_PRESENCE = 8005;// 刷新当前群在线人数---8005
	public static final int AIRTALKEE_ALERTSTART = 8006;// 临时会话来电提醒---8006
	public static final int AIRTALKEE_ALERTSTOP = 8007;// 临时会话来电提醒结束事件---8007
	public static final int AIRTALKEE_TALKSTART = 8008;// 本人申请到话语权---8008
	public static final int AIRTALKEE_TALKEND = 8009;// 本人释放话语权---8009
	public static final int AIRTALKEE_TALKFAILD = 8010;// 本人申请功能话语权失败---8010
	public static final int AIRTALKEE_TALKQUEUE = 8019;//本人申请话语权排队----8019
	public static final int AIRTALKEE_OTHERTALKSTART = 8011;// 别人开始讲话---8011
	public static final int AIRTALKEE_OTHERTALKEND = 8012;// 别人讲话结束---8012
	public static final int AIRTALKEE_VALUMECHANGE = 8013;// 音频强度---8013
	public static final int AIRTALKEE_MESSAGEERC = 8014;// 接受自定义消息---8014
	public static final int AIRTALKEE_MESSAGESENTTYPE = 8015;// 发送自定义消息---8015
	public static final int AIRTALKEE_SYSTEMCUSTOMPUSH = 8016;// 收到push消息---8016
	public static final int AIRTALKEE_MEDIABUTTON = 8017;// 刷新说话按钮的状态---8017
	public static final int AIRTALKEE_ESTABLISH_STATUS = 8018;// 临时或群组会话状态事件---8018
	public static final int AIRTALKEE_MEMBERLIST = 8020;// 获取成员列表
	public static final int AIRTALKEE_RECORD = 8021;//录音相关
	public static final int AIRTALKEE_AUDIOPLAY = 8022;//播放语音相关
	public static final int AIRTALKEE_OFFLINEMESSAGE = 8023;//接受离线消息
	public static final int AIRTALKEE_SYSTEMCUSTONPUSHLIST = 8024;//接受多条离线消息
	public static final int AIRTALKEE_RECORDPLAYLOADED = 8025; //要播放的语音下载完成
	//新增频道群组禁言回调通知
	public static final int AIRTALKEE_CHANNELSESSIONSHUTUP = 8030; //群组频道本人被禁言
	//新增本人申请说话结束的集中状态通知：
	public static final int AIRTALKEE_TALKEND_REASON_GRABED = 8041;//本人话语权被更高级别用户抢断
	public static final int AIRTALKEE_TALKEND_REASON_TIMEOUT = 8042;//本人申请话语权超时,未申请上话语 权而结束
	public static final int AIRTALKEE_TALKEND_REASON_TIMEUP = 8043;//本人发言时长已到,服务器自动终 止用户的发言
	public static final int AIRTALKEE_TALKEND_REASON_EXCEPTION = 8044;//由于用户网络原因,连续间隔 一定时间服务器依然未能收到用户的通话数据包,而产生的异常,从而自 动收回用户的话语权
	public static final int AIRTALKEE_TALKEND_REASON_LISTEN_ONLY = 8045;//本人仅有只听权限,不允许发言
	public static final int AIRTALKEE_TALKEND_REASON_SPEAKING_FULL = 8046;//本人因服务器不允许排队或者排队已满而被终止发言
	
	/**
	 * ----------------------------帐户相关的操作--------------------------------------
	 * -
	 * */
	public void create(int _mairTalkeeHandler, String ip);
	
	/**
	 * @brief 扩展配置AirTalkee服务参数
	 * @param[in] pAirTalkee - AirTalkee对象结构体指针
	 * @param[in] spIp - 连接愛淘客服务器地址
	 * @param[in] spPort - 连接愛淘客服务器端口
	 * @param[in] spLocalPort - 连接愛淘客服务器本地端口
	 * @param[in] mdsrIp - mdsr地址
	 * @param[in] mdsrPort - mdsr端口
	 * @return - 无
	 */
	public void configserver(String spIp, int spPort, int spLocalPort, String mdsrIp, int mdsrPort);

	// 销毁本模块
	public void destroy();

	// 登录
	public void login(String _userId, String _password);

	// 退出登录
	public void logout();

	// 自定义消息UDP上报(见面功能使用)
	public void systemCusromReport(String _updData);

	/**
	 * -------------------------------群组相关的操作----------------------------------
	 * --
	 * */

	// 从服务器取所有群组的在线人数(仅获取一次)
	public void channelOnLineCountGet();

	// 从服务器周期性的获取所有群组的在线人数(间隔秒数)
	public void channelOnLineCountStart(int _timeSeconds);

	// 停止获取所有群的在线人数
	public void channelOnLineCountGetStop();

	// 群组管理员　设置某人禁音30分钟
	public void channelSessionManageShutup(String _userid);

	// 群组管理员踢出组员 30分钟
	public void channelSessionManageKickout(String _userid);

	// 群成员自己设置静音 true为静音 false为非静音
	public void channelSessionSetSilence(boolean isSlient);

	// 获得指定群组的成员列表
	public void channelMemberGet(String _channelId);

	/**
	 * -------------------------------------------------------------------
	 * */

	// 呼叫群组或个人
	public void sessionCall(boolean isGroup, String _channelId);

	// 结束指定的临时呼叫,或取消正在呼出的临时呼叫,或退出群组会话
	public void sessionBye();

	// 接听临时呼叫来电
	public void sessionIncomingAccept();

	// 拒绝临时来电;　
	public void sessionIncomingReject();

	// 在当前会话中申请话语权
	public void talkRequest();

	// 在当前会话中释放已拥有的话语权
	public void talkRelease();

	/**
	 * -------------------------------------------------------------------
	 * */
	
	/**
	* @brief 发送自定义消息
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] messageBody - 自定义消息体
	* @param[in] isCustom - true:自定义消息；
	* @return - msgID - char*:消息唯一id
	*/
	public String messageSend1(String _messageBody, boolean isCustom, boolean allowOfflineSend);

	/**
	* @brief 给多个用户发送自定义消息
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] aidlist - 用户队列
	* @param[in] messageBody - 自定义消息体
	* @param[in] isCustom - true:自定义消息；
	* @return - msgID - char*:消息唯一id
	*/
	public String messageSend2(String aidlist, String messageBody,  boolean isCustom, boolean allowOfflineSend);
	
	/**
	* @brief 给单个用户发送自定义消息
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] aid - 指定用户
	* @param[in] messageBody - 自定义消息体
	* @param[in] isCustom - true:自定义消息；
	* @return - msgID - char*:消息唯一id
	*/
	public String messageSend3(String aid, String messageBody, boolean isCustom, boolean allowOfflineSend);
	
	/**
	* @brief 给指定群组发送自定义消息
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] groupid - 指定群组id
	* @param[in] messageBody - 自定义消息体
	* @param[in] isCustom - true:自定义消息；
	* @return - msgID - char*:消息唯一id
	*/
	public String messageSend4(String groupid, String messageBody, boolean isCustom, boolean allowOfflineSend);
	
	/**
	* @brief 设置本地语音文件存储路径
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] path - 语音文件存储路径
	* @return - 无
	*/
	public void setMessageRecordPath(String path);
	
	/**
	* @brief 给当前群组频道开始语音录制
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @return - 无
	*/
	public void messageRecordStart1(boolean allowOfflineSend);
	
	/**
	* @brief 给多个用户开始语音录制
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] aidlist - 用户队列
	* @return - 无
	*/
	public void messageRecordStart2(String aidlist, boolean allowOfflineSend);
	
	/**
	* @brief 给指定用户开始语音录制
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] aid - 指定用户
	* @return - 无
	*/
	public void messageRecordStart3(String aid, boolean allowOfflineSend);

	/**
	* @brief 给指定群组开始语音录制
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] groupid - 指定群组id
	* @return - 无
	*/
	public void messageRecordStart4(String groupid, boolean allowOfflineSend);
	
	/**
	* @brief 停止语音录制
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] iscancel - true:正常结束开始语音发送，false为：取消刚录制的语音
	* @return - 无
	*/
	public void messageRecordStop(boolean iscancel);
	
	/**
	* @brief 给多个用户开始重发语音数据
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] aidlist - 用户队列
	* @param[in] msgid - 消息编码
	* @param[in] resid - 语音文件资源编码
	* @param[in] time - 语音文件时长
	* @return - msgID - char*:消息唯一id
	*/
	public String messageRecordResend1(String aidlist, String msgid, String resid, int time, boolean allowOfflineSend);
	
	/**
	* @brief 给单个用户开始重发语音数据
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] aid - 单个用户
	* @param[in] msgid - 消息编码
	* @param[in] resid - 语音文件资源编码
	* @param[in] time - 语音文件时长
	* @return - msgID - char*:消息唯一id
	*/
	public String messageRecordResend2(String aid, String msgid, String resid, int time, boolean allowOfflineSend);
	
	
	/**
	* @brief 给指定群组开始重发语音数据
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] groupid - 单个用户
	* @param[in] msgid - 消息编码
	* @param[in] resid - 语音文件资源编码
	* @param[in] time - 语音文件时长
	* @return - msgID - char*:消息唯一id
	*/
	public String messageRecordResend3(String groupid, String msgid, String resid, int time, boolean allowOfflineSend);
	
	/**
	* @brief 根据msgid，resid启动播放指定语音文件
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @param[in] msgid - 消息编码
	* @param[in] resid - 语音文件资源编码
	* @return - 无
	*/
	public void messageRecordPlayStart(String msgid, String resid);
	
	/**
	* @brief 停止正在播放的语音文件
	* @param[in] pAirTalkee - AirTalkee对象结构体指针
	* @return - 无
	*/
	public void messageRecordPlayStop();
	
	/**
	 * @brief 删除指定resid所在本地语音文件
	 * @param[in] pAirTalkee - AirTalkee对象结构体指针
	 * @param[in] resid - 语音文件资源编码
	 * @return - 无
	 */
	public void messageRecordFileDel(String resid);
	
	/**
	 * @brief 根据msgid，resid下载指定语音文件
	 * @param[in] pAirTalkee - AirTalkee对象结构体指针
	 * @param[in] msgid - 消息编码
	 * @param[in] resid - 语音文件资源编码
	 * @return - 无
	*/
	public void MessageRecordPlayDownload(String msgid, String resid);
	
	/**
	 * -------------------------------------------------------------------
	 * */

	// 网络连接打开
	public void netWorkOpen();

	// 网络连接关闭
	public void netWorkClose();

	// 向通用平台发送消息
	public void AirTalkeeEvent(int statecode, String json, int param1, int param2);

}
