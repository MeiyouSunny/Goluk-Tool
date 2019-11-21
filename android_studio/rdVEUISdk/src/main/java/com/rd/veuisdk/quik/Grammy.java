package com.rd.veuisdk.quik;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.AnimationGroup;
import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.EffectType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class Grammy {

    private static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    private static final String TAG = "Grammy";
    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;

    /**
     * @param scene
     * @param virtualVideo
     * @param asp
     */
    public static void loadAnimation(Scene scene, VirtualVideo virtualVideo, float asp) {
        mPlayerRectF = QuikHandler.getShowRectF(asp);
        List<MediaObject> allMedia = scene.getAllMedia();
        float nLineTo = 0;
        int len = allMedia.size();
        for (int n = 0; n < len; n++) {
            MediaObject mediaObject = allMedia.get(n);
            int re = n % 7;
            AnimationInfo animationInfo;
            if (re == 0) {
                animationInfo = loadAnimation1(mediaObject, 0, Math.min(mediaObject.getIntrinsicDuration(), 2f));
            } else if (re == 1) {
                animationInfo = loadAnimation2(mediaObject, 0, Math.min(mediaObject.getIntrinsicDuration(), 2.6f));
            } else if (re == 2) {
                animationInfo = loadAnimation3(mediaObject, 0, Math.min(mediaObject.getIntrinsicDuration(), 2.5f));
            } else if (re == 3) {
                animationInfo = loadAnimation4(mediaObject, 0, Math.min(mediaObject.getIntrinsicDuration(), 2.5f));
            } else if (re == 4) {
                animationInfo = loadAnimation5(mediaObject, 0, Math.min(mediaObject.getIntrinsicDuration(), 1.3f));
            } else if (re == 5) {
                animationInfo = loadAnimation6(mediaObject, 0, Math.min(mediaObject.getIntrinsicDuration(), 1.25f));
            } else {
                animationInfo = loadAnimation7(mediaObject, 0, Math.min(mediaObject.getIntrinsicDuration(), 2.5f));
            }
            float timeStart = nLineTo;
            nLineTo += animationInfo.endTime;
            if (animationInfo.effectType != null && null != virtualVideo) {
                try {
                    virtualVideo.addEffect(animationInfo.effectType, nLineTo - 1, nLineTo);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
            if (mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                mediaObject.setIntrinsicDuration(animationInfo.endTime);
            }
            mediaObject.setTimelineRange(timeStart, nLineTo);

            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE && mediaObject.getWidth() < mediaObject.getHeight() && asp != QuikHandler.ASP_916) {
                QuikHandler.fixVerVideoFeather(mediaObject, asp);
            } else {
                //图片、非竖屏视频
                mediaObject.addAnimationGroup(animationInfo.animationGroup);
            }
        }
    }

    private static AnimationInfo loadAnimation1(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(-0.1f, -0.2f, 1.1f, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(startTime + ((endTime - startTime) / 2.0f));
        animationObject.setRectPosition(new RectF(-0.1f, 0, 1.1f, 1.2f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        Matrix matrix = new Matrix();
        matrix.postScale(1.3f, 1.3f, 0.5f, 0.5f);
        RectF rectEnd = new RectF();
        matrix.mapRect(rectEnd, new RectF(-0.1f, 0, 1.1f, 1.2f));
        animationObject.setRectPosition(rectEnd);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        listAnimation.add(animationObject);
        AnimationInfo animationInfo = new AnimationInfo(new AnimationGroup(listAnimation), EffectType.GAUSSIAN_BLUR, startTime, endTime);
        return animationInfo;
    }

    private static AnimationInfo loadAnimation2(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(-0.05f, -0.1f, 1.05f, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 2f);
        animationObject.setRectPosition(new RectF(-0.05f, 0, 1.05f, 1.1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 2.2f);
        VisualFilterConfig visualFilterConfig = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR);
        visualFilterConfig.setDefaultValue(0.01f);
        animationObject.addVisualFilterConfig(visualFilterConfig);
        animationObject.setRectPosition(new RectF(-0.05f, 0, 1.05f, 1.1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        visualFilterConfig = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR);
        visualFilterConfig.setDefaultValue(0.02f);
        animationObject.addVisualFilterConfig(visualFilterConfig);
        animationObject.setRectPosition(new RectF(-0.05f, 0, 1.05f, 1.1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        AnimationInfo animationInfo = new AnimationInfo(new AnimationGroup(listAnimation), null, startTime, endTime);
        return animationInfo;
    }

    private static AnimationInfo loadAnimation3(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(-0.05f, -0.1f, 1.05f, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 1.9f);
        animationObject.setRectPosition(new RectF(-0.05f, 0, 1.05f, 1.1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 1.901f);
        Matrix matrix = new Matrix();
        matrix.postScale(1.3f, 1.3f, 0.5f, 0.5f);
        RectF rectEnd = new RectF();
        matrix.mapRect(rectEnd, new RectF(-0.05f, 0, 1.05f, 1.1f));
        animationObject.setRectPosition(rectEnd);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        VisualFilterConfig visualFilterConfig = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY);
        animationObject.addVisualFilterConfig(visualFilterConfig);
        animationObject.setRectPosition(rectEnd);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        AnimationInfo animationInfo = new AnimationInfo(new AnimationGroup(listAnimation), null, startTime, endTime);
        return animationInfo;
    }

    private static AnimationInfo loadAnimation4(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(-0.05f, -0.1f, 1.05f, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 1.8f);
        animationObject.setRectPosition(new RectF(-0.05f, 0, 1.05f, 1.1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        VisualFilterConfig visualFilterConfig = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR);
        visualFilterConfig.setDefaultValue(0.2f);
        animationObject.addVisualFilterConfig(visualFilterConfig);
        animationObject.setRectPosition(new RectF(-0.05f, 0, 1.05f, 1.1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        AnimationInfo animationInfo = new AnimationInfo(new AnimationGroup(listAnimation), null, startTime, endTime);
        return animationInfo;
    }

    private static AnimationInfo loadAnimation5(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(-0.05f, -0.1f, 1.05f, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 0.9f);
        animationObject.setRectPosition(new RectF(-0.05f, 0, 1.05f, 1.1f));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 0.901f);
        Matrix matrix = new Matrix();
        matrix.postScale(1.2f, 1.2f, 0.5f, 0.5f);
        RectF endRect1 = new RectF();
        matrix.mapRect(endRect1, new RectF(-0.05f, 0, 1.05f, 1.1f));
        animationObject.setRectPosition(endRect1);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(startTime + 1.1f);
        animationObject.setRectPosition(endRect1);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(startTime + 1.101f);
        matrix.reset();
        matrix.postScale(1.4f, 1.4f, 0.5f, 0.5f);
        RectF endRect2 = new RectF();
        matrix.mapRect(endRect2, new RectF(-0.05f, 0, 1.05f, 1.1f));
        animationObject.setRectPosition(endRect2);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        animationObject.setRectPosition(endRect2);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        AnimationInfo animationInfo = new AnimationInfo(new AnimationGroup(listAnimation), null, startTime, endTime);
        return animationInfo;
    }

    private static AnimationInfo loadAnimation6(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        Matrix matrix = new Matrix();
        matrix.postScale(1.1f, 1.1f, 0.5f, 0.5f);
        RectF startRect = new RectF();
        matrix.mapRect(startRect, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(startRect);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        AnimationInfo animationInfo = new AnimationInfo(new AnimationGroup(listAnimation), null, startTime, endTime);
        return animationInfo;
    }

    private static AnimationInfo loadAnimation7(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime - 0.6f);
        Matrix matrix = new Matrix();
        matrix.postScale(1.1f, 1.1f, 0.5f, 0.5f);
        RectF endRect = new RectF();
        matrix.mapRect(endRect, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(endRect);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        matrix.reset();
        matrix.postScale(1.3f, 1.3f, 0.5f, 0.5f);
        RectF endRect2 = new RectF();
        matrix.mapRect(endRect2, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(endRect2);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        listAnimation.add(animationObject);
        AnimationInfo animationInfo = new AnimationInfo(new AnimationGroup(listAnimation), EffectType.GAUSSIAN_BLUR, startTime, endTime);
        return animationInfo;
    }

    private static class AnimationInfo {
        private AnimationGroup animationGroup;
        private EffectType effectType;
        private float startTime;
        private float endTime;

        public AnimationInfo(AnimationGroup animationGroup, EffectType effectType, float startTime, float endTime) {
            this.animationGroup = animationGroup;
            this.effectType = effectType;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
