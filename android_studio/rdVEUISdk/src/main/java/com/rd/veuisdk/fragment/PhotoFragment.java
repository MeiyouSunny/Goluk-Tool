package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.GalleryAdapter;
import com.rd.veuisdk.base.BaseMvpFragment;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.mvp.persenter.GalleryPersenter;
import com.rd.veuisdk.mvp.view.IGalleryView;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.List;

/**
 * 画中画-图库-图片
 */
public class PhotoFragment extends BaseMvpFragment<GalleryPersenter<IGalleryView>> implements IGalleryView {
    public static PhotoFragment newInstance() {

        Bundle args = new Bundle();

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_media_layout;
    }


    @Override
    public GalleryPersenter<IGalleryView> initPersenter() {
        return new GalleryPersenter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mPresenter.initData(false);
        return mRoot;
    }

    private GalleryAdapter mAdapter;

    @Override
    public void initView(View view) {
        mRecyclerView = $(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        mRecyclerView.setLayoutManager(layoutManager);
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new GalleryAdapter(getContext());

        mAdapter.setOnItemClickListener(new OnItemClickListener<ImageItem>() {
            @Override
            public void onItemClick(int position, ImageItem item) {
                if (null != mCallBack) {
                    mCallBack.onItem(item);
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setCallBack(GalleryFragment.ICallBack callBack) {
        mCallBack = callBack;
    }

    private GalleryFragment.ICallBack mCallBack;


    @Override
    public void onSuccess(List<ImageItem> list) {
        SysAlertDialog.cancelLoadingDialog();
        mAdapter.addAll(true,list);
    }

    @Override
    public void showLoading() {
        SysAlertDialog.showLoadingDialog(getContext(), R.string.isloading);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRoot=null;
        mRecyclerView = null;
        mAdapter = null;
    }


}
