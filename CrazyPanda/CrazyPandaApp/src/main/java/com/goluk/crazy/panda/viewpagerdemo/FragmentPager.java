package com.goluk.crazy.panda.viewpagerdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goluk.crazy.panda.R;

/**
 * Created by leege100 on 2016/8/18.
 */
public class FragmentPager extends Fragment {

    View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_pager,container,false);
        return mRootView;
    }
}
