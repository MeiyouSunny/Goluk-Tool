package com.goluk.crazy.panda.main.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.goluk.crazy.panda.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by leege100 on 2016/8/18.
 */
public class FragmentSquarePager extends Fragment {

    View mRootView;
    @BindView(R.id.iv_image)
    ImageView ivImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_pager, container, false);
        ButterKnife.bind(this, mRootView);
        Glide.with(this)
                .load(R.mipmap.test_square_pager)
                .into(ivImage);
        return mRootView;
    }
}
