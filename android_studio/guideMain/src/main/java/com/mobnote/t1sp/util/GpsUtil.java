package com.mobnote.t1sp.util;

import net.sf.marineapi.bean.GPSData;

import java.util.List;

public class GpsUtil {

    public static String avgSpeed(List<GPSData> list, long timeMilles) {
        if (list == null || list.size() == 0) {
            return "0";
        }

        int mail = totalMailslength(list);
        if (timeMilles == 0) {
            return "0";
        }
        timeMilles = timeMilles / 1000;
        float speed = ((float) mail / timeMilles) * 3.6F;
        return String.format("%.2f", speed);
    }

    public static int currentSpeed(GPSData data) {
        return data == null ? 0 : (int) (data.speed * 3.6);
    }

    public static long totalTime(List<GPSData> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        long start = 0;
        for (GPSData data : list) {
            if (data.time != 0) {
                start = data.time;
                break;
            }
        }
        long end = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).time != 0) {
                end = list.get(i).time;
                break;
            }
        }
        return (end - start) + 1;
    }

    public static final double MAX_LENGTH_PER_SECOND = 66.67; // 240KM/h

    /**
     * 单位米
     */
    public static int totalMailslength(List<GPSData> list) {
        if (list == null || list.size() <= 1) {
            return 0;
        }
        float[] results = new float[1];
        int distance = 0;
        GPSData lastValidData = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (lastValidData.latitude == 0) {
                lastValidData = list.get(i);
                continue;
            }
            if (list.get(i).latitude == 0) {
                continue;
            }
            android.location.Location.distanceBetween(lastValidData.latitude, lastValidData.longitude,
                    list.get(i).latitude, list.get(i).longitude, results);
            if (results[0] > MAX_LENGTH_PER_SECOND)
                continue;
            distance += results[0];
            lastValidData = list.get(i);
        }
        return distance;
    }

    public static String totalMails(List<GPSData> list) {
        int distance = totalMailslength(list);
        float distanceKm = distance / 1000F;
        if (distance == 0)
            return "0";
        if (distance < 100) {
            return String.format("%.2f", distanceKm);
        }

        return String.format("%.1f", distanceKm);
    }
}
