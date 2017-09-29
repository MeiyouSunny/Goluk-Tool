package com.rd.veuisdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.cache.HttpImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.InputUtls;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.net.JSONObjectEx;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.SubtitleObject;
import com.rd.veuisdk.IVideoEditorHandler.EditorPreivewPositionListener;
import com.rd.veuisdk.TTFHandler.ITTFHandlerListener;
import com.rd.veuisdk.adapter.SpecialStyleAdapter;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.export.IExportSub;
import com.rd.veuisdk.export.SubExportUtils;
import com.rd.veuisdk.model.FilterInfo2;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.ColorPicker;
import com.rd.veuisdk.ui.ColorPicker.IColorListener;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.PopViewUtil;
import com.rd.veuisdk.ui.SinglePointRotate;
import com.rd.veuisdk.ui.SizePicker;
import com.rd.veuisdk.ui.SquareView;
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
 * 添加字幕用到的handler
 *
 * @author JIAN
 */
class SubtitleHandler {
    @SuppressWarnings("unused")
    private final String TAG = "SubtitleHandler";
    private View mRoot;
    private View mAddLayout, mMenuLayout;
    private IVideoEditorHandler mEditorHandler;
    private Context mContext;
    private EditText mEtSubtitle;
    private RadioGroup mRgMenu, mRgShadow;
    private ImageView mPlayState;
    private TimelineHorizontalScrollView mScrollView;
    private LinearLayout mIMediaLinearLayout, mISizeLinearLayout;
    private ThumbNailLine mSubtitleLine;
    private ArrayList<WordInfo> mWordInfoList = new ArrayList<WordInfo>(),
            mTempWordList = new ArrayList<WordInfo>();
    private FrameLayout mLinearWords;
    private GridView mGvSubtitleStyle;
    private GridView mGvTTF;
    private HttpImageFetcher mFetcher;
    private ColorPicker mColorPicker;
    private SquareView mSvNone, mSvWhite, mSvBlack;
    private TimeLine mSizeTimeline;
    private SizePicker mSizePicker;
    private View mStyleLayout, mTTFLayout, mColorLayout, mSizeLayout;
    private View mViewWordHint;
    private Button mDelete;
    private ImageView mBtnAddSubtitle;
    private TextView mTvAddSubtitle;
    private TextView mTvProgress;
    private WordInfo mCurrentInfo;
    private ImageView mIvClear;
    private boolean mIsUpdate = false;
    private int mLayoutWidth = 1024, mLayoutHeight = 1024;

    private ISubHandler mISubtitle;
    private TextView mTvSave;
    private int mStateSize = 0;

    /**
     * 终极集合（点击完成时 或者提示是否保存时） 保存到该集合
     */
    public interface ISubHandler {
        void setTitle(int strResId);

        void onBackPressed();

        void onViewVisible(boolean mIsVisible);
    }


    public void initView(View view) {
        mRoot = view;
        mRoot.requestLayout();
        mGvSubtitleStyle = (GridView) mRoot.findViewById(R.id.style_sub);
        mColorPicker = (ColorPicker) mRoot.findViewById(R.id.subtitle_picker);
        mGvTTF = (GridView) mRoot.findViewById(R.id.gridview_ttf);

        mBtnAddSubtitle = (ImageView) mRoot.findViewById(R.id.add_subtitle);
        mTvAddSubtitle = (TextView) mRoot.findViewById(R.id.tv_add_subtitle);
        mDelete = (Button) mRoot.findViewById(R.id.subtitle_del_item);
        mTvProgress = (TextView) mRoot.findViewById(R.id.tvAddProgress);


        ImageCacheParams cacheParams = new ImageCacheParams(view.getContext(),
                null);
        cacheParams.compressFormat = CompressFormat.PNG;
        // 缓冲占用系统内存的10%
        cacheParams.setMemCacheSizePercent(0.1f);
        mFetcher = new HttpImageFetcher(view.getContext(), 252, 222);
        mFetcher.addImageCache(view.getContext(), cacheParams);

        mContext = mRoot.getContext();
        mAddLayout = mRoot.findViewById(R.id.subtitle_add_layout);
        mMenuLayout = mRoot.findViewById(R.id.subtitle_menu_layout);

        mBtnAddSubtitle.setOnClickListener(onAddListener);
        mPlayState = (ImageView) mRoot.findViewById(R.id.ivPlayerState);
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        mStyleLayout = mRoot.findViewById(R.id.subtitle_style_layout);
        mTTFLayout = mRoot.findViewById(R.id.subtitle_ttf_layout);
        mColorLayout = mRoot.findViewById(R.id.subtitle_color_layout);
        mSizeLayout = mRoot.findViewById(R.id.subtitle_size_layout);
        mIvClear = (ImageView) mRoot.findViewById(R.id.ivClear);

        mTvSave = (TextView) mRoot.findViewById(R.id.subtitle_save);
        mTvSave.setOnClickListener(onSaveChangeListener);
        mEtSubtitle = (EditText) mRoot.findViewById(R.id.subtitle_et);
        mRgMenu = (RadioGroup) mRoot.findViewById(R.id.subtitle_menu_group);

        mRgMenu.setOnCheckedChangeListener(onCheckedChangeListener);

        mScrollView = (TimelineHorizontalScrollView) mRoot
                .findViewById(R.id.priview_subtitle_line);
        mScrollView.enableUserScrolling(true);

        mIMediaLinearLayout = (LinearLayout) mRoot
                .findViewById(R.id.subtitleline_media);
        mSubtitleLine = (ThumbNailLine) mRoot.findViewById(R.id.subline_view);

        mColorPicker.setColorListener(mColorPickListener);

        mRgShadow = (RadioGroup) mRoot.findViewById(R.id.shadow_group);
        mRgShadow.setOnCheckedChangeListener(onShadowChangListener);
        mSvNone = (SquareView) mRoot
                .findViewById(R.id.subtitle_stroke_none);

        mSvWhite = (SquareView) mRoot
                .findViewById(R.id.subtitle_stroke_white);

        mSvBlack = (SquareView) mRoot
                .findViewById(R.id.subtitle_stroke_black);

        mSvBlack.setOnClickListener(mRadioBtnClickListener);
        mSvWhite.setOnClickListener(mRadioBtnClickListener);
        mSvNone.setOnClickListener(mRadioBtnClickListener);

        mIvClear.setOnClickListener(mClearSubtitle);

        mSizePicker = (SizePicker) mRoot.findViewById(R.id.subtitle_sizepicker);
        mSizeTimeline = (TimeLine) mRoot.findViewById(R.id.timeline_size);
        mISizeLinearLayout = (LinearLayout) mRoot
                .findViewById(R.id.subtitle_size_linearlayout);

        mSizeTimeline.addScrollListener(mSizeOnScrollListener);
        mScrollView.addScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(mViewTouchListener);

        mGvSubtitleStyle.setOnItemClickListener(mStyleItemlistener);

        mSubtitleLine.setSubtitleThumbNailListener(mSubtitleListener);
        mViewWordHint = mRoot.findViewById(R.id.word_hint_view);

        mDelete.setOnClickListener(mOnDeleteListener);
        controlKeyboardLayout(mTreeView, mRoot.findViewById(R.id.thelocation));
        mDisplay = CoreUtils.getMetrics();
    }

