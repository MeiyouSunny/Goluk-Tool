package com.rd.veuisdk.quik;

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

class Boxed {

    private static final String TAG = "Boxed";

    static RectF fixShowRectF(RectF inView) {
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
        List<MediaObject> allMedias = scene.getAllMedia();
        mPlayerRectF = QuikHandler.getShowRectF(asp);
        float lineStart = 0;
        int len = allMedias.size();
        for (int n = 0; n < len; n++) {
            MediaObject mediaObject = allMedias.get(n);
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
                mediaObject.addAnimationGroup(loadAnimation(mediaObject, n, 0, mediaObject.getLineDuration(), asp));
            }
//            Log.e(TAG, "loadAnimation: " + n + "/" + len + "   line:" + mediaObject.getTimelineFrom() + "<>" + mediaObject.getTimelineTo());
            lineStart = tLineTo;
        }


    }


    private static final float FULLSCREENPHOTO = 20 / 9.0f;

    /**
     * @param mediaObject
     * @param startTime
     * @param endTime
     * @param asp
     * @return
     */
    private static AnimationGroup loadAnimation(MediaObject mediaObject, int i, float startTime, float endTime, float asp) {

        float aspect = (float) mediaObject.getWidth() / mediaObject.getHeight();
        //保证上下左右四边至少间隔0.1f

        RectF showRectF;
        float minMargin = 0.1f;
        if (aspect >= FULLSCREENPHOTO) {
            //全景图片   （优先显示图片内容）
            minMargin = 0.0f;
        }
        float maxSolid = 1 - (2 * minMargin);
        if (asp == QuikHandler.ASP_169) {
            //上下保留margin
            float videoW = (maxSolid) * aspect;
            float left = (1 - videoW) / 2;
            showRectF = new RectF(left, minMargin, 1 - left, 1 - minMargin);

        } else if (asp == QuikHandler.ASP_1) {
            //上下保留margin
            float videoW = (maxSolid) * aspect;
            float left = (1 - videoW) / 2;
            showRectF = new RectF(left, minMargin, 1 - left, 1 - minMargin);

        } else {
            //9：16 时，降低边框距离
            if (aspect >= FULLSCREENPHOTO) {
                //全景图片   （优先显示图片内容）
                minMargin = 0.0f;
            } else {
                minMargin = 0.05f;
            }
            maxSolid = 1 - (2 * minMargin);

            //左右保留margin
            float videoHeight = (maxSolid) * aspect;
            float top = (1 - videoHeight) / 2;
            showRectF = new RectF(minMargin, top, 1 - minMargin, 1 - top);

        }

        float scale = maxSolid / Math.max(showRectF.width(), showRectF.height());

        //缩小，保证每一边至少间隔0.1f
        RectF target = QuikHandler.createRect(showRectF, scale);
//        Log.e(TAG, "loadAEFragment: " + showRectF + "   " + target + "   " + scale);

        //两个缩小，两个放大循环执行
        boolean isScale = (i % 4) < 2;

        List<AnimationObject> listAnimation = new ArrayList<>();
        AnimationObject animationObject = new AnimationObject(startTime);
        RectF rectStart = new RectF(target);
        animationObject.setRectPosition(rectStart);
        Rect clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);


        animationObject.setClipRect(QuikHandler.createRect(clip, isScale ? 1 : 0.6f));
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime - 0.35f);
        RectF rectEnd = new RectF(target);
        animationObject.setRectPosition(rectEnd);
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(QuikHandler.createRect(clip, isScale ? 0.75f : 0.95f));
        listAnimation.add(animationObject);

        animationObject = new AnimationObject(endTime);
        animationObject.setRectPosition(new RectF(target));
        clip = new Rect();
        MiscUtils.fixClipRect(fixShowRectF(animationObject.getRectPosition()), mediaObject.getWidth(), mediaObject.getHeight(), clip);
        animationObject.setClipRect(QuikHandler.createRect(clip, isScale ? 0.65f : 1f));
        animationObject.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
        listAnimation.add(animationObject);

        return new AnimationGroup(listAnimation);
    }

}
