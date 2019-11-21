package com.rd.veuisdk.demo.zishuo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.BaseFragment;

import java.util.ArrayList;

/**
 * 题词库 详情
 */
public class InscriptionDetailFragment extends BaseFragment {

    private static final String TITLE_KEY = "title";
    private static final String CONTENT_KEY = "content";

    private TextView mTvTitle;
    private ListTextView mListTextView;

    private ArrayList<String> mContent;
    private String mTitle;

    public static InscriptionDetailFragment newInstance(String title, ArrayList<String> content) {
        InscriptionDetailFragment fragment = new InscriptionDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, title);
        bundle.putStringArrayList(CONTENT_KEY, content);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE_KEY);
            mContent = getArguments().getStringArrayList(CONTENT_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_inscription_detail, container, false);
        init();
        return mRoot;
    }

    private void init() {
        mTvTitle = $(R.id.tv_title);
        mListTextView = $(R.id.ltv_detail);
        //返回
        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.cancel();
                }
            }
        });
        //使用
        $(R.id.btn_use).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    TempZishuoParams.getInstance().setStrings(mContent);
                    mListener.sure();
                }
            }
        });

        mTvTitle.setText(mTitle);
        mListTextView.setStringList(mContent);
    }

    public void setContent(String title, ArrayList<String> content) {
        mContent = content;
        mTitle = title;
        mTvTitle.setText(mTitle);
        mListTextView.setStringList(mContent);
    }

    private InscriptionLibraryFragment.InscriptionListener mListener;

    public void setListener(InscriptionLibraryFragment.InscriptionListener listener) {
        mListener = listener;
    }

}
