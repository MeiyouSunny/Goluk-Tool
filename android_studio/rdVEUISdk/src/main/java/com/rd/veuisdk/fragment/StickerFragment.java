package com.rd.veuisdk.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.IVideoEditorHandler.EditorPreivewPositionListener;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.adapter.StickerAdapter;
import com.rd.veuisdk.adapter.StickerDataAdapter;
import com.rd.veuisdk.adapter.StickerSortAdapter;
import com.rd.veuisdk.adapter.StyleAdapter;
import com.rd.veuisdk.demo.VideoEditAloneActivity;
import com.rd.veuisdk.export.StickerExportHandler;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.IStickerSortApi;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.mvp.model.Sticker2FragmentModel;
import com.rd.veuisdk.mvp.model.StickerFragmentModel;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.ui.AppConfig;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.PopViewUtil;
import com.rd.veuisdk.ui.SinglePointRotate;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.apng.ApngImageLoader;
import com.rd.veuisdk.utils.apng.assist.PngImageLoader;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;

import java.util.ArrayList;
import java.util.List;

public class StickerFragment extends SSBaseFragment<StickerInfo, StickerAdapter, StickerFragmentModel> {

    private static final String PARAM_TYPE_URL = "param_type_url";
    private static final String PARAM_DATA_URL = "param_data_url";

    private FrameLayout mLinearWords;
    //????????????
    private boolean mIsUpdate = false;
    //?????????
    private int mHalfWidth = 0;
    private int mStateSize = 0; //?????????????????????
    //??????????????????
    private boolean mIsDownloading = false;
    //??????
    private String mTypeUrl;
    private String mDataUrl;
    //??????
    private RecyclerView mRvStickerSort;
    private StickerSortAdapter mSortAdapter;
    private Handler mHandler;
    //??????
    private RecyclerView mRvStickerData;
    private StickerDataAdapter mDataAdpter;
    //????????????????????????
    private boolean bDataPrepared = false;
    // ??????true?????????false
    private boolean mAddStep = false;
    //UI??????????????????(?????????????????????????????????????????????????????????)
    private boolean bUIPrepared = false;
    private Sticker2FragmentModel mStickerModel;
    //?????????????????????????????????????????????
    private String mCategory;
    private boolean mEdit = false;

