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
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.ui.SinglePointRotate;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class SubExportUtils {

    private ArrayList<SubtitleObject> mSubtitleList = new ArrayList<SubtitleObject>();
    private HashMap<Integer, Boolean> mMaps = new HashMap<Integer, Boolean>(); // 记录线程是否工作完毕
    private int mLayoutWidth, mLayoutHeight;
    private Context mContext;
    private static int mLastOutWidth = 0, mLastOutHeight = 0;
    private ArrayList<WordInfo> mWordList = new ArrayList<WordInfo>();

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

    public SubExportUtils(Context _context, ArrayList<WordInfo> _list,
                          int lwidth, int lheight) {
        mContext = _context;
        mWordList = _list;

//		for (int mPosition = 0; mPosition < mWordList.size(); mPosition++) {
//			WordInfo mWordInfo = _list.get(mPosition);
//			Log.e("SubExportUtils....." + mPosition, mWordInfo.getId() + ".....");
//		}

        mLayoutWidth = lwidth;
        mLayoutHeight = lheight;
    }

    /**
     * 导出字幕
     *
     * @param mOutVideoWidth  保存视频高度
     * @param mOutVideoHeight 保存视频宽度
     * @return
     */
    public ArrayList<SubtitleObject> onExport(int mOutVideoWidth, int mOutVideoHeight) {
        mSubtitleList.clear();
        ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                .getSubsDuraionChecked();
        int len = tempList.size();
        mMaps.clear();
        if (len == 0) {
            return mSubtitleList;
        } else {
            WordInfo mWordInfo, tempItem;
            // 前次导出的视频宽高与当前导出时候的目标视频的宽高是否相等
//		boolean isEquals = (mLastOutWidth == mOutVideoWidth && mLastOutHeight == mOutVideoHeight);
//		mLastOutWidth = mOutVideoWidth;
//		mLastOutHeight = mOutVideoHeight;
            for (int mPosition = 0; mPosition < len; mPosition++) {
                mWordInfo = tempList.get(mPosition);

                //tempItem = getInfo(mWordInfo.getId());
                //if (isEquals && mWordInfo.equals(tempItem) && !tempItem.IsChanged()) {

                //Log.e("export....", "!changed." + mWordInfo.getList().size());

                //mSubtitleList.addAll(mWordInfo.getList());
                //continue;
                //}

                mWordInfo.recycle();
                //
                int mcenterx = (int) (mWordInfo.getCenterxy()[0] * mLayoutWidth);
                int mcentery = (int) (mWordInfo.getCenterxy()[1] * mLayoutHeight);

                StyleInfo fi = SubUtils.getInstance().getStyleInfo(mWordInfo.getStyleId());
//			fi.zoomFactor = mWordInfo.getZoomFactor();
                FilterInfo2 mfi = fi.getFilterInfo2();

                String bgpath = null;
//			if (fi.frameArry.size() > 0) {
//				bgpath = fi.frameArry.valueAt(0).pic;
//			}
                bgpath = mWordInfo.getBgPicpath();
//			SinglePointRotate mSprMain  =null;
//			mSprMain = new SinglePointRotate(mContext, mWordInfo.getRotateAngle(),
//					TextUtils.isEmpty(mWordInfo.getText()) ? mfi.getHint() : mWordInfo
//							.getText(),
//					mWordInfo.getTextColor() == Color.WHITE ? mfi.getTextDefaultColor()
//							: mWordInfo.getTextColor(), mWordInfo.getTtfLocalPath(),
//					mWordInfo.getDisf(), new Point(mLayoutWidth, mLayoutHeight),
//					new Point(mcenterx, mcentery), mWordInfo.getTextSize(),
//					mWordInfo.getShadowColor(), fi, bgpath);
//			String path = PathUtils.getTempFileNameForSdcard(
//					PathUtils.TEMP_WORD + mPosition, "png");
//			mSprMain.save(path);
//			mWordInfo.setPicpath(path);
//			mWordInfo.setBgPicpath(bgpath);
//			mSprMain.recycle();
//			mSprMain = null;
//			Log.e("mWordInfo.info. sub...", mWordInfo.getDisf() + ".........." + mWordInfo.IsChanged());
                if (mWordInfo.IsChanged()) {
                    //mWordInfo.setWidth((int) (mWordInfo.getWidthx() * mOutVideoWidth));
                    //mWordInfo.setHeight((int) (mWordInfo.getHeighty() * mOutVideoHeight));
                } else {
//				if (mWordInfo.getWidth() == AppConfiguration.DEFAULT_WIDTH) {
//					mWordInfo.setWidth((int) (mWordInfo.getWidthx() * mOutVideoWidth));
//				}
//				if (mWordInfo.getHeight() == AppConfiguration.DEFAULT_WIDTH)
//					mWordInfo.setHeight((int) (mWordInfo.getHeighty() * mOutVideoHeight));
                }
                mWordInfo.setRealx(mWordInfo.getLeft() * mOutVideoWidth);
                mWordInfo.setRealy(mWordInfo.getTop() * mOutVideoHeight);
                SubtitleObject mSubtitle = null;
                mSubtitle = new SubtitleObject(mWordInfo.getPicpath(), mWordInfo.getWidth(),
                        mWordInfo.getHeight(), mOutVideoWidth, mOutVideoHeight);
                mSubtitle.setTimelineRange(Utils.ms2s(mWordInfo.getStart()), Utils.ms2s(mWordInfo.getEnd()));

                mSubtitle.setFadeInOut(Utils.ms2s(100), Utils.ms2s(100));

                RectF rect = new RectF((float) mWordInfo.getLeft(), (float) mWordInfo.getTop(), (float) (mWordInfo.getLeft() + mWordInfo.getWidthx()), (float) (mWordInfo.getTop() + mWordInfo.getHeighty()));

                //Log.e("2rect...", rect.toShortString() + "......" + mOutVideoWidth+ "*" + mOutVideoHeight);

                mSubtitle.setShowRectangle(rect, mOutVideoWidth,
                        mOutVideoHeight);
                mSubtitleList.add(mSubtitle);
                mWordInfo.addSubObject(mSubtitle);
                if (mPosition < mWordList.size()) {
                    mWordList.set(mPosition, mWordInfo);
                }

            }
            return mSubtitleList;
        }
    }

    public void onExport(int mOutVideoWidth, int mOutVideoHeight,
                         IExportSub back) {
        mSubtitleList.clear();
        ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                .getSubsDuraionChecked();
        int len = tempList.size();
        mMaps.clear();
        if (len == 0) {
            back.onSub(mSubtitleList);
        } else {
            boolean resetdata = false;
            WordInfo mWordInfo, tempItem;
            // 前次导出的视频宽高与当前导出时候的目标视频的宽高是否相等
            boolean isEquals = (mLastOutWidth == mOutVideoWidth && mLastOutHeight == mOutVideoHeight);
//			Log.e("onExport....", "isEquals." + isEquals);
            mLastOutWidth = mOutVideoWidth;
            mLastOutHeight = mOutVideoHeight;
            // boolean asp = TempVideoParams.getInstance()
            // .checkAspectRatioChanged();
            for (int mPosition = 0; mPosition < len; mPosition++) {
                mMaps.put(mPosition, false);
                mWordInfo = tempList.get(mPosition);
                // if (asp) {
                tempItem = getInfo(mWordInfo.getId());
                if (isEquals && mWordInfo.equals(tempItem) && !tempItem.IsChanged()) {

//					Log.e("1export....", "!changed." + mWordInfo.getList().size());

                    mSubtitleList.addAll(mWordInfo.getList());
                    resetdata = false;
                    mMaps.put(mPosition, true);
                    continue;
                }
                // }
                mWordInfo.recycle();
                resetdata = true;
                ThreadPoolUtils.executeEx(new ExtRunnable(mPosition, mWordInfo, mOutVideoWidth,
                        mOutVideoHeight, back));

            }

            checkIsCreatePhotoFinish(back, resetdata);

        }

    }

    private class ExtRunnable implements Runnable {

        private WordInfo mWordInfo;
        private int mOutVideoWidth, mOutVideoHeight;
        private SinglePointRotate mSprMain;
        private SubtitleObject mSubtitle;
        private int mPosition;
        private IExportSub mSubCallback;

        public ExtRunnable(int mi, WordInfo minfo, int mnOutVideoWidth,
                           int mnOutVideoHeight, IExportSub _mback) {
            mPosition = mi;
            mWordInfo = minfo;
            mOutVideoWidth = mnOutVideoWidth;
            mOutVideoHeight = mnOutVideoHeight;
            mSubCallback = _mback;
        }

        @Override
        public void run() {
            int mcenterx = (int) (mWordInfo.getCenterxy()[0] * mLayoutWidth);
            int mcentery = (int) (mWordInfo.getCenterxy()[1] * mLayoutHeight);

            StyleInfo fi = SubUtils.getInstance().getStyleInfo(mWordInfo.getStyleId());
//			fi.zoomFactor = mWordInfo.getZoomFactor();
            FilterInfo2 mfi = fi.getFilterInfo2();

            String bgpath = null;
            if (fi.frameArry.size() > 0) {
                bgpath = fi.frameArry.valueAt(0).pic;
            }

            mSprMain = new SinglePointRotate(mContext, mWordInfo.getRotateAngle(),
                    TextUtils.isEmpty(mWordInfo.getText()) ? mfi.getHint() : mWordInfo
                            .getText(),
                    mWordInfo.getTextColor() == Color.WHITE ? mfi.getTextDefaultColor()
                            : mWordInfo.getTextColor(), mWordInfo.getTtfLocalPath(),
                    mWordInfo.getDisf(), new Point(mLayoutWidth, mLayoutHeight),
                    new Point(mcenterx, mcentery), mWordInfo.getTextSize(),
                    mWordInfo.getShadowColor(), fi, bgpath);
            String path = PathUtils.getTempFileNameForSdcard(
                    PathUtils.TEMP_WORD + mPosition, "png");
            mSprMain.save(path);
            mWordInfo.setPicpath(path);
            mWordInfo.setBgPicpath(bgpath);
            mSprMain.recycle();
//			Log.e("mWordInfo.info. sub...", mWordInfo.getDisf() + ".........." + mWordInfo.IsChanged());
            if (mWordInfo.IsChanged()) {
                mWordInfo.setWidth((int) (mWordInfo.getWidthx() * mOutVideoWidth));
                mWordInfo.setHeight((int) (mWordInfo.getHeighty() * mOutVideoHeight));
            } else {
                if (mWordInfo.getWidth() == AppConfiguration.DEFAULT_WIDTH) {
                    mWordInfo.setWidth((int) (mWordInfo.getWidthx() * mOutVideoWidth));
                }
                if (mWordInfo.getHeight() == AppConfiguration.DEFAULT_WIDTH)
                    mWordInfo.setHeight((int) (mWordInfo.getHeighty() * mOutVideoHeight));
            }
            mWordInfo.setRealx(mWordInfo.getLeft() * mOutVideoWidth);
            mWordInfo.setRealy(mWordInfo.getTop() * mOutVideoHeight);

            mSubtitle = new SubtitleObject(mWordInfo.getPicpath(), mWordInfo.getWidth(),
                    mWordInfo.getHeight(), mOutVideoWidth, mOutVideoHeight);
            mSubtitle.setTimelineRange(Utils.ms2s(mWordInfo.getStart()), Utils.ms2s(mWordInfo.getEnd()));

            mSubtitle.setFadeInOut(Utils.ms2s(100), Utils.ms2s(100));
            RectF rect = new RectF((float) mWordInfo.getLeft(), (float) mWordInfo.getTop(), (float) (mWordInfo.getLeft() + mWordInfo.getWidthx()), (float) (mWordInfo.getTop() + mWordInfo.getHeighty()));


//			Log.e("1rect...", rect.toShortString() + "......" + mOutVideoWidth+ "*" + mOutVideoHeight);
            // [-15,450][443,586]......640*640
            mSubtitle.setShowRectangle(rect, mOutVideoWidth,
                    mOutVideoHeight);
            mSubtitleList.add(mSubtitle);
            mWordInfo.addSubObject(mSubtitle);
            if (mPosition < mWordList.size()) {
                mWordList.set(mPosition, mWordInfo);
            }
            mMaps.put(mPosition, true);
            checkIsCreatePhotoFinish(mSubCallback, true);
        }

    }

    private void checkIsCreatePhotoFinish(IExportSub mSubCallback, boolean resetData) {
        boolean over = true;
        for (Entry<Integer, Boolean> item : mMaps.entrySet()) {

            if (!item.getValue()) {
                over = false;
                break;
            }
        }
        if (over) {
            if (resetData)
                TempVideoParams.getInstance().setSubs(mWordList);
            // 绘制完毕
            mSubCallback.onSub(mSubtitleList);

        }
    }

}
