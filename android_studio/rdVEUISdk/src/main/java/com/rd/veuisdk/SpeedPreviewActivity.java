package com.rd.veuisdk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.DragItemScrollView;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 视频调速
 *
 * @author jian
 */
public class SpeedPreviewActivity extends BaseActivity {
    private static final String TAG = "SpeedPreviewActivity";

    @BindView(R2.id.rlPreview)
    PreviewFrameLayout mPflVideoPreview;
    @BindView(R2.id.tvEditorDuration)
    TextView mTvVideoDuration;
    @BindView(R2.id.ivPlayerState)
    ImageView mIvVideoPlayState;
    @BindView(R2.id.epvPreview)
    VirtualVideoView mMediaPlayer;
    @BindView(R2.id.pbPreview)
    RdSeekBar mPbPreview;
    @BindView(R2.id.dragViewSpeed)
    DragItemScrollView mDragSpeed;


    private int mLastPlayPostion;
    private MediaObject mMedia;
    private int mSpeedIndex;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_prieview);
        ButterKnife.bind(this);

        mStrActivityPageName = getString(R.string.speed_priview);

        Scene scene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        mMedia = scene.getAllMedia().get(0);
        mOldSpeed = mMedia.getSpeed();
        initView();
        onLoad();
        playVideo();
        mSpeedIndex = getSpeedIndex(mMedia.getSpeed());
        mDragSpeed.postDelayed(new Runnable() {

            @Override
            public void run() {
                mDragSpeed.setCheckIndex(mSpeedIndex);
            }
        }, 200);

        mDragSpeed.setCheckedChangedListener(mOnScrollListener);
    }

    private int getSpeedIndex(float speed) {
        int mIndex = 2;
        if (speed == 0.25f) {
            mIndex = 0;
        } else if (speed == 0.5f) {
            mIndex = 1;
        } else if (speed == 2f) {
            mIndex = 3;
        } else if (speed == 4f) {
            mIndex = 4;
        } else {
            mIndex = 2;
        }
        return mIndex;
    }

    private float getSpeed(int mSpeedIndex) {
        float speed = 1f;
        if (mSpeedIndex == 0) {
            speed = 0.25f;
        } else if (mSpeedIndex == 1) {
            speed = 0.5f;
        } else if (mSpeedIndex == 2) {
            speed = 1f;
        } else if (mSpeedIndex == 3) {
            speed = 2f;
        } else if (mSpeedIndex == 4) {
            speed = 4f;
        } else {
            speed = 1f;
        }
        return speed;
    }

    private DragItemScrollView.onCheckedListener mOnScrollListener = new DragItemScrollView.onCheckedListener() {

        @Override
        public void onCheckedChanged(boolean user, int mSpeedIndex) {

            if (user) {
                float nspeed = getSpeed(mSpeedIndex);
                mMedia.setSpeed(nspeed);
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                onLoad();
                playVideo();
            }

        }
    };

    /**
     * 加载媒体资源
     */
    private void onLoad() {
        mPbPreview.setHighLights(null);
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMedia);
        VirtualVideo virtualVideo = new VirtualVideo();
        try {
            virtualVideo.addScene(scene);
            virtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private OnClickListener mOnPlayerClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    };

    private VirtualVideoView.VideoViewListener mPlayerListener = new VirtualVideoView.VideoViewListener() {

        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            int ms = Utils.s2ms(mMediaPlayer.getDuration());
            mPbPreview.setMax(ms);
            mTvVideoDuration.setText(DateTimeUtils.stringForMillisecondTime(
                    ms, true, true));
            updatePreviewFrameAspect(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            com.rd.veuisdk.utils.Utils.autoToastNomal(
                    SpeedPreviewActivity.this, R.string.preview_error);
            onBackPressed();
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            onComplete();
        }

        @Override
        public void onGetCurrentPosition(float position) {
            mPbPreview.setProgress(Utils.s2ms(position));
        }
    };


    private void initView() {
        PreviewFrameLayout layout = (PreviewFrameLayout) findViewById(R.id.rlPreviewLayout);

        layout.setAspectRatio(AppConfiguration.ASPECTRATIO);
        TextView title = (TextView) findViewById(R.id.tvTitle);
        title.setText(mStrActivityPageName);
        findViewById(R.id.btnLeft).setVisibility(View.INVISIBLE);

        mPflVideoPreview.setClickable(true);


        mLastPlayPostion = -1;

        mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreview);
        mMediaPlayer.setOnClickListener(mOnPlayerClickListener);
        mMediaPlayer.setOnPlaybackListener(mPlayerListener);
        mMediaPlayer.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {

                if (what == VirtualVideoView.INFO_WHAT_PLAYBACK_PREPARING) {
                    SysAlertDialog.showLoadingDialog(SpeedPreviewActivity.this,
                            R.string.isloading, false, null);
                    VirtualVideo v;
                } else if (what == VirtualVideo.MEDIA_INFO_GET_VIDEO_HIGHTLIGHTS) {
                    int[] ls = (int[]) obj;
                    mPbPreview.setHighLights(ls);
                }

                return false;
            }
        });

    }

    private void onComplete() {
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        mMediaPlayer.seekTo(0);
        mPbPreview.setProgress(0);
    }

    /**
     * 更新预览视频播放器比例
     */
    protected void updatePreviewFrameAspect(int nVideoWidth, int nVideoHeight) {

        mPflVideoPreview.setAspectRatio((float) nVideoWidth / nVideoHeight);

    }

    private float mOldSpeed = 1.0f;

    @Override
    public void clickView(View v) {
        super.clickView(v);
        int id = v.getId();
        if (id == R.id.rlPreview) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        } else if (id == R.id.public_menu_cancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();
        } else if (id == R.id.public_menu_sure) {
            Intent data = new Intent();
            Scene scene = new Scene();

            VideoOb temp = (VideoOb) mMedia.getTag();
            if (null != temp) {
                float ft = mMedia.getSpeed() / mOldSpeed;
                temp.nStart = temp.nStart / ft;
                temp.nEnd = temp.nEnd / ft;
                mMedia.setTag(temp);
            }
            scene.addMedia(mMedia);

            data.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
            setResult(RESULT_OK, data);
            onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
            mLastPlayPostion = Utils.s2ms(mMediaPlayer.getCurrentPosition());
            pauseVideo();
        }

    }

    @Override
    protected void onDestroy() {
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayer.start();
        // 从后台切换到前台，并且已经成功打开正常的视频，进行继续播放
        if (mLastPlayPostion == 0) {
            mMediaPlayer.seekTo(0);
        }
        if (mLastPlayPostion > 0) {
            if (null != mMediaPlayer) {
                mMediaPlayer.seekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
                playVideo();
            }
        }
    }

    private void playVideo() {
        mMediaPlayer.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(this, mIvVideoPlayState);
    }

    private void pauseVideo() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }
}
