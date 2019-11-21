package com.rd.veuisdk.quik;

import android.graphics.Rect;
import android.graphics.RectF;

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
class Seren {

    /**
     * 播放器view的位置
     */
    private static RectF mPlayerRectF;
    private final static String TAG = "Seren";
    private static float mScaleX = 1.0f, mScaleY = 1.0f;

    /**
     * @param scene
     * @param asp
     */
    public static void loadAnimation(Scene scene, float asp) {
        scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
        if (asp == QuikHandler.ASP_169) {
            mScaleX = QuikHandler.ASP_916;
            mScaleY = 1;
        } else if (asp == QuikHandler.ASP_916) {
            mScaleX = 1;
            mScaleY = QuikHandler.ASP_916;

        } else {
            mScaleX = QuikHandler.ASP_1;
            mScaleY = QuikHandler.ASP_1;
        }

        mPlayerRectF = QuikHandler.getShowRectF(asp);
        List<MediaObject> list = new ArrayList<>();
        list.addAll(scene.getAllMedia());
        int len = list.size();
        scene.getAllMedia().clear();
        float lineStart = 0;
        //每个图片n秒
        final float itemDu = 5f;
        boolean can4Frame = false;
        boolean bLeftExit = true;
        List<MediaObject> mediaObjects = new ArrayList<>();
        for (int n = 0; n < len; n++) {
            MediaObject mediaObject = list.get(n);
            float du = 0;
            if (n == 0) {
                mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                du = (mediaObject.getLineDuration());
                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    loadAnimVideo(mediaObject, du, bLeftExit);
                    bLeftExit = !bLeftExit;
                } else {
                    loadAnimation1(mediaObject, du);
                }

                mediaObjects.add(mediaObject);

            } else {

                if (n == (len - 1) && n > 1) { //最后一个
                    //缩小退出
                    lineStart -= 0f;
                    mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                    du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                    loadAnim5(mediaObject, du);
                    mediaObjects.add(mediaObject);
                } else {
                    int re = n % 14;
                    if (re == 1) {
                        //第0、1个媒体有交叉

                        //3层
                        lineStart -= itemDu * 0.1f;
                        mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                        du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                        loadAnimation2(mediaObject, du);
                        mediaObjects.add(mediaObject);
                    } else if (re == 2) {


                        lineStart -= itemDu * 0.05f;
                        //残影
                        MediaObject tmp = list.get(n - 1).clone();
                        tmp.clearAnimationGroup();
                        tmp.setTimelineRange(lineStart, lineStart + itemDu);
                        du = (tmp.getTimelineTo() - tmp.getTimelineFrom());
                        tmp.setAnimationList(loadAnimationHint2(tmp, du));
                        mediaObjects.add(tmp);


                        //3层
                        mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                        du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                        loadAnimation3(mediaObject, du);
                        mediaObjects.add(mediaObject);

                    } else if (re == 3) {


                        lineStart -= itemDu * 0.05f;
                        //第n-3个的残影
                        MediaObject tmp = list.get(n - 3).clone();
                        tmp.clearAnimationGroup();
                        tmp.setTimelineRange(lineStart, lineStart + (itemDu * 0.8f));
                        du = (tmp.getTimelineTo() - tmp.getTimelineFrom());
                        tmp.setAnimationList(loadAnimationHint(tmp, du));
                        mediaObjects.add(tmp);


                        //2层
                        mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                        du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                        mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                        loadAnimation1(mediaObject, du);
                        mediaObjects.add(mediaObject);

                    } else if (re == 4) {

                        if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                            lineStart -= itemDu * 0.1f;
                            mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                            du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                            loadAnimVideo1(mediaObject, du, bLeftExit);
                            bLeftExit = !bLeftExit;
                        } else {
                            lineStart -= itemDu * 0.1f;
                            mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                            du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                            loadAnim2(mediaObject, du, (n == 1) ? (new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY)) : null);
                        }
                        mediaObjects.add(mediaObject);


                    } else {
                        //以下项不会出现残影

                        if (re == 5) {
                            lineStart -= itemDu * 0.0f;
                            mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                            du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                            loadAnim4(mediaObject, du);
                        } else if (re == 6) { // loadAnim6     re == 6 与 re == 7同时显示 ， re==4 duration  更长

                            if (n <= (len - 2)) {

                                lineStart -= itemDu * 0.2f;
                                //下一个  (倒序添加)
                                MediaObject next = list.get(n + 1);
                                next.setTimelineRange(lineStart, lineStart + (itemDu * 2));
                                loadAnim6(next, itemDu, itemDu, 1);
                                mediaObjects.add(next);


                                mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                                loadAnim6(mediaObject, du, 0, 0);
                                du = (next.getTimelineTo() - next.getTimelineFrom());

                                n++;

                            } else {

                                mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                                loadAnim6(mediaObject, du, 0, 0);
                            }

                        } else if (re == 7) {
                            //re==6 已经 判断了next   ( ********* n++)


                        } else if (re == 8) { //loadAnim7 与loadAnim8 同时显示

                            lineStart -= itemDu * 0.05f;
                            if (n <= (len - 2)) {
                                MediaObject next = list.get(n + 1);
                                if (next.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                                    //图片两个同时显示，注意层级
                                    next.setTimelineRange(lineStart, lineStart + itemDu);
                                    du = (next.getTimelineTo() - next.getTimelineFrom());
                                    loadAnim8(next, du);
                                    mediaObjects.add(next);
                                    n++;


                                    mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                    du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                                    loadAnim7(mediaObject, du);
                                } else {
                                    //两个媒体，线性排列

                                    //当前视频
                                    mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                    du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                                    loadAnimVideo1(mediaObject, du, false);
                                    mediaObjects.add(mediaObject);


                                    //下一个视频
                                    lineStart += du;
                                    lineStart -= itemDu * 0.1f;
                                    mediaObject = list.get(n + 1);
                                    mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                    du = (mediaObject.getTimelineTo() - next.getTimelineFrom());
                                    loadAnimVideo1(mediaObject, du, false);
                                    n++;


                                }


                            } else {
                                mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                                loadAnim7(mediaObject, du);
                            }
                        } else if (re == 9) {
                            //re==8 时  ，已经读取了re==9

                        } else {

                            //后面的媒体足够执行 10、11、12、 13 、last  且图片
                            can4Frame = false;
                            if (re == 10) {
                                can4Frame = n < (len - 4) && mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE && list.get(n + 1).getMediaType() == MediaType.MEDIA_IMAGE_TYPE
                                        && list.get(n + 2).getMediaType() == MediaType.MEDIA_IMAGE_TYPE && list.get(n + 3).getMediaType() == MediaType.MEDIA_IMAGE_TYPE;
                            }

                            if (can4Frame) {
                                MediaObject next = list.get(n + 3);
                                next.setTimelineRange(lineStart, lineStart + (4 * itemDu));
                                loadAnim10(next, itemDu);
                                mediaObjects.add(next);


                                next = list.get(n + 2);
                                next.setTimelineRange(lineStart, lineStart + (3 * itemDu));
                                loadAnim11(next, itemDu);
                                mediaObjects.add(next);


                                next = list.get(n + 1);
                                next.setTimelineRange(lineStart, lineStart + (itemDu * 2));
                                loadAnim12(next, itemDu);
                                mediaObjects.add(next);


                                mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                loadAnim13(mediaObject, itemDu);


                                n += 3;// ***********************************读取了未来的3个媒体
                                du = 4 * itemDu;

                            } else {

                                lineStart -= itemDu * 0.1f;
                                mediaObject.setTimelineRange(lineStart, lineStart + itemDu);
                                du = (mediaObject.getTimelineTo() - mediaObject.getTimelineFrom());
                                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                                    loadAnimVideo1(mediaObject, du, bLeftExit);
                                } else {
                                    loadAnim3(mediaObject, du, bLeftExit);
                                }
                                bLeftExit = !bLeftExit;

                            }

                        }
                        //添加到集合
                        mediaObjects.add(mediaObject);
                    }
                }


            }
            lineStart += du;
        }

        list.clear();

        for (int i = 0; i < mediaObjects.size(); i++) {
            scene.addMedia(mediaObjects.get(i));
        }
    }


    static RectF fixShowRectF(RectF inView) {
        return QuikHandler.fixShowRectF(mPlayerRectF, inView);
    }

    /**
     * 两层
     *
     * @param mediaObject
     * @param duration
     */
    private static void loadAnimation1(MediaObject mediaObject, float duration) {
//            //第二层动画  (右往左)
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        RectF showF = new RectF(127 / 275.0f, 54 / 275.0f, 186 / 275.0f, 127 / 275.0f);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        RectF src = new RectF(showF);
        src.offset(0.8f, 0);
        object.setRectPosition(src);
        Rect clip = new Rect();
        RectF tmp = fixShowRectF(src);
        MiscUtils.fixClipRect(tmp, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        object = new AnimationObject(duration * 0.2f);
        src = new RectF(showF);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setRectPosition(src);

        clip = new Rect();
        tmp = fixShowRectF(showF);
        MiscUtils.fixClipRect(tmp, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.3f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.1f);
        clip = new Rect();
        tmp = fixShowRectF(src);
        MiscUtils.fixClipRect(tmp, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAlpha(0.5f);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.13f);
        clip = new Rect();
        tmp = fixShowRectF(src);
        MiscUtils.fixClipRect(tmp, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAlpha(0.5f);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        list.add(object);

        object = new AnimationObject(duration);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1f);
        object.setAlpha(0.5f);
        clip = new Rect();
        tmp = fixShowRectF(src);
        MiscUtils.fixClipRect(tmp, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        src.offset(-0.7f, -0.105f);
        object.setRectPosition(src);
        list.add(object);


        mediaObject.addAnimationGroup(new AnimationGroup(0, duration, list));


        //一个媒体对象绑定多层动画序列
        //第一层动画序列
        list = new ArrayList<>();
        object = new AnimationObject(0);
        showF = new RectF(54 / 275.0f, 74 / 275.0f, 145 / 275.0f, 205 / 275.0f);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        src = new RectF(showF);
        src.offset(0.8f, 0.2f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        src = new RectF(showF);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.3f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(showF), 1.05f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.25f);
        object.setRectPosition(src);
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        object = new AnimationObject(duration);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(src, 1f);
        src.offset(-0.95f, 0);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        object.setRectPosition(src);
        list.add(object);
        mediaObject.addAnimationGroup(new AnimationGroup(list));


    }


    /**
     * 第3个显示第一个的hint   （黑白从最右->左->右移动）
     *
     * @param mediaObject
     * @param duration
     * @return
     */
    private static List<AnimationObject> loadAnimationHint(MediaObject mediaObject, float duration) {


        RectF dst2 = new RectF(13 / 275.0f, 68 / 275.0f, 75 / 275.0f, 146 / 275.0f);
        RectF dst1 = new RectF(-19 / 275.0f, 68 / 275.0f, 41 / 275.0f, 146 / 275.0f);
        RectF dst0 = new RectF(400 / 410.0f, 116 / 410.0f, (400 + (350 - 278)) / 410.0f, 235 / 410.0f);

        dst0 = QuikHandler.createRect(dst0, mScaleX, mScaleY);
        dst2 = QuikHandler.createRect(dst2, mScaleX, mScaleY);
        dst1 = QuikHandler.createRect(dst1, mScaleX, mScaleY);


        List<AnimationObject> list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        object.setRectPosition(dst0);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(dst0), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAlpha(0.2f);
        list.add(object);


        object = new AnimationObject(duration * 0.3f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setRectPosition(dst1);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(dst1), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setAlpha(1f);
        list.add(object);


        object = new AnimationObject(duration);
        object.setRectPosition(dst2);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(dst2), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        list.add(object);
        return list;

    }

    /**
     * 3层
     *
     * @param mediaObject
     * @param duration
     * @return
     */
    private static void loadAnimation2(MediaObject mediaObject, float duration) {
        List<AnimationObject> list = new ArrayList<>();

//            //第3层动画  (右往左)
        AnimationObject object = new AnimationObject(0);

        RectF showF = new RectF((134 - 54) / 275.0f, 152 / 275.0f, (212 - 54) / 275.0f, 207 / 275.0f);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        RectF src = new RectF(showF);
        src.offset(0.3f, 0);
        object.setRectPosition(src);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(showF), 1.0f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.33f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR));
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1f);
        src.offset(-0.6f, -0.105f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        mediaObject.addAnimationGroup(new AnimationGroup(0, duration, list));


//            //第二层动画  (右往左)

        list = new ArrayList<>();
        object = new AnimationObject(0);
        showF = new RectF(68 / 275.0f, 180 / 275.0f, 146 / 275.0f, 235 / 275.0f);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        src = new RectF(showF);
        src.offset(0.35f, 0.102f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(showF), 1f);
        object.setAlpha(0.6f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        object.setAlpha(0.6f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.2f);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration);
        object.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        object.setAlpha(0.6f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1f);
        src.offset(-0.9f, 0.005f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);
        mediaObject.addAnimationGroup(new AnimationGroup(0, duration, list));


        //一个媒体对象绑定多层动画序列
        //第一层动画序列
        list = new ArrayList<>();
        object = new AnimationObject(0);
        showF = new RectF(46 / 275.0f, 65 / 275.0f, 253 / 275.0f, 220 / 275.0f);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        src = QuikHandler.createRect(new RectF(showF), 0.8f);
        src.offset(0.8f, 0f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(showF), 0.9f);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(showF), 1);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        object = new AnimationObject(duration);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(src, 1f);
        src.offset(0 - src.right, 0);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);
        mediaObject.addAnimationGroup(new AnimationGroup(0, duration, list));


    }

    /**
     * 3层。显示上一个媒体的残影
     *
     * @param mediaObject
     * @param duration
     * @return
     */
    private static List<AnimationObject> loadAnimationHint2(MediaObject mediaObject, float duration) {

        List<AnimationObject> list = new ArrayList<>();
        final float pw = 330.0f, ph = 330.0f;

//         第4层动画
        AnimationObject object = new AnimationObject(0);

        RectF src = new RectF(169 / pw, 154 / ph, 241 / pw, 248 / ph);
        src = QuikHandler.createRect(src, mScaleX, mScaleY);
        src.offset(0.05f, 0);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        mediaObject.setClipRectF(new RectF(clip));
        mediaObject.setShowRectF(src);
        object.setRectPosition(new RectF(src));
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        src = new RectF(-6 / pw, 160 / ph, 48 / pw, 236 / ph);
        src = QuikHandler.createRect(src, mScaleX, mScaleY);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.33f);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 0.95f);
        object.setRectPosition(src);
        object.setAlpha(0.5f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);
        return list;

    }

    /**
     * 3层
     *
     * @param mediaObject
     * @param duration
     * @return
     */
    private static void loadAnimation3(MediaObject mediaObject, float duration) {
        List<AnimationObject> list;
        final float pw = 330.0f, ph = 330.0f;

        //第3层动画
        list = new ArrayList<>();
        AnimationObject object = new AnimationObject(0);
        RectF showF = new RectF(241 / pw, 81 / ph, 294 / pw, 173 / ph);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        RectF src = new RectF(showF);
        object.setRectPosition(src);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        src = new RectF(0 / pw, 76 / ph, 65 / pw, 166 / ph);
        src = QuikHandler.createRect(src, mScaleX, mScaleY);
        object.setAlpha(0.6f);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.1f);
        src.offset(0.15f, 0);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration);
        object.setAlpha(0.6f);
        src = new RectF(src);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        src.offset(-0.1f, 0);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);
        mediaObject.addAnimationGroup(new AnimationGroup(0, duration, list));


        //第2 层
        list = new ArrayList<>();
        object = new AnimationObject(0);
        showF = new RectF(266 / pw, 124 / ph, 303 / pw, 192 / ph);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        showF.offset(1f, 0);
        src = QuikHandler.createRect(new RectF(showF), 1.5f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        src = new RectF(src);
        src.offset(-1, 0);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(src), 1.1f);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        object = new AnimationObject(duration);
        object.setRectPosition(new RectF(src));
        clip = new Rect();
        object.setAlpha(0.5f);
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);
        AnimationGroup animationGroup = new AnimationGroup(0, duration, list);
        animationGroup.setAudioMute(true);//视频静音
        mediaObject.addAnimationGroup(animationGroup);


        //第1层
        list = new ArrayList<>();
        object = new AnimationObject(0);
        showF = new RectF(77 / pw, 40 / ph, 275 / pw, 303 / ph);
        showF = QuikHandler.createRect(showF, mScaleX, mScaleY);
        src = QuikHandler.createRect(new RectF(showF), 1f);
        src.offset(1, 0);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.2f);
        showF = new RectF(src);
        showF.offset(-1, 0);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(showF), 1);
        object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);

        object = new AnimationObject(duration * 0.8f);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(new RectF(showF), 1.05f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        list.add(object);


        object = new AnimationObject(duration);
        src = com.rd.veuisdk.quik.QuikHandler.createRect(showF, 1f);
        object.setRectPosition(src);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(src), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        object.setClipRect(clip);
        object.setAlpha(0.8f);
        list.add(object);
        //视频静音
        animationGroup = new AnimationGroup(0, duration, list);
        animationGroup.setAudioMute(true);
        mediaObject.addAnimationGroup(animationGroup);

    }


    private static void loadAnim2(MediaObject mediaObject, float du, VisualFilterConfig config) {

        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        final float size = 230.0f;

        RectF showRectF = new RectF(68 / size, 99 / size, 144 / size, 172 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        mediaObject.setClipRectF(new RectF(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        animationObject = new AnimationObject(du * ((null == config) ? 0.2f : 0.5f));
        animationObject.addVisualFilterConfig(config);
        showRectF = new RectF(65 / size, 95 / size, 150 / size, 177 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du * 0.8f);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        showRectF = new RectF(19 / size, 17 / size, 211 / size, 213 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du);
        showRectF = new RectF(0f, 0f, 1f, 1f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRectPosition(showRectF);
        animationObject.setAlpha(0.8f);
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    /**
     * @param mediaObject
     * @param du
     * @param bLeftExit   true 往左退出，false 往右退出
     */
    private static void loadAnim3(MediaObject mediaObject, float du, boolean bLeftExit) {
        final float size = 305.0f;
        List<AnimationObject> list = new ArrayList<>();

        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(76 / size, 83 / size, 154 / size, 194 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        animationObject = new AnimationObject(du * 0.4f);
        showRectF = new RectF(58 / size, 20 / size, 234 / size, 275 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du * 0.8f);
        showRectF = new RectF(43 / size, 8 / size, 247 / size, 291 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRectPosition(showRectF);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);


        animationObject = new AnimationObject(du);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        showRectF = new RectF(showRectF);
        showRectF = QuikHandler.createRect(showRectF, 2f);
//        if (bLeftExit) {
        showRectF.offset(0.1f - showRectF.right, 0);
//        } else {
//            showRectF.offset(0.9f - showRectF.left, 0);
//        }
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(clip);
        animationObject.setRectPosition(showRectF);
        animationObject.setAlpha(0f);
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    private static void loadAnim4(MediaObject mediaObject, float du) {


        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        final float size = 270.0f;
        RectF showRectF = new RectF(140 / size, 119 / size, 257 / size, 190 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        animationObject = new AnimationObject(du * 0.4f);

        showRectF = new RectF(18 / size, 78 / size, 257 / size, 207 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setRectPosition(showRectF);
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);


        animationObject = new AnimationObject(du * 0.8f);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        RectF dst = new RectF(-6 / size, 57 / size, 273 / size, 207 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(dst), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setRectPosition(new RectF(dst));
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);


        animationObject = new AnimationObject(du);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        dst = new RectF(dst);
        dst = QuikHandler.createRect(dst, 2f);

        dst.offset(0.95f - dst.left, 0);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(dst), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setRectPosition(new RectF(dst));
        animationObject.setClipRect(new Rect(clip));
        animationObject.setAlpha(0.1f);
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    /**
     * 缩小
     *
     * @param mediaObject
     * @param du
     */
    private static void loadAnim5(MediaObject mediaObject, float du) {

        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        final float size = 265.0f;
        RectF showRectF = new RectF(0 / size, 62 / size, 265 / size, 206 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        animationObject = new AnimationObject(du * 0.8f);
        showRectF = new RectF(24 / size, 64 / size, 228 / size, 187 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setRectPosition(showRectF);
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);


        animationObject = new AnimationObject(du);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
        showRectF = QuikHandler.createRect(new RectF(showRectF), 0.95f);
        animationObject.setRectPosition(new RectF(showRectF));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setAlpha(0.1f);
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    /**
     * @param mediaObject
     * @param du
     * @param lineF
     */
    private static void loadAnim6(MediaObject mediaObject, float du, float lineF, int index) {
        final float size = 230.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(116 / size, 69 / size, 206 / size, 161 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        if (index > 0) {
            showRectF = QuikHandler.createRect(new RectF(showRectF), 1 + (0.05f * index));
            showRectF.offset(showRectF.width() * 0.05f * index, -(showRectF.height() * 0.1f * index));
        }

        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        mediaObject.setClipRectF(new RectF(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        if (index > 0) {
            animationObject = new AnimationObject(lineF / 2);
            showRectF = new RectF(showRectF);
            showRectF.offset(0.1f, 0);
            animationObject.setRectPosition(showRectF);
            animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
            animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
            animationObject.setClipRect(new Rect(clip));
            list.add(animationObject);
        }

        showRectF = new RectF(showRectF);

        if (index > 0) {
            animationObject = new AnimationObject(lineF - (du * 0.11f));
            showRectF.offset(-0.1f, 0);
            animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        } else {
            animationObject = new AnimationObject(lineF + (du * 0.11f));
            animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
            showRectF.offset(-0.05f, 0.05f);
        }
        animationObject.setRectPosition(showRectF);
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);

        animationObject = new AnimationObject(lineF + (du * 0.5f));
        showRectF = QuikHandler.createRect(new RectF(showRectF), 1.2f);
        showRectF.offset(-showRectF.width() / 2.0f, showRectF.height() / 2.0f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(lineF + (du * 0.8f));
        RectF tmp = new RectF(0, 0, 1, 1);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(tmp), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(tmp);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);

        animationObject = new AnimationObject(lineF + du);
        tmp = new RectF(tmp);
        tmp.offset(-tmp.width(), 0);
        animationObject.setRectPosition(tmp);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(tmp), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);
        mediaObject.setAnimationList(list);

    }

    private static void loadAnim7(MediaObject mediaObject, float du) {
        final float size = 330.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(84 / size, 93 / size, 171 / size, 217 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject((du * 0.8f));
        RectF tmp = new RectF(45 / size, 0, 287 / size, 1f);
        tmp = QuikHandler.createRect(tmp, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(tmp), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(tmp);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);

        animationObject = new AnimationObject(du);
        tmp = QuikHandler.createRect(new RectF(tmp), 2f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(tmp), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setAlpha(0.8f);
        animationObject.setRectPosition(tmp);
        list.add(animationObject);
        mediaObject.setAnimationList(list);

    }

    private static void loadAnim8(MediaObject mediaObject, float du) {
        final float size = 330.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(149 / size, 133 / size, 213 / size, 191 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject((du * 0.75f));
        RectF tmp = new RectF(163 / size, 133 / size, 288 / size, 221 / size);
        tmp = QuikHandler.createRect(tmp, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(tmp), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(tmp);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);

        animationObject = new AnimationObject((du));
        tmp = new RectF(tmp);
        tmp.offset(0.01f, 0);
        animationObject.setAlpha(0.1f);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(tmp);
        list.add(animationObject);
        mediaObject.setAnimationList(list);

    }


    /**
     * loadAnim10 、loadAnim11、loadAnim12  、loadAnim13， 4个媒体，多层显示 （伪3D效果）
     *
     * @param mediaObject
     * @param du
     */

    private static void loadAnim10(MediaObject mediaObject, float du) {
        float size = 410.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(134 / size, 179 / size, 184 / size, 220 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        size = 320.0f;
        animationObject = new AnimationObject(du * 0.45f);
        showRectF = new RectF(140 / size, 146 / size, 189 / size, 184 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du + (du * 0.45f));
        showRectF = new RectF(76 / size, 146 / size, 109 / size, 186 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(2 * du + (du * 0.85f));
        showRectF = new RectF(194 / size, 146 / size, 244 / size, 216 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);

        animationObject = new AnimationObject(du * 3 + (du * 0.25f));
        size = 408.0f;
        showRectF = new RectF(24 / size, 61 / size, 386 / size, 345 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        animationObject.setRectPosition(showRectF);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);


        animationObject = new AnimationObject(du * 3 + (du * 0.8f));
        showRectF = QuikHandler.createRect(showRectF, 1.2f);
        animationObject.setRectPosition(showRectF);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);

        animationObject = new AnimationObject(du * 4);
        animationObject.setAlpha(0.1f);
        showRectF = QuikHandler.createRect(showRectF, 2f);
        animationObject.setRectPosition(showRectF);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    private static void loadAnim11(MediaObject mediaObject, float du) {
        float size = 410.0f;
        List<AnimationObject> list = new ArrayList<>();

        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(104 / size, 147 / size, 136 / size, 206 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(showRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        size = 320.0f;
        animationObject = new AnimationObject(du * 0.45f);
        showRectF = new RectF(111 / size, 112 / size, 147 / size, 174 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(showRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du + (du * 0.45f));
        showRectF = new RectF(4 / size, 90 / size, 53 / size, 185 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(showRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        animationObject = new AnimationObject(du + (du * 0.8f));
        showRectF = QuikHandler.createRect(showRectF, 1.1f);
        clip = new Rect();
        MiscUtils.fixClipRect(showRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.addVisualFilterConfig(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GRAY));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(2 * du + (du * 0.25f));
        showRectF = new RectF(87 / size, 20 / size, 208 / size, 258 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(showRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);

        animationObject = new AnimationObject(2 * du + (du * 0.8f));
        showRectF = QuikHandler.createRect(new RectF(showRectF), 1.2f);
        clip = new Rect();
        MiscUtils.fixClipRect(showRectF, mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);


        animationObject = new AnimationObject(3 * du);
        showRectF = QuikHandler.createRect(showRectF, 2f);
        showRectF.offset(-0.5f, 0.5f);
        animationObject.setAlpha(0.1f);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    private static void loadAnim12(MediaObject mediaObject, float du) {
        float size = 410.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);

        RectF showRectF = new RectF(175 / size, 131 / size, 215 / size, 218 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        size = 320.0f;
        animationObject = new AnimationObject((du * 0.45f));
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        showRectF = new RectF(225 / size, 89 / size, 279 / size, 197 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        showRectF.offset(0.08f, 0);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        animationObject = new AnimationObject((du * 0.8f));
        clip = new Rect();
        showRectF = QuikHandler.createRect(showRectF, 1.025f);
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du + (du * 0.45f));
        animationObject.setClipRect(new Rect(clip));
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        showRectF = new RectF(89 / size, 20 / size, 217 / size, 278 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du + (du * 0.8f));
        showRectF = QuikHandler.createRect(new RectF(showRectF), 1.2f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);


        animationObject = new AnimationObject(du + (du));
        animationObject.setAlpha(0.1f);
        showRectF = QuikHandler.createRect(showRectF, 2f);
        showRectF.offset(0.5f, 0.5f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    private static void loadAnim13(MediaObject mediaObject, float du) {
        float size = 410.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(0 / size, 105 / size, 116 / size, 254 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject((du * 0.45f));
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        showRectF = new RectF(69 / size, 24 / size, 338 / size, 384 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject((du * 0.8f));
        showRectF = QuikHandler.createRect(new RectF(showRectF), 1.2f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        list.add(animationObject);


        animationObject = new AnimationObject(du);
        animationObject.setAlpha(0.1f);
        showRectF = QuikHandler.createRect(showRectF, 2f);
        showRectF.offset(-0.5f, 0.5f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        mediaObject.setAnimationList(list);

    }

    /**
     * 第0个视频
     *
     * @param mediaObject
     * @param du
     * @param bExitByLeft
     */
    private static void loadAnimVideo(MediaObject mediaObject, float du, boolean bExitByLeft) {
        float size = 410.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(98 / size, 43 / size, 289 / size, 372 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        animationObject = new AnimationObject(du * 0.8f);
        showRectF = QuikHandler.createRect(showRectF, 1.2f);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du);
        showRectF = new RectF(showRectF);
        showRectF.offset(0 - showRectF.right, 0);
//        showRectF.offset(bExitByLeft ? -0.5f : 0.5f, 0);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setAlpha(0.8f);
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        mediaObject.setAnimationList(list);
    }

    private static void loadAnimVideo1(MediaObject mediaObject, float du, boolean bExitByLeft) {
        float size = 410.0f;
        List<AnimationObject> list = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(0);
        RectF showRectF = new RectF(98 / size, 43 / size, 289 / size, 372 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        showRectF.offset(1 - showRectF.left, 0);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du * 0.2f);
        showRectF = new RectF(98 / size, 43 / size, 289 / size, 372 / size);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du * 0.8f);
        showRectF = new RectF(0, 0, 1, 1);
        showRectF = QuikHandler.createRect(showRectF, mScaleX, mScaleY);
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE_DECELERATE);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);


        animationObject = new AnimationObject(du);
        showRectF = new RectF(showRectF);
        showRectF.offset(0 - showRectF.right, 0);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(showRectF), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(new Rect(clip));
        animationObject.setAlpha(0.8f);
        animationObject.setRectPosition(showRectF);
        list.add(animationObject);

        mediaObject.setAnimationList(list);
    }

}
