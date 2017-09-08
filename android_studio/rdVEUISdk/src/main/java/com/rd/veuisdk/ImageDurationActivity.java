package com.rd.veuisdk;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.DragItemScrollView;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

/**
 * 图片时长
 *
 * @author JIAN
 */
public class ImageDurationActivity extends BaseActivity {
    private static final String TAG = "ImageDurationActivity";
    private Scene mScene;
    int mTimeIndex = 0;
    private int mCurTime;

    ExtButton mBtnLeft;
    TextView mTvTitle;
    VirtualVideoView mMediaPlayer;
    ImageView mIvPlayState;
    SeekBar mSbEditor;
    TextView mTvCurrentDuration;
    DragItemScrollView mDragDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.photo_duration);
        setContentView(R.layout.activity_image_duration);
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (null == mScene) {
            finish();
            return;
        }
        initViews();
        initPlayer();
        mTimeIndex = getIndex((int) mScene.getDuration());
        mCurTime = mArrTimes[mTimeIndex];
        onPlay(mArrTimes[mTimeIndex]);
        mDragDuration.setCheckedChangedListener(mOnScrollListener);
        mDragDuration.post(new Runnable() {
            @Override
            public void run() {
                mDragDuration.setCheckIndex(mTimeIndex);
            }
        });
    }

    private int getIndex(int duration) {
        int mTimeIndex = 0;
        for (int i = 0; i < mArrTimes.length; i++) {
            if (mArrTimes[i] + 0.2 >= duration && duration >= mArrTimes[i] - 0.2) {
                mTimeIndex = i;
                break;
            }
        }
        return mTimeIndex;

    }

    private int[] mArrTimes = new int[]{3, 4, 5, 6, 7};

    private DragItemScrollView.onCheckedListener mOnScrollListener = new DragItemScrollView.onCheckedListener() {

        @Override
        public void onCheckedChanged(boolean user, int mTimeIndex) {
            if (user) {
                mCurTime = mArrTimes[mTimeIndex];
                onPlay(mCurTime);
            }
        }
    };

    @Override
    protected void onPause() {
        videoPause();
        super.onPause();
    }

    private void videoPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        setImageViewSrc(R.id.ivPlayerState, R.drawable.btn_play);
        mIvPlayState.setVisibility(View.VISIBLE);
    }

    private void videoPlay() {
        mMediaPlayer.start();
        setImageViewSrc(R.id.ivPlayerState, R.drawable.btn_pause);
        ViewUtils.fadeOut(this, mIvPlayState);
    }

    @Override
    protected void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mMediaPlayer.stop();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                ImageDurationActivity.this.finish();
                ImageDurationActivity.this.overridePendingTransition(0, 0);
            }
        }, 300);

    }

    private void initViews() {
        mBtnLeft = (ExtButton) findViewById(R.id.btnLeft);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        mMediaPlayer = (VirtualVideoView) findViewById(R.id.vvMediaPlayer);
        mIvPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        mSbEditor = (SeekBar) findViewById(R.id.sbEditor);
        mTvCurrentDuration = (TextView) findViewById(R.id.tvEditorDuration);
        mDragDuration = (DragItemScrollView) findViewById(R.id.drag_image_duration);

        PreviewFrameLayout layout = (PreviewFrameLayout) findViewById(R.id.rlImageLayout);
        layout.setAspectRatio(AppConfiguration.ASPECTRATIO);
        mBtnLeft.setVisibility(View.INVISIBLE);
        mTvTitle.setText(mStrActivityPageName);

        mIvPlayState.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    videoPause();
                } else {
                    videoPlay();
                }
            }
        });
        mSbEditor.setMax(Utils.s2ms(mScene.getDuration()));
    }

    private String getProgressStr(int nprogress) {
        return DateTimeUtils.stringForMillisecondTime(nprogress, true, true);
    }

    private void onPlay(int nProgress) {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        for (MediaObject mo : mScene.getAllMedia()) {
            mo.setIntrinsicDuration(nProgress);
            mo.setTimeRange(0, nProgress);
        }
        VirtualVideo virtualVideo = new VirtualVideo();
        virtualVideo.addScene(mScene);
        try {
            virtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        videoPlay();
    }

    public void clickView(View v) {
        int id = v.getId();
        if (id == R.id.public_menu_cancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();

        } else if (id == R.id.public_menu_sure) {
            Intent intent = new Intent();
            for (MediaObject mo : mScene.getAllMedia()) {
                VideoOb vo = (VideoOb) mo.getTag();
                vo.nStart = mo.getTrimStart();
                vo.nEnd = mo.getIntrinsicDuration();
                vo.rStart = vo.nStart;
                vo.rEnd = vo.nEnd;
                vo.TStart = vo.nStart;
                vo.TEnd = vo.nEnd;
            }
            intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mScene);
            setResult(RESULT_OK, intent);
            onBackPressed();
        } else if (id == R.id.durationApplyToAll) {
            onCreateDialog(DIALOG_APPLYTOALL_ID).show();
        }

    }

    private void initPlayer() {
        mMediaPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {

            @Override
            public void onPlayerPrepared() {
                int ms = Utils.s2ms(mMediaPlayer.getDuration());
                mSbEditor.setMax(ms);
                mTvCurrentDuration.setText(getProgressStr(ms));
                onVideoViewPrepared(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e("onPlayerError", "what.." + what + ".....extra" + extra);
                SysAlertDialog.showAutoHideDialog(ImageDurationActivity.this,
                        R.string.string_null, R.string.error_preview_retry, Toast.LENGTH_SHORT);
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                setImageViewSrc(R.id.ivPlayerState, R.drawable.btn_play);
                setViewVisibility(R.id.ivPlayerState, true);
            }

            @Override
            public void onGetCurrentPosition(float position) {
                mSbEditor.setProgress(Utils.s2ms(position));
            }


        });
        mMediaPlayer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    videoPause();
                } else {
                    videoPlay();
                }
            }
        });
        onPlay((int) mScene.getDuration());
    }

    protected void onVideoViewPrepared(int nVideoWidth, int nVideoHeight) {
        PreviewFrameLayout pflVideoTrim = (PreviewFrameLayout) findViewById(R.id.rlVideoCropFramePreview);
        pflVideoTrim.setAspectRatio((double) nVideoWidth / nVideoHeight);
    }

    private static final int DIALOG_APPLYTOALL_ID = 1;

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;
        String strMessage = null;
        if (id == DIALOG_APPLYTOALL_ID) {
            strMessage = getString(R.string.image_duration_apply_to_all);
            dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.no),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.yes),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.putExtra(
                                    IntentConstants.EXTRA_EXT_APPLYTOALL_DURATION, mCurTime);
                            setResult(RESULT_OK, intent);
                            onBackPressed();
                        }
                    });
        }
        return dialog;
    }

}
