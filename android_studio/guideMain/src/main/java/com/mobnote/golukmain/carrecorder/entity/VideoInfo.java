package com.mobnote.golukmain.carrecorder.entity;

public class VideoInfo {
    /**
     * Video-only mark
     */
    public long id;
    /* 视频封面 */
    public String thumbUrl;
    /* 本地视频封面 */
    public String localThumbUrl;
    /**
     * Time created
     */
    public String videoCreateDate;
    /**
     * Video size
     */
    public String videoSize;
    /**
     * Currently in selection mode
     */
    public boolean isSelect;
    /**
     * Video playing method
     */
    public String videoPath;
    /**
     * Video length
     */
    public String countTime = null;
    /**
     * Definition: 1080p 720p 480p
     */
    public String videoHP;
    /**
     * Release mark
     */
    public boolean isRecycle = false;
    /**
     * Video recording starting time (sec)
     */
    public long time;
    /**
     * Video name
     */
    public String filename;
    /**
     * Would you like to display the new icon?
     */
    public boolean isNew = false;
    /* 视频类型 */
    public int type;

    /* T2S:相对路径(如:A:\DCIM\Normal\2019_0919_110011_000001.MP4) */
    public String relativePath;
    /* 远程视频地址 */
    public String videoUrl;
}
