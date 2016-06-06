package com.goluk.ipcsdk.bean;

/**
 * Created by zenghao on 2016/5/27.
 */
public class RecordStorageState {
    /** Is the SD card in place? Yes(1)|NO(0). The following section only applies when the SD card is in its place. */
    public int SDCardActive;
    /** Not enough space remaining in SD card. Unable to continue recording (1) | SD card memory space is normal (0) */
    public int isSpaceTooSmall;
    /** Capacity of SD card (MB) */
    public double totalSdSize;
    /** Size of user's files (MB) */
    public double userFilesSize;
    /** Space remaining in SD card (MB) */
    public double leftSize;
    /** Max limit for dashcam recordings (MB) */
    public double normalRecQuota;
    /** Current size of dashcam recordings (MB) */
    public double normalRecSize;
    /** Max limit for emergency recordings (MB) */
    public double urgentRecQuota;
    /** Current size of emergency recordings (MB) */
    public double urgentRecSize;
    /** Max limit for epic recordings (MB) */
    public double wonderfulRecQuota;
    /** Current size of epic recordings (MB) */
    public double wonderfulRecSize;
    /** Max limit for screen captures (MB) */
    public double picQuota;
    /** Current size of screen captures (MB) */
    public double picSize;
}
