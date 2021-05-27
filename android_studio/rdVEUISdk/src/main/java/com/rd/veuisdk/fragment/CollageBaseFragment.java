package com.rd.veuisdk.fragment;


import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.adapter.CollageAdapter;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.mvp.model.CollageFragmentModel;
import com.rd.veuisdk.ui.DragView;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.ScrollLayout;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.util.ArrayList;


/**
 * 画中画
 */
public abstract class CollageBaseFragment extends RBaseFragment {


    protected boolean isDraging = false;//是否正在调整当个画中画的位置

    private GalleryFragment mGalleryFragment;

    private View hintView;
    private TextView tvProgress;
    private ImageView ivPlayState;
    private TimelineHorizontalScrollView mTimelineHorizontalScrollView;
    private LinearLayout mLinearLayout;
    protected ThumbNailLine mThumbNailLine;
    private Button mBtnAdd;
    private View mBtnDelete, mBtnCancel;
    protected CollageFragment.CallBack mCallBack;
    protected ViewGroup mMixMenuLayout;
    protected ViewGroup mMixAddLayout;
    private TextView tvAdded;
    protected FrameLayout mLinearWords;
    private RecyclerView mRecyclerView;
    private int mStateSize;
    protected CollageAdapter mCollageAdapter;
    private int[] mSizeParams;
    private int mHalfWidth = 0;
    private DisplayMetrics mDisplayMetrics;
    protected int mDuration = 1000; //单位：毫秒
    private boolean bThumbPrepared = false;
    protected IVideoEditorHandler mEditorHandler;
    protected ScrollLayout mScrollLayout;
    protected CollageFragmentModel mModel;

    /***
     * 图库布局是否可见
     * @return
     */
    boolean isGalleryLayout() {
        return (null != mMixMenuLayout) && mMixMenuLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mEditorHandler = (IVideoEditorHandler) context;
    }


    public void setOtherFragmentHeight(int otherFragmentHeight) {
        mOtherFragmentHeight = otherFragmentHeight;
    }

    private int mOtherFragmentHeight = 500;

    private View mediaTypeLayout;
    private View recycleParent;
    private RadioGroup mRadioGroup;
    private RadioButton rbVideo, rbPhoto;
    private TextView tvTitle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mModel = new CollageFragmentModel(TempVideoParams.getInstance().getCollageDurationChecked());
        mRoot = inflater.inflate(R.layout.fragment_collage_layout, container, false);
        mScrollLayout = $(R.id.collageScrollLayout);
        {
            //指定当前容器的高度
            Rect rect = new Rect();
            container.getGlobalVisibleRect(rect);
            Rect rect1 = new Rect();
            ViewGroup vp = (ViewGroup) container.getParent();
            vp.getGlobalVisibleRect(rect1);
            mScrollLayout.setDefaultHeight(mOtherFragmentHeight / (rect1.height() + 0.0f));
        }
        tvAdded = $(R.id.tvAdded);
        tvAdded.setVisibility(View.GONE);
        isDraging = false;
        mMixAddLayout = $(R.id.add_layout);
        mMixMenuLayout = $(R.id.fragmentParent);
        mMixMenuLayout.setVisibility(View.GONE);
        bThumbPrepared = false;
        mDisplayMetrics = CoreUtils.getMetrics();
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        hintView = $(R.id.word_hint_view);
        tvProgress = $(R.id.tvAddProgress);
        ivPlayState = $(R.id.ivPlayerState);
        mTimelineHorizontalScrollView = $(R.id.priview_subtitle_line);
        mLinearLayout = $(R.id.subtitleline_media);
        mThumbNailLine = $(R.id.subline_view);
        mThumbNailLine.setEnableRepeat(true);
        mBtnAdd = $(R.id.btn_add_item);
        mBtnCancel = $(R.id.btn_edit_item);
        mBtnDelete = $(R.id.btn_del_item);
        tvTitle = $(R.id.tvTitle);
        mEditorHandler.registerEditorPostionListener(mPositionListener);
        mBtnAdd.setText(R.string.add);
        mRecyclerView = $(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mCollageAdapter = new CollageAdapter(tvAdded, mModel.getList());
        mCollageAdapter.setTitleView(tvTitle);
        mRecyclerView.setAdapter(mCollageAdapter);

        mediaTypeLayout = $(R.id.mediaTypeLayout);
        recycleParent = $(R.id.recycleParent);
        mRadioGroup = $(R.id.rgFormat);
        rbVideo = $(R.id.rbVideo);
        rbPhoto = $(R.id.rbPhoto);
        return mRoot;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDuration = mEditorHandler.getDuration();
        mTimelineHorizontalScrollView.setCanTouch(true);
        mThumbNailLine.setCantouch(true);
        mThumbNailLine.setMoveItem(true);

        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.collage);

