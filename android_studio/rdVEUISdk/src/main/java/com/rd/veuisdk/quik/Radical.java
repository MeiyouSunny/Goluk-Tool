package com.rd.veuisdk.quik;

import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JIAN
 * @create 2018/9/12
 * @Describe
 */
class Radical {

    private static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

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


            mediaObject.setTimelineRange(lineStart, lineStart + du);

            if (n == 0) {
                mediaObject.setAnimationList(loadAnimation1(mediaObject, du));
            } else {
                if (n == (allMedia.size() - 1) && n > 1) {
                    //退出
                    mediaObject.setTimelineRange(lineStart, lineStart + du);
                    mediaObject.setAnimationList(loadAnimation3(mediaObject, du, asp == 1));
                } else {
                    if (n % 2 == 1) {
                        if (asp == 1) {
                            //旋转45，放大   与上一个媒体有重贴
                            lineStart -= 2;
                            mediaObject.setTimelineRange(lineStart, lineStart + du);
                            mediaObject.setAnimationList(loadAnimation2(mediaObject, du));
                        } else {
                            mediaObject.setTimelineRange(lineStart, lineStart + du);
                            mediaObject.setAnimationList(loadAnimation1(mediaObject, du));
                        }
                    } else {
                        //慢慢变小
                        mediaObject.setTimelineRange(lineStart, lineStart + du);
                        mediaObject.setAnimationList(loadAnimation1(mediaObject, du));
                    }
                }
            }
            lineStart += du;

        }
    }

    /**
     * 慢慢变小
     *
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
        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setRectPosition(src);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        src = QuikHandler.createRect(new RectF(src), 0.7f);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setRectPosition(src);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        list.add(object);
        return list;


    }

    /**
     * 旋转45，再放大
     *
     * @param mediaObject
     * @param duration
     * @return
     */
    private static List<AnimationObject> loadAnimation2(MediaObject mediaObject, float duration) {
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        RectF rectF = new RectF(0.15f, 0.15f, 0.85f, 0.85f);
        object.setRotate(45);
        RectF showRectF = QuikHandler.createRect(rectF, 0.5f);
        object.setRectPosition(showRectF);

        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);

        list.add(object);

        object = new AnimationObject(duration * 0.15f);
        object.setRotate(45);

        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setRectPosition(new RectF(rectF));
        list.add(object);


        object = new AnimationObject(duration * 0.195f);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        rectF = new RectF(-0.1f, -0.1f, 1.1f, 1.1f);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setRectPosition(rectF);
        list.add(object);

        object = new AnimationObject(duration);
        rectF = QuikHandler.createRect(new RectF(rectF), 1.5f);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setRectPosition(rectF);
        object.setRotate(0);
        list.add(object);


        return list;

    }

    /**
     * * 旋转45，由大到小 退出
     *
     * @param mediaObject
     * @param duration
     * @param rotate
     * @return
     */
    private static List<AnimationObject> loadAnimation3(MediaObject mediaObject, float duration, boolean rotate) {
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        RectF rectF = new RectF(0.15f, 0.15f, 0.85f, 0.85f);
        boolean isImg = mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE;
        if (isImg && rotate) {
            object.setRotate(45);
        }
        RectF showRectF = QuikHandler.createRect(rectF, 1f);
        object.setRectPosition(showRectF);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration);
        if (isImg && rotate) {
            object.setRotate(45);
        }
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        showRectF = QuikHandler.createRect(new RectF(rectF), (isImg && rotate) ? 0.2f : 0.1f);
        object.setRectPosition(showRectF);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);

        list.add(object);


        return list;

    }
}
