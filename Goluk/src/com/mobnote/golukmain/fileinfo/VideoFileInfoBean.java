package com.mobnote.golukmain.fileinfo;

/**
 * 保存视频文件的信息，对应数据库操作
 * 
 * @author jyf
 */
public class VideoFileInfoBean {
	/** 文件名 ，做为key */
	public String filename;
	/** 精彩，紧急，循环 */
	public String type; //
	/** 文件大小 */
	public String filesize;
	/** 720p,1080p,480p */
	public String resolution;
	/** 视频长度 */
	public String period;
	/** 时间 */
	public String timestamp;
	/** 照片文件名 */
	public String picname;
	/** 设备名 */
	public String devicename;
	/** GPS文件名 */
	public String gpsname;
	/** 插入数据库的时间 */
	public String savetime;
	/** 预留字段 */
	public String reserve1;
	/** 预留字段 */
	public String reserve2;
	/** 预留字段 */
	public String reserve3;
	/** 预留字段 */
	public String reserve4;

	@Override
	public String toString() {
		String msg = "filename:" + filename + "  type:" + type + " filesize:" + filesize + " resolution:" + resolution
				+ "  period:" + period + "  timestamp:" + timestamp + " devicename:" + devicename + " savetime:"
				+ savetime;
		return msg;
	}

}
