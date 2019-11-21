package com.rd.veuisdk.demo;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.InputUtls;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.SdkService;
import com.rd.veuisdk.callback.ICompressVideoCallback;
import com.rd.veuisdk.manager.CompressConfiguration;
import com.rd.veuisdk.manager.VideoMetadataRetriever;
import com.rd.veuisdk.ui.ExtSeekBar;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.Date;

import static com.rd.veuisdk.SdkEntry.getSdkService;

public class VideoCompressActivity extends BaseActivity implements View.OnClickListener {

    //播放器
    private PreviewFrameLayout mPreviewPlayer;
    private VirtualVideoView mMediaPlayer;
    private VirtualVideo mVirtualVideo;
    //菜单、自定义尺寸、播放、
    private LinearLayout mMenuLayout, mSizeLayout, mThelocationLayout;
    private RelativeLayout mPlayLayout;
    //原始视频信息
    private TextView mSrcSize, mSrcFPS, mSrcBit, mOldSize;
    //自定义设置 码率、分辨率
    private SeekBar mSbNewBit;
    private TextView mTvNewBit;
    private RadioGroup mRgSize;
    private RadioButton mRbCustomize;
    private TextView mNewSize;
    //自定义尺寸
    private EditText mEtSubtitle;
    private TextView mTvVideoW, mTvVideoH;
    private ExtSeekBar mSbVideoW, mSbVideoH;
    private CheckBox mCbConstrain;
    private int mStateSize = 0;
    //场景
    private Scene mScene = null;
    private MediaObject mMediaObject = null;
    private float mAsp = 0;
    //播放 全屏 撤销
    private ImageView mBtnPlay, mBtnFullScreen;
    private TextView mTvTitle;
    /**
     * 是否全屏
     */
    private View mContent;
    private boolean isFullScreen = false;
    /**
     * 导出的设置信息
     */
    private CheckBox mCbHardware;
    private int mNewWidth = 480;
    private int mNewHeight = 640;
    private int mNewFps = 0;
    private double mNewBit = 0.85;
    /**
     * 状态 0默认 1自定义尺寸
     */
    private int mStateUI = 0;
    //视频最大宽高
    private final int MIN_VIDEO = 240;
    private final int MAX_VIDEO = 1920;
    private int maxVideoW = MAX_VIDEO - MIN_VIDEO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_compress);
        initView();
        init();
    }

    private void initView() {
        mBtnPlay = findViewById(R.id.btnPlay);
        mBtnFullScreen = findViewById(R.id.btnFullScreen);
        mTvTitle = findViewById(R.id.tvBottomTitle);
        mMenuLayout = findViewById(R.id.menu_layout);
        mSizeLayout = findViewById(R.id.size_layout);
        mPlayLayout = findViewById(R.id.rlPlayLayout);
        mSrcSize = findViewById(R.id.srcSize);
        mSrcFPS = findViewById(R.id.srcFrame);
        mSrcBit = findViewById(R.id.srcBitRate);
        mOldSize = findViewById(R.id.tv_compress_old_size);
        mSbNewBit = findViewById(R.id.sb_compress_bit);
        mTvNewBit = findViewById(R.id.tv_compress_bit);
        mRgSize = findViewById(R.id.rg_compress_size);
        mNewSize = findViewById(R.id.tv_compress_new_size);
        mCbHardware = findViewById(R.id.cb_hardware);
        //自定义尺寸
        mRbCustomize = ((RadioButton) findViewById(R.id.rb_size_customize));
        mTvVideoW = findViewById(R.id.et_video_w);
        mTvVideoH = findViewById(R.id.et_video_h);
        mEtSubtitle = findViewById(R.id.et_subtitle);
        mSbVideoW = findViewById(R.id.sb_video_w);
        mSbVideoH = findViewById(R.id.sb_video_h);
        mCbConstrain = findViewById(R.id.cb_size_constrain);
        mThelocationLayout = findViewById(R.id.thelocation);
        //播放器的框
        mPreviewPlayer = (PreviewFrameLayout) findViewById(R.id.rlPreview_playerHori);
        mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreviewHori);

        mBtnPlay.setOnClickListener(this);
        mBtnFullScreen.setOnClickListener(this);
        findViewById(R.id.ivCancel).setOnClickListener(this);
        findViewById(R.id.ivSure).setOnClickListener(this);
        mRbCustomize.setOnClickListener(this);

        mRgSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_size_original) {
                    //原始尺寸
                    mNewWidth = mMediaObject.getWidth();
                    mNewHeight = mMediaObject.getHeight();
                } else if (checkedId == R.id.rb_size_480) {
                    //480P
                    mNewWidth = 480;
                    mNewHeight = 640;
                } else if (checkedId == R.id.rb_size_720) {
                    //720P
                    mNewWidth = 720;
                    mNewHeight = 1280;
                } else if (checkedId == R.id.rb_size_1080) {
                    //1080P
                    mNewWidth = 1080;
                    mNewHeight = 1920;
                } else if (checkedId == R.id.rb_size_customize) {
                    //自定义

                }
            }
        });

        //码率
        mSbNewBit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mNewBit = Double.parseDouble(String.format("%.2f", (progress + 0.0f) / 100));
                    mTvNewBit.setText(mNewBit + "M");
                    setNewSize();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //播放器
        mMediaPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {
                SysAlertDialog.cancelLoadingDialog();
            }

            @Override
            public void onPlayerCompletion() {

            }

            @Override
            public void onGetCurrentPosition(float position) {

            }
        });
        mPreviewPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen) {
                    if (mPlayLayout.getVisibility() == View.VISIBLE) {
                        mPlayLayout.setVisibility(View.GONE);
                        mHandler.removeMessages(FULL_SCREEN);
                    } else {
                        mPlayLayout.setVisibility(View.VISIBLE);
                        mHandler.sendEmptyMessageDelayed(FULL_SCREEN, 3 * 1000);
                    }
                }
            }
        });

        //自定义尺寸
        mSbVideoW.setMinValue(MIN_VIDEO);
        mSbVideoW.setMax(maxVideoW);
        mSbVideoH.setMinValue(MIN_VIDEO);
        mSbVideoH.setMax(maxVideoW);
        mSbVideoW.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setVideoW(progress, 1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbVideoH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setVideoH(progress, 1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTvVideoW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateSize = 1;
                mEtSubtitle.setText("");
                mEtSubtitle.setHint(getString(R.string.compress_input_width) + mTvVideoW.getText().toString());
                controlKeyboardLayout();
                InputUtls.showInput(mEtSubtitle);
            }
        });
        mTvVideoH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateSize = 2;
                mEtSubtitle.setText("");
                mEtSubtitle.setHint(getString(R.string.compress_input_height) + mTvVideoH.getText().toString());
                controlKeyboardLayout();
                InputUtls.showInput(mEtSubtitle);
            }
        });
        mEtSubtitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mInput && mStateSize != 0) {
                    int temp = 0;
                    if (TextUtils.isEmpty(s)) {
                        return;
                    } else {
                        temp = Integer.parseInt(s.toString());
                    }
                    if (temp < MIN_VIDEO) {
                        return;
                    }
                    if (temp > MAX_VIDEO) {
                        mEtSubtitle.setText(String.valueOf(MAX_VIDEO));
                        return;
                    }
                    if (mStateSize == 1) {
                        mTvVideoW.setText(String.valueOf(temp));
                        setVideoW(temp - MIN_VIDEO, 2);
                    } else if (mStateSize == 2) {
                        mTvVideoH.setText(String.valueOf(temp));
                        setVideoH(temp - MIN_VIDEO, 2);
                    }
                }
                mStateSize = 0;
                removeInputListener();
                InputUtls.hideKeyboard(mEtSubtitle);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void init() {
        SysAlertDialog.showLoadingDialog(VideoCompressActivity.this, R.string.isloading);
        mTvTitle.setText(getString(R.string.compress));
        //修正播放器容器显示比例
        AppConfiguration.fixAspectRatio(this);
        //获取场景
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (mScene == null) {
            //没有媒体
            onToast(R.string.no_media);
            finish();
            return;
        }
        mMediaObject = mScene.getAllMedia().get(0);
        mAsp = mMediaObject.getWidth() / mMediaObject.getHeight();
        mPreviewPlayer.setAspectRatio(mAsp);
        mContent = findViewById(android.R.id.content);
        //加载场景
        mVirtualVideo = new VirtualVideo();
        mVirtualVideo.addScene(mScene);
        try {
            mVirtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        initHandler();
        //显示原始信息
        setMediaInfo();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnPlay) {
            //播放
            playOrPause();
        } else if (i == R.id.btnFullScreen) {
            setFullScreen(!isFullScreen);
        } else if (i == R.id.ivSure) {
            if (mStateUI == 0) {
                stopVideo();
                export();
            } else if (mStateUI == 1) {
                //自定义分辨率界面
                mNewWidth = Math.min(MAX_VIDEO, MIN_VIDEO + mSbVideoW.getProgress());
                mNewHeight = Math.min(MAX_VIDEO, MIN_VIDEO + mSbVideoH.getProgress());
                mRbCustomize.setText(mNewWidth + "*" + mNewHeight);
                setUI(0);
            }
        } else if (i == R.id.ivCancel) {
            onBackPressed();
        } else if (i == R.id.rb_size_customize) {
            //自定义尺寸界面
            setUI(1);
        }
    }

    /**
     * 设置全屏
     */
    private void setFullScreen(boolean isFull) {
        SysAlertDialog.showLoadingDialog(this, R.string.loading);
        pauseVideo();
        if (isFull) {
            if (CoreUtils.hasIceCreamSandwich()) {
                // 全屏时，隐藏虚拟键区
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            mMenuLayout.setVisibility(View.GONE);
            mBtnFullScreen.setImageResource(R.drawable.edit_intercept_revert);
            isFullScreen = true;
            mPreviewPlayer.setAspectRatio(mAsp);
            mHandler.sendEmptyMessageAtTime(FULL_SCREEN, 3 * 1000);
            if (mMediaPlayer.getVideoWidth() > mMediaPlayer.getVideoHeight()) {
                if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (CoreUtils.hasIceCreamSandwich()) {
                mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            mMenuLayout.setVisibility(View.VISIBLE);
            mPlayLayout.setVisibility(View.VISIBLE);
            mBtnFullScreen.setImageResource(R.drawable.edit_intercept_fullscreen);
            isFullScreen = false;
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        SysAlertDialog.cancelLoadingDialog();
        playVideo();
    }

    /**
     * 设置UI
     */
    public void setUI(int state) {
        mStateUI = state;
        if (mStateUI == 0) {
            mSizeLayout.setVisibility(View.GONE);
        } else if (mStateUI == 1) {
            mSizeLayout.setVisibility(View.VISIBLE);
            mTvVideoW.setText(String.valueOf(mNewWidth));
            mTvVideoH.setText(String.valueOf(mNewHeight));
            mSbVideoW.setProgress(mNewWidth - MIN_VIDEO);
            mSbVideoH.setProgress(mNewHeight - MIN_VIDEO);
        }
    }

    //取消全屏
    private final int FULL_SCREEN = 100;
    private Handler mHandler;

    private void initHandler() {
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {

                switch (msg.what) {
                    case FULL_SCREEN:
                        if (isFullScreen) {
                            mPlayLayout.setVisibility(View.GONE);
                        } else {
                            mPlayLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }

            }

            ;
        };
    }

    /**
     * 导出
     */
    private void export() {
        //保存压缩参数
        initCompressConfig();
        //执行压缩
        SdkEntry.onCompressVideo(this, mMediaObject.getMediaPath(), new ICompressVideoCallback() {

            private HorizontalProgressDialog epdExport = null;
            private Dialog dialog = null;
            private boolean cancelExport = false;
            private Date startDate;

            @Override
            public void onCompressStart() {
                startDate = new Date(System.currentTimeMillis());
                cancelExport = false;
                if (epdExport == null) {
                    epdExport = SysAlertDialog.showHoriProgressDialog(
                            VideoCompressActivity.this, getString(R.string.exporting),
                            false, true, new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    cancelExport = true;
                                }
                            });
                    epdExport.setCanceledOnTouchOutside(false);
                    epdExport.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                        @Override
                        public void onCancel() {
                            dialog = SysAlertDialog.showAlertDialog(
                                    VideoCompressActivity.this, "",
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
                                                SdkEntry.cancelCompressVideo();
                                            }
                                        }
                                    });
                        }
                    });
                }
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            @Override
            public void onProgress(int progress, int max) {
                if (null != epdExport) {
                    epdExport.setProgress(progress);
                    epdExport.setMax(max);
                }
            }

            @Override
            public void onCompressError(String errorLog) {
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

            @Override
            public void onCompressComplete(String path) {
                Date endDate = new Date(System.currentTimeMillis());
                long diff = endDate.getTime() - startDate.getTime();
                VideoMetadataRetriever vmr = new VideoMetadataRetriever();
                vmr.setDataSource(path);
                float duration = Float.valueOf(vmr.extractMetadata(VideoMetadataRetriever.METADATA_KEY_VIDEO_DURATION));
                Toast.makeText(VideoCompressActivity.this, "压缩倍速: " + String.format("%.2f", (duration * 1000) / diff) + "x", Toast.LENGTH_LONG).show();
                if (epdExport != null) {
                    epdExport.dismiss();
                    epdExport = null;
                }
                if (dialog != null) {
                    dialog.dismiss();
                    dialog.cancel();
                    dialog = null;
                }
                gotoNext(path);
            }
        });
    }

    /**
     * 返回数据
     */
    private void gotoNext(String outpath) {
        onVideoExport(this, outpath);
        Intent intent = new Intent();
        intent.putExtra(SdkEntry.EDIT_RESULT, outpath);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void onVideoExport(Context context, String videoPath) {
        if (!TextUtils.isEmpty(videoPath)) {
            // 读取导出视频的媒体信息，如宽度，持续时间等
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(videoPath);
                int nVideoWidth = Integer
                        .parseInt(retriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int nVideoHeight = Integer
                        .parseInt(retriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                int duration = Integer
                        .parseInt(retriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                // 写入系统相册
                insertToGalleryr(context, videoPath, duration, nVideoWidth,
                        nVideoHeight);
            } catch (Exception ex) {
            } finally {
                retriever.release();
            }
        } else {
            Log.d(TAG, "获取视频地址失败");
        }
    }

    private void insertToGalleryr(Context context, String path, int duration,
                                  int width, int height) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(MediaStore.Video.Media.TITLE, "未定义");
        videoValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        videoValues.put(MediaStore.Video.Media.DATA, path);
        videoValues.put(MediaStore.Video.Media.ARTIST,
                context.getString(R.string.app_name));
        videoValues.put(MediaStore.Video.Media.DATE_TAKEN,
                String.valueOf(System.currentTimeMillis()));
        videoValues.put(MediaStore.Video.Media.DESCRIPTION,
                context.getString(R.string.app_name));
        videoValues.put(MediaStore.Video.Media.DURATION, duration);
        videoValues.put(MediaStore.Video.Media.WIDTH, width);
        videoValues.put(MediaStore.Video.Media.HEIGHT, height);
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoValues);
    }

    /**
     * 初始压缩配置
     */
    private void initCompressConfig() {
        SdkService sdkService = getSdkService();
        if (null != sdkService) {
            sdkService
                    .initCompressConfiguration(new CompressConfiguration.Builder()
                            // 设置是否使用硬件加速
                            .enableHWCode(mCbHardware.isChecked())
                            // 设置压缩视频码率
                            .setBitRate(mNewBit)
                            // 设置视频分辨率
                            .setVideoSize(mNewWidth,
                                    mNewHeight)
                            // 设置保存路径
                            .setSavePath(
                                    Environment.getExternalStorageDirectory()
                                            + "/DCIM/").get());
        }
    }

    /**
     * 设置初始化值
     */
    private void setMediaInfo() {
        VideoMetadataRetriever metadataRetriever = new VideoMetadataRetriever();
        metadataRetriever.setDataSource(mMediaObject.getMediaPath());
        String frame = metadataRetriever.extractMetadata(VideoMetadataRetriever.METADATA_KEY_VIDEO_FRAME_RATE);
        String audioBit = metadataRetriever.extractMetadata(VideoMetadataRetriever.METADATA_KEY_AUDIO_BIT_RATE);
        String bit = metadataRetriever.extractMetadata(VideoMetadataRetriever.METADATA_KEY_VIDEO_BIT_RATE);
        float bitRate = (float) (Integer.parseInt(bit) / 1000.0 / 1000);
        mAudio = Integer.parseInt(audioBit) / 1000.0 / 1000;

        mNewWidth = mMediaObject.getWidth();
        mNewHeight = mMediaObject.getHeight();
        mNewFps = Integer.parseInt(frame);
        mNewBit = bitRate;

        //原视频信息
        mSrcBit.setText(String.format("%.2f", bitRate) + "Mbps");
        mSrcFPS.setText(frame);
        mSrcSize.setText(mNewWidth + "*" + mNewHeight);

        //设置最大码率
        mSbNewBit.setMax((int) (mNewBit * 100));
        mSbNewBit.setProgress((int) (mNewBit * 100));
        mTvNewBit.setText(String.format("%.2f", mNewBit) + "M");

        //设置视频大小显示
        mOldSize.setText(String.format("%.2f", (mAudio + mNewBit) * mMediaObject.getDuration() / 8) + "MB");
        setNewSize();
    }

    /**
     * 音频比特率
     */
    private double mAudio = 0;

    /**
     * 设置压缩后文件大小
     */
    private void setNewSize() {
        mNewSize.setText(String.format("%.2f", (mAudio + mNewBit) * mMediaObject.getDuration() / 8) + "MB");
    }

    /**
     * 播放暂停
     */
    private void playOrPause() {
        if (mMediaPlayer.isPlaying()) {
            pauseVideo();
        } else {
            playVideo();
        }
    }

    /**
     * 播放
     */
    private void playVideo() {
        //如果是剪除 时间太短 画面会跳
        mMediaPlayer.start();
        mBtnPlay.setImageResource(R.drawable.btn_edit_pause);
    }

    /**
     * 暂停
     */
    private void pauseVideo() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mBtnPlay.setImageResource(R.drawable.btn_edit_play);
    }

    /**
     * 停止
     */
    private void stopVideo() {
        mMediaPlayer.stop();
        mBtnPlay.setImageResource(R.drawable.btn_edit_play);
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            setFullScreen(false);
        } else if (mStateUI == 0) {
            onShowAlert();
        } else if (mStateUI == 1) {
            ((RadioButton) findViewById(R.id.rb_size_original)).setChecked(true);
            mRbCustomize.setText(getString(R.string.compress_size_customize));
            setUI(0);
        }
    }

    /**
     * 提示是否放弃保存
     */
    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(this,
                getString(R.string.dialog_tips),
                getString(R.string.cancel_all_changed),
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }, false, null).show();
    }

    @Override
    protected void onDestroy() {
        if (null != mMediaPlayer) {
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }
        mScene = null;
        mMediaObject = null;
        mHandler.removeCallbacksAndMessages(null);
        removeInputListener();
        InputUtls.hideKeyboard(mEtSubtitle);
        super.onDestroy();
    }

    private boolean mInput = false;

    //设置高度
    private void setVideoH(int height, int type) {
        mInput = true;
        if (mCbConstrain.isChecked()) {
            int w = (int) ((height + MIN_VIDEO + 0.0f) / mMediaObject.getHeight() * mMediaObject.getWidth());
            if (w > MAX_VIDEO) {
                mSbVideoW.setProgress(maxVideoW);
                int h = (int) ((MAX_VIDEO + 0.0f) / mMediaObject.getWidth() * mMediaObject.getHeight());
                mSbVideoH.setProgress(h - MIN_VIDEO);

                mTvVideoW.setText(String.valueOf(MAX_VIDEO));
                mTvVideoH.setText(String.valueOf(h));
            } else if (w < MIN_VIDEO) {
                mSbVideoW.setProgress(0);
                int h = (int) ((MIN_VIDEO + 0.0f) / mMediaObject.getWidth() * mMediaObject.getHeight());
                mSbVideoH.setProgress(h - MIN_VIDEO);

                mTvVideoW.setText(String.valueOf(MIN_VIDEO));
                mTvVideoH.setText(String.valueOf(h));
            } else {
                mSbVideoW.setProgress(w - MIN_VIDEO);
                mSbVideoH.setProgress(height);
                mTvVideoW.setText(String.valueOf(w));
                mTvVideoH.setText(String.valueOf(height + MIN_VIDEO));
            }
        } else if (type == 1) {
            mTvVideoH.setText(String.valueOf(height + MIN_VIDEO));
        } else if (type == 2) {
            mSbVideoH.setProgress(height);
        }
        mInput = false;
    }

    //设置宽度
    private void setVideoW(int width, int type) {
        mInput = true;
        if (mCbConstrain.isChecked()) {
            int h = (int) ((width + MIN_VIDEO + 0.0f) / mMediaObject.getWidth() * mMediaObject.getHeight());
            if (h > MAX_VIDEO) {
                mSbVideoH.setProgress(maxVideoW);
                int w = (int) ((MAX_VIDEO + 0.0f) / mMediaObject.getHeight() * mMediaObject.getWidth());
                mSbVideoW.setProgress(w - MIN_VIDEO);

                mTvVideoH.setText(String.valueOf(MAX_VIDEO));
                mTvVideoW.setText(String.valueOf(w));
            } else if (h < MIN_VIDEO) {
                mSbVideoH.setProgress(0);
                int w = (int) ((MIN_VIDEO + 0.0f) / mMediaObject.getHeight() * mMediaObject.getWidth());
                mSbVideoW.setProgress(w - MIN_VIDEO);

                mTvVideoH.setText(String.valueOf(MIN_VIDEO));
                mTvVideoW.setText(String.valueOf(w));
            } else {
                mSbVideoH.setProgress(h - MIN_VIDEO);
                mSbVideoW.setProgress(width);
                mTvVideoH.setText(String.valueOf(h));
                mTvVideoW.setText(String.valueOf(width + MIN_VIDEO));
            }
        } else if (type == 1) {
            mTvVideoW.setText(String.valueOf(width + MIN_VIDEO));
        } else if (type == 2) {
            mSbVideoW.setProgress(width);
        }
        mInput = false;
    }

    //注册输入法监听，动态调整bu布局setY
    private com.rd.veuisdk.listener.OnGlobalLayoutListener mGlobalLayoutListener;

    private void controlKeyboardLayout() {
        removeInputListener();
        if (null != mContent) {
            mGlobalLayoutListener = new com.rd.veuisdk.listener.OnGlobalLayoutListener(mContent, mMenuLayout, mThelocationLayout);
            mContent.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    private void removeInputListener() {
        if (null != mContent) {
            if (null != mGlobalLayoutListener) {
                //先移除监听再隐藏输入法(移除的时候调用了强制恢复布局)
                mContent.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                mGlobalLayoutListener.resetUI();
                mGlobalLayoutListener = null;
            }
        }
    }

}
