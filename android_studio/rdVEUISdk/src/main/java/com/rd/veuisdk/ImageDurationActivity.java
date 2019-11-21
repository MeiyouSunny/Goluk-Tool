package com.rd.veuisdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.RulerSeekbar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * 图片时长
 */
public class ImageDurationActivity extends BaseActivity {
    private Scene mScene;
    private float mCurTime;
    private TextView mTvTitle;
    private VirtualVideoView mMediaPlayer;
    private ImageView mIvPlayState;
    private SeekBar mSbEditor;
    private TextView mTvCurrentDuration;
    private RulerSeekbar mDragDuration;
    private ArrayList<EffectInfo> mEffectInfos;
    private TextView tvDuration;
    private CheckBox cbApplyToAll;

    /**
     * 图片时长
     */
    static void onImageDuration(Context context, Scene scene, boolean needExport, int requestCode) {
        Intent intent = new Intent(context, ImageDurationActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.INTENT_NEED_EXPORT, needExport);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 是否需要导出视频
     */
    private boolean bExportVideo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "ImageDurationActivity";
        setContentView(R.layout.activity_image_duration);
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (null == mScene) {
            finish();
            return;
        }
        bExportVideo = getIntent().getBooleanExtra(IntentConstants.INTENT_NEED_EXPORT, false);
        MediaObject mMedia = mScene.getAllMedia().get(0);
        mEffectInfos = new ArrayList<>();
        if (null != mMedia.getEffectInfos()) {
            mEffectInfos.addAll(mMedia.getEffectInfos());
        }
        mVirtualvideo = new VirtualVideo();
        mMedia.setEffectInfos(null);

        initViews();
        initPlayer();
        mCurTime = mScene.getDuration();
        onPlay(mCurTime);
        if (bExportVideo) {
            cbApplyToAll.setVisibility(View.GONE);
        }
        mDragDuration.setOnSeekListener(mOnSeekListener);
        mDragDuration.setMax(100);
        mDragDuration.post(new Runnable() {
            @Override
            public void run() {
                resetTimeText(mCurTime);
                mDragDuration.setProgress(getProgress(mCurTime, mDragDuration.getMax()));
            }
        });
    }


    private RulerSeekbar.OnSeekListener mOnSeekListener = new RulerSeekbar.OnSeekListener() {

        @Override
        public void onSeekStart(float progress, int max) {
            resetTimeText(getTime(progress, max));
        }

        @Override
        public void onSeek(float progress, int max) {
            resetTimeText(getTime(progress, max));
        }

        @Override
        public void onSeekEnd(float progress, int max) {
            mCurTime = getTime(progress, max);
            onPlay(mCurTime);
        }
    };


    private void resetTimeText(float time) {
        tvDuration.setText(mDecimalFormat.format(time) + "秒");
    }

    private final DecimalFormat mDecimalFormat = new DecimalFormat("##0.00");
    private final float MIN = 0.1f;
    private final float DU = 8f - MIN;

    private float getTime(float progress, int max) {
        float time = MIN + DU * progress / max;
        return time;
    }

    private float getProgress(float time, int max) {
        float progress = max * (time - MIN) / DU;
        return progress;
    }

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
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }
        super.onDestroy();
        if (null != mVirtualvideo) {
            mVirtualvideo.release();
            mVirtualvideo = null;
        }
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
        mTvTitle = $(R.id.tvBottomTitle);
        mMediaPlayer = $(R.id.vvMediaPlayer);
        mIvPlayState = $(R.id.ivPlayerState);
        mSbEditor = $(R.id.sbEditor);
        mTvCurrentDuration = $(R.id.tvEditorDuration);
        mDragDuration = $(R.id.dragViewDuration);
        tvDuration = $(R.id.tvCurDuration);
        cbApplyToAll = $(R.id.cbDurationApplyToAll);
        mTvTitle.setText(R.string.photo_duration);
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

    private VirtualVideo mVirtualvideo = null;

    private void onPlay(float nProgress) {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mVirtualvideo.reset();
        for (MediaObject mo : mScene.getAllMedia()) {
            mo.setIntrinsicDuration(nProgress);
            mo.setTimeRange(0, nProgress);
        }
        reload(mVirtualvideo);
        try {
            mVirtualvideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        videoPlay();
    }

    private void reload(VirtualVideo virtualVideo) {
        virtualVideo.addScene(mScene);
    }


    public void clickView(View v) {
        int id = v.getId();
        if (id == R.id.ivCancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();

        } else if (id == R.id.ivSure) {
            if (bExportVideo) {
                onExport();
            } else {
                Intent intent = new Intent();
                MediaObject mo = mScene.getAllMedia().get(0);
                VideoOb vo = (VideoOb) mo.getTag();
                vo.nStart = mo.getTrimStart();
                vo.nEnd = mo.getIntrinsicDuration();
                vo.rStart = vo.nStart;
                vo.rEnd = vo.nEnd;
                vo.TStart = vo.nStart;
                vo.TEnd = vo.nEnd;
                //修正特效有效时间线
                EffectManager.fixEffect(mo, mEffectInfos);
                if (cbApplyToAll.isChecked()) {
                    intent.putExtra(
                            IntentConstants.EXTRA_EXT_APPLYTOALL_DURATION, mCurTime);
                } else {
                    intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mScene);
                }
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        }
    }


    private void onExport() {
        onPause();
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
            }
        });
        VideoConfig videoConfig = exportHandler.getExportConfig(0);
        exportHandler.onExport(true, videoConfig);
    }


    private void initPlayer() {
        mMediaPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {

            @Override
            public void onPlayerPrepared() {
                int ms = Utils.s2ms(mMediaPlayer.getDuration());
                mSbEditor.setMax(ms);
                mTvCurrentDuration.setText(getProgressStr(ms));
                float asp = mMediaPlayer.getVideoWidth() / (mMediaPlayer.getVideoHeight() + 0.0f);
                ((PreviewFrameLayout) $(R.id.rlVideoCropFramePreview)).setAspectRatio(asp);
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e("onPlayerError", "what.." + what + ".....extra" + extra);
                onToast(R.string.preview_error);
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
        onPlay(mScene.getDuration());
    }


    private static final int DIALOG_APPLYTOALL_ID = 1;

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;
        if (id == DIALOG_APPLYTOALL_ID) {
            dialog = SysAlertDialog.showAlertDialog(this, "", getString(R.string.image_duration_apply_to_all),
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
