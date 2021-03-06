package com.rd.veuisdk.export;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;

import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.model.FrameInfo;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.TimeArray;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.ui.SinglePointRotate;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;

/**
 * 贴纸实时插入
 *
 * @create 2019/9/11
 */
public class StickerExportHandler {

    private Context mContext;
    private final float mOutVideoWidth;
    private final float mOutVideoHeight;
    private ArrayList<StickerInfo> mStickerInfos;
    private final String TAG = "StickerExportHandler";

    /**
     * 多个贴纸 - 实时生成lite列表
     *
     * @param list
     * @param outVideoWidth
     * @param outVideoHeight
     */
    public StickerExportHandler(Context context, ArrayList<StickerInfo> list, int outVideoWidth, int outVideoHeight) {
        mContext = context;
        mStickerInfos = list;
        mOutVideoWidth = outVideoWidth + 0.0f;
        mOutVideoHeight = outVideoHeight + 0.0f;
    }

    private StickerInfo mStickerInfo;

    /**
     * 单个贴纸 -生成lite列表
     *
     * @param stickerInfo
     * @param outVideoWidth
     * @param outVideoHeight
     */
    public StickerExportHandler(Context context, StickerInfo stickerInfo, int outVideoWidth, int outVideoHeight) {
        mContext = context;
        mStickerInfo = stickerInfo;
        mOutVideoWidth = outVideoWidth + 0.0f;
        mOutVideoHeight = outVideoHeight + 0.0f;
    }

    /**
     * 快速构建liteObject
     *
     * @param virtualVideo null!=virtualVideo 时，构建list完成后，更新到播放器；
     */
    public void export(VirtualVideo virtualVideo) {
        if (null != mStickerInfos) { //贴纸列表
            for (StickerInfo stickerInfo : mStickerInfos) {
                stickerInfo.recycle();
                StyleInfo styleInfo = StickerUtils.getInstance().getStyleInfo(stickerInfo.getStyleId());
                if (null != styleInfo) {
                    createLiteObjectList(stickerInfo, styleInfo);
                }
            }
        } else if (null != mStickerInfo) { //单个贴纸


            if (null != virtualVideo) {
                //从虚拟视频中删除旧的对象（实时生效）
                mStickerInfo.removeListLiteObject(virtualVideo);
            }
            mStickerInfo.recycle();  //清理全部旧的list资源
            StyleInfo styleInfo = StickerUtils.getInstance().getStyleInfo(mStickerInfo.getStyleId());
            if (null != styleInfo) {
                //构建新的list资源
                createLiteObjectList(mStickerInfo, styleInfo);
            }

//测试单帧画面
//            CaptionLiteObject liteObject = initFrameData(mStickerInfo, styleInfo, styleInfo.frameArray.valueAt(styleInfo.frameArray.size() - 1));
//            liteObject.setTimelineRange(Utils.ms2s(mStickerInfo.getStart()), Utils.ms2s(mStickerInfo.getEnd()));
//            mStickerInfo.addSubObject(liteObject);

            if (null != virtualVideo) { //插入新的对象
                ArrayList<CaptionLiteObject> tmp = mStickerInfo.getList();
                int count = tmp.size();
                for (int j = 0; j < count; j++) {
                    virtualVideo.updateSubtitleObject(tmp.get(j));  //实时插入新的lite对象到虚拟视频
                }
            }
        }
    }

    private RectF createRectF(StickerInfo info) {
        Rect tmpRect = info.getRectOriginal();
        return new RectF(tmpRect.left / mOutVideoWidth, tmpRect.top / mOutVideoHeight, tmpRect.right / mOutVideoWidth, tmpRect.bottom / mOutVideoHeight);
    }


