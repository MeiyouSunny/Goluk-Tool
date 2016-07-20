package com.mobnote.golukmain.livevideo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobnote.golukmain.R;

/**
 * Created by leege100 on 2016/7/20.
 */
public class LiveCommentFragment extends Fragment{

    View mRootView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_live_comment,container,false);
        return mRootView;
    }
}
