package com.goluk.ipcsdk.bean;

/**
 * Created by hanzheng on 2016/5/27.
 */
public class FileInfo {
    /** File-only mark */
    public int id;
    /**Time when recording starts**/
    public String timestamp;
    /** Video recording starting time (sec) */
    public long time;
    /** Video length (sec)*/
    public int period;
    /** Opening event type */
    public int type;
    /** Word length (MB) */
    public double size;
    /** File name */
    public String location;
    /** Definition 1080p 720p */
    public String resolution;
    /** Does this contain screen captures from videos? Yes (1) | No (0) */
    public int withSnapshot;
    /** Does this contain GPS files? Yes (1) | No (0) */
    public int withGps;
}
