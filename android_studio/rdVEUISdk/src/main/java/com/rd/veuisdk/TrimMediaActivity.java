package com.rd.veuisdk;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.crop.CropView;
import com.rd.veuisdk.manager.TrimConfiguration;
import com.rd.veuisdk.mix.ModeUtils;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.ExtProgressDialog;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.HorizontalScrollViewEx;
import com.rd.veuisdk.ui.ProgressView;
import com.rd.veuisdk.ui.VideoThumbNailAlterView;
import com.rd.veuisdk.ui.extrangseekbar.ExtRangeSeekbarPlus;
import com.rd.veuisdk.ui.extrangseekbar.ExtRangeSeekbarPlus.onRangDurationListener;
import com.rd.veuisdk.ui.extrangseekbar.RangSeekBarBase;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.FileLog;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.io.File;


/**
 * 截取
 *
 * @author JIAN
 */
@SuppressLint("HandlerLeak")
public class TrimMediaActivity extends BaseActivity {
    private static final String TAG = "TrimMediaActivity";

    private final int MIN_THUMB_DURATION = 1000;
    private int TRIM_LONG = 4;
    private int TRIM_SHOT = 2;
    private int TRIM_SINGLE = 0;
    private final String ISFIRST = "thisisfirst";

    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvRemainDuration;
    private TextView mTvFrontTime;
    private TextView mTvBehindTime;
    private RelativeLayout mRlTitleBar;

    private ImageView mIvVideoPlayState;

    private boolean mHasChanged = false;
    private VirtualVideoView mMediaPlayer;
    private boolean mIsLongClick;
    private int mFrameCount;

    private Scene mScene;
    private MediaObject mMediaObject;
    private VideoOb mOb;
    private ProgressView mProgressView;
    private VideoThumbNailAlterView mThumbNailView;
    private boolean mIsFirst = true;
    private String mSavePath;
    private RadioGroup mMenuGroup;
    private TextView mTvSingleFixedTime;
    private RadioButton mRbShotTime, mRbLongTime;
    private int mSingleFixText = 0;
    private int mShotText = 2;
    private int mLongText = 4;

    private RelativeLayout mRlAddTime;
    private TextView mTvAddtime;
    private TextView mTvOldTime;
    private boolean mOnChange = true;
    private int mOldSeekbarTime;

    private CheckBox mCbSquare;
    private boolean mIsSquare;

    private int mPlaybackWidth;

    private float mMediaRatio;

    ScrollView mSvPlayer;
    HorizontalScrollViewEx mHsvPlayer;
    private boolean mIsLandVideo;

    private boolean mEnableSquare = true;

    private int mTrimReturnType;

    private TrimConfiguration mTrimConfig;

    private int mTrimType;

    public static boolean mTrimFromEdit = false;

    //直接返回数据(画中画直接返回trim之后的视频路径)
    public static final String RESULT_DATA = "result_data";
    //仅裁剪时间轴
    public static final String ONLYLINE = "only_line_trime";
    public static final String CROP_ASPECTRATIO = "crop_aspectratio";
    //bFromMix  true, 画中画裁剪，预览时，视频内容完全显示，裁剪指定比例的视频内容
    private boolean bFromMix = false;
    private boolean bOnlyLine = true;
    private CropView cvCropView;
    private float cropAspRatio = 1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent in = getIntent();
        mTrimFromEdit = in.getBooleanExtra(IntentConstants.TRIM_FROM_EDIT, false);
        bFromMix = in.getBooleanExtra(RESULT_DATA, false);
        bOnlyLine = in.getBooleanExtra(ONLYLINE, true);
        cropAspRatio = in.getFloatExtra(CROP_ASPECTRATIO, 1f);
        super.onCreate(savedInstanceState);

        mStrActivityPageName = getString(R.string.preview_intercept);
        setContentView(R.layout.activity_trim_meida);

        mTrimConfig = SdkEntry.getSdkService().getTrimConfig();
        mPlaybackWidth = getWindowManager().getDefaultDisplay().getWidth();

        mEnableSquare = mTrimConfig.enable1x1;
        mTrimReturnType = mTrimConfig.trimReturnMode;

        mShotText = mTrimConfig.trimDuration1;
        mLongText = mTrimConfig.trimDuration2;
        mSingleFixText = mTrimConfig.trimSingleFixDuration;
        TRIM_SHOT = mShotText * 1000;
        TRIM_LONG = mLongText * 1000;
        TRIM_SINGLE = mSingleFixText * 1000;
        mTrimType = mTrimConfig.trimType;
        mScene = in.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (mTrimFromEdit) {
            mEnableSquare = false;
            mTrimType = TrimConfiguration.TRIM_TYPE_FREE;
        }
        registerReceiver(mReceiver, new IntentFilter(SdkEntry.TRIM_RETURN));
        if (mScene == null) {
            Log.w(TAG, "Trim media object not exists!");
            finish();
            return;
        }
        mMediaObject = mScene.getAllMedia().get(0);
        mOb = (VideoOb) mMediaObject.getTag();

