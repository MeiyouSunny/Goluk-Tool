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

class Epic {
    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;
    private static final String TAG = "Epic";

    static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        List<MediaObject> allMedia = scene.getAllMedia();
        mPlayerRectF = QuikHandler.getShowRectF(asp);


        float[] itemDus = {2.5f, 1.5f, 2.1f, 1.6f, 0.92f, 0.93f, 1.6f};
        float lineStart = 0;
        for (int n = 0; n < allMedia.size(); n++) {
            MediaObject mediaObject = allMedia.get(n);


            float du = itemDus[n % itemDus.length];
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //视频以视频的duration为准
                du = Math.min(du, mediaObject.getIntrinsicDuration());
            } else {
                mediaObject.setIntrinsicDuration(du);
            }


            int re = n % 7;

            AnimationInfo animationInfo = null;
            if (re == 0) {
                animationInfo = loadScaleAnimation(mediaObject, 0, du);
            } else if (re == 1) {
                animationInfo = loadScaleAnimation(mediaObject, 0, du);
            } else if (re == 2) {
                animationInfo = loadScaleAnimation(mediaObject, 0, du);
            } else if (re == 3) {
                animationInfo = loadUpDownMoveAnimation(mediaObject, 0, du, false);
            } else if (re == 4) {
                animationInfo = loadScaleAnimation(mediaObject, 0, du);
            } else if (re == 5) {
                animationInfo = loadScaleAnimation(mediaObject, 0, du);
            } else if (re == 6) {
                animationInfo = loadScaleAnimation(mediaObject, 0, du);
            }

            mediaObject.setTimelineRange(lineStart, lineStart + du);
            mediaObject.setIntrinsicDuration(animationInfo.endTime);
            mediaObject.addAnimationGroup(animationInfo.animationGroup);
            mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
            lineStart += du;

        }
    }

    private static AnimationInfo loadUpDownMoveAnimation(MediaObject mediaObject, float startTime, float endTime, boolean isUptoDown) {
        RectF upRect = new RectF(-0.5f, 0f, 1.5f, 2f);
        RectF downRect = new RectF(-0.5f, -1f, 1.5f, 1f);
        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        if (isUptoDown) {
            animationObject.setRectPosition(upRect);
        } else {
            animationObject.setRectPosition(downRect);
        }
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);


        animationObject = new AnimationObject(endTime);
        if (isUptoDown) {
            animationObject.setRectPosition(downRect);
        } else {
            animationObject.setRectPosition(upRect);
        }
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);
        return new AnimationInfo(new AnimationGroup(listAnimation), startTime, endTime);
    }

    private static AnimationInfo loadScaleAnimation(MediaObject mediaObject, float startTime, float endTime) {
        List<AnimationObject> listAnimation = new ArrayList<>();

        AnimationObject animationObject = new AnimationObject(startTime);
        animationObject.setRectPosition(new RectF(0, 0, 1, 1));
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        Matrix matrix = new Matrix();
        matrix.postScale(1.09f, 1.09f, 0.5f, 0.5f);
        RectF rectEnd = new RectF();
        matrix.mapRect(rectEnd, new RectF(0, 0, 1, 1));
        animationObject.setRectPosition(rectEnd);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        listAnimation.add(animationObject);

        return new AnimationInfo(new AnimationGroup(listAnimation), startTime, endTime);
    }

    private static class AnimationInfo {
        private AnimationGroup animationGroup;
        private float startTime;
        private float endTime;

        public AnimationInfo(AnimationGroup animationGroup, float startTime, float endTime) {
            this.animationGroup = animationGroup;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

}
