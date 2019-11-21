package com.rd.veuisdk.quik;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

class Action {
    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;

    private static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    private static final String TAG = "Action";

    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        mPlayerRectF = QuikHandler.getShowRectF(asp);
        List<MediaObject> allMedia = scene.getAllMedia();
        float[] itemDus = {2.4f, 2.5f, 3.25f, 3.1f, 2.8f, 2.4f};
        int len = allMedia.size();
        float lineStart = 0;
        for (int n = 0; n < len; n++) {
            MediaObject mediaObject = allMedia.get(n);
            float du = itemDus[n % itemDus.length];
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //视频以视频的duration为准
                du = Math.min(du, mediaObject.getIntrinsicDuration());
                du = QuikHandler.getLineDuration(lineStart, du + lineStart);
            } else {
                mediaObject.setIntrinsicDuration(du);
            }
            float tLineTo = lineStart + du;
            mediaObject.setTimelineRange(lineStart, tLineTo);
            if (n == (len - 1)) {
                //最后一个
                QuikHandler.exit(mediaObject, mPlayerRectF, du);
            } else {
                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE && mediaObject.getWidth() < mediaObject.getHeight() && asp != QuikHandler.ASP_916) {
                    QuikHandler.fixVerVideoFeather(mediaObject, asp);
                } else {
                    if (n == 0) {
                        mediaObject.setAnimationList(loadAnimation7(mediaObject, 0, du));
                    } else {
                        int re = n % 6;
                        if (re == 1) {
                            mediaObject.setAnimationList(loadAnimation1(mediaObject, 0, du));
                        } else if (re == 2) {
                            mediaObject.setAnimationList(loadAnimation2(mediaObject, 0, du));
                        } else if (re == 3) {
                            mediaObject.setAnimationList(loadAnimation3(mediaObject, 0, du));
                        } else if (re == 4) {
                            mediaObject.setAnimationList(loadAnimation4(mediaObject, 0, du));
                        } else if (re == 5) {
                            mediaObject.setAnimationList(loadAnimation5(mediaObject, 0, du));
                        } else if (re == 6) {
                            mediaObject.setAnimationList(loadAnimation6(mediaObject, 0, du));
                        }
                    }
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                }
            }

            lineStart = tLineTo;

        }
    }

    private static List<AnimationObject> loadAnimation1(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(1f, -1f, 2f, 1f));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(startTime + 0.15f);
        animationObject.setRectPosition(new RectF(-0.5f, -1f, 1.5f, 1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        animationObject.setRectPosition(new RectF(-0.5f, 0f, 1.5f, 2f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        float frontFadeDuration = 0.5f;
        AnimationObject lastStartAnimation = listAnimation.get(1);
        AnimationObject lastEndAnimation = listAnimation.get(2);
        float lastStartTime = lastStartAnimation.getAtTime();
        float lastEndTime = lastEndAnimation.getAtTime();
        float lastDuration = lastEndTime - lastStartTime;
        float lastNewEndTime = lastEndTime - frontFadeDuration;
        RectF lastStartRect = lastStartAnimation.getRectPosition();
        RectF lastEndRect = lastEndAnimation.getRectPosition();
        float newLeft = (lastEndRect.left - lastStartRect.left) * lastNewEndTime / lastDuration + lastStartRect.left;
        float newTop = (lastEndRect.top - lastStartRect.top) * lastNewEndTime / lastDuration + lastStartRect.top;
        float newRight = (lastEndRect.right - lastStartRect.right) * lastNewEndTime / lastDuration + lastStartRect.right;
        float newBottom = (lastEndRect.bottom - lastStartRect.bottom) * lastNewEndTime / lastDuration + lastStartRect.bottom;
        RectF lastNewRect = new RectF(newLeft, newTop, newRight, newBottom);
        lastEndAnimation.setAtTime(lastNewEndTime);
        lastEndAnimation.setRectPosition(lastNewRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(lastEndAnimation.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        lastEndAnimation.setClipRect(clip);


        animationObject = new AnimationObject(lastEndTime);
        animationObject.setAlpha(0);
        animationObject.setRectPosition(lastEndRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        return listAnimation;
    }


    private static List<AnimationObject> loadAnimation2(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAlpha(0);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime);
        Matrix matrix = new Matrix();
        matrix.postScale(1.1f, 1.1f, 0.5f, 0.5f);
        RectF rectEnd = new RectF();
        matrix.mapRect(rectEnd, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectEnd);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        AnimationObject lastStartAnimation = listAnimation.get(0);
        AnimationObject lastEndAnimation = listAnimation.get(1);
        float lastStartTime = lastStartAnimation.getAtTime();
        float lastEndTime = lastEndAnimation.getAtTime();
        float lastDuration = lastEndTime - lastStartTime;
        float frontFadeDuration = 0.5f;
        float lastNewEndTime = frontFadeDuration;
        RectF lastStartRect = lastStartAnimation.getRectPosition();
        RectF lastEndRect = lastEndAnimation.getRectPosition();
        float newLeft = (lastEndRect.left - lastStartRect.left) * lastNewEndTime / lastDuration + lastStartRect.left;
        float newTop = (lastEndRect.top - lastStartRect.top) * lastNewEndTime / lastDuration + lastStartRect.top;
        float newRight = (lastEndRect.right - lastStartRect.right) * lastNewEndTime / lastDuration + lastStartRect.right;
        float newBottom = (lastEndRect.bottom - lastStartRect.bottom) * lastNewEndTime / lastDuration + lastStartRect.bottom;
        RectF lastNewRect = new RectF(newLeft, newTop, newRight, newBottom);
        lastEndAnimation.setAtTime(lastNewEndTime);
        lastEndAnimation.setRectPosition(lastNewRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(lastEndAnimation.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        lastEndAnimation.setClipRect(clip);
        lastEndAnimation.setAlpha(1);


        animationObject = new AnimationObject(lastEndTime);
        animationObject.setRectPosition(lastEndRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(lastEndRect), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        return listAnimation;
    }


    private static List<AnimationObject> loadAnimation3(MediaObject mediaObject, float startTime, float endTime) {
        float frontFadeDuration = 0.25f;
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setAlpha(0);
        Matrix matrix = new Matrix();
        matrix.postScale(3f, 3f, 0.5f, 0.5f);
        RectF rectStart = new RectF();
        matrix.mapRect(rectStart, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectStart);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(frontFadeDuration);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        animationObject.setAlpha(1);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime - 0.34f);
        matrix.reset();
        matrix.postScale(1.1f, 1.1f, 0.5f, 0.5f);
        RectF rectend = new RectF();
        matrix.mapRect(rectend, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectend);

        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        matrix.reset();
        matrix.postScale(3f, 3f, 0.5f, 0.5f);
        RectF rend = new RectF();
        matrix.mapRect(rend, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rend);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAlpha(0);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        listAnimation.add(animationObject);
        return listAnimation;
    }


    private static List<AnimationObject> loadAnimation4(MediaObject mediaObject, float startTime, float endTime) {
        float frontFadeDuration = 0.15f;
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setAlpha(0.3f);
        Matrix matrix = new Matrix();
        matrix.postScale(1.5f, 1.5f, 0.5f, 0.5f);
        RectF rectStart = new RectF();
        matrix.mapRect(rectStart, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectStart);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + frontFadeDuration);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        animationObject.setAlpha(1);
        matrix.reset();
        matrix.postScale(1.1f, 1.1f, 0.5f, 0.5f);
        RectF rectend = new RectF();
        matrix.mapRect(rectend, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectend);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime - 0.34f);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        animationObject = new AnimationObject(endTime);
        matrix.reset();
        matrix.postScale(3f, 3f, 0.5f, 0.5f);
        RectF rend = new RectF();
        matrix.mapRect(rend, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rend);
        animationObject.setRotate(15);
        animationObject.setAlpha(0.3f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        listAnimation.add(animationObject);
        return listAnimation;
    }

    private static List<AnimationObject> loadAnimation5(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        Matrix matrix = new Matrix();
        matrix.postScale(1.1f, 1.1f, 0.5f, 0.5f);
        RectF rectStart = new RectF();
        matrix.mapRect(rectStart, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectStart);
        animationObject.setAlpha(0.3f);
        animationObject.setRotate(352);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(startTime + 0.15f);
        animationObject.setAlpha(1);
        animationObject.setRotate(360);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime);
        matrix.reset();
        matrix.postScale(1.1f, 1.1f, 0.5f, 0.5f);
        animationObject.setRotate(360);
        RectF rectEnd = new RectF();
        matrix.mapRect(rectEnd, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectEnd);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        return listAnimation;
    }


    private static List<AnimationObject> loadAnimation6(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(-0.5f, -1f, 1.5f, 1f));
        animationObject.setAlpha(0);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime);
        animationObject.setRectPosition(new RectF(-0.5f, 0f, 1.5f, 2f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        float frontFadeDuration = 0.5f;
        AnimationObject lastStartAnimation = listAnimation.get(0);
        AnimationObject lastEndAnimation = listAnimation.get(1);
        float lastStartTime = lastStartAnimation.getAtTime();
        float lastEndTime = lastEndAnimation.getAtTime();
        float lastDuration = lastEndTime - lastStartTime;
        float lastNewEndTime = frontFadeDuration;
        RectF lastStartRect = lastStartAnimation.getRectPosition();
        RectF lastEndRect = lastEndAnimation.getRectPosition();
        float newLeft = (lastEndRect.left - lastStartRect.left) * lastNewEndTime / lastDuration + lastStartRect.left;
        float newTop = (lastEndRect.top - lastStartRect.top) * lastNewEndTime / lastDuration + lastStartRect.top;
        float newRight = (lastEndRect.right - lastStartRect.right) * lastNewEndTime / lastDuration + lastStartRect.right;
        float newBottom = (lastEndRect.bottom - lastStartRect.bottom) * lastNewEndTime / lastDuration + lastStartRect.bottom;
        RectF lastNewRect = new RectF(newLeft, newTop, newRight, newBottom);
        lastEndAnimation.setAtTime(lastNewEndTime);
        lastEndAnimation.setRectPosition(lastNewRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(lastEndAnimation.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        lastEndAnimation.setClipRect(clip);
        lastEndAnimation.setAlpha(1);


        animationObject = new AnimationObject(lastEndTime - 0.3f);
        animationObject.setRectPosition(lastEndRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(lastEndTime - 0.15f);
        animationObject.setRectPosition(new RectF(-1, 0, 1, 2));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(lastEndTime);
        animationObject.setRectPosition(new RectF(-2, 0, 0, 2));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        listAnimation.add(animationObject);
        return listAnimation;
    }

    private static List<AnimationObject> loadAnimation7(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(-0.5f, -1f, 1.5f, 1f));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        animationObject.setRectPosition(new RectF(-0.5f, 0f, 1.5f, 2f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        float frontFadeDuration = 0.5f;
        AnimationObject lastStartAnimation = listAnimation.get(0);
        AnimationObject lastEndAnimation = listAnimation.get(1);
        float lastStartTime = lastStartAnimation.getAtTime();
        float lastEndTime = lastEndAnimation.getAtTime();
        float lastDuration = lastEndTime - lastStartTime;
        float lastNewEndTime = lastEndTime - frontFadeDuration;
        RectF lastStartRect = lastStartAnimation.getRectPosition();
        RectF lastEndRect = lastEndAnimation.getRectPosition();
        float newLeft = (lastEndRect.left - lastStartRect.left) * lastNewEndTime / lastDuration + lastStartRect.left;
        float newTop = (lastEndRect.top - lastStartRect.top) * lastNewEndTime / lastDuration + lastStartRect.top;
        float newRight = (lastEndRect.right - lastStartRect.right) * lastNewEndTime / lastDuration + lastStartRect.right;
        float newBottom = (lastEndRect.bottom - lastStartRect.bottom) * lastNewEndTime / lastDuration + lastStartRect.bottom;
        RectF lastNewRect = new RectF(newLeft, newTop, newRight, newBottom);
        lastEndAnimation.setAtTime(lastNewEndTime);
        lastEndAnimation.setRectPosition(lastNewRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(lastEndAnimation.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        lastEndAnimation.setClipRect(clip);


        animationObject = new AnimationObject(lastEndTime);
        animationObject.setAlpha(0);
        animationObject.setRectPosition(lastEndRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        return listAnimation;
    }


}
