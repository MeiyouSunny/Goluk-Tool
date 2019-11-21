package com.rd.veuisdk.utils;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.graphics.Paint;
import com.rd.vecore.models.CanvasObject;
import com.rd.vecore.models.CustomDrawObject;
import com.rd.vecore.models.MediaObject;
import com.rd.veuisdk.model.CollageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 画中画manager
 */
public class CollageManager {
    private static final String TAG = "CollageManager";

    private static CustomDrawObject mCustomDrawObject;
    private static List<MediaObject> collageMediaList = new ArrayList<>();

    /**
     * 退出释放数据
     */
    public static void recycle() {
        if (null != mCustomDrawObject) {
            mCustomDrawObject.recycle();
            mCustomDrawObject = null;
        }
        if (null != collageMediaList) {
            collageMediaList.clear();
            collageMediaList = null;
        }
    }

    /***
     * 加载画中画
     * @param virtualVideo
     * @param mCollageInfos   画中画内容
     * @param duration   主媒体时长 单位：ms
     */
    public static void loadMix(VirtualVideo virtualVideo, final List<CollageInfo> mCollageInfos, int duration) {
        //画中画
        int len = 0;
        if (null == collageMediaList) {
            collageMediaList = new ArrayList<>();
        } else {
            collageMediaList.clear();
        }
        len = mCollageInfos.size();

        for (int i = 0; i < len; i++) {
            collageMediaList.add(mCollageInfos.get(i).getMediaObject());
        }
        final com.rd.vecore.graphics.Paint mPaint = new com.rd.vecore.graphics.Paint();
        mPaint.setAntiAlias(true);

        final float durationS = Utils.ms2s(duration);
        //创建自绘
        mCustomDrawObject = new CustomDrawObject(durationS) {
            //实现clone方法，方便通过预览时的虚拟视频，截图
            @Override
            public CustomDrawObject clone() {
                return null;
            }

            @Override
            public void draw(CanvasObject canvas, float progress) {
                onDraw(canvas, progress, durationS, mPaint);
            }
        };

        mCustomDrawObject.setMediaList(collageMediaList);
        virtualVideo.addCustomDraw(mCustomDrawObject);

    }

    private static void onDraw(CanvasObject canvas, float progress, float duration, Paint paint) {
        //当前进度 单位：秒
        float currentProgress = progress * duration;
        MediaObject info;
        int len = collageMediaList.size();
        for (int i = 0; i < len; i++) {
            info = collageMediaList.get(i);
            if (info.getTimelineFrom() <= currentProgress && currentProgress <= info.getTimelineTo()) {
                canvas.drawMediaObject(info, paint);
            }
        }
    }




    /**
     * 实时插入媒体(必须暂停状态下)
     *
     * @param info
     */
    public static void insertCollage(CollageInfo info) {
        if (null != info) {
            MediaObject mediaObject = info.getMediaObject();
            //实时插入 （用于解码）
            mCustomDrawObject.addMediaObject(mediaObject);
            collageMediaList.add(mediaObject); //用于绘制
        }
    }


    /***
     * 移除单个画中画
     * @param info
     */
    public static void remove(CollageInfo info) {
        if (null != info) {
            MediaObject mediaObject = info.getMediaObject();
            //实时移除
            mCustomDrawObject.remove(mediaObject);
            collageMediaList.remove(mediaObject); //用于绘制
        }
    }

    /**
     * 更新单个画中画
     *
     * @param info
     */
    public static void udpate(CollageInfo info) {
        if (null != info) {
            //先移除
            remove(info);
            //再添加
            insertCollage(info);
        }
    }
}
