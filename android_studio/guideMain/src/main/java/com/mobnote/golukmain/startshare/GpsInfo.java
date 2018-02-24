package com.mobnote.golukmain.startshare;

/**
 * 轨迹点信息
 */
public class GpsInfo {

    public long ltime;
    public double lat;
    public double lon;
    public double radius;
    public double direction;
    public int altitude;
    public int speed;
    /* 61：GPS定位；161：网络定位 */
    public int type = 61;

}
