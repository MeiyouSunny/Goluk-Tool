package com.mobnote.golukmain.videosuqare;

/**
 * 这里包括视频的获奖，以及聚合相关
 * 
 * @author jyf
 */
public class VideoExtra {

	/** 活动Id */
	public String topicid;
	/** 频道 id */
	public String channelid;
	/** 是否是推荐视频 0: 否 1:是 */
	public String isrecommend;
	/** 是否获奖 0: 否 1:是 */
	public String isreward;
	/** 聚合活动字符串 */
	public String topicname;

	/** 系统获奖*/
	public String atflag;
	/** 人工获奖*/
	public String sysflag;
}
