package com.rd.veuisdk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.DewatermarkObject;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.IVideoEditorHandler.EditorPreivewPositionListener;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.adapter.MOAdapter;
import com.rd.veuisdk.crop.CropView;
import com.rd.veuisdk.demo.VideoEditAloneActivity;
import com.rd.veuisdk.fragment.helper.IFragmentHandler;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.MOInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.mvp.model.MOFragmentModel;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.util.ArrayList;

/**
 * 马赛克和水印
 * step1 :新增单个 -》选中样式 （位置  类型  ） 、apply (拉满时间轴) - --》>  播放（确认单个的时间线  是否取消 ）  ->完成 （放入recycleview ->单选单个可以重新编辑 ）
 */
public class OSDFragment extends RBaseFragment {

    public static OSDFragment newInstance() {
        OSDFragment subtitleFragment = new OSDFragment();
        Bundle bundle = new Bundle();
        subtitleFragment.setArguments(bundle);
        return subtitleFragment;
    }


    public OSDFragment() {
        super();
    }


    private Context mContext;

    private IVideoEditorHandler mEditorHandler;


    private VirtualVideo mVirtualVideo;

    /**
     * @param mISubtitle
     */
    public void setHandler(IFragmentHandler mISubtitle) {
        this.mISubtitle = mISubtitle;
    }

