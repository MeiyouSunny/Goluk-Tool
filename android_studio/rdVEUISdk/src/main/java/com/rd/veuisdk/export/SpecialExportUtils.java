package com.rd.veuisdk.export;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextUtils;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.models.SubtitleObject;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.model.FilterInfo2;
import com.rd.veuisdk.model.SpecialInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.StyleT;
import com.rd.veuisdk.model.TimeArray;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SpecialUtils;
import com.rd.veuisdk.ui.SinglePointRotate;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class SpecialExportUtils {
    private Context mContext;
    private int mLayoutWidth = 1024;
    private int mLayoutHeight = 1024;
    private HashMap<Integer, Boolean> mMaps = new HashMap<Integer, Boolean>(); // 记录线程是否工作完毕
    private static int mLastOutWidth = 0, mLastOutHeight = 0;
    private ArrayList<WordInfo> mWordList = new ArrayList<WordInfo>();
    private ArrayList<SpecialInfo> mSubtitleList = new ArrayList<SpecialInfo>();

    public SpecialExportUtils(Context _context, ArrayList<WordInfo> _list,
                              int lwidth, int lheight) {
        mContext = _context;
        mWordList = _list;
        mLayoutWidth = lwidth;
        mLayoutHeight = lheight;
    }

    private WordInfo getInfo(int id) {

        WordInfo temp, target = null;
        int len = mWordList.size();
        for (int mPosition = 0; mPosition < len; mPosition++) {
            temp = mWordList.get(mPosition);
            if (temp.getId() == id) {
                target = temp;
                break;
            }
        }
        return target;

    }

    /**
     * 导出字幕
     *
     * @param nOutVideoWidth
     *            保存视频高度
     * @param nOutVideoHeight
     *            保存视频宽度
     *
     * @return
     */
    /**
     * 导出字幕
     *
     * @param nOutVideoWidth  保存视频高度
     * @param nOutVideoHeight 保存视频宽度
     * @return
     */
    public ArrayList<SpecialInfo> onExport(int nOutVideoWidth,
                                           int nOutVideoHeight) {
        mSubtitleList.clear();
        ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                .getSpecailsDurationChecked();
        int len = tempList.size();
        mMaps.clear();
        if (len == 0) {
            return mSubtitleList;
        } else {
            WordInfo mWordInfo, tempItem;
            for (int mPosition = 0; mPosition < len; mPosition++) {
                mWordInfo = tempList.get(mPosition);
                mWordInfo.recycle();
                //
                int mcenterx = (int) (mWordInfo.getCenterxy()[0] * mLayoutWidth);
                int mcentery = (int) (mWordInfo.getCenterxy()[1] * mLayoutHeight);

                StyleInfo fi = SpecialUtils.getInstance().getStyleInfo(
                        mWordInfo.getStyleId());

                FilterInfo2 mfi = fi.getFilterInfo2();

                String bgpath = null;

                bgpath = mWordInfo.getBgPicpath();
                mWordInfo.setRealx(mWordInfo.getLeft() * nOutVideoWidth);
                mWordInfo.setRealy(mWordInfo.getTop() * nOutVideoHeight);
                SpecialInfo mSpecialInfo;

                SubtitleObject mSubtitle = null;
                mSubtitle = new SubtitleObject(mWordInfo.getPicpath(),
                        mWordInfo.getWidth(), mWordInfo.getHeight(), nOutVideoWidth, nOutVideoHeight);
                mSubtitle.setTimelineRange(Utils.ms2s(mWordInfo.getStart()), Utils.ms2s(mWordInfo.getEnd()));

                mSubtitle.setFadeInOut(Utils.ms2s(100), Utils.ms2s(100));

                RectF rect = new RectF((float) mWordInfo.getLeft(), (float) mWordInfo.getTop(), (float) (mWordInfo.getLeft() + mWordInfo.getWidthx()), (float) (mWordInfo.getTop() + mWordInfo.getHeighty()));
                // Log.e("2rect...", rect.toShortString() + "......" +
                // nOutVideoWidth+ "*" + nOutVideoHeight);

                mSubtitle.setShowRectangle(rect, nOutVideoWidth,
                        nOutVideoHeight);
                mWordInfo.addSubObject(mSubtitle);
                if (mPosition < mWordList.size()) {
                    mWordList.set(mPosition, mWordInfo);
                }

            }
            return mSubtitleList;
        }
    }

    public void onExport(int nOutVideoWidth, int nOutVideoHeight,
                         IExportSpecial back) {
        mSubtitleList.clear();
        ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                .getSpecailsDurationChecked();
        int lenList = tempList.size();

        if (lenList == 0) {
            back.onSpecial(mSubtitleList);
        } else {
            WordInfo mWordInfo, tempItem;
            // Log.e("lensze..." + lenList + "个特效", "texiao");
            SpecialInfo mSpecialInfo = null;
            boolean canreset = false;
            // 前次导出的视频宽高与当前导出时候的目标视频的宽高是否相等
            boolean isEquals = (mLastOutWidth == nOutVideoWidth && mLastOutHeight == nOutVideoHeight);
            mLastOutWidth = nOutVideoWidth;
            mLastOutHeight = nOutVideoHeight;
            // boolean asp = TempVideoParams.getInstance()
            // .checkAspectRatioChanged();
            for (int mPosition = 0; mPosition < lenList; mPosition++) {
                mMaps.put(mPosition, false);
                mWordInfo = tempList.get(mPosition);
                tempItem = getInfo(mWordInfo.getId());
                // if (asp) {
                if (isEquals && mWordInfo.equals(tempItem) && !tempItem.IsChanged()) {
                    // Log.e("之前已经保存过" + mPosition, mWordInfo.getId() + "..保存过.." +
                    // mWordInfo.getText()
                    // + "...." + mWordInfo.getList().size());
                    mSpecialInfo = new SpecialInfo((int) mWordInfo.getStart(),
                            (int) mWordInfo.getEnd());
                    mSpecialInfo.setList(mWordInfo.getList());
                    mSubtitleList.add(mSpecialInfo);
                    mMaps.put(mPosition, true);
                    canreset = false;
                    continue;
                }
                // }
                canreset = true;
                mWordInfo.recycle();
                ThreadPoolUtils.executeEx(new ExtRunnable(mPosition, mWordInfo, nOutVideoWidth,
                        nOutVideoHeight, back));

            }
            checkCreatePhotoFinished(back, canreset);
        }

    }

    private String TAG = SpecialExportUtils.class.getName();

    private class ExtRunnable implements Runnable {

        private WordInfo mWordInfo;
        private int nOutVideoWidth, nOutVideoHeight;
        private SinglePointRotate mSprMain;
        private int mItemTime = 100;
        private SubtitleObject mSubtitle;
        private int mPosition;
        private IExportSpecial mSpecialCallback;
        private SpecialInfo mSpecialInfo;

        public ExtRunnable(int mi, WordInfo minfo, int mnOutVideoWidth,
                           int mnOutVideoHeight, IExportSpecial _mback) {
            mPosition = mi;
            mWordInfo = minfo;
            nOutVideoWidth = mnOutVideoWidth;
            nOutVideoHeight = mnOutVideoHeight;
            mSpecialCallback = _mback;
            mSpecialInfo = new SpecialInfo((int) mWordInfo.getStart(), (int) mWordInfo.getEnd());
        }

        @Override
        public void run() {

            // Log.e("ExtRunnablebegin", mWordInfo.getPicpath() + "..." + mWordInfo.getStyleId()
            // + "...");

            // long told = System.currentTimeMillis();
            int mcenterx = (int) (mWordInfo.getCenterxy()[0] * mLayoutWidth);
            int mcentery = (int) (mWordInfo.getCenterxy()[1] * mLayoutHeight);
            // Log.e("mWordInfo.info. sp...", mWordInfo.getDisf()+".........."+mWordInfo.IsChanged());
            if (mWordInfo.IsChanged()) {
                mWordInfo.setWidth((int) (mWordInfo.getWidthx() * nOutVideoWidth));
                mWordInfo.setHeight((int) (mWordInfo.getHeighty() * nOutVideoHeight));
            } else {
                if (mWordInfo.getWidth() == AppConfiguration.DEFAULT_WIDTH) {
                    mWordInfo.setWidth((int) (mWordInfo.getWidthx() * nOutVideoWidth));
                }
                if (mWordInfo.getHeight() == AppConfiguration.DEFAULT_WIDTH)
                    mWordInfo.setHeight((int) (mWordInfo.getHeighty() * nOutVideoHeight));
            }


            mWordInfo.setRealx(mWordInfo.getLeft() * nOutVideoWidth);
            mWordInfo.setRealy(mWordInfo.getTop() * nOutVideoHeight);

            StyleInfo fi = SpecialUtils.getInstance().getStyleInfo(
                    mWordInfo.getStyleId());
            // Log.e("SpecialUtils.getInstance().getStyleInfo", fi.pid + "..."
            // + fi.caption + "...." + fi.code + "...." + fi.mlocalpath);
            RectF rect = new RectF((float) mWordInfo.getLeft(), (float) mWordInfo.getTop(), (float) (mWordInfo.getLeft() + mWordInfo.getWidthx()), (float) (mWordInfo.getTop() + mWordInfo.getHeighty()));
//            Log.e("sputils22222", mLayoutWidth + "*" + mLayoutHeight + Arrays.toString(mWordInfo.getCenterxy()) + ".." + mWordInfo.getWidth() + "*" + mWordInfo.getHeight() + ",,,," + mWordInfo.getLeft() + "..." + mWordInfo.getTop() + "...." + mWordInfo.getWidthx() + "....." + mWordInfo.getHeighty() + "...." + rect.toShortString() + ",,,,," + nOutVideoWidth + "*" + nOutVideoHeight);

            FilterInfo2 mfi = fi.getFilterInfo2();

            StyleT st = null;

            mItemTime = fi.frameArry.valueAt(1).time // 每帧持续时间
                    - fi.frameArry.valueAt(0).time;
            int mdu = (int) (mWordInfo.getEnd() - mWordInfo.getStart());
            int timeArraySize = fi.timeArrays.size();

//            String msg = mPosition + "....." + mWordInfo.getStart() + "<>" + mWordInfo.getEnd() + "--->";
            if (timeArraySize == 2 || timeArraySize == 3) { // 前面不循环

                // 处理不循环的部分

                TimeArray headArray = fi.timeArrays.get(0);

                int tCount = headArray.getDuration() / mItemTime;
                for (int m = 0; m < tCount; m++) {
                    st = fi.frameArry.valueAt(m);
                    if (null == st) {
                        continue;
                    }
                    mSprMain = getSingle(mWordInfo, mfi, fi, st, mcenterx, mcentery);

                    String path = PathUtils.getTempFileNameForSdcard(
                            PathUtils.TEMP_WORD + mPosition + "j_" + m + "_"
                                    + headArray.getBegin(), "png");
//                    android.util.Log.e(TAG, msg + "header 部分" + path);
                    mSprMain.save(path);
                    mSprMain.recycle();
                    if (new File(path).exists()) {
                        mSubtitle = new SubtitleObject(path, mWordInfo.getWidth(),
                                mWordInfo.getHeight(), nOutVideoWidth, nOutVideoHeight);
                        int nstart = (int) mWordInfo.getStart() + m * mItemTime;
                        if (nstart < mWordInfo.getEnd()) { // 子动画在该特效时间区域内
                            int nend = nstart + mItemTime;
                            mSubtitle.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(nend));
                            mSubtitle.setShowRectangle(rect,
                                    nOutVideoWidth, nOutVideoHeight);
                            mWordInfo.addSubObject(mSubtitle);
                            mSpecialInfo.add(mSubtitle);

                        }

                    }
                }
                int mTstart = (int) (mWordInfo.getStart() + headArray.getDuration());

                if (mTstart < mWordInfo.getEnd()) { // 处理循环部分

                    if (timeArraySize == 2) {// ，只循环后面

                        mdu = (int) (mWordInfo.getEnd() - mTstart);
                        headArray = fi.timeArrays.get(1);
                        tCount = headArray.getBegin() / mItemTime;
                        int mTimeDuration = headArray.getDuration();
                        int tLineCount = (int) Math.ceil((mdu + 0.0)
                                / mTimeDuration);
                        int len = mTimeDuration / mItemTime;
                        int max = len + tCount;
                        for (int j = tCount; j < max && j < fi.frameArry.size(); j++) {
                            st = fi.frameArry.valueAt(j);
                            if (null == st) {
                                continue;
                            }
                            mSprMain = getSingle(mWordInfo, mfi, fi, st, mcenterx, mcentery);

                            String path = PathUtils.getTempFileNameForSdcard(
                                    PathUtils.TEMP_WORD + mPosition + "j_" + j
                                            + "begin_" + headArray.getBegin(),
                                    "png");
                            mSprMain.save(path);
                            mSprMain.recycle();

                            if (new File(path).exists()) {
                                for (int m = 0; m < tLineCount; m++) {
                                    mSubtitle = new SubtitleObject(path,
                                            mWordInfo.getWidth(), mWordInfo.getHeight(), nOutVideoWidth, nOutVideoHeight);
                                    int nstart = (int) mTstart + (j - tCount)
                                            * mItemTime + mTimeDuration * m;
                                    if (nstart < mWordInfo.getEnd()) { // 子动画在该特效时间区域内
                                        int nend = nstart + mItemTime;
                                        mSubtitle.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(nend));
                                        mSubtitle.setShowRectangle(rect,
                                                nOutVideoWidth, nOutVideoHeight);
                                        mWordInfo.addSubObject(mSubtitle);
                                        mSpecialInfo.add(mSubtitle);

                                    }

                                }

                            }
                        }
                    } else if (timeArraySize == 3) {// 中间部分循环，末尾不循环

                        TimeArray lastArray = fi.timeArrays.get(2); // 优先处理不循环的片段

                        //特效时间段很长，允许中间段特效循环
                        if (lastArray.getDuration() < (mWordInfo.getEnd() - mWordInfo.getStart() - fi.timeArrays.get(0).getDuration())) {
                            // 中间部分特效可以循环


                            int startIndex = lastArray.getBegin() / mItemTime, endIndex = lastArray
                                    .getEnd() / mItemTime;

                            int mstart = (int) (mWordInfo.getEnd() - lastArray
                                    .getDuration());

                            for (int j = startIndex; j < endIndex; j++) {
                                st = fi.frameArry.valueAt(j);
                                if (null == st) {
                                    continue;
                                }
                                mSprMain = getSingle(mWordInfo, mfi, fi, st, mcenterx,
                                        mcentery);

                                String path = PathUtils
                                        .getTempFileNameForSdcard(
                                                PathUtils.TEMP_WORD + mPosition + "j_"
                                                        + j + "begin_"
                                                        + lastArray.getBegin(),
                                                "png");
//                                android.util.Log.e(TAG, msg + "末尾部分不循环run: " + path);
                                mSprMain.save(path);
                                mSprMain.recycle();

                                if (new File(path).exists()) {
                                    mSubtitle = new SubtitleObject(path,
                                            mWordInfo.getWidth(), mWordInfo.getHeight(), nOutVideoWidth, nOutVideoHeight);
                                    int nstart = (int) mstart + mItemTime
                                            * (j - startIndex);
                                    if (nstart < mWordInfo.getEnd()) { // 子动画在该特效时间区域内
                                        int nend = nstart + mItemTime;
                                        mSubtitle.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(nend));
                                        mSubtitle.setShowRectangle(rect,
                                                nOutVideoWidth, nOutVideoHeight);
                                        mWordInfo.addSubObject(mSubtitle);
                                        mSpecialInfo.add(mSubtitle);

                                    }

                                }
                            }
                            // 处理中间部分的循环逻辑
                            int nEnd = (int) (mWordInfo.getEnd() - lastArray
                                    .getDuration());

                            mdu = (nEnd - mTstart); // 循环部分的持续循环的时间
                            headArray = fi.timeArrays.get(1);

                            int mTimeDuration = headArray.getDuration();

                            tCount = headArray.getBegin() / mItemTime;
                            int tLineCount = (int) Math.ceil((mdu + 0.0)
                                    / mTimeDuration);
                            int len = mTimeDuration / mItemTime;
                            int max = len + tCount;

                            for (int j = tCount; j < max; j++) {
                                st = fi.frameArry.valueAt(j);
                                if (null == st) {
                                    continue;
                                }
                                mSprMain = getSingle(mWordInfo, mfi, fi, st, mcenterx,
                                        mcentery);

                                String path = PathUtils
                                        .getTempFileNameForSdcard(
                                                PathUtils.TEMP_WORD + mPosition + "j_"
                                                        + j + "begin_"
                                                        + headArray.getBegin(),
                                                "png");

//                                android.util.Log.e(TAG, msg + "中间部分循环run: " + path);
                                mSprMain.save(path);
                                mSprMain.recycle();

                                if (new File(path).exists()) {

                                    for (int m = 0; m < tLineCount; m++) {
                                        mSubtitle = new SubtitleObject(
                                                path, mWordInfo.getWidth(),
                                                mWordInfo.getHeight(), nOutVideoWidth, nOutVideoHeight);
                                        int nstart = (int) mTstart
                                                + (j - tCount) * mItemTime
                                                + mTimeDuration * m;
                                        if (nstart < nEnd) { // 子动画在该特效时间区域内
                                            int nend = nstart + mItemTime;
                                            mSubtitle.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(nend));
                                            mSubtitle.setShowRectangle(rect,
                                                    nOutVideoWidth,
                                                    nOutVideoHeight);
                                            mWordInfo.addSubObject(mSubtitle);
                                            mSpecialInfo.add(mSubtitle);

                                        }

                                    }

                                }
                            }

                        } else { // 时间区域太短，直接跳过中间循环部分，显示末尾

                            int mstart = (int) (mWordInfo.getStart() + headArray
                                    .getDuration());
                            int startIndex = lastArray.getBegin() / mItemTime, endIndex = (int) ((mWordInfo
                                    .getEnd() - mstart) / mItemTime);

                            int len = fi.frameArry.size();
                            for (int j = startIndex; j < endIndex; j++) {
                                if (j < len) {
                                    st = fi.frameArry.valueAt(j);
                                    if (null == st) {
                                        continue;
                                    }

                                    mSprMain = getSingle(mWordInfo, mfi, fi, st, mcenterx,
                                            mcentery);

                                    String path = PathUtils
                                            .getTempFileNameForSdcard(
                                                    PathUtils.TEMP_WORD + mPosition
                                                            + "j_" + j
                                                            + "begin_"
                                                            + headArray.getBegin(),
                                                    "png");
                                    mSprMain.save(path);

//                                    android.util.Log.e(TAG, msg + "时间太短，跳过中间部分显示末尾: " + path);
                                    mSprMain.recycle();

                                    if (new File(path).exists()) {
                                        mSubtitle = new SubtitleObject(
                                                path, mWordInfo.getWidth(),
                                                mWordInfo.getHeight(), nOutVideoWidth, nOutVideoHeight);
                                        int nstart = (int) mstart + mItemTime
                                                * (j - startIndex);
                                        if (nstart < mWordInfo.getEnd()) { // 子动画在该特效时间区域内
                                            int nend = nstart + mItemTime;
                                            mSubtitle.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(nend));
                                            mSubtitle.setShowRectangle(rect,
                                                    nOutVideoWidth,
                                                    nOutVideoHeight);
                                            mWordInfo.addSubObject(mSubtitle);
                                            mSpecialInfo.add(mSubtitle);

                                        }

                                    }
                                }
                            }

                        }

                    }
                }

            } else {
                int tLineCount = (int) Math.ceil((mdu + 0.0) / fi.du);
                int len = fi.frameArry.size();
                for (int j = 0; j < len; j++) {
                    st = fi.frameArry.valueAt(j);
                    if (null == st) {
                        continue;
                    }
                    mSprMain = getSingle(mWordInfo, mfi, fi, st, mcenterx, mcentery);

                    String path = PathUtils.getTempFileNameForSdcard(
                            PathUtils.TEMP_WORD + mPosition + "j_" + j, "png");
                    mSprMain.save(path);
                    mSprMain.recycle();

                    if (new File(path).exists()) {
                        for (int m = 0; m < tLineCount; m++) {
                            mSubtitle = new SubtitleObject(path,
                                    mWordInfo.getWidth(), mWordInfo.getHeight(), nOutVideoWidth, nOutVideoHeight);
                            int nstart = (int) mWordInfo.getStart() + j * mItemTime + m
                                    * fi.du;
                            if (nstart < mWordInfo.getEnd()) { // 子动画在该特效时间区域内
                                int nend = nstart + mItemTime;
                                mSubtitle.setTimelineRange(Utils.ms2s(nstart), Utils.ms2s(nend));
                                mSubtitle.setShowRectangle(rect,
                                        nOutVideoWidth, nOutVideoHeight);
                                mWordInfo.addSubObject(mSubtitle);
                                mSpecialInfo.add(mSubtitle);

                            }

                        }

                    }
                }
            }
            mSubtitleList.add(mSpecialInfo);
            mWordList.set(mPosition, mWordInfo);
            mMaps.put(mPosition, true);
            checkCreatePhotoFinished(mSpecialCallback, true);
            // Log.e("load specialok 是否绘制图片完成" + over + "..." + mPosition,
            // mSubtitleList.size()
            // + "个对象,本次特效耗时:" + (System.currentTimeMillis() - told)
            // + " ms");

            // Log.e("ExtRunnable", mWordInfo.getPicpath() + "..." + mWordInfo.getStyleId()
            // + "...");

        }
    }

    private SinglePointRotate getSingle(WordInfo mWordInfo, FilterInfo2 mfi,
                                        StyleInfo fi, StyleT st, int mcenterx, int mcentery) {
        return new SinglePointRotate(mContext, mWordInfo.getRotateAngle(),
                TextUtils.isEmpty(mWordInfo.getText()) ? mfi.getHint() : mWordInfo.getText(),
                mWordInfo.getTextColor() == Color.WHITE ? mfi.getTextDefaultColor() : mWordInfo
                        .getTextColor(), mWordInfo.getTtfLocalPath(), mWordInfo.getDisf(),
                new Point(mLayoutWidth, mLayoutHeight), new Point(mcenterx,
                mcentery), mWordInfo.getTextSize(), mWordInfo.getShadowColor(), fi,
                st.pic);
    }

    private void checkCreatePhotoFinished(IExportSpecial mSpecialCallback,
                                          boolean resetData) {
        boolean over = true;
        for (Entry<Integer, Boolean> item : mMaps.entrySet()) {

            if (!item.getValue()) {
                over = false;
                break;
            }
        }
        if (over) {

            // 绘制完毕
            if (resetData)
                TempVideoParams.getInstance().setSpecial(mWordList);
            mSpecialCallback.onSpecial(mSubtitleList);

        }

    }
}
