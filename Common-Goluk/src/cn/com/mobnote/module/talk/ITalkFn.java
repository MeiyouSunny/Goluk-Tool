package cn.com.mobnote.module.talk;

import cn.com.mobnote.logic.IGolukCommFn;

public interface ITalkFn extends IGolukCommFn {
	/**
	 * 爱滔客对讲调用命令
	 * */

	/** 加入直播者的群组，参数为群组信息的json */
	public static final int Talk_CommCmd_JoinGroupWithInfo = 0;
	/** 创建自己直播的群组 */
	public static final int Talk_CommCmd_JoinPersonGroup = 1;
	/** 退出直播群组，进入大区群组 */
	public static final int Talk_CommCmd_QuitGroup = 2;
	/** 请求说话 */
	public static final int Talk_CommCmd_TalkRequest = 3;
	/** 释放说话 */
	public static final int Talk_CommCmd_TalkRelease = 4;
	/** 网络恢复 */
	public static final int Talk_CommCmd_RecoveryNetwork = 5;
	/** 开始上报自己的位置(用于开启直播功能中使用)*/
	public static final int Talk_Command_StartUploadPosition = 6;
	/** 停止上报位置 (退出直播时功能使用) */
	public static final int Talk_Command_StopUploadPosition = 7;

	/**
	 * 爱滔客回调事件
	 * */

	/** 设置用户修改信息事件 */
	public static final int Talk_Event_SetUserInfo = 1;
	/** 进入频道相关事件 */
	public static final int Talk_Event_ChanleIn = 2;
	/** 频道内交互事件 */
	public static final int Talk_Event_ChanleInterAction = 3;
	/** 频道内位置信息及标题信息变更事件 */
	public static final int Talk_Event_ChanleInfoChange = 4;
	/** 网络变化播放声音 */
	public static final int Talk_Event_ChanleNetChangePrompt = 5;
	/** 发送给平台的信息 */
	public static final int Talk_Event_ToPlatform = 6;

	public void TalkNotifyCallBack(int type, String data);

}
