package com.goluk.ipcsdk.bean;

/**
 * Created by hanzheng on 2016/5/27.
 */
public class VideoFileInfo {
    /** 文件唯一标识 */
    public int id;
    /**开始录制时间**/
    public String timestamp;
    /** 视频文件录制起始时间（秒） */
    public long time;
    /** 时长(秒) */
    public int period;
    /** 起因事件类型 */
    public int type;
    /** 字长(MB) */
    public double size;
    /** 文件名 */
    public String location;
    /** 分辨率 1080p 720p */
    public String resolution;
    /** 是否具有录像截图文件。是(1) | 否(0) */
    public int withSnapshot;
    /** 是否具有gps文件。是(1) | 否(0) */
    public int withGps;
}
