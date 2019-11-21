package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.veuisdk.IVideoEditorQuikHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.QuikAdapter;
import com.rd.veuisdk.listener.OnItemClickListener;

/**
 * 仿quik效果，模板切换
 *
 */
public class QuikFragment extends BaseFragment {


    public static QuikFragment newInstance() {
        return new QuikFragment();
    }

    public QuikFragment() {
        super();
    }


    private IVideoEditorQuikHandler mHlrVideoEditor;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mHlrVideoEditor = (IVideoEditorQuikHandler) activity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private RecyclerView mRecyclerView;
    private QuikAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_quik_layout, null);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mAdapter = new QuikAdapter(getContext());
        mAdapter.setOnItemClickListener(new OnItemClickListener<Object>() {
            @Override
            public void onItemClick(int position, Object object) {
                onSelectedImp(position);
            }
        });
        //设置适配器
        mAdapter.addAll(true, mHlrVideoEditor.getQuikHandler().getList(), lastItemId);
        mRecyclerView.setAdapter(mAdapter);
        if (lastItemId == -1) {
            onSelectedImp(0);
        }
        return mRoot;

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mAdapter) {
            mAdapter.setOnItemClickListener(null);
            mAdapter = null;
        }
        mRoot = null;

    }

    private int lastItemId = -1;

    /**
     * @param index
     */
    private void switchItem(int index) {
//        if (index > 0) {
        mHlrVideoEditor.onQuik(mAdapter.getItem(index));
//        } else {
//            mHlrVideoEditor.onQuik(null);
//        }
    }

    public void onSelectedImp(int nItemId) {
//        if (nItemId >= 1) {
        if (lastItemId != nItemId) {
            switchItem(nItemId);
            lastItemId = nItemId;
            mAdapter.onItemChecked(nItemId);
        }
//        } else {
//            lastItemId = nItemId;
//            switchItem(nItemId);
//            mAdapter.onItemChecked(lastItemId);
//        }
//        isf (!mHlrVideoEditor.isPlaying()) {
//            mHlrVideoEditor.start();
//        }

    }

    /**
     * 更新单个Quik效果
     */
    public void updateItem() {
        if (null != mAdapter) {
            mAdapter.updateItem(mHlrVideoEditor.getQuikHandler().getList());
        }
    }
}
