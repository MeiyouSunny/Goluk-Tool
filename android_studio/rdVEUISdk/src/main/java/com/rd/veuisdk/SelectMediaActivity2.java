package com.rd.veuisdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.rd.veuisdk.adapter.DirAdapter;
import com.rd.veuisdk.adapter.GalleryAdapter;
import com.rd.veuisdk.base.BaseMvpActivity;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.IDirInfo;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.mvp.persenter.SelectMediaPersenter;
import com.rd.veuisdk.mvp.view.ISelectMediaView;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.List;

/**
 * 画中画-图库选中
 */
public class SelectMediaActivity2 extends BaseMvpActivity<SelectMediaPersenter<ISelectMediaView>> implements ISelectMediaView {
    @Override
    public SelectMediaPersenter<ISelectMediaView> initPersenter() {
        return new SelectMediaPersenter<>(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.select_media_layout2;
    }


    private RecyclerView mRecyclerViewDir, mRecyclerViewMedia;
    private GalleryAdapter mGalleryAdapter;
    private DirAdapter mDirAdapter;
    private static final String PARAM_MEDIA_TYPE = "media_type";

    /**
     * 入口
     *
     * @param context
     * @param video
     * @param requstCode
     */
    public static void onMixMedia(Context context, boolean video, int requstCode) {
        Intent i = new Intent(context, SelectMediaActivity2.class);
        i.putExtra(PARAM_MEDIA_TYPE, video);
        ((Activity) context).startActivityForResult(i, requstCode);

    }

    private boolean isVideo;


    /**
     * @param path
     */
    private void onResult(String path) {
        Intent intent = new Intent();
        intent.putExtra(IntentConstants.EXTRA_MEDIA_LIST, path);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mRecyclerViewMedia.getVisibility() == View.VISIBLE) {
            mRecyclerViewMedia.setVisibility(View.GONE);
            mRecyclerViewDir.setVisibility(View.VISIBLE);
            mRecyclerViewDir.startAnimation(AnimationUtils.loadAnimation(this,R.anim.onback_last_menu));
        } else {
            super.onBackPressed();
        }
    }

    private TextView tvNotFound, tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isVideo = getIntent().getBooleanExtra(PARAM_MEDIA_TYPE, true);
        tvNotFound =$(R.id.tv_media_hint);
        tvTitle =$(R.id.tvTitle);
        tvTitle.setText(isVideo ? R.string.select_media_title_video : R.string.select_media_title_photo);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mRecyclerViewDir = $(R.id.recyclerViewDir);
        mDirAdapter = new DirAdapter(this,true);
        mDirAdapter.setOnItemClickListener(new OnItemClickListener<IDirInfo>() {

            @Override
            public void onItemClick(int position, IDirInfo dirInfo) {

                mRecyclerViewDir.setVisibility(View.GONE);
                mRecyclerViewMedia.setVisibility(View.VISIBLE);

                mRecyclerViewMedia.startAnimation(AnimationUtils.loadAnimation(SelectMediaActivity2.this,R.anim.center_show));

                if (null != dirInfo) {
                    mGalleryAdapter.addAll(dirInfo.getList());

                }
            }
        });

        initReyclerViewMedia(mRecyclerViewDir, mDirAdapter, new GridLayoutManager(this, 2));


        mRecyclerViewMedia = $(R.id.recyclerViewMedia);

        mGalleryAdapter = new GalleryAdapter(this);
        mGalleryAdapter.setOnItemClickListener(new OnItemClickListener<ImageItem>() {
            @Override
            public void onItemClick(int position, ImageItem item) {
                onResult(item.image.getDataPath());
            }
        });
        initReyclerViewMedia(mRecyclerViewMedia, mGalleryAdapter, new GridLayoutManager(this, 4));
        //加载数据
        mPresenter.initData(isVideo);
    }

    /**
     *
     * @param recyclerView
     * @param adapter
     * @param layoutManager
     */
    private void initReyclerViewMedia(RecyclerView recyclerView, RecyclerView.Adapter adapter, GridLayoutManager layoutManager) {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        //设置添加或删除item时的动画，这里使用默认动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGalleryAdapter = null;
        mRecyclerViewMedia = null;
    }

    @Override
    public void onSuccess(List<IDirInfo> list) {
        SysAlertDialog.cancelLoadingDialog();
        mRecyclerViewMedia.setVisibility(View.GONE);
        if (null != list && list.size() > 0) {
            mRecyclerViewDir.setVisibility(View.VISIBLE);
            tvNotFound.setVisibility(View.GONE);
            mDirAdapter.addAll(list);
        } else {
            mRecyclerViewDir.setVisibility(View.GONE);
            tvNotFound.setText(isVideo ? R.string.video_not_found : R.string.photo_not_found);
            tvNotFound.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void showLoading() {
        SysAlertDialog.showLoadingDialog(this, R.string.isloading);
    }


}
