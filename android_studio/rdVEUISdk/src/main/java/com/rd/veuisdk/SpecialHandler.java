package com.rd.veuisdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rd.cache.HttpImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.JSONObjectEx;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.IVideoEditorHandler.EditorPreivewPositionListener;
import com.rd.veuisdk.adapter.SpecialStyleAdapter;
import com.rd.veuisdk.database.SpecialData;
import com.rd.veuisdk.export.IExportSpecial;
import com.rd.veuisdk.export.SpecialExportUtils;
import com.rd.veuisdk.model.FrameInfo;
import com.rd.veuisdk.model.SpecialInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SpecialUtils;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.PopViewUtil;
import com.rd.veuisdk.ui.SinglePointRotate;
import com.rd.veuisdk.ui.SinglePointRotate.onDelListener;
import com.rd.veuisdk.ui.SizePicker;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.ui.TimeLine;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 添加特效用到的handler
 *
 * @author JIAN
 */
class SpecialHandler {
    private final String TAG = "SpecialHandler";
    private static final int OFFSET_END_POSTION = 0;
    private View mRoot;
    private View mMenuLayout;
    private IVideoEditorHandler mEditorHandler;
    private Context mContext;
    private RadioButton mFunny, mUnclassified, mMore;
    private ImageView mPlayState;
    private TimelineHorizontalScrollView mScrollView;
    private LinearLayout mMediaLinearLayout, mSizeLinearLayout;
    private ThumbNailLine mSubLine;
    private ArrayList<WordInfo> mWordInfoList = new ArrayList<WordInfo>(),
            mTempWordList = new ArrayList<WordInfo>();
    private FrameLayout mLinearWords;
    private GridView mSubtitleStyle;
    private HttpImageFetcher mFetcher;
    private View mViewWordHint;
    private View mAddLayout;
    private Button mBtnDelete;
    private ImageView mIvAddSubtitle;
    private TextView mTvAdd;
    private ISpecailListener mSpListener;
    private SizePicker mSizePicker;
    private TextView mTvProgress;
    private TextView mBtnSave;
    private WordInfo mCurInfo;
    private boolean mIsUpdate = false;
    private int mHalfWidth = 0;
    private TimeLine mSizeTimeline;
    private int mStateSize = 0; //播放按钮的大小

    private boolean mIsDownloading = false;


