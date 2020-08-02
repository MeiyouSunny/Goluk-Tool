package com.mobnote.t1sp.gps;

/**
 * Created by HuangJW on 2020-7-25 1:31.
 * Mail: 499655607@qq.com
 * Powered by Vcolco
 */
public class GPSData {
    public static int COORD_TYPE_GPS = 0;
    public static int COORD_TYPE_BD0911 = 1;
    public static int COORD_TYPE_AMAP = 2;
    public long time;
    public double latitude;
    public double longitude;
    public int altitude;
    public int angle;
    public int speed;
    public int coordType;
    public int status;

    public GPSData() {
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        var1.append("[time = ");
        var1.append(this.time);
        var1.append(", latitude = ");
        var1.append(this.latitude);
        var1.append(", longitude = ");
        var1.append(this.longitude);
        var1.append(", altitude = ");
        var1.append(this.altitude);
        var1.append(", angle = ");
        var1.append(this.angle);
        var1.append(", speed = ");
        var1.append(this.speed);
        var1.append("]");
        return var1.toString();
    }
}