        mMediaRatio = (float) mMediaObject.getWidth() / mMediaObject.getHeight();
        if (mMediaRatio >= 1.0) {
            mIsLandVideo = true;
        } else {
            mIsLandVideo = false;
        }
        if (bFromMix) {//画中画裁剪，固定显示方式
            mIsLandVideo = true;
        }
        initView();
        loadVideo();
    }

    private void loadVideo() {
        Intent in = getIntent();
        mIsShowLight = in.getBooleanExtra(ISFIRST, true);


        if (mOb == null) {
            mOb = new VideoOb(mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), 0, null, 0);
        }


        mMediaObject.setTimeRange(mOb.TStart, mOb.TEnd);

        VirtualVideo virtualVideo = new VirtualVideo();
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMediaObject);
        virtualVideo.addScene(scene);
        try {
            virtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private ExtRangeSeekbarPlus mRangeSeekBar;


    private void initView() {
        mSavePath = mTrimConfig.savePath;
        String strTitle = mTrimConfig.title;
        int titleBarColor = mTrimConfig.titleBarColor;
        String cancelText = mTrimConfig.buttonCancelText;
        String confirmText = mTrimConfig.buttonConfirmText;
        int buttonColor = mTrimConfig.buttonColor;

        mSvPlayer = (ScrollView) findViewById(R.id.svPlayer);
        mHsvPlayer = (HorizontalScrollViewEx) findViewById(R.id.hsvPlayer);

        mRlAddTime = (RelativeLayout) findViewById(R.id.rlAddTime);
        mTvAddtime = (TextView) findViewById(R.id.tvAddTime);
        mTvOldTime = (TextView) findViewById(R.id.tvOldTime);

        if (mIsLandVideo) {
            mSvPlayer.setVisibility(View.GONE);
            mHsvPlayer.setVisibility(View.VISIBLE);
            mProgressView = (ProgressView) findViewById(R.id.progressViewHori);
            //外层框
            mPflVideoPreview = (PreviewFrameLayout) findViewById(R.id.rlPreviewHori);
            //播放器的框
            mPreviewPlayer = (PreviewFrameLayout) findViewById(R.id.rlPreview_playerHori);
            mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreviewHori);
            cvCropView = (CropView) findViewById(R.id.cvVideoCropHori);

        } else {
            mSvPlayer.setVisibility(View.VISIBLE);
            mHsvPlayer.setVisibility(View.GONE);
            mPflVideoPreview = (PreviewFrameLayout) findViewById(R.id.rlPreview);
            mPreviewPlayer = (PreviewFrameLayout) findViewById(R.id.rlPreview_player);
            mMediaPlayer = (VirtualVideoView) findViewById(R.id.epvPreview);
            cvCropView = (CropView) findViewById(R.id.cvVideoCrop);
            mProgressView = (ProgressView) findViewById(R.id.progressView);
        }
        if (bFromMix) {
            if (bOnlyLine) {
                cvCropView.setVisibility(View.GONE);
            } else {
                cvCropView.setVisibility(View.VISIBLE);
                cvCropView.setCanMove(true);
                cvCropView.setIcropListener(new CropView.ICropListener() {
                    @Override
                    public void onPlayState() {
                    }

                    @Override
                    public void onMove() {
                    }
                });
            }


        } else {
            cvCropView.setVisibility(View.GONE);
        }
        LinearLayout.LayoutParams rlVideolp = new LinearLayout.LayoutParams(
                mPlaybackWidth, mPlaybackWidth);
        mPflVideoPreview.setLayoutParams(rlVideolp);
        mHsvPlayer.enableScroll(false);
        mRlTitleBar = (RelativeLayout) findViewById(R.id.rlTitleBar);
        mPflVideoPreview.setAspectRatio(1);
        if (strTitle != null) {
            ((TextView) findViewById(R.id.tvTitle)).setText(strTitle);
        }
        if (titleBarColor != 0) {
            mRlTitleBar.setBackgroundColor(titleBarColor);
        }
        if (buttonColor != 0) {
            findViewById(R.id.public_menu_sure).setBackgroundColor(
                    buttonColor);
            findViewById(R.id.public_menu_cancel).setBackgroundColor(
                    buttonColor);
        }
        if (confirmText != null) {
            ExtButton ebtnSure = (ExtButton) findViewById(R.id.ebtnSure);
            ebtnSure.setText(confirmText);
            ebtnSure.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (cancelText != null) {
            ExtButton ebtnCancel = (ExtButton) findViewById(R.id.ebtnCancel);
            ebtnCancel.setText(cancelText);
            ebtnCancel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        // 切换裁切比例
        mCbSquare = (CheckBox) findViewById(R.id.cbTrim1x1);
        mCbSquare.setOnCheckedChangeListener(mOnSquareCheckListener);
        if (bFromMix) {
            mCbSquare.setVisibility(View.INVISIBLE);
            mProgressView.setVisibility(View.GONE);
        } else {
            mProgressView.setScroll(true);
            mProgressView.setListener(mOnProListener);
            if (mEnableSquare) {
                mCbSquare.setVisibility(View.VISIBLE);
            } else {
                mCbSquare.setVisibility(View.INVISIBLE);
            }
        }

        mMenuGroup = (RadioGroup) findViewById(R.id.trim_menu_group);
        mTvSingleFixedTime = (TextView) findViewById(R.id.tv_single_fixed_time);
        mRbShotTime = (RadioButton) findViewById(R.id.trim_shot);
        mRbLongTime = (RadioButton) findViewById(R.id.trim_long);

        if (mTrimType == TrimConfiguration.TRIM_TYPE_SINGLE_FIXED) {
            mMenuGroup.setVisibility(View.INVISIBLE);
            mTvSingleFixedTime.setVisibility(View.VISIBLE);
            mTvSingleFixedTime.setText(mSingleFixText + "s");
        } else if (mTrimType == TrimConfiguration.TRIM_TYPE_DOUBLE_FIXED) {
            mMenuGroup.setVisibility(View.VISIBLE);
            mTvSingleFixedTime.setVisibility(View.INVISIBLE);
        }
        mRbShotTime.setText(mShotText + "s");
        mRbLongTime.setText(mLongText + "s");
        mMenuGroup.setVisibility(View.INVISIBLE);

        mThumbNailView = (VideoThumbNailAlterView) findViewById(R.id.split_videoview);

        mRangeSeekBar = (ExtRangeSeekbarPlus) findViewById(R.id.m_extRangeSeekBar);

        // 不将边缘变成灰色
        mRangeSeekBar.setHorizontalFadingEdgeEnabled(false);


        if (mTrimType == TrimConfiguration.TRIM_TYPE_FREE) {
            mRangeSeekBar.setMoveMode(true);  //自由截取把手
            mRangeSeekBar
                    .setOnRangSeekBarChangeListener(mRangeSeekBarChangeListener);
        } else {
            mRangeSeekBar.setMoveMode(false);  //定长截取框
            mRangeSeekBar.setItemVideo(mOnVideoTrimListener);
            mMenuGroup.setOnCheckedChangeListener(mOnMenuGroupListener);
        }

        mPreviewPlayer.setOnClickListener(mOnPlayListener);

        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        mIvVideoPlayState.setOnClickListener(mOnPlayListener);

        mTvFrontTime = (TextView) findViewById(R.id.tvInterceptFrontTime);
        mTvBehindTime = (TextView) findViewById(R.id.tvInterceptBehindTime);
        mTvRemainDuration = (TextView) findViewById(R.id.tvRemainDuration);

        mMediaPlayer.setOnPlaybackListener(mPlayViewListener);
        mMediaPlayer.setOnInfoListener(mPlayerInfoListener);

    }

    private Handler mHideTitleHandler = new Handler();
    private Runnable mHideTitleRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRlTitleBar != null) {
                mRlTitleBar.setVisibility(View.INVISIBLE);
            }
        }
    };


    private CompoundButton.OnCheckedChangeListener mOnSquareCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mIsSquare = isChecked;
            if (isChecked) {
                mProgressView.setScroll(false);
                if (bFromMix) {
                    mHsvPlayer.enableScroll(false);
                } else {
                    mHsvPlayer.enableScroll(true);
                }
                RelativeLayout.LayoutParams lpSvPlayer = new RelativeLayout.LayoutParams(
                        mPlaybackWidth, mPlaybackWidth);
                mSvPlayer.setLayoutParams(lpSvPlayer);
                mHsvPlayer.setLayoutParams(lpSvPlayer);
                mSvPlayer.post(new Runnable() {
                    @Override
                    public void run() {
                        mSvPlayer.scrollTo(0,
                                ((int) (mPlaybackWidth / mMediaRatio) - mPlaybackWidth) / 2);
                    }
                });
                mHsvPlayer.post(new Runnable() {
                    @Override
                    public void run() {
                        mHsvPlayer.scrollTo(
                                ((int) (mPlaybackWidth * mMediaRatio) - mPlaybackWidth) / 2, 0);
                    }
                });
                if (bFromMix) {
                    mPflVideoPreview.setAspectRatio(1);
                } else {
                    LinearLayout.LayoutParams rlVideolp;
                    if (mMediaRatio > 1.0) {
                        rlVideolp = new LinearLayout.LayoutParams(
                                (int) (mPlaybackWidth * mMediaRatio),
                                mPlaybackWidth);
                    } else {
                        rlVideolp = new LinearLayout.LayoutParams(
                                mPlaybackWidth,
                                (int) (mPlaybackWidth / mMediaRatio));
                    }
                    mPflVideoPreview.setAspectRatio(mMediaRatio);
                    mPflVideoPreview.setLayoutParams(rlVideolp);
                }
            } else {
                mProgressView.setScroll(true);
                mHsvPlayer.enableScroll(false);
                RelativeLayout.LayoutParams lpSvPlayer = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                mSvPlayer.setLayoutParams(lpSvPlayer);
                mHsvPlayer.setLayoutParams(lpSvPlayer);
                LinearLayout.LayoutParams rlVideolp = new LinearLayout.LayoutParams(
                        mPlaybackWidth, mPlaybackWidth);
                mPflVideoPreview.setLayoutParams(rlVideolp);
                mPflVideoPreview.setAspectRatio(1);
            }
        }
    };

    private OnClickListener mOnPlayListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            playOrPause();
        }
    };

    private void playOrPause() {
        mRlTitleBar.bringToFront();

        mIvVideoPlayState.bringToFront();
        if (mMediaPlayer.isPlaying()) {
            pauseVideo();
            mHideTitleHandler.postDelayed(mHideTitleRunnable, 5000);
            mRlTitleBar.setVisibility(View.VISIBLE);
        } else {
            if (mRightHandleChanged) {
                mMediaPlayer.seekTo(mOb.nStart);
                mRightHandleChanged = false;
            }
            playVideo();
            mHideTitleHandler.removeCallbacks(mHideTitleRunnable);
            mRlTitleBar.setVisibility(View.INVISIBLE);
        }
    }

    private RadioGroup.OnCheckedChangeListener mOnMenuGroupListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            if (checkedId == mRbShotTime.getId()) {
                if (null != mOb) {
                    int viewDuration = (int) (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed());
                    // 短
                    int nduration;
                    if (mTrimType == TrimConfiguration.TRIM_TYPE_SINGLE_FIXED) {
                        nduration = viewDuration < TRIM_SINGLE ? viewDuration : TRIM_SINGLE;
                    } else {
                        nduration = viewDuration < TRIM_SHOT ? viewDuration : TRIM_SHOT;
                    }
                    mRangeSeekBar.setItemDuration(nduration);
                }
                mRbLongTime.setBackgroundResource(0);
                mRbShotTime
                        .setBackgroundResource(R.drawable.menu_item_checked);
            } else { // 长
                if (null != mOb) {
                    int viewDuration = (int) (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed());
                    int nduration = viewDuration < TRIM_LONG ? viewDuration
                            : TRIM_LONG;
                    mRangeSeekBar.setItemDuration(nduration);
                }
                mRbShotTime.setBackgroundResource(0);
                mRbLongTime
                        .setBackgroundResource(R.drawable.menu_item_checked);
            }
            doPrepareTrim();
        }
    };

    private void doPrepareTrim() {
        long nstart = mRangeSeekBar.getSelectedMinValue(), nend = mRangeSeekBar
                .getSelectedMaxValue();
        mOnVideoTrimListener.onItemVideoChanged(nstart, nend);
    }

    private final int PREPARED = 6;
    private final int CANCEL_EXPORT = 7;

    private Dialog mCancelLoading;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case PREPARED:
                    int halfScreenWidth = CoreUtils.getMetrics().widthPixels / 2;
                    VirtualVideo player = (VirtualVideo) msg.obj;
                    try {
                        player.build(TrimMediaActivity.this);
                    } catch (InvalidStateException e) {
                        e.printStackTrace();
                    }
                    int[] params = mThumbNailView.setVirtualVideo(true, player);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            params[0], params[1]);
                    lp.setMargins(0, 0, halfScreenWidth, 0);
                    mThumbNailView.setLayoutParams(lp);

                    FrameLayout.LayoutParams lframe = new LayoutParams(lp.width
                            + lp.leftMargin + lp.rightMargin, lp.height);
                    lframe.setMargins(0, 10, 0, 0);

                    mThumbNailView.setStartThumb();

                    if (null != mOb) {
                        int viewDuration = (int) (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed());
                        int nduration;
                        if (mTrimType == TrimConfiguration.TRIM_TYPE_SINGLE_FIXED) {
                            nduration = viewDuration < TRIM_SINGLE ? viewDuration
                                    : TRIM_SINGLE;
                            mRangeSeekBar.setItemDuration(nduration);
                        } else if (mTrimType == TrimConfiguration.TRIM_TYPE_DOUBLE_FIXED) {
                            nduration = viewDuration < TRIM_SHOT ? viewDuration
                                    : TRIM_SHOT;
                            mRangeSeekBar.setItemDuration(nduration);
                        }

                    }
                    if (mTrimType == TrimConfiguration.TRIM_TYPE_SINGLE_FIXED) {
                        mMenuGroup.setVisibility(View.INVISIBLE);
                        mTvSingleFixedTime.setVisibility(View.VISIBLE);
                    } else if (mTrimType == TrimConfiguration.TRIM_TYPE_DOUBLE_FIXED) {
                        mMenuGroup.check(R.id.trim_shot);
                        mMenuGroup.setVisibility(View.VISIBLE);
                        mTvSingleFixedTime.setVisibility(View.INVISIBLE);
                        mRbLongTime.setBackgroundResource(0);
                        mRbShotTime.setBackgroundResource(R.drawable.menu_item_checked);
                    }
                    mRangeSeekBar.setVisibility(View.VISIBLE);
                    mMediaPlayer.seekTo((mOb.nStart + 0.25f));//防止预览缩略图黑屏
                    doPrepareTrim();
                    break;
                case CANCEL_EXPORT:
                    mCancelLoading = SysAlertDialog.showLoadingDialog(
                            TrimMediaActivity.this, R.string.canceling, false,
                            new OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (null != mVideoSave) {
                                        mVideoSave = null;
                                    }
                                    mCancelLoading = null;
                                }
                            });
                    mCancelLoading.show();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (null != mCancelLoading)
                                mCancelLoading.setCancelable(true);
                        }
                    }, 5000);
                    break;
                default:
                    break;
            }

        }

        ;
    };

    private static boolean mIsShowLight = true;

    private VirtualVideo.OnInfoListener mPlayerInfoListener = new VirtualVideo.OnInfoListener() {

        @Override
        public boolean onInfo(int what, int extra, Object obj) {
            if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
                SysAlertDialog.showLoadingDialog(TrimMediaActivity.this,
                        R.string.isloading, false, null);
            } else if (what == VirtualVideo.INFO_WHAT_GET_VIDEO_HIGHTLIGHTS && obj != null) {
                int[] arrHightLights = (int[]) obj;
                // TODO:hightlight时间数组，单位为ms
                if (mIsShowLight) {
                    mRangeSeekBar.setHighLights(arrHightLights);
                    mIsShowLight = false;
                }
            }
            return false;
        }

    };
    private ExtProgressDialog mPdLoading;
    private MediaObject mTempObjThumb;
    private RectF mRectVideoClipBound = new RectF();
    /**
     * 播放器
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {

        @Override
        public void onPlayerPrepared() {


            if (mIsFirst) {
                if (mTrimConfig.default1x1CropMode) {
                    mCbSquare.setChecked(true);
                } else {
                    mCbSquare.setChecked(false);
                }
                ThreadPoolUtils.executeEx(new Runnable() {

                    @Override
                    public void run() {
                        VirtualVideo virtualVideo = new VirtualVideo();
                        Scene scene = VirtualVideo.createScene();
                        mTempObjThumb = mMediaObject.clone();
                        mTempObjThumb.setTimeRange(mOb.TStart, mOb.TEnd);
                        scene.addMedia(mTempObjThumb);
                        virtualVideo.addScene(scene);
                        mHandler.sendMessage(mHandler.obtainMessage(PREPARED,
                                virtualVideo));

                    }
                });
                mIsFirst = false;
            }


            SysAlertDialog.cancelLoadingDialog();
            updatePreviewFrameAspect(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());

            if (mOb != null) {
                mTvFrontTime.setText(gettime(Utils.s2ms(mOb.nStart)));
                mTvBehindTime.setText(gettime(Utils.s2ms(mOb.nEnd)));
                if (mOb.nEnd - mOb.nStart <= 1) {//1秒
                    mTvRemainDuration.setText(gettime(1000));
                } else {
                    mTvRemainDuration.setText(gettime(Utils.s2ms(mOb.nEnd - mOb.nStart)));
                }
            }

            if (mOb != null) {
                int viewDuration = (int) (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed());
                mRangeSeekBar.setDuration(viewDuration);
                mRangeSeekBar.setSeekBarRangeValues(Utils.s2ms(mOb.nStart), Utils.s2ms(mOb.nEnd));
                mRangeSeekBar.setProgress(Utils.s2ms(mOb.nStart));
            }
            if (mOb != null) {
                mProgressView.setDuration(Utils.s2ms(mOb.TEnd - mOb.TStart));
                mProgressView.setProgress(Utils.s2ms(mOb.nStart));
            }
            if (null != mPdLoading) {
                mPdLoading = SysAlertDialog.showLoadingDialog(getApplicationContext(),
                        R.string.isloading);
                mPdLoading.show();
            }
//            mPflVideoPreview.post(new Runnable() {
//                public void run() {
//                    mMediaPlayer.seekTo(Math.max(mOb.nEnd, 0.3f));
//                }
//            });


            VideoConfig srcConfig = new VideoConfig();
            RectF videoBound = new RectF(0, 0, mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            if (VirtualVideo.getMediaInfo(mMediaObject.getMediaPath(), srcConfig) > 0) {
                videoBound.set(0, 0, srcConfig.getVideoWidth(), srcConfig.getVideoHeight());
            }

            if (mRectVideoClipBound.isEmpty()) {
                int tmpW = (int) videoBound.width(), tmpH = (int) videoBound.height();
                Rect temp = new Rect();
                MediaObject.getClipSrcRect(tmpW, tmpH, cropAspRatio, temp);
                mRectVideoClipBound = new RectF(temp);
//                Log.i(TAG, bCanScaleBigger + "." + tmpW + "*" + tmpH + "onPlayerPrepared: " + mRectVideoClipBound.toShortString() + "....temp.>" + temp.toShortString() + "....>" + temp.width() + "*" + temp.height());

            }
            if (!bOnlyLine) {
                cvCropView.initialize(mRectVideoClipBound, videoBound, 0);
                cvCropView.applyAspect(1, 1 / cropAspRatio);
                if (!bCanScaleBigger) {
                    cvCropView.setLockSize(true);
                }
                cvCropView.setCanMove(true);
            }
//            Log.e(TAG + cropAspRatio, mMediaPlayer.getVideoWidth() + "*" + mMediaPlayer.getVideoHeight() + "onPlayerPrepared: videoBound" + videoBound.toShortString() + "...clip:" + mRectVideoClipBound.toShortString());
        }

        private boolean bCanScaleBigger = true;

        @Override
        public boolean onPlayerError(int what, int extra) {
            Log.e(TAG, "Player error:" + what + "," + extra);
            SysAlertDialog.cancelLoadingDialog();
            SysAlertDialog.showAlertDialog(TrimMediaActivity.this,
                    getString(R.string.edit_priview),
                    getString(R.string.error_preview_trim),
                    getString(R.string.sure), null, null, null);
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            mMediaPlayer.seekTo(mOb.rStart);
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            mIvVideoPlayState.setVisibility(View.VISIBLE);
            mRangeSeekBar.setProgress(mRangeSeekBar.getSelectedMinValue());
        }

        @Override
        public void onGetCurrentPosition(float position) {
            if (mIsLongClick && position > 0) {
                mFrameCount++;
                if (mFrameCount > 1) {
                    mMediaPlayer.pause();
                    mFrameCount = 0;
                }
            }

            int nPosition = Utils.s2ms(position);
            if (mHasChanged) {
                mRangeSeekBar.setProgress(nPosition);
                mProgressView.setProgress(nPosition);
            } else {
                if (null != mOb) {
                    mRangeSeekBar.setProgress(nPosition);
                }
                mProgressView.setProgress(nPosition);
            }
            if (nPosition < Utils.s2ms(mOb.nStart) - 50) {
                mMediaPlayer.seekTo(mOb.nStart);
                mProgressView.setProgress(Utils.s2ms(mOb.nStart));
            }
            if (nPosition > Utils.s2ms(mOb.nEnd)) {
                mMediaPlayer.seekTo(mOb.nStart);
                mProgressView.setProgress(Utils.s2ms(mOb.nStart));
                mRangeSeekBar.setProgress(Utils.s2ms(mOb.nStart));
                mMediaPlayer.pause();
            }
        }


    };

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
            mProgressView.setProgress(np);
            mLastProgress = np;
        }

    }

    private String gettime(int progress) {
        progress = Math.max(0, progress);
        return DateTimeUtils.stringForMillisecondTime(progress, true, true);
    }

    /**
     * 更新预览视频播放器比例
     */
    protected void updatePreviewFrameAspect(int nVideoWidth, int nVideoHeight) {
        mPreviewPlayer.setAspectRatio(nVideoWidth / (nVideoHeight + 0.0f));
    }

    private PreviewFrameLayout mPreviewPlayer;
    private VirtualVideo mVideoSave;

    @Override
    public void onBackPressed() {
        finish();
    }

    String mStrSaveMp4FileName;

    @Override
    public void clickView(View v) {
        super.clickView(v);
        int id = v.getId();

        if (id == R.id.public_menu_sure || id == R.id.ebtnSure) {
            if (mTrimFromEdit) {
                Intent data = new Intent();
                mMediaObject.setTimeRange(mOb.TStart + mOb.rStart, mOb.TStart + mOb.rEnd);
                Scene scene = new Scene();
                scene.addMedia(mMediaObject);
                data.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
                data.putExtra(ISFIRST, false);
                setResult(RESULT_OK, data);
                onBackPressed();
            } else {
                if (mTrimReturnType == TrimConfiguration.TRIM_DYNAMIC_RETURN) {
                    SdkEntryHandler.getInstance().onTrimDialog(
                            TrimMediaActivity.this,
                            SdkEntry.TRIMVIDEO_DURATION_EXPORT);
                } else if (mTrimReturnType == TrimConfiguration.TRIM_RETURN_MEDIA) {
                    stopVideo();
                    exportVideo();
                } else {
                    if (mOb != null) {
                        Intent i = new Intent();
                        float start = mOb.nStart;
                        float end = mOb.nEnd;
                        i.putExtra(SdkEntry.TRIM_START_TIME, start);
                        i.putExtra(SdkEntry.TRIM_END_TIME, end);
                        i.putExtra(SdkEntry.TRIM_MEDIA_PATH, mMediaObject.getMediaPath());
                        if (mIsSquare) {
                            int left = (int) (mMediaObject.getWidth() * ((float) mHsvPlayer
                                    .getScrollX() / mPflVideoPreview.getWidth()));
                            int right = (int) (mMediaObject.getWidth()
                                    * ((float) mHsvPlayer.getScrollX() + mPlaybackWidth) / mPflVideoPreview.getWidth());
                            int top = (int) (mMediaObject.getHeight() * ((float) mSvPlayer
                                    .getScrollY() / mPflVideoPreview.getHeight()));
                            int bottom = (int) (mMediaObject.getHeight()
                                    * ((float) mSvPlayer.getScrollY() + mPlaybackWidth) / mPflVideoPreview.getHeight());
                            Rect rcCrop = new Rect(left, top, right, bottom);
                            i.putExtra(SdkEntry.TRIM_CROP_RECT, rcCrop);
                        }
                        setResult(RESULT_OK, i);
                        SdkEntryHandler.getInstance().onInterceptVideoDuration(
                                TrimMediaActivity.this, start, end);
                        TrimMediaActivity.this.finish();
                    }
                }
            }
        } else if (id == R.id.public_menu_cancel || id == R.id.ebtnCancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();
        }
    }

    private ExportListener mListenerSave = new ExportListener() {


        private HorizontalProgressDialog hpdSave;

        @Override
        public void onExportStart() {
            if (hpdSave == null) {
                String str = bFromMix ? getString(R.string.exportining) : getString(R.string.exporting);

                hpdSave = SysAlertDialog.showHoriProgressDialog(
                        TrimMediaActivity.this, str,
                        false, true, new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mVideoSave.cancelExport();
                                hpdSave = null;
                                mHandler.obtainMessage(CANCEL_EXPORT)
                                        .sendToTarget();
                            }
                        });
                hpdSave.setCanceledOnTouchOutside(false);
                hpdSave.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                    @Override
                    public void onCancel() {
                        SysAlertDialog.showAlertDialog(
                                TrimMediaActivity.this,
                                "",
                                getString(R.string.cancel_export_video),
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
                                        hpdSave.cancel();
                                        hpdSave.dismiss();
                                        mVideoSave.cancelExport();
                                    }
                                });
                    }
                });
            }
            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public boolean onExporting(int nProgress, int nMax) {
            if (null != hpdSave) {
                hpdSave.setProgress(nProgress);
                hpdSave.setMax(nMax);
            }
            return true;
        }

        @Override
        public void onExportEnd(int nResult) {
            if (null != mVideoSave) {
                mVideoSave.release();
                mVideoSave = null;
            }
            mMediaObject.setTimeRange(mOb.TStart, mOb.TEnd);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if (nResult >= VirtualVideo.RESULT_SUCCESS) {
                if (!TextUtils.isEmpty(mStrSaveMp4FileName)) {
                    if (bFromMix) {
                        Intent i = new Intent();
                        i.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH, mStrSaveMp4FileName);
                        setResult(RESULT_OK, i);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra(SdkEntry.TRIM_MEDIA_PATH, mStrSaveMp4FileName);
                        TrimMediaActivity.this.setResult(RESULT_OK, intent);
                        SdkEntryHandler.getInstance().onTrimDurationExport(
                                TrimMediaActivity.this, mStrSaveMp4FileName);
                    }
                    TrimMediaActivity.this.finish();
                }
            } else {
                new File(mStrSaveMp4FileName).delete();
                if (nResult != VirtualVideo.RESULT_EXPORT_CANCEL) {
                    if (nResult == VirtualVideo.RESULT_CORE_ERROR_ENCODE_VIDEO
                            && mHWCodecEnabled) {
                        // FIXME:开启硬编后出现了编码错误，使用软编再试一次
                        mHWCodecEnabled = false;
                        exportVideo();
                        return;
                    }
                    String strMessage = getString(R.string.export_failed);
                    SysAlertDialog.showAutoHideDialog(TrimMediaActivity.this,
                            null, strMessage, Toast.LENGTH_SHORT);
                    FileLog.writeLog(strMessage + ",result:" + nResult);
                } else if (null != mCancelLoading) {
                    mCancelLoading.dismiss();
                    mCancelLoading = null;
                }
            }
            if (hpdSave != null) {
                hpdSave.dismiss();
                hpdSave = null;
            }
        }


    };

    @Override
    protected void onPause() {
        // listView.setResume(false);
        super.onPause();
        mMediaPlayer.pause();
        if (null != mMediaPlayer) {
            pauseVideo();
        }

    }

    @Override
    protected void onDestroy() {
        if (null != mMediaPlayer) {
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }

        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsFirst && !mFromReceiver) {
            playVideo();
        }
        mFromReceiver = false;

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
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    private void stopVideo() {
        mMediaPlayer.stop();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
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
                int mtd = (int) (maxValue - minValue);
                if (mtd <= MIN_THUMB_DURATION
                        && (Utils.s2ms(mOb.TEnd - mOb.TStart)) >= MIN_THUMB_DURATION) {
                    minValue = maxValue - MIN_THUMB_DURATION;
                }
                mOb.nStart = Utils.ms2s(minValue);
                mOb.nEnd = Utils.ms2s(maxValue);


                mOb.rStart = Utils.ms2s((int) (minValue * mMediaObject.getSpeed()));
                mOb.rEnd = Utils.ms2s((int) (maxValue * mMediaObject.getSpeed()));

//                Log.e(TAG, "prepareMedia: R" + mOb.rStart + "<>" + mOb.rEnd + "--------------n->" + mOb.nStart + "<>" + mOb.nEnd);


                mTvFrontTime.setText(gettime(Utils.s2ms(mOb.nStart)));
                mTvBehindTime.setText(gettime(Utils.s2ms(mOb.nEnd)));
                mRangeSeekBar.setSeekBarRangeValues(mRangeSeekBar.getSelectedMinValue(),
                        mRangeSeekBar.getSelectedMaxValue());
                mTvRemainDuration.setText(gettime(Math.max(MIN_THUMB_DURATION, mtd)));


            }
        }
    }

    // 定长截取listener
    private onRangDurationListener mOnVideoTrimListener = new onRangDurationListener() {

        @Override
        public void onItemVideoChanged(long minValue, long maxValue) {
            prepareMedia(minValue, maxValue);
        }

        @Override
        public void onItemVideoChanging(long start, long end) {

            if (null != mMediaObject) {
                if (null != mOb) {
                    mTvFrontTime.setText(gettime((int) start));
                    mTvBehindTime.setText(gettime((int) end));
                    if (end - start <= 1000) {
                        mTvRemainDuration.setText(gettime(1000));
                    } else {
                        mTvRemainDuration
                                .setText(gettime((int) (end - start)));
                    }
                }
                seekTo(start);
            }
        }

        @Override
        public void onSeekto(long seekto) {
            seekTo(seekto);
        }

        @Override
        public void onItemVideoPasue(int ntime) {
            pauseVideo();
            seekTo(ntime);
        }

    };

    // 自由截取listener
    private RangSeekBarBase.OnRangeSeekBarChangeListener mRangeSeekBarChangeListener = new RangSeekBarBase.OnRangeSeekBarChangeListener() {

        private int m_nCurrentThumbPressed;

        @Override
        public void rangeSeekBarValuesChanged(long minValue, long maxValue,
                                              long currentValue) {
//            Log.e("raSBarValuesChanged-", m_nCurrentThumbPressed + "xxxx" +
//                    minValue + "--" + maxValue + "--" + currentValue);
            switch (m_nCurrentThumbPressed) {
                case RangSeekBarBase.CURRENT_THUMB_PRESSED: // 指定当前值
                    mRightHandleChanged = false;
                    seekTo(currentValue);
                    break;
                case RangSeekBarBase.MAX_THUMB_PRESSED: // 指定范围 最小值
                    mRightHandleChanged = true;
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
            // Log.e("rangeSeekBarValuesChanging", m_nCurrentThumbPressed +
            // "---"
            // + setValue);
            switch (m_nCurrentThumbPressed) {
                case RangSeekBarBase.CURRENT_THUMB_PRESSED: // 指定当前值
                    seekTo((int) setValue);
                    break;
                case RangSeekBarBase.MIN_THUMB_PRESSED: // 指定范围 最小值
                case RangSeekBarBase.MAX_THUMB_PRESSED: // 指定范围 最大值
                    seekTo((int) setValue);
                    if (null != mMediaObject) {
                        if (null != mOb) {
                            mTvFrontTime.setText(gettime((int) mRangeSeekBar
                                    .getSelectedMinValue()));
                            mTvBehindTime.setText(gettime((int) mRangeSeekBar
                                    .getSelectedMaxValue()));
                            if (mRangeSeekBar.getSelectedMaxValue()
                                    - mRangeSeekBar.getSelectedMinValue() <= 1000) {
                                mTvRemainDuration.setText(gettime(1000));
                            } else {
                                mTvRemainDuration
                                        .setText(gettime((int) (mRangeSeekBar
                                                .getSelectedMaxValue() - mRangeSeekBar
                                                .getSelectedMinValue())));
                            }
                        }
                    }
                    break;
                default:
                    break;

            }
        }

        @Override
        public boolean beginTouch(int thumbPressed) {
            m_nCurrentThumbPressed = thumbPressed;
            // Log.e("beginTouch", m_nCurrentThumbPressed + "");
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

    };

    private boolean mRightHandleChanged;

    private ProgressView.onProgressListener mOnProListener = new ProgressView.onProgressListener() {

        boolean isplaying = false;

        @Override
        public void onStart() {
            isplaying = mMediaPlayer.isPlaying();
            if (isplaying) {
                pauseVideo();
            }
        }

        @Override
        public void onProgressing(int progress) {
            if (mRangeSeekBar.mCurHandle == mRangeSeekBar.HANDLE_NONE) {
                mRightHandleChanged = false;
                int tstart = Utils.s2ms(mOb.nStart);
                if (progress < tstart) {
                    progress = tstart;
                }
                int tend = Utils.s2ms(mOb.nEnd);
                if (progress > tend) {
                    progress = tend;
                }
                mMediaPlayer.seekTo(Utils.ms2s(progress));
                if (null != mMediaObject && mMediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    if (null != mOb) {
                        mRangeSeekBar.setProgress(progress);
                    }
                }
            }
        }

        @Override
        public void onChanged() {
            mRlAddTime.setVisibility(View.INVISIBLE);
            mOnChange = true;
            mProgressView.setShowTime(true);
            if (mRangeSeekBar.mCurHandle == mRangeSeekBar.HANDLE_LEFT
                    || mRangeSeekBar.mCurHandle == mRangeSeekBar.HANDLE_RIGHT) {

                if (mRangeSeekBar.mCurHandle == mRangeSeekBar.HANDLE_RIGHT) {
                    mRightHandleChanged = true;
                }
                if (null != mMediaObject) {
                    if (null != mOb) {
                        mOb.nStart = Utils.ms2s(mRangeSeekBar.getSelectedMinValue());
                        mOb.nEnd = Utils.ms2s(mRangeSeekBar.getSelectedMaxValue());

                        float rEnd = mOb.nEnd * mMediaObject.getSpeed();

                        mOb.rEnd = rEnd;
                        mOb.rStart = mOb.nStart * mMediaObject.getSpeed();

                        float nstart = mOb.rStart + mOb.TStart;
                        float nend = mOb.rEnd + mOb.TStart;

                        mTvFrontTime.setText(gettime((int) mRangeSeekBar
                                .getSelectedMinValue()));
                        mTvBehindTime.setText(gettime((int) mRangeSeekBar
                                .getSelectedMaxValue()));
                        if (mOb.nEnd - mOb.nStart <= 1) {
                            mTvRemainDuration.setText(gettime(1000));
                        } else {
                            mTvRemainDuration.setText(gettime(Utils.s2ms(mOb.nEnd - mOb.nStart)));
                        }
                    }
                }
            }
        }

        @Override
        public void onClick() {
            playOrPause();
        }

        @Override
        public void onSeekbarChanging(int nscale) {
            int scale = nscale;
            if (mRangeSeekBar.mCurHandle == mRangeSeekBar.HANDLE_LEFT) {
                if (mOnChange) {
                    mOnChange = false;
                    mProgressView.setShowTime(false);
                    mOldSeekbarTime = (int) mRangeSeekBar.getSelectedMinValue();
                }

                mRlAddTime.setVisibility(View.VISIBLE);
                mTvOldTime.setText(gettime(mOldSeekbarTime));

                int np = Utils.s2ms(mOb.nStart) + scale;
                if (np < 0) {
                    np = 0;
                    scale = 0 - Utils.s2ms(mOb.nStart);
                }
                if (np > mRangeSeekBar.getSelectedMaxValue() - MIN_THUMB_DURATION) {
                    np = (int) (mRangeSeekBar.getSelectedMaxValue() - MIN_THUMB_DURATION);
                    scale = (int) (mRangeSeekBar.getSelectedMaxValue() - MIN_THUMB_DURATION)
                            - Utils.s2ms(mOb.nStart);
                }
                if (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed() <= MIN_THUMB_DURATION) {
                    scale = 0;
                }
                String strAddTime = DateTimeUtils.stringForMillisecondTime(np - Utils.s2ms(mOb.nStart));
                if (scale < 0) {
                    strAddTime = "-" + strAddTime;
                } else {
                    strAddTime = "+" + strAddTime;
                }

                mTvAddtime.setText(strAddTime);
                float second = Utils.ms2s(np);
                mRangeSeekBar.setMin(np);
                mMediaPlayer.seekTo(second);
                mProgressView.setProgress(np);

                if (null != mOb) {
                    mTvFrontTime.setText(gettime((int) mRangeSeekBar
                            .getSelectedMinValue()));
                    mTvBehindTime.setText(gettime((int) mRangeSeekBar
                            .getSelectedMaxValue()));
                    if (mRangeSeekBar.getSelectedMaxValue()
                            - mRangeSeekBar.getSelectedMinValue() <= 1000) {
                        mTvRemainDuration.setText(gettime(1000));
                    } else {
                        mTvRemainDuration.setText(gettime((int) (mRangeSeekBar
                                .getSelectedMaxValue() - mRangeSeekBar
                                .getSelectedMinValue())));
                    }

                }

            } else if (mRangeSeekBar.mCurHandle == mRangeSeekBar.HANDLE_RIGHT) {
                mRlAddTime.setVisibility(View.VISIBLE);
                if (mOnChange) {
                    mOnChange = false;
                    mProgressView.setShowTime(false);
                    mOldSeekbarTime = (int) mRangeSeekBar.getSelectedMaxValue();
                }
                mRlAddTime.setVisibility(View.VISIBLE);
                mTvOldTime.setText(gettime(mOldSeekbarTime));

                int np = Utils.s2ms(mOb.nEnd) + scale;
                if (np > Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed()) {
                    np = Utils.s2ms((mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed());
                    scale = np - Utils.s2ms(mOb.nEnd);
                }
                if (np < mRangeSeekBar.getSelectedMinValue() + MIN_THUMB_DURATION) {
                    np = (int) (mRangeSeekBar.getSelectedMinValue() + MIN_THUMB_DURATION);
                    scale = (int) (mRangeSeekBar.getSelectedMinValue() + MIN_THUMB_DURATION) - Utils.s2ms(mOb.nEnd);
                }
                if (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed() <= MIN_THUMB_DURATION) {
                    scale = 0;
                }
                String strAddTime = DateTimeUtils.stringForMillisecondTime(scale);
                if (scale < 0) {
                    strAddTime = "-" + strAddTime;
                } else {
                    strAddTime = "+" + strAddTime;
                }
                mTvAddtime.setText(strAddTime);
                float second = Utils.ms2s(np);
                mRangeSeekBar.setMax(np);
                mMediaPlayer.seekTo(second);
                mProgressView.setProgress(np);

                if (null != mOb) {
                    mTvFrontTime.setText(gettime((int) mRangeSeekBar
                            .getSelectedMinValue()));
                    mTvBehindTime.setText(gettime((int) mRangeSeekBar
                            .getSelectedMaxValue()));
                    // m_tvVideoDuration
                    // .setText(gettime((int) ((mOb.TEnd - mOb.TStart) / mOb
                    // .getVideoObjectSpeed())));
                    if (mRangeSeekBar.getSelectedMaxValue()
                            - mRangeSeekBar.getSelectedMinValue() <= 1000) {
                        mTvRemainDuration.setText(gettime(1000));
                    } else {
                        mTvRemainDuration.setText(gettime((int) (mRangeSeekBar
                                .getSelectedMaxValue() - mRangeSeekBar
                                .getSelectedMinValue())));
                    }
                }
            }
        }
    };

    private boolean mFromReceiver = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SdkEntry.TRIM_RETURN)) {
                int type = intent.getIntExtra(SdkEntry.TRIM_RETURN_TYPE, -1);
                mFromReceiver = true;
                if (type == 0) {
                    stopVideo();
                    exportVideo();
                } else if (type == 1) {
                    if (mOb != null) {
                        SdkEntryHandler.getInstance().onInterceptVideoDuration(
                                TrimMediaActivity.this, Utils.s2ms(mOb.nStart), Utils.s2ms(mOb.nEnd));
                        Intent i = new Intent();
                        i.putExtra(SdkEntry.TRIM_START_TIME, mOb.nStart);
                        i.putExtra(SdkEntry.TRIM_END_TIME, mOb.nEnd);
                        i.putExtra(SdkEntry.TRIM_MEDIA_PATH, mMediaObject.getMediaPath());
                        if (mIsSquare) {
                            int left = (int) (mMediaObject.getWidth() * ((float) mHsvPlayer
                                    .getScrollX() / mPflVideoPreview
                                    .getWidth()));
                            int right = (int) (mMediaObject.getWidth()
                                    * ((float) mHsvPlayer.getScrollX() + mPlaybackWidth) / mPflVideoPreview
                                    .getWidth());
                            int top = (int) (mMediaObject.getHeight() * ((float) mSvPlayer
                                    .getScrollY() / mPflVideoPreview
                                    .getHeight()));
                            int bottom = (int) (mMediaObject.getHeight()
                                    * ((float) mSvPlayer.getScrollY() + mPlaybackWidth) / mPflVideoPreview
                                    .getHeight());
                            Rect rcCrop = new Rect(left, top, right, bottom);
                            i.putExtra(SdkEntry.TRIM_CROP_RECT, rcCrop);
                        }
                        TrimMediaActivity.this.setResult(RESULT_OK, i);
                        TrimMediaActivity.this.finish();
                    }
                }
            }
        }
    };

    private boolean mHWCodecEnabled = CoreUtils.hasJELLY_BEAN_MR2();

    private void exportVideo() {

//        Log.e(TAG, "exportVideo: " + cvCropView.getCrop().toShortString() + "...." + cvCropView.getPhoto().toShortString());
        VideoConfig vc = new VideoConfig();
        if (bFromMix) {
            if (!bOnlyLine) {
                RectF clip = cvCropView.getCrop();
                mMediaObject.setClipRectF(new RectF(clip.left, clip.top, clip.right, clip.bottom));
                mMediaObject.setShowRectF(null);
            }
        } else {
            float aspClip = 1f;
            if (mIsSquare) {
                int left = (int) (mMediaObject.getWidth() * ((float) mHsvPlayer
                        .getScrollX() / mPflVideoPreview.getWidth()));
                int right = (int) (mMediaObject.getWidth()
                        * ((float) mHsvPlayer.getScrollX() + mPlaybackWidth) / mPflVideoPreview
                        .getWidth());
                int top = (int) (mMediaObject.getHeight() * ((float) mSvPlayer
                        .getScrollY() / mPflVideoPreview.getHeight()));
                int bottom = (int) (mMediaObject.getHeight()
                        * ((float) mSvPlayer.getScrollY() + mPlaybackWidth) / mPflVideoPreview
                        .getHeight());

                mMediaObject.setClipRectF(new RectF(left, top, right, bottom));
                mMediaObject.setShowRectF(null);

                RectF clip = mMediaObject.getClipRectF();
                aspClip = clip.width() / clip.height();

            } else {
                aspClip = mMediaObject.getWidth() / (mMediaObject.getHeight() + 0.0f);
            }
            vc.setAspectRatio(mTrimConfig.getVideoMaxWH(), aspClip);
        }
        mMediaObject.setTimeRange(mOb.nStart, mOb.nEnd);

        mVideoSave = new VirtualVideo();
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMediaObject);
        mVideoSave.addScene(scene);


        VideoConfig trimVideoInfo = new VideoConfig();
        if (VirtualVideo.getMediaInfo(mMediaObject.getMediaPath(), trimVideoInfo, true) > 0) {
            vc.setVideoFrameRate(Math.max(10, Math.min(trimVideoInfo.getVideoFrameRate(), 30)));
            if (trimVideoInfo.getVideoEncodingBitRate() <= 0) {
                vc.setVideoEncodingBitRate(
                        SdkEntry.getSdkService().getTrimConfig().getVideoBitratebps());
            } else {
                vc.setVideoEncodingBitRate(Math.min(trimVideoInfo.getVideoEncodingBitRate(),
                        SdkEntry.getSdkService().getTrimConfig().getVideoBitratebps()));
            }
        } else {
            vc.setVideoEncodingBitRate(SdkEntry.getSdkService().getTrimConfig().getVideoBitratebps());
        }

        if (bFromMix) {
            //新版画中画截取时生成临时文件
            mStrSaveMp4FileName = PathUtils.getTempFileNameForSdcard(
                    PathUtils.TEMP_MIX, "mp4");
        } else {
            if (mSavePath != null || !TextUtils.isEmpty(mSavePath)) {
                File path = new File(mSavePath);
                PathUtils.checkPath(path);
                mStrSaveMp4FileName = PathUtils.getTempFileNameForSdcard(mSavePath,
                        "VIDEO", "mp4");
            } else {
                mStrSaveMp4FileName = PathUtils.getMp4FileNameForSdcard();
            }
        }

        vc.enableHWEncoder(mHWCodecEnabled);
        vc.enableHWDecoder(mHWCodecEnabled);
        if (bFromMix) {
            RectF outSize = ModeUtils.getFixOutSize((int) cvCropView.getCrop().width(), (int) cvCropView.getCrop().height());
            vc.setVideoSize((int) outSize.width(), (int) outSize.height());
            vc.setAspectRatio(outSize.width() / outSize.height());
        }
        mVideoSave.export(TrimMediaActivity.this, mStrSaveMp4FileName, vc, mListenerSave);
    }

}
