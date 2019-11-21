package com.rd.veuisdk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.GraffitiAdapter;
import com.rd.veuisdk.demo.VideoEditAloneActivity;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.GraffitiInfo;
import com.rd.veuisdk.ui.ColorDragScrollView;
import com.rd.veuisdk.ui.ColorPicker;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.PaintView;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.util.ArrayList;


/**
 * 涂鸦fragment
 */
public class GraffitiFragment extends BaseFragment {

    public static GraffitiFragment newInstance() {

        Bundle args = new Bundle();

        GraffitiFragment fragment = new GraffitiFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private PaintView mPaintView;
    private IVideoEditorHandler mEditorHandler;
    private boolean bThumbPrepared = false;
    private View menuLayout;
    private View addLayout;
    private int mDuration;

    private int mHalfWidth = 0;
    private DisplayMetrics mDisplayMetrics;
    private int mStateSize;
    private View hintView;
    private TextView tvProgress;
    private ImageView ivPlayState;
    private TimelineHorizontalScrollView mTimelineHorizontalScrollView;
    private ThumbNailLine mThumbNailLine;
    private LinearLayout mLinearLayout;
    private Button mBtnAdd;
    private View mBtnDelete;
    private TextView tvTitle;
    private View mRecyclerParent;
    private ArrayList<GraffitiInfo> mList = new ArrayList<>();


    public void setPaintView(PaintView paintView) {
        mPaintView = paintView;
    }

    private ColorDragScrollView mColorScrollView;

    private GraffitiAdapter mAdapter;
    private IParamData mParamData;
    //备份之前的数据
    private ArrayList<GraffitiInfo> backup = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mEditorHandler = (IVideoEditorHandler) context;
        mParamData = ((IParamHandler) context).getParamData();
        mList = mParamData.getGraffitiList();
        if (null == mList) {
            mList = new ArrayList<>();
        }
        if (mList.size() > 0) {
            backup = new ArrayList<>();
            backup.addAll(mList);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_graffiti, container, false);
        bThumbPrepared = false;
        mDisplayMetrics = CoreUtils.getMetrics();
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        tvProgress = $(R.id.tvAddProgress);
        ivPlayState = $(R.id.ivPlayerState);
        mTimelineHorizontalScrollView = $(R.id.priview_sticker_line);
        mLinearLayout = $(R.id.subtitleline_media);
        mThumbNailLine = $(R.id.subline_view);
        mRecyclerParent = $(R.id.recycleParent);
        hintView = $(R.id.word_hint_view);
        tvTitle = $(R.id.tvTitle);
        tvTitle.setText(R.string.graffiti);
        if (null != mPaintView) {
            mPaintView.setVisibility(View.VISIBLE);
            mPaintView.setCanDraw(true);
        }
        addLayout = $(R.id.sticker_add_layout);
        menuLayout = $(R.id.subtitle_color_layout);
        mEditorHandler.registerEditorPostionListener(mPositionListener);
        mBtnAdd = $(R.id.btn_add_item);
        $(R.id.btn_edit_item).setVisibility(View.GONE);
        mBtnDelete = $(R.id.btn_del_item);
        mColorScrollView = $(R.id.scrollColorPicker);
        mBtnAdd.setText(R.string.add);
        reset();
        isGraffitiItemFirstForLine = false;
        RecyclerView mRecyclerView = $(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new GraffitiAdapter(getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<GraffitiInfo>() {
            @Override
            public void onItemClick(int position, GraffitiInfo info) {
                onEdit(info);

            }
        });
        mRecyclerView.setAdapter(mAdapter);
        checkRecyclerVisible();
        return mRoot;
    }

    /**
     * 响应编辑UI
     *
     * @param info
     */
    private void onEdit(GraffitiInfo info) {
        if (mEditorHandler.isPlaying()) {
            pauseVideo();
        }
        //跳到指定位置
        int seekto = info.getTimelineFrom() + 1;
        mEditorHandler.seekTo(seekto);  // 额外加1ms
        onScrollProgress(seekto);
        //选中当前片段(编辑)
        mCurrentEdit = info;
        isEdit = true;
        //当前编辑项高亮
        mThumbNailLine.setCantouch(true);
        mThumbNailLine.setMoveItem(true);
        mThumbNailLine.showCurrent(mCurrentEdit.getId());
        mBtnDelete.setVisibility(View.VISIBLE);
    }

    private void checkRecyclerVisible() {
        if (mList.size() > 0) {
            tvTitle.setVisibility(View.GONE);
            mRecyclerParent.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            mRecyclerParent.setVisibility(View.GONE);
        }
        mAdapter.addAll(mList, -1);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDuration = mEditorHandler.getDuration();
        onInitThumbTimeLine();
        onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());
        initListener();
    }

