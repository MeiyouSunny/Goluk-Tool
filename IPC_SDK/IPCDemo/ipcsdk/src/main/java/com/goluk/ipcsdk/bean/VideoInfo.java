package com.goluk.ipcsdk.bean;

import android.graphics.Bitmap;

/**
 * Created by zenghao on 2016/5/27.
 */
public class VideoInfo {
    /** Video-only mark */
    public long id;
    /** Screen capture from video */
    public int videoImg;
    /** Screen capture from video */
    public Bitmap videoBitmap;
    /** Time created */
    public String videoCreateDate = null;
    /** Video size */
    public String videoSize;
    /** Currently in selection mode */
    public boolean isSelect;
    /** Video playing method */
    public String videoPath;
    /** Video length */
    public String countTime = null;
    /** Definition: 1080p 720p 480p*/
    public String videoHP;
    /** Release mark */
    public boolean isRecycle = false;
    /** Video recording starting time (sec) */
    public long time;
    /** Video name */
    public String filename;
    /** Would you like to display the new icon? */
    public boolean isNew = false;
//	/** Sync complete? **/
//	public boolean isAsync;
}
