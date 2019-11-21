package com.rd.veuisdk.demo;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.ExportHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.VideoThumbNailAlterView;
import com.rd.veuisdk.ui.extrangseekbar.RangSeekBarBase;
import com.rd.veuisdk.ui.extrangseekbar.TrimRangeSeekbarPlus;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;

public class InterceptActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "InterceptActivity";
    //播放 全屏 撤销
    private ImageView mBtnPlay, mBtnFullScreen, mBtnCancel;
    //标题 总的时间 截取时间
    private TextView mTvTitle, mTvDuration, mTvRemainDuration;
    //修剪 剪除 放大选择区域
    private Button mBtnConstruct, mBtnCutoff;
    //底部 功能区
    private LinearLayout mFeaturesLayout;
    private FrameLayout mMenuLayout;
    //截取视频
    private VideoThumbNailAlterView mThumbNailView;
    //有把手进度条
    private TrimRangeSeekbarPlus mRangeSeekBar;
    //播放器
    private PreviewFrameLayout mPreviewPlayer;
    private VirtualVideoView mMediaPlayer;
    private VirtualVideo mVirtualVideo;
    private VideoOb mOb;
    private MediaObject mMediaObject;
    private RelativeLayout mPlayLayout;
    //截取的时间起点
    private float mStart = 0;
    //场景
    private Scene mScene = null;
    /**
     * 修剪还是剪除(1 修剪 2剪除)
     */
    private int mConstructAndCutoff = 1;
    /**
     * 是否全屏
     */
    private View mContent;
    private boolean isFullScreen = false;
    private float aspClip = 1;
    //截取后的总时间 秒
    private float srcVideoDuration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intercept);
        initView();
        init();
    }

    private void init() {
        SysAlertDialog.showLoadingDialog(InterceptActivity.this, R.string.isloading);
        mTvTitle.setText(getString(R.string.preview_trim));
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
        mContent = findViewById(android.R.id.content);
        mMediaObject = mScene.getAllMedia().get(0);
        float f = mMediaObject.getWidth() / mMediaObject.getHeight();
        mPreviewPlayer.setAspectRatio(f);
        mOb = (VideoOb) mMediaObject.getTag();
        if (mOb == null) {
            mOb = new VideoOb(mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), 0, null, VideoOb.DEFAULT_CROP);
        }
        //加载场景
        mVirtualVideo = new VirtualVideo();
        if(intercept()) {
           build();
        } else {
            SysAlertDialog.cancelLoadingDialog();
            finish();
        }
    }

    private void initView() {
        mBtnPlay = findViewById(R.id.btnPlay);
        mBtnFullScreen = findViewById(R.id.btnFullScreen);
        mBtnCancel = findViewById(R.id.btnCancel);
        mTvTitle = findViewById(R.id.tvBottomTitle);
        mTvDuration = findViewById(R.id.tvInterceptBehindTime);
        mTvRemainDuration = findViewById(R.id.tvRemainDuration);
        mBtnConstruct = findViewById(R.id.btnConstruct);
        mBtnCutoff = findViewById(R.id.btnCutoff);
        mThumbNailView = findViewById(R.id.split_videoview);
        mRangeSeekBar = findViewById(R.id.m_extRangeSeekBar);
        //播放器的框
        mPreviewPlayer = (PreviewFrameLayout) findViewById(R.id.rlPreview_playerHori);
        mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreviewHori);
        //功能区 底部 播放全屏按钮
        mFeaturesLayout = findViewById(R.id.llFeatures);
        mMenuLayout = findViewById(R.id.llmenu);
        mPlayLayout = findViewById(R.id.rlPlayLayout);

        mBtnPlay.setOnClickListener(this);
        mBtnFullScreen.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        findViewById(R.id.ivCancel).setOnClickListener(this);
        findViewById(R.id.ivSure).setOnClickListener(this);
        mBtnConstruct.setOnClickListener(this);
        mBtnCutoff.setOnClickListener(this);
        findViewById(R.id.btnAmplification).setOnClickListener(this);

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
        //设置播放器监听
        mMediaPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
            @Override
            public void onPlayerPrepared() {
                SysAlertDialog.cancelLoadingDialog();
                if (mOb != null) {
                    //原始视频的duration，不受trim 和speed的影响
                    srcVideoDuration = 0;
                    ArrayList<Float> t = mBreakpoint.get(mBreakpoint.size() - 1);
                    for (int i = t.size() - 1; i >= 0; i--) {
                        srcVideoDuration += (t.get(i) - t.get(--i));
                    }
                    mTvDuration.setText(getTime( Utils.s2ms(srcVideoDuration)));
                    mTvRemainDuration.setText(getTime( Utils.s2ms(srcVideoDuration)));
                    int viewDuration = (int) (Utils.s2ms(srcVideoDuration / mMediaObject.getSpeed()));
                    mRangeSeekBar.setDuration(viewDuration);
                    if (mConstructAndCutoff == 2) {
                        mRangeSeekBar.setSeekBarRangeValues(viewDuration / 2, viewDuration / 2 + 1000);
                    } else {
                        mRangeSeekBar.setSeekBarRangeValues(0, viewDuration);
                    }
                    mRangeSeekBar.setProgress(0);
                }
                //设置缩略图
                Scene scene;
                MediaObject object;
                VirtualVideo vitualVideo = new VirtualVideo();
                ArrayList<Float> t = mBreakpoint.get(mBreakpoint.size() - 1);
                for (int i = 0; i < t.size(); i++) {
                    object = new MediaObject(mMediaObject);
                    object.setTimeRange(t.get(i), t.get(++i));
                    scene = VirtualVideo.createScene();
                    scene.addMedia(object);
                    vitualVideo.addScene(scene);
                }
                float asp = (mMediaPlayer.getVideoWidth() + 0.0f) / mMediaPlayer.getVideoHeight();
                mThumbNailView.recycle();
                mThumbNailView.setVirtualVideo(asp, vitualVideo);
                mThumbNailView.setStartThumb();

                mOb.nEnd = Utils.ms2s(mRangeSeekBar.getSelectedMaxValue());
                mOb.nStart = Utils.ms2s(mRangeSeekBar.getSelectedMinValue());
                mOb.TEnd = Utils.ms2s(mRangeSeekBar.getSelectedMaxValue()) + mStart;
                mOb.TStart = Utils.ms2s(mRangeSeekBar.getSelectedMinValue()) + mStart;
                mMediaPlayer.seekTo(0 + 0.25f);

            }

            @Override
            public void onPlayerCompletion() {
                mMediaPlayer.seekTo(0);
                mBtnPlay.setImageResource(R.drawable.btn_edit_play);
                mRangeSeekBar.setProgress(mRangeSeekBar.getSelectedMinValue());
            }

            @Override
            public void onGetCurrentPosition(float position) {
                int nPosition = Utils.s2ms(position);
                mRangeSeekBar.setProgress(nPosition);
                if (mConstructAndCutoff == 1) {
                    if (nPosition < Utils.s2ms(mOb.nStart) - 50) {
                        mMediaPlayer.seekTo(mOb.nStart);
                    }
                    if (nPosition > Utils.s2ms(mOb.nEnd)) {
                        mMediaPlayer.seekTo(mOb.nStart);
                        mRangeSeekBar.setProgress(Utils.s2ms(mOb.nStart));
                    }
                } else if (mConstructAndCutoff == 2) {
                    if (nPosition > Utils.s2ms(mOb.nStart) && nPosition < Utils.s2ms(mOb.nEnd)) {
                        pauseVideo();
                        mMediaPlayer.seekTo(mOb.nEnd);
                        mRangeSeekBar.setProgress(Utils.s2ms(mOb.nEnd));
                        playVideo();
                    }
                }

            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "Player error:" + what + "," + extra);
                SysAlertDialog.cancelLoadingDialog();
                SysAlertDialog.showAlertDialog(InterceptActivity.this,
                        "",
                        getString(R.string.preview_error),
                        getString(R.string.sure), null, null, null);
                return false;
            }
        });
        // 不将边缘变成灰色
        mRangeSeekBar.setHorizontalFadingEdgeEnabled(false);
        mRangeSeekBar.setMoveMode(true);  //自由截取把手
        mRangeSeekBar.setOnRangSeekBarChangeListener(new RangSeekBarBase.OnRangeSeekBarChangeListener() {
            private int m_nCurrentThumbPressed;

            @Override
            public boolean beginTouch(int thumbPressed) {
                m_nCurrentThumbPressed = thumbPressed;
                if (m_nCurrentThumbPressed != RangSeekBarBase.NONE_THUMB_PRESSED) {
                    if (mMediaPlayer.isPlaying()) {
                        pauseVideo();
                        if (m_nCurrentThumbPressed == RangSeekBarBase.CURRENT_THUMB_PRESSED) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void rangeSeekBarValuesChanged(long minValue, long maxValue, long currentValue) {
                switch (m_nCurrentThumbPressed) {
                    case RangSeekBarBase.CURRENT_THUMB_PRESSED: // 指定当前值
                        seekTo(currentValue);
                        break;
                    case RangSeekBarBase.MAX_THUMB_PRESSED: // 指定范围 最小值
                        seekTo(maxValue);
                        prepareMedia(mRangeSeekBar.getSelectedMinValue(), mRangeSeekBar.getSelectedMaxValue());
                        break;
                    case RangSeekBarBase.MIN_THUMB_PRESSED: // 指定范围 最大值
                        seekTo(minValue);
                        prepareMedia(mRangeSeekBar.getSelectedMinValue(), mRangeSeekBar.getSelectedMaxValue());
                        break;
                    default:
                        break;
                }
                m_nCurrentThumbPressed = RangSeekBarBase.NONE_THUMB_PRESSED;
            }

            @Override
            public void rangeSeekBarValuesChanging(long setValue) {
                switch (m_nCurrentThumbPressed) {
                    case RangSeekBarBase.CURRENT_THUMB_PRESSED: // 指定当前值
                        seekTo((int) setValue);
                        break;
                    case RangSeekBarBase.MIN_THUMB_PRESSED: // 指定范围 最小值
                    case RangSeekBarBase.MAX_THUMB_PRESSED: // 指定范围 最大值
                        seekTo((int) setValue);
                        long min = mRangeSeekBar.getSelectedMinValue();
                        long max = mRangeSeekBar.getSelectedMaxValue();
                        onTrimText(min, max);
                        break;
                    default:
                        break;

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnPlay) {
            //播放
            playOrPause();
        } else if (i == R.id.btnFullScreen) {
            setFullScreen(!isFullScreen);
        } else if (i == R.id.btnConstruct) {
            if (mConstructAndCutoff != 1) {
                mConstructAndCutoff = 1;
                constructAndCutoff();
            }
        } else if (i == R.id.btnCutoff) {
            if (mConstructAndCutoff != 2) {
                mConstructAndCutoff = 2;
                constructAndCutoff();
            }
        } else if (i == R.id.btnAmplification) {
            if (isAmplificationChange()) {
                onAmplificationAlert();
            } else {
                onToast(getString(R.string.trim_video_prompt));
            }
        } else if (i == R.id.btnCancel) {
            SysAlertDialog.showLoadingDialog(InterceptActivity.this, R.string.isloading);
            pauseVideo();
            cancel();
        } else if (i == R.id.ivSure) {
            aspClip = mMediaObject.getWidth() / (mMediaObject.getHeight() + 0.0f);
            stopVideo();
            if (isAmplificationChange()) {
                ExportHandler exportHandler = new ExportHandler(this, new com.rd.veuisdk.ExportHandler.ExportCallBack() {

                    @Override
                    public void onCancel() {
                        cancel();
                    }

                    @Override
                    public void addData(VirtualVideo virtualVideo) {
//                    if (isAmplificationChange()) {
                        //先裁剪
//                        if (intercept()) {
//                            export(virtualVideo);
//                        }
//                    } else {
//                        export(virtualVideo);
//                    }
                        if (intercept()) {
                            export(virtualVideo);
                        }
                    }
                });
                exportHandler.onExport(aspClip, true);
            } else {
                onToast(getString(R.string.trim_video_prompt));
            }
        } else if (i == R.id.ivCancel) {
            onBackPressed();
        }
    }

    /**
     * 导出
     * @param virtualVideo
     */
    private void export(VirtualVideo virtualVideo) {
        if (mBreakpoint.size() <= 0) {
            return;
        }
        Scene scene = VirtualVideo.createScene();
        MediaObject object;
        ArrayList<Float> t = mBreakpoint.get(mBreakpoint.size() - 1);
        for (int i = 0; i < t.size(); i++) {
            object = new MediaObject(mMediaObject);
            object.setTimeRange(t.get(i), t.get(++i));
            scene.addMedia(object);
        }
        virtualVideo.addScene(scene);
    }

    /**
     * 设置全屏
     */
    private void setFullScreen(boolean isFull){
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
            mFeaturesLayout.setVisibility(View.GONE);
            mMenuLayout.setVisibility(View.GONE);
            mBtnFullScreen.setImageResource(R.drawable.edit_intercept_revert);
            isFullScreen = true;
            float f = mMediaObject.getWidth() / mMediaObject.getHeight();
            mPreviewPlayer.setAspectRatio(f);
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
            mFeaturesLayout.setVisibility(View.VISIBLE);
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
     * 放大选择区域
     */
    private void onAmplification() {
        if (intercept()) {
            build();
        } else {
            //出现错误 撤销一步
            cancel();
        }
    }

    //截取点
    private ArrayList<ArrayList<Float>> mBreakpoint = new ArrayList<>();
    private ArrayList<Float> temp;

    /**
     * 截取
     * @return
     */
    private boolean intercept() {
        temp = new ArrayList<>();
        int length = mBreakpoint.size();
        if (mConstructAndCutoff == 1) {
            if (length > 0) {
                ArrayList<Float> lastBreak = mBreakpoint.get(length - 1);
                int i = 0;
                for (; i < lastBreak.size(); i++) {
                    if (i != 0 && i % 2 == 0) {
                        mOb.TStart += (lastBreak.get(i) - lastBreak.get(i - 1));
                        mOb.TEnd += (lastBreak.get(i) - lastBreak.get(i - 1));
                    }
                    if (lastBreak.get(i) > mOb.TStart) {
                        temp.add(mOb.TStart);
                        break;
                    }
                }
                for (; i < lastBreak.size(); i++) {
                    if (i != 0 && i % 2 == 0) {
                        mOb.TEnd += (lastBreak.get(i) - lastBreak.get(i - 1));
                    }
                    if (lastBreak.get(i) > mOb.TEnd) {
                        temp.add(mOb.TEnd);
                        break;
                    } else {
                        temp.add(lastBreak.get(i));
                    }
                }
            } else {
                temp.add(mOb.TStart);
                temp.add(mOb.TEnd);
            }
        } else if (mConstructAndCutoff == 2) {
            if (length > 0) {
                ArrayList<Float> lastBreak = mBreakpoint.get(length - 1);
                int i = 0;
                for (; i < lastBreak.size(); i++) {
                    if (i != 0 && i % 2 == 0) {
                        mOb.TStart += (lastBreak.get(i) - lastBreak.get(i - 1));
                        mOb.TEnd += (lastBreak.get(i) - lastBreak.get(i - 1));
                    }
                    if (lastBreak.get(i) > mOb.TStart) {
                        temp.add(mOb.TStart);
                        break;
                    } else {
                        temp.add(lastBreak.get(i));
                    }
                }
                for (; i < lastBreak.size(); i++) {
                    if (i != 0 && i % 2 == 0) {
                        mOb.TEnd += (lastBreak.get(i) - lastBreak.get(i - 1));
                    }
                    if (lastBreak.get(i) > mOb.TEnd) {
                        temp.add(mOb.TEnd);
                        break;
                    }
                }
                for (;i < lastBreak.size(); i++) {
                    temp.add(lastBreak.get(i));
                }
            } else {
                temp.add(0f);
                temp.add(mOb.TStart);
                temp.add(mOb.TEnd);
                temp.add(mMediaObject.getDuration());
            }
        }
        if (temp.size() % 2 == 0) {
            mBreakpoint.add(temp);
            return true;
        } else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 重新加载
     */
    private void build() {
        if (mBreakpoint.size() <= 0) {
            return;
        }
        mMediaPlayer.reset();
        mVirtualVideo.reset();
        Scene scene;
        MediaObject object;
        ArrayList<Float> t = mBreakpoint.get(mBreakpoint.size() - 1);
        mStart = t.get(0);
        for (int i = 0; i < t.size(); i++) {
            object = new MediaObject(mMediaObject);
            object.setTimeRange(t.get(i), t.get(++i));
            scene = VirtualVideo.createScene();
            scene.addMedia(object);
            mVirtualVideo.addScene(scene);
        }
        mMediaPlayer.setAutoRepeat(true);
        try {
            mVirtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
        setCancelHide();
    }

    /**
     * 撤销按钮 显示隐藏
     */
    private void setCancelHide() {
        if(mBreakpoint.size() > 1) {
            mBtnCancel.setVisibility(View.VISIBLE);
        } else {
            mBtnCancel.setVisibility(View.GONE);
        }
    }

    /**
     * 撤销
     */
    private void cancel() {
        if (mBreakpoint.size() > 1) {
            mBreakpoint.remove(mBreakpoint.size() - 1);
            build();
        } else {
            onToast("不能撤销！");
            SysAlertDialog.cancelLoadingDialog();
        }
    }

    /**
     * 单位毫秒
     *
     * @param minValue
     * @param maxValue
     */
    private void prepareMedia(long minValue, long maxValue) {
        if (null != mMediaObject) {
            if (null != mOb) {
                mOb.nStart = Utils.ms2s(minValue);
                mOb.nEnd = Utils.ms2s(maxValue);

                float speed = mMediaObject.getSpeed();

                mOb.rStart = Utils.ms2s((int) (minValue * speed));
                mOb.rEnd = Utils.ms2s((int) (maxValue * speed));

                mOb.TStart = Utils.ms2s((int) (minValue * speed)) + mStart;
                mOb.TEnd = Utils.ms2s((int) (maxValue * speed)) + mStart;

                onTrimText(minValue, maxValue);
            }
        }
    }

    /**
     * 滑动把手时需设置对应的时间文本
     *
     * @param min
     * @param max
     */
    private void onTrimText(long min, long max) {
        if (mConstructAndCutoff == 1) {
            mTvRemainDuration.setText(getTime((int) Math.max(0, max - min)));
        } else if (mConstructAndCutoff == 2){
            mTvRemainDuration.setText(getTime((int) Math.max(0, Utils.s2ms(srcVideoDuration) - (max - min))));
        }
    }

    /**
     * 判断是否 选择了区域
     * @return
     */
    private boolean isAmplificationChange() {
        //截取剩余时间或者截取时间少于1秒 不能截取
        if (Math.abs(mOb.nEnd - mOb.nStart - srcVideoDuration) < 1 || mOb.nEnd - mOb.nStart < 1) {
            return false;
        }
        return true;
    }

    /**
     * 最短时间
     */
    private final int MIN_THUMB_DURATION = 1000;

    /**
     * 设置时间格式
     * @param progress
     * @return
     */
    public String getTime(int progress) {
        progress = Math.max(0, progress);
        return DateTimeUtils.stringForMillisecondTime(progress, true, true);
    }

    /**
     * 设置修剪 剪除
     */
    private void constructAndCutoff() {
        if (mConstructAndCutoff == 1) {
            mBtnConstruct.setBackgroundResource(R.drawable.trim_video_construct_p);
            mBtnConstruct.setTextColor(Color.BLACK);
            mBtnCutoff.setBackgroundResource(R.drawable.cover_tail_n);
            mBtnCutoff.setTextColor(Color.WHITE);
        } else if (mConstructAndCutoff == 2){
            mBtnConstruct.setBackgroundResource(R.drawable.cover_head_n);
            mBtnConstruct.setTextColor(Color.WHITE);
            mBtnCutoff.setBackgroundResource(R.drawable.trim_video_cutoff_p);
            mBtnCutoff.setTextColor(Color.BLACK);
        }
        //设置显示默认
        mRangeSeekBar.setShaowType(mConstructAndCutoff);
        mRangeSeekBar.invalidate();
        onTrimText(Utils.s2ms(mOb.TStart), Utils.s2ms(mOb.TEnd));
        //暂停
        pauseVideo();
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
        if (mConstructAndCutoff == 2) {
            if (Math.abs(mOb.nEnd - mOb.nStart - srcVideoDuration) < 1) {
                Toast.makeText(this, getString(R.string.trim_video_area), Toast.LENGTH_SHORT).show();
                return;
            }
        }
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

    //防止频繁seekto
    private long mLastProgress = 0;

    /**
     * 单位:毫秒
     *
     * @param currentValue
     */
    private void seekTo(long currentValue) {
        int np = (int) currentValue;
        if (np < 500 || (Math.abs(mLastProgress - np) > 150)) {// 防止频繁seekto
            mMediaPlayer.seekTo(Utils.ms2s(np));
            mLastProgress = np;
        }
    }

    /**
     * 返回
     */
    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            setFullScreen(false);
        } else {
            onShowAlert();
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

    /**
     * 放大选择区域提示
     */
    private void onAmplificationAlert() {
        SysAlertDialog.createAlertDialog(this,
                getString(R.string.preview_trim),
                getString(R.string.trim_video_delete),
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
                        SysAlertDialog.showLoadingDialog(InterceptActivity.this, R.string.isloading);
                        pauseVideo();
                        onAmplification();
                    }
                }, false, null).show();
    }

    //取消导出
    private final int FULL_SCREEN = 100;

    private Handler mHandler = new Handler() {
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

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mMediaPlayer) {
            pauseVideo();
        }
        if (mBreakpoint.size() > 1) {
            intercept();
        }
    }

    @Override
    protected void onDestroy() {
        if (null != mMediaPlayer) {
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        if(null != mThumbNailView) {
            mThumbNailView.recycle();
            mThumbNailView = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBreakpoint.size() > 1) {
            cancel();
        }
    }

}
