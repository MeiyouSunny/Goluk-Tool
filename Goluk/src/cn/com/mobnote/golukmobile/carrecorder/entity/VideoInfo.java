package cn.com.mobnote.golukmobile.carrecorder.entity;

import android.graphics.Bitmap;

public class VideoInfo {

	public long id;

	/** 视频截图 */
	public int videoImg;
	/** 视频截图 */
	public Bitmap videoBitmap;
	/** 文件创建时间 */
	public String videoCreateDate = null;

	public String videoSize;

	/** 当前选择中状态 */
	public boolean isSelect;
	/** 视频播放路径 */
	public String videoPath;
	/** 视频时长 */
	public String countTime = null;
	/**分辨率  1080p 720p*/
	public String videoHP;
	/** 释放标识 */
	public boolean isRecycle=false;
	/**视频文件录制起始时间（秒）  */
	public long time;
}
