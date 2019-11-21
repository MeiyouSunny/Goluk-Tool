package com.rd.veuisdk.quik;

import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.AnimationGroup;
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
class Flick {
    private static final String TAG = "Flick";
    //16:9
    private static RectF show1 = new RectF(40 / 260.0f, 0, 170 / 260.0f, 1);
    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;
    private static float mAsp;


    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        mAsp = asp;
        if (mAsp == QuikHandler.ASP_169) {
            show1.set(40 / 260.0f, 0, 170 / 260.0f, 1);
        } else if (mAsp == QuikHandler.ASP_1) {
            show1.set(0.2f, 0, 0.8f, 1);
        } else {
            show1.set(0, 0, 1, 1);
        }
        scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
        mPlayerRectF = QuikHandler.getShowRectF(asp);
        List<MediaObject> list = new ArrayList<>();
        list.addAll(scene.getAllMedia());

        int len = list.size();
        scene.getAllMedia().clear();
        float lineStart = 0;
        //因为左右分割会增加一个媒体，对媒体序列从新组合
        for (int i = 0; i < len; i++) {
            MediaObject mediaObject = list.get(i);
            mediaObject.setTimelineRange(lineStart, lineStart + 1.0f);

            float du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();

            if (i == 0) { //满屏
                mediaObject.setClipRectF(null);
                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    //视频的trim
                    mediaObject.setTimeRange(mediaObject.getLineDuration(), mediaObject.getIntrinsicDuration());
                }
                mediaObject.setTimelineRange(lineStart, lineStart + Math.min(4, mediaObject.getIntrinsicDuration()));
                du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();
                mediaObject.setAnimationList(loadAnimationScaleMove(mediaObject, du, new RectF(0, 0, 1, 1)));
                scene.addMedia(mediaObject);

            } else if (i == 1) {
                Rect clip = new Rect();
                loadAnimation1(mediaObject, clip);
                scene.addMedia(mediaObject);
                lineStart += du;

                MediaObject tmp = mediaObject.clone();
                tmp.setClipRectF(null);
                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    //视频的trim
                    tmp.setTimeRange(mediaObject.getLineDuration(), tmp.getIntrinsicDuration());
                }
                tmp.setTimelineRange(lineStart, lineStart + 4);
                du = tmp.getTimelineTo() - tmp.getTimelineFrom();
                tmp.setAnimationList(loadAnimationScaleMove(tmp, du, show1));
                scene.addMedia(tmp);

            } else {
                if (i == (len - 1) && i > 2) {
                    //最后一段视频(         i=2+)
                    //先放大
                    mediaObject.setTimelineRange(lineStart, lineStart + 3f);
                    Rect clipRect = new Rect();
                    RectF show = new RectF(0, 0, 1f, 1f);
                    MiscUtils.fixClipRect(fixShowRectF(show), mediaObject.getWidth(), mediaObject.getHeight(), clipRect);
                    Rect dstClip = QuikHandler.createRect(clipRect, 0.5f);
                    mediaObject.setAnimationList(loadAnimationScale(mediaObject, new Rect(dstClip), show));
                    scene.addMedia(mediaObject);
                    du = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();


                    //向左边退出
                    MediaObject tmp = mediaObject.clone();
                    tmp.clearAnimationGroup();
                    lineStart += du;
                    tmp.setTimelineRange(lineStart, lineStart + 2);
                    if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        //视频的trim
                        tmp.setTimeRange(mediaObject.getLineDuration(), tmp.getIntrinsicDuration());
                    }
                    loadAnimation3(tmp, new Rect(dstClip));
                    scene.addMedia(tmp);
                    du = tmp.getTimelineTo() - tmp.getTimelineFrom();

                } else {
                    Rect clip = new Rect();
                    int re = i % 3;
                    if (re == 2) {
                        //放大->
                        mediaObject.setTimelineRange(lineStart, lineStart + 4f);
                        RectF show = new RectF(0, 0, 1f, 1f);
                        MiscUtils.fixClipRect(fixShowRectF(show), mediaObject.getWidth(), mediaObject.getHeight(), clip);
                        Rect dstClip = QuikHandler.createRect(clip, 0.5f);
                        mediaObject.addAnimationGroup(new AnimationGroup(loadAnimationScale(mediaObject, new Rect(dstClip), show)));
                        scene.addMedia(mediaObject);
                        du = mediaObject.getLineDuration();


                        //往上退出
                        lineStart += du;
                        MediaObject tmp = mediaObject.clone();
                        //移除clone的动画效果
                        tmp.clearAnimationGroup();
                        if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                            //视频的trim
                            tmp.setTimeRange(mediaObject.getLineDuration(), tmp.getIntrinsicDuration());
                        }
                        tmp.setTimelineRange(lineStart, lineStart + 2);
                        loadAnimation4(tmp, new Rect(dstClip));
                        scene.addMedia(tmp);
                        du = tmp.getTimelineTo() - tmp.getTimelineFrom();
                    } else {
                        RectF showRectF;
                        if (re == 1) {
                            //从右->左
                            showRectF = loadAnimation2(mediaObject, clip);
                        } else {
                            //从下->上
                            lineStart -= du * 0.2f;    //loadAnimation4  1秒
                            mediaObject.setTimelineRange(lineStart, lineStart + 1.0f);
                            showRectF = loadAnimation1(mediaObject, clip);
                        }
                        scene.addMedia(mediaObject);


                        du = mediaObject.getLineDuration();
                        //放大
                        lineStart += du;
                        MediaObject tmp = mediaObject.clone();
                        tmp.clearAnimationGroup();
                        tmp.setTimelineRange(lineStart, lineStart + 3);
                        if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                            //视频的trim
                            tmp.setTimeRange(mediaObject.getLineDuration(), tmp.getIntrinsicDuration());
                        }
                        du = tmp.getTimelineTo() - tmp.getTimelineFrom();
                        tmp.setAnimationList(loadAnimationScaleMove2(tmp, du, new RectF(showRectF), new Rect(clip)));
                        scene.addMedia(tmp);

                    }
                }
            }
            lineStart += du;

        }

    }

    private static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    /**
     * 拆成4个媒体，最后再合并成一个媒体，   从下到上
     *
     * @param mediaObject
     * @param clipRect    保留的媒体区域
     * @return 当前显示位置
     */
    private static RectF loadAnimation1(MediaObject mediaObject, Rect clipRect) {
        float[] fb = null;
        float[] fe = null;

        boolean isImage = mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE;
        if (isImage) {
            fb = new float[]{0.1f, 0.15f, 0.1f, 0.2f};
            fe = new float[]{0.7f, 0.75f, 0.8f, 0.85f};
        } else {
            fb = new float[]{0.1f, 0.1f, 0.1f, 0.1f};
            fe = new float[]{0.7f, 0.7f, 0.7f, 0.7f};
        }

        float count = fb.length;

        RectF show = new RectF(show1);
        Rect tmp = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(show), mediaObject.getWidth(), mediaObject.getHeight(), tmp);


        RectF dst = new RectF(show.left, show.top, show.left + (show.width() / count), show.bottom);

        RectF mclip = QuikHandler.createRect(new RectF(tmp), 0.5f);

        clipRect.set((int) mclip.left, (int) mclip.top, (int) mclip.right, (int) mclip.bottom);

        mediaObject.setClipRectF(mclip);
        try {
            mediaObject.changeFilter(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

        float itemShowWidthF = show.width() / count;

        //媒体被拆分成4个媒体,从左到右边
        int itemWidthPx = (int) (mclip.width() / count);

        Rect clip = new Rect((int) mclip.left, (int) mclip.top, (int) (mclip.left + itemWidthPx), (int) mclip.bottom);
        float duration = mediaObject.getLineDuration();
        for (int i = 0; i < count; i++) {
            clip = new Rect(clip);
            if (i > 0) {
                clip.offset(itemWidthPx, 0);
            }
            dst = new RectF(dst);
            if (i > 0) {
                dst.offset(itemShowWidthF, 0);
            }
            RectF rectF = new RectF(dst);
            rectF.offset(0, 1 * (i + 1));

            List<AnimationObject> list = new ArrayList<>();
            AnimationObject object;
            if (isImage) {

                object = new AnimationObject(0);
                object.setRectPosition(rectF);
                object.setClipRect(new Rect(clip));
                list.add(object);

                object = new AnimationObject(duration * fb[i]);
                object.setRectPosition(rectF);
                object.setClipRect(new Rect(clip));
                list.add(object);

            } else {
                object = new AnimationObject(0);
                object.setRectPosition(rectF);
                object.setClipRect(new Rect(clip));
                list.add(object);
            }


            object = new AnimationObject(duration * fe[i]);
            object.setRectPosition(dst);
            object.setClipRect(new Rect(clip));
            object.setAnimationInterpolation((i % 2 == 0) ? AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE : AnimationObject.AnimationInterpolation.DECELERATE);
            list.add(object);

            object = new AnimationObject(duration);
            object.setRectPosition(dst);
            object.setClipRect(new Rect(clip));
            list.add(object);
            mediaObject.addAnimationGroup(new AnimationGroup(0, duration, list));


        }
        return show;

    }

    /**
     * * 拆成5个媒体，最后再合并成一个媒体， 从右到左
     *
     * @param mediaObject
     * @param clipRect
     * @return
     */
    private static RectF loadAnimation2(MediaObject mediaObject, Rect clipRect) {

        RectF show1 = new RectF(0, 0, 1, 1);
        RectF show = new RectF(show1);
        MiscUtils.fixClipRect(fixShowRectF(show), mediaObject.getWidth(), mediaObject.getHeight(), clipRect);


        float[] fb = null;
        float[] fe = null;

        if (mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
            if (mAsp == QuikHandler.ASP_169) {
                fb = new float[]{0.1f, 2f, 0, 1.5f, 0.5f, 1.5f, 3f};
                fe = new float[]{6, 6.5f, 7f, 7.5f, 7.8f, 8.0f, 8.2f};
            } else {
                fb = new float[]{0.1f, 2f, 0, 1.5f, 0.5f, 1.5f, 3f, 3.5f, 4f};
                fe = new float[]{6, 6.5f, 7f, 7.5f, 7.8f, 8.0f, 8.2f, 8.3f, 8.5f};
            }
        } else {
            if (mAsp == QuikHandler.ASP_169) {
                fb = new float[]{0, 0, 0, 0, 0, 0, 0,};
                fe = new float[]{8f, 8f, 8f, 8f, 8f, 8f, 8f};
            } else {
                fb = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
                fe = new float[]{8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f};
            }
        }

        float count = fb.length;
        RectF dst = new RectF(show.left, show.top, show.right, show.top + (show.height() / count));

        float itemShowHeightF = show.height() / count;

        //媒体被拆分成4个媒体,从左到右边
        int h = clipRect.height();

        int itemHeightPx = (int) (h / count);
        Rect clip = new Rect(clipRect.left, clipRect.top, clipRect.right, clipRect.top + itemHeightPx);
        float duration = mediaObject.getLineDuration();
        boolean isImage = mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE;
        for (int i = 0; i < count; i++) {
            clip = new Rect(clip);
            if (i > 0) {
                clip.offset(0, itemHeightPx);
            }
            dst = new RectF(dst);
            if (i > 0) {
                dst.offset(0, itemShowHeightF);
            }
            RectF rectF = new RectF(dst);
            rectF.offset(1, 0);

            List<AnimationObject> list = new ArrayList<>();
            AnimationObject object;
            if (isImage) {
                object = new AnimationObject(0);
                object.setClipRect(clip);
                object.setRectPosition(rectF);
                list.add(object);

                object = new AnimationObject(duration / 10 * fb[i]);

            } else {
                object = new AnimationObject(duration / 10 * fb[i]);
                //每个小块的总路程不一致 （视频时，散乱效果）
                rectF.offset(0.5f * i, 0);
            }
            object.setRectPosition(rectF);
            object.setClipRect(clip);
            list.add(object);


            object = new AnimationObject(duration / 10 * fe[i]);
            object.setRectPosition(dst);
            object.setClipRect(clip);
            object.setAnimationInterpolation((i % 2 == 0) ? AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE : AnimationObject.AnimationInterpolation.DECELERATE);
            list.add(object);

            object = new AnimationObject(duration);
            object.setRectPosition(dst);
            object.setClipRect(clip);
            list.add(object);
            mediaObject.addAnimationGroup(new AnimationGroup(list));
        }
        return show1;

    }


    /**
     * 从右到左 ，退出屏幕
     *
     * @param mediaObject
     * @param srcClip
     */
    private static void loadAnimation3(MediaObject mediaObject, Rect srcClip) {


        RectF show = new RectF(0, 0, 1, 1);

        float[] fbArr = null;
        float[] fend = null;

        boolean isImage = mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE;

        if (isImage) {
            if (mAsp == QuikHandler.ASP_169) {
                fbArr = new float[]{0.1f, 1.5f, 1f, 1.2f, 1.9f, 2.3f};
                fend = new float[]{5, 5.5f, 6.0f, 6.5f, 7f, 7.5f};
            } else {
                fbArr = new float[]{0.1f, 1.5f, 1f, 1.2f, 1.9f, 2.3f, 2.5f, 3.0f};
                fend = new float[]{5, 5.5f, 6.0f, 6.5f, 7f, 7.5f, 7.8f, 8.1f};
            }
        } else {
            if (mAsp == QuikHandler.ASP_169) {
                fbArr = new float[]{0f, 0f, 0f, 0f, 0f, 0f};
                fend = new float[]{8f, 8f, 8f, 8f, 8f, 8f};
            } else {
                fbArr = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
                fend = new float[]{8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f};
            }
        }

        float count = fbArr.length;
        RectF dst = new RectF(show.left, show.top, show.right, show.top + (show.height() / count));

        float itemShowHeightF = show.height() / count;


        //媒体被拆分成4个媒体,从左到右边
        int h = (int) srcClip.height();

        int itemHeightPx = (int) (h / count);


        srcClip = new Rect(srcClip.left, srcClip.top, srcClip.right, srcClip.top + itemHeightPx);
        float duration = mediaObject.getTimelineTo() - mediaObject.getTimelineFrom();
        for (int i = 0; i < count; i++) {

            srcClip = new Rect(srcClip);
            if (i > 0) {
                srcClip.offset(0, itemHeightPx);
            }
            dst = new RectF(dst);
            if (i > 0) {
                dst.offset(0, itemShowHeightF);

            }

            List<AnimationObject> list = new ArrayList<>();
            AnimationObject object;
            if (isImage) {

                object = new AnimationObject(0);
                object.setRectPosition(dst);
                object.setClipRect(new Rect(srcClip));
                list.add(object);

                object = new AnimationObject(duration / 10 * fbArr[i]);
                object.setRectPosition(dst);
                object.setClipRect(new Rect(srcClip));
                list.add(object);
            } else {
                object = new AnimationObject(0);
                object.setRectPosition(dst);
                object.setClipRect(new Rect(srcClip));
                list.add(object);
            }


            object = new AnimationObject(duration);
            RectF rectF = new RectF(dst);
            if (isImage) {
                rectF.offset(-1, 0);
            } else {
                //视频多偏移点
                rectF.offset(-1 + ((count - i) * -0.5f), 0);
            }
            object.setClipRect(new Rect(srcClip));
            object.setRectPosition(rectF);
            object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
            list.add(object);


            mediaObject.addAnimationGroup(new AnimationGroup(list));


        }


    }


    /**
     * 放大平移
     *
     * @param tmp
     * @param duration
     * @param showRectF
     * @return
     */
    private static List<AnimationObject> loadAnimationScaleMove(MediaObject tmp, float duration, RectF showRectF) {
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(showRectF);
        Rect clipRect = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), tmp.getWidth(), tmp.getHeight(), clipRect);
        Rect clip = new Rect(clipRect);

        RectF mclip = QuikHandler.createRect(new RectF(clip), 0.5f);
        object.setClipRect(new Rect((int) mclip.left, (int) mclip.top, (int) mclip.right, (int) mclip.bottom));
        tmp.setShowRectF(showRectF);
        try {
            tmp.changeFilter(null);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        list.add(object);

        object = new AnimationObject(duration);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(showRectF);
        MiscUtils.fixClipRect(fixShowRectF(showRectF), tmp.getWidth(), tmp.getHeight(), clipRect);
        object.setClipRect(clipRect);
        list.add(object);
        return list;

    }

    /**
     * 放大平移
     *
     * @param tmp
     * @param duration
     * @param showRectF
     * @return
     */
    private static List<AnimationObject> loadAnimationScaleMove2(MediaObject tmp, float duration, RectF showRectF, Rect clip) {

        tmp.setShowRectF(new RectF(showRectF));
        tmp.setClipRectF(new RectF(clip));

        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(showRectF);
        object.setClipRect(new Rect(clip));
        list.add(object);

        object = new AnimationObject(duration);
        object.setRectPosition(new RectF(showRectF));
        object.setClipRect(QuikHandler.createRect(clip, 0.5f));
        list.add(object);
        return list;

    }

    /**
     * 全屏放大 (位置不变，clip变化)
     *
     * @param tmp
     * @param dstClip
     * @param src
     * @return
     */
    private static List<AnimationObject> loadAnimationScale(MediaObject tmp, Rect dstClip, RectF src) {
        float duration = tmp.getLineDuration();
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(src);
        Rect clipRect = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), tmp.getWidth(), tmp.getHeight(), clipRect);
        object.setClipRect(clipRect);
        tmp.setShowRectF(src);
        list.add(object);


        object = new AnimationObject(duration);
        object.setRectPosition(src);
        object.setClipRect(dstClip);
        list.add(object);
        return list;

    }


    /**
     * 从下到上，  退出屏幕
     *
     * @param mediaObject
     * @param clipRect    保留的媒体区域
     * @return 当前显示位置
     */
    private static RectF loadAnimation4(MediaObject mediaObject, Rect clipRect) {

        boolean isImage = mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE;

        float[] fb = null;
        float[] fe = null;

        if (isImage) {
            if (mAsp == QuikHandler.ASP_916) {
                fb = new float[]{0.1f, 1.5f, 1f, 2f, 2.1f};
                fe = new float[]{9f, 8.5f, 8f, 10.0f, 9.4f};
            } else {
                fb = new float[]{0.1f, 1.5f, 1f, 2f, 2.1f, 2.5f, 1.9f};
                fe = new float[]{9f, 8.5f, 8f, 10.0f, 9.4f, 8.3f, 9.9f};
            }
        } else {
            if (mAsp == QuikHandler.ASP_916) {
                fb = new float[]{0, 0f, 0f, 0f, 0f};
                fe = new float[]{10f, 10f, 10f, 10f, 10f};
            } else {
                fb = new float[]{0, 0f, 0f, 0f, 0f, 0f, 0f};
                fe = new float[]{10f, 10f, 10f, 10f, 10f, 10f, 10f};
            }
        }
        float count = fb.length;

        RectF show = new RectF(0, 0, 1f, 1f);
        Rect tmp = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(show), mediaObject.getWidth(), mediaObject.getHeight(), tmp);

        RectF dst = new RectF(show.left, show.top, show.left + (show.width() / count), show.bottom);

        Rect mclip = QuikHandler.createRect(new Rect(tmp), 0.5f);

        clipRect.set((int) mclip.left, (int) mclip.top, (int) mclip.right, (int) mclip.bottom);


        float itemShowWidthF = show.width() / count;

        //媒体被拆分成4个媒体,从左到右边
        int itemWidthPx = (int) (mclip.width() / count);

        Rect clip = new Rect(mclip.left, mclip.top, mclip.left + itemWidthPx, mclip.bottom);
        float duration = mediaObject.getLineDuration();
        for (int i = 0; i < count; i++) {
            clip = new Rect(clip);
            if (i > 0) {
                clip.offset(itemWidthPx, 0);
            }
            dst = new RectF(dst);
            if (i > 0) {
                dst.offset(itemShowWidthF, 0);
            }


            List<AnimationObject> list = new ArrayList<>();
            AnimationObject object;
            if (isImage) {
                object = new AnimationObject(0);
                object.setRectPosition(new RectF(dst));
                object.setClipRect(clip);
                list.add(object);

                object = new AnimationObject(duration / 10 * fb[i]);
                object.setRectPosition(new RectF(dst));
                object.setClipRect(clip);
                list.add(object);

            } else {
                object = new AnimationObject(0);
                object.setRectPosition(new RectF(dst));
                object.setClipRect(new Rect(clip));
                list.add(object);
            }


            if (isImage) {
                object = new AnimationObject(duration / 10 * fe[i]);
                RectF rectF = new RectF(dst);
                rectF.offset(0, -1);
                object.setRectPosition(rectF);
                object.setClipRect(clip);
                object.setAnimationInterpolation((i % 2 == 0) ? AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE : AnimationObject.AnimationInterpolation.DECELERATE);
                list.add(object);

                object = new AnimationObject(duration);
                object.setRectPosition(rectF);
                object.setClipRect(clip);
                list.add(object);
            } else {
                RectF rectF = new RectF(dst);
                rectF.offset(0, -1.0f + ((-0.5f) * i));
                object = new AnimationObject(duration);
                object.setAnimationInterpolation((i % 2 == 0) ? AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE : AnimationObject.AnimationInterpolation.DECELERATE);
                object.setRectPosition(rectF);
                object.setClipRect(clip);
                list.add(object);

            }
            mediaObject.addAnimationGroup(new AnimationGroup(list));
        }
        return show;

    }


}
