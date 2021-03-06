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
import androidx.recyclerview.widget.RecyclerView;
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
 * ??????????????????
 * step1 :???????????? -??????????????? ?????????  ??????  ??? ???apply (???????????????) - --???>  ?????????????????????????????????  ???????????? ???  ->?????? ?????????recycleview ->?????????????????????????????? ???
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
     * ????????????
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
            //?????????????????????????????????????????????????????????????????????
            onSaveBtnItem(Utils.s2ms(mPlayer.getDuration()), false);
            if (isAddStep) {
                mSubtitleLine.showCurrent(mCurrentInfo.getId());
                mSubtitleLine.setHideCurrent();
                long start = mCurrentInfo.getStart();
//                ???????????????mCurrentInfo.getEnd()
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
                //?????????????????? ???????????????????????????
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
                //????????????????????????????????????
                mCurrentInfo.setShowRectF(new RectF(tmp));
            }
            lastRectF = null;
            //????????????????????????
            onCurrentEditSave(Utils.ms2s(arr[1]));
        } else {
            isAddStep = false;
            resetMenuUI();
            mSubtitleLine.setShowCurrentFalse();
            mCurrentInfo = null;
        }
    }


    /**
     * ??????????????????
     */
    private View.OnClickListener mOsdClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCurrentInfo.getObject().remove();
            int id = v.getId();
            if (id == R.id.rb_blur) { //????????????
                mStrengthLayout.setVisibility(View.VISIBLE);
                onItemMosaicChecked(dbStyleList.get(0));
            } else if (id == R.id.rb_pixl) { //?????????
                mStrengthLayout.setVisibility(View.VISIBLE);
                onItemMosaicChecked(dbStyleList.get(1));
            } else if (id == R.id.rb_osd) { //?????????
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
    private View mDelete;  //??????
    private Button mTvAddSubtitle;//??????????????? ????????????
    private Button mBtnCancelEdit; //???????????????????????????
    private TextView mTvProgress;
    /**
     * ????????????????????????
     */
    private MOInfo mCurrentInfo;
    private boolean mIsUpdate = false;
    private int mLayoutWidth = 1024, mLayoutHeight = 1024;

    private IFragmentHandler mISubtitle;
    private int mStateSize = 0;
    private CropView mCropView;
    /**
     * ?????? ???????????? ??? ???????????????
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
        //?????????????????????item???????????????????????????????????????
        tvTitle.setText(R.string.dewatermark);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MOAdapter(getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<MOInfo>() {
            @Override
            public void onItemClick(int position, MOInfo moInfo) {
                if (mEditorHandler.isPlaying()) {
                    mEditorHandler.pause();
                }
                //??????????????????
                mEditorHandler.seekTo((int) moInfo.getStart());
                onScrollProgress((int) moInfo.getStart());

                //???????????????????????????
                moInfo.getObject().remove();
                mPlayer.refresh();

                mDelete.setVisibility(View.VISIBLE);

                //????????????
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
     * ??????????????????????????????????????????????????????
     */
    private void unRegisterDrawRectListener() {
        mCropView.setVisibility(View.GONE);
    }


    /**
     * ??????????????????MO
     *
     * @param moId
     */
    private MOInfo deleteItemImp(int moId) {

        //step1 ??????????????????
        int len = mMOInfoList.size();
        MOInfo deleteItem = null;
        for (int i = 0; i < len; i++) {
            if (mMOInfoList.get(i).getId() == moId) {
                deleteItem = mMOInfoList.remove(i);
                break;
            }
        }
        //step 2:  ?????????????????????????????????
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
     * ???????????????UI
     */
    private void resetMenuUI() {
        mTvAddSubtitle.setText(R.string.add);
        mBtnCancelEdit.setVisibility(View.GONE);
        mDelete.setVisibility(View.GONE);
    }

    //????????????Item
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
            //??????UI
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
     * ??????????????????????????????????????????List
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
     * ?????????????????????
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
//            mEditorHandler.reload(false); 191111 ,??????UI????????????reload???
            if (!bUIPrepared) {  //???????????????????????????????????????????????????
                onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());
            }
        }
        mSubtitleLine.setCantouch(true);
        mSubtitleLine.setMoveItem(true);

        //????????????
        mFragmentModel.getData(getContext(), dbStyleList);

    }


    private EditorPreivewPositionListener mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

        @Override
        public void onEditorPreviewComplete() {
            onScrollCompleted();
            if (mTvAddSubtitle.getText().toString().equals(mContext.getString(R.string.complete))) {
                //????????????
                onCurrentSave(mPlayer.getDuration());

                //??????UI???????????????????????????????????????????????????
                mEditorHandler.seekTo(0);
                onScrollProgress(0);
            }
        }

        @Override
        public void onEditorPrepred() {
            if (null != mEditorHandler) { // ?????????????????????????????????,??????loading...
                mEditorHandler.cancelLoading();
            }
            if (!bUIPrepared) {
                //???????????????????????????????????????????????????
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
     * UI??????????????????
     */
    private boolean bUIPrepared = false;


    private boolean bExMode = false;//?????????????????????????????? ????????????

    /**
     * ????????????????????????????????????????????????
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
     * ????????????
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


    //????????????
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
                    //????????????????????????
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
                if (isAddStep) { //????????????
                    if (null != mCurrentInfo) {
                        onCurrentSave(mPlayer.getCurrentPosition());
                    }
                }
            }
        }
    };

    /**
     * ??????true?????????false
     */
    private boolean isAddStep = false;


    /**
     * ??????????????????item??? ?????????????????????????????????
     */
    private boolean isMenuIng = false;


    /**
     * ??????????????????
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
             * ??????
             */
            if (menu.equals(mContext.getString(R.string.add))) {

                if (null != mCurrentInfo) {
                    //?????????????????????
                    onEditSure();
                }

                // ???????????????????????????

                int progress = mScrollView.getProgress();
                isAddStep = true;
                mCurrentInfo = new MOInfo();
                try {
                    mCurrentInfo.getObject().setVirtualVideo(mVirtualVideo, mPlayer);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
                mCurrentInfo.setId(Utils.getWordId());

                //????????????????????????????????????????????????GL????????????,??????????????????????????????????????????????????????????????????????????????????????????
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
     * ?????????????????????
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
     * ????????????????????????
     * @param end  ??????????????? ?????????S
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
     * ????????????????????????
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
            //??????
            if (null != mCurrentInfo) {
                if (deleteItemImp(mCurrentInfo.getId()) != mCurrentInfo) {
                    mCurrentInfo.getObject().remove();
                    mPlayer.refresh();
                }
                mCurrentInfo = null;
            }
            //??????????????????
            resetMenuUI();
        }
    };


    /**
     * ???????????????????????????
     */
    private void onEditWordImp(MOInfo info) {
        if (null != info) {
            isAddStep = false;
            //???????????????????????????
            mCurrentInfo = new MOInfo(info);
            //?????????????????????
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
     * @param nPos       ??????????????????  ?????????ms
     * @param isRealLine ??????????????????????????????????????????
     */
    private void onWordEnd(int nPos, boolean isRealLine) {

        if (null == mCurrentInfo) {
            return;
        }
        mSubtitleLine.setIsAdding(isAddStep);
        pauseVideo();
        if (-1 == nPos) {
            //????????????->?????????????????????captionObject???????????????
            int[] mrect = mSubtitleLine.getCurrent(mCurrentInfo.getId());
            if (null != mrect) {
                mCurrentInfo.setTimelineRange(mrect[0], mrect[1], false);
            }
        } else {
            //?????????false ,????????????????????????????????????????????????????????????getObject().quitEditCaptionMode(true);?????????Core????????????|?????????????????????
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
     * ????????????????????????
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
                //???????????????????????????
                if (null != mCurrentInfo && !isAddStep) {
                    onEditSure();
                }
                playVideo();
            }

        }
    };

    private void playVideo() {
        if (null != mCurrentInfo) {
            //?????????????????????????????????
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
     * ?????????id?????????????????????????????????
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
     * ??????menu_layout ????????????(????????????)???????????????
     */
    private void onSaveBtnItem(int end, boolean isShowRealLine) {
        SysAlertDialog.showLoadingDialog(mContext, R.string.isloading);
        onSaveBtnItemImp(end, isShowRealLine);

    }

    /***
     * ????????????mo
     */
    private void onSaveBtnItemImp(int end, boolean isShowRealLine) {
        mCurrentInfo.setShowRectF(mCropView.getCropF());
        if (isAddStep) {
            //??????????????????
            unRegisterDrawRectListener();
            int start = (int) mCurrentInfo.getStart();
            onWordEnd(end, isShowRealLine);  //?????????????????????
            //?????????????????????,???????????????
            mPlayer.refresh();
            mEditorHandler.seekTo(start);
            mTvAddSubtitle.setText(R.string.complete);
            mBtnCancelEdit.setText(R.string.cancel);
            mBtnCancelEdit.setVisibility(View.VISIBLE);
        } else {
            mSubtitleLine.setShowCurrentFalse();
            //??????????????????
            onSaveToList(false);
            //?????????????????????,???????????????
            mPlayer.refresh();
        }
        onMenuViewOnBackpressed();
        isMenuIng = false;
        SysAlertDialog.cancelLoadingDialog();
    }

    /**
     * ????????????????????????????????????????????????????????? ( apply(true) )
     *
     * @param needStart
     */
    private void saveInfo(boolean needStart) {
        if (null != mCurrentInfo) {
            //??????????????????
            mCurrentInfo.getObject().quitEditCaptionMode(true);
            //??????UI??????????????????
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
     * ???????????????
     *
     * @param info
     */
    private void saveToList(MOInfo info) {
        int re = getIndex(info.getId());
        if (re > -1) {
            mMOInfoList.set(re, info); // ????????????
        } else {
            mMOInfoList.add(info); // ??????
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param id ???????????????Id
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
     * ????????????
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
     * ????????????
     */
    private void onStartSub() {
        isMenuIng = true;
        if (mEditorHandler != null) {
            mEditorHandler.pause();
        }
        mAddLayout.setVisibility(View.GONE);


        //??????????????????
        resetGroupRB(mCurrentInfo.getStyleId());

        mMenuLayout.setVisibility(View.VISIBLE);
        initItemWord(mCurrentInfo);
        checkTitleLayout();

    }

    private RectF lastRectF = null;

    /**
     * ??????????????????
     */
    private void onEditItemUI() {
        //???????????????????????????
        lastRectF = mCurrentInfo.getShowRectF();
        initItemWord(mCurrentInfo);
        checkTitleLayout();
        mCurrentInfo.getObject().quitEditCaptionMode(true);
    }


    /**
     * ??????????????????
     *
     * @param info
     */
    private void initItemWord(MOInfo info) {
        //????????????
        setCommonStyleImp(getStyleInfo(info.getStyleId()), info);
    }

    /***
     * ????????????
     * @param si
     * @param info
     */
    private void setCommonStyleImp(StyleInfo si, MOInfo info) {
        RectF clip = null;
        RectF showRect = info.getShowRectF();
        if (showRect.isEmpty()) {
            //??????
            clip = new RectF(si.mShowRectF);
            try {   //????????????
                info.getObject().setMORectF(si.getType(), si.mShowRectF);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } else { //??????
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
     * ?????????????????????
     */
    public void setImage(int resId) {
        if (isRunning && null != mPlayState) {
            mPlayState.setImageResource(resId);
        }
    }

    /**
     * ???menu->addlayout
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
     * ?????????????????????????????????????????????????????????(1??????????????????,2????????????))
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
     * ??????
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
     * ????????????????????????????????? ???????????????????????????????????????
     */
    private void onSaveToList(boolean clearCurrent) {
        unRegisterDrawRectListener();
        saveInfo(clearCurrent);
        if (clearCurrent)
            mSubtitleLine.clearCurrent();
    }

    /**
     * ????????????
     */
    public void onSurebtn() {
        onBackToActivity(true);
        TempVideoParams.getInstance().setMosaics(mMOInfoList);
    }

    /**
     * ??????????????????
     *
     * @param progress (??????ms)
     */
    private void onScrollProgress(int progress) {
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }

    private int getScrollX(long progress) {
        return (int) (progress * (mSubtitleLine.getThumbWidth() / mDuration));
    }

    /**
     * ????????????
     */
    private void onScrollCompleted() {
        onScrollTo((int) mSubtitleLine.getThumbWidth());
        setProgressText(mDuration);
        mPlayState.setImageResource(R.drawable.edit_music_play);
    }

    /**
     * ??????????????????
     *
     * @param mScrollX
     */
    private void onScrollTo(int mScrollX) {
        mScrollView.appScrollTo(mScrollX, true);
    }

    /**
     * ????????????
     *
     * @param info
     */
    private void onItemMosaicChecked(StyleInfo info) {
        if (null != mCurrentInfo && null != info) {
            mCurrentInfo.setStyleId(info.pid);
            RectF rectF = mCropView.getCropF();
            if (!rectF.isEmpty()) {
                //???????????????????????????
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

    //???????????????
    public void setHideEdit() {
        if (mSubtitleLine != null) {
            mSubtitleLine.setHideCurrent();
            mAdapter.addAll(mMOInfoList, tvAdded, -1);
        }
    }

}
