package com.goluk.ipcdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.goluk.ipcsdk.utils.IPCFileUtils;
import com.rd.car.player.RtspPlayerView;

/**
 * Created by leege100 on 16/6/2.
 */
public class RealTimePlayActivity extends FragmentActivity{
    private RtspPlayerView mRtmpPlayerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_play);
        initView();
        setupView();
        start();
    }

    private void setupView() {
        mRtmpPlayerView.setAudioMute(true);
        mRtmpPlayerView.setZOrderMediaOverlay(true);
        mRtmpPlayerView.setBufferTime(1000);
        mRtmpPlayerView.setConnectionTimeout(30000);
        mRtmpPlayerView.setVisibility(View.VISIBLE);

//        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRtmpPlayerLayout.getLayoutParams();
//        lp.width = screenWidth;
//        lp.height = (int) (screenWidth / 1.7833);
//        lp.leftMargin = 0;
//        mRtmpPlayerLayout.setLayoutParams(lp);
    }

    public void start() {
        if (null != mRtmpPlayerView) {
            mRtmpPlayerView.setVisibility(View.VISIBLE);
            String url = IPCFileUtils.getRtmpPreviewUrl();
            mRtmpPlayerView.setDataSource(url);
            mRtmpPlayerView.start();
        }
    }

    private void initView() {
        mRtmpPlayerView = (RtspPlayerView) findViewById(R.id.mRtspPlayerView);
    }
}
