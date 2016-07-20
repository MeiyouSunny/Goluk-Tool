package com.mobnote.golukmain.livevideo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;

import cn.com.mobnote.module.location.ILocationFn;

/**
 * Created by leege100 on 2016/7/19.
 */
abstract class AbstractLiveFragment extends Fragment implements ILiveMap , ILocationFn, View.OnClickListener {
    public static final int[] shootImg = { R.drawable.live_btn_6s_record, R.drawable.live_btn_5s_record,
            R.drawable.live_btn_4s_record, R.drawable.live_btn_3s_record, R.drawable.live_btn_2s_record,
            R.drawable.live_btn_1s_record };

    public static final int[] mHeadImg = { 0, R.drawable.editor_boy_one, R.drawable.editor_boy_two,
            R.drawable.editor_boy_three, R.drawable.editor_girl_one, R.drawable.editor_girl_two,
            R.drawable.editor_girl_three, R.drawable.head_unknown };

    public static final int[] mBigHeadImg = { R.drawable.editor_head_feault7, R.drawable.editor_head_boy1,
            R.drawable.editor_head_boy2, R.drawable.editor_head_boy3, R.drawable.editor_head_girl4,
            R.drawable.editor_head_girl5, R.drawable.editor_head_girl6, R.drawable.editor_head_feault7 };

    public LiveActivity mLiveActivity;
    private View mRootView;
    public static final String TAG = "LiveFragment";
    /** 定位按钮 */
    protected Button mLocationBtn = null;
    protected RelativeLayout mMapRootLayout;
    Bundle mSavedInstanceState;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_live,container,false);
        mMapRootLayout = (RelativeLayout) mRootView.findViewById(R.id.layout_live_map);
        mLiveActivity = (LiveActivity) getActivity();
        mLocationBtn = (Button) mRootView.findViewById(R.id.btn_live_location);
        mLocationBtn.setOnClickListener(this);
        GolukApplication.getInstance().addLocationListener(TAG, this);
        initMap(savedInstanceState);
        return mRootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_live_location)
        toMyLocation();
    }
}
