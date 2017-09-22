package com.rd.veuisdk.utils;

import android.graphics.RectF;

import com.rd.vecore.models.FlipType;

/**
 * Created by JIAN on 2017/9/13.
 */

public class RotateUtils {

    /***
     * 旋转进项clip 辅助
     * @param srcW
     * @param srcH
     * @param angle
     * @param flipType
     * @param mRectVideoClipBound
     */
    public static void fixRotate(int srcW, int srcH, int angle, FlipType flipType, RectF mRectVideoClipBound) {
        if ((angle == 90 || angle == 270)
                && (flipType == FlipType.FLIP_TYPE_HORIZONTAL ||
                flipType == FlipType.FLIP_TYPE_VERTICAL)) {
            RectF rcCrop = new RectF(Math.round(srcW - mRectVideoClipBound.right),
                    Math.round(srcH - mRectVideoClipBound.bottom),
                    Math.round(srcW - mRectVideoClipBound.left),
                    Math.round(srcH - mRectVideoClipBound.top));
            mRectVideoClipBound = new RectF(rcCrop);
        }
    }
}
