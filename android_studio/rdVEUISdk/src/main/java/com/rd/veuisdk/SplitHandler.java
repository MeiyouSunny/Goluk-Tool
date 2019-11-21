package com.rd.veuisdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.model.SplitItem;
import com.rd.veuisdk.model.SplitThumbItemInfo;
import com.rd.veuisdk.ui.AutoView;
import com.rd.veuisdk.ui.DraggedView;
import com.rd.veuisdk.ui.PriviewLayout;
import com.rd.veuisdk.ui.PriviewLinearLayout;
import com.rd.veuisdk.ui.VideoThumbNailView;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.ThumbNailUtils;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView.onLongListener;

import java.util.ArrayList;

class SplitHandler {
    private String TAG = "SplitHandler";

    private View mRoot;
    private LinearLayout mMediaLinearLayout;
    private Context mContext;
    private TimelineHorizontalScrollView mScrollView;
    private ISplitHandler iSplitHandler;
    private VideoThumbNailView mSplitView;
    private FrameLayout mDraggedLayout;
    private DraggedView mDraggedView;
    private PriviewLinearLayout mPriviewLinearLayout;
    private AutoView mFirstDialog;

    private View mSplitLayout;
    private TextView mTvSplitProgress;
    private TextView mTvEndTime;
    private PriviewLayout mParentFrame;
    private RelativeLayout mScreen;
    private int mThumbMargin = 0;


