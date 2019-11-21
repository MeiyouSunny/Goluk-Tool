package com.rd.veuisdk;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.CanvasObject;
import com.rd.vecore.models.CustomDrawObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.quik.QuikHandler;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.CustomDrawTextHandler;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

/**
 * 字说 （基于自绘接口实现自说功能）
 */
public class ZishuoDrawActivity extends BaseActivity {
    private String TAG = "ZishuoDrawActivity";
    private PreviewFrameLayout mPreviewFrame;
    private ExtButton mBtnNext, mBtnLeft;
    private TextView mTvTitle;
    private VirtualVideoView player;
    private ImageView mIvVideoPlayState;
    private RdSeekBar mRdSeekBar;
    private TextView currentTv;
    private TextView totalTv;
    private float mCurProportion;
    private VirtualVideo mVirtualVideo;
    private String bgVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_zishuo_layout);
        SysAlertDialog.showLoadingDialog(ZishuoDrawActivity.this, R.string.isloading);
        bgVideoPath = PathUtils.getAssetFileNameForSdcard("zishuo", ".mp4");
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            @Override
            public void onBackground() {
                AssetManager assetManager = getAssets();
                if (!com.rd.veuisdk.utils.FileUtils.isExist(bgVideoPath)) {
                    CoreUtils.assetRes2File(assetManager, "quik/zishuo.mp4", bgVideoPath);
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                build(false);
            }
        });

        TTFData.getInstance().initilize(this);//字体初始化数据库
        initView();
        mTvTitle.setText(R.string.zishuo);
        mCurProportion = QuikHandler.ASP_916;
        mPreviewFrame.setAspectRatio(mCurProportion);
        player.setPreviewAspectRatio(mCurProportion);
        player.setAutoRepeat(true);
        mVirtualVideo = new VirtualVideo();
        mVirtualVideo.setIsZishuo(true);

        player.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isPlayingORecording) {
                    pause();
                } else {
                    start();
                }
            }
        });
        initPlayerListener(player);
    }


    private VirtualVideo mSnapshotEditor;


    public void start() {
        isPlayingORecording = true;
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(ZishuoDrawActivity.this, mIvVideoPlayState);
    }

    /**
     * 暂停播放
     */
    public void pause() {
        player.pause();
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        isPlayingORecording = false;
    }

    public void seekTo(int msec) {
        player.seekTo(Utils.ms2s(msec));
        onSeekTo(msec);
    }

    public void stop() {
        player.stop();
        onSeekTo(0);
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getDuration() {
        return Utils.s2ms(player.getDuration());
    }

    public int getCurrentPosition() {
        return Utils.s2ms(player.getCurrentPosition());
    }


    private void initView() {
        mRdSeekBar = findViewById(R.id.sbEditor);
        currentTv = findViewById(R.id.tvCurTime);
        totalTv = findViewById(R.id.tvTotalTime);
        mPreviewFrame = findViewById(R.id.previewFrame);
        mBtnNext = findViewById(R.id.btnRight);
        mBtnLeft = findViewById(R.id.btnLeft);
        mTvTitle = findViewById(R.id.tvTitle);
        player = findViewById(R.id.player);
        mIvVideoPlayState = findViewById(R.id.ivPlayerState);
        mBtnNext.setVisibility(View.VISIBLE);
        mBtnNext.setTextColor(getResources().getColor(R.color.main_orange));
        mBtnNext.setText(R.string.export);

        findViewById(R.id.btnDraft).setVisibility(View.GONE);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightButtonClick();
            }
        });

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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


    /**
     * 准备重新加载
     *
     * @param needDialog
     */
    private void build(boolean needDialog) {
        if (needDialog) {
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        }
        if (player.isPlaying()) {
            pause();
        }
        player.reset();
        player.setPreviewAspectRatio(mCurProportion);
        mVirtualVideo.reset();
        //重新加载
        reload(mVirtualVideo);
        try {
            mVirtualVideo.build(player);
            onSeekTo(0);
            start();
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }


    }


    private boolean isPlayingORecording = false;


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
                SysAlertDialog.cancelLoadingDialog();
                int ms = Utils.s2ms(player.getDuration());
                mRdSeekBar.setMax(ms);
                totalTv.setText(getFormatTime(ms));
                onSeekTo(0);
                player.setBackgroundColor(Color.BLACK);

            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "onPlayerError: " + what + "..." + extra);
                if (extra == -14) {
                    build(false);
                }
                return false;
            }

            @Override
            public void onPlayerCompletion() {
            }

            @Override
            public void onGetCurrentPosition(float position) {
                onSeekTo(Utils.s2ms(position));
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

    /**
     * @param progress
     */
    private void onSeekTo(int progress) {
        currentTv.setText(getFormatTime(progress));
        mRdSeekBar.setProgress(progress);
    }


    @Override
    protected void onDestroy() {
        SysAlertDialog.cancelLoadingDialog();
        TTFUtils.recycle();
        TTFData.getInstance().close();
        if (null != player) {
            player.stop();
            player.cleanUp();
            player = null;
        }
        if (mSnapshotEditor != null) {
            mSnapshotEditor.release();
            mSnapshotEditor = null;
        }
        //清理
        TempVideoParams.getInstance().recycle();
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
        if (lastProgress != -1) {
            player.seekTo(lastProgress);
            onSeekTo(Utils.s2ms(lastProgress));
            start();
            lastProgress = -1f;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        lastProgress = -1;
        if (null != player) {
            if (player.isPlaying()) {
                //暂停
                player.pause();
            }
            //记录播放器位置
            lastProgress = player.getCurrentPosition();
        }
    }

    @Override
    public void onBackPressed() {
        String strMessage = getString(R.string.quit_edit);
        SysAlertDialog.showAlertDialog(this, "", strMessage,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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


    /**
     * 加载视频资源
     *
     * @param virtualVideo
     */
    private boolean reload(VirtualVideo virtualVideo) {
        try {
            MediaObject mediaObject = new MediaObject(bgVideoPath);
            Scene scene = VirtualVideo.createScene();
            scene.addMedia(mediaObject);
            final float finalMaxTrim = mediaObject.getDuration();
            final CustomDrawTextHandler customDrawTextHandler = new CustomDrawTextHandler();
            virtualVideo.addCustomDraw(new CustomDrawObject(finalMaxTrim) {
                @Override
                public CustomDrawObject clone() {
                    return null;
                }

                @Override
                public void draw(CanvasObject canvas, float progress) {
                    float ps = progress * finalMaxTrim;
                    if (null != customDrawTextHandler) {
                        customDrawTextHandler.drawText(canvas, ps);
                    }
                }
            });
            virtualVideo.addScene(scene);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 响应确定与导出
     */
    private void onRightButtonClick() {
        pause();
        ExportHandler mExportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
            }
        });
        mExportHandler.onExport(mCurProportion, false);
    }


}