    private View mTreeView;

    public SubtitleHandler(View _treeview, ISubHandler mISubtitle,
                           IVideoEditorHandler hlrEditor, FrameLayout sublayout) {
        this.mISubtitle = mISubtitle;
        this.mTreeView = _treeview;
        mEditorHandler = hlrEditor;
        mLinearWords = sublayout;
    }

    private View.OnClickListener mRadioBtnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onShadowVisibile(v.getId());
        }
    };

    private ITTFHandlerListener mTTFListener = new ITTFHandlerListener() {

        @Override
        public void onItemClick(String ttf, int position) {
            if (null != mSprCurView) {
                if (ttf.equals(mContext.getString(R.string.default_ttf))) {
                    mSprCurView.setDefualtTtf(true);
                    mTTFHandler.ToReset();
                } else {
                    mSprCurView.setTTFLocal(ttf);
                }
            }
        }
    };

    private OnClickListener mOnDeleteListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int nProgress = mScrollView.getProgress();
            mCurrentInfo = checkWord(nProgress);

            if (null != mCurrentInfo) {
                onDelWordItem(
                        (SinglePointRotate) mLinearWords.findViewById(mCurrentInfo
                                .getId()), mCurrentInfo.getId());
                mCurrentInfo = null;
            }
            checkVisible(nProgress);
        }
    };

    private IViewTouchListener mViewTouchListener = new IViewTouchListener() {

        @Override
        public void onActionDown() {
            if (isEditing()) {
                mEditorHandler.pause();
                if (mAddStep) {
                    onWordEnd();
                } else {
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
        onAdapterResume();
    }

    private void onAdapterResume() {
        if (null != mAnimAdapter) {
            mAnimAdapter.onResume();
        }

    }

    void onPasue() {
        if (null != mAnimAdapter) {
            mAnimAdapter.onPasue();
        }

    }

    void onDestory() {
        bUIPrepared = false;
        mHandler.removeCallbacks(resetSubDataRunnable);
        if (mSubtitleLine != null) {
            mSubtitleLine.recycle(true);
        }
        if (null != mTreeView) {
            mTreeView.getViewTreeObserver().addOnGlobalLayoutListener(null);
        }
        mHandler.removeMessages(MSG_LISTVIEW);
        mHandler = null;
    }

    private OnItemClickListener mStyleItemlistener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            onStyleItem(position, mCurrentInfo);

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

    private void onStyleItem(int position, WordInfo winfo) {
        StyleInfo info = mAnimAdapter.getItem(position);
        if (null == info) {
            return;
        }
//        Log.e(this.toString() + "onStyleItem...." + position,
//                info.isdownloaded
//                        + "cccc" + winfo.getDisf() + "........." + info.disf
//                        + "...styleId" + info.pid);
        if (info.isdownloaded) {
            if (null == winfo) {
                Log.e(TAG, "onStyleItem:  winfo is null");
                return;
            }
            if (winfo.getDisf() == 1) {
                float mdisf = getCustomSize(22);
                winfo.setDisf(mdisf);
            }
            winfo.setStyleId(info.pid);
            mAnimAdapter.setCheckItem(position);
            if (mSprCurView != null) {
                int index = getIndex(winfo.getId());
                if (index >= 0) {
                    mSprCurView.setRotate(winfo.getRotateAngle());
                } else {
                    mSprCurView.setRotate(info.rotateAngle);
                }


                if (info.lashen != 1) {
                    int x = (int) (mLayoutWidth * info.centerxy[0]);
                    int y = (int) (mLayoutHeight * info.centerxy[1]);
                    mSprCurView.setCenter(new Point(x, y));
                    mSprCurView.setFirstIn(false);
                } else {
                    mSprCurView.setFirstIn(true);
                }

                if (info.frameArry.size() > 0) {
                    mSprCurView.setStyleInfo(true, info,
                            (int) (winfo.getEnd() - winfo.getStart()), true,
                            winfo.getDisf());
                }

                if (info.type == 0) {

                    if (!TextUtils.isEmpty(winfo.getText())) {
                        mEtSubtitle.setText(winfo.getText());
                    } else {
                        if (null != info.getFilterInfo2()) {
                            mEtSubtitle.setHint(R.string.sub_hint);
                            mSprCurView.setInputText(info.getFilterInfo2()
                                    .getHint());
                        }
                    }
                    mSprCurView.setImageStyle(info);
                } else {
                    mSprCurView.setInputText(winfo.getText());
                }

            }
        } else {
            // 执行下载
            int visiblep = position % Math.max(1, mGvSubtitleStyle.getChildCount());
            View child = mGvSubtitleStyle.getChildAt(visiblep);
            if (null != child) {
                mAnimAdapter.onDown(position, (ImageView) child
                                .findViewById(R.id.ttf_state),
                        (CircleProgressBarView) child
                                .findViewById(R.id.ttf_pbar));
            }

        }

    }

    private void setShadowColor(int color) {
        if (null != mSprCurView) {
            mSprCurView.setShadowColor(color);
        }

    }

    private IThumbLineListener mSubtitleListener = new IThumbLineListener() {

        @Override
        public void onTouchUp() {
            mViewTouchListener.onActionUp();
            // checkVisible(mEditorHandler.getCurrentPosition());
        }

        ;

        @Override
        public void updateThumb(int id, int start, int end) {
            // Log.e("robeein","id: "+id +" start: "+ start+" end: "+end);
            mIsUpdate = true;
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }

            int index = getIndex(id);
            // Log.e("robeein: ",index+" listcount: "+mWordInfoList.size());
            if (index >= 0) {
                WordInfo info = mWordInfoList.get(index);
                if (null != info && id == info.getId()) {
                    // Log.e("robeein","info: "+info.toString());
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
            // Log.e("onCheckItem", changed + ".,,,,,,,,,,id," + id
            // + "..mWordInfoList.size().." + mWordInfoList.size());
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }
            if (AppConfiguration.isFirstShowDragSub()) {

                int[] pxs = mSubtitleLine.getCurrentPx(id);
                if (null != pxs) {
                    PopViewUtil.showPopupWindowStyle(mSubtitleLine, true, true, 120,
                            true, (pxs[0] + pxs[1]) / 2,
                            new PopViewUtil.CallBack() {

                                @Override
                                public void onClick() {
                                    AppConfiguration.setIsFirstDragSub();
                                }
                            }, R.string.drag_for_sub, 0.5);
                }
            } else {
                int index = getIndex(id);
                if (index >= 0 && index < mWordInfoList.size() - 1) {
                    WordInfo info = mWordInfoList.get(index);
                    mCurrentInfo = new WordInfo(info);
                    checkVisible(mCurrentInfo);
                    if (changed) {
                        mLinearWords.removeAllViews();
                        if (null != mSprCurView) {
                            mSprCurView.recycle();
                            mSprCurView = null;
                        }
                        initItemSub();
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

    /**
     * 颜色回调
     */
    private IColorListener mColorPickListener = new IColorListener() {

        @Override
        public void getColor(int color, int position) {
            if (null != mSprCurView) {
                mSprCurView.setInputTextColor(color);
            }
        }
    };

    /**
     * 字体handler
     */
    private TTFHandler mTTFHandler;

    private DisplayMetrics mDisplay;
    private int mDuration;

    /**
     * 点击取消保存和重新进入时重置List
     */
    private void onListReset(boolean isInit) {
        mWordInfoList.clear();
        if (isInit) {
            mTempWordList.clear();
        }
        ArrayList<WordInfo> tempList = TempVideoParams.getInstance()
                .getSubsDuraionChecked();
        mWordInfoList.clear();
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

    public void init(FrameLayout sublayout) {
        mIsSubing = true;
        mLinearWords = sublayout;
        mDuration = mEditorHandler.getDuration();
        mAddStep = false;
        mLayoutWidth = mLinearWords.getWidth();
        mLayoutHeight = mLinearWords.getHeight();
        mViewWordHint.setVisibility(View.VISIBLE);
        mRoot.setVisibility(View.VISIBLE);

        mMenuLayout.setVisibility(View.GONE);
        mAddLayout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.editor_preview_slide_in));
        mAddLayout.setVisibility(View.VISIBLE);

        SubUtils.getInstance().recycle();
        onListReset(true);

        onInitThumbTimeLine();
        setImage(R.drawable.edit_music_play);
        mTvAddSubtitle.setText(R.string.add_subtitle);
        mAnimAdapter = new SpecialStyleAdapter(mContext, true);
        mGvSubtitleStyle.setAdapter(mAnimAdapter);

        if (null != mEditorHandler) { // 清除另一种方式下的字幕
            mEditorHandler
                    .registerEditorPostionListener(mEditorPreivewPositionListener);
            mEditorHandler.reload(false);
            mEditorHandler.seekTo(0);
        }
        mSubtitleLine.setCantouch(true);
        mSubtitleLine.setMoveItem(true);
        mPlayState.setOnClickListener(onStateChangeListener);
        mMenuLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        mBtnAddSubtitle.setImageResource(R.drawable.add_subtitle_btn);
        getData(false);
        mTTFHandler = new TTFHandler(mGvTTF, mTTFListener);
        onResume();
    }

    private OnGlobalLayoutListener onGlobalLayoutListener = new OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            mMenuLayout.getWindowVisibleDisplayFrame(rect);
            int visible = mMenuLayout.getVisibility();
            if (visible == View.VISIBLE) {
                // 已经打开，开始记录
                if (rect.bottom == mDisplay.heightPixels) {// 已经关闭输入法，设置背景图片为键盘
                    // input.setImageResource(R.drawable.inputmanager);
                } else {
                    // input.setImageResource(R.drawable.subtitle_style);
                }
            } else {
                // input.setImageResource(R.drawable.inputmanager);
            }
        }
    };

    private EditorPreivewPositionListener mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

        @Override
        public void onEditorPreviewComplete() {
            onScrollCompleted();
        }

        @Override
        public void onEditorPrepred() {
            if (null != mEditorHandler) { // 清除另一种方式下的字幕,取消loading...
                mEditorHandler.cancelLoading();
            }
            onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
            if (mAddStep) {
                if (mCurrentInfo.getStart() < nPosition) {
                    if (!mSubtitleLine.canAddSub(nPosition, mDuration, mSizeParams[0],
                            -1, -1)) {
                        onWordEnd(nPosition);
                    }
                }
            }
        }
    };

    private int[] mSizeParams;

    private int mHalfWidth = 0;

    private void onInitThumbTimeLine() {

        mHalfWidth = mDisplay.widthPixels / 2;
        mScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        mSizeParams = mSubtitleLine
                .setDuration(mDuration, mScrollView.getHalfParentWidth());
        mScrollView.setLineWidth(mSizeParams[0]);
        mScrollView.setDuration(mDuration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mSizeParams[0]
                + 2 * mSubtitleLine.getpadding(), mSizeParams[1]);

        lp.setMargins(mScrollView.getHalfParentWidth() - mSubtitleLine.getpadding(),
                0, mHalfWidth - mSubtitleLine.getpadding(), 0);

        mSubtitleLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);

        mIMediaLinearLayout.setLayoutParams(lframe);
        mViewWordHint.setVisibility(View.GONE);

    }

    //UI数据恢复成功
    private boolean bUIPrepared = false;

    /**
     * 初始化缩略图时间轴和恢复字幕数据
     *
     * @param virtualVideo
     */
    private void onInitThumbTimeLine(VirtualVideo virtualVideo) {
        mSubtitleLine.setVirtualVideo(virtualVideo);
        mSubtitleLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        onScrollProgress(0);
        mHandler.postDelayed(resetSubDataRunnable, 100);
    }

    //恢复数据
    private Runnable resetSubDataRunnable = new Runnable() {

        @Override
        public void run() {
            ArrayList<SubInfo> sublist = new ArrayList<SubInfo>();
            int len = mWordInfoList.size();
            SubInfo info;
            WordInfo item;
            for (int i = 0; i < len; i++) {
                item = mWordInfoList.get(i);
                info = new SubInfo(item);
                info.setStr(item.getText());
                sublist.add(info);
            }
            mSubtitleLine.prepareData(sublist);
            checkVisible(0);
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            if (AppConfiguration.isFirstShowInsertSub()) {
                PopViewUtil.showPopupWindowStyle(mSubtitleLine, true, true, 120,
                        true, mSubtitleLine.getpadding(),
                        new PopViewUtil.CallBack() {

                            @Override
                            public void onClick() {
                                AppConfiguration.setIsFirstInsertSub();
                            }
                        }, R.string.drag_thumb_for_insert_sub, 0.5);
            }

            bUIPrepared = true;
        }
    };

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
        mISizeLinearLayout.setLayoutParams(lframe);

    }

    private void setDisf(int scrollX) {
        double[] ds = mSizePicker.getInfo(scrollX);
        if (null != mSprCurView) {
            mSprCurView.setDisf((float) ds[0]);
            setDisfText(ds[1]);
        }
        if (null != mCurrentInfo) {
            mCurrentInfo.setDisf((float) ds[0]);
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

    private void setProgressText(int progress) {
        mTvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
    }

    private ScrollViewListener mThumbOnScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY,
                                     boolean appScroll) {

            // Log.d(TAG, "onScrollProgress..." + scrollX + "...." + appScroll);
            if (isEditing()) {
                mSubtitleLine.setStartThumb(mScrollView.getScrollX());
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
            // Log.d(TAG, "onScrollEnd..." + scrollX + "...." + appScroll);
            if (isEditing()) {
                int nprogress = mScrollView.getProgress();
                mSubtitleLine.setStartThumb(mScrollView.getScrollX());
                if (!appScroll) {
                    if (mEditorHandler != null) {
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
            // Log.d(TAG, "onScrollBegin..." + scrollX + "...." + appScroll);
            if (isEditing()) {
                int nprogress = mScrollView.getProgress();
                mSubtitleLine.setStartThumb(mScrollView.getScrollX());
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

    private boolean mAddStep = false;// 新增true，编辑false

    private WordInfo checkVisible(int nprogress) {

        WordInfo current = checkWord(nprogress);
        checkVisible(current);
        return current;
    }

    private void checkVisible(WordInfo current) {
        if (!mAddStep) {
            if (null != current) {
                mBtnAddSubtitle.setImageResource(R.drawable.subtitle_edit);
                mDelete.setVisibility(View.VISIBLE);
                mTvAddSubtitle.setText(R.string.edit_subtitle);
                mTvSave.setText(R.string.complete);
                mSubtitleLine.showCurrent(current.getId());
            } else {
                mTvAddSubtitle.setText(R.string.add_subtitle);
                mBtnAddSubtitle.setImageResource(R.drawable.add_subtitle_btn);
                mDelete.setVisibility(View.GONE);
                mTvSave.setText(R.string.add);
                mSubtitleLine.setShowCurrentFalse();
            }
        }
    }

    private void onToast(String msg) {
        Utils.autoToastNomal(mContext, msg);
    }

    private void onToast(int msg) {
        Utils.autoToastNomal(mContext, msg);
    }

    /**
     * 正在编辑字幕...
     */
    private boolean mIsSubing = false;

    /**
     * 开始添加字幕
     */
    private OnClickListener onAddListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (!bUIPrepared) {
                android.util.Log.e(TAG, "onAddListener: recovering sub data ...");
                return;
            }
            if (mEditorHandler != null && mEditorHandler.isPlaying()) {
                pauseVideo();
            }
            if (mSprCurView != null) { // 保存处于编辑状态的item
                mSubtitleLine.clearCurrent();
                mSprCurView.recycle();
                mSprCurView = null;
            }
            // Log.e("onAddListener--", this.toString());
            if (mAnimAdapter.getCount() == 0) {
                int re = CoreUtils.checkNetworkInfo(v.getContext()
                        .getApplicationContext());
                if (re == CoreUtils.UNCONNECTED) {
                    com.rd.veuisdk.utils.Utils.autoToastNomal(v.getContext(),
                            R.string.please_check_network);
                } else

                {
                    SysAlertDialog.showLoadingDialog(mContext,
                            mContext.getString(R.string.isloading));
                }
                getData(true);

            } else {
                String menu = mTvAddSubtitle.getText().toString();
                /**
                 * 新增字幕
                 */
                if (menu.equals(mContext.getString(R.string.add_subtitle))) {

                    // 判断该区域能否添加

                    int progress = mScrollView.getProgress();

                    int header = TempVideoParams.getInstance().getThemeHeader();
                    if (progress < header) {
                        onToast(R.string.addsub_video_head_failed);
                        return;
                    }
                    int last = TempVideoParams.getInstance().getThemeLast();
                    if (progress > mDuration - last) {
                        onToast(R.string.addsub_video_end_failed);
                        return;
                    }


                    if (!mSubtitleLine.canAddSub(progress, mDuration, mSizeParams[0],
                            header, last)) {
                        onToast(R.string.addsub_video_between_failed);
                        return;
                    }

                    mISubtitle.onViewVisible(false);
                    mAddStep = true;
                    mCurrentInfo = new WordInfo();
                    mCurrentInfo.setStart(progress);
                    mCurrentInfo.setId(Utils.getWordId());

                    int end = progress;
                    mCurrentInfo.setEnd(end);
                    mSubtitleLine.addRect(progress, end, "", mCurrentInfo.getId());

                    mTvAddSubtitle.setText(R.string.sure);
                    mEtSubtitle.setText("");
                    onStartSub(mAnimAdapter.getItem(0).isdownloaded);
                    v.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            onStyleItem(0, mCurrentInfo);
                        }
                    }, 100);

                } else if (menu.equals(mContext.getString(R.string.sure))) {
                    onWordEnd();
                } else { // 编辑当前字幕
                    mAddStep = false;
                    WordInfo temp = checkVisible(mScrollView.getProgress());
                    if (null != temp) {
                        mCurrentInfo = new WordInfo(temp);
                        if (null != mCurrentInfo) {
                            onStyleItem(mAnimAdapter.getPosition(mCurrentInfo
                                    .getStyleId()), mCurrentInfo);
                            onStartSub(true);

                        }
                    }

                }
            }

        }
    };
    private static final int OFFSET_END_POSTION = 0;

    /**
     * 结束点
     */
    private void onWordEnd() {
        onWordEnd(-1);
    }

    private void onWordEnd(long nPos) {
        if (null == mCurrentInfo) {
            return;
        }
        // 保存
        mAddStep = false;
        mSubtitleLine.setIsAdding(mAddStep);
        pauseVideo();
        mCurrentInfo.setEnd(nPos < 0 ? mEditorHandler.getCurrentPosition() : nPos);
        onSaveToList(false);

        int end = (int) mCurrentInfo.getEnd();
        mSubtitleLine.replace(mCurrentInfo.getId(), (int) mCurrentInfo.getStart(), end);
        if (null != mSprCurView) {
            boolean hasExit = checkExit(mSprCurView.getId());
            if (hasExit) {
                mSubtitleLine.replace(mCurrentInfo.getId(), mCurrentInfo.getText());
            } else {
                mSubtitleLine.removeById(mSprCurView.getId());
            }
            mSprCurView.recycle();
            mSprCurView = null;
        }
        end = end + OFFSET_END_POSTION;// 向后偏移
        if (end >= mEditorHandler.getDuration()) {
            end = mEditorHandler.getDuration() - 20;
        }
        mEditorHandler.seekTo(end);
        mCurrentInfo = null;
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
                mSubtitleLine.clearCurrent();
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

    private OnClickListener onInputListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onInputManager();
        }
    };

    /**
     * 点击输入法开关
     */
    private void onInputManager() {
        InputMethodManager input = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        input.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        mEtSubtitle.postDelayed(new Runnable() {

            @Override
            public void run() {
                mEtSubtitle.selectAll();
                mEtSubtitle.setSelectAllOnFocus(true);
            }
        }, 150);

    }

    private void hideInput() {
        InputMethodManager input = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // input.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        input.hideSoftInputFromWindow(mEtSubtitle.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private SinglePointRotate mSprCurView;
    private OnClickListener onSaveChangeListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mIsDownloading) {
                SysAlertDialog.showAutoHideDialog(mContext, "",
                        mContext.getString(R.string.downloading),
                        Toast.LENGTH_SHORT);
            } else {
                onSaveBtnItem();
            }
        }
    };

    /**
     * 检测该id之前是否存在于字幕列表
     *
     * @param id
     * @return
     */
    private boolean checkExit(int id) {
        WordInfo temp;
        boolean hasExit = false;
        for (int i = 0; i < mWordInfoList.size(); i++) {
            temp = mWordInfoList.get(i);
            if (temp.getId() == id) {
                hasExit = true;
                break;
            }
        }
        return hasExit;
    }

    /**
     * 点击menu_layout 中得保存(保存文本，颜色，字体等信息)，继续播放
     */
    private void onSaveBtnItem() {
        mISubtitle.onViewVisible(true);
        mIsSetWatcher = false;
        if (!TextUtils.isEmpty(mEtSubtitle.getText())) {
            mCurrentInfo.setText(mEtSubtitle.getText().toString());
        } else {
            if (null != mSprCurView)
                mCurrentInfo.setText(mSprCurView.getText().toString());
        }

        mSubtitleLine.replace(mCurrentInfo.getId(), mCurrentInfo.getText());
        onSaveToList(false);

        onMenuViewOnBackpressed();

        if (mAddStep) {
            if (null != mSprCurView) {
                mLinearWords.removeView(mSprCurView);
                mSprCurView.recycle();
                mSprCurView = null;
            }
            mEditorHandler.seekTo((int) mCurrentInfo.getStart());
            playVideo();
            mBtnAddSubtitle.setImageResource(R.drawable.moremusic_save);
            mTvAddSubtitle.setText(R.string.sure);
        }
        if (mSprCurView != null) {
            mSprCurView.recycle();
            mSprCurView = null;
        }
        mViewTouchListener.onActionUp();
    }

    private void saveInfo(boolean needStart) {

        if (null != mCurrentInfo) {

            int[] mrect = mSubtitleLine.getCurrent(mCurrentInfo.getId());
            if (null != mrect) {
                mCurrentInfo.setStart(mrect[0]);
                mCurrentInfo.setEnd(mrect[1]);
            }
            if (null != mSprCurView) {
                double mlayout_width = mLayoutWidth + .0, mlayout_height = mLayoutHeight + .0;

                Double fx = mSprCurView.getLeft() / mlayout_width;
                Double fy = mSprCurView.getTop() / mlayout_height;

                float x1 = (float) (mSprCurView.getCenter().x / (mlayout_width));
                float y1 = (float) (mSprCurView.getCenter().y / (mlayout_height));
                float[] centerxy = new float[]{x1, y1};

                mCurrentInfo.setLeft(fx);
                mCurrentInfo.setTop(fy);
                mCurrentInfo.setTextSize((int) (mSprCurView.getTextSize()));
                mCurrentInfo.setRotateAngel(mSprCurView.getRotateAngle());
                mCurrentInfo.setTtfLocalPath(mSprCurView.getTTFlocal());
                mCurrentInfo.setTextColor(mSprCurView.getTextColor());
                mCurrentInfo.setWidthx(mSprCurView.getWidth() / mlayout_width);
                mCurrentInfo.setHeighty(mSprCurView.getHeight() / mlayout_height);
                mCurrentInfo.setCenterxy(centerxy);
                mCurrentInfo.setDisf(mSprCurView.getDisf());
                mCurrentInfo.setZoomFactor(mSprCurView.getZoomFactor());
                mCurrentInfo.setShadowColor(mSprCurView.getShadowColor());
                mCurrentInfo.setText(mSprCurView.getText());
            }

            if (needStart) {
                if (mEditorHandler != null) {
                    mEditorHandler.stop();
                    mEditorHandler.start();
                }
            }

            int re = getIndex(mCurrentInfo.getId());
            if (re > -1) {
                mWordInfoList.set(re, mCurrentInfo); // 重新编辑
            } else {
                mWordInfoList.add(mCurrentInfo); // 新增
            }
            mCurWordId = 0;
            checkVisible(mCurrentInfo);
        }

        if (null != mColorPicker) {
            mColorPicker.ToReset();
        }

        if (null != mTTFHandler) {
            mTTFHandler.ToReset();
        }

    }

    /**
     * 当前字幕在集合的索引
     *
     * @param id 当前字幕的Id
     * @return
     */
    private int getIndex(int id) {
        int index = -1;
        for (int i = 0; i < mWordInfoList.size(); i++) {
            if (id == mWordInfoList.get(i).getId()) {
                index = i;
                break;
            }
        }
        return index;
    }


    /**
     * 导出字幕
     *
     * @param nOutVideoWidth  保存视频高度
     * @param nOutVideoHeight 保存视频宽度
     * @return
     */
    public void onExport(int nOutVideoWidth, int nOutVideoHeight,
                         IExportSub back) {

        SubExportUtils expUtils = new SubExportUtils(mContext, mWordInfoList,
                mLayoutWidth, mLayoutHeight);

        expUtils.onExport(nOutVideoWidth, nOutVideoHeight, back);

    }

    public ArrayList<SubtitleObject> onExport(int nOutVideoWidth,
                                              int nOutVideoHeight) {

        SubExportUtils expUtils = new SubExportUtils(mContext, mWordInfoList,
                mLayoutWidth, mLayoutHeight);

        return expUtils.onExport(nOutVideoWidth, nOutVideoHeight);

    }

    private boolean mIsSetWatcher = false;

    /**
     * 执行删除单个word
     */
    private void onDelWordItem(SinglePointRotate Single, int id) {
//        Log.e(TAG, (null != Single) + "onDelWordItem: " + id);
        if (null != Single) {
            mLinearWords.removeView(Single);
            mSubtitleLine.removeById(Single.getId());
            removeById(Single.getId());
            Single.recycle();
            Single = null;
            mCurWordId = 0;

        } else {
            mSubtitleLine.removeById(id);
            removeById(id);
        }
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
        }

        if (mAddLayout.getVisibility() != View.VISIBLE) {
            mAddLayout.setVisibility(View.VISIBLE);
        }
        mAddStep = false;
        checkVisible(mScrollView.getProgress());
        mCurrentInfo = null;
        mSprCurView = null;
    }

    private void onCheckStyle(WordInfo info) {
        if (mAnimAdapter.getCount() > 0) {
            mAnimAdapter.setCheckItem(mAnimAdapter.getPosition(info
                    .getStyleId()));

        }
    }

    private void initItemSub() {
        mSprCurView = (SinglePointRotate) mLinearWords.findViewById(mCurrentInfo
                .getId());
        if (null == mSprCurView) {
            mSprCurView = initItemWord(mCurrentInfo);
            mLinearWords.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (null != mSprCurView)
                        mLinearWords.addView(mSprCurView);
                }
            }, 0);

        } else {
            mSprCurView.setVisibility(View.VISIBLE);
            mSprCurView.setInputText(mSprCurView.getText());
        }
        onCheckStyle(mCurrentInfo);
        mSprCurView.setDelListener(new SinglePointRotate.onDelListener() {

            @Override
            public void onDelete(SinglePointRotate single) {
                onDelWordItem(single, -1);
            }
        });
        mSprCurView.setControl(true);// 显示控制按钮，可以随意拖动
        if (!TextUtils.isEmpty(mCurrentInfo.getTtfLocalPath())) {
            mSprCurView.setTTFLocal(mCurrentInfo.getTtfLocalPath());
        }

    }

    /**
     * 开始编辑字幕
     */
    private void onStartSub(boolean isdownload) {
        mIsSubing = true;
        mIsEditing = true;
        mISubtitle.setTitle(R.string.edit_subtitle);
        mISubtitle.onViewVisible(false);
        mIsSetWatcher = true;
        if (mEditorHandler != null) {
            mEditorHandler.pause();
        }
        mAddLayout.setVisibility(View.GONE);
        mMenuLayout.setVisibility(View.VISIBLE);
        if (isdownload) {
            initItemSub();
            onAdapterResume();
        }
        mEtSubtitle.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (mIsSetWatcher) {
                    if (null != mSprCurView) {
                        mSprCurView.setInputText(s.toString());
                    }
                    if (null != mCurrentInfo) {
                        mCurrentInfo.setText(s.toString());
                    }

                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ((RadioButton) mRoot.findViewById(R.id.subtitle_style))
                .setChecked(true);

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
                } else {
                    tv.setInputText(tv.getText());
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

        int vCount = mLinearWords.getChildCount();

        for (int j = 0; j < vCount; j++) {
            View v = mLinearWords.getChildAt(j);
            for (int i = 0; i < mWordInfoList.size(); i++) {
                WordInfo w = mWordInfoList.get(i);
                if (null != w && null != v && v.getId() == w.getId()) {
                    if (w.getStart() <= cp && cp <= w.getEnd()) {
                    } else {
                        if (v.getId() != mCurWordId) {
                            mLinearWords.removeView(v);
                            ((SinglePointRotate) v).recycle();
                            v = null;
                        }
                    }
                }

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

            if (item.getStart() - 5 <= ntime && ntime < item.getEnd() + 5) {
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

        StyleInfo si = SubUtils.getInstance().getStyleInfo(w.getStyleId());
        si.zoomFactor = w.getZoomFactor();

        FilterInfo2 fi = si.getFilterInfo2();

        int color = Color.WHITE;

        String text = "";
        if (null != fi) {
            text = TextUtils.isEmpty(w.getText()) ? fi.getHint() : w.getText();
            color = (w.getTextColor() == Color.WHITE ? fi.getTextDefaultColor()
                    : w.getTextColor());

        }
        if (TextUtils.isEmpty(w.getText()))
            mEtSubtitle.setHint(R.string.sub_hint);
        else {
            if (null != fi) {
                mEtSubtitle.setText(w.getText());
            }
        }
        String bgpath = null;
        if (si.frameArry.size() > 0) {
            bgpath = si.frameArry.valueAt(0).pic;
        }
        SinglePointRotate tv = new SinglePointRotate(mLinearWords.getContext(),
                w.getRotateAngle(), text, color, w.getTtfLocalPath(),
                w.getDisf(), new Point(mLayoutWidth, mLayoutHeight), new Point(
                mcenterx, mcentery), w.getTextSize(),
                w.getShadowColor(), si, bgpath);

        tv.setOnClickListener(new SinglePointRotate.onClickListener() {

            @Override
            public void onClick(SinglePointRotate v) {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                }
                if (mMenuLayout.getVisibility() != View.VISIBLE) {
                    mSubtitleLine.editSub(v.getId());
                    WordInfo info = getItem(v.getId());

                    if (info != null) {
                        mCurrentInfo = new WordInfo(info);
                        if (null != mCurrentInfo) {
                            onStartSub(true);
                            mEtSubtitle.setText(mCurrentInfo.getText());
                        }
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

    private OnClickListener mClearSubtitle = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mEtSubtitle.setText("");
        }
    };

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup mRgMenu, int checkedId) {

            onVisibile(checkedId);
        }
    };

    private RadioGroup.OnCheckedChangeListener onShadowChangListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup mRgMenu, int checkedId) {

            onShadowVisibile(checkedId);
        }
    };
    private SpecialStyleAdapter mAnimAdapter = null;
    private IGetData mIData;

    /**
     * 获取字幕样式
     */
    private void getData(boolean addItem) {
        if (mAnimAdapter.getCount() == 0) {
            mIData = new IGetData(addItem);
            ThreadPoolUtils.execute(mIData);
        }
    }

    /**
     * 获取字幕资源列表
     */
    class IGetData implements Runnable {
        boolean onAddItemClick = false;

        public IGetData(boolean addItem) {
            onAddItemClick = addItem;
        }

        @Override
        public void run() {
            ArrayList<StyleInfo> mWordInfoList = SubUtils.getInstance()
                    .getDownLoadedList(mContext);

            mAnimAdapter.addStyles(mWordInfoList);
            mHandler.sendEmptyMessageDelayed(MSG_LISTVIEW, 200);
            SubUtils.getInstance().clearArray();
            String content = null;
            if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                content = null;
            } else {
                content = SubUtils.getInstance().getSubJson();
            }
            mGvSubtitleStyle.post(new Runnable() {

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
                        ArrayList<StyleInfo> dbList = SubData.getInstance()
                                .getAll();
                        StyleInfo temp = null;
                        JSONObject jobj = null;
                        JSONArray jarr = jex.getJSONArray("data");
                        final JSONObject jicon = jex.getJSONObject("icon");
                        String timeunix = jicon.getString("timeunix");
                        if (!AppConfiguration.checkSubIconIsLasted(timeunix)) {
                            downloadData(timeunix, jicon);
                        }

                        int len = jarr.length();

                        for (int i = 0; i < len; i++) {

                            jobj = jarr.getJSONObject(i);
                            temp = new StyleInfo();
                            temp.code = jobj.optString("name");
                            temp.caption = jobj.optString("zimu");
                            temp.pid = temp.code.hashCode();
                            temp.index = i;
                            temp.nTime = jobj.getLong("timeunix");
                            StyleInfo dbTemp = checkExit(dbList, temp);

                            if (null != dbTemp) {
                                if (SubData.getInstance().checkDelete(temp,
                                        dbTemp)) {
                                    temp.isdownloaded = false;
                                } else {
                                    temp.isdownloaded = true;
                                    temp.isdownloaded = dbTemp.isdownloaded;
                                    if (temp.isdownloaded) {
                                        temp.mlocalpath = dbTemp.mlocalpath;
                                        File idfile = new File(temp.mlocalpath);
                                        CommonStyleUtils.checkStyle(idfile,
                                                temp);
                                    }
                                }
                            }

                            SubUtils.getInstance().putStyleInfo(temp);
                        }
                        if (null != dbList) {
                            dbList.clear();
                        }
                        ArrayList<StyleInfo> newall = SubUtils.getInstance()
                                .getStyleInfos();
                        SubData.getInstance().replaceAll(newall);
                        if (mMenuLayout.getVisibility() == View.VISIBLE) {
                            mAnimAdapter.addStyles(newall);
                            if (null != mHandler) {
                                mHandler.removeMessages(MSG_LISTVIEW);
                                mHandler.sendEmptyMessageDelayed(MSG_LISTVIEW,
                                        200);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
            if (onAddItemClick) {
                // 执行新增一个字幕逻辑
                mBtnAddSubtitle.post(new Runnable() {
                    @Override
                    public void run() {
                        onAddListener.onClick(mBtnAddSubtitle);
                    }
                });

            }
            mIData = null;

        }
    }

    ;

    /**
     * 下载字幕图标
     *
     * @param timeUnix
     * @param jicon
     */
    private void downloadData(final String timeUnix, final JSONObject jicon) {
        DownLoadUtils utils = new DownLoadUtils(jicon.optString("name")
                .hashCode(), jicon.optString("zimu"), "zip");
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

                boolean re = fold.renameTo(zip);
//                Log.e(TAG, fold
//                        .getAbsolutePath() + "Finished: " + re + ".....>" + zip.getAbsolutePath());

                if (re && zip.exists()) { // 解压
                    FileUtils.deleteAll(new File(PathUtils.getRdSubPath(),
                            "icon"));
                    try {
                        String dirpath = FileUtils.unzip(zip.getAbsolutePath(),
                                PathUtils.getRdSubPath());
//                        Log.e(TAG, "Finished: 解压成功" + dirpath + "--->" + PathUtils.getRdSubPath());
                        if (!TextUtils.isEmpty(dirpath)) {
                            mHandler.sendEmptyMessage(MSG_ICON);
                            String[] icons = new File(dirpath)
                                    .list(new FilenameFilter() {

                                        @Override
                                        public boolean accept(File dir,
                                                              String filename) {
                                            return filename.endsWith(".png");
                                        }
                                    });
                            AppConfiguration
                                    .setSubIconVersion(timeUnix, dirpath,
                                            (null != icons) ? icons.length : 0);
                        }
                        zip.delete(); // 删除原mv的临时文件
                    } catch (IOException e) {
                        e.printStackTrace();
                        zip.delete(); // 删除原mv的临时文件
                        mHandler.sendEmptyMessage(MSG_ICON);
                    }
                }
            }
        });
    }

    private final int MSG_ICON = 5665, MSG_LISTVIEW = 5689;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_ICON:
                    mAnimAdapter.updateIcon();
                    break;
                case MSG_LISTVIEW:
                    mAnimAdapter.setListview(mGvSubtitleStyle);
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

    /**
     * 控制menu部分的布局状态
     *
     * @param checkedId
     */
    private void onVisibile(int checkedId) {
        if (checkedId == R.id.subtitle_style) {
            mISubtitle.setTitle(R.string.sub_style);
            if (null != mTTFHandler)
                mTTFHandler.onPasue();
            mStyleLayout.setVisibility(View.VISIBLE);
            mTTFLayout.setVisibility(View.GONE);
            mColorLayout.setVisibility(View.GONE);
            mSizeLayout.setVisibility(View.GONE);
            getData(false);

        } else if (checkedId == R.id.subtitle_ttf) {
            hideInput();
            mISubtitle.setTitle(R.string.et_subtitle_ttf);
            mTTFLayout.setVisibility(View.VISIBLE);
            mTTFHandler.refleshData(true);
            if (mSprCurView != null) {
                String strTTFlocal = mSprCurView.getTTFlocal();
                if (!TextUtils.isEmpty(strTTFlocal)) {
                    int ttfNum = mTTFHandler.ttfAdapter.getCount();
                    for (int i = 0; i < ttfNum; i++) {
                        String localPath = mTTFHandler.ttfAdapter.getItem(i).local_path;
                        if (localPath != null && localPath.equals(strTTFlocal)) {
                            mTTFHandler.ttfAdapter.setCheck(i);
                            break;
                        }
                    }
                } else {
                    mTTFHandler.ToReset();// 执行默认字体
                }
            }
            mStyleLayout.setVisibility(View.GONE);
            mColorLayout.setVisibility(View.GONE);
            mSizeLayout.setVisibility(View.GONE);

        } else if (checkedId == R.id.subtitle_color) {
            hideInput();
            mISubtitle.setTitle(R.string.et_subtitle_color);
            mTTFHandler.onPasue();
            mStyleLayout.setVisibility(View.GONE);
            mTTFLayout.setVisibility(View.GONE);
            mSizeLayout.setVisibility(View.GONE);
            mColorLayout.setVisibility(View.VISIBLE);

            if (null != mSprCurView) {
                mColorPicker.checkColor(mSprCurView.getTextColor());
                if (mSprCurView.shadowColor == 0) {
                    mRgShadow.check(R.id.subtitle_stroke_none);
                } else if (mSprCurView.shadowColor == mContext.getResources()
                        .getColor(R.color.white)) {
                    mRgShadow.check(R.id.subtitle_stroke_white);
                } else if (mSprCurView.shadowColor == mContext.getResources()
                        .getColor(R.color.black)) {
                    mRgShadow.check(R.id.subtitle_stroke_black);
                }
            }
            mColorPicker.invalidate();
        } else if (checkedId == R.id.subtitle_size) {
            hideInput();
            mISubtitle.setTitle(R.string.et_subtitle_size);
            mTTFHandler.onPasue();
            mStyleLayout.setVisibility(View.GONE);
            mTTFLayout.setVisibility(View.GONE);
            mColorLayout.setVisibility(View.GONE);
            mSizeLayout.setVisibility(View.VISIBLE);
            onInitSizeTimeLine();
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

    /**
     * 控制描边部分的布局状态
     *
     * @param checkedId
     */
    private void onShadowVisibile(int checkedId) {
        if (checkedId == R.id.subtitle_stroke_white) {
            setShadowColor(mContext.getResources().getColor(R.color.white));
        } else if (checkedId == R.id.subtitle_stroke_black) {
            setShadowColor(mContext.getResources().getColor(R.color.black));
        } else {
            setShadowColor(0);
        }
    }

    /**
     * 播放控制器图标
     *
     * @param resId
     */
    public void setImage(int resId) {
        if (isEditing()) {
            mPlayState.setImageResource(resId);
        }
    }

    /**
     * 返回
     *
     * @return
     */
    public int onSubBackPressed() {
        if (mMenuLayout.getVisibility() == View.VISIBLE) {

            if (mAddStep) {
                mBtnAddSubtitle.setImageResource(R.drawable.add_subtitle_btn);
                mDelete.setVisibility(View.INVISIBLE);
                mTvAddSubtitle.setText(R.string.add_subtitle);
            }
            onMenuBackPressed();
            if (null != mAnimAdapter) {
                mAnimAdapter.clearDownloading();
            }
            mAddStep = false;
            return -1;

        } else if (mRoot.getVisibility() == View.VISIBLE) {
            if (!CommonStyleUtils.isEquals(mWordInfoList, TempVideoParams.getInstance()
                    .getSubsDuraionChecked())
                    || mIsUpdate) {
                onShowAlert();
                return 0;
            } else {
                onBackToActivity(false);
                return 1;
            }
        } else {
            mIsSubing = false;
            return 0;
        }

    }

    private void onMenuBackPressed() {
        mISubtitle.onViewVisible(true);
        if (null != mSprCurView) {
            boolean hasExit = checkExit(mSprCurView.getId());
            if (hasExit) {
                mSubtitleLine.replace(mCurrentInfo.getId(), mCurrentInfo.getText());
            } else {
                mSubtitleLine.removeById(mSprCurView.getId());
            }
            mSprCurView.recycle();
            mSprCurView = null;
        }
        mCurrentInfo = null;
        mLinearWords.removeAllViews();
        onMenuViewOnBackpressed();

        mViewTouchListener.onActionUp();

    }

    /**
     * 点击上级界面（场景：正在编辑字幕样式，(1点击保存按钮,2点击返回))
     */
    private void onMenuViewOnBackpressed() {
        mEtSubtitle.setText("");
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
        }
        mTTFHandler.onPasue();
        if (mAddLayout.getVisibility() != View.VISIBLE) {
            mAddLayout.setVisibility(View.VISIBLE);
        }
        InputUtls.hideKeyboard(mEtSubtitle);
        mISubtitle.setTitle(R.string.et_subtitle_edit);
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
                        onBackToActivity(false);
                        dialog.dismiss();
                        mISubtitle.onBackPressed();
                    }
                }, false, null).show();
    }

    /**
     * 返回
     *
     * @param save
     */
    private void onBackToActivity(boolean save) {
        mIsUpdate = false;
        if (mAddStep) {
            onWordEnd();
        }
        if (save) {
            onSaveToList(true);
        } else {
            for (int n = 0; n < mWordInfoList.size(); n++) {
                mWordInfoList.get(n).set(mTempWordList.get(n));
            }
        }
        mHandler.removeMessages(MSG_ICON);
        mHandler.removeMessages(MSG_LISTVIEW);
        mLinearWords.removeAllViews();
        mAddLayout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.editor_preview_slide_out));
        mAddLayout.setVisibility(View.GONE);
        mRoot.setVisibility(View.GONE);
        if (null != mSubtitleLine) {
            mSubtitleLine.recycle();
        }
        onScrollTo(0);
        setProgressText(0);
        mIsSubing = false;
        if (null != mAnimAdapter) {
            mAnimAdapter.onDestory();
        }

        mTTFHandler.onDestory();
        mTTFHandler = null;
        mEditorHandler.unregisterEditorProgressListener(mEditorPreivewPositionListener);
        mSubtitleLine.clearAll();
        bUIPrepared = false;
    }

    /**
     * 保存当前编辑字幕到集合 （完成按钮，播放按钮控制）
     */
    private void onSaveToList(boolean clearCurrent) {

        if (mSprCurView != null) {
            saveInfo(clearCurrent);
        }
        if (clearCurrent)
            mSubtitleLine.clearCurrent();
        mISubtitle.setTitle(R.string.add_subtitle);
    }

    /**
     * 点击完成
     */
    public int onSurebtn() {
        onBackToActivity(true);
        TempVideoParams.getInstance().setSubs(mWordInfoList);
        return 1;

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
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {

        if (null != mCurrentInfo && mAddStep) {
            mCurrentInfo.setEnd(progress);

            mSubtitleLine.update(mCurrentInfo.getId(), (int) mCurrentInfo.getStart(),
                    progress);

        }

        initWords(progress);
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }

    private int getScrollX(long progress) {
        return (int) (progress * (mSubtitleLine.getThumbWidth() / mDuration));
    }

    /**
     * 播放完成
     */
    private void onScrollCompleted() {
        onScrollTo((int) mSubtitleLine.getThumbWidth());
        setProgressText(mDuration);
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

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context mContext, Intent intent) {

            String path = intent.getStringExtra(TTFAdapter.TTF_ITEM);
            if (!TextUtils.isEmpty(path)) {
                if (null != mSprCurView
                        && mTTFLayout.getVisibility() == View.VISIBLE) {
                    mSprCurView.setTTFLocal(path);
                }

            }

        }

    };

    public BroadcastReceiver mSubtitleReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context mContext, Intent intent) {
            int position = intent.getIntExtra(
                    SpecialStyleAdapter.DOWNLOADED_ITEM_POSITION, -1);
            if (-1 != position && mMenuLayout.getVisibility() == View.VISIBLE) {
                if (null != mCurrentInfo) {
                    StyleInfo info = mAnimAdapter.getItem(position);
                    if (null != info) {
                        mCurrentInfo.setStyleId(info.pid);
                        // Log.e("subtitler....setStyleId..", "setStyleId--"
                        // + info.pid);
                    }
                }
                if (null != mCurrentInfo) {
                    initItemSub();
                    onStyleItem(position, mCurrentInfo);
                } else {
                    Log.e(TAG, "onReceive: 当前没有字幕");
                }
            }

        }

    };

    private boolean mIsDownloading = false;

    void setIsLoading(boolean isloading) {
        if (isEditing()) {
            mIsDownloading = isloading;
            // 防止下载完成设置adapter.setcheck(int p)无法notifi
            mAnimAdapter.notifyDataSetChanged();
        }
    }

    private int mStatusBarHeight = 0;
    private boolean mIsEditing = false;

    private void controlKeyboardLayout(final View root, final View scrollToView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (isEditing() && mIsEditing) {
                            mIsEditing = false;
                            Rect rect = new Rect();
                            // 获取root在窗体的可视区域
                            root.getWindowVisibleDisplayFrame(rect);
                            // 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                            int rootInvisibleHeight = root.getRootView()
                                    .getHeight() - rect.bottom;

                            // 若不可视区域高度大于100，则键盘显示
                            if (rootInvisibleHeight > 100) { // rootInvisibleHeight
                                // 值为输入法Frame的高度
                                int[] location = new int[2];
                                // 获取scrollToView在窗体的坐标
                                scrollToView.getLocationInWindow(location);

                                View t = root.findViewById(R.id.theframelayout);

                                int tY = mDisplay.heightPixels
                                        - rootInvisibleHeight
                                        - scrollToView.getHeight()
                                        - mStatusBarHeight;

                                if (location[1] > tY) { // 输入法打开对于目标区域有遮挡
                                    t.setY(tY);
                                }
                                if (mEtSubtitle.getVisibility() == View.VISIBLE) {
                                    mEtSubtitle.postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            mEtSubtitle.selectAll();
                                            mEtSubtitle.setSelectAllOnFocus(true);
                                        }
                                    }, 150);
                                }
                            } else {
                                // 键盘隐藏
                                View framelayout = root
                                        .findViewById(R.id.theframelayout);
                                framelayout.setY(mDisplay.heightPixels
                                        - framelayout.getHeight()
                                        - mStatusBarHeight);

                            }
                        }

                    }

                });

    }
}
