package com.rd.veuisdk.quik;

import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.AspectRatioFitMode;
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
class Slice {
    private static final RectF mRightSrc = new RectF(0.51f, 0, 1, 1);
    private static final RectF mRightDst = new RectF(0.5f, -0.15f, 1.1f, 1.2f);
    private static final RectF mLeftSrc = new RectF(0, 0, 0.49f, 1);
    private static final RectF mLeftDst = new RectF(-0.1f, -0.15f, 0.5f, 1.2f);
    private static final float black_du = 1.99f;

    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;


    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        mPlayerRectF = QuikHandler.getShowRectF(asp);
        scene.setPermutationMode(PermutationMode.COMBINATION_MODE);

        List<MediaObject> list = new ArrayList<>();
        list.addAll(scene.getAllMedia());

        int len = list.size();
        scene.getAllMedia().clear();
        float lineStart = 0;
        //因为左右分割会增加一个媒体，对媒体序列从新组合
        for (int i = 0; i < len; i++) {
            MediaObject mediaObject = list.get(i);


            float du = 2;
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //视频以视频的duration为准
                du = Math.min(du, mediaObject.getIntrinsicDuration());
            } else {
                mediaObject.setIntrinsicDuration(du);
            }

            mediaObject.setTimelineRange(lineStart, lineStart + du);


