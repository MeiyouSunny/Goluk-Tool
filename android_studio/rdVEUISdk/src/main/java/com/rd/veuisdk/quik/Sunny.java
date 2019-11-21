package com.rd.veuisdk.quik;

import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JIAN
 * @create 2018/9/12
 * @Describe
 */
class Sunny {
    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;

    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
        mPlayerRectF = QuikHandler.getShowRectF(asp);
        List<MediaObject> allMedia = scene.getAllMedia();
        float lineStart = 0;
        for (int n = 0; n < allMedia.size(); n++) {
            MediaObject mediaObject = allMedia.get(n);
            float du = 5f;
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //视频以视频的duration为准
                du = Math.min(du, mediaObject.getIntrinsicDuration());
            } else {
                mediaObject.setIntrinsicDuration(du);

            }
            if (n == 0) {
                mediaObject.setTimelineRange(lineStart, lineStart + du);
                du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();
                mediaObject.setAnimationList(loadAnimation1(mediaObject, du));

            } else {
                if (n % 3 == 1) {
                    //向下滑必须与前一个媒体有交集，否则背景黑色
                    lineStart -= 2;
                    mediaObject.setTimelineRange(lineStart, lineStart + du);
                    du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();
                    mediaObject.setAnimationList(loadAnimation2(mediaObject, du));
                } else if (n % 3 == 2) {
                    //慢慢变大
                    mediaObject.setTimelineRange(lineStart, lineStart + du);
                    du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();
                    mediaObject.setAnimationList(loadAnimation3(mediaObject, du));
                } else {
                    //慢慢变小
                    mediaObject.setTimelineRange(lineStart, lineStart + du);
                    du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();
                    mediaObject.setAnimationList(loadAnimation1(mediaObject, du));
                }
            }
            lineStart += du;
        }
    }

    /**
     * 慢慢变小
     *
     * @param mediaObject
     * @param duration
     * @return
     */
    private static List<AnimationObject> loadAnimation1(MediaObject mediaObject, float duration) {
        List<AnimationObject> list = new ArrayList<>();
        RectF showF = new RectF(0, 0, 1, 1);
        //慢速缩小
        AnimationObject object = new AnimationObject(0);
        RectF src = new RectF(showF);
        src = QuikHandler.createRect(new RectF(src), 2.5f);
        object.setRectPosition(src);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        src = QuikHandler.createRect(new RectF(src), 0.7f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_WARM));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        return list;


    }

    /**
     * 放大平移
     *
     * @param mediaObject
     * @param duration
     * @return
     */
    private static List<AnimationObject> loadAnimation3(MediaObject mediaObject, float duration) {
        List<AnimationObject> list = new ArrayList<>();
        RectF src = new RectF(0, 0, 1, 1);
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(src);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.3f);
        src.offset(0.15f, -0.1f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_WARM));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);
        return list;

    }

    /**
     * 从上往下移动 ，慢速放大
     *
     * @param duration
     * @return
     */
    private static List<AnimationObject> loadAnimation2(MediaObject mediaObject, float duration) {
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        RectF rectF = new RectF(-0.05f, -0.05f, 1.05f, 1.05f);
        rectF.offset(0, -1f);
        object.setRectPosition(new RectF(rectF));

        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.105f);
        rectF = new RectF(rectF);
        rectF.offset(0, 1);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_WARM));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(rectF);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        object = new AnimationObject(duration);
        rectF = new RectF(-0.1f, -0.1f, 1.1f, 1.1f);
        rectF = QuikHandler.createRect(new RectF(rectF), 1.5f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_WARM));
        object.setRectPosition(rectF);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        return list;

    }

}