    private MOFragmentModel mFragmentModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "OSDFragment";
        mPageName = getString(R.string.dewatermark);
        mFragmentModel = new MOFragmentModel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mEditorHandler = (IVideoEditorHandler) context;
    }

    private RadioGroup mGroupMosaic;
    private View mStrengthLayout;
    private SeekBar mStrengthBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_osd_layout, container, false);
        mStrengthLayout = $(R.id.strengthLayout);
        mStrengthLayout.setVisibility(View.VISIBLE);
        mStrengthBar = $(R.id.sbarStrength);
        mStrengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                isFromUser = fromUser;
                if (isFromUser) {
                    if (null != mCurrentInfo && Math.abs(lastProgress - progress) > 5) {
                        lastProgress = progress;
                        mCurrentInfo.setValue(progress / (mStrengthBar.getMax() + 0.0f));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            private boolean isFromUser = false;

            private int lastProgress = -1;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isFromUser) {
                    if (null != mCurrentInfo) {
                        mCurrentInfo.setValue(seekBar.getProgress() / (mStrengthBar.getMax() + 0.0f));
                    }
                }
            }
        });
        initView();
        init();
        return mRoot;
    }

    /**
     * 底部显示
     */
    private void checkTitleLayout() {
        if (mMOInfoList.size() > 0 && !isMenuIng) {
            tvTitle.setVisibility(View.GONE);
            mReycleParent.setVisibility(View.VISIBLE);
        } else {
            mReycleParent.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkTitleLayout();
        mAdapter.addAll(mMOInfoList, tvAdded, -1);


        $(R.id.rb_blur).setOnClickListener(mOsdClickListener);
        $(R.id.rb_pixl).setOnClickListener(mOsdClickListener);
        $(R.id.rb_osd).setOnClickListener(mOsdClickListener);

        mPlayState.setOnClickListener(onStateChangeListener);
        mScrollView.addScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(mViewTouchListener);
    }

    @Override
    void onLeftClick() {
        int re = onBackPressed();
        if (re == 1) {
            mISubtitle.onBackPressed();
        }
    }


    @Override
    void onRightClick() {
        if (isMenuIng) {
            //时间线拉到末尾，从开始位置播放，确认结束时间点
            onSaveBtnItem(Utils.s2ms(mPlayer.getDuration()), false);
            if (isAddStep) {
                mSubtitleLine.showCurrent(mCurrentInfo.getId());
                mSubtitleLine.setHideCurrent();
                long start = mCurrentInfo.getStart();
//                此处不能用mCurrentInfo.getEnd()
                mSubtitleLine.update(mCurrentInfo.getId(), start, start + 5);
                playVideoImp();
            } else {
                mCurrentInfo = null;
                resetMenuUI();
                checkTitleLayout();
                mAdapter.addAll(mMOInfoList, tvAdded, BaseRVAdapter.UN_CHECK);
            }
        } else {
            if (null != mCurrentInfo) {
                //保存当前编辑 （等效于完成按钮）
                onEditSure();
            } else {
                onSurebtn();
                mISubtitle.onBackPressed();

            }
        }
    }

    private void onEditSure() {
        int[] arr = mSubtitleLine.getCurrent(mCurrentInfo.getId());
        if (null != arr) {
            RectF tmp = mCropView.getCropF();
            if (null != tmp && null != lastRectF && !tmp.equals(lastRectF)) {
                //位置有变化需要新生成对象
                mCurrentInfo.setShowRectF(new RectF(tmp));
            }
            lastRectF = null;
            //当前的开始结束点
            onCurrentEditSave(Utils.ms2s(arr[1]));
        } else {
            isAddStep = false;
            resetMenuUI();
            mSubtitleLine.setShowCurrentFalse();
            mCurrentInfo = null;
        }
    }


    /**
     * 马赛克、水印
     */
    private View.OnClickListener mOsdClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCurrentInfo.getObject().remove();
            int id = v.getId();
            if (id == R.id.rb_blur) { //高斯模糊
                mStrengthLayout.setVisibility(View.VISIBLE);
                onItemMosaicChecked(dbStyleList.get(0));
            } else if (id == R.id.rb_pixl) { //像素化
                mStrengthLayout.setVisibility(View.VISIBLE);
                onItemMosaicChecked(dbStyleList.get(1));
            } else if (id == R.id.rb_osd) { //去水印
                mStrengthLayout.setVisibility(View.INVISIBLE);
                onItemMosaicChecked(dbStyleList.get(2));
            }
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().quitEditCaptionMode(true);
            }
        }
    };


    private View mAddLayout, mMenuLayout;
    private ImageView mPlayState;
    private TimelineHorizontalScrollView mScrollView;
    private LinearLayout mIMediaLinearLayout;
    private ThumbNailLine mSubtitleLine;
    private ArrayList<MOInfo> mMOInfoList = new ArrayList<>(),
            mTempWordList = new ArrayList<>();
    private FrameLayout mLinearWords;
    private VirtualVideoView mPlayer;
    private View mViewWordHint;
    private View mDelete;  //删除
    private Button mTvAddSubtitle;//新增、完成 公用一个
    private Button mBtnCancelEdit; //取消、编辑共用一个
    private TextView mTvProgress;
    /**
     * 正在编辑中的字幕
     */
    private MOInfo mCurrentInfo;
    private boolean mIsUpdate = false;
    private int mLayoutWidth = 1024, mLayoutHeight = 1024;

    private IFragmentHandler mISubtitle;
    private int mStateSize = 0;
    private CropView mCropView;
    /**
     * 底部 添加列表 和 已添加提示
     */
    private ViewGroup mReycleParent;
    private RecyclerView mRecyclerView;
    private MOAdapter mAdapter;
    private TextView tvAdded;
    private TextView tvTitle;

    private void initView() {
        mGroupMosaic = $(R.id.rgMosaic);
        tvTitle = $(R.id.tvTitle);
        mReycleParent = $(R.id.recycleParent);
        tvAdded = $(R.id.tvAdded);
        mRecyclerView = $(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        tvTitle.setText(R.string.dewatermark);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MOAdapter(getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<MOInfo>() {
            @Override
            public void onItemClick(int position, MOInfo moInfo) {
                if (mEditorHandler.isPlaying()) {
                    mEditorHandler.pause();
                }
                //跳到指定位置
                mEditorHandler.seekTo((int) moInfo.getStart());
                onScrollProgress((int) moInfo.getStart());

                //移除播放器中的对象
                moInfo.getObject().remove();
                mPlayer.refresh();

                mDelete.setVisibility(View.VISIBLE);

                //编辑状态
                onEditWordImp(moInfo);

                mEditorHandler.seekTo((int) (moInfo.getEnd() + moInfo.getStart()) / 2);

            }
        });
        mAdapter.setThumb(new MOAdapter.IThumb() {
            @Override
            public int getThumb(int styleId) {
                return getStyleThumb(styleId);
            }
        });

        mRecyclerView.setAdapter(mAdapter);

        mTvAddSubtitle = $(R.id.btn_add_item);
        mBtnCancelEdit = $(R.id.btn_edit_item);
        mDelete = $(R.id.btn_del_item);
        mTvProgress = $(R.id.tvAddProgress);


        mAddLayout = $(R.id.add_layout);
        mMenuLayout = $(R.id.osd_menu_layout);

        mPlayState = $(R.id.ivPlayerState);
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);

        mTvAddSubtitle.setOnClickListener(onAddListener);
        mBtnCancelEdit.setOnClickListener(onCancelDelete);


        mScrollView = $(R.id.priview_subtitle_line);
        mScrollView.enableUserScrolling(true);

        mIMediaLinearLayout = $(R.id.subtitleline_media);
        mSubtitleLine = $(R.id.subline_view);
        mSubtitleLine.setEnableRepeat(true);

        mSubtitleLine.setSubtitleThumbNailListener(mThumbLineListener);
        mViewWordHint = $(R.id.word_hint_view);
        mDelete.setOnClickListener(mOnDeleteListener);
        mDisplay = CoreUtils.getMetrics();

    }


    /**
     * 退出编辑模式时，解绑回调并影藏控制器
     */
    private void unRegisterDrawRectListener() {
        mCropView.setVisibility(View.GONE);
    }


    /**
     * 删除已添加的MO
     *
     * @param moId
     */
    private MOInfo deleteItemImp(int moId) {

        //step1 从集合中删除
        int len = mMOInfoList.size();
        MOInfo deleteItem = null;
        for (int i = 0; i < len; i++) {
            if (mMOInfoList.get(i).getId() == moId) {
                deleteItem = mMOInfoList.remove(i);
                break;
            }
        }
        //step 2:  从时间轴和播放器中删除
        if (null != deleteItem) {
            mSubtitleLine.removeById(deleteItem.getId());
            deleteItem.getObject().remove();
            mPlayer.refresh();
        }
        checkTitleLayout();
        unRegisterDrawRectListener();
        return deleteItem;
    }

    /**
     * 恢复按钮的UI
     */
    private void resetMenuUI() {
        mTvAddSubtitle.setText(R.string.add);
        mBtnCancelEdit.setVisibility(View.GONE);
        mDelete.setVisibility(View.GONE);
    }

    //删除当前Item
    private View.OnClickListener mOnDeleteListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            pauseVideo();

            if (null != mCurrentInfo) {
                if (deleteItemImp(mCurrentInfo.getId()) != mCurrentInfo) {
                    mCurrentInfo.getObject().remove();
                    mPlayer.refresh();
                }
                mCurrentInfo = null;
            }
            //恢复UI
            mAdapter.addAll(mMOInfoList, tvAdded, BaseRVAdapter.UN_CHECK);
            resetMenuUI();

        }
    };

    private IViewTouchListener mViewTouchListener = new IViewTouchListener() {

        @Override
        public void onActionDown() {
            mEditorHandler.pause();
            if (isAddStep) {
            } else {
                int nprogress = mScrollView.getProgress();
                mEditorHandler.seekTo(nprogress);
                setProgressText(nprogress);
            }

        }

        @Override
        public void onActionMove() {
            int nprogress = mScrollView.getProgress();
            mEditorHandler.seekTo(nprogress);
            setProgressText(nprogress);
        }

        @Override
        public void onActionUp() {
            mScrollView.resetForce();
            int nprogress = mScrollView.getProgress();
            setProgressText(nprogress);
        }
    };

    @Override
    public void onDestroyView() {
        mScrollView.removeScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(null);
        onScrollProgress(0);
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bUIPrepared = false;
        mHandler.removeCallbacks(resetSubDataRunnable);
        if (mSubtitleLine != null) {
            mSubtitleLine.recycle(true);
        }
        mRoot = null;
    }


    private IThumbLineListener mThumbLineListener = new IThumbLineListener() {

        private int tempStart, tempEnd;

        @Override
        public void onTouchUp() {
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }
            float start = Utils.ms2s(tempStart);
            float end = Utils.ms2s(tempEnd);
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().setTimelineRange(start, end, true);
            }
        }

        @Override
        public void updateThumb(int id, int start, int end) {
            mIsUpdate = true;
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }
            tempStart = start;
            tempEnd = end;
        }


        @Override
        public void onCheckItem(boolean changed, int id) {
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }
//            int index = getIndex(id);
//            Log.e(TAG, "onCheckItem: " + index);
//            if (index >= 0 && index < mMOInfoList.size() - 1) {
//                MOInfo info = mMOInfoList.get(index);
//                mCurrentInfo = new MOInfo(info);
//                try {
//                    mCurrentInfo.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
//                } catch (InvalidArgumentException e) {
//                    e.printStackTrace();
//                }
//                if (changed) {
//                    initItemWord(mCurrentInfo);
//                }
//            }
        }
    };


    private DisplayMetrics mDisplay;
    private int mDuration = 1000;

    /**
     * 点击取消保存和重新进入时重置List
     */
    private void onListReset(boolean isInit) {
        mMOInfoList.clear();
        if (isInit) {
            mTempWordList.clear();
        }
        ArrayList<MOInfo> tempList = TempVideoParams.getInstance()
                .getMosaicDuraionChecked();
        mMOInfoList.clear();
        int len = tempList.size();
        MOInfo tmp;
        for (int i = 0; i < len; i++) {
            tmp = tempList.get(i);
            tmp.resetChanged();
            mMOInfoList.add(tmp);
            if (isInit) {
                mTempWordList.add(tmp.clone());
            }
        }

    }


    /**
     * 进入字幕的入口
     */
    private void init() {
        mVirtualVideo = mEditorHandler.getEditorVideo();
        mLinearWords = mEditorHandler.getSubEditorParent();
        mPlayer = mEditorHandler.getEditor();
        mDuration = mEditorHandler.getDuration();
        isAddStep = false;
        mLayoutWidth = mLinearWords.getWidth();
        mLayoutHeight = mLinearWords.getHeight();


        mCropView = new CropView(mContext);
        mCropView.setOverlayShadowColor(Color.TRANSPARENT);
        mCropView.setEnableDrawSelectionFrame(false);
        mCropView.setVisibility(View.GONE);
        mCropView.setTouchListener(new CropView.ITouchListener() {
            @Override
            public void onTouchDown() {
            }

            @Override
            public void onTouchUp() {
                if (null != mCurrentInfo) {
                    mCurrentInfo.setShowRectF(mCropView.getCropF());
                    mCurrentInfo.getObject().quitEditCaptionMode(true);
                }

            }
        });

        mLinearWords.addView(mCropView);
        mViewWordHint.setVisibility(View.VISIBLE);
        mMenuLayout.setVisibility(View.GONE);
        mAddLayout.setVisibility(View.VISIBLE);
        onListReset(true);
        onInitThumbTimeLine();
        setImage(R.drawable.edit_music_play);
        mTvAddSubtitle.setText(R.string.add);

        if (null != mEditorHandler) {
            mEditorHandler.registerEditorPostionListener(mEditorPreivewPositionListener);
//            mEditorHandler.reload(false); 191111 ,新版UI就不必再reload，
            if (!bUIPrepared) {  //防止后台切回前台，字幕轴重新初始化
                onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());
            }
        }
        mSubtitleLine.setCantouch(true);
        mSubtitleLine.setMoveItem(true);

        //获取样式
        mFragmentModel.getData(getContext(), dbStyleList);

    }


    private EditorPreivewPositionListener mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

        @Override
        public void onEditorPreviewComplete() {
            onScrollCompleted();
            if (mTvAddSubtitle.getText().toString().equals(mContext.getString(R.string.complete))) {
                //保存当前
                onCurrentSave(mPlayer.getDuration());

                //滑动UI到开始时刻（与播放器预览效果一致）
                mEditorHandler.seekTo(0);
                onScrollProgress(0);
            }
        }

        @Override
        public void onEditorPrepred() {
            if (null != mEditorHandler) { // 清除另一种方式下的字幕,取消loading...
                mEditorHandler.cancelLoading();
            }
            if (!bUIPrepared) {
                //防止后台切回前台，字幕轴重新初始化
//                onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());
            }
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
            if (null != mCurrentInfo && mCurrentInfo.getStart() < nPosition) {
                mSubtitleLine.update(mCurrentInfo.getId(), mCurrentInfo.getStart(), nPosition);
            }

        }
    };

    private int[] mSizeParams;

    private int mHalfWidth = 0;

    private void onInitThumbTimeLine() {

        mHalfWidth = mDisplay.widthPixels / 2;
        mScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        mSizeParams = mSubtitleLine.setDuration(mDuration, mScrollView.getHalfParentWidth());
        mScrollView.setLineWidth(mSizeParams[0]);
        mScrollView.setDuration(mDuration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mSizeParams[0]
                + 2 * mSubtitleLine.getpadding(), mSizeParams[1]);

        lp.setMargins(mScrollView.getHalfParentWidth() - mSubtitleLine.getpadding(),
                0, mHalfWidth - mSubtitleLine.getpadding(), 0);

        mSubtitleLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new FrameLayout.LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);

        mIMediaLinearLayout.setLayoutParams(lframe);
        mViewWordHint.setVisibility(View.GONE);

    }

    /**
     * UI数据恢复成功
     */
    private boolean bUIPrepared = false;


    private boolean bExMode = false;//是否增强模式取缩略图 （耗时）

    /**
     * 初始化缩略图时间轴和恢复字幕数据
     *
     * @param virtualVideo
     */
    private void onInitThumbTimeLine(VirtualVideo virtualVideo) {
        mSubtitleLine.setVirtualVideo(virtualVideo, bExMode);
        mSubtitleLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        onScrollProgress(0);
        mHandler.postDelayed(resetSubDataRunnable, 100);
    }


    private StyleInfo getStyleInfo(int styleId) {
        return getStyleInfo(dbStyleList, styleId);
    }

    /**
     * 获取封面
     *
     * @param styleId
     * @return
     */
    private int getStyleThumb(int styleId) {
        StyleInfo info = getStyleInfo(dbStyleList, styleId);
        if (null != info) {
            if (info.getType() == DewatermarkObject.Type.mosaic) {
                return R.drawable.mosaic_square_n;
            } else if (info.getType() == DewatermarkObject.Type.blur) {
                return R.drawable.mosaic_blur_n;
            } else {
                return R.drawable.osd_square_n;
            }
        }
        return R.drawable.mosaic_square_n;
    }

    private StyleInfo getStyleInfo(ArrayList<StyleInfo> list, int styleId) {
        StyleInfo styleInfo = null;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            StyleInfo tmp = list.get(i);
            if (tmp.pid == styleId) {
                styleInfo = tmp;
                break;
            }
        }
        return styleInfo;

    }


    //恢复数据
    private Runnable resetSubDataRunnable = new Runnable() {

        @Override
        public void run() {
            ArrayList<SubInfo> sublist = new ArrayList<>();
            int len = mMOInfoList.size();
            for (int i = 0; i < len; i++) {
                sublist.add(new SubInfo(mMOInfoList.get(i)));
            }
            mSubtitleLine.prepareData(sublist);
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            bUIPrepared = true;
        }
    };


    private void setProgressText(int progress) {
        mTvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
        mSubtitleLine.setDuration(progress);
        mAdapter.setDuration(progress);
    }

    private ScrollViewListener mThumbOnScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY,
                                     boolean appScroll) {
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            int nprogress = mScrollView.getProgress();
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.seekTo(nprogress);
                }
                setProgressText(nprogress);
            }
        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY,
                                boolean appScroll) {
            int nprogress = mScrollView.getProgress();
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.seekTo(nprogress);
                }
                if (null != mCurrentInfo && (mCurrentInfo.getEnd() < nprogress || mCurrentInfo.getStart() > nprogress)) {
                    //完成当前编辑的项
                    onCurrentSave(Utils.ms2s(mCurrentInfo.getEnd()));
                }
            }
            setProgressText(nprogress);

        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY,
                                  boolean appScroll) {
            int nprogress = mScrollView.getProgress();
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                    mEditorHandler.seekTo(nprogress);
                }
                setProgressText(nprogress);
                if (isAddStep) { //强制结束
                    if (null != mCurrentInfo) {
                        onCurrentSave(mPlayer.getCurrentPosition());
                    }
                }
            }
        }
    };

    /**
     * 新增true，编辑false
     */
    private boolean isAddStep = false;


    /**
     * 是否编辑当个item中 （新增或编辑时的状态）
     */
    private boolean isMenuIng = false;


    /**
     * 开始添加字幕
     */
    private View.OnClickListener onAddListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (dbStyleList.size() < 3) {
                Log.e(TAG, "onAddListener: recovering sub data ...");
                return;
            }
            if (mEditorHandler != null && mEditorHandler.isPlaying()) {
                pauseVideo();
            }

            String menu = mTvAddSubtitle.getText().toString();
            /**
             * 新增
             */
            if (menu.equals(mContext.getString(R.string.add))) {

                if (null != mCurrentInfo) {
                    //先保存当前编辑
                    onEditSure();
                }

                // 判断该区域能否添加

                int progress = mScrollView.getProgress();
                isAddStep = true;
                mCurrentInfo = new MOInfo();
                try {
                    mCurrentInfo.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                mCurrentInfo.setId(Utils.getWordId());

                //默认添加到整条时间线，预览时通过GL实现预览,所以必须设置一个大于当前时刻的结束点，待保存时重新修正结束点
                mCurrentInfo.setTimelineRange(progress, Utils.s2ms(mPlayer.getDuration()), false);
                mSubtitleLine.addRect(progress, progress + 10, "", mCurrentInfo.getId());
                mTvAddSubtitle.setText(R.string.complete);
                StyleInfo styleInfo = dbStyleList.get(0);
                mCurrentInfo.setStyleId(styleInfo.pid);
                onStartSub();


                mCurrentInfo.getObject().quitEditCaptionMode(true);

                saveToList(mCurrentInfo);


            } else if (menu.equals(mContext.getString(R.string.complete))) {
                if (null != mCurrentInfo) {
                    onCurrentSave(mPlayer.getCurrentPosition());
                }


            }

        }
    };
    private int[] rgRbIds = new int[]{R.id.rb_blur, R.id.rb_pixl, R.id.rb_osd};

    /**
     * 恢复选中的效果
     *
     * @param styleId
     */
    private void resetGroupRB(int styleId) {
        int index = getStyleIndex(styleId);
        if (index >= 0 && index < rgRbIds.length) {
            mGroupMosaic.check(rgRbIds[index]);
        }
        if (mGroupMosaic.getCheckedRadioButtonId() != R.id.rb_osd) {
            mStrengthLayout.setVisibility(View.VISIBLE);
        }
    }

    /***
     * 保存（完成）当前
     * @param end  结束时间点 单位：S
     */
    private void onCurrentSave(float end) {
        if (null != mCurrentInfo) {
            pauseVideo();
            mCurrentInfo.setEnd(Utils.s2ms(end));
            if (mCurrentInfo.getStart() < mCurrentInfo.getEnd()) {
                mSubtitleLine.update(mCurrentInfo.getId(), mCurrentInfo.getStart(), mCurrentInfo.getEnd());
                mSubtitleLine.setShowCurrentFalse();
                saveToList(mCurrentInfo);
            } else {
                mSubtitleLine.removeById(mCurrentInfo.getId());
            }
            checkTitleLayout();
            mAdapter.addAll(mMOInfoList, tvAdded, BaseRVAdapter.UN_CHECK);
            mCurrentInfo = null;
        }
        isAddStep = false;
        mCropView.setVisibility(View.GONE);
        resetMenuUI();
    }

    /***
     * 当前编辑的项保存
     * @param end
     */
    private void onCurrentEditSave(float end) {
        if (null != mCurrentInfo) {
            pauseVideo();
            mCurrentInfo.setEnd(Utils.s2ms(end), false);
            mSubtitleLine.update(mCurrentInfo.getId(), mCurrentInfo.getStart(), mCurrentInfo.getEnd());
            mSubtitleLine.setShowCurrentFalse();
            saveInfo(false);
            checkTitleLayout();
            mAdapter.addAll(mMOInfoList, tvAdded, -1);
            mCurrentInfo = null;
        }
        isAddStep = false;
        resetMenuUI();
    }

    private View.OnClickListener onCancelDelete = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            pauseVideo();
            //取消
            if (null != mCurrentInfo) {
                if (deleteItemImp(mCurrentInfo.getId()) != mCurrentInfo) {
                    mCurrentInfo.getObject().remove();
                    mPlayer.refresh();
                }
                mCurrentInfo = null;
            }
            //恢复按钮状态
            resetMenuUI();
        }
    };


    /**
     * 编辑已经存在的元素
     */
    private void onEditWordImp(MOInfo info) {
        if (null != info) {
            isAddStep = false;
            //编辑时，构造新对象
            mCurrentInfo = new MOInfo(info);
            //当前编辑项高亮
            mSubtitleLine.showCurrent(mCurrentInfo.getId());
            try {
                mCurrentInfo.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            onEditItemUI();
        }
    }


    /**
     * @param nPos       真实的时间线  单位：ms
     * @param isRealLine 时间轴上是否显示真实的时间线
     */
    private void onWordEnd(int nPos, boolean isRealLine) {

        if (null == mCurrentInfo) {
            return;
        }
        mSubtitleLine.setIsAdding(isAddStep);
        pauseVideo();
        if (-1 == nPos) {
            //点击完成->确认按钮，修改captionObject中的时间线
            int[] mrect = mSubtitleLine.getCurrent(mCurrentInfo.getId());
            if (null != mrect) {
                mCurrentInfo.setTimelineRange(mrect[0], mrect[1], false);
            }
        } else {
            //这里的false ,是为了防止设置时间线更新了，退出编辑时（getObject().quitEditCaptionMode(true);）显示Core中马赛克|水印时再次更新
            mCurrentInfo.setEnd(nPos < 0 ? mEditorHandler.getCurrentPosition() : nPos, false);
        }
        onSaveToList(false);
        int start = (int) mCurrentInfo.getStart();
        mSubtitleLine.replace(mCurrentInfo.getId(), start, isRealLine ? (int) mCurrentInfo.getEnd() : (start + 10));
        boolean hasExit = checkExit(mCurrentInfo.getId());
        if (hasExit) {
            mSubtitleLine.replace(mCurrentInfo.getId(), "");
        } else {
            mSubtitleLine.removeById(mCurrentInfo.getId());
        }
    }

    /**
     * 调整播放那个状态
     */
    private View.OnClickListener onStateChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mEditorHandler.isPlaying()) {
                pauseVideo();
            } else {
                if (Math.abs(mEditorHandler.getCurrentPosition() - mEditorHandler.getDuration()) < 300) {
                    mEditorHandler.seekTo(0);
                }
                //保存当前编辑的状态
                if (null != mCurrentInfo && !isAddStep) {
                    onEditSure();
                }
                playVideo();
            }

        }
    };

    private void playVideo() {
        if (null != mCurrentInfo) {
            //退出时保存控制器的位置
            mCurrentInfo.setShowRectF(mCropView.getCropF());
            mCurrentInfo.getObject().quitEditCaptionMode(true);
        }
        playVideoImp();
    }

    private void playVideoImp() {
        mEditorHandler.start();
        setImage(R.drawable.edit_music_pause);

    }

    private void pauseVideo() {
        mEditorHandler.pause();
        setImage(R.drawable.edit_music_play);

    }

    /**
     * 检测该id之前是否存在于字幕列表
     *
     * @param id
     * @return
     */
    private boolean checkExit(int id) {
        MOInfo temp;
        boolean hasExit = false;
        for (int i = 0; i < mMOInfoList.size(); i++) {
            temp = mMOInfoList.get(i);
            if (temp.getId() == id) {
                hasExit = true;
                break;
            }
        }
        return hasExit;
    }

    /**
     * 点击menu_layout 中得保存(保存位置)，继续播放
     */
    private void onSaveBtnItem(int end, boolean isShowRealLine) {
        SysAlertDialog.showLoadingDialog(mContext, R.string.isloading);
        onSaveBtnItemImp(end, isShowRealLine);

    }

    /***
     * 保存单个mo
     */
    private void onSaveBtnItemImp(int end, boolean isShowRealLine) {
        mCurrentInfo.setShowRectF(mCropView.getCropF());
        if (isAddStep) {
            //新增（保存）
            unRegisterDrawRectListener();
            int start = (int) mCurrentInfo.getStart();
            onWordEnd(end, isShowRealLine);  //特别处理时间线
            //播放器内容改变,刷新播放器
            mPlayer.refresh();
            mEditorHandler.seekTo(start);
            mTvAddSubtitle.setText(R.string.complete);
            mBtnCancelEdit.setText(R.string.cancel);
            mBtnCancelEdit.setVisibility(View.VISIBLE);
        } else {
            mSubtitleLine.setShowCurrentFalse();
            //编辑（保存）
            onSaveToList(false);
            //播放器内容改变,刷新播放器
            mPlayer.refresh();
        }
        onMenuViewOnBackpressed();
        isMenuIng = false;
        SysAlertDialog.cancelLoadingDialog();
    }

    /**
     * 不管时间线和位置有没变化，一律生成新的 ( apply(true) )
     *
     * @param needStart
     */
    private void saveInfo(boolean needStart) {
        if (null != mCurrentInfo) {
            //退出编辑模式
            mCurrentInfo.getObject().quitEditCaptionMode(true);
            //隐藏UI层的控制按钮
            unRegisterDrawRectListener();
            if (needStart) {
                if (mEditorHandler != null) {
                    mEditorHandler.stop();
                    mEditorHandler.start();
                }
            }
            saveToList(mCurrentInfo);
        }

    }

    /**
     * 保存到集合
     *
     * @param info
     */
    private void saveToList(MOInfo info) {
        int re = getIndex(info.getId());
        if (re > -1) {
            mMOInfoList.set(re, info); // 重新编辑
        } else {
            mMOInfoList.add(info); // 新增
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
        for (int i = 0; i < mMOInfoList.size(); i++) {
            if (id == mMOInfoList.get(i).getId()) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 样式下标
     *
     * @param styleId
     * @return
     */
    private int getStyleIndex(int styleId) {
        int index = -1;
        int len = dbStyleList.size();
        for (int i = 0; i < len; i++) {
            if (styleId == dbStyleList.get(i).pid) {
                index = i;
                break;
            }
        }
        return index;
    }


    /**
     * 开始新增
     */
    private void onStartSub() {
        isMenuIng = true;
        if (mEditorHandler != null) {
            mEditorHandler.pause();
        }
        mAddLayout.setVisibility(View.GONE);


        //被选中的样式
        resetGroupRB(mCurrentInfo.getStyleId());

        mMenuLayout.setVisibility(View.VISIBLE);
        initItemWord(mCurrentInfo);
        checkTitleLayout();

    }

    private RectF lastRectF = null;

    /**
     * 开始编辑字幕
     */
    private void onEditItemUI() {
        //记录之前的显示位置
        lastRectF = mCurrentInfo.getShowRectF();
        initItemWord(mCurrentInfo);
        checkTitleLayout();
        mCurrentInfo.getObject().quitEditCaptionMode(true);
    }


    /**
     * 单个编辑字幕
     *
     * @param info
     */
    private void initItemWord(MOInfo info) {
        //应用样式
        setCommonStyleImp(getStyleInfo(info.getStyleId()), info);
    }

    /***
     * 设置样式
     * @param si
     * @param info
     */
    private void setCommonStyleImp(StyleInfo si, MOInfo info) {
        RectF clip = null;
        RectF showRect = info.getShowRectF();
        if (showRect.isEmpty()) {
            //新增
            clip = new RectF(si.mShowRectF);
            try {   //指定类型
                info.getObject().setMORectF(si.getType(), si.mShowRectF);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else { //编辑
            try {
                info.getObject().setMOType(si.getType());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
            clip = new RectF(showRect);
        }
        fixClipPx(clip);
        mCropView.initialize(clip, new RectF(0, 0, mLayoutWidth, mLayoutHeight), 0);
        mCropView.setVisibility(View.VISIBLE);

    }

    /**
     * @param clip
     */
    private void fixClipPx(RectF clip) {
        clip.left *= mLayoutWidth;
        clip.top *= mLayoutHeight;
        clip.right *= mLayoutWidth;
        clip.bottom *= mLayoutHeight;
    }


    private ArrayList<StyleInfo> dbStyleList = new ArrayList<>();


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                default:
                    break;
            }
        }

        ;
    };


    /**
     * 播放控制器图标
     */
    public void setImage(int resId) {
        if (isRunning && null != mPlayState) {
            mPlayState.setImageResource(resId);
        }
    }

    /**
     * 从menu->addlayout
     */
    private void onMenuBackPressed() {
        if (null != mCurrentInfo) {
            mSubtitleLine.removeById(mCurrentInfo.getId());
            mCurrentInfo.getObject().remove();
            int index = getIndex(mCurrentInfo.getId());
            if (index >= 0) {
                MOInfo moInfo = mMOInfoList.remove(index);
                if (moInfo != mCurrentInfo) {
                    moInfo.getObject().remove();
                }
            }
            mCurrentInfo = null;
            mPlayer.refresh();
        }
        onMenuViewOnBackpressed();
        mViewTouchListener.onActionUp();
        unRegisterDrawRectListener();
    }

    /**
     * 点击上级界面（场景：正在编辑字幕样式，(1点击保存按钮,2点击返回))
     */
    private void onMenuViewOnBackpressed() {
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
        }
        if (mAddLayout.getVisibility() != View.VISIBLE) {
            mAddLayout.setVisibility(View.VISIBLE);
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
                        if (mExitListener != null) {
                            mExitListener.exit(1);
                        }
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
        if (mExitListener != null) {
            if (save) {
                onSaveToList(true);
                if (null != mCurrentInfo) {
                    mCurrentInfo.getObject().quitEditCaptionMode(true);
                }
                mEditorHandler.onSure();
            } else {
                mEditorHandler.onBack();
            }
            return;
        }
        mIsUpdate = false;
        if (save) {
            onSaveToList(true);
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().quitEditCaptionMode(true);
            }
            mEditorHandler.onSure();
        } else {
            unRegisterDrawRectListener();
            if (null != mCurrentInfo) {
                mCurrentInfo.getObject().quitEditCaptionMode(false);
            }
            int len = mMOInfoList.size();
            for (int n = 0; n < len; n++) {
                mMOInfoList.get(n).set(mTempWordList.get(n));
            }
            mEditorHandler.onBack();
        }

        mAddLayout.setVisibility(View.GONE);
        if (null != mSubtitleLine) {
            mSubtitleLine.recycle();
        }
        onScrollTo(0);
        setProgressText(0);
        mEditorHandler.unregisterEditorProgressListener(mEditorPreivewPositionListener);
        mSubtitleLine.clearAll();
        bUIPrepared = false;

    }

    /**
     * 保存当前编辑字幕到集合 （完成按钮，播放按钮控制）
     */
    private void onSaveToList(boolean clearCurrent) {
        unRegisterDrawRectListener();
        saveInfo(clearCurrent);
        if (clearCurrent)
            mSubtitleLine.clearCurrent();
    }

    /**
     * 点击完成
     */
    public void onSurebtn() {
        onBackToActivity(true);
        TempVideoParams.getInstance().setMosaics(mMOInfoList);
    }

    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {
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

    /**
     * 切换样式
     *
     * @param info
     */
    private void onItemMosaicChecked(StyleInfo info) {
        if (null != mCurrentInfo && null != info) {
            mCurrentInfo.setStyleId(info.pid);
            RectF rectF = mCropView.getCropF();
            if (!rectF.isEmpty()) {
                //保存当前的矩形位置
                mCurrentInfo.setShowRectF(rectF);
            }
            initItemWord(mCurrentInfo);
        }
    }


    @Override
    public int onBackPressed() {
        if (isMenuIng) {
            onMenuBackPressed();
            isMenuIng = false;
            mAdapter.addAll(mMOInfoList, tvAdded, BaseRVAdapter.UN_CHECK);
            resetMenuUI();
            checkTitleLayout();
            isAddStep = false;
            return -1;
        } else {
            if (!CommonStyleUtils.isEqualsMosaic(mMOInfoList, TempVideoParams.getInstance().getMosaicDuraionChecked())
                    || mIsUpdate) {
                onShowAlert();
                return 0;
            } else {
                onBackToActivity(false);
                return 1;
            }
        }
    }

    private VideoEditAloneActivity.ExitListener mExitListener;

    public void setExitListener(VideoEditAloneActivity.ExitListener exitListener) {
        this.mExitListener = exitListener;
    }

    //隐藏编辑框
    public void setHideEdit() {
        if (mSubtitleLine != null) {
            mSubtitleLine.setHideCurrent();
            mAdapter.addAll(mMOInfoList, tvAdded, -1);
        }
    }

}