    /**
     * 普通贴纸(单帧画面)
     *
     * @param stickerInfo
     * @param styleInfo
     * @param picPath     单帧贴纸画面
     * @param centerX
     * @param centerY
     * @param rectF       贴纸的显示位置  0~~1.0f
     */
    private void initFrameData(StickerInfo stickerInfo, StyleInfo styleInfo, String picPath, int centerX, int centerY, RectF rectF) {
        SinglePointRotate singlePointRotate = new SinglePointRotate(mContext, stickerInfo.getRotateAngle(),
                TextUtils.isEmpty(stickerInfo.getText()) ? styleInfo.getHint() : stickerInfo.getText(),
                stickerInfo.getTextColor() == Color.WHITE ? styleInfo.getTextDefaultColor() : stickerInfo
                        .getTextColor(), stickerInfo.getTtfLocalPath(), stickerInfo.getDisf(),
                new Point((int) mOutVideoWidth, (int) mOutVideoHeight), new Point(centerX,
                centerY), stickerInfo.getTextSize(), stickerInfo.getShadowColor(), styleInfo,
                picPath);

        Rect tmp = singlePointRotate.getOriginalRect();

        stickerInfo.setRectOriginal(tmp);
        rectF.set(tmp.left / mOutVideoWidth, tmp.top / mOutVideoHeight, tmp.right / mOutVideoWidth, tmp.bottom / mOutVideoHeight);
        singlePointRotate.recycle();
    }


