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
	/**分辨率  1:1080p 2:720p*/
	public int videoHP = 0;
	/** 释放标识 */
	public boolean isRecycle=false;

}
