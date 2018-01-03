package com.goluk.ipcdemo;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.goluk.ipcsdk.utils.GolukIPCUtils;

/**
 * Created by leege100 on 16/6/2.
 */
public class RealTimePlayActivity extends FragmentActivity implements View.OnClickListener{
    private VideoView mRtmpPlayerView;
    private Button mPlayBt;
    private Button mStopBt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_play);
        initView();
        setupView();
        start();
    }

    @Override
    protected void onDestroy() {
        if(mRtmpPlayerView != null){
            //mRtmpPlayerView.cleanUp();
            mRtmpPlayerView = null;
        }
        super.onDestroy();
    }

    private void initView() {
        mRtmpPlayerView = (VideoView) findViewById(R.id.mRtspPlayerView);
        mPlayBt = (Button) findViewById(R.id.bt_play);
        mStopBt = (Button) findViewById(R.id.bt_stop);
    }

    private void setupView() {
        //mRtmpPlayerView.setAudioMute(true);
        mRtmpPlayerView.setZOrderMediaOverlay(true);
        //mRtmpPlayerView.setBufferTime(1000);
        //mRtmpPlayerView.setConnectionTimeout(30000);
        mRtmpPlayerView.setVisibility(View.VISIBLE);
        mRtmpPlayerView.setVisibility(View.VISIBLE);

        WindowManager wm1 = this.getWindowManager();
        int screenWidth = wm1.getDefaultDisplay().getWidth();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRtmpPlayerView.getLayoutParams();
        lp.width = screenWidth;
        lp.height = (int) (screenWidth / 1.7833);
        lp.leftMargin = 0;
        mRtmpPlayerView.setLayoutParams(lp);

        mPlayBt.setOnClickListener(this);
        mStopBt.setOnClickListener(this);
    }

    public void start() {
        if (null != mRtmpPlayerView) {
            String url = GolukIPCUtils.getRtmpPreviewUrl();
            if(!TextUtils.isEmpty(url)){
                //mRtmpPlayerView.setDataSource(url);
                mRtmpPlayerView.setVideoURI(Uri.parse(url));
                mRtmpPlayerView.start();
            }
        }
    }

    public void stop(){
        if (null != mRtmpPlayerView && mRtmpPlayerView.isPlaying()) {
            mRtmpPlayerView.stopPlayback();
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_play:
                start();
                break;
            case R.id.bt_stop:
                stop();
                break;
            default:
                break;
        }
    }
}
