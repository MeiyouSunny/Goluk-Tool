package com.rd.veuisdk;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateRelativeLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.vecore.RdVECore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AnimationObject;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MVInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.ui.ExtCircleImageView;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.AnimHandler;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.rd.vecore.VirtualVideo.getMediaInfo;

/**
 * 搞怪小视频-api实现 （简影演示）
 */
public class SilhouetteActivity extends BaseActivity {
    private String TAG = "SilhouetteActivity";

    private static final int CANCEL_EXPORT = 1 << 7;//取消导出

    private PreviewFrameLayout mPreviewFrame;
    private ExtButton mBtnNext;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private ImageView mIvVideoPlayState;

    private RdSeekBar mRdSeekBar;
    private TextView currentTv;
    private TextView totalTv;

    private ExtCircleImageView mIvMvNone;
    private ExtCircleImageView mCivSky;
    private ExtCircleImageView mCivAd;
    private ExtCircleImageView mCivGoodNight;

    private RotateRelativeLayout mProgressLayout;
    private int mCurMvId;

    private MediaObject mMainMedia; //可替换媒体
    private CaptionLiteObject mCaptionLiteObject;//在最上层的字幕
    private MediaObject mSkyBg;
    private VirtualVideo mVirtualVideo;

    private String MV_AD_PATH, MV_SKY_PATH, MEDIA_SKY, MV_GOODNIGHT_PATH;

    MVInfo skyInfo;
    MVInfo adInfo;
    MVInfo goodnightInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MV_SKY_PATH = new File(PathUtils.getRdAssetPath(), "jy_sky.zip").getAbsolutePath();
        MV_AD_PATH = new File(PathUtils.getRdAssetPath(), "jy_ad.zip").getAbsolutePath();
        MEDIA_SKY = new File(PathUtils.getRdAssetPath(), "jy_sky_asset4.mp4").getAbsolutePath();
        MV_GOODNIGHT_PATH = new File(PathUtils.getRdAssetPath(), "jy_goodnight.zip").getAbsolutePath();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_silhouette_layout);
        initView();

        mMainMedia = (MediaObject) getIntent().getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST).get(0);
        mMainMedia.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
        mTvTitle.setText(R.string.priview_title);
        mPreviewFrame.setAspectRatio(9f / 16f);
        player.setPreviewAspectRatio(9f / 16f);
        player.setAutoRepeat(true);
        mVirtualVideo = new VirtualVideo();


        player.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!bInterceptRepeat) {
                    //防止重复点击
                    bInterceptRepeat = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bInterceptRepeat = false;
                        }
                    }, 500);

                    if (isPlayingORecording) {
                        pause();
                    } else {
                        start();
                    }
                }
            }
        });
        initPlayerListener(player);
        exportMvResource();
    }

    private void start() {
        isPlayingORecording = true;
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(SilhouetteActivity.this, mIvVideoPlayState);
    }

    /**
     * 暂停播放
     */
    private void pause() {
        player.pause();
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        isPlayingORecording = false;
    }

    private void initView() {
        mProgressLayout = (RotateRelativeLayout) findViewById(R.id.rlPlayerBottomMenu);
        mRdSeekBar = (RdSeekBar) findViewById(R.id.sbEditor);
        currentTv = (TextView) findViewById(R.id.tvCurTime);
        totalTv = (TextView) findViewById(R.id.tvTotalTime);
        mPreviewFrame = (PreviewFrameLayout) findViewById(R.id.previewFrame);
        mBtnNext = (ExtButton) findViewById(R.id.btnRight);
        mBtnNext.setTextColor(getResources().getColor(R.color.main_orange));
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        player = (VirtualVideoView) findViewById(R.id.player);
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        mBtnNext.setVisibility(View.VISIBLE);
        mBtnNext.setText(R.string.export);
        mIvMvNone = (ExtCircleImageView) findViewById(R.id.ivMvNone);
        mCivGoodNight = (ExtCircleImageView) findViewById(R.id.civGoodNight);
        mCivGoodNight.setBorderWidth((int) getResources().getDimension(R.dimen.circlebuttonborderwidth));
        mCivSky = (ExtCircleImageView) findViewById(R.id.civSky);
        mCivSky.setBorderWidth((int) getResources().getDimension(R.dimen.circlebuttonborderwidth));
        mCivAd = (ExtCircleImageView) findViewById(R.id.civAd);
        mCivAd.setBorderWidth((int) getResources().getDimension(R.dimen.circlebuttonborderwidth));

        //mv恢复默认
        mIvMvNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIvMvNone.isChecked()) {
                    return;
                }
                resetResource();
                mIvMvNone.setChecked(true);
                mIvMvNone.setBackgroundResource(R.drawable.none_filter_p);
                mCurMvId = 0;
                //清除动画
                mMainMedia.setAnimationList(null);
                //设置显示区域
                mMainMedia.setShowRectF(new RectF(0, 0, 1, 1));
                mMainMedia.setTimelineRange(0, 5);
                mMainMedia.setAudioMute(false);
                build();
            }
        });
        // 晚安
        mCivGoodNight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCivGoodNight.isChecked()) {
                    return;
                }
                resetResource();
                mCivGoodNight.setChecked(true);

                mPreviewFrame.setAspectRatio(9f / 16f);
                player.setPreviewAspectRatio(9f / 16f);
                mCurMvId = goodnightInfo.getId();
                mMainMedia.setShowRectF(new RectF(0, 0, 1, 1));
                mMainMedia.setTimelineRange(0, 14);
                mMainMedia.setAudioMute(true);
                mMainMedia.setAnimationList(null);
                mCaptionLiteObject = new CaptionLiteObject(
                        SilhouetteActivity.this, "asset://jyMV/jy_goodnight_subtitle.png");
                mCaptionLiteObject.setFadeInOut(0.5f, 0.5f); //淡入淡出
                mCaptionLiteObject.setTimelineRange(5, 10);

                try {
                    //字幕加滤镜
                    mCaptionLiteObject.changeFilter(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR));
                    mCaptionLiteObject.changeFilter(new VisualFilterConfig(VisualFilterConfig.FILTER_ID_WARM));
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }

//                if (true) {
//                    //矩形没动画
//                    RectF showRectF = new RectF(54 / 540.0f, 100 / 960.f, 125 / 540.0f, 560 / 960.f);
//                    mCaptionLiteObject.setAngle(60);
//                    mCaptionLiteObject.setShowRectF(showRectF);
//                }
//
//                if (true) {
//                     //异形没动画
//                    mCaptionLiteObject.setShowPointFs(new PointF(0.1f, 0.0f), new PointF(0.5f, 0.1f), new PointF(0.15f, 0.75f), new PointF(0.8f, 0.95f));
//
//                }
                float duration = (mCaptionLiteObject.getTimelineEnd() - mCaptionLiteObject.getTimelineStart());
                if (true) {
                    //矩形动画
                    RectF showRectF = new RectF(54 / 540.0f, 100 / 960.f, 125 / 540.0f, 560 / 960.f);
                    mCaptionLiteObject.setShowRectF(showRectF);
                    mCaptionLiteObject.setAnimationList(new AnimHandler().createAnimList(1, showRectF,
                            duration));
                }

//                if (true) {
//                    //异形动画
//                    mCaptionLiteObject.setAnimationList(new AnimHandler().createAnimList(101, null, duration));
//                }
                build();
            }
        });
        // 天空mv
        mCivSky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCivSky.isChecked()) {
                    return;
                }
                mPreviewFrame.setAspectRatio(9f / 16f);
                player.setPreviewAspectRatio(9f / 16f);
                resetResource();
                mCivSky.setChecked(true);
                mCurMvId = skyInfo.getId();
                //添加动画
                List<AnimationObject> animationObjects = new ArrayList<>();
                AnimationObject animation = new AnimationObject();
                animation.setAlpha(0f);
                animation.setAtTime(0f);
                animationObjects.add(animation);

                animation = new AnimationObject();
                animation.setAtTime(3f);
                animation.setAlpha(0.7f);
                animationObjects.add(animation);

                animation = new AnimationObject();
                animation.setAtTime(10f);
                animation.setAlpha(0.7f);
                animationObjects.add(animation);

                mMainMedia.setAnimationList(animationObjects);
                //设置显示区域
                mMainMedia.setShowRectF(new RectF(0, 0, 1, 0.7f));
                mMainMedia.setTimelineRange(7, 17);
                mMainMedia.setAudioMute(true);
                build();
            }
        });
        // 广告
        mCivAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCivAd.isChecked()) {
                    return;
                }
                resetResource();
                mCivAd.setChecked(true);

                // 播放比例设置正方形
                mPreviewFrame.setAspectRatio(1);
                player.setPreviewAspectRatio(1);
                //设置异性顶点位置
                mMainMedia.setShowPointFs(
                        new PointF(0.42f, 0.22f),
                        new PointF(0.78f, 0.09f),
                        new PointF(0.42f, 0.58f),
                        new PointF(0.91f, 0.47f));
                mCurMvId = adInfo.getId();
                mMainMedia.setTimeRange(0, 5);
                mMainMedia.setTimelineRange(0, 5);
                mMainMedia.setAudioMute(true);
                mMainMedia.setAnimationList(null);
                build();
            }
        });

        findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMBtnBackClicked();
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMBtnNextClicked();
            }
        });


        mRdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    float p = Utils.ms2s(progress);
                    player.seekTo(p);
                    currentTv.setText(getFormatTime(progress));
                }
            }

            private boolean isPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if ((isPlaying = player.isPlaying())) {
                    isPlaying = true;
                    player.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPlaying) {
                    player.start();
                }
            }
        });
    }


    private void build() {
        reload(mVirtualVideo);
        try {
            mVirtualVideo.build(player);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        start();
    }

    private void resetResource() {
        mIvMvNone.setBackgroundResource(R.drawable.none_filter_n);
        mIvMvNone.setChecked(false);
        mCivSky.setChecked(false);
        mCivAd.setChecked(false);
        mCivGoodNight.setChecked(false);
        mCaptionLiteObject = null;
    }


    private Dialog mCancelLoading;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CANCEL_EXPORT: {
                    mCancelLoading = SysAlertDialog.showLoadingDialog(
                            SilhouetteActivity.this, R.string.canceling, false,
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (null != exportVideo) {
                                        exportVideo = null;
                                    }
                                    mCancelLoading = null;
                                }
                            });
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null != mCancelLoading)
                                mCancelLoading.setCancelable(true);
                        }
                    }, 5000);
                }
                break;
                default: {
                }
                break;
            }
        }
    };


    private int thumbCount = 2;

    private boolean isPlayingORecording = false;
    private boolean bInterceptRepeat = false;


    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    /**
     * 注册播放器回调
     *
     * @param player
     */
    private void initPlayerListener(final VirtualVideoView player) {

        player.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {

                float dura = player.getDuration();
                SysAlertDialog.cancelLoadingDialog();
                int ms = Utils.s2ms(dura);
                mRdSeekBar.setMax(ms);
                totalTv.setText(getFormatTime(ms));
                onSeekTo(0);
                mProgressLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "mute-onPlayerError: " + what + "..." + extra);
                onSeekTo(0);
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                Log.i(TAG, "onPlayerCompletion:  播放完毕-->" + player.getDuration());
            }

            @Override
            public void onGetCurrentPosition(float position) {
                onSeekTo(position);
            }
        });


        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                Log.i(TAG, "onInfo: " + what + "..." + extra + "..." + obj);
                return true;
            }
        });


    }

    /***
     * 初始化播放器媒体资源
     */
    private void initPlayerData() {
        mVirtualVideo.reset();
        player.reset();
        try {
            player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
            //加载全部视频
            boolean hasVideo = reload(mVirtualVideo);
            if (hasVideo) {
                mVirtualVideo.build(player);
            } else {
                Log.e(TAG, "initPlayerData: 没有视频!");
            }

        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        onSeekTo(0);

        // 注册本地mv
        try {
            goodnightInfo = RdVECore.registerMV(MV_GOODNIGHT_PATH);
            skyInfo = RdVECore.registerMV(MV_SKY_PATH);
            adInfo = RdVECore.registerMV(MV_AD_PATH);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 初始化天空mv背景媒体
        try {
            mSkyBg = new MediaObject(this, MEDIA_SKY);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


    private void onSeekTo(float progress) {
        int tp = Utils.s2ms(progress);
        currentTv.setText(getFormatTime(tp));
        mRdSeekBar.setProgress(tp);
    }

    @Override
    protected void onDestroy() {

        if (null != player) {
            player.setOnPlaybackListener(null);
            player.stop();
            player.cleanUp();
        }
        mVirtualVideo.release();
        super.onDestroy();
    }


    private float lastProgress = -1f;

    @Override
    protected void onStart() {
        super.onStart();
        isPlayingORecording = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (lastProgress != -1f) {
            //*****如果播放器在切到后台时，已经stop(),需要重新build(),再预览
            //还原播放器位置，恢复缩略图
            player.seekTo(lastProgress);
            onSeekTo(lastProgress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastProgress = -1f;

        if (null != player) {
            if (player.isPlaying()) {
                //暂停
                player.pause();
            }
            //记录播放器位置
            lastProgress = player.getCurrentPosition();
        }
    }

    public void onToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {


        String strMessage = getString(R.string.quit_edit);
        Dialog dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialog = null;
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null != player) {
                            player.stop();
                        }
                        finish();
                    }
                });


    }


    private void onMBtnBackClicked() {
        onBackPressed();
    }

    private void onMBtnNextClicked() {
        onExport();
    }


    /**
     * 加载视频资源
     *
     * @param virtualVideo
     */
    private boolean reload(VirtualVideo virtualVideo) {
        virtualVideo.reset();
        Scene scene = VirtualVideo.createScene();
        scene.setDisAspectRatio(player.getPreviewAspectRatio());
        if (mCivSky.isChecked()) {
            // 天空mv 在底层添加一个背景媒体
            scene.addMedia(mSkyBg);
        }
        virtualVideo.setMV(mCurMvId);
        scene.addMedia(mMainMedia);
        scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
        virtualVideo.addScene(scene);
        if (mCaptionLiteObject != null) {
            virtualVideo.addSubtitle(mCaptionLiteObject); //TODO:添加叠加字幕
        }
        return true;
    }

    private VirtualVideo exportVideo;
    private HorizontalProgressDialog vExpDialog;

    /**
     * 点击下一步->导出
     */
    private void onExport() {
        onPause();
        exportVideo = new VirtualVideo();
        if (reload(exportVideo)) {
            //可以导出有资源
            final String outpath = PathUtils.getDstFilePath(null);
            VideoConfig videoConfig = new VideoConfig();
            videoConfig.setCalcSquareSize(true);
            if (mCivAd.isChecked()) {
                videoConfig.setAspectRatio(960, 1);
            } else {
                videoConfig.setAspectRatio(960, 9f / 16f);
            }

            videoConfig.setVideoFrameRate(15);

            exportVideo.export(this, outpath, videoConfig, new ExportListener() {

                @Override
                public void onExportStart() {
                    vExpDialog = SysAlertDialog.showHoriProgressDialog(
                            SilhouetteActivity.this, getString(R.string.exporting),
                            false, false, new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {

                                }
                            });
                    vExpDialog.setCanceledOnTouchOutside(false);
                    vExpDialog.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                        @Override
                        public void onCancel() {
                            SysAlertDialog.showAlertDialog(
                                    SilhouetteActivity.this,
                                    "",
                                    getString(R.string.cancel_export),
                                    getString(R.string.no),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog, int which) {
                                        }
                                    },
                                    getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            vExpDialog.cancel();
                                            vExpDialog.dismiss();
                                            exportVideo.cancelExport();
                                            mHandler.obtainMessage(CANCEL_EXPORT)
                                                    .sendToTarget();
                                        }
                                    });
                        }
                    });
                }

                @Override
                public boolean onExporting(int progress, int max) {
                    if (null != vExpDialog) {
                        vExpDialog.setMax(max);
                        vExpDialog.setProgress(progress);
                    }
                    return true;
                }

                @Override
                public void onExportEnd(int nResult) {
                    exportVideo.release();
                    if (null != vExpDialog) {
                        vExpDialog.cancel();
                        vExpDialog = null;
                    }
                    SysAlertDialog.cancelLoadingDialog();
                    if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                        exportVideo = null;
                        VideoConfig tmp = new VideoConfig();
                        float du = getMediaInfo(outpath, tmp);
                        gotoNext(outpath);
                    } else {
                        if (null != mCancelLoading) {
                            mCancelLoading.cancel();
                            mCancelLoading.dismiss();
                            mCancelLoading = null;
                        }
                        if (nResult == VirtualVideo.RESULT_EXPORT_CANCEL) {
                            onToast(getString(R.string.export_canceled));
                        } else {
                            onToast(getString(R.string.export_failed));
                        }
                        FileUtils.deleteAll(outpath);//清除失败的临时文件
                    }
                }
            });
        } else {
            exportVideo.release();
            onToast(getResources().getString(R.string.album_no_video));
        }
    }


    /**
     * 返回数据
     *
     * @param outpath
     */
    private void gotoNext(String outpath) {
        SdkEntryHandler.getInstance().onExport(this, outpath);
        Intent intent = new Intent();
        intent.putExtra(SdkEntry.EDIT_RESULT, outpath);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 导出mv资源
     */
    @SuppressLint("StaticFieldLeak")
    private void exportMvResource() {
        if (!(PathUtils.fileExists(MV_AD_PATH)
                && PathUtils.fileExists(MV_SKY_PATH)
                && PathUtils.fileExists(MEDIA_SKY)
                && PathUtils.fileExists(MV_GOODNIGHT_PATH))) {
            new AsyncTask<Integer, Integer, Void>() {
                private boolean mGotError;

                @Override
                protected void onPreExecute() {
                    SysAlertDialog.showLoadingDialog(SilhouetteActivity.this, "导出MV资源...");
                }

                @Override
                protected Void doInBackground(Integer... params) {
                    mGotError = true;
                    do {
                        if (!CoreUtils.assetRes2File(getAssets(), "jyMV/jy_sky.zip", MV_SKY_PATH))
                            break;
                        if (!CoreUtils.assetRes2File(getAssets(), "jyMV/jy_ad.zip", MV_AD_PATH))
                            break;
                        if (!CoreUtils.assetRes2File(getAssets(), "jyMV/jy_sky_asset4.mp4", MEDIA_SKY))
                            break;
                        if (!CoreUtils.assetRes2File(getAssets(), "jyMV/jy_goodnight.zip", MV_GOODNIGHT_PATH))
                            break;
                        mGotError = false;
                    } while (false);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    SysAlertDialog.cancelLoadingDialog();
                    if (mGotError) {
                        onToast("MV资源不存在!");
                        finish();
                    } else {
                        initPlayerData();
                    }
                }
            }.execute();
        } else {
            initPlayerData();
        }
    }

}