    /**
     * 构建lite列表
     *
     * @param stickerInfo
     * @param styleInfo
     */
    private void createLiteObjectList(StickerInfo stickerInfo, StyleInfo styleInfo) {
        //两种预览比例一致
        float asp = mOutVideoWidth / mOutVideoHeight;
        //增加容错
        boolean isCommonAsp = (stickerInfo.getPreviewAsp() == asp) || Math.abs(stickerInfo.getPreviewAsp() - asp) < 0.05f;

        int centerX = (int) (stickerInfo.getCenterxy()[0] * mOutVideoWidth);
        int centerY = (int) (stickerInfo.getCenterxy()[1] * mOutVideoHeight);

        // LiteObject目标显示位置  0~1.0f
        RectF showRectF;
        if (!isCommonAsp) {
            RectF rectF = new RectF();

            if (mOutVideoWidth != stickerInfo.getParentWidth()) {
                //预览size改变 -需要从新计算显示的缩放系数
                float disf = mOutVideoWidth / stickerInfo.getParentWidth() * stickerInfo.getDisf();
                stickerInfo.setDisf(disf);
            }
            //比例改变之后，需要重新修正显示位置，保证（中心点、旋转角度、缩放比例不变）
            initFrameData(stickerInfo, styleInfo, styleInfo.frameArray.valueAt(0).pic, centerX, centerY, rectF);
            showRectF = new RectF(rectF);
        } else {
            showRectF = createRectF(stickerInfo);
        }

        FrameInfo st = null;
        int mItemTime = styleInfo.getFrameDuration();
        int mdu = (int) (stickerInfo.getEnd() - stickerInfo.getStart());
        int timeArraySize = styleInfo.timeArrays.size();
        CaptionLiteObject liteObject = null;

        if (timeArraySize == 2 || timeArraySize == 3) {
            // 前面不循环
            // 处理不循环的部分
            TimeArray headArray = styleInfo.timeArrays.get(0);

            int tCount = headArray.getDuration() / mItemTime;
            for (int m = 0; m < tCount; m++) {
                st = styleInfo.frameArray.valueAt(m);
                if (null == st) {
                    continue;
                }
                liteObject = initFrameData(stickerInfo, st, showRectF);
                int nstart = (int) stickerInfo.getStart() + m * mItemTime;
                if (nstart < stickerInfo.getEnd()) { // 子动画在该特效时间区域内
                    int nend = nstart + mItemTime;
                    liteObject.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(Math.min(stickerInfo.getEnd(), nend)));
                    stickerInfo.addSubObject(liteObject);
                }
            }
            int mTstart = (int) (stickerInfo.getStart() + headArray.getDuration());
            if (mTstart < stickerInfo.getEnd()) {
                // 处理循环部分
                if (timeArraySize == 2) {//  只循环后面
                    mdu = (int) (stickerInfo.getEnd() - mTstart);
                    headArray = styleInfo.timeArrays.get(1);
                    tCount = headArray.getBegin() / mItemTime;
                    int mTimeDuration = headArray.getDuration();
                    int tLineCount = (int) Math.ceil((mdu + 0.0)
                            / mTimeDuration);
                    int len = mTimeDuration / mItemTime;
                    int max = len + tCount;
                    for (int j = tCount; j < max && j < styleInfo.frameArray.size(); j++) {
                        st = styleInfo.frameArray.valueAt(j);
                        if (null == st) {
                            continue;
                        }
                        CaptionLiteObject tmp = initFrameData(stickerInfo, st, showRectF);
                        for (int m = 0; m < tLineCount; m++) {
                            liteObject = tmp.clone();
                            int nstart = (int) mTstart + (j - tCount) * mItemTime + mTimeDuration * m;
                            if (nstart < stickerInfo.getEnd()) { // 子动画在该特效时间区域内
                                int nend = nstart + mItemTime;
                                liteObject.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(Math.min(stickerInfo.getEnd(), nend)));
                                stickerInfo.addSubObject(liteObject);
                            }
                        }
                    }
                } else if (timeArraySize == 3) {
                    // 中间部分循环，末尾不循环
                    TimeArray lastArray = styleInfo.timeArrays.get(2); // 优先处理不循环的片段
                    //特效时间段很长，允许中间段特效循环
                    if (lastArray.getDuration() < (stickerInfo.getEnd() - stickerInfo.getStart() - styleInfo.timeArrays.get(0).getDuration())) {
                        // 中间部分特效可以循环
                        int startIndex = lastArray.getBegin() / mItemTime, endIndex = lastArray
                                .getEnd() / mItemTime;

                        int mstart = (int) (stickerInfo.getEnd() - lastArray.getDuration());

                        for (int j = startIndex; j < endIndex; j++) {
                            st = styleInfo.frameArray.valueAt(j);
                            if (null == st) {
                                continue;
                            }
                            CaptionLiteObject tmp = initFrameData(stickerInfo, st, showRectF);
                            liteObject = tmp.clone();
                            int nstart = (int) mstart + mItemTime * (j - startIndex);
                            if (nstart < stickerInfo.getEnd()) { // 子动画在该特效时间区域内
                                int nend = nstart + mItemTime;
                                liteObject.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(Math.min(stickerInfo.getEnd(), nend)));
                                stickerInfo.addSubObject(liteObject);
                            }
                        }
                        // 处理中间部分的循环逻辑
                        int nEnd = (int) (stickerInfo.getEnd() - lastArray
                                .getDuration());

                        mdu = (nEnd - mTstart); // 循环部分的持续循环的时间
                        headArray = styleInfo.timeArrays.get(1);

                        int mTimeDuration = headArray.getDuration();

                        tCount = headArray.getBegin() / mItemTime;
                        int tLineCount = (int) Math.ceil((mdu + 0.0) / mTimeDuration);
                        int len = mTimeDuration / mItemTime;
                        int max = len + tCount;

                        for (int j = tCount; j < max; j++) {
                            st = styleInfo.frameArray.valueAt(j);
                            if (null == st) {
                                continue;
                            }
                            CaptionLiteObject tmp = initFrameData(stickerInfo, st, showRectF);
                            for (int m = 0; m < tLineCount; m++) {
                                liteObject = tmp.clone();
                                int nstart = (int) mTstart + (j - tCount) * mItemTime + mTimeDuration * m;
                                if (nstart < nEnd) { // 子动画在该特效时间区域内
                                    int nend = nstart + mItemTime;
                                    liteObject.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(Math.min(stickerInfo.getEnd(), nend)));
                                    stickerInfo.addSubObject(liteObject);
                                }
                            }
                        }
                    } else {
                        // 时间区域太短，直接跳过中间循环部分，显示末尾
                        int mstart = (int) (stickerInfo.getStart() + headArray.getDuration());
                        int startIndex = lastArray.getBegin() / mItemTime, endIndex = (int) ((stickerInfo.getEnd() - mstart) / mItemTime);
                        int len = styleInfo.frameArray.size();
                        for (int j = startIndex; j < endIndex; j++) {
                            if (j < len) {
                                st = styleInfo.frameArray.valueAt(j);
                                if (null == st) {
                                    continue;
                                }
                                CaptionLiteObject tmp = initFrameData(stickerInfo, st, showRectF);
                                liteObject = tmp.clone();
                                int nstart = mstart + mItemTime * (j - startIndex);
                                if (nstart < stickerInfo.getEnd()) { // 子动画在该特效时间区域内
                                    liteObject.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(Math.min(stickerInfo.getEnd(), nstart + mItemTime)));
                                    stickerInfo.addSubObject(liteObject);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (styleInfo.frameArray.size() == 1) {
                //只有一张图
                VisualFilterConfig visualFilterConfig = null;
                if (!TextUtils.isEmpty(styleInfo.filter) && (styleInfo.filter.equals(StyleInfo.FILTER_PIX) || styleInfo.filter.equals(StyleInfo.FILTER_BLUR))) {
                    if (styleInfo.filter.equals(StyleInfo.FILTER_PIX)) {
                        //马赛克
                        visualFilterConfig = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_PIXELATE);
                    } else if (styleInfo.filter.equals(StyleInfo.FILTER_BLUR)) {
                        //高斯
                        visualFilterConfig = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR);
                    }
                    liteObject = initFrameData(stickerInfo, styleInfo.filterPng, showRectF);
                } else {
                    st = styleInfo.frameArray.valueAt(0);
                    liteObject = initFrameData(stickerInfo, st, showRectF);
                }
                if (null != liteObject) {
                    liteObject.setTimelineRange(Utils.ms2s(stickerInfo.getStart()), Utils.ms2s(stickerInfo.getEnd()));
                    if (null != visualFilterConfig) {
                        try {
                            liteObject.changeFilter(visualFilterConfig);
                            liteObject.setAppliedByMask(true);
                        } catch (InvalidArgumentException ignored) {
                        }
                    }
                    stickerInfo.addSubObject(liteObject);
                }
            } else {
                //多张图
                int tLineCount = (int) Math.ceil((mdu + 0.0) / styleInfo.du);
                //需要导出的图片张数
                int len = styleInfo.frameArray.size();
                for (int j = 0; j < len; j++) {
                    st = styleInfo.frameArray.valueAt(j);
                    if (null == st) {
                        continue;
                    }
                    CaptionLiteObject tmp = initFrameData(stickerInfo, st, showRectF);
                    for (int m = 0; m < tLineCount; m++) {
                        int nstart = (int) stickerInfo.getStart() + j * mItemTime + m * styleInfo.du;
                        if (nstart < stickerInfo.getEnd()) { // 子动画在该特效时间区域内
                            liteObject = tmp.clone();
                            int nend = nstart + mItemTime;
                            liteObject.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(Math.min(nend, stickerInfo.getEnd())));
                            stickerInfo.addSubObject(liteObject);
                        }
                    }
                }
            }
        }

        stickerInfo.setParent(mOutVideoWidth, mOutVideoHeight);
    }

    /**
     * 普通贴纸(单帧画面)
     *
     * @param frameInfo 单帧画面
     */
    private CaptionLiteObject initFrameData(StickerInfo stickerInfo, FrameInfo frameInfo, RectF showRectF) {
        return initFrameData(stickerInfo, frameInfo.pic, showRectF);
    }

    /**
     * 普通贴纸(单帧画面)
     *
     * @param path  单帧画面
     * @param rectF 显示位置0~1.0f
     */
    private CaptionLiteObject initFrameData(StickerInfo stickerInfo, String path, RectF rectF) {
        CaptionLiteObject captionLiteObject = new CaptionLiteObject(mContext, path);
        captionLiteObject.setShowRectF(new RectF(rectF));
        captionLiteObject.setAngle(-(int) (stickerInfo.getRotateAngle()));
        return captionLiteObject;
    }

}
