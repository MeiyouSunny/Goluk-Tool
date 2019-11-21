package com.rd.veuisdk.quik;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.AnimationGroup;
import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

class Lapse {
    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;
    static String TAG = "Lapse";

    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        mPlayerRectF = QuikHandler.getShowRectF(asp);
        float lineStart = 0;

        for (MediaObject mediaObject : scene.getAllMedia()) {
            float tLineTo = lineStart;
            float du = 3f;
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //视频以视频的duration为准
                du = Math.min(du, mediaObject.getIntrinsicDuration());
            } else {
                mediaObject.setIntrinsicDuration(du);
            }
            tLineTo = lineStart + du;
            mediaObject.setTimelineRange(lineStart, tLineTo);

            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                if (mediaObject.getWidth() < mediaObject.getHeight()) {
                    QuikHandler.fixVerVideoFeather(mediaObject, asp);
                } else {
                    //静态播放
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                }
            } else {
                mediaObject.addAnimationGroup(loadAnimation1(mediaObject, 0, du));
                mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
            }
//            Log.e(TAG, "loadAnimation: " + "   line:" + mediaObject.getTimelineFrom() + "<>" + mediaObject.getTimelineTo() + "   " + du);

            lineStart = tLineTo;
        }
    }

    private static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    private static AnimationGroup loadAnimation1(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        RectF src = new RectF(-0.5f, -1f, 1.5f, 1f);
        animationObject.setRectPosition(src);

        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime - 0.6f);
        src = new RectF(-0.5f, -0.5f, 1.5f, 1.5f);
        animationObject.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime);
        Matrix matrix = new Matrix();
        matrix.postScale(3f, 3f, 0.5f, 0.5f);
        RectF rend = new RectF();
        matrix.mapRect(rend, new RectF(-0.5f, -0.5f, 1.5f, 1.5f));
        animationObject.setRectPosition(rend);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(rend), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRotate(15);
        animationObject.setAlpha(0.3f);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        listAnimation.add(animationObject);
        return new AnimationGroup(listAnimation);
    }
}