        onInitThumbTimeLine();
        onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());

        initListener();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEditorHandler.unregisterEditorProgressListener(mPositionListener);
        mHandler.removeCallbacks(resetSubDataRunnable);
        mTimelineHorizontalScrollView.removeScrollListener(mScrollViewListener);
        mThumbNailLine.setSubtitleThumbNailListener(null);
        onScrollTo(0);
        mGalleryFragment = null;
        if (null != mLinearWords) {
            mLinearWords.removeAllViewsInLayout();
        }
        if (null != dragView) {
            dragView.recycle();
            dragView = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGalleryFragment = null;
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

    void playVideo() {
        mEditorHandler.start();
        ivPlayState.setImageResource(R.drawable.edit_music_pause);
    }

    void pauseVideo() {
        mEditorHandler.pause();
        ivPlayState.setImageResource(R.drawable.edit_music_play);
    }

    //是否需要缩略图轴滚动到0 （部分reload的时不需要 （保存当个完成时））
    protected int nScrollProgress = 0;
    protected DragView dragView;
    /**
     * 正在的编辑单个，只有 回调时（setOnItemClick（））
     */
    protected boolean isItemEditing = false;

    private void initListener() {

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消当前，与delete 类似
                //主动干预保存
                isMixItemFirstForLine = false;
                pauseVideo();
                onDeleteOCancelMix(dragView);
            }
        });


        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String menu = mBtnAdd.getText().toString();
                pauseVideo();
                if (menu.equals(getString(R.string.add))) {
                    //先保存之前编辑的项
                    checkEidtSave(true, 0);
                    onAddStep1Begin();
                } else if (menu.equals(getString(R.string.complete))) {
                    onSaveEditMix();
                } else {

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
                mCurrentCollageInfo.updateMixInfo(start, end);
            }
        });


        mCollageAdapter.setOnItemClickListener(new OnItemClickListener<CollageInfo>() {
            @Override
            public void onItemClick(int position, CollageInfo item) {
                onEditMixClicked(item.getId());
            }
        });

        ivPlayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onPlayStateClicked();

            }
        });
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteOCancelMix(dragView);
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
     * 移除图库
     */
    void removeGalleryFragment() {
        mScrollLayout.setEnableFullParent(false);
        removeFragment(mGalleryFragment);
        checkTitleBarVisible();
        mediaTypeLayout.setVisibility(View.GONE);
        mMixMenuLayout.setVisibility(View.GONE);
        mMixAddLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 标题是否可见
     */
      void checkTitleBarVisible() {
        if (mCollageAdapter.getItemCount() > 0) {
            recycleParent.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
        } else {
            recycleParent.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 准备新增单个画中画
     */
    private void onAddStep1Begin() {
        mMixAddLayout.setVisibility(View.GONE);
        mMixMenuLayout.setVisibility(View.VISIBLE);

        rbVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mGalleryFragment) {
                    mGalleryFragment.onVideoClick();
                }
            }
        });
        rbPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mGalleryFragment) {
                    mGalleryFragment.onPhotoClick();
                }
            }
        });
        if (null == mGalleryFragment) {
            mGalleryFragment = GalleryFragment.newInstance();
        }
        mGalleryFragment.setCheckVideo(mRadioGroup.getCheckedRadioButtonId() == R.id.rbVideo);
        mGalleryFragment.setGallerySizeListener(new GalleryFragment.IGallerySizeListener() {
            @Override
            public void onGallerySizeClicked() {
                mScrollLayout.setEnableFullParent(!mScrollLayout.isFullParent());
            }
        });
        mGalleryFragment.setCallBack(new GalleryFragment.IGalleryCallBack() {
            @Override
            public void onVideo(ImageItem item) {
                getVideoCallBack().onItem(item);
            }

            @Override
            public void onPhoto(ImageItem item) {
                getPhotoCallBack().onItem(item);
            }

            @Override
            public void onRGCheck(boolean isVideo) {
                mRadioGroup.check(isVideo ? R.id.rbVideo : R.id.rbPhoto);

            }
        });

        changeFragment(R.id.fragmentParent, mGalleryFragment);

        recycleParent.setVisibility(View.GONE);
        mediaTypeLayout.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.GONE);

    }

    private void onInitThumbTimeLine() {

        mHalfWidth = mDisplayMetrics.widthPixels / 2;
        mTimelineHorizontalScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        mSizeParams = mThumbNailLine.setDuration(mDuration, mTimelineHorizontalScrollView.getHalfParentWidth());
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

    /**
     * 初始化缩略图时间轴和恢复字幕数据
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
        updateSubList();
    }


    /**
     * 更新时间轴上的数据
     */
    public void updateSubList() {
        ArrayList<SubInfo> subInfos = new ArrayList<>();
        int len = mModel.getList().size();
        for (int i = 0; i < len; i++) {
            subInfos.add(mModel.getList().get(i).getSubInfo());
        }
        mThumbNailLine.prepareData(subInfos);
        mCollageAdapter.addAll(mModel.getList(), -1);
    }


    //当前正在编辑的画中画
    protected CollageInfo mCurrentCollageInfo;


    //还未完全打勾（确认添加）    (true :添加单个媒体时调整完位置，还未完全添加时间线； false 其他状态 )
    protected boolean isMixItemFirstForLine = false;

    private IVideoEditorHandler.EditorPreivewPositionListener mPositionListener = new IVideoEditorHandler.EditorPreivewPositionListener() {
        @Override
        public void onEditorPrepred() {
            mDuration = mEditorHandler.getDuration();
            if (!bThumbPrepared) {
                bThumbPrepared = true;
            } else {
                CollageInfo collageInfo = mCurrentCollageInfo;
                if (null != collageInfo) {
                    onScrollProgress(collageInfo.getSubInfo().getTimelinefrom());
                } else {
                    onScrollProgress(nScrollProgress);
                }
            }
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
        }

        @Override
        public void onEditorPreviewComplete() {
            if (isMixItemFirstForLine) {
                isItemEditing = true;
                //当前画中画保存
                checkEidtSave(true, mDuration);
            }
            onScrollCompleted();

        }
    };

    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    public void onScrollProgress(int progress) {
        if (isMixItemFirstForLine && null != mCurrentCollageInfo) {
            SubInfo tmp = mCurrentCollageInfo.getSubInfo();
            mThumbNailLine.update(tmp.getId(), tmp.getTimelinefrom(), progress);
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
     * @param mScrollX
     */
    private void onScrollTo(int mScrollX) {

        mTimelineHorizontalScrollView.appScrollTo(mScrollX, true);

    }

    private void setProgressText(int progress) {
        tvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
        mThumbNailLine.setDuration(progress);
        mCollageAdapter.setDuration(progress);
    }


    /**
     * 是否调整位置
     *
     * @param draging true 正处于编辑位置；false 其他状态
     */
    public void setDragState(boolean draging) {
        isDraging = draging;
    }


    /**
     * 恢复到默认状态下的UI
     */
    void onResetMenuUI() {
        mBtnDelete.setVisibility(View.GONE);
        mBtnCancel.setVisibility(View.GONE);
        mBtnAdd.setText(R.string.add);
    }

    /**
     * 选择当前画中画，处理UI
     */
    void checkItemMixMenuUI(int subId) {
        //UI 进度条选中、 可删除 、新增
        mThumbNailLine.showCurrent(subId);
        mBtnDelete.setVisibility(View.VISIBLE);
        mBtnAdd.setText(R.string.add);
    }

    /**
     * 保存完成时的UI
     */
    void onSaveItemCompeletedUI() {
        mBtnCancel.setVisibility(View.GONE);
        mBtnAdd.setText(R.string.add);
        mBtnDelete.setVisibility(View.GONE);
    }

    /**
     * 仅显示新增状态
     */
    void onResetAddState() {
        mThumbNailLine.setShowCurrentFalse();
        mBtnAdd.setText(R.string.add);
        mBtnCancel.setVisibility(View.GONE);
        mBtnDelete.setVisibility(View.GONE);
    }

    /**
     * 可取消、新增画中画
     */
    void onItemStep1SaveMenuUI(int subId) {
        mThumbNailLine.showCurrent(subId);
        mThumbNailLine.setHideCurrent();
        mBtnDelete.setVisibility(View.GONE);
        mBtnCancel.setVisibility(View.VISIBLE);
        mBtnAdd.setText(R.string.complete);
    }


    /***
     * 退出新增画中画流程
     */
    void onExitMixItem() {
        if (null != dragView) {
            mLinearWords.removeView(dragView);
            dragView.recycle();
            dragView = null;
        }

        if (null != mCurrentCollageInfo) {
            mModel.remove(mCurrentCollageInfo);
            mThumbNailLine.removeById(mCurrentCollageInfo.getSubInfo().getId());
            mCurrentCollageInfo = null;
        }

        //删除当前缩率图轴上的时间线
        mThumbNailLine.setShowCurrentFalse();
        nScrollProgress = 0;
        setDragState(false);
        dragView = null;
        onResetMenuUI();

        //移除图库fragment
        removeGalleryFragment();
        isDraging = false;
    }


//********************************************************************以下为重大逻辑方法


    /**
     * 点击播放、暂停按钮
     */
    abstract void onPlayStateClicked();


    /**
     * 取消按钮|正真意义的删除按钮
     *
     * @param dragView
     * @return true 真正有做删除 （onbackpressed()-> return;  false 没做真正的删除功能），
     */
    abstract boolean onDeleteOCancelMix(DragView dragView);


    /**
     * 新增单个画中画保存  （rightClicked） （拉满时间线，并自动播放）
     */
    abstract void onAddStep1Save();


    /***
     * 点击play 、pause 、add|complete  、right 时保存当前正在编辑的画中画
     */
    abstract void onSaveEditMix();

    /**
     * adapter回调，选中单个并编辑单个
     * setOnItemClick( mixinfo)
     */
    abstract void onEditMixClicked(int mixId);


    /***
     * 图库视频回调
     * @return
     */
    abstract GalleryFragment.ICallBack getVideoCallBack();

    /**
     * 图片回调
     *
     * @return
     */
    abstract GalleryFragment.ICallBack getPhotoCallBack();


    /***
     * 新增单个之前，先检查保存正在编辑的画中画
     * @param reload
     */
    abstract void checkEidtSave(boolean reload, int end);


}
