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
    /* T3 碰撞灵敏度值*/
    public static final int COLLISION_OFF_T3 = 0;
    public static final int COLLISION_LOW_T3 = 110;
    public static final int COLLISION_LOWER_T3 = 130;
    public static final int COLLISION_MIDDLE_T3 = 150;
    public static final int COLLISION_HIGHTER_T3 = 170;
    public static final int COLLISION_HIGHT_T3 = 190;

    /**
     * 根据值获取对应的碰撞灵敏度文本
     */
    public static int getCollisionTextResIdByValue(int collisionValue) {
        if (collisionValue == COLLISION_OFF || collisionValue == COLLISION_OFF_T3)
            return R.string.carrecorder_tcaf_close;

        if (collisionValue == COLLISION_LOW || collisionValue == COLLISION_LOW_T3)
            return R.string.str_low;

        if (collisionValue == COLLISION_LOWER || collisionValue == COLLISION_LOWER_T3)
            return R.string.lower;

        if (collisionValue == COLLISION_MIDDLE || collisionValue == COLLISION_MIDDLE_T3)
            return R.string.str_middle;

        if (collisionValue == COLLISION_HIGHTER || collisionValue == COLLISION_HIGHTER_T3)
            return R.string.highter;

        if (collisionValue == COLLISION_HIGHT || collisionValue == COLLISION_HIGHT_T3)
            return R.string.str_high;

        return -1;
    }

}
