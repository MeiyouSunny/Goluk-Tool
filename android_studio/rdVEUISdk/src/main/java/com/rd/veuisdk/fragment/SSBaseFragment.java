package com.rd.veuisdk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.adapter.StyleAdapter;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.mvp.model.BaseModel;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.util.ArrayList;

/**
 * 字幕、贴纸公共部分
 */
public abstract class SSBaseFragment<E, T extends BaseRVAdapter, M extends BaseModel> extends BaseFragment {
    private boolean isRegisted = false;
    protected ArrayList<E> mList = new ArrayList<>(); //当前fragment 的增删改
    protected ArrayList<E> mBackupList = new ArrayList<>();//备份进入fragment之前的数据
    protected IVideoEditorHandler mEditorHandler;
    protected View mMenuLayout;
    protected ExtButton btnAdd, btnDel, btnEdit;
    protected TextView mTvProgress;
    protected ImageView mPlayState;
    protected TimelineHorizontalScrollView mScrollView;
    protected LinearLayout mMediaLinearLayout;
    protected ThumbNailLine mThumbNailLine;
    protected View mViewHint;
    protected View mAddLayout;
    protected ViewGroup mRecyclerParent;
    protected RecyclerView mRecyclerView;
    protected TextView tvTitle;
    protected BaseRVAdapter mAdapter;
    protected E mCurrentInfo;
    protected M mModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getContext();
        if (getActivity() instanceof IVideoEditorHandler) {
            mEditorHandler = (IVideoEditorHandler) getActivity();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initBaseView();
        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnLeftClick();
            }
        });

        IntentFilter intentFilter = createIntentFilter();
        isRegisted = false;
        if (null != intentFilter) {
            getActivity().registerReceiver(mReceiver, intentFilter);
            isRegisted = true;
        }
    }

    /**
     * 初始化公告组件
     */
    private void initBaseView() {
        btnAdd = $(R.id.btn_add_item);
        btnDel = $(R.id.btn_del_item);
        btnEdit = $(R.id.btn_edit_item);
        if (checkEnableAI()) {
            btnEdit.setText(R.string.auto_recognition);
            btnEdit.setVisibility(View.VISIBLE);
        } else {
            btnEdit.setText(R.string.edit);
        }


        mTvProgress = $(R.id.tvAddProgress);
        mMenuLayout = $(R.id.special_menu_layout);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnAddClick();
            }
        });
        mPlayState = $(R.id.ivPlayerState);
        mAddLayout = $(R.id.subtitle_add_layout);
        mScrollView = $(R.id.priview_subtitle_line);
        mScrollView.enableUserScrolling(true);

        mMediaLinearLayout = $(R.id.subtitleline_media);
        mThumbNailLine = $(R.id.subline_view);
        mThumbNailLine.setEnableRepeat(true);  // 允许重复
        mViewHint = $(R.id.word_hint_view);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClick();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnEdit.getText().toString().equals(getString(R.string.auto_recognition))) {
                    //语音识别
                    onAutoRecognition();
                } else {
                    onEditClick();
                }

            }
        });
        $(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnRightClick();
            }
        });


        tvTitle = $(R.id.tvTitle);
        mRecyclerParent = $(R.id.recycleParent);
        mRecyclerView = $(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = initAdapter();
        mAdapter.setOnItemClickListener(new OnItemClickListener<E>() {
            @Override
            public void onItemClick(int position, E info) {
                if (mEditorHandler.isPlaying()) {
                    mEditorHandler.pause();
                }
                onAdapterItemClick(position, info);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        initListener();
    }

    protected void checkTitleLayout() {
        if (mList.size() > 0) {
            tvTitle.setVisibility(View.GONE);
            mRecyclerParent.setVisibility(View.VISIBLE);
        } else {
            mRecyclerParent.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    abstract T initAdapter();

    /**
     * 选中单个(准备编辑)
     */
    abstract void onAdapterItemClick(int position, E info);

    /**
     * 公共组件设置
     */
    abstract void initListener();

    /**
     * 新增单个
     */
    abstract void onBtnAddClick();

    /**
     * 保存并退出
     */
    abstract void onBtnRightClick();

    /**
     * 编辑按钮
     */
    abstract void onEditClick();

    /**
     * 自动识别
     */
    abstract void onAutoRecognition();

    /***
     * 是否需要启用AI功能  (有视频文件)
     */
    abstract boolean checkEnableAI();


    /**
     * 删除按钮
     */
    abstract void onDeleteClick();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unRegister();
        if (null != mModel) {
            mModel.recycle();
        }
    }

    private void unRegister() {
        if (isRegisted) {
            isRegisted = false;
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
        if (null != mModel) {
            mModel.recycle();
        }
    }

    abstract IntentFilter createIntentFilter();

    /**
     * 是否有样式正在下载...
     */
    abstract void onDownload(boolean isDownloadLoading);

    /**
     * 下载样式完成
     *
     * @param position 样式下标
     */
    abstract void onStyleItemDownloaded(int position);

    /**
     * 字体下载完成
     *
     * @param path 字体路径
     */
    abstract void onTTFDownloaded(String path);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context mContext, Intent intent) {
            String action = intent.getAction();
            if (isRunning) {
                if (TextUtils.equals(action, StyleAdapter.ACTION_HAS_DOWNLOAD_ING)) {
                    onDownload(intent.getBooleanExtra(StyleAdapter.ITEM_IS_DOWNLOADING, true));
                } else if (TextUtils.equals(StyleAdapter.ACTION_SUCCESS_SPECIAL, action) || TextUtils.equals(StyleAdapter.ACTION_SUCCESS_CAPTION, action)) {
                    onStyleItemDownloaded(intent.getIntExtra(StyleAdapter.DOWNLOADED_ITEM_POSITION, -1));
                } else if (TextUtils.equals(TTFAdapter.ACTION_TTF, action)) {
                    String path = intent.getStringExtra(TTFAdapter.TTF_ITEM);
                    if (!TextUtils.isEmpty(path)) {
                        onTTFDownloaded(path);
                    }
                }
            }
        }
    };

    /**
     * 默认状态下，清除recycleview中被选中的item  、 隐藏 编辑、 删除 、退出缩略图编辑模式
     */
    abstract void resetUI();

    /**
     * 一级界面-返回
     */
    abstract void onBtnLeftClick();

    /**
     * 二级界面-返回
     */
    abstract void onMenuBackClick();


    @Override
    public int onBackPressed() {
        if (null != mMenuLayout && mMenuLayout.getVisibility() == View.VISIBLE) {
            //二级页面返回
            onMenuBackClick();
            return -1;
        } else {
            //一级页面返回
            onBtnLeftClick();
        }
        return super.onBackPressed();
    }
}