            if (i == 0) {
                if (mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                    loadAnimation1(scene.getAllMedia(), mediaObject, du);
                } else {
                    loadAnimationVideo1(scene.getAllMedia(), mediaObject, du);
                }
            } else {
                if (i == (len - 1) && i > 1) {
                    //左黑右先显示
                    if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        if (mediaObject.getWidth() < mediaObject.getHeight()) {
                            QuikHandler.fixVerVideoFeather(mediaObject, asp);
                        } else {
                            //静态播放
                            mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                        }
                        scene.addMedia(mediaObject);
                    } else {
                        loadAnimationLast(scene.getAllMedia(), mediaObject, du);
                    }
                } else {
                    int re = i % 4;
                    if (re == 1) {
                        mediaObject.setAnimationList(loadAnimation2(mediaObject, du));
                        scene.addMedia(mediaObject);
                    } else if (re == 2) {
                        //左上右下 分别显示
                        loadAnimation4In(scene.getAllMedia(), mediaObject, du);
                        lineStart += du;

                        //放大
                        MediaObject tmp = mediaObject.clone();
                        tmp.clearAnimationGroup();
                        tmp.setTimelineRange(lineStart, lineStart + 4);
                        du = tmp.getTimelineTo() - tmp.getTimelineFrom();
                        RectF show1 = new RectF(0, 0, 1, 1);
                        RectF show2 = QuikHandler.createRect(show1, 1.2f);
                        tmp.setAnimationList(loadAnimation2(mediaObject, show1, show2, du));
                        scene.addMedia(tmp);


                    } else if (re == 3) {
                        //放大
                        MediaObject tmp = mediaObject.clone();
                        tmp.clearAnimationGroup();
                        tmp.setTimelineRange(lineStart, lineStart + 4);
                        du = tmp.getTimelineTo() - tmp.getTimelineFrom();
                        RectF show1 = new RectF(0, 0, 1, 1);
                        RectF show2 = QuikHandler.createRect(show1, 1.2f);
                        tmp.setAnimationList(loadAnimation2(mediaObject, show2, show1, du));
                        scene.addMedia(tmp);
                        lineStart += du;


                        mediaObject.setTimelineRange(lineStart, lineStart + 2);
                        du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();


                        //左上右下 分别显示
                        loadAnimation4Out(scene.getAllMedia(), mediaObject, du);
                    } else {
                        loadAnimation3(scene.getAllMedia(), mediaObject, du);
                    }
                }
            }
            lineStart += du;

        }
    }


    private static String TAG = "Slice";

    /**
     * 左右分割  ,左边先显示右边黑
     *
     * @param mediaObjects
     * @param mediaObject
     * @param duration
     */
    private static void loadAnimation1(List<MediaObject> mediaObjects, MediaObject mediaObject, float duration) {


        //媒体被拆分成两个媒体，left

        List<AnimationObject> list = new ArrayList<>();


        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);

        mediaObject.setClipRectF(new RectF(clip.left, clip.top, clip.centerX(), clip.bottom));
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(mLeftSrc);
        mediaObject.setShowRectF(mLeftSrc);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setRectPosition(mLeftDst);
        list.add(object);
        mediaObject.setAnimationList(list);
        mediaObjects.add(mediaObject);


        //另一个半  right
        MediaObject clone = mediaObject.clone();
        duration = clone.getTimelineTo() - clone.getTimelineFrom();

        list = new ArrayList<>();

        clone.setClipRectF(new RectF(clip.centerX(), clip.top, clip.right, clip.bottom));
        object = new AnimationObject(0);
        object.setRectPosition(mRightSrc);
        object.setAlpha(0f);
        clone.setShowRectF(mRightSrc);
        list.add(object);


        float atTime = duration / 10 * black_du;
        float p = atTime / duration;
        object = new AnimationObject(atTime);
        object.setRectPosition(fixRectF(mRightSrc, mRightDst, p));
        object.setAlpha(0f);
        list.add(object);
        clone.setAnimationList(list);


        atTime = duration / 10 * (black_du + 0.01f);
        p = atTime / duration;
        object = new AnimationObject(atTime);
        object.setRectPosition(fixRectF(mRightSrc, mRightDst, p));
        object.setAlpha(1f);
        list.add(object);
        clone.setAnimationList(list);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setRectPosition(mRightDst);
        list.add(object);
        clone.setAnimationList(list);

        mediaObjects.add(clone);


    }

    /**
     * 视频左右分割
     *
     * @param mediaObjects
     * @param mediaObject
     * @param duration
     */
    private static void loadAnimationVideo1(List<MediaObject> mediaObjects, MediaObject mediaObject, float duration) {

        //媒体被拆分成两个媒体，left

        List<AnimationObject> list = new ArrayList<>();


        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        mediaObject.setClipRectF(new RectF(clip.left, clip.top, clip.centerX(), clip.bottom));
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(mLeftSrc);
        mediaObject.setShowRectF(mLeftSrc);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setRectPosition(mLeftDst);
        list.add(object);
        mediaObject.setAnimationList(list);
        mediaObjects.add(mediaObject);


        //另一个半  right
        MediaObject clone = mediaObject.clone();
        clone.setAudioMute(true);
        duration = clone.getTimelineTo() - clone.getTimelineFrom();

        list = new ArrayList<>();
        clone.setClipRectF(new RectF(clip.centerX(), clip.top, clip.right, clip.bottom));
        object = new AnimationObject(0);
        object.setRectPosition(mRightSrc);
        clone.setShowRectF(mRightSrc);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setRectPosition(mRightDst);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        list.add(object);
        clone.setAnimationList(list);

        mediaObjects.add(clone);


    }

    /**
     * @param src
     * @param dst
     * @param p
     * @return
     */
    private static RectF fixRectF(RectF src, RectF dst, float p) {
        return new RectF(src.left + ((dst.left - src.left) * p), src.top + ((dst.top - src.top) * p), src.right + ((dst.right - src.right) * p),
                src.bottom + ((dst.bottom - src.bottom) * p));
    }


    /**
     * 两边分割，同时显示
     *
     * @param mediaObjects
     * @param mediaObject
     * @param duration
     */
    private static void loadAnimation3(List<MediaObject> mediaObjects, MediaObject mediaObject, float duration) {


        //媒体被拆分成两个媒体，left
        List<AnimationObject> list = new ArrayList<>();
        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        mediaObject.setClipRectF(new RectF(clip.left, clip.top, clip.centerX(), clip.bottom));
        AnimationObject object = new AnimationObject(0);

        object.setRectPosition(mLeftSrc);
        mediaObject.setShowRectF(mLeftSrc);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setRectPosition(mLeftDst);
        list.add(object);
        mediaObject.setAnimationList(list);

        mediaObjects.add(mediaObject);


        //另一个半  right
        MediaObject clone = mediaObject.clone();
        if (clone.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            clone.setAudioMute(true);
        }
        duration = clone.getTimelineTo() - clone.getTimelineFrom();

        list = new ArrayList<>();
        clone.setClipRectF(new RectF(clip.centerX(), clip.top, clip.right, clip.bottom));
        object = new AnimationObject(0);
        object.setRectPosition(mRightSrc);
        clone.setShowRectF(mRightSrc);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setRectPosition(mRightDst);
        list.add(object);
        clone.setAnimationList(list);

        mediaObjects.add(clone);


    }

    /**
     * 放大平移
     *
     * @param duration
     * @return
     */
    private static List<AnimationObject> loadAnimation2(MediaObject mediaObject, float duration) {
        RectF src = new RectF(0, 0, 1, 1);
        RectF dst = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.3f);
        dst.offset(0.15f, -0.1f);
        return loadAnimation2(mediaObject, src, dst, duration);

    }

    private static List<AnimationObject> loadAnimation2(MediaObject mediaObject, RectF show1, RectF show2, float duration) {
        List<AnimationObject> list = new ArrayList<>();
        RectF src = new RectF(show1);
        AnimationObject object = new AnimationObject(0);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        mediaObject.setClipRectF(new RectF(clip));
        object.setClipRect(new Rect(clip));
        object.setRectPosition(src);
        list.add(object);

        object = new AnimationObject(duration);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(show2);
        clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(new Rect(clip));
        list.add(object);
        return list;

    }

    private static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    /**
     * 左右分割 （最后）                      //左黑右先显示
     *
     * @param mediaObjects
     * @param mediaObject
     * @param duration
     */
    private static void loadAnimationLast(List<MediaObject> mediaObjects, MediaObject mediaObject, float duration) {

        //媒体被拆分成两个媒体，left

        List<AnimationObject> list = new ArrayList<>();


        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(new RectF(0, 0, 1, 1)), mediaObject.getWidth(), mediaObject.getHeight(), clip);

        mediaObject.setClipRectF(new RectF(clip.left, clip.top, clip.centerX(), clip.bottom));
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(mLeftSrc);
        object.setAlpha(0f);
        mediaObject.setShowRectF(mLeftSrc);
        list.add(object);


        float atTime = duration / 10 * black_du;
        float p = atTime / duration;
        object = new AnimationObject(atTime);
        object.setRectPosition(fixRectF(mLeftSrc, mLeftDst, p));
        object.setAlpha(0f);
        list.add(object);


        atTime = duration / 10 * (black_du + 0.01f);
        p = atTime / duration;
        object = new AnimationObject(atTime);
        object.setRectPosition(fixRectF(mLeftSrc, mLeftDst, p));
        object.setAlpha(1f);
        list.add(object);


        object = new AnimationObject(duration);
        object.setRectPosition(mLeftDst);
        list.add(object);
        mediaObject.setAnimationList(list);
        mediaObjects.add(mediaObject);


        //另一个半  right
        MediaObject clone = mediaObject.clone();
        duration = clone.getTimelineTo() - clone.getTimelineFrom();
        list = new ArrayList<>();
        clone.setClipRectF(new RectF(clip.centerX(), clip.top, clip.right, clip.bottom));
        object = new AnimationObject(0);
        RectF dst = new RectF(mRightSrc);
        object.setRectPosition(dst);
        clone.setShowRectF(dst);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setRectPosition(mRightDst);
        list.add(object);
        clone.setAnimationList(list);
        mediaObjects.add(clone);


    }

    /**
     * 左右拆分，左上右下 ，分别显示进入
     *
     * @param mediaObjects
     * @param mediaObject
     * @param duration
     */
    private static void loadAnimation4In(List<MediaObject> mediaObjects, MediaObject mediaObject, float duration) {


        //媒体被拆分成两个媒体，left   (从下到上)
        RectF showRectF = new RectF(0, 0, 1, 1);

        List<AnimationObject> list = new ArrayList<>();
        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        Rect vClip = new Rect(clip.left, clip.top, clip.centerX(), clip.bottom);
        mediaObject.setClipRectF(new RectF(vClip));
        AnimationObject object = new AnimationObject(0);
        RectF rectF = new RectF(showRectF.left, showRectF.top, showRectF.centerX(), showRectF.bottom);
        RectF tmp = new RectF(rectF);
        tmp.offset(0, 1);
        object.setRectPosition(tmp);
        object.setClipRect(vClip);
        mediaObject.setShowRectF(tmp);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        object.setClipRect(new Rect(vClip));
        object.setRectPosition(rectF);
        list.add(object);
        mediaObject.setAnimationList(list);
        mediaObjects.add(mediaObject);

        //另一个半  right   从上到下
        MediaObject clone = mediaObject.clone();
        if (clone.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            clone.setAudioMute(true);//视频静音
        }
        duration = clone.getTimelineTo() - clone.getTimelineFrom();
        list = new ArrayList<>();
        clone.setClipRectF(new RectF(vClip.right, clip.top, clip.right, clip.bottom));
        object = new AnimationObject(0);

        RectF dst = new RectF(rectF.right, showRectF.top, showRectF.right, showRectF.bottom);
        dst.offset(0, -1f);
        object.setRectPosition(dst);
        clone.setShowRectF(dst);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        rectF = new RectF(dst);
        rectF.offset(0, 1f);
        object.setRectPosition(new RectF(rectF));
        list.add(object);
        clone.setAnimationList(list);

        mediaObjects.add(clone);


    }

    /**
     * 左上，右下 分别离开
     *
     * @param mediaObjects
     * @param mediaObject
     * @param duration
     */
    private static void loadAnimation4Out(List<MediaObject> mediaObjects, MediaObject mediaObject, float duration) {


        //媒体被拆分成两个媒体，left

        List<AnimationObject> list = new ArrayList<>();
        Rect clip = new Rect();
        MiscUtils.fixClipRect(mPlayerRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        Rect vClip = new Rect(clip.left, clip.top, clip.centerX(), clip.bottom);
        mediaObject.setClipRectF(new RectF(vClip));
        AnimationObject object = new AnimationObject(0);

        RectF rectF = new RectF(0, 0, 0.5f, 1);
        RectF tmp = new RectF(rectF);
        object.setClipRect(new Rect(vClip));
        object.setRectPosition(tmp);
        mediaObject.setShowRectF(tmp);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        rectF = new RectF(rectF);
        rectF.offset(0, -1f);
        object.setRectPosition(rectF);
        list.add(object);
        //clone之后再设置动画
        mediaObject.setAnimationList(list);

        mediaObjects.add(mediaObject);


        //另一个半  right   从上到下
        MediaObject clone = mediaObject.clone();
        if (clone.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            clone.setAudioMute(true);
        }
        duration = clone.getTimelineTo() - clone.getTimelineFrom();
        list = new ArrayList<>();
        clone.setClipRectF(new RectF(vClip.right, clip.top, clip.right, clip.bottom));
        object = new AnimationObject(0);

        RectF dst = new RectF(0.5f, 0, 1, 1);
        object.setRectPosition(dst);
        clone.setShowRectF(dst);
        list.add(object);


        object = new AnimationObject(duration);     //此时下一张图开始播放动画
        rectF = new RectF(dst);
        rectF.offset(0, 1f);
        object.setRectPosition(rectF);
        list.add(object);
        clone.setAnimationList(list);

        mediaObjects.add(clone);


    }


}