    public SpecialHandler(View view, IVideoEditorHandler hlrEditor,
                          FrameLayout sublayout, ISpecailListener listener) {
        mEditorHandler = hlrEditor;
        mRoot = view;
        mSpListener = listener;

        ImageCacheParams cacheParams = new ImageCacheParams(view.getContext(),
                null);
        cacheParams.compressFormat = CompressFormat.PNG;
        // 缓冲占用系统内存的10%
        cacheParams.setMemCacheSizePercent(0.1f);
        mFetcher = new HttpImageFetcher(view.getContext(), 252, 222);
        mFetcher.addImageCache(view.getContext(), cacheParams);
        mLinearWords = sublayout;
        mContext = mRoot.getContext();
        mSubtitleStyle = (GridView) mRoot.findViewById(R.id.special_style_gridview);

        mIvAddSubtitle = (ImageView) mRoot.findViewById(R.id.add_subtitle);
        mTvAdd = (TextView) mRoot.findViewById(R.id.tv_add_subtitle);
        mBtnDelete = (Button) mRoot.findViewById(R.id.subtitle_del_item);
        mTvProgress = (TextView) mRoot.findViewById(R.id.tvAddProgress);

        mMenuLayout = mRoot.findViewById(R.id.special_menu_layout);

        mIvAddSubtitle.setOnClickListener(onAddListener);

        mPlayState = (ImageView) mRoot.findViewById(R.id.ivPlayerState);
        mAddLayout = mRoot.findViewById(R.id.subtitle_add_layout);
        mFunny = (RadioButton) mRoot.findViewById(R.id.funny);
        mUnclassified = (RadioButton) mRoot.findViewById(R.id.unclassified);
        mMore = (RadioButton) mRoot.findViewById(R.id.special_more);

        mScrollView = (TimelineHorizontalScrollView) mRoot
                .findViewById(R.id.priview_subtitle_line);
        mScrollView.enableUserScrolling(true);

        mMediaLinearLayout = (LinearLayout) mRoot
                .findViewById(R.id.subtitleline_media);
        mSubLine = (ThumbNailLine) mRoot.findViewById(R.id.subline_view);

        mScrollView.addScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(mViewTouchListener);

        mSubtitleStyle.setOnItemClickListener(mStyleItemListener);
        mSubLine.setSubtitleThumbNailListener(mSubtitleListener);
        mViewWordHint = mRoot.findViewById(R.id.word_hint_view);

        mBtnSave = (TextView) mRoot.findViewById(R.id.save_special);
        mBtnSave.setOnClickListener(onSaveListener);

        mBtnDelete.setOnClickListener(mOnDeleteListener);
        mSizePicker = (SizePicker) mRoot.findViewById(R.id.subtitle_sizepicker);
        mSizeTimeline = (TimeLine) mRoot.findViewById(R.id.timeline_size);
        mSizeLinearLayout = (LinearLayout) mRoot
                .findViewById(R.id.subtitle_size_linearlayout);

        mSizeTimeline.addScrollListener(mSizeOnScrollListener);
        ((RadioGroup) mRoot.findViewById(R.id.sp_rg))
                .setOnCheckedChangeListener(onCheckedChangeListener);
        mHalfWidth = CoreUtils.getMetrics().widthPixels / 2;
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);

    }


    private OnClickListener onSaveListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mIsDownloading) {
                SysAlertDialog.showLoadingDialog(mContext,
                        mContext.getString(R.string.isloading));
            } else {
                SysAlertDialog.cancelLoadingDialog();
                onSaveBtnItem();
            }
        }
    };
    private OnClickListener mOnDeleteListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mCurInfo = checkWord(mScrollView.getProgress());

            if (null != mCurInfo) {
                View target = mLinearWords.findViewById(mCurInfo.getId());
                if (null != target) {
                    mLinearWords.removeView(target);
                    ((SinglePointRotate) target).recycle();
                }
                onDelWordItem(mCurInfo.getId());

            }
            mCurInfo = null;
        }
    };

    private IViewTouchListener mViewTouchListener = new IViewTouchListener() {

        @Override
        public void onActionDown() {
            if (isEditing()) {
                if (mAddStep) {
                    onWordEnd();
                } else {
                    mEditorHandler.pause();
                    int nprogress = mScrollView.getProgress();
                    mEditorHandler.seekTo(nprogress);
                    clearView(nprogress);
                    initWords(nprogress);
                    setProgressText(nprogress);
                    checkVisible(nprogress);
                }
            }
        }

        @Override
        public void onActionMove() {
//            Log.e(TAG, "onActionMove");
            if (isEditing()) {
                int nprogress = mScrollView.getProgress();
                mEditorHandler.seekTo(nprogress);
                clearView(nprogress);
                initWords(nprogress);
                setProgressText(nprogress);
                checkVisible(nprogress);
            }
        }

        @Override
        public void onActionUp() {
//            Log.e(TAG, "onActionUp");
            if (isEditing()) {
                mScrollView.resetForce();
                int nprogress = mScrollView.getProgress();
                clearView(nprogress);
                initWords(nprogress);
                setProgressText(nprogress);
                checkVisible(nprogress);

            }

        }
    };

    void onResume() {
        onFetcher();
    }

    private void onFetcher() {
        if (null != mSpAapter) {
            mSpAapter.onResume();
        }
        if (null != mSprCurView) {
            mSprCurView.onResume();
        }
    }

    void onPasue() {
        if (null != mSpAapter) {
            mSpAapter.onPasue();
        }
        if (null != mSprCurView) {
            mSprCurView.onPasue();
        }
    }

    void onDestory() {
        bUIPrepared = false;
        mHandler.removeCallbacks(resetSpDataRunnable);
        if (null != mWordInfoList) {
            mWordInfoList.clear();
        }
        mSubLine.recycle(true);
        if (null != mSpAapter) {
            mSpAapter.onDestory();
            mSpAapter = null;
        }

        mScrollView.addScrollListener(null);
        mScrollView.setViewTouchListener(null);
        mSizeTimeline.addScrollListener(null);
        mSubtitleStyle.setOnItemClickListener(null);
        mSubLine.setSubtitleThumbNailListener(null);

        mSubLine = null;

        mSizeOnScrollListener = null;
        mViewTouchListener = null;
        mThumbOnScrollListener = null;
        mStyleItemListener = null;
        mSubtitleListener = null;
        mGetSpecialRunnable = null;
        mHandler.removeMessages(MSG_LISTVIEW);
        mHandler = null;
    }

    private void setProgressText(int progress) {
        mTvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
    }

    private OnItemClickListener mStyleItemListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            onStyleItem(position);

        }
    };

    /**
     * @param txtSize
     * @return
     */
    private float getCustomSize(int txtSize) {
        //像素大小转成缩放比
        float disf = mSizePicker.textToDisf(txtSize);
        mSizePicker.setDisf(disf);
        return disf;
    }

    private void onStyleItem(int position) {
        StyleInfo info = mSpAapter.getItem(position);
        if (null == info) {
            Log.e(TAG, "onStyleItem->info==null");
            return;
        }
        if (info.isdownloaded) {
            mCurInfo.setStyleId(info.pid);
            if (mCurInfo.getDisf() == 1) {
//                mCurInfo.setDisf(info.disf);
                float mdisf = 1.4f;
                if (CoreUtils.getMetrics().density >= 3.0) {
                    //1080p屏幕
                    mdisf = getCustomSize(22);
                } else {
                    //<=720p 屏幕
                    mdisf = getCustomSize(17);
                }
                mCurInfo.setDisf(mdisf);
            }
            mSpAapter.setCheckItem(position);
            if (mSprCurView != null) {
                int index = getIndex(mCurInfo.getId());
                if (index >= 0) {
                    mSprCurView.setRotate(mCurInfo.getRotateAngle());
                } else {
                    mSprCurView.setRotate(info.rotateAngle);
                }
                if (info.frameArray.size() > 0) {

                    mSprCurView.setStyleInfo(true, info, (int) (mCurInfo
                                    .getEnd() - mCurInfo.getStart()), true,
                            mCurInfo.getDisf());

                }
                int x = 0, y = 0;
                if (mCurInfo.getCenterxy()[0] != 0.5
                        && mCurInfo.getCenterxy()[1] != 0.5) {
                    x = (int) (mLayoutWidth * mCurInfo.getCenterxy()[0]);
                    y = (int) (mLayoutHeight * mCurInfo.getCenterxy()[1]);
                } else {
                    x = (int) (mLayoutWidth * info.centerxy[0]);
                    y = (int) (mLayoutHeight * info.centerxy[1]);
                }
                mSprCurView.setCenter(new Point(x, y));
                if (info.type == 0) {
                    mSprCurView.setInputText(info.getHint());
                    mSprCurView.setImageStyle(info);
                } else {
                    mSprCurView.setInputText("");
                }

            }

        } else {
            // 执行下载
            int visiblep = position % mSubtitleStyle.getChildCount();
            View child = mSubtitleStyle.getChildAt(visiblep);
            if (null != child) {
                mSpAapter.onDown(position, (ImageView) child
                                .findViewById(R.id.ttf_state),
                        (CircleProgressBarView) child
                                .findViewById(R.id.ttf_pbar));

            }

        }

    }

    private IThumbLineListener mSubtitleListener = new IThumbLineListener() {

        @Override
        public void onTouchUp() {
            mViewTouchListener.onActionUp();
        }

        @Override
        public void updateThumb(int id, int start, int end) {
            Log.e("updateThumb..", id + "....." + start + "....." + end);
            mIsUpdate = true;
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }

            int index = getIndex(id);
            if (index >= 0) {
                WordInfo info = mWordInfoList.get(index);
                if (null != info && id == info.getId()) {
                    info.setStart(start);
                    info.setEnd(end);
                    mWordInfoList.set(index, info);
                    updateItem(info);
                }

            }

        }

        @Override
        public void onTouch(int id, int start, int end) {


        }

        @Override
        public void onCheckItem(boolean changed, int id) {
            // Log.e("onCheckItem..", id + "....." + changed);
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }

            if (AppConfiguration.isFirstShowDragSp()) {

                int[] pxs = mSubLine.getCurrentPx(id);
                if (null != pxs) {
                    PopViewUtil.showPopupWindowStyle(mSubLine, true, true, 120,
                            true, (pxs[0] + pxs[1]) / 2,
                            new PopViewUtil.CallBack() {

                                @Override
                                public void onClick() {
                                    AppConfiguration.setIsFirstDragSp();
                                }
                            }, R.string.drag_for_sp, 0.5);
                }
            } else {

                int index = getIndex(id);
                if (index >= 0) {
                    WordInfo info = mWordInfoList.get(index);
                    mCurInfo = new WordInfo(info);
                    checkVisible(mCurInfo);
                    if (changed) {
                        mLinearWords.removeAllViews();
                        if (null != mSprCurView) {
                            mSprCurView.recycle();
                            mSprCurView = null;
                        }
                        initsp();
                    }
                }
            }

        }
    };

    private void updateItem(WordInfo info) {

        if (null != info) {

            int progress = (int) (((info.getEnd() - info.getStart()) / 2) + info
                    .getStart());
            initWords(progress);
            clearView(progress);

        }
    }

    /**
     * 根据id 获取集合中的数据
     *
     * @param id
     * @return
     */
    private WordInfo getItem(int id) {
        int index = getIndex(id);
        return (index != -1) ? mWordInfoList.get(index) : null;
    }

    /**
     * 移除指定的项
     *
     * @param id
     */
    private void removeById(int id) {
        int index = getIndex(id);
        if (index > -1 && index <= (mWordInfoList.size() - 1)) {
            mWordInfoList.remove(index);
        }
    }

    private int mDuration;

    /**
     * 重新进入和退出时清除List到默认
     */
    private void onListReset(boolean isInit) {
        mWordInfoList.clear();
        if (isInit) {
            mTempWordList.clear();
        }
        ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                .getSpecailsDurationChecked();
        int len = tempList.size();
        WordInfo temp;
        for (int i = 0; i < len; i++) {
            temp = tempList.get(i);
            temp.resetChanged();

            mWordInfoList.add(temp);
            if (isInit) {
                mTempWordList.add(temp.clone());
            }
        }

    }

    /**
     * 进入特效
     */
    public void init() {
        mIsSubing = true;
        mAddStep = false;
        mDuration = mEditorHandler.getDuration();
        mLayoutWidth = mLinearWords.getWidth();
        mLayoutHeight = mLinearWords.getHeight();
        onListReset(true);
        mRoot.setVisibility(View.VISIBLE);
        mViewWordHint.setVisibility(View.VISIBLE);
        mMenuLayout.setVisibility(View.GONE);
        mAddLayout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.editor_preview_slide_in));
        mAddLayout.setVisibility(View.VISIBLE);
        initThumbTimeLine();
        setImage(R.drawable.edit_music_play);
        mTvAdd.setText(R.string.add_special);

        if (null != mEditorHandler) { // 清除另一种方式下的字幕
            mEditorHandler
                    .registerEditorPostionListener(mEditorPreivewPositionListener);
            mEditorHandler.reload(false);
            mEditorHandler.seekTo(0);
        }
        mSubLine.setCantouch(true);
        mSubLine.setMoveItem(true);
        mPlayState.setOnClickListener(onStateChangeListener);

        mIvAddSubtitle.setImageResource(R.drawable.add_special_btn);
        mSpAapter = new SpecialStyleAdapter(mContext, false);
        SpecialUtils.getInstance().recycle();
        mSubtitleStyle.setAdapter(mSpAapter);
        if (null != mSpAapter && mSpAapter.getCount() == 0) {
            getSpecialData();
        }

    }

    private EditorPreivewPositionListener mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

        @Override
        public void onEditorPreviewComplete() {
            onScrollCompleted();
            if (mAddStep) {
                onWordEnd();
            }
        }

        @Override
        public void onEditorPrepred() {
            if (null != mEditorHandler) { // 取消loading...
                mEditorHandler.cancelLoading();
            }
            initThumbTimeLine(mEditorHandler.getSnapshotEditor());
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
            if (mAddStep) {
                int mleft = mSubLine.getCurrent();
                if (-1 != mleft) {
                    int maxMs = mSubLine.getMaxRightbyMs(mleft);
                    if (nPosition >= maxMs) {
                        onWordEnd();
                    }
                }
            }
        }
    };


    /**
     * 初始化缩略图时间轴
     */
    private void initThumbTimeLine(VirtualVideo virtualVideo) {

        mSubLine.setVirtualVideo(virtualVideo);
        mSubLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        onScrollProgress(0);
        mHandler.postDelayed(resetSpDataRunnable, 100);
    }

    //恢复特效数据
    private Runnable resetSpDataRunnable = new Runnable() {
        @Override
        public void run() {
            ArrayList<SubInfo> sublist = new ArrayList<SubInfo>();
            int len = mWordInfoList.size();
            for (int i = 0; i < len; i++) {
                sublist.add(new SubInfo(mWordInfoList.get(i)));
            }
            mSubLine.prepareData(sublist);

            checkVisible(0);
            mSubLine.setStartThumb(mScrollView.getScrollX());

            if (AppConfiguration.isFirstShowInsertSp()) {
                PopViewUtil.showPopupWindowStyle(mSubLine, true, true, 120,
                        true, mSubLine.getpadding(),
                        new PopViewUtil.CallBack() {

                            @Override
                            public void onClick() {
                                AppConfiguration.setIsFirstInsertSp();
                            }
                        }, R.string.drag_thumb_for_insert_sp, 0.5);
            }
            bUIPrepared = true;
        }
    };


    /**
     * 初始化缩略图时间轴
     */
    private void initThumbTimeLine() {

        mScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        int[] params = mSubLine.setDuration(mDuration,
                mScrollView.getHalfParentWidth());
        mScrollView.setLineWidth(params[0]);
        mScrollView.setDuration(mDuration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(params[0]
                + 2 * mSubLine.getpadding(), params[1]);

        lp.setMargins(mScrollView.getHalfParentWidth() - mSubLine.getpadding(),
                0, mHalfWidth - mSubLine.getpadding(), 0);
        mSubLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);
        mMediaLinearLayout.setLayoutParams(lframe);
        mSubLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        mViewWordHint.setVisibility(View.GONE);
    }

    private ScrollViewListener mThumbOnScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY,
                                     boolean appScroll) {
//            Log.e("onScrollProgress...", "onScrollProgress..." + appScroll
//                    + "...." + scrollX);
            if (isEditing()) {
                mSubLine.setStartThumb(mScrollView.getScrollX());
                int nprogress = mScrollView.getProgress();
                if (!appScroll) {
                    if (mEditorHandler != null) {
                        mEditorHandler.seekTo(nprogress);
                    }
                    clearView(nprogress);
                    initWords(nprogress);
                    setProgressText(nprogress);
                }
                checkVisible(nprogress);
            }

        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY,
                                boolean appScroll) {

            if (isEditing()) {

                int nprogress = mScrollView.getProgress();
                mSubLine.setStartThumb(mScrollView.getScrollX());
                if (!appScroll) {
                    if (mEditorHandler != null) {
//                        Log.e(TAG, "onScrollEnd..." + appScroll + ".....>" + nprogress);
                        mEditorHandler.pause();
                        mEditorHandler.seekTo(nprogress);
                    }
                }
                clearView(nprogress);
                initWords(nprogress);
                setProgressText(nprogress);
                checkVisible(nprogress);
            }

        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY,
                                  boolean appScroll) {
//            Log.e("onScrollBegin...", "onScrollBegin..." + appScroll + "...."
//                    + scrollX);
            if (isEditing()) {
                int nprogress = mScrollView.getProgress();
                mSubLine.setStartThumb(mScrollView.getScrollX());
                if (!appScroll) {
                    if (mEditorHandler != null) {
                        mEditorHandler.pause();
                        mEditorHandler.seekTo(nprogress);
                    }
                    clearView(nprogress);
                    setProgressText(nprogress);
                }

                checkVisible(nprogress);
            }

        }
    };

    private WordInfo checkVisible(int nprogress) {

        WordInfo current = checkWord(nprogress);

        checkVisible(current);
        return current;
    }

    private void checkVisible(WordInfo current) {
        if (!mAddStep) {
            if (null != current) {
                mIvAddSubtitle.setImageResource(R.drawable.subtitle_edit_p);
                mBtnDelete.setVisibility(View.VISIBLE);
                mTvAdd.setText(R.string.special_edit);
                mBtnSave.setText(R.string.complete);
                mSubLine.showCurrent(current.getId());
            } else {
                mBtnSave.setText(R.string.add);
                mTvAdd.setText(R.string.add_special);
                mIvAddSubtitle.setImageResource(R.drawable.add_special_btn);
                mBtnDelete.setVisibility(View.GONE);
                mSubLine.setShowCurrentFalse();
            }
        }
    }

    private void onToast(String msg) {
        Utils.autoToastNomal(mContext, msg);
    }

    /**
     * 正在编辑字幕...
     */
    private boolean mIsSubing = false;

    private boolean mAddStep = false;// 新增true，编辑false

    //UI数据恢复成功(播放器初始化成功，且之前的集合恢复成功)
    private boolean bUIPrepared = false;
    /**
     * 开始添加字幕
     */

    private OnClickListener onAddListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!bUIPrepared) {
                android.util.Log.e(TAG, "onAddListener: recovering special data ...");
                return;
            }

            if (mEditorHandler != null && mEditorHandler.isPlaying()) {
                pauseVideo();
            }

            if (mSprCurView != null) { // 保存处于编辑状态的item
                mSubLine.clearCurrent();
                mSprCurView.recycle();
                mSprCurView = null;
            }

            if (mSpAapter.getCount() == 0) {
                int re = CoreUtils.checkNetworkInfo(v.getContext()
                        .getApplicationContext());
                if (re == CoreUtils.UNCONNECTED) {
                    com.rd.veuisdk.utils.Utils.autoToastNomal(v.getContext(),
                            R.string.please_check_network);
                } else {
                    SysAlertDialog.showLoadingDialog(mContext,
                            mContext.getString(R.string.isloading));

                }
                getSpecialData();
            } else {
                String menu = mTvAdd.getText().toString();
                if (menu.equals(mContext.getString(R.string.add_special))) {

                    int progress = mScrollView.getProgress();

                    int header = TempVideoParams.getInstance().getThemeHeader();
                    if (progress < header) {
                        onToast(v.getContext().getString(
                                R.string.addspecial_video_head_failed));
                        return;
                    }
                    int last = TempVideoParams.getInstance().getThemeLast();
                    int videoDuration = mDuration - last - header;
                    if (progress > videoDuration) {
                        onToast(v.getContext().getString(
                                R.string.addspecial_video_end_failed));
                        return;
                    }
                    if (progress > (header + videoDuration - Math.min(
                            videoDuration / 20, 500))) {
                        onToast(v.getContext().getString(
                                R.string.addspecial_video_between_failed));
                        return;
                    }
                    mAddStep = true;
                    int itemlength = CommonStyleUtils
                            .getItemLength(videoDuration);
                    int end = progress + itemlength;
                    end = Math.min(end, header + videoDuration);
                    mCurInfo = new WordInfo();
                    mCurInfo.setStart(progress);
                    mCurInfo.setEnd(end);
                    mCurInfo.setId(Utils.getWordId());

//                    android.util.Log.e(TAG, "onAddListener: " + mCurInfo.getId());

                    mSubLine.addRect(progress, end, "", mCurInfo.getId());

                    int checkId = mSpAapter.getCheckId();
                    StyleInfo info = mSpAapter.getItem(checkId);
                    if (info.isdownloaded) {
                        mCurInfo.setStyleId(info.pid);
                        onStartSub(true);
                    } else {
                        onStartSub(false);
                    }

                    v.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // Log.e("v.postDelayed", this.toString() + ".."
                            // + mSpAapter.getCheckId());
                            onStyleItem(mSpAapter.getCheckId());
                        }
                    }, 100);

                } else if (menu.equals(mContext.getString(R.string.sure))) {
                    onWordEnd();
                } else {
                    mAddStep = false;
                    WordInfo temp = checkVisible(mScrollView.getProgress());

                    if (null != temp) {
                        mCurInfo = new WordInfo(temp);
                    }
                    if (null != mCurInfo) {
                        onStyleItem(mSpAapter.getPosition(mCurInfo
                                .getStyleId()));
                        onStartSub(true);

                    }
                }

            }

        }
    };

    /**
     * 响应item的结束点
     */
    private void onWordEnd() {
        if (null == mCurInfo) {
            return;
        }

        // 保存
        mAddStep = false;
        mSubLine.setIsAdding(mAddStep);
        pauseVideo();
        mCurInfo.setEnd(mEditorHandler.getCurrentPosition());
        onSaveToList(false);
        int end = (int) mCurInfo.getEnd();
        mSubLine.replace(mCurInfo.getId(), (int) mCurInfo.getStart(), end);
        if (null != mSprCurView) {
            boolean hasExit = (null != getOldWord(mSprCurView.getId()));
//            android.util.Log.e(TAG, "onWordEnd: " + hasExit);
            if (hasExit) {
                mSubLine.replace(mCurInfo.getId(), mCurInfo.getText());
            } else {
                mSubLine.removeById(mSprCurView.getId());
                mSubLine.clearCurrent();
            }
            mSprCurView.recycle();
            mSprCurView = null;
        }
        end = end + OFFSET_END_POSTION;// 向后偏移

        if (end >= mEditorHandler.getDuration()) {
            end = mEditorHandler.getDuration() - 20;
        }
//        mEditorHandler.seekTo(end);
        mCurInfo = null;
        onScrollProgress(end);

    }

    /**
     * 调整播放那个状态
     */
    private OnClickListener onStateChangeListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mAddStep) {
                onWordEnd();
                mSubLine.clearCurrent();
            } else {

                if (mEditorHandler.isPlaying()) {
                    pauseVideo();
                } else {
                    if (Math.abs(mEditorHandler.getCurrentPosition()
                            - mEditorHandler.getDuration()) < 300) {
                        mEditorHandler.seekTo(0);
                    }
                    playVideo();
                }

            }
            // mSubLine.clearCurrent();
        }
    };

    private void playVideo() {
        mEditorHandler.start();
        setImage(R.drawable.edit_music_pause);
    }

    private void pauseVideo() {
        mEditorHandler.pause();
        setImage(R.drawable.edit_music_play);
    }

    private SinglePointRotate mSprCurView;

    private void saveInfo(boolean clearCurrent) {
        int[] mTime = mSubLine.getCurrent(mSprCurView.getId());
        if (null != mTime) {
            mCurInfo.setStart(mTime[0]);
            mCurInfo.setEnd(mTime[1]);
        }

        if (null != mSprCurView) {
            double mlayout_width = mLayoutWidth + .0, mlayout_height = mLayoutHeight + .0;

            Double fx = mSprCurView.getLeft() / mlayout_width;
            Double fy = mSprCurView.getTop() / mlayout_height;

            float x1 = (float) (mSprCurView.getCenter().x / mlayout_width);
            float y1 = (float) (mSprCurView.getCenter().y / mlayout_height);
            float[] centerxy = new float[]{x1, y1};

            mCurInfo.setLeft(fx);
            mCurInfo.setTop(fy);
            mCurInfo.setTextSize((int) (mSprCurView.getTextSize()));
            mCurInfo.setRotateAngel(mSprCurView.getRotateAngle());
            mCurInfo.setTtfLocalPath(mSprCurView.getTTFlocal());
            mCurInfo.setTextColor(mSprCurView.getTextColor());
            mCurInfo.setWidthx(mSprCurView.getWidth() / mlayout_width);
            mCurInfo.setHeighty(mSprCurView.getHeight() / mlayout_height);
            mCurInfo.setCenterxy(centerxy);
            mCurInfo.setDisf(mSprCurView.getDisf());
            mCurInfo.setShadowColor(mSprCurView.getShadowColor());
        }

        mSubLine.replace(mCurInfo.getId(), (int) mCurInfo.getStart(),
                (int) mCurInfo.getEnd());
        int re = getIndex(mCurInfo.getId());
        if (re > -1) {
            mWordInfoList.set(re, mCurInfo); // 重新编辑
        } else {
            mWordInfoList.add(mCurInfo); // 新增
        }
        mCurWordId = 0;

        checkVisible(mCurInfo);

        if (clearCurrent) {
            if (mEditorHandler != null) {
                mEditorHandler.stop();
                mEditorHandler.start();
            }
        }

    }

    /**
     * 当前字幕在集合的索引
     *
     * @param id 当前字幕的Id
     * @return
     */
    private synchronized int getIndex(int id) {
        int index = -1;
        for (int i = 0; i < mWordInfoList.size(); i++) {
            if (id == mWordInfoList.get(i).getId()) {
                index = i;
                break;
            }
        }
        return index;
    }

    private int mLayoutWidth = 1024;
    private int mLayoutHeight = 1024;

    /**
     * 导出字幕
     *
     * @param nOutVideoWidth  保存视频高度
     * @param nOutVideoHeight 保存视频宽度
     * @return
     */
    public void onExport(int nOutVideoWidth, int nOutVideoHeight,
                         IExportSpecial back) {

        SpecialExportUtils sexpUtils = new SpecialExportUtils(mContext, mWordInfoList,
                mLayoutWidth, mLayoutHeight);
        sexpUtils.onExport(nOutVideoWidth, nOutVideoHeight, back);

    }

    public ArrayList<SpecialInfo> onExport(int nOutVideoWidth,
                                           int nOutVideoHeight) {

        SpecialExportUtils sexpUtils = new SpecialExportUtils(mContext, mWordInfoList,
                mLayoutWidth, mLayoutHeight);
        return sexpUtils.onExport(nOutVideoWidth, nOutVideoHeight);
    }

    /**
     * 执行删除单个word
     */
    private void onDelWordItem(int mSingleId) {
        mSubLine.removeById(mSingleId);
        mSubLine.clearCurrent();
        removeById(mSingleId);
        mCurWordId = 0;
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
        }
        mCurInfo = null;
        mSprCurView = null;
        checkVisible(mScrollView.getProgress());
    }

    /**
     * 单个特效
     */
    private void initsp() {
        if (null != mCurInfo) {
            mSprCurView = (SinglePointRotate) mLinearWords.findViewById(mCurInfo
                    .getId());

            if (null == mSprCurView) {
                mSprCurView = initItemWord(mCurInfo);
                mLinearWords.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        if (null != mSprCurView)
                            mLinearWords.addView(mSprCurView);
                    }
                }, 100);

            } else {
                mSprCurView.setVisibility(View.VISIBLE);
                mSprCurView.previewSpecailByUserEdit();
            }
            onCheckStyle(mCurInfo);
            mSprCurView.setDelListener(new onDelListener() {

                @Override
                public void onDelete(SinglePointRotate single) {
                    onDelWordItem(single.getId());
                    mLinearWords.removeView(single);
                    onRebackMenuToAddSpecial();
                    mSpListener.onViewVisible(true);
                    mAddStep = false;
                }
            });
            mSprCurView.setControl(true);// 显示控制按钮，可以随意拖动
            if (!TextUtils.isEmpty(mCurInfo.getTtfLocalPath())) {
                mSprCurView.setTTFLocal(mCurInfo.getTtfLocalPath());
            }
        }
    }

    /**
     * 删除特效 和保存特效后 ->返回到上一级页面 (添加特效)
     */
    private void onRebackMenuToAddSpecial() {
        if (mAddLayout.getVisibility() != View.VISIBLE) {
            mAddLayout.setVisibility(View.VISIBLE);
        }

    }

    private void onCheckStyle(WordInfo info) {
        if (mSpAapter.getCount() > 0) {
            // Log.e("oncheckStyle" + this.toString(), info.getText() + "");
            mSpAapter.setCheckItem(mSpAapter.getPosition(info.getStyleId()));

        }
    }

    /**
     * 开始编辑字幕
     */
    private void onStartSub(boolean isdownload) {
        mIsSubing = true;
        if (null != mEditorHandler) {
            mEditorHandler.pause();
        }
        mMenuLayout.setVisibility(View.VISIBLE);
        mSpListener.onViewVisible(false);
        mAddLayout.setVisibility(View.GONE);
        if (isdownload) {
            initsp();
            onFetcher();
        }

        if (null != mCurInfo) {
            int cp = mSpAapter.getPosition(mCurInfo.getStyleId());
            mSpAapter.setCheckItem(cp);
        } else {
            mSpAapter.setCheckItem(-1);
        }
        ((RadioButton) mRoot.findViewById(R.id.sp_style)).setChecked(true);

    }

    private int mCurWordId = 0;// 正在编辑的字幕

    /**
     * 初始化单个编辑字幕的textview hander 刷新
     */
    private void initWords(int cp) {
        int len = mWordInfoList.size();
        for (int i = 0; i < len; i++) {

            WordInfo w = mWordInfoList.get(i);
            int mid = w.getId();
            SinglePointRotate tv = (SinglePointRotate) mLinearWords
                    .findViewById(mid);
            if (w.getStart() <= cp && cp <= w.getEnd()) {
                if (null == tv) {
                    tv = initItemWord(w);
                    mLinearWords.addView(tv);
                }
                StyleInfo si = SpecialUtils.getInstance().getStyleInfo(
                        w.getStyleId());
                if (null != si.frameArray && si.frameArray.size() > 0) {

                    int itemTime = si.frameArray.valueAt(1).time
                            - si.frameArray.valueAt(0).time;
                    int np = (int) (cp - w.getStart());//当前进度在该特效段中的位置

                    int tdu = np;
                    int timeArrayCount = si.timeArrays.size();
                    boolean currentIsEditing = (null != mCurInfo && mCurInfo.getId() == w.getId());
                    if (timeArrayCount == 2) {
//                        int headDuration = si.timeArrays.get(0).getDuration();
//                        if (np > headDuration) { // 循环后面部分
//                            TimeArray loopArray = si.timeArrays.get(1);
//                            int duration = loopArray.getDuration();
//                            int j = 0;
//                            int item = 0;
//                            while (true) {
//                                item = np - duration * j;
//                                if (item <= duration) {
//                                    break;
//                                } else {
//                                    j++;
//                                }
//                            }
//                            tdu = item + headDuration;
//                            if (tdu >= si.du)
//                                tdu = tdu % si.du;
//                        }

                    } else if (timeArrayCount == 3) {

//                        int headDuration = si.timeArrays.get(0).getDuration();
                        // 循环后面部分
//                        if (np > headDuration) {
//                            TimeArray endArray = si.timeArrays.get(2);
//                            int mLastP = (int) (w.getEnd() - endArray
//                                    .getDuration());
//                            if (np > mLastP) { // 末尾部分
//                                int off = np - mLastP;
//                                tdu = endArray.getBegin() + off;
//                            } else { // 循环中间部分
//                                TimeArray loopArray = si.timeArrays.get(1);
//                                int duration = loopArray.getDuration();
//                                int j = 0;
//                                int item = 0;
//                                np = np - headDuration;
//                                while (true) {
//                                    item = np - duration * j;
//                                    if (item <= duration) {
//                                        break;
//                                    } else {
//                                        j++;
//                                    }
//                                }
//                                tdu = item + headDuration;
//
//                            }
//
//                        }
                    } else {
                        if (np > si.du) {
                            int j = 1;
                            int item = si.du + itemTime;
                            while (true) {
                                item = np - si.du * j;
                                if (item <= si.du) {
                                    break;
                                } else {
                                    j++;
                                }
                            }
                            tdu = item;
                        }
                    }
//                    Log.e(TAG, timeArrayCount + "timeArrayCount:initwords" + i + "----wordinfo:" + w.getStart() + "<>" + w.getEnd() + "---->" + tdu + "................cp:" + cp);

                    FrameInfo st = CommonStyleUtils.search(tdu, si.frameArray, si.timeArrays, currentIsEditing, (int) (w.getEnd() - w.getStart()));
                    if (null != st) {
                        tv.setImageStyle(st.pic, true);
                    } else {
                        Log.e(TAG, "initword  st is null");
                    }
                }

            } else {
                if (null != tv && mid != mCurWordId) {
                    mLinearWords.removeView(tv);
                    tv.recycle();
                    tv = null;
                }
            }

        }
    }

    /**
     * 清除当前时刻的字幕
     *
     * @param cp
     */
    private void clearView(int cp) {

        for (int j = 0; j < mLinearWords.getChildCount(); j++) {
            View v = mLinearWords.getChildAt(j);
            if (null != mWordInfoList && null != v) {
                for (int i = 0; i < mWordInfoList.size(); i++) {
                    WordInfo w = mWordInfoList.get(i);
                    if (v.getId() == w.getId()) {
                        if (w.getStart() <= cp && cp <= w.getEnd()) {
                        } else {
                            if (v.getId() != mCurWordId) {
                                mLinearWords.removeView(v);
                                ((SinglePointRotate) v).recycle();
                            }

                        }
                    }
                }
            } else {
                mLinearWords.removeAllViews();
            }
        }

    }

    /**
     * 当前位置是否有字幕
     *
     * @param ntime
     * @return
     */
    public WordInfo checkWord(int ntime) {
        WordInfo temp = null;
        for (WordInfo item : mWordInfoList) {

            if (item.getStart() - 5 <= ntime && ntime < item.getEnd() + 5) { // 防止误差
                // 5
                temp = item;
                break;
            }
            temp = null;
        }
        return temp;

    }

    public WordInfo getOldWord(int id) {
        WordInfo temp = null;
        for (WordInfo item : mWordInfoList) {

            if (item.getId() == id) {
                temp = item;
                break;
            }
            temp = null;
        }
        return temp;

    }

    /**
     * 单个编辑字幕
     *
     * @param w info
     * @return
     */
    private SinglePointRotate initItemWord(WordInfo w) {

        int mcenterx = (int) (w.getCenterxy()[0] * mLayoutWidth);
        int mcentery = (int) (w.getCenterxy()[1] * mLayoutHeight);

        StyleInfo si = SpecialUtils.getInstance().getStyleInfo(w.getStyleId());


        String text = "";
        int color = Color.WHITE;
        if (null != si) {

            text = TextUtils.isEmpty(w.getText()) ? si.getHint() : w.getText();

            color = (w.getTextColor() == Color.WHITE ? si.getTextDefaultColor()
                    : w.getTextColor());
        }
        String bgpath = null;
        if (si.frameArray.size() > 0) {
            bgpath = si.frameArray.valueAt(0).pic;
        }

        SinglePointRotate tv = new SinglePointRotate(mLinearWords.getContext(),
                w.getRotateAngle(), text, color, w.getTtfLocalPath(),
                w.getDisf(), new Point(mLayoutWidth, mLayoutHeight), new Point(
                mcenterx, mcentery), w.getTextSize(),
                w.getShadowColor(), si, bgpath);

        tv.setOnClickListener(new SinglePointRotate.onClickListener() {

            @Override
            public void onClick(SinglePointRotate v) {
                if (mMenuLayout.getVisibility() != View.VISIBLE) {
                    if (mEditorHandler != null) {
                        mEditorHandler.pause();
                    }
                    int id = v.getId();
                    WordInfo info = getItem(id);
                    if (null != info) {
                        mSubLine.editSub(id);
                        mCurInfo = new WordInfo(info);
                        onStartSub(true);
                    }
                }
            }
        });

        Double fx = mLayoutWidth * w.getLeft();
        Double fy = mLayoutHeight * w.getTop();
        int mleft = fx.intValue();
        int mtop = fy.intValue();
        tv.setId(w.getId());
        tv.layout(mleft, mtop, mleft + tv.getWidth(), tv.getHeight() + mtop);

        return tv;

    }

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            onVisibile(checkedId);
        }
    };

    private SpecialStyleAdapter mSpAapter = null;
    private Runnable mGetSpecialRunnable = new Runnable() {

        @Override
        public void run() {
            mSpAapter.addStyles(SpecialUtils.getInstance().getStyleDownloaded());
            if (null != mHandler) {
                mHandler.removeMessages(MSG_LISTVIEW);
                mHandler.sendEmptyMessageDelayed(MSG_LISTVIEW, 200);
            }

            String content = null;
            if (CoreUtils.checkNetworkInfo(mContext.getApplicationContext()) != CoreUtils.UNCONNECTED) {
                content = SpecialUtils.getInstance().getSpecialJson();
            }

            mSubtitleStyle.post(new Runnable() {

                @Override
                public void run() {
                    SysAlertDialog.cancelLoadingDialog();
                }
            });

            if (!TextUtils.isEmpty(content)) {
                JSONObjectEx jex;
                try {
                    jex = new JSONObjectEx(content);
                    if (null != jex && jex.getInt("code") == 200) {
                        ArrayList<StyleInfo> dbList = SpecialData.getInstance()
                                .getAll();

                        StyleInfo temp = null;
                        JSONObject jobj = null;
                        JSONArray jarr = jex.getJSONArray("data");
                        final JSONObject jicon = jex.getJSONObject("icon");
                        String timeunix = jicon.getString("timeunix");
                        if (!AppConfiguration
                                .checkSpecialIconIsLasted(timeunix)) {
                            downloadData(timeunix, jicon);
                        }

                        int len = jarr.length();
                        SpecialUtils.getInstance().clearArray();
                        for (int i = 0; i < len; i++) {
                            jobj = jarr.getJSONObject(i);
                            temp = new StyleInfo();
                            temp.code = jobj.optString("name");
                            temp.caption = jobj.optString("caption");
                            temp.pid = temp.code.hashCode();
                            temp.nTime = jobj.getLong("timeunix");
                            temp.st = com.rd.veuisdk.utils.CommonStyleUtils.STYPE.special;
                            temp.index = i;
                            StyleInfo dbTemp = checkExit(dbList, temp);
                            if (null != dbTemp) {
                                if (SpecialData.getInstance().checkDelete(temp,
                                        dbTemp)) {
                                    temp.isdownloaded = false;
                                } else {
                                    temp.isdownloaded = dbTemp.isdownloaded;
                                    if (temp.isdownloaded) {
                                        temp.mlocalpath = dbTemp.mlocalpath;
                                        File idfile = new File(temp.mlocalpath);
                                        CommonStyleUtils.checkStyle(idfile,
                                                temp);
                                        // temp.icon = dbTemp.icon;

                                    }
                                }
                            }
                            SpecialUtils.getInstance().putStyleInfo(temp);
                        }

                        ArrayList<StyleInfo> newList = SpecialUtils
                                .getInstance().getStyleInfos();
                        SpecialData.getInstance().replaceAll(newList);

                        if (null != dbList) {
                            dbList.clear();
                        }
                        mSpAapter.addStyles(newList);
                        if (null != mHandler) {
                            mHandler.removeMessages(MSG_LISTVIEW);
                            mHandler.sendEmptyMessageDelayed(MSG_LISTVIEW, 200);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }

        }
    };

    //特效图标
    private void downloadData(final String timeUnix, final JSONObject jicon) {
        DownLoadUtils utils = new DownLoadUtils(jicon.optString("name")
                .hashCode(), jicon.optString("caption"), "zip");
        utils.DownFile(new IDownFileListener() {

            @Override
            public void onProgress(long arg0, int arg1) {

            }

            @Override
            public void Canceled(long arg0) {

            }

            @Override
            public void Finished(long mid, String localPath) {
                File fold = new File(localPath);
                File zip = new File(fold.getParent() + "/"
                        + jicon.optString("name") + ".zip");
                fold.renameTo(zip);
                if (zip.exists()) { // 解压
                    try {
                        FileUtils.deleteAll(new File(PathUtils
                                .getRdSpecialPath(), "icon"));
                        String dirpath = FileUtils.unzip(zip.getAbsolutePath(),
                                PathUtils.getRdSpecialPath());
                        if (!TextUtils.isEmpty(dirpath)) {
                            String[] icons = new File(dirpath).list(new FilenameFilter() {

                                @Override
                                public boolean accept(File dir,
                                                      String filename) {
                                    return filename.endsWith(".png");
                                }
                            });
                            AppConfiguration
                                    .setSpecialIconVersion(timeUnix, dirpath,
                                            (null != icons) ? icons.length : 0);

                        }
                        zip.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.obtainMessage(MSG_ICON).sendToTarget();

            }
        });
    }

    private final int MSG_ICON = 568, MSG_LISTVIEW = 45745;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_ICON:
                    mSpAapter.notifyDataSetChanged();
                    break;
                case MSG_LISTVIEW:
                    mSpAapter.setListview(mSubtitleStyle);
                    break;

                default:
                    break;
            }
        }

        ;
    };

    private StyleInfo checkExit(ArrayList<StyleInfo> dbList, StyleInfo temp) {
        int dblen = dbList.size();
        StyleInfo db = null;
        for (int j = 0; j < dblen; j++) {
            StyleInfo dbTemp = dbList.get(j);
            if (dbTemp.caption.equals(temp.caption)) {
                db = dbTemp;
                break;
            }
        }
        return db;

    }

    private void getSpecialData() {
        ThreadPoolUtils.execute(mGetSpecialRunnable);
    }

    /**
     * 控制menu部分的布局状态
     *
     * @param checkedId
     */
    private void onVisibile(int checkedId) {

        mMore.invalidate();
        mFunny.invalidate();
        mUnclassified.invalidate();
        if (checkedId == R.id.sp_style) {
            mSubtitleStyle.setVisibility(View.VISIBLE);
            mSizeTimeline.setVisibility(View.GONE);
        } else if (checkedId == R.id.sp_size) {
            mSizeTimeline.setVisibility(View.VISIBLE);
            mSubtitleStyle.setVisibility(View.GONE);
            onInitSizeTimeLine();
            if (null != mSprCurView) {
                mSizePicker.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        int scrollX = mSizePicker.setDisf(mSprCurView.getDisf());
                        mSizeTimeline.appScrollTo(scrollX, true);
                        double[] ds = mSizePicker.getInfo(scrollX);
                        setDisfText(ds[1]);
                    }
                }, 200);
            }
        }
    }

    /**
     * 初始化缩略图时间轴
     */
    private void onInitSizeTimeLine() {

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                mSizePicker.getNeedMinWidth(),
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(mSizeTimeline.getHalfParentWidth(), 0,
                mSizeTimeline.getHalfParentWidth(), 0);
        mSizePicker.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);
        mSizeLinearLayout.setLayoutParams(lframe);

    }

    /**
     * 播放控制器图标
     *
     * @param resId
     */
    void setImage(int resId) {
        if (isEditing()) {
            mPlayState.setImageResource(resId);
        }
    }

    /**
     * 返回
     *
     * @return
     */
    int onSubBackPressed() {
        if (mMenuLayout.getVisibility() == View.VISIBLE) {

            if (null != mSprCurView) {
                if (null == getOldWord(mSprCurView.getId())) {
                    mSubLine.removeById(mSprCurView.getId());
                } else {

                }
                mSprCurView.recycle();
                mSprCurView = null;
                mLinearWords.removeAllViews();
            }
            mCurInfo = null;

            onMenuViewOnBackpressed();

            if (null != mSpAapter) {
                mSpAapter.clearDownloading();
            }
            mAddStep = false;
            return -1;

        } else if (mRoot.getVisibility() == View.VISIBLE) {
            if (!CommonStyleUtils.isEquals(mWordInfoList, TempVideoParams.getInstance()
                    .getSpecailsDurationChecked())
                    || mIsUpdate) {
                onShowAlert();
                return 0;
            } else {
                onBackToActivity();
            }
            return 1;
        } else {
            mIsSubing = false;
            return 0;
        }

    }

    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.dialog_tips),
                mContext.getString(R.string.cancel_all_changed),
                mContext.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, mContext.getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onListReset(false);
                        for (int n = 0; n < mWordInfoList.size(); n++) {
                            mWordInfoList.get(n).set(mTempWordList.get(n));
                        }
                        onBackToActivity();
                        dialog.dismiss();
                        mSpListener.onBackPressed();
                    }
                }, false, null).show();
    }

    /**
     * 返回到主Activity(配音，配乐)界面
     */
    private void onBackToActivity() {
        mIsUpdate = false;
        if (mAddStep) {
            onWordEnd();
        }
        if (null != mSprCurView) {
            if (null == getOldWord(mSprCurView.getId())) {
                mSubLine.removeById(mSprCurView.getId());
            }
        }
        setProgressText(0);
        onScrollTo(0);
        mSubLine.clearCurrent();
        mLinearWords.removeAllViews();
        mAddLayout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.editor_preview_slide_out));
        mAddLayout.setVisibility(View.GONE);
        mRoot.setVisibility(View.GONE);
        mSubLine.recycle();
        mIsSubing = false;
        if (null != mSpAapter) {
            mSpAapter.onDestory();
        }

        mEditorHandler
                .unregisterEditorProgressListener(mEditorPreivewPositionListener);
        mSubLine.clearAll();
        bUIPrepared = false;
    }

    /**
     * 点击完成,返回到主editActivity
     */
    void onSure() {
        if (isEditing()) {
            onSaveToList(false);
            onBackToActivity();

            ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                    .getSpecails();
            boolean canreset = true;

            if (null != tempList) {
                int tsize = tempList.size();
                int msize = mWordInfoList.size();
                if (msize == tsize) {
                    for (int i = 0; i < msize; i++) {
                        if (!contains(mWordInfoList.get(i), tempList)) {
                            // Log.d(TAG, "contains-->" + mWordInfoList.get(i).getId());
                            canreset = true;
                            break;
                        } else {
                            canreset = false;
                        }

                    }

                } else {
                    canreset = true;
                }

            }
            if (canreset)
                TempVideoParams.getInstance().setSpecial(mWordInfoList);
        }

    }

    private boolean contains(WordInfo info, ArrayList<WordInfo> tempList) {
        boolean exit = false;
        int len = tempList.size();
        for (int i = 0; i < len; i++) {
            if (tempList.get(i).equals(info)) {
                exit = true;
                break;
            }
        }
        return exit;
    }


    /**
     * 点击menu_layout 中得保存(保存文本，颜色，字体等信息)，继续播放
     */
    private void onSaveBtnItem() {

        onSaveToList(false);

        onMenuViewOnBackpressed();
        if (mAddStep) {
            if (null != mSprCurView) {
                mLinearWords.removeView(mSprCurView);
                mSprCurView.recycle();
                mSprCurView = null;
            }
            mEditorHandler.seekTo((int) mCurInfo.getStart());
            playVideo();
            mIvAddSubtitle.setImageResource(R.drawable.moremusic_save);
            mTvAdd.setText(R.string.sure);
        }
        if (mSprCurView != null) { // 保存处于编辑状态的item
            mSprCurView.setControl(false);
        }
        // Log.d(TAG, "onSaveBtnItem->" + (mCurInfo == null));
    }

    /**
     * 点击上级界面（场景：正在编辑特效样式，(1点击保存按钮,2点击返回))
     */
    private void onMenuViewOnBackpressed() {
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
            onRebackMenuToAddSpecial();
            mViewTouchListener.onActionUp();
        }
        mSpListener.onViewVisible(true);

    }

    private void onSaveToList(boolean clearCurrent) {
        if (mSprCurView != null) {
            saveInfo(clearCurrent);
        }
        if (clearCurrent)
            mSubLine.clearCurrent();
    }

    /**
     * 是否处于编辑状态
     *
     * @return
     */
    public boolean isEditing() {
        return mIsSubing;
    }

    /**
     * 播放中的进度
     *
     * @param progress
     */
    private void onScrollProgress(int progress) {
//        android.util.Log.e(TAG, "onScrollProgress: " + progress);
        if (null != mCurInfo && mAddStep) {
            mCurInfo.setEnd(progress);
            mSubLine.update(mCurInfo.getId(), (int) mCurInfo.getStart(),
                    progress);
        }
        initWords(progress);
        onScrollTo(getScrollX(progress));
        setProgressText(progress);

    }

    private int getScrollX(long progress) {
        return (int) (progress * (mSubLine.getThumbWidth() / mDuration));
    }

    /**
     * 播放完成
     */
    private void onScrollCompleted() {

        setProgressText(mDuration);
        onScrollTo((int) mSubLine.getThumbWidth());
        mPlayState.setImageResource(R.drawable.edit_music_play);

    }

    /**
     * 设置播放进度
     *
     * @param mScrollX
     */
    private void onScrollTo(int mScrollX) {

        mScrollView.appScrollTo(mScrollX, true);
    }

    public interface ISpecailListener {
        void onViewVisible(boolean mIsVisible);

        void onBackPressed();
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context mContext, Intent intent) {
            int position = intent.getIntExtra(
                    SpecialStyleAdapter.DOWNLOADED_ITEM_POSITION, -1);
            if (-1 != position && mMenuLayout.getVisibility() == View.VISIBLE) {

                if (null != mCurInfo) {
                    StyleInfo info = mSpAapter.getItem(position);
                    if (null != info) {
                        mCurInfo.setStyleId(info.pid);
                    }
                    initsp();
                    onStyleItem(position);
                }
                if (null != mSpAapter) {
                    mSpAapter.notifyDataSetChanged();
                }
            }

        }

    };

    private void setDisf(int scrollX) {
        if (null != mSprCurView) {
            double[] ds = mSizePicker.getInfo(scrollX);
            mSprCurView.setDisf((float) ds[0]);
            setDisfText(ds[1]);
        }
    }

    private void setDisfText(double text) {
        mSizeTimeline.setText(String.format("%.2f", text));
    }

    private ScrollViewListener mSizeOnScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY,
                                     boolean appScroll) {
            if (!appScroll) {
                setDisf(scrollX);
            }
        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY,
                                boolean appScroll) {
            if (!appScroll) {
                setDisf(scrollX);
            }
        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY,
                                  boolean appScroll) {
            if (!appScroll) {
                setDisf(scrollX);
            }
        }
    };

    public void setIsLoading(boolean isloading) {
        if (isEditing()) {
            mIsDownloading = isloading;
            if (!isloading) {
                SysAlertDialog.cancelLoadingDialog();
                // 防止下载完成设置adapter.setcheck(int p)无法notifi
                // mSpAapter.notifyDataSetChanged();
            }
        }
    }

}
