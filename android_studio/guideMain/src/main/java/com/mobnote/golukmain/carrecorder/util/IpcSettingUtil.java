package com.mobnote.golukmain.carrecorder.util;

import com.mobnote.golukmain.R;

/**
 * IPC设置相关
 */
public class IpcSettingUtil {

    /* 碰撞灵敏度值:  高：300、较高：380、中：420、较低：510、低：600、关闭: 1000*/
    public static final int COLLISION_OFF = 1000;
    public static final int COLLISION_LOW = 600;
    public static final int COLLISION_LOWER = 510;
    public static final int COLLISION_MIDDLE = 420;
    public static final int COLLISION_HIGHTER = 380;
    public static final int COLLISION_HIGHT = 300;

    /**
     * 根据值获取对应的碰撞灵敏度文本
     */
    public static int getCollisionTextResIdByValue(int collisionValue) {
        switch (collisionValue) {
            case COLLISION_OFF:
                return R.string.carrecorder_tcaf_close;
            case COLLISION_LOW:
                return R.string.str_low;
            case COLLISION_LOWER:
                return R.string.lower;
            case COLLISION_MIDDLE:
                return R.string.str_middle;
            case COLLISION_HIGHTER:
                return R.string.highter;
            case COLLISION_HIGHT:
                return R.string.str_high;
        }

        return -1;
    }

}