    public SplitHandler(Context context, View root, final ISplitHandler iHandler) {
        mIsSpliting = false;
        mRoot = root;
        mContext = context;
        iSplitHandler = iHandler;
        mMediaLinearLayout = Utils.$(mRoot, R.id.timeline_media);
        mScrollView = Utils.$(mRoot, R.id.priview_edit_split);
        mScrollView.enableUserScrolling(true);
        mScrollView.setFadingEdgeLength(0);
        mThumbMargin = mContext.getResources().getDimensionPixelSize(R.dimen.split_thumb_margin);
        mScrollView.setHorizontalFadingEdgeEnabled(false);
        mScrollView.setHorizontalScrollBarEnabled(false);
        mSplitView = Utils.$(mRoot, R.id.split_videoview);
        mScreen = Utils.$(mRoot, R.id.rlSplitScreen);

        mDraggedLayout = Utils.$(mRoot, R.id.thelinearDraggedLayout);
        mDraggedView = Utils.$(mRoot, R.id.dragged_info_trash_View);

        mPriviewLinearLayout = Utils.$(mRoot, R.id.the_priview_layout_content);
        mParentFrame = Utils.$(mRoot, R.id.mroot_priview_layout);
        mSplitLayout = Utils.$(mRoot, R.id.split_layout);
        mTvEndTime = Utils.$(mRoot, R.id.tvEnd);
        mTvSplitProgress = Utils.$(mRoot, R.id.split_item_progress);
        mFirstDialog = Utils.$(mRoot, R.id.split_first_dialog);

        Utils.$(mRoot, R.id.prepare_split).setOnClickListener(mSplitLisenter);
        ((TextView) Utils.$(mRoot, R.id.tvBottomTitle)).setText(R.string.preview_spilt);
        Utils.$(mRoot, R.id.ivSure).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ArrayList<MediaObject> mlistvideo = getMediaObject();
                        if (null == mlistvideo || mlistvideo.size() < 1) {
                            iHandler.onCancel();
                        } else {
                            iHandler.onSure(mlistvideo);
                        }
                        recycle();
                    }
                });
        Utils.$(mRoot, R.id.ivCancel).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        iHandler.onCancel();
                    }
                });
        mScrollView.addScrollListener(mScrollListener);
        mScrollView.setViewTouchListener(new IViewTouchListener() {

            @Override
            public void onActionDown() {
                if (mMediaPrepared) {
                    iHandler.onTouchPause();
                    setProgressText(mScrollView.getScrollX());
                }
            }

            @Override
            public void onActionMove() {
                int srollX = mScrollView.getScrollX();
                setProgressText(srollX);
                int porgress = getProgressByPx(srollX);
                iHandler.onSeekTo(porgress);
            }

            @Override
            public void onActionUp() {
                setProgressText(mScrollView.getScrollX());
                mScrollView.resetForce();
            }

        });
        mScrollView.setLongListener(new onLongListener() {

            @Override
            public void onLong(int x, int y) {
                if (!hasSplit()) {
                    return;
                }
                mDraggedView.setTrashListener(mOnTrashlistener);
                int len = mSplitView.getSplits().size();
                if (len <= 1) {
                    Utils.autoToastNomal(mScrollView.getContext(),
                            R.string.last_split_cannt_del);
                    return;
                }
                iSplitHandler.onScrollBegin(mScrollView.getProgress());
                mPriviewLinearLayout.setEnableTouch(false);
                mParentFrame.setForceToTarget(true);
                // 找到对应的videoTimeLine
                SplitItem mLine = null;

                int mTleft = x - mScrollView.getHalfParentWidth();
                int mTright = x - mScrollView.getHalfParentWidth();

                for (int i = 0; i < len; i++) {
                    SplitItem tempLine = mSplitView.getSplits().get(i);

                    Rect mtempRect = tempLine.getRect();
                    int theleft = (int) (mtempRect.left - mScrollView.getScrollX());
                    int theright = (int) (mtempRect.right - mScrollView.getScrollX());
                    if (theleft < mTleft && mTright < theright) {
                        mLine = tempLine;
                        break;
                    }

                }
                if (null != mLine) {
                    mDraggedLayout.setVisibility(View.VISIBLE);
                    int mScreenLeft = -mScrollView.getHalfParentWidth() + mScrollView.getScrollX();
                    Rect currentItem = mLine.getRect();
                    Rect currentScreen = new Rect(mScreenLeft - 2,
                            currentItem.top - 5,
                            CoreUtils.getMetrics().widthPixels + mScreenLeft
                                    + 2, currentItem.bottom + 5);

                    int mleft = 0;
                    int moffset = mSplitView.getSplitRectLeft(mLine.getStart(), mLine);

                    int maxWidth = mDispMetrics.widthPixels / 3 * 2;

                    int maxRectWidth = mLine.getRect().width();
                    if (maxRectWidth > maxWidth) {

                        int half = mDispMetrics.widthPixels / 2;
                        int mr = mScrollView.getScrollX() + x;
                        if (x > half) { // 点击的是右边
                            moffset = Math.min(mr, mLine.getRect().right) - maxWidth;
                        } else {
                            moffset = Math.min(Math.max(mLine.getRect().left, mr - maxWidth / 2),
                                    mLine.getRect().right - maxWidth);

                        }

                    }

                    Bitmap tempbmp;
                    if (currentScreen.left < currentItem.left
                            && currentItem.right < currentScreen.right) {// 该区域在可视范围内
                        mleft = currentItem.left - mScrollView.getScrollX()
                                + mScrollView.getHalfParentWidth();
                        tempbmp = getThumbBitmap(moffset, mLine);

                    } else {
                        mleft = x - moffset;
                        if (mleft < 0) {
                            mleft = 0;
                        }
                        tempbmp = getThumbBitmap(moffset, mLine);
                    }

                    int[] location = new int[2];
                    mScrollView.getLocationOnScreen(location);
                    int[] top = new int[2];
                    View playview_rel = Utils.$(mRoot, R.id.rlPreview);
                    playview_rel.getLocationOnScreen(top);
                    final int mtop = location[1] - top[1];
                    final int mleftt = mleft;
                    mSplitView.setIsEditing(mLine);

                    final Bitmap bp = tempbmp;

                    final int imageCenterY = playview_rel.getHeight() / 2;
                    mDraggedView.postDelayed(new Runnable() {

                        @Override
                        public void run() { // 计算中心点
                            mDraggedView.setTrash(true);
                            mDraggedView.initTrashRect(imageCenterY);
                            mDraggedView.setData(bp, mleftt, mtop,
                                    mleftt + bp.getWidth(),
                                    mtop + bp.getHeight());

                        }
                    }, 200);
                }
            }
        });

    }

    /**
     * 获取该区域内的缩略图，生成一张小范围的大致的缩略图
     *
     * @param moffset
     * @param mLine
     * @return
     */
    private Bitmap getThumbBitmap(int moffset, SplitItem mLine) {
        int width = 0, height = ThumbNailUtils.THUMB_HEIGHT;
        ArrayList<SplitThumbItemInfo> thumblist = mLine.getList();
        width = mLine.getRect().width();
        int maxWidth = mDispMetrics.widthPixels / 3 * 2;
        final int mwidth = Math.min(maxWidth, width);
        Bitmap bmp = Bitmap.createBitmap(mwidth, height, Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        Bitmap tbmp = null;
        Rect tdst = new Rect();
        SplitThumbItemInfo info;
        int len = thumblist.size();

        for (int i = 0; i < len; i++) {
            info = thumblist.get(i);
            tbmp = mSplitView.getBitmapFromMemCache(info.nTime);
            if (null != tbmp && !tbmp.isRecycled()) {
                tdst.set(info.dst.left - moffset, info.dst.top, info.dst.right
                        - moffset, info.dst.bottom);
                if (tdst.left < mwidth) {
                    if (tdst.right > mwidth) {
                        int offpx = tdst.right - mwidth;
                        tdst.set(tdst.left, tdst.top, tdst.right - (offpx),
                                tdst.bottom);
                        Rect msrc = new Rect(info.src.left, info.src.top,
                                info.src.right - offpx, info.src.bottom);
                        cv.drawBitmap(tbmp, msrc, tdst, null);

                    } else {
                        cv.drawBitmap(tbmp, info.src, tdst, null);

                    }
                }
            }
        }
        cv.save();
        return bmp;

    }


    private DraggedView.onTrashListener mOnTrashlistener = new DraggedView.onTrashListener() {

        @Override
        public void onCancel() {
            if (mDraggedLayout.getVisibility() == View.VISIBLE) {
                mDraggedLayout.setVisibility(View.GONE);
            }
            mSplitView.setIsEditing(null);
            mParentFrame.setForceToTarget(false);
            mPriviewLinearLayout.setEnableTouch(true);

        }

        @Override
        public void onDelete() {
            SplitItem tempItem = mSplitView.getIsEditing();
            if (null != tempItem) {
                mSplitView.remove(tempItem);
            }
            mTvEndTime.setText(DateTimeUtils.stringForMillisecondTime(
                    mSplitView.getDuration(), true, true));
            if (mDraggedLayout.getVisibility() == View.VISIBLE) {
                mDraggedLayout.setVisibility(View.GONE);
            }
            mParentFrame.setForceToTarget(false);
            mPriviewLinearLayout.setEnableTouch(true);
            if (mSplitView.getSplits().size() < 0) {
                onBackPressed();
            } else {

                initLayout();
                mScrollView.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        int progress = getProgressByPx(mScrollView.getScrollX());
                        iSplitHandler.onTemp(getMediaObject(), progress);
                        setProgressText(mScrollView.getScrollX());
                    }
                }, 200);
            }

        }
    };

    private void initLayout() {

        Rect rect = mSplitView.getMaxRect();

        mScrollView.setLineWidth(rect.right);
        mScrollView.setDuration((int) mSplitView.getDuration());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                rect.right, ThumbNailUtils.THUMB_HEIGHT);
        lp.setMargins(mScrollHalf, 0, mScrollHalf,
                0);
        mSplitView.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 3, 0, 0);
        mMediaLinearLayout.setLayoutParams(lframe);
    }

    private DisplayMetrics mDispMetrics = CoreUtils.getMetrics();

    /**
     * 根据当前播放器的位置，计算出当前应该显示的进度(np在每个item范围内)
     *
     * @param scrollX 相对于播放器进度为0时的位置 （单位px）
     */
    private void setProgressText(int scrollX) {

        int np = 0;
        if (hasSplit()) {
            SplitItem item = mSplitView.getSplitItemByScrollX(scrollX);
            if (null != item) {
                np = getScrollProgressBySelf(item, scrollX);
            } else {
                np = 0;
            }
        } else {
            np = mScrollView.getProgress();
        }
        setProgress(np);
    }

    /**
     * 显示进度
     *
     * @param np 单位:ms
     */
    private void setProgress(int np) {
        mTvSplitProgress.setText("+" + DateTimeUtils.stringForMillisecondTime(np));
    }

    /**
     * 获取该位置上videoobject的进度
     *
     * @param scrollX
     * @return
     */
    private int getProgressByPx(int scrollX) {
        if (hasSplit()) {
            SplitItem item = mSplitView.getSplitItemByScrollX(scrollX);
            if (null != item) {
                return getScrollProgress(item, scrollX);
            }
            return 0;

        } else {
            return mScrollView.getProgress();
        }

    }

    private ScrollViewListener mScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY,
                                     boolean appScroll) {
            int[] dialogTextLocal = new int[2];
            mFirstDialog.getTextView().getLocationOnScreen(dialogTextLocal);
            if (dialogTextLocal[0] > 0) {
                mFirstDialog.setTranslationX(-scrollX);
            }
            if (scrollX == 0) {
                mFirstDialog.setTranslationX(0);
            }
            if (!appScroll && mIsSpliting) {
                int progress = getProgressByPx(scrollX);
                setProgressText(scrollX);
                if (mMediaPrepared) {
                    iSplitHandler.onSeekTo(progress);
                }
            }

        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY, boolean appScroll) {
            if (!appScroll && mIsSpliting) {
                int progress = getProgressByPx(scrollX);
                setProgressText(scrollX);
                if (mMediaPrepared) {
                    iSplitHandler.onScrollEnd(progress);
                }
            }
        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY,
                                  boolean appScroll) {
            if (!appScroll && mIsSpliting) {
                int progress = getProgressByPx(scrollX);
                setProgressText(scrollX);
                if (mMediaPrepared)
                    iSplitHandler.onScrollBegin(progress);
            }
        }
    };

    /***
     * @param item
     *            当前位置所在的时间片段
     * @param scrollX
     *            当前滚动的位置（单位px)
     * @return 手动滚动时间轴 ，获取当前位置的progress (单位ms)
     */
    private int getScrollProgress(SplitItem item, int scrollX) {
        int np = getScrollProgressBySelf(item, scrollX) + item.getStart();
        return Math.max(np, Math.min(np, item.getEnd()));
    }

    /**
     * @param item    当前的item
     * @param scrollX 滚动组件滑动的痕迹，相对于最左边的位置
     * @return 当前item范围内的progress
     */

    private int getScrollProgressBySelf(SplitItem item, int scrollX) {
        int np = (int) ((scrollX - item.getRect().left + 0.0)
                / item.getRect().width() * item.getDuration());
        return Math.max(np, Math.min(np, item.getDuration()));
    }

    /**
     * 拆分后一个视频分成n块，记录每块的起止位置，
     *
     * @return
     */
    public ArrayList<MediaObject> getMediaObject() {

        ArrayList<MediaObject> list = new ArrayList<MediaObject>();
        SplitItem tempItem;
        int len = mSplitView.getSplits().size();
        MediaObject src = mMedia.getAllMedia().get(0);
        ArrayList<EffectInfo> effectInfos = src.getEffectInfos();
        //原始媒体绑定的滤镜特效
        EffectInfo info = (effectInfos == null || effectInfos.size() == 0) ? null : effectInfos.get(0);
        for (int i = 0; i < len; i++) {
            MediaObject clone = src.clone();
            tempItem = mSplitView.getSplits().get(i);
            clone.setSpeed(src.getSpeed());
            float start = Utils.ms2s(tempItem.getTlstart());
            float send = Utils.ms2s(tempItem.getTlend());
            clone.setTimeRange(start, send);
            clone.setTag(null);
            if (null != info && info.getFilterId() != EffectInfo.Unknown) {
                EffectInfo tmp = info.clone();
                tmp.setTimeRange(0, clone.getDuration());
                ArrayList<EffectInfo> tmpList = new ArrayList<>();
                tmpList.add(tmp);
                //修正特效滤镜的时间线
                clone.setEffectInfos(tmpList);
            } else {
                //时间特效全清
                clone.setEffectInfos(null);
            }
            list.add(clone);
        }
        return list;
    }

    private boolean mMediaPrepared = false;

    public void setPrepared(boolean prepared) {
        mMediaPrepared = prepared;
    }

    public boolean onBackPressed() {
        mPriviewLinearLayout.setEnableTouch(true);
        recycle();
        return true;
    }

    private void recycle() {
        mSplitView.recycle();
        mSplitLayout.setVisibility(View.GONE);
        mPriviewLinearLayout.setEnableTouch(true);
        mScrollView.appScrollTo(0, true);
        mIsSpliting = false;
        System.gc();
        System.runFinalization();
    }

    // 分割保证最小片段duration
    private final int MIN_SPLIT_DURATION = 400;
    private OnClickListener mSplitLisenter = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // 1 找到当前halfParent(偏移的X对应的视频长度)对应的splitItem
            iSplitHandler.onScrollBegin(-1);
            if (hasSplit()) {
                int px = mScrollView.getScrollX();
                SplitItem info = mSplitView.getSplitItemByScrollX(px);
                if (null != info) {
                    // 有拆分
                    int progress = getProgressByPx(px);
                    int np = progress - info.getStart();
                    if (np < 0) {
                        np = 0;
                    }
                    if (np > MIN_SPLIT_DURATION
                            && (info.getEnd() - info.getStart() - np) > MIN_SPLIT_DURATION) {

                        progress = (int) (info.getStart() + (px
                                - info.getRect().left + 0.0)
                                / (info.getRect().width()) * info.getDuration());

                        mSplitView.onSplit(progress);
                        /**
                         * 分割之后移动到下一段的开始位置
                         */
                        mScrollView.appScrollBy(
                                mSplitView.getSplitThumbWidth(), true);
                        iSplitHandler.onTemp(getMediaObject(), progress);
                        setProgress(0);
                        initLayout();
                    } else {
                        onAutoSplitLessThan();
                    }
                }
            } else {
                long progress = mScrollView.getProgress();
                if (progress < MIN_SPLIT_DURATION
                        || (mDuration - progress) < MIN_SPLIT_DURATION) {
                    onAutoSplitLessThan();
                    return;
                }
                int p = (int) progress;
                mSplitView.onSplit(p);
                mScrollView.appScrollBy(mSplitView.getSplitThumbWidth(), true);
                iSplitHandler.onTemp(getMediaObject(), p);
                setProgress(0);
                initLayout();
            }

        }
    };

    private void onAutoSplitLessThan() {
        float min = MIN_SPLIT_DURATION / 1000.0f;
        String str = mContext.getString(R.string.split_duration_less_than, min);
        Utils.autoToastNomal(mContext, str);
    }


    private boolean hasSplit() {
        return (mSplitView.getSplits().size() >= 1) ? true : false;
    }

    interface ISplitHandler {

        void onSeekTo(int progress);

        void onScrollBegin(int progress);

        void onScrollEnd(int progress);

        void onSure(ArrayList<MediaObject> list);

        void onCancel();

        /**
         * 分割视频后，从该位置播放
         *
         * @param list
         * @param progress
         */
        void onTemp(ArrayList<MediaObject> list, int progress);

        void onTouchPause();

    }

    private boolean mIsSpliting = false; // 进入拆分界面

    /**
     * 正在拆分界面
     *
     * @return
     */
    public boolean isSpliting() {
        return mIsSpliting;
    }

    private Scene mMedia;
    private int mDuration;

    public void init(Scene mo) {
        mSplitLayout.setVisibility(View.VISIBLE);
        mMedia = mo;
        mTvEndTime.setText(DateTimeUtils.stringForMillisecondTime(
                Utils.s2ms(mMedia.getDuration()), true, true));
        setPrepared(true);
        mIsSpliting = true;
        mScrollView.drawBaseline(true);
        setProgress(0);// 还原text
        mHandler.obtainMessage(PREPARED).sendToTarget();
    }

    private final int PREPARED = 6;

    private int mScrollHalf = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {

                case PREPARED: {

                    int halfScreenWidth = CoreUtils.getMetrics().widthPixels / 2;
                    mScrollHalf = halfScreenWidth - mThumbMargin;
                    mScrollView.setHalfParentWidth(mScrollHalf);

                    VirtualVideo virtualVideo = new VirtualVideo();
                    virtualVideo.addScene(mMedia);
                    try {
                        virtualVideo.build(mContext);
                    } catch (InvalidStateException e) {
                        e.printStackTrace();
                    }
                    mDuration = Utils.s2ms(virtualVideo.getDuration());
                    int[] params = mSplitView.setVirtualVdieo(virtualVideo, mMedia);

                    mScrollView.setLineWidth(params[0]);
                    mScrollView.setDuration((int) mDuration);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            params[0], params[1]);
                    lp.setMargins(mScrollHalf, 0, mScrollHalf, 0);
                    mSplitView.setLayoutParams(lp);

                    FrameLayout.LayoutParams lframe = new LayoutParams(lp.width
                            + lp.leftMargin + lp.rightMargin, lp.height);
                    lframe.setMargins(0, 10, 0, 0);
                    mMediaLinearLayout.setLayoutParams(lframe);
                    mSplitView.setStartThumb();
                    if (AppConfiguration.isFirstShowDialogSplit()) {
                        AppConfiguration.setIsFirstDialogSplit();
                        mFirstDialog.setAutoViewMargin(0);
                        mFirstDialog.setUpOrDown(false, 80, R.string.drag_for_split, true, 0.5);
                        mScreen.setVisibility(View.VISIBLE);
                        mScreen.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mScreen.setVisibility(View.GONE);
                                mFirstDialog.setVisibility(View.GONE);
                            }
                        });