    private void playVideo() {
        mEditorHandler.start();
        ivPlayState.setImageResource(R.drawable.edit_music_pause);
        mThumbNailLine.setHideCurrent();
    }

    private void pauseVideo() {
        mEditorHandler.pause();
        ivPlayState.setImageResource(R.drawable.edit_music_play);
    }

    /**
     * 调整播放那个状态
     */
    private View.OnClickListener onStateChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mEditorHandler.isPlaying()) {
                pauseVideo();
                if (mCurrentEdit != null) {
                    if (isGraffitiItemFirstForLine) { //确认当前新增的涂鸦的结束点
                        checkAddLineSave(mEditorHandler.getCurrentPosition());
                    }
                }
            } else {
                if (Math.abs(mEditorHandler.getCurrentPosition() - mEditorHandler.getDuration()) < 300) {
                    mEditorHandler.seekTo(0);
                }
                if (isEdit) { //退出编辑时间线
                    exitEditMode();
                }
                playVideo();
            }
        }
    };

    /**
     * 透明度
     *
     * @param alpha 0~100
     */
    private void setAlpha(int alpha) {
        mTvColorAlphaPercent.setText(alpha + "%");
        mPaintView.setAlpha(1 - (alpha / 100.0f));
    }

    private final float MAX_STROKE_WIDTH = 25.0f;
    private SeekBar.OnSeekBarChangeListener mOnStrokeWidthChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (null != mPaintView) {
                mPaintView.setStrokeWidth((int) (MAX_STROKE_WIDTH * progress / 100));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private TextView mTvColorAlphaPercent;
    private SeekBar.OnSeekBarChangeListener mOnColorAlphaChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setAlpha(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private final int ALPHA = 10;

    private void initListener() {
        SeekBar mSbarAlpha = $(R.id.sbSubtitleColorAlpha);
        mSbarAlpha.setOnSeekBarChangeListener(mOnColorAlphaChangeListener);

        SeekBar mSbStrokeWidth = $(R.id.sbStrokeWdith);
        mSbStrokeWidth.setProgress((int) (100 * mPaintView.getStrokeWidth() / MAX_STROKE_WIDTH));
        mSbStrokeWidth.setOnSeekBarChangeListener(mOnStrokeWidthChangeListener);

        mTvColorAlphaPercent = $(R.id.tvColorAlphaPercent);
        mSbarAlpha.setProgress(ALPHA);
        setAlpha(ALPHA);
        $(R.id.ivColorDefault).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //撤销上一个操作  path
                mPaintView.revoke();

            }
        });
        mColorScrollView.setColorChangedListener(new ColorPicker.IColorListener() {
            @Override
            public void getColor(int color, int position) {
                mPaintView.setPaintColor(color);
            }
        });
        ivPlayState.setOnClickListener(onStateChangeListener);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    //编辑事件线中，  --保存当前编辑
                    exitEditMode();
                }
                pauseVideo();
                String menu = mBtnAdd.getText().toString();
                if (menu.equals(getString(R.string.add))) {
                    //先保存之前编辑的项
                    onAddStep1Begin();
                } else if (menu.equals(getString(R.string.complete))) {
                    checkAddLineSave(mEditorHandler.getCurrentPosition());
                }
            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checked = mAdapter.getCheckedIndex();
                if (checked >= 0) {
                    //删除列表中的选中项
                    GraffitiInfo graffitiInfo = mList.remove(checked);
                    if (null != graffitiInfo) {
                        mThumbNailLine.removeById(graffitiInfo.getId());
                        checkRecyclerVisible();
                        mBtnDelete.setVisibility(View.GONE);
                        if (null != mListener) {
                            mListener.onDelete(graffitiInfo.getLiteObject());
                        }
                    }
                } else {
                    if (isGraffitiItemFirstForLine) {
                        //删除正在新增的项
                        exitAddLineMode();
                    }
                }
            }
        });

        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        $(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGraffitiLayout()) {
                    reset();
                    mCurrentEdit = new GraffitiInfo(mEditorHandler.getCurrentPosition(), mDuration);
                    //创建简单字幕并绑定到播放器
                    createGraffitiAndInsert();
                    playVideo();
                } else {
                    String menu = mBtnAdd.getText().toString();
                    if (menu.equals(getString(R.string.complete))) {
                        pauseVideo();
                        checkAddLineSave(mEditorHandler.getCurrentPosition());
                    } else {
                        if (isEdit) {
                            exitEditMode();
                            return;
                        }
                        onSure();
                    }
                }
            }
        });

        mThumbNailLine.setSubtitleThumbNailListener(new IThumbLineListener() {
            int id, start, end;

            @Override
            public void updateThumb(int id, int start, int end) {
                this.id = id;
                this.start = start;
                this.end = end;

            }

            @Override
            public void onCheckItem(boolean changed, int id) {

            }

            @Override
            public void onTouchUp() {
                if (null != mCurrentEdit) {
                    mCurrentEdit.updateTimeline(start, end);
                    mListener.onUpdate(mCurrentEdit.getLiteObject());
                }
            }
        });
        mTimelineHorizontalScrollView.addScrollListener(mScrollViewListener);
        mTimelineHorizontalScrollView.setViewTouchListener(new IViewTouchListener() {
            @Override
            public void onActionDown() {

            }

            @Override
            public void onActionMove() {
                int progress = mTimelineHorizontalScrollView.getProgress();
                setProgressText(progress);
            }

            @Override
            public void onActionUp() {
                mTimelineHorizontalScrollView.resetForce();
                int progress = mTimelineHorizontalScrollView.getProgress();
                setProgressText(progress);
            }
        });
    }

    /**
     * 涂鸦->简单字幕
     */
    private void createGraffitiAndInsert() {
        String path = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP + "_graffiti_", "png");
        mPaintView.save(path);
        mPaintView.setCanDraw(false);
        mPaintView.setVisibility(View.GONE);
        mPaintView.clear();
        mCurrentEdit.setTimelineTo(mDuration);
        mCurrentEdit.setPath(path);
        mCurrentEdit.createObject();

        //实时插入简单字幕
        mListener.onUpdate(mCurrentEdit.getLiteObject());
        mThumbNailLine.addRect(mCurrentEdit.getTimelineFrom(), mCurrentEdit.getTimelineFrom() + 10, "", mCurrentEdit.getId());
        mThumbNailLine.showCurrent(mCurrentEdit.getId());
        mThumbNailLine.setHideCurrent();
        isGraffitiItemFirstForLine = true;
        mBtnAdd.setText(R.string.complete);
        mBtnDelete.setVisibility(View.VISIBLE);
    }

    private ScrollViewListener mScrollViewListener = new ScrollViewListener() {

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY, boolean appScroll) {

            mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
            if (!appScroll) {
                int progress = mTimelineHorizontalScrollView.getProgress();
                pauseVideo();
                setProgressText(progress);
            }
        }

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY, boolean appScroll) {
            mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
            if (!appScroll) {
                int progress = mTimelineHorizontalScrollView.getProgress();
                mEditorHandler.seekTo(progress);
                setProgressText(progress);
            }
        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY, boolean appScroll) {
            int progress = mTimelineHorizontalScrollView.getProgress();
            mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
            if (!appScroll) {
                mEditorHandler.seekTo(progress);
            }
            setProgressText(progress);
        }
    };

    private void onSure() {
        isEdit = false;
        mParamData.setGraffitiList(mList);
        mEditorHandler.onSure();
    }

    private GraffitiInfo mCurrentEdit;

    /***
     * 编辑涂鸦内容中
     */
    private boolean isGraffitiLayout() {
        return menuLayout.getVisibility() == View.VISIBLE;
    }

    //进入涂鸦菜单，选择颜色并，绘制涂鸦效果
    private void onAddStep1Begin() {
        isEdit = false;
        mAdapter.setChecked(-1);
        mRecyclerParent.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        mThumbNailLine.setShowCurrentFalse();
        menuLayout.setVisibility(View.VISIBLE);
        addLayout.setVisibility(View.GONE);
        int color = Color.WHITE;
        mColorScrollView.setChecked(color);
        mPaintView.clear();
        mPaintView.setVisibility(View.VISIBLE);
        mPaintView.setCanDraw(true);
        mPaintView.setPaintColor(color);
    }


    private void reset() {
        addLayout.setVisibility(View.VISIBLE);
        menuLayout.setVisibility(View.GONE);
        mPaintView.setCanDraw(false);
        mPaintView.setVisibility(View.GONE);
    }


    private void onInitThumbTimeLine() {

        mHalfWidth = mDisplayMetrics.widthPixels / 2;
        mTimelineHorizontalScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        int[] mSizeParams = mThumbNailLine.setDuration(mDuration, mTimelineHorizontalScrollView.getHalfParentWidth());
        mTimelineHorizontalScrollView.setLineWidth(mSizeParams[0]);
        mTimelineHorizontalScrollView.setDuration(mDuration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mSizeParams[0]
                + 2 * mThumbNailLine.getpadding(), mSizeParams[1]);

        lp.setMargins(mTimelineHorizontalScrollView.getHalfParentWidth() - mThumbNailLine.getpadding(),
                0, mHalfWidth - mThumbNailLine.getpadding(), 0);

        mThumbNailLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new FrameLayout.LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);
        mLinearLayout.setLayoutParams(lframe);
        hintView.setVisibility(View.GONE);

    }


    //还未完全打勾（确认添加）    (true :添加单个媒体时调整完位置，还未完全添加时间线； false 其他状态 )
    private boolean isGraffitiItemFirstForLine = false;

    private IVideoEditorHandler.EditorPreivewPositionListener mPositionListener = new IVideoEditorHandler.EditorPreivewPositionListener() {
        @Override
        public void onEditorPrepred() {
            mDuration = mEditorHandler.getDuration();
            Log.e(TAG, "onEditorPrepred: " + mDuration + " " + bThumbPrepared);
            if (!bThumbPrepared) {
                bThumbPrepared = true;
            }
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
        }

        @Override
        public void onEditorPreviewComplete() {

            Log.e(TAG, "onEditorPreviewComplete: " + this);

            if (isGraffitiItemFirstForLine) {
                //当前涂鸦保存
                checkAddLineSave(mDuration);
            }
            onScrollCompleted();

        }
    };

    /**
     * 确定当前新增的涂鸦的结束点
     */
    private void checkAddLineSave(int lineTo) {
        mCurrentEdit.setTimelineTo(lineTo);
        mList.add(mCurrentEdit);
        isGraffitiItemFirstForLine = false;
        mThumbNailLine.setShowCurrentFalse();
        resetGraffitiUI();
        checkRecyclerVisible();
        if (null != mListener) {
            mListener.onUpdate(mCurrentEdit.getLiteObject());
        }
        mCurrentEdit = null;
    }

    public void setListener(IGraffitiListener listener) {
        mListener = listener;
    }

    private IGraffitiListener mListener;

    public static interface IGraffitiListener {

        /**
         * 新增或修改
         */
        void onUpdate(CaptionLiteObject liteObject);

        void onDelete(CaptionLiteObject liteObject);

    }

    /**
     * 初始化缩略图时间轴和恢复数据
     *
     * @param virtualVideo
     */
    private void onInitThumbTimeLine(VirtualVideo virtualVideo) {
        mThumbNailLine.setVirtualVideo(virtualVideo, false);
        mThumbNailLine.prepare(mTimelineHorizontalScrollView.getHalfParentWidth() + mHalfWidth);
        mHandler.postDelayed(resetSubDataRunnable, 100);
    }

    private Runnable resetSubDataRunnable = new Runnable() {
        @Override
        public void run() {
            resetThumbData();
        }
    };
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                default: {
                }
                break;
            }
        }
    };

    /**
     * 恢复时间轴的数据
     */
    private void resetThumbData() {
        onScrollProgress(0);
        mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
        restoreData();
    }

    private void restoreData() {
        ArrayList<SubInfo> subInfos = new ArrayList<>();
        int len = mList.size();
        for (int i = 0; i < len; i++) {
            GraffitiInfo graffitiInfo = mList.get(i);
            subInfos.add(new SubInfo(graffitiInfo.getTimelineFrom(), graffitiInfo.getTimelineTo(), graffitiInfo.getId()));
        }
        mThumbNailLine.prepareData(subInfos);
        mAdapter.addAll(mList, -1);
    }


    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {
        if (isGraffitiItemFirstForLine && null != mCurrentEdit) {
            mThumbNailLine.update(mCurrentEdit.getId(), mCurrentEdit.getTimelineFrom(), progress);
        }
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }


    private int getScrollX(long progress) {
        return (int) (progress * (mThumbNailLine.getThumbWidth() / mDuration));
    }

    /**
     * 播放完成
     */
    private void onScrollCompleted() {
        onScrollTo(0);
        setProgressText(0);
        ivPlayState.setImageResource(R.drawable.edit_music_play);
    }


    /**
     * 设置播放进度
     *
     * @param mScrollX 像素
     */
    private void onScrollTo(int mScrollX) {

        mTimelineHorizontalScrollView.appScrollTo(mScrollX, true);

    }

    private void setProgressText(int progress) {
        tvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
        mThumbNailLine.setDuration(progress);
        mAdapter.setDuration(progress);
    }

    /**
     * 切换到指定项时，响应编辑操作，调整时间线
     */
    private boolean isEdit = false;

    @Override
    public int onBackPressed() {
        if (isGraffitiLayout()) {
            exitAddStep1Mode();
            checkRecyclerVisible();
            return -1;
        } else {
            mAdapter.setChecked(-1);
            if (!isEdit) {
                //退出新增流程
                if (null != mCurrentEdit) {
                    exitAddLineMode();
                    return -1;
                }
            } else {
                exitEditMode();
                return -1;
            }
            if (!Utils.isEquals(backup, mList)) {
                showAlert();
                return -1;
            } else {
                //退出到上一界面
                mParamData.setGraffitiList(backup);
                mEditorHandler.onBack();
                return super.onBackPressed();
            }
        }
    }

    /**
     * 警告是否放弃
     */
    private void showAlert() {
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
                        dialog.dismiss();
                        mParamData.setGraffitiList(backup);
                        if (mExitListener != null) {
                            mExitListener.exit(1);
                        }
                        mEditorHandler.onBack();
                    }
                }, false, null).show();
    }

    /***
     * 退出新增流程
     */
    private void exitAddStep1Mode() {
        mCurrentEdit = null;
        isGraffitiItemFirstForLine = false;
        mPaintView.clear();
        reset();
    }

    /***
     * 退出新增-确定时间线流程
     */
    private void exitAddLineMode() {
        pauseVideo();
        mThumbNailLine.clearCurrent();
        mThumbNailLine.removeById(mCurrentEdit.getId());
        isGraffitiItemFirstForLine = false;
        //真实删除新增的
        mListener.onDelete(mCurrentEdit.getLiteObject());
        mCurrentEdit = null;
        resetGraffitiUI();
    }

    /**
     * 保存||退出时，恢复默认状态
     */
    private void resetGraffitiUI() {
        mBtnAdd.setText(R.string.add);
        mBtnDelete.setVisibility(View.GONE);
    }

    /**
     * 退出时间线组件可编辑模式
     */
    private void exitNailLineEdit() {
        mThumbNailLine.setCantouch(false);
        //mThumbNailLine.setMoveItem(false);
    }

    /**
     * 退出编辑时间线的流程
     */
    private void exitEditMode() {
        saveEditItemLine();
        mBtnDelete.setVisibility(View.GONE);
        mAdapter.addAll(mList, -1);
    }

    /**
     * 保存当前编辑
     */
    private void saveEditItemLine() {
        isEdit = false;
        exitNailLineEdit();
        mThumbNailLine.setShowCurrentFalse();
        mCurrentEdit = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEditorHandler.unregisterEditorProgressListener(mPositionListener);
    }

    private VideoEditAloneActivity.ExitListener mExitListener;

    public void setExitListener(VideoEditAloneActivity.ExitListener exitListener) {
        this.mExitListener = exitListener;
    }

    //隐藏编辑框
    public void setHideEdit() {
        if (mThumbNailLine != null) {
            mThumbNailLine.setHideCurrent();
            mAdapter.addAll(mList, -1);
        }
    }

}
