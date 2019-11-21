package com.rd.veuisdk.fragment.splice;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.SpliceModeAdapter;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.SpliceModeInfo;
import com.rd.veuisdk.utils.ISpliceHandler;

import java.util.List;

/**
 * 拼接-模板、比例
 */
public class SpliceModeFragment extends BaseFragment {
    private RecyclerView mRecyclerView;
    private SpliceModeAdapter mAdapter;
    private List<SpliceModeInfo> mList;


    public static SpliceModeFragment newInstance() {
        Bundle args = new Bundle();

        SpliceModeFragment fragment = new SpliceModeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setList(List<SpliceModeInfo> list) {
        mList = list;
    }

    @Override
    public void recycle() {
        super.recycle();
        if (null != mList) {
            mList.clear();
            mList = null;
        }
    }

    private ISpliceHandler mSpliceHandler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSpliceHandler = (ISpliceHandler) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_splice_style_layout, container, false);
        mRecyclerView = $(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new SpliceModeAdapter(mList, mSpliceHandler.getCheckedModeIndex());
        mAdapter.setOnItemClickListener(new OnItemClickListener<SpliceModeInfo>() {
            @Override
            public void onItemClick(int position, SpliceModeInfo item) {
                mSpliceHandler.onMode(position, item);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        return mRoot;
    }


    public static final float ASP_1 = 1.0f;
    private final float ASP_34 = 3 / 4.0f;
    private final float ASP_43 = 4 / 3.0f;
    private final float ASP_169 = 16 / 9.0f;
    private final float ASP_916 = 9 / 16.0f;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RadioGroup radioGroup = $(R.id.rgProportion);
        $(R.id.rbProportion34).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onProportion(ASP_34);
            }
        });
        $(R.id.rbProportion1x1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onProportion(ASP_1);
            }
        });
        $(R.id.rbProportion43).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onProportion(ASP_43);
            }
        });
        $(R.id.rbProportion169).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onProportion(ASP_169);
            }
        });
        $(R.id.rbProportion916).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onProportion(ASP_916);
            }
        });
        if (mSpliceHandler.getProportion() == ASP_34) {
            radioGroup.check(R.id.rbProportion34);
        } else if (mSpliceHandler.getProportion() == ASP_1) {
            radioGroup.check(R.id.rbProportion1x1);
        } else if (mSpliceHandler.getProportion() == ASP_43) {
            radioGroup.check(R.id.rbProportion43);
        } else if (mSpliceHandler.getProportion() == ASP_169) {
            radioGroup.check(R.id.rbProportion169);
        } else if (mSpliceHandler.getProportion() == ASP_916) {
            radioGroup.check(R.id.rbProportion916);
        }
    }
}