    public static StickerFragment newInstance(String type, String data) {
        StickerFragment subtitleFragment = new StickerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TYPE_URL, type);
        bundle.putString(PARAM_DATA_URL, data);
        subtitleFragment.setArguments(bundle);
        return subtitleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "StickerFragment";
        Bundle bundle = getArguments();
        String url = bundle.getString(PARAM_TYPE_URL);
        mTypeUrl = TextUtils.isEmpty(url) ? null : url.trim();
        mDataUrl = bundle.getString(PARAM_DATA_URL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_sticker_layout, container, false);
        mStickerModel = new Sticker2FragmentModel(new Sticker2FragmentModel.StickerCallBack() {

            @Override
            public void onSuccess(List list) {
            }

            @Override
            public void onFailed() {
                if (isRunning) {
                    bDataPrepared = false;
                    SysAlertDialog.cancelLoadingDialog();
                    onToast(R.string.load_http_failed);
                }
            }

            @Override
            public void onStickerSort(ArrayList<IStickerSortApi> stickerApis) {
                mSortAdapter.addAll(stickerApis, 0);
                //?????????????????????
                if (stickerApis.size() > 0) {
                    getSpecialData();
                }
            }

            @Override
            public void onStickerSortData(List<StyleInfo> list, String category) {
                //????????????????????????
                bDataPrepared = true;
                if (null != mHandler && isRunning) {
                    mCategory = category;
                    //????????????
                    mHandler.removeMessages(MSG_DATA);
                    mHandler.obtainMessage(MSG_DATA, list).sendToTarget();

                    //???????????????????????????????????????????????????????????????????????????????????????
                    mHandler.removeMessages(MSG_DRAFT_CHECKPROGRESS);
                    mHandler.sendEmptyMessage(MSG_DRAFT_CHECKPROGRESS);

                    //???????????????????????????icon
                    mHandler.sendEmptyMessage(MSG_RV_ICON);

                    mHandler.obtainMessage(MSG_ICON).sendToTarget();
                }
                SysAlertDialog.cancelLoadingDialog();
            }
        }, mTypeUrl, mDataUrl);
        mCurrentInfo = null;
        initHandler();
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        init();
        checkTitleLayout();
    }

    private void initView() {
        mRvStickerSort = $(R.id.sticker_sort);
        mRvStickerData = $(R.id.sticker_data);
        //????????????
        mMenuLayout = $(R.id.special_menu_layout);

        // ???????????????????????????
        $(R.id.ivAddStickerSure).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsDownloading) {
                    SysAlertDialog.showLoadingDialog(mContext,
                            mContext.getString(R.string.isloading));
                } else {
                    if (null != mCurrentInfo) {
                        int id = mCurrentInfo.getStyleId();
                        StyleInfo styleInfo = StickerUtils.getInstance().getStyleInfo(id);
                        if (null != styleInfo && styleInfo.isdownloaded) {
                            SysAlertDialog.cancelLoadingDialog();
                            onSaveBtnItem(styleInfo);
                            if (mEditorHandler.isPlaying()) {
                                mThumbNailLine.setHideCurrent();
                            }
                        } else {
                            //????????????????????????????????????????????????
                            Log.e(TAG, "onSaveListener : error");
                        }
                    } else {
                        //??????
                        Log.e(TAG, "onSaveListener is null ");
                    }
                }
            }
        });
        //??????
        $(R.id.ivAddStickerCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuBackClick();
            }
        });

        mHalfWidth = CoreUtils.getMetrics().widthPixels / 2;
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);

        //????????????
        btnEdit.setText(R.string.edit);
        tvTitle.setText(R.string.sticker);
        setImage(R.drawable.edit_music_play);

        //?????????????????????
        mAdapter = new StickerAdapter(getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<StickerInfo>() {
            @Override
            public void onItemClick(int position, StickerInfo info) {
                if (mEditorHandler.isPlaying()) {
                    mEditorHandler.pause();
                }
                //??????????????????
                mEditorHandler.seekTo((int) info.getStart() + 1);  // ?????????1ms ???????????????????????????
                onScrollProgress((int) info.getStart() + 1);

                //??????????????????(??????)
                mCurrentInfo = new StickerInfo(info);
                //?????????????????????
                checkVisible(mCurrentInfo);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * ????????????
     */
    private void init() {
        mAddStep = false;
        mDuration = mEditorHandler.getDuration();
        mLayoutWidth = mLinearWords.getWidth();
        mLayoutHeight = mLinearWords.getHeight();
        onListReset(true);
        mViewHint.setVisibility(View.VISIBLE);
        mMenuLayout.setVisibility(View.GONE);
        //??????????????????
        initThumbTimeLine();
        mThumbNailLine.setCantouch(true);
        mThumbNailLine.setMoveItem(true);
        // ?????????????????????????????????
        if (null != mEditorHandler) {
            mEditorHandler.registerEditorPostionListener(mEditorPreivewPositionListener);
            mEditorHandler.reload(false);
            mEditorHandler.seekTo(0);
        }
        //????????????
        mPlayState.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mAddStep) {
                    btnAdd.setText(R.string.add);
                    onWordEnd(mEditorHandler.getCurrentPosition());
                    mThumbNailLine.clearCurrent();
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
        });

        StickerUtils.getInstance().recycle();
        //??????recyclerview
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvStickerSort.setLayoutManager(manager);
        mSortAdapter = new StickerSortAdapter();
        mRvStickerSort.setAdapter(mSortAdapter);
        mSortAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                //??????????????? ???????????????????????????
                bDataPrepared = false;
                mStickerModel.getStickerData((String) item);
            }
        });

        LinearLayoutManager manager2 = new LinearLayoutManager(getContext());
        manager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvStickerData.setLayoutManager(manager2);
        mDataAdpter = new StickerDataAdapter(mContext, false, !TextUtils.isEmpty(mTypeUrl));
        mRvStickerData.setAdapter(mDataAdpter);
        mDataAdpter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                onStyleItem(position);
            }
        });
        mRvStickerData.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE: //??????
                        ApngImageLoader.getInstance().resume();
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING: //
                        ApngImageLoader.getInstance().pause();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING: //
                        ApngImageLoader.getInstance().pause();
                        break;
                }
            }
        });

        //????????????
        bDataPrepared = false;
        mStickerModel.getApiSort();
        //?????????????????????
        ((StickerAdapter) mAdapter).addAll(mList, BaseRVAdapter.UN_CHECK);
    }

    /**
     * ???????????????????????????
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initThumbTimeLine(VirtualVideo virtualVideo) {
        mThumbNailLine.setVirtualVideo(virtualVideo);
        mThumbNailLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        onScrollProgress(0);
        if (null != mHandler && getActivity() != null && !getActivity().isDestroyed()) {
            mHandler.removeCallbacks(resetSpDataRunnable);
            mHandler.postDelayed(resetSpDataRunnable, 100);
        }
    }

    /**
     * ???????????????????????????
     */
    private void initThumbTimeLine() {
        mScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        int[] params = mThumbNailLine.setDuration(mDuration,
                mScrollView.getHalfParentWidth());
        mScrollView.setLineWidth(params[0]);
        mScrollView.setDuration(mDuration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(params[0]
                + 2 * mThumbNailLine.getpadding(), params[1]);

        lp.setMargins(mScrollView.getHalfParentWidth() - mThumbNailLine.getpadding(),
                0, mHalfWidth - mThumbNailLine.getpadding(), 0);
        mThumbNailLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new FrameLayout.LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);
        mMediaLinearLayout.setLayoutParams(lframe);
        mThumbNailLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        mViewHint.setVisibility(View.GONE);
    }

    /**
     * ????????????
     */
    private void initsp() {
        if (null != mCurrentInfo) {
            mSprCurView = Utils.$(mLinearWords, mCurrentInfo.getId());
            if (null == mSprCurView) {
                mSprCurView = initItemWord(mCurrentInfo);
                mLinearWords.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (null != mSprCurView) {
                            ViewParent vp = mSprCurView.getParent();
                            if (null != vp && vp instanceof ViewGroup) {
                                ((ViewGroup) vp).removeView(mSprCurView);
                            }
                            mLinearWords.addView(mSprCurView);
                        }
                    }
                }, 100);

            } else {
                mSprCurView.setVisibility(View.VISIBLE);
                mSprCurView.previewSpecailByUserEdit();
            }
            onCheckStyle(mCurrentInfo);
            mSprCurView.setDelListener(new SinglePointRotate.onDelListener() {

                @Override
                public void onDelete(SinglePointRotate single) {
                    onDelWordItem(single.getId());
                    mLinearWords.removeView(single);
                    onRebackMenuToAddSpecial();
                    mAddStep = false;
                    checkTitleLayout();
                    //????????????????????????
                    resetUI();
                }
            });
            mSprCurView.setControl(true);// ???????????????????????????????????????
        }
    }

    @Override
    StickerAdapter initAdapter() {
        return new StickerAdapter(getContext());
    }

    /**
     * ????????????(????????????)
     */
    @Override
    void onAdapterItemClick(int position, StickerInfo info) {
        //??????????????????
        mEditorHandler.seekTo((int) info.getStart() + 1);  // ?????????1ms ???????????????????????????
        onScrollProgress((int) info.getStart() + 1);

        //??????????????????(??????)
        mCurrentInfo = new StickerInfo(info);
        //?????????????????????
        checkVisible(mCurrentInfo);
    }

    /**
     * ??????????????????
     */
    @Override
    void initListener() {
        mScrollView.addScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(mViewTouchListener);
        mThumbNailLine.setSubtitleThumbNailListener(new IThumbLineListener() {
            StickerInfo info;

            @Override
            public void onTouchUp() {
                mViewTouchListener.onActionUp();
                if (null != info) {
                    bindLiteList(info);
                }
            }

            @Override
            public void updateThumb(int id, int start, int end) {
                mIsUpdate = true;
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                }
                int index = getIndex(id);
                info = null;
                if (index >= 0) {
                    info = mList.get(index);
                    if (null != info && id == info.getId()) {
                        info.removeListLiteObject(mEditorHandler.getEditorVideo());
                        info.setStart(start);
                        info.setEnd(end);
                        mList.set(index, info);
                    }
                }
            }

            @Override
            public void onCheckItem(boolean changed, int id) {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                }
                int index = getIndex(id);
                if (index >= 0) {
                    StickerInfo info = mList.get(index);
                    mCurrentInfo = new StickerInfo(info);
                    checkVisible(mCurrentInfo);
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
        });
    }

    /**
     * ????????????
     */
    @Override
    void onBtnAddClick() {
        if (!bUIPrepared) {
            android.util.Log.e(TAG, "onBtnAddClick: recovering special data ...");
            return;
        }
        if (mEditorHandler != null && mEditorHandler.isPlaying()) {
            pauseVideo();
        }

        if (mSprCurView != null) { // ???????????????????????????item
            mThumbNailLine.clearCurrent();
            mSprCurView.recycle();
            mSprCurView = null;
        }
        PopViewUtil.cancelPopWind();
        if (mDataAdpter.getItemCount() == 0) {
            Context context = getContext();
            int re = CoreUtils.checkNetworkInfo(context);
            if (re == CoreUtils.UNCONNECTED) {
                Utils.autoToastNomal(context, R.string.please_check_network);
            } else {
                SysAlertDialog.showLoadingDialog(mContext, mContext.getString(R.string.isloading));
            }
            getSpecialData();
        } else {
            if (bDataPrepared) {
                String menu = btnAdd.getText().toString();
                if (menu.equals(mContext.getString(R.string.add))) {
                    int progress = mScrollView.getProgress();
                    int header = TempVideoParams.getInstance().getThemeHeader();
                    if (progress < header) {
                        onToast(
                                R.string.addsticker_video_head_failed);
                        return;
                    }
                    int last = TempVideoParams.getInstance().getThemeLast();
                    int videoDuration = getMaxEnd() - last - header;
                    if (progress > videoDuration) {
                        onToast(
                                R.string.addsticker_video_end_failed);
                        return;
                    }
                    if (progress > (header + videoDuration - Math.min(
                            videoDuration / 20, 500))) {
                        onToast(
                                R.string.addsticker_video_between_failed);
                        return;
                    }
                    mAddStep = true;
                    int end = progress + 10;
                    end = Math.min(end, header + videoDuration);
                    mCurrentInfo = new StickerInfo();
                    mCurrentInfo.setTimelineRange(progress, end);
                    mCurrentInfo.setId(Utils.getWordId());

                    mThumbNailLine.addRect(progress, end, "", mCurrentInfo.getId());
                    int checkId = mDataAdpter.getCheckId();
                    StyleInfo info = mDataAdpter.getItem(checkId);
                    if (info.isdownloaded) {
                        mCurrentInfo.setStyleId(info.pid);
                        mCurrentInfo.setCategory(info.category, info.icon);
                        onStartSub(true);
                    } else {
                        onStartSub(false);
                    }
                    mThumbNailLine.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            onStyleItem(mDataAdpter.getCheckId());
                        }
                    }, 100);
                } else if (menu.equals(mContext.getString(R.string.right))) {
                    onItemSave(mEditorHandler.getCurrentPosition());
                }
            } else {
                android.util.Log.e(TAG, "onBtnAddClick: bDataPrepared is " + bDataPrepared);
            }

        }
    }

    /**
     * ??????
     */
    private void onItemSave(int progress) {
        btnAdd.setText(R.string.add);
        onWordEnd(progress);
        resetUI();
    }

    /**
     * ???????????????
     */
    @Override
    void onBtnRightClick() {
        pauseVideo();
        if (mAdapter.getChecked() >= 0) {
            if (mAddStep) {
                onWordEnd(mEditorHandler.getCurrentPosition());
            }
            resetUI();
        } else {
            if (mAddStep) {
                onWordEnd(mEditorHandler.getCurrentPosition());
                onSure();
                resetUI();
                return;
            }
            mEditorHandler.onSure();
        }
    }


    /**
     * ????????????
     */
    @Override
    void onEditClick() {
        mAddStep = false;
        StickerInfo temp = getCurSelectedTemp();
        if (null != temp) {
            //?????????????????????liteObject???????????????UI????????????
            temp.removeListLiteObject(mEditorHandler.getEditorVideo());
            mCurrentInfo = new StickerInfo(temp);
        }

        if (null != mCurrentInfo) {
            int progress = (int) ((mCurrentInfo.getEnd() + mCurrentInfo.getStart()) / 2);
            mEditorHandler.seekTo(progress);
            onScrollToUI(progress);
            //??????????????????????????????
            if (!TextUtils.isEmpty(mCurrentInfo.getCategory())) {
                if (mCurrentInfo.getCategory().equals(mCategory)) {
                    onStyleItem(mDataAdpter.getPosition(mCurrentInfo.getStyleId()));
                    onStartSub(true);
                    mThumbNailLine.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onStyleItem(mDataAdpter.getCheckId());
                        }
                    }, 100);
                } else {
                    SysAlertDialog.showLoadingDialog(getContext(), getString(R.string.loading));
                    mEdit = true;
                    mSortAdapter.setCurrent(mCurrentInfo.getCategory());
                    mStickerModel.getStickerData(mCurrentInfo.getCategory());
                }
            }
        }
    }

    @Override
    void onAutoRecognition() {

    }

    @Override
    boolean checkEnableAI() {
        return false;
    }

    /**
     * ????????????
     */
    @Override
    void onDeleteClick() {
        pauseVideo();
        mCurrentInfo = getCurSelectedTemp();
        if (null != mCurrentInfo) {
            //??????????????????????????????????????????????????????
            mCurrentInfo.removeListLiteObject(mEditorHandler.getEditorVideo());
            mCurrentInfo.recycle();
            View target = mLinearWords.findViewById(mCurrentInfo.getId());
            if (null != target) {
                mLinearWords.removeView(target);
                ((SinglePointRotate) target).recycle();
            }
            mEditorHandler.getEditor().refresh();
            onDelWordItem(mCurrentInfo.getId());
        }
        checkTitleLayout();
    }

    private StickerInfo getCurSelectedTemp() {
        StickerInfo tmp = null;
        if (!mList.isEmpty() && ((StickerAdapter) mAdapter).getIndex() != -1) {
            tmp = mList.get(((StickerAdapter) mAdapter).getIndex());
        }
        return tmp;
    }

    /**
     * @param sublayout
     */
    public void setHandler(FrameLayout sublayout) {
        mLinearWords = sublayout;
    }


    private IViewTouchListener mViewTouchListener = new IViewTouchListener() {

        @Override
        public void onActionDown() {
            if (mAddStep) {
                onWordEnd(mEditorHandler.getCurrentPosition());
            } else {
                mEditorHandler.pause();
                int progress = mScrollView.getProgress();
                mEditorHandler.seekTo(progress);
                setProgressText(progress);
            }
        }

        @Override
        public void onActionMove() {
            int progress = mScrollView.getProgress();
            mEditorHandler.seekTo(progress);
            setProgressText(progress);
        }

        @Override
        public void onActionUp() {
            mScrollView.resetForce();
            int progress = mScrollView.getProgress();
            setProgressText(progress);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        onFetcher();
    }

    private void onFetcher() {
        if (null != mSprCurView) {
            mSprCurView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        onPasue();
    }

    private float onPasue() {
        if (null != mSprCurView) {
            mSprCurView.onPasue();
        }
        if (mAddStep) {
            return (null != mCurrentInfo) ? Utils.ms2s(mCurrentInfo.getStart()) : 0;
        }
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(resetSpDataRunnable);
        ApngImageLoader.getInstance().clearMemoryCache();
        PngImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bUIPrepared = false;
        mHandler.removeCallbacks(resetSpDataRunnable);

        if (null != mDataAdpter) {
            mDataAdpter.onDestory();
            mDataAdpter = null;
        }
        mScrollView.removeScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(null);
        if (null != mThumbNailLine) {
            mThumbNailLine.clearAll();
            mThumbNailLine.recycle(true);
            mThumbNailLine.setSubtitleThumbNailListener(null);
        }
        mHandler = null;
    }

    @Override
    IntentFilter createIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StyleAdapter.ACTION_SUCCESS_SPECIAL);
        intentFilter.addAction(StyleAdapter.ACTION_HAS_DOWNLOAD_ING);
        return intentFilter;
    }

    /**
     * ????????????????????????
     *
     * @param isDownloadLoading
     */
    @Override
    void onDownload(boolean isDownloadLoading) {
        mIsDownloading = isDownloadLoading;
        if (!isDownloadLoading) {
            SysAlertDialog.cancelLoadingDialog();
        }
    }

    /**
     * ??????????????????
     */
    @Override
    void onStyleItemDownloaded(int position) {
        if (-1 != position && mMenuLayout.getVisibility() == View.VISIBLE) {
            if (null != mCurrentInfo) {
                StyleInfo info = mDataAdpter.getItem(position);
                if (null != info) {
                    mCurrentInfo.setStyleId(info.pid);
                    mCurrentInfo.setCategory(info.category, info.icon);
                }
                initsp();
                onStyleItem(position);
            }
            if (null != mDataAdpter) {
//                mSpAapter.notifyDataSetChanged();
                mDataAdpter.notifyItemChanged(position);
            }
        }
    }

    @Override
    void onTTFDownloaded(String path) {
    }

    /**
     * ????????????
     */
    private void setProgressText(int progress) {
        mTvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
        //?????????????????????????????????????????????????????????
        mThumbNailLine.setDuration(progress);
        ((StickerAdapter) mAdapter).setDuration(progress);
    }

    private float getCustomSize(int txtSize) {
        //???????????????????????????
        float MIN = AppConfig.MIN_SCALE;
        float MAX = AppConfig.MAX_SCALE;
        txtSize = Math.min(35, Math.max(16, txtSize));
        return MIN + ((MAX - MIN) * (txtSize - 16) / (35 - 16 + 0.0f));
    }

    //????????????
    private void onStyleItem(int position) {
        mRvStickerData.scrollToPosition(position);
        StyleInfo info = mDataAdpter.getItem(position);
        if (null == info) {
            Log.e(TAG, "onStyleItem->info==null");
            return;
        }
        if (info.isdownloaded) {
            mCurrentInfo.setStyleId(info.pid);
            mCurrentInfo.setCategory(info.category, info.icon);
            if (mCurrentInfo.getDisf() == 1) {
                //?????????
                if (info.isSetSizeW()) { //config.???????????????????????????
                    mCurrentInfo.setDisf(info.disf);
                } else { //????????????????????????????????????????????????????????????
                    mCurrentInfo.setDisf(getCustomSize(21));
                }
            }
            mDataAdpter.setCheckItem(position);
            if (mSprCurView != null) {
                int index = getIndex(mCurrentInfo.getId());
                if (index >= 0) {
                    mSprCurView.setRotate(mCurrentInfo.getRotateAngle());
                } else {
                    mSprCurView.setRotate(info.rotateAngle);
                }
                if (info.frameArray.size() > 0) {
                    mSprCurView.setStyleInfo(true, info, (int) (mCurrentInfo
                                    .getEnd() - mCurrentInfo.getStart()), true,
                            mCurrentInfo.getDisf());

                }
                int x = 0, y = 0;
                if (mCurrentInfo.getCenterxy()[0] != 0.5
                        && mCurrentInfo.getCenterxy()[1] != 0.5) {
                    x = (int) (mLayoutWidth * mCurrentInfo.getCenterxy()[0]);
                    y = (int) (mLayoutHeight * mCurrentInfo.getCenterxy()[1]);
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
            // ????????????
            int at = position % mDataAdpter.getItemCount();
            View child = mRvStickerData.getLayoutManager().findViewByPosition(at);
            if (null != child) {
                mDataAdpter.onDown(position, (CircleProgressBarView) Utils.$(child, R.id.ttf_pbar));
            }

        }

    }

    /**
     * ??????id ????????????????????????
     *
     * @param id
     * @return
     */
    private StickerInfo getItem(int id) {
        int index = getIndex(id);
        return (index != -1) ? mList.get(index) : null;
    }

    /**
     * ??????????????????
     *
     * @param id
     */
    private int removeById(int id) {
        int index = getIndex(id);
        if (index > -1 && index <= (mList.size() - 1)) {
            mList.remove(index);
        }
        return index;
    }

    private int mDuration;

    /**
     * ??????????????????????????????List?????????
     */
    private void onListReset(boolean isInit) {
        mList.clear();
        if (isInit) {
            mBackupList.clear();
        }
        ArrayList<StickerInfo> tempList = TempVideoParams.getInstance()
                .getSpecailsDurationChecked();
        int len = tempList.size();
        StickerInfo temp;
        for (int i = 0; i < len; i++) {
            temp = tempList.get(i);
            temp.resetChanged();
            mList.add(temp);
            if (isInit) {
                mBackupList.add(temp.clone());
            }
        }

    }

    /**
     * ??????
     */
    private EditorPreivewPositionListener mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

        @Override
        public void onEditorPreviewComplete() {
            onScrollCompleted();
            if (mAddStep) { //????????????
                onItemSave(getMaxEnd()); //??????????????????
            }
        }

        @Override
        public void onEditorPrepred() {
            if (null != mEditorHandler) { // ??????loading...
                mEditorHandler.cancelLoading();
            }
            if (!bUIPrepared) {
                //??????????????????????????????????????????????????????
                initThumbTimeLine(mEditorHandler.getSnapshotEditor());
            }
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
        }
    };

    //??????????????????
    private Runnable resetSpDataRunnable = new Runnable() {
        @Override
        public void run() {
            ArrayList<SubInfo> sublist = new ArrayList<>();
            int len = mList.size();
            for (int i = 0; i < len; i++) {
                sublist.add(new SubInfo(mList.get(i)));
            }
            mThumbNailLine.prepareData(sublist);
            if (mCurrentInfo != null) {
                mThumbNailLine.showCurrent(mCurrentInfo.getId());
            }

            mThumbNailLine.setStartThumb(mScrollView.getScrollX());

            if (AppConfiguration.isFirstShowInsertSp()) {
                PopViewUtil.showPopupWindowStyle(mThumbNailLine, true, true, 120,
                        true, mThumbNailLine.getpadding(),
                        new PopViewUtil.CallBack() {

                            @Override
                            public void onClick() {
                                AppConfiguration.setIsFirstInsertSp();
                            }
                        }, R.string.drag_thumb_for_insert_sticker, 0.5);
            }
            bUIPrepared = true;
        }
    };

    private ScrollViewListener mThumbOnScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY, boolean appScroll) {
            mThumbNailLine.setStartThumb(mScrollView.getScrollX());
            int progress = mScrollView.getProgress();
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.seekTo(progress);
                }
                setProgressText(progress);
            }
        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY, boolean appScroll) {
            int progress = mScrollView.getProgress();
            mThumbNailLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll && mEditorHandler != null) {
                mEditorHandler.pause();
                mEditorHandler.seekTo(progress);
            }
            setProgressText(progress);
        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY, boolean appScroll) {
            int progress = mScrollView.getProgress();
            mThumbNailLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                    mEditorHandler.seekTo(progress);
                }
                setProgressText(progress);
            }
        }
    };

    private void checkVisible(StickerInfo current) {
        if (null != current) {
            btnDel.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            mThumbNailLine.showCurrent(current.getId());
        } else {
            btnDel.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            mThumbNailLine.setShowCurrentFalse();
        }
    }

    /**
     * ??????item????????????
     */
    private void onWordEnd(int progress) {
        if (null == mCurrentInfo) {
            return;
        }

        if (progress >= mEditorHandler.getDuration()) {
            progress = getMaxEnd();
        }

        // ??????
        mAddStep = false;
        mThumbNailLine.setIsAdding(mAddStep);
        pauseVideo();
        mCurrentInfo.setEnd(progress);

        VirtualVideo virtualVideo = mEditorHandler.getEditorVideo();
        //???????????????liteObject
        mCurrentInfo.removeListLiteObject(virtualVideo);
        onSaveToList(false);

        //????????????liteObject
        new StickerExportHandler(mContext, mCurrentInfo, mLinearWords.getWidth(), mLinearWords.getHeight()).export(virtualVideo);

        int end = (int) mCurrentInfo.getEnd();
        mThumbNailLine.replace(mCurrentInfo.getId(), (int) mCurrentInfo.getStart(), end);
        if (null != mSprCurView) {
            boolean hasExit = (null != getOldWord(mSprCurView.getId()));
            if (hasExit) {
                mThumbNailLine.replace(mCurrentInfo.getId(), mCurrentInfo.getText());
            } else {
                mThumbNailLine.removeById(mSprCurView.getId());
                mThumbNailLine.clearCurrent();
            }
            mSprCurView.recycle();
            mSprCurView = null;
        }

        onScrollProgress(end);

    }


    private int getMaxEnd() {
        //?????????????????????
        return mEditorHandler.getDuration() - 10;
    }


    private void playVideo() {
        mEditorHandler.start();
        setImage(R.drawable.edit_music_pause);
        mThumbNailLine.setHideCurrent();
        ((StickerAdapter) mAdapter).addAll(mList, BaseRVAdapter.UN_CHECK);
    }

    private void pauseVideo() {
        mHandler.removeCallbacks(mAutoAddItemRunnable);
        mEditorHandler.pause();
        setImage(R.drawable.edit_music_play);
    }

    private SinglePointRotate mSprCurView;

    private void saveInfo(boolean clearCurrent) {
        int[] mTime = mThumbNailLine.getCurrent(mSprCurView.getId());
        if (null != mTime) {
            mCurrentInfo.setStart(mTime[0]);
            mCurrentInfo.setEnd(Math.min(getMaxEnd(), mTime[1]));
        }

        if (null != mSprCurView) {
            double mlayout_width = mLayoutWidth + .0, mlayout_height = mLayoutHeight + .0;

            Double fx = mSprCurView.getLeft() / mlayout_width;
            Double fy = mSprCurView.getTop() / mlayout_height;


            float x1 = (float) (mSprCurView.getCenter().x / mlayout_width);
            float y1 = (float) (mSprCurView.getCenter().y / mlayout_height);
            float[] centerxy = new float[]{x1, y1};

            mCurrentInfo.setLeft(fx);
            mCurrentInfo.setTop(fy);
            mCurrentInfo.setRotateAngle(mSprCurView.getRotateAngle());
            mCurrentInfo.setCenterxy(centerxy);
            mCurrentInfo.setDisf(mSprCurView.getDisf());
            mCurrentInfo.setShadowColor(mSprCurView.getShadowColor());
        }

        mThumbNailLine.replace(mCurrentInfo.getId(), (int) mCurrentInfo.getStart(),
                (int) mCurrentInfo.getEnd());
        int re = getIndex(mCurrentInfo.getId());
        int index;
        if (re > -1) {
            mList.set(re, mCurrentInfo); // ????????????
            index = re;
        } else {
            mList.add(mCurrentInfo); // ??????
            index = mList.size() - 1;
        }
        ((StickerAdapter) mAdapter).addAll(mList, index);
        checkVisible(mCurrentInfo);
        if (clearCurrent) {
            if (mEditorHandler != null) {
                mEditorHandler.stop();
                mEditorHandler.start();
                mThumbNailLine.setHideCurrent();
            }
        }

    }

    /**
     * ??????????????????????????????
     *
     * @param id ???????????????Id
     * @return
     */
    private synchronized int getIndex(int id) {
        int index = -1;
        for (int i = 0; i < mList.size(); i++) {
            if (id == mList.get(i).getId()) {
                index = i;
                break;
            }
        }
        return index;
    }

    private int mLayoutWidth = 1024;
    private int mLayoutHeight = 1024;

    /**
     * ????????????liteObject
     */
    private void bindLiteList(StickerInfo stickerInfo) {
        if (null == stickerInfo) {
            return;
        }
        int nWidth = mLinearWords.getWidth();
        int nHeight = mLinearWords.getHeight();
        stickerInfo.setPreviewAsp((float) (nWidth / (nHeight + .0f)));
        stickerInfo.setParent(nWidth, nHeight);
        VirtualVideo virtualVideo = mEditorHandler.getEditorVideo();
        //??????listLiteObject
        stickerInfo.removeListLiteObject(virtualVideo);
        //????????????lite??????
        new StickerExportHandler(mContext, stickerInfo, nWidth, nHeight).export(virtualVideo);
    }

    /**
     * ??????????????????word
     *
     * @param mSingleId ??????id
     */
    private void onDelWordItem(int mSingleId) {
        mThumbNailLine.removeById(mSingleId);
        mThumbNailLine.clearCurrent();
        removeById(mSingleId);
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
        }
        mCurrentInfo = null;
        mSprCurView = null;
        ((StickerAdapter) mAdapter).addAll(mList, BaseRVAdapter.UN_CHECK);
    }

    /**
     * ???????????? ?????????????????? ->???????????????????????? (????????????)
     */
    private void onRebackMenuToAddSpecial() {
        if (mAddLayout.getVisibility() != View.VISIBLE) {
            mAddLayout.setVisibility(View.VISIBLE);
        }
    }

    private void onCheckStyle(StickerInfo info) {
        if (mDataAdpter.getItemCount() > 0) {
            mDataAdpter.setCheckItem(mDataAdpter.getPosition(info.getStyleId()));
        }
    }

    /**
     * ??????????????????
     */
    private void onStartSub(boolean isdownload) {
        if (null != mEditorHandler) {
            mEditorHandler.pause();
        }
        mMenuLayout.setVisibility(View.VISIBLE);
        mAddLayout.setVisibility(View.GONE);
        if (isdownload) {
            initsp();
            onFetcher();
        }
        if (null != mCurrentInfo) {
            int cp = mDataAdpter.getPosition(mCurrentInfo.getStyleId());
            mDataAdpter.setCheckItem(cp);
        } else {
            mDataAdpter.setCheckItem(-1);
        }

    }

    /**
     * ??????????????????
     *
     * @param id
     * @return
     */
    private StickerInfo getOldWord(int id) {
        StickerInfo temp = null;
        for (StickerInfo item : mList) {
            if (item.getId() == id) {
                temp = item;
                break;
            }
            temp = null;
        }
        return temp;
    }

    /**
     * ??????????????????
     */
    private SinglePointRotate initItemWord(StickerInfo w) {
        int mcenterx = (int) (w.getCenterxy()[0] * mLayoutWidth);
        int mcentery = (int) (w.getCenterxy()[1] * mLayoutHeight);

        StyleInfo si = StickerUtils.getInstance().getStyleInfo(w.getStyleId());
        if (null != si) {

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
                        StickerInfo info = getItem(id);
                        if (null != info) {
                            mThumbNailLine.editSub(id, true);
                            mCurrentInfo = new StickerInfo(info);
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
        return null;
    }

    private final int MSG_ICON = 568, MSG_RV_ICON = 569,
            MSG_DATA = 1565, MSG_DRAFT_CHECKPROGRESS = 300;

    private void initHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_RV_ICON: {
                        if (isRunning) {
                            if (null != mAdapter) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    break;
                    case MSG_ICON:
                        if (isRunning) {
                            if (null != mDataAdpter) {
                                mDataAdpter.updateIcon();
                            }
                        }
                        break;
                    case MSG_DATA:
                        if (null != mDataAdpter) {
                            ArrayList<StyleInfo> styleInfos = (ArrayList<StyleInfo>) msg.obj;
                            if (styleInfos != null && styleInfos.size() > 0) {
                                mDataAdpter.addStyles(styleInfos);
                                mRvStickerData.scrollToPosition(0);
                                if (mEdit) {
                                    onStyleItem(mDataAdpter.getPosition(mCurrentInfo.getStyleId()));
                                    onStartSub(true);
                                    mThumbNailLine.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            onStyleItem(mDataAdpter.getCheckId());
                                        }
                                    }, 100);
                                    mEdit = false;
                                }
                            }
                        }
                        break;
                    case MSG_DRAFT_CHECKPROGRESS:
                        onScrollProgress(mEditorHandler.getCurrentPosition());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    //??????????????????
    private void getSpecialData() {
        bDataPrepared = false;
        mStickerModel.getStickerData(mSortAdapter.getCurrent());
    }

    /**
     * ?????????????????????
     */
    private void setImage(@DrawableRes int resId) {
        if (isRunning && null != mPlayState) {
            mPlayState.setImageResource(resId);
        }
    }

    /**
     * ??????????????????
     */
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
                        int len = mList.size();
                        for (int n = 0; n < len; n++) {
                            mList.get(n).set(mBackupList.get(n));
                        }
                        if (mExitListener != null) {
                            mExitListener.exit(1);
                        }
                        onBackToActivity();
                        dialog.dismiss();
                        mEditorHandler.onBack();
                    }
                }, false, null).show();
    }

    /**
     * ????????????Activity(???????????????)??????
     */
    private void onBackToActivity() {
        if (mExitListener != null) {
            return;
        }
        mIsUpdate = false;
        if (mAddStep) {
            onWordEnd(mEditorHandler.getCurrentPosition());
        } else {
            if (null != mSprCurView) {
                mSprCurView.recycle();
                mSprCurView = null;
            }
        }
        if (null != mSprCurView) {
            if (null == getOldWord(mSprCurView.getId())) {
                mThumbNailLine.removeById(mSprCurView.getId());
            }
        }
        mThumbNailLine.clearCurrent();
        mLinearWords.removeAllViews();
        if (null != mDataAdpter) {
            mDataAdpter.onDestory();
        }
        mEditorHandler.unregisterEditorProgressListener(mEditorPreivewPositionListener);
        bUIPrepared = false;
    }

    /**
     * ????????????,????????????editActivity
     */
    private void onSure() {
        onBackToActivity();
        ArrayList<StickerInfo> tempList = TempVideoParams.getInstance().getRSpecialInfos();
        boolean canReset = true;
        if (null != tempList) {
            int len = tempList.size();
            int size = mList.size();
            if (size == len) {
                for (int i = 0; i < size; i++) {
                    if (!contains(mList.get(i), tempList)) {
                        canReset = true;
                        break;
                    } else {
                        canReset = false;
                    }
                }
            } else {
                canReset = true;
            }
        }
        if (canReset)
            TempVideoParams.getInstance().setSpecial(mList);

    }

    /**
     * ??????????????????
     */
    private boolean contains(StickerInfo info, ArrayList<StickerInfo> tempList) {
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
     * ?????????????????????????????????
     */
    private Runnable mAutoAddItemRunnable = new Runnable() {
        @Override
        public void run() {
            onBtnAddClick();
        }
    };

    /**
     * ??????menu_layout ????????????(???????????????????????????????????????)???????????????
     */
    private void onSaveBtnItem(StyleInfo styleInfo) {
        Rect rect = mSprCurView.getOriginalRect();
        mCurrentInfo.setRectOriginal(rect);
        onSaveToList(false);
        if (mAddStep) { //????????????
            mCurrentInfo.setEnd(getMaxEnd()); //????????????????????????
        }
        onMenuViewOnBackpressed();
        bindLiteList(mCurrentInfo);
        if (null != mSprCurView) {
            mLinearWords.removeView(mSprCurView);
            mSprCurView.recycle();
            mSprCurView = null;
        }
        if (mAddStep) {
            mEditorHandler.seekTo((int) mCurrentInfo.getStart());
            if (styleInfo.unLoop) {
                //????????????
                mHandler.postDelayed(mAutoAddItemRunnable, styleInfo.du);
            }
            playVideo();

            btnAdd.setText(R.string.right);
        }
        if (mSprCurView != null) { // ???????????????????????????item
            mSprCurView.setControl(false);
        }
        checkTitleLayout();
    }


    /**
     * ?????????-????????????????????????????????????????????????(1??????????????????,2????????????))
     */
    private void onMenuViewOnBackpressed() {
        if (mMenuLayout.getVisibility() == View.VISIBLE) {
            mMenuLayout.setVisibility(View.GONE);
            onRebackMenuToAddSpecial();
            mViewTouchListener.onActionUp();
        }
    }

    private void onSaveToList(boolean clearCurrent) {
        if (mSprCurView != null) {
            saveInfo(clearCurrent);
        }
        if (clearCurrent)
            mThumbNailLine.clearCurrent();
    }

    /**
     * ??????????????????
     */
    private void onScrollProgress(int progress) {
        if (null != mCurrentInfo && mAddStep) {
            mCurrentInfo.setEnd(progress);
            mThumbNailLine.update(mCurrentInfo.getId(), (int) mCurrentInfo.getStart(), progress);
        }
        onScrollToUI(progress);
    }

    /**
     * ??????
     */
    private void onScrollToUI(int progress) {
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }

    /**
     * ??????????????????
     */
    private int getScrollX(long progress) {
        return (int) (progress * (mThumbNailLine.getThumbWidth() / mDuration));
    }

    /**
     * ????????????
     */
    private void onScrollCompleted() {
        setProgressText(mDuration);
        onScrollTo((int) mThumbNailLine.getThumbWidth());
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
     * ????????????-->????????????
     */
    @Override
    void onMenuBackClick() {
        if (null != mSprCurView) {
            if (null == getOldWord(mSprCurView.getId())) {
                mThumbNailLine.removeById(mSprCurView.getId());
            }
            mSprCurView.recycle();
            mSprCurView = null;
            mLinearWords.removeAllViews();
        }
        onMenuViewOnBackpressed();
        if (!mAddStep) {
            //??????????????????????????????liteList
            StickerInfo stickerInfo = getCurSelectedTemp();
            bindLiteList(stickerInfo);
        }
        if (null != mDataAdpter) {
            mDataAdpter.clearDownloading();
        }
        mAddStep = false;
    }

    /**
     * ??????????????????
     */
    @Override
    void onBtnLeftClick() {
        if (mAdapter.getChecked() >= 0) {
            //?????????????????????????????????
            resetUI();
            pauseVideo();
        } else {
            if (!CommonStyleUtils.isEqualsSp(mList, TempVideoParams.getInstance()
                    .getSpecailsDurationChecked()) || mIsUpdate) {
                onShowAlert();
            } else {
                onBackToActivity();
                mEditorHandler.onBack();
            }
        }
    }

    /**
     * UI?????????????????????
     */
    @Override
    void resetUI() {
        mThumbNailLine.setShowCurrentFalse();
        mCurrentInfo = null;
        btnEdit.setVisibility(View.GONE);
        btnDel.setVisibility(View.GONE);
        btnAdd.setText(R.string.add);
        ((StickerAdapter) mAdapter).addAll(mList, BaseRVAdapter.UN_CHECK);
        mAddStep = false;
    }

    //????????????????????????
    private VideoEditAloneActivity.ExitListener mExitListener;

    public void setExitListener(VideoEditAloneActivity.ExitListener exitListener) {
        this.mExitListener = exitListener;
    }

    //???????????????
    public void setHideEdit() {
        if (mThumbNailLine != null) {
            mThumbNailLine.setHideCurrent();
            ((StickerAdapter) mAdapter).addAll(mList, BaseRVAdapter.UN_CHECK);
        }
    }

}
