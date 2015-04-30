package cn.com.mobnote.golukmobile.live;

import java.io.Serializable;

public class LiveDataInfo implements Serializable {

	private static final long serialVersionUID = -8534844170998963067L;
	/** 访问状态 200表示成功，其它表示结束 */
	public int code;
	/** 目前不做处理 */
	public boolean state;
	/** 主动直播标识 1: 表示主动直播 2：表示被动直播 */
	public int active;
	/** 视频播放地址 */
	public String playUrl;
	/** 视频ID */
	public String vid;

	/** 群组Id */
	public String groupId;
	public String groupnumber;
	public String groupType;
	public int membercount;
	public String title;
	/** 视频剩余时间 */
	public int restTime;
	/** 视频描述 */
	public String desc;

}
