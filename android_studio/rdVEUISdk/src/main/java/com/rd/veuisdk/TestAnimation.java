package com.rd.veuisdk;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Transition;
import com.rd.vecore.models.TransitionType;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.utils.AnimHandler;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;

import static com.rd.vecore.models.AspectRatioFitMode.IGNORE_ASPECTRATIO;

/**
 * 异形顶点动画和媒体动画(矩形)
 * 作者：JIAN on 2017/11/29 12:37
 */
public class TestAnimation extends BaseActivity {
    private PreviewFrameLayout rlPreview;
    private VirtualVideo mVirtualVideo;
    private VirtualVideoView player;
    private ImageView mIvVideoPlayState;
    static final String VIDEOPATH = "videopath";
    static final String ENABLEANIM = "enableAnim";
    private TextView mTvVideoCurrentPos;
    private TextView mTvVideoDuration;
    private SeekBar mSbPlayControl;
    private String TAG = "TestAnimation";
    private ArrayList<String> arrMediaListPath;

    private AnimHandler animHandler = null;
    private boolean isAnimEd = false;
    private final float ASP = 1.0f;
    private Button btn;
    //默认照片电影模式
    private boolean isAnimMode = true;
    private String musicPath = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_animation);
        findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.edit);
        isAnimEd = false;
        animHandler = new AnimHandler();
        try {
            musicPath = PathUtils.getAssetFileNameForSdcard("asset", "huiyi.mp3");
            CoreUtils.assetRes2File(getAssets(), "music/huiyi.mp3", musicPath);
        } catch (Exception e) {
            musicPath = null;
        }
        rlPreview = (PreviewFrameLayout) findViewById(R.id.rlPreview);
        rlPreview.setAspectRatio(1.0f);
        player = (VirtualVideoView) findViewById(R.id.vvMediaPlayer);
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        isAnimMode = getIntent().getBooleanExtra(ENABLEANIM, true);
        arrMediaListPath = getIntent().getStringArrayListExtra(VIDEOPATH);
        if (null == arrMediaListPath || arrMediaListPath.size() < 1) {
            onBackPressed();
            onToast(getString(R.string.select_medias));
        }
        mTvVideoCurrentPos = (TextView) findViewById(R.id.tvEditorCurrentPos);
        mTvVideoDuration = (TextView) findViewById(R.id.tvEditorDuration);
        mSbPlayControl = (SeekBar) findViewById(R.id.sbEditor);
        rlPreview.setOnClickListener(mOnClickListener);
        mIvVideoPlayState.setOnClickListener(mOnClickListener);
        btn = (Button) findViewById(R.id.testAnim);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAnimEd = (!isAnimEd);
                if (isAnimEd) {
                    btn.setText(getString(R.string.clearanim));
                } else {
                    btn.setText(getString(R.string.addanim));
                }
                try {
                    SysAlertDialog.showLoadingDialog(TestAnimation.this, R.string.isloading);
                    player.stop();
                    player.reset();
                    player.setPreviewAspectRatio(ASP);
                    mSbPlayControl.setProgress(0);
                    pasuePlayerUI();
                    player.setAspectRatioFitMode(IGNORE_ASPECTRATIO);
                    reload(mVirtualVideo);
                    mVirtualVideo.build(player);
                } catch (InvalidStateException e) {
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                exportVideo();

            }
        });
        mSbPlayControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    onSeekTo((int) (1000 * ((0.0 + player.getDuration()) * progress) / seekBar.getMax()));
                }
            }

            boolean isplaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isplaying = player.isPlaying();
                pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (isplaying) {
                    start();
                }
            }
        });


        mVirtualVideo = new VirtualVideo();

        initListener();
        try {
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
            player.reset();
            player.setPreviewAspectRatio(ASP);
            player.setAspectRatioFitMode(AspectRatioFitMode.IGNORE_ASPECTRATIO);
            reload(mVirtualVideo);
            mVirtualVideo.build(player);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        initHandler();
    }

    private String filePath;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (player.isPlaying()) {
                pause();
            } else {
                start();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != player) {
            player.cleanUp();
            player = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.reset();
            mVirtualVideo.release();
        }
    }

    /**
     * 导出视频
     */
    private void exportVideo() {
        player.stop();
        mSbPlayControl.setProgress(0);
        try {
            VirtualVideo mvideo = new VirtualVideo();
            reload(mvideo);
            filePath = PathUtils.getDstFilePath(null);
            VideoConfig videoConfig = new VideoConfig();
            int outWidth = 720;
            videoConfig.setVideoSize(outWidth, (int) (outWidth / ASP));
            videoConfig.setVideoEncodingBitRate(3000 * 1000);
            videoConfig.setVideoFrameRate(24);
            videoConfig.setKeyFrameTime(1);
            //软解
            videoConfig.enableHWDecoder(false);
            //硬编
            videoConfig.enableHWEncoder(true);

            mvideo.export(TestAnimation.this, filePath, videoConfig, mExportListener);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }


    }

    private final int CANCEL_EXPORT = 6;
    private Handler mHandler;
    private Dialog mCancelLoading;

    private void initHandler() {
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == CANCEL_EXPORT) {
                    mCancelLoading = SysAlertDialog.showLoadingDialog(
                            TestAnimation.this,
                            getString(R.string.canceling), false,
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mCancelLoading = null;

                                }
                            });
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (null != mCancelLoading) {
                                mCancelLoading.setCancelable(true);
                            }
                        }
                    }, 5000);
                }
            }
        };
    }

    private ExportListener mExportListener = new ExportListener() {
        private HorizontalProgressDialog epdExport = null;
        private Dialog dialog = null;
        private boolean cancelExport = false;

        @Override
        public boolean onExporting(int nProgress, int nMax) {
            if (null != epdExport) {
                epdExport.setProgress(nProgress);
                epdExport.setMax(nMax);
            }
            if (cancelExport) {
                return false;
            }
            return true;
        }

        @Override
        public void onExportStart() {
            cancelExport = false;
            if (epdExport == null) {
                epdExport = SysAlertDialog.showHoriProgressDialog(
                        TestAnimation.this, getString(R.string.exporting),
                        false, false, new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                cancelExport = true;
                                mHandler.obtainMessage(CANCEL_EXPORT).sendToTarget();

                            }
                        });
                epdExport.setCanceledOnTouchOutside(false);
                epdExport.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                    @Override
                    public void onCancel() {
                        dialog = SysAlertDialog.showAlertDialog(
                                TestAnimation.this, "",
                                getString(R.string.cancel_export),
                                getString(R.string.no),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }

                                }, getString(R.string.yes),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (epdExport != null) {
                                            epdExport.cancel();
                                        }
                                    }
                                });
                    }
                });
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }
            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onExportEnd(int nResult) {

            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (!TestAnimation.this.isFinishing()) {
                if (epdExport != null) {
                    epdExport.dismiss();
                    epdExport = null;
                }
                if (dialog != null) {
                    dialog.dismiss();
                    dialog.cancel();
                    dialog = null;
                }
            }
            Log.e(TAG, "onExportEnd: " + nResult);
            if (nResult >= VirtualVideo.RESULT_SUCCESS) {

                Intent intent = new Intent();
                intent.putExtra(SdkEntry.EDIT_RESULT,
                        filePath);
                setResult(RESULT_OK, intent);
                TestAnimation.this.finish();
            } else {
                if (null != mCancelLoading) {
                    mCancelLoading.cancel();
                }
                new File(filePath).delete();
                try {
                    reload(mVirtualVideo);
                    start();
                } catch (InvalidStateException e) {
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 加载媒体资源
     */
    private void reload(VirtualVideo mVirtualVideo) throws InvalidStateException, InvalidArgumentException {

        mVirtualVideo.reset();

        int count = 2;//(防止添加两个场景)
        float lineDuration = 0;
        for (int j = 0; j < count; j++) {
            Scene scene = VirtualVideo.createScene();
//            if (isAnimMode) {
//                        组合排列
            scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
//            } else {
////                //线性排列
//                scene.setPermutationMode(PermutationMode.LINEAR_MODE);
//            }
            int len = arrMediaListPath.size();

            for (int i = 0; i < len; i++) {
                MediaObject mediaObject = new MediaObject(this, arrMediaListPath.get(i));
                float duration = 0;
                if (mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                    duration = 15;
                    //必须设置持续时间时间
                    mediaObject.setIntrinsicDuration(duration);
                    mediaObject.setAlpha(0.5f);
//                    //设置的时间线基于当前场景的时间线，而非虚拟视频的时间线
                } else {
                    //视频设置独立的音频模式，方便控制视频原音大小
                    mediaObject.setIndependentMixFactor(true);
                    mediaObject.setMixFactor(80);
                    duration = mediaObject.getDuration();
                }
                RectF showRect = new RectF();
                if (i < 4) {
                    showRect.set(0.25f * i, 0.1f, 0.25f * (i + 1), 0.45f);
                } else {
                    int n = i - 4;
                    showRect.set(0.25f * n, 0.6f, 0.25f * (n + 1), 0.96f);
                }
                if (isAnimMode) {
                    //照片电影相关
                    if (isAnimEd) {
                        //照片加动画
                        mediaObject.setShowRectF(showRect);
                        mediaObject.setAnimationList(animHandler.createAnimList(i + 1, showRect, duration));
                    } else {
                        //不加动画
                        mediaObject.setShowRectF(showRect);
                    }
                    //更加显示区域进行crop原始区域的内容
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                } else {
                    //异形顶点相关
                    //显示原始媒体的全部内容
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
                    if (isAnimEd) {
                        mediaObject.setShowRectF(new RectF(0, 0, 1f, 1f));
                        //带动画异形+动画(显示全部视频图片内容)
                        mediaObject.setAnimationList(animHandler.createAnimList(i % 2 == 0 ? 102 : 101, showRect, duration));
//                        mediaObject.setAnimationList(animHandler.createAnimList((i % 2) == 0 ? 100 : 101, showRect, duration));
                    } else {
                        //静态异形显示(显示全部视频图片内容)
                        mediaObject.setShowPointFs(new PointF(0.2f, 0.3f), new PointF(0.95f, 0.4f), new PointF(0.1f, 0.5f), new PointF(0.8f, 0.8f));
                        //防止有竖屏+横屏媒体时，竖屏的媒体出现两层
                        mediaObject.setBackgroundVisiable(false);
                    }
                }
                scene.addMedia(mediaObject);

            }
            if (j < (count - 1)) {
                //标准转场
//                    Transition transition = new Transition(TransitionType.TRANSITION_TO_DOWN);
//                    自定义转场
                Transition transition = new Transition(TransitionType.TRANSITION_GRAY, "asset:///transition/transition_180.JPG");
                transition.setDuration(2f);
                scene.setTransition(transition);
            } else {
                //最后一个场景，不要添加转场
            }
//            lineDuration += duration;
            mVirtualVideo.addScene(scene);
        }
        Log.e(TAG, "reload: " + lineDuration);
        if (FileUtils.isExist(musicPath)) {
            Music ao = VirtualVideo.createMusic(musicPath);
            if (null != ao) {
                ao.setMixFactor(50);
//                ao.setTimelineRange(0, lineDuration);
                mVirtualVideo.addMusic(ao);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    public void start() {
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(this, mIvVideoPlayState);
    }

    public void pause() {

        if (player.isPlaying()) {
            player.pause();
        }

        pasuePlayerUI();

    }

    private void pasuePlayerUI() {
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }

    private void initListener() {
        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                return false;
            }
        });
        player.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {
                SysAlertDialog.cancelLoadingDialog();
                Log.e(TAG, "onPlayerPrepared: " + player.getDuration());
                float asp = player.getVideoWidth() / (player.getVideoHeight() + 0.0f);
//                rlPreview.setAspectRatio(asp);
//                rlPreviewControl.setAspectRatio(asp);
                mSbPlayControl.setMax(Utils.s2ms(player.getDuration()));
                pasuePlayerUI();

            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                SysAlertDialog.cancelLoadingDialog();
                com.rd.veuisdk.utils.Utils.autoToastNomal(TestAnimation.this,
                        R.string.preview_error);
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                onComplete();

            }

            @Override
            public void onGetCurrentPosition(float position) {
                int p = Utils.s2ms(position);
                mSbPlayControl.setProgress(p);
                mTvVideoCurrentPos.setText(gettime(p));
            }
        });
    }


    private String gettime(int progress) {
        return DateTimeUtils.stringForMillisecondTime(progress, false, true);
    }

    private View.OnClickListener mPlayStateListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            clickView(v);
        }
    };

    /**
     * 单位：毫秒
     *
     * @param seekto
     */
    private void onSeekTo(int seekto) {
        player.seekTo((seekto / 1000.0f));
        mTvVideoCurrentPos.setText(gettime(seekto));
    }

    private void onComplete() {
        onSeekTo(0);
        mSbPlayControl.setProgress(0);
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        mTvVideoCurrentPos.setText(DateTimeUtils.stringForMillisecondTime(0,
                false, true));

        Log.e(TAG, "onComplete: ");
    }


}