//                        PopViewUtil.showPopupWindow(mSplitView, false,
//                                true, 80, true, -80,
//                                new PopViewUtil.CallBack() {
//
//                                    @Override
//                                    public void onClick() {
//                                        AppConfiguration
//                                                .setIsFirstDragSplit();
//                                    }
//                                }, R.string.drag_for_split, 0.5);


                    }
                }
                break;
                default:
                    break;
            }

        }

        ;
    };

    /**
     * 播放中的进度
     *
     * @param progress 单位：毫秒
     */
    public void onScrollProgress(int progress) {
        if (mSplitView.getSplits().size() > 0) {
            SplitItem item = mSplitView.getSplitItemByMin(progress);
            if (null != item) {
                setProgress((int) Math.max(progress - item.getStart(), 0));
                double mp = ((progress - item.getStart() + 0.0) / item
                        .getDuration());
                int moffset = (int) (item.getRect().width() * mp);
                int scrollX = item.getRect().left + moffset;
                mScrollView.appScrollTo(scrollX, true);
            }
        } else {
            mScrollView.setProgress(progress);
            setProgress(progress);
        }

    }

    /**
     * @param progress ms
     */
    public void onScrollProgressOff(int progress) {
        if (mSplitView.getSplits().size() > 0) {
            SplitItem item = mSplitView.getSplitItemByMin(progress);
            if (null != item) {
                setProgress(Math.max(progress - item.getStart(), 0));
                double mp = ((progress - item.getStart() + 0.0) / item
                        .getDuration());
                int moffset = (int) (item.getRect().width() * mp);
                int scrollX = item.getRect().left + moffset;
                mScrollView.appScrollTo(scrollX, true);
            }
        } else {
            mScrollView.setProgress(progress);
            setProgress(progress);
        }
    }

    /**
     * 播放完成
     */
    public void onScrollCompleted() {
        mScrollView.appScrollTo(0, true);
        setProgress(0);
    }

}
