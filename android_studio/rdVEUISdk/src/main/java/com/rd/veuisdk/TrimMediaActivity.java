package com.rd.veuisdk;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.adapter.FilterLookupAdapter;
import com.rd.veuisdk.crop.CropView;
import com.rd.veuisdk.fragment.helper.FilterLookupLocalHandler;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.manager.TrimConfiguration;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.HorizontalScrollViewEx;
import com.rd.veuisdk.ui.ProgressView;
import com.rd.veuisdk.ui.VideoThumbNailAlterView;
import com.rd.veuisdk.ui.extrangseekbar.ExtRangeSeekbarPlus;
import com.rd.veuisdk.ui.extrangseekbar.ExtRangeSeekbarPlus.onRangDurationListener;
import com.rd.veuisdk.ui.extrangseekbar.RangSeekBarBase;
import com.rd.veuisdk.ui.extrangseekbar.VideoTrimFixedView;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.FileLog;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;


/**
 * 截取
 *
 * @author JIAN
 */
@SuppressLint("HandlerLeak")
public class TrimMediaActivity extends BaseActivity {
    private final String TAG = "TrimMediaActivity";

    private final int MIN_THUMB_DURATION = 1000;
    private int TRIM_LONG = 4;
    private int TRIM_SHOT = 2;
    private int TRIM_SINGLE = 0;
    private final String PARAM_IS_FIRST = "param_is_first";

    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvRemainDuration;
    private TextView mTvFrontTime;
    private TextView mTvBehindTime;
    private RelativeLayout mRlTitleBar;
    private ImageView mIvVideoPlayState;
    private boolean mHasChanged = false;
    private VirtualVideoView mMediaPlayer;
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
    private float mSingleFixText = 0;
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
    private ScrollView mSvPlayer;
    private HorizontalScrollViewEx mHsvPlayer;
    private boolean mIsLandVideo;

    private boolean mEnableSquare = true;

    private int mTrimReturnType;

    private TrimConfiguration mTrimConfig;

    private int mTrimType;

    private boolean mTrimFromEdit = false;


    static final int RESULT_AE_REPLACE = -20;

    //直接返回数据(画中画直接返回trim之后的视频路径)
    private static final String RESULT_DATA = "result_data";
    //仅裁剪时间轴
    public static final String CROP_ASPECTRATIO = "crop_aspectratio";
    public static final String PARAM_FROM_AE = "param_from_ae";
    public static final String PARAM_ROTATE = "param_rotate";
    public static final String PARAM_SINGLEFIXTRIM_DURATION = "param_singlefixtrim_duration";
    //bFromMix  true, 画中画裁剪，预览时，视频内容完全显示，裁剪指定比例的视频内容
    private boolean bFromMix = false;
    private boolean bOnlyLine = true;
    private CropView cvCropView;
    private float cropAspRatio = 1f;
    //ae模式显示时，完整显示视频，需要锁定比例动态裁剪
    private boolean bFromAE = false;

    /**
     * @param context
     * @param scene
     * @param fromEdit
     * @param maxTrim
     * @param cropAspRatio
     * @param requestCode
     */
    static void onAETrim(Context context, Scene scene, boolean fromEdit, float maxTrim, float cropAspRatio, int requestCode) {
        // 视频 截取
        Intent intent = new Intent(context, TrimMediaActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.TRIM_FROM_EDIT, fromEdit);
        intent.putExtra(TrimMediaActivity.CROP_ASPECTRATIO, cropAspRatio);
        intent.putExtra(TrimMediaActivity.PARAM_FROM_AE, true);
        intent.putExtra(TrimMediaActivity.PARAM_SINGLEFIXTRIM_DURATION, maxTrim);
        ((Activity) context).startActivityForResult(intent, requestCode);

    }

    //是否显示旋转按钮 (只有相册导入时才为true)
    private boolean bRotateVisible = false;

    /**
     * 相册导入时，截取视频
     *
     * @param context
     * @param mediaObject
     * @param requestCode
     */
    static void onImportTrim(Context context, MediaObject mediaObject, int requestCode) {
        Intent intent = new Intent(context, TrimMediaActivity.class);
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mediaObject);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.TRIM_FROM_EDIT, true);
        intent.putExtra(TrimMediaActivity.PARAM_FROM_AE, false);
        intent.putExtra(TrimMediaActivity.PARAM_ROTATE, true);
        ((Activity) context).startActivityForResult(intent, requestCode);


    }

    private int rotateAngle = 0;
    private ArrayList<EffectInfo> mEffectInfos;
    private ViewGroup menuTrim, menuTrimAE;

    private boolean bNeedLoadCover = true;
    private TextView mBtnVolume, mBtnFilter;
    //音量
    private LinearLayout mLlVolume;
    private SeekBar mSbVolume;
    //滤镜
    private LinearLayout mLlFilter;
    private RecyclerView mRvFilter;
    private FilterLookupAdapter mAdapter;
    private SeekBar mSbStrength;
    private TextView mTvFilter;
    private int tmpIndex = 0;
    private int lastItemId = 0;
    private int mLastPageIndex = 0;
    protected VisualFilterConfig tmpLookup = null;
    private VideoTrimFixedView mVtfvFixed;
    /**
     * 默认的锐度
     */
    protected float mDefaultValue = Float.NaN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent in = getIntent();
        bFromAE = in.getBooleanExtra(TrimMediaActivity.PARAM_FROM_AE, false);
        bRotateVisible = in.getBooleanExtra(TrimMediaActivity.PARAM_ROTATE, false);
        if (bFromAE) {
            mTrimFromEdit = true;
            bFromMix = true;
            bOnlyLine = false;
        } else {
            mTrimFromEdit = in.getBooleanExtra(IntentConstants.TRIM_FROM_EDIT, false);
            bFromMix = in.getBooleanExtra(RESULT_DATA, false);
        }

        cropAspRatio = in.getFloatExtra(CROP_ASPECTRATIO, 1f);
        mStrActivityPageName = getString(R.string.preview_trim);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_meida);
        registerReceiver(mReceiver, new IntentFilter(SdkEntry.TRIM_RETURN));

        mVtfvFixed = $(R.id.vtfv_fixed);
        mLlVolume = $(R.id.ll_factor);
        mSbVolume = $(R.id.sbFactor);
        menuTrim = $(R.id.menuTrim);
        menuTrimAE = $(R.id.menuTrimAE);
        menuTrim.setVisibility(bFromAE ? View.GONE : View.VISIBLE);
        menuTrimAE.setVisibility(bFromAE ? View.VISIBLE : View.GONE);

        bNeedLoadCover = true;

        mTrimConfig = SdkEntry.getSdkService().getTrimConfig();
        mPlaybackWidth = CoreUtils.getMetrics().widthPixels;

        mEnableSquare = mTrimConfig.enable1x1;
        mTrimReturnType = mTrimConfig.trimReturnMode;

        mShotText = mTrimConfig.trimDuration1;
        mLongText = mTrimConfig.trimDuration2;
        if (bFromAE) {
            mSingleFixText = in.getFloatExtra(PARAM_SINGLEFIXTRIM_DURATION, 1f);
        } else {
            mSingleFixText = mTrimConfig.trimSingleFixDuration;
            mTrimType = mTrimConfig.trimType;
        }
        mScene = in.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (mScene == null) {
            Log.w(TAG, "Trim media object not exists!");
            finish();
            return;
        }
        mMediaObject = mScene.getAllMedia().get(0);

        if (bFromAE) {
            float duration = VirtualVideo.getMediaInfo(mMediaObject.getMediaPath(), null);
            if (duration > 0) {
                mSingleFixText = Math.min(duration, mSingleFixText);
            }
        }


        TRIM_SHOT = mShotText * 1000;
        TRIM_LONG = mLongText * 1000;
        TRIM_SINGLE = (int) (mSingleFixText * 1000);

        if (mTrimFromEdit) {
            mEnableSquare = false;
            mTrimType = TrimConfiguration.TRIM_TYPE_FREE;
        }


        mEffectInfos = new ArrayList<>();
        if (null != mMediaObject.getEffectInfos()) {
            mEffectInfos.addAll(mMediaObject.getEffectInfos());
        }
        mMediaObject.setEffectInfos(null);

//        Log.e(TAG, "onCreate: " + mMediaObject.getWidth() + "*" + mMediaObject.getHeight() + "   " +
//                "" + mMediaObject.getAngle() + "  " + mMediaObject.getClipRectF() + "   " + mMediaObject.getShowRectF());
        mOb = (VideoOb) mMediaObject.getTag();
//        Log.e(TAG, "onCreate: " + mOb);
        mMediaRatio = (float) mMediaObject.getWidth() / mMediaObject.getHeight();

        if (mMediaRatio >= 1.0) {
            mIsLandVideo = true;
        } else {
            mIsLandVideo = false;
        }
        if (bFromAE) {
            mTrimType = TrimConfiguration.TRIM_TYPE_SINGLE_FIXED;
            bFromMix = true;
        } else {
            if (bFromMix) {//画中画裁剪，固定显示方式
                mIsLandVideo = true;
            }
        }

        rotateAngle = mMediaObject.getAngle();

        //AE
        if (bFromAE) {
            //截取滑动控件
            mVtfvFixed.setVisibility(View.VISIBLE);
            mVtfvFixed.setListener(new VideoTrimFixedView.OnChangeListener() {
                @Override
                public void OnChanged(long start, long end) {
                    prepareMedia(start, end);
                }

                @Override
                public void OnPause() {
                    pauseVideo();
                }

                @Override
                public void OnSeek(long time) {
                    seekTo(time);
                }
            });
            mBtnVolume = $(R.id.tvVolume);
            mBtnFilter = $(R.id.tvFilter);
            //滤镜
            mLlFilter = $(R.id.llFilter);
            mRvFilter = $(R.id.recyclerViewFilter);
            mSbStrength = $(R.id.sbarStrength);
            mTvFilter = $(R.id.tvFilterValue);
            $(R.id.strengthLayout).setVisibility(View.VISIBLE);
            mSbStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mDefaultValue = progress / 100.0f;
                    mTvFilter.setText(progress + "%");
                    if (fromUser && tmpLookup != null) {
                        tmpLookup.setDefaultValue(mDefaultValue);
                        try {
                            if (buildMedia != null) {
                                buildMedia.changeFilter(tmpLookup);
                            }
                            if (null != mMediaObject) {
                                mMediaObject.changeFilter(tmpLookup);
                            }
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mBtnVolume.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //onMediaVolumeClick();
                    mLlFilter.setVisibility(View.GONE);
                    mLlVolume.setVisibility(View.VISIBLE);
                    mBtnVolume.setTextColor(getResources().getColor(R.color.main_orange));
                    mBtnFilter.setTextColor(getResources().getColor(R.color.white));
                }
            });
            mBtnVolume.setVisibility(View.VISIBLE);
            mBtnFilter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLlFilter.setVisibility(View.VISIBLE);
                    mLlVolume.setVisibility(View.GONE);
                    mBtnVolume.setTextColor(getResources().getColor(R.color.white));
                    mBtnFilter.setTextColor(getResources().getColor(R.color.main_orange));
                }
            });
            mLlFilter.setVisibility(View.VISIBLE);
            mBtnFilter.setVisibility(View.VISIBLE);
            mBtnVolume.setTextColor(getResources().getColor(R.color.white));
            mBtnFilter.setTextColor(getResources().getColor(R.color.main_orange));

            ArrayList<WebFilterInfo> infos = (new FilterLookupLocalHandler(this)).getArrayList();
            mSbStrength.setMax(100);
            if (mMediaObject != null && mMediaObject.getFilterList() != null && mMediaObject.getFilterList().size() > 0) {
                tmpLookup = mMediaObject.getFilterList().get(0);
                int value = Float.isNaN(tmpLookup.getSharpen()) ? 100 : (int) (tmpLookup.getSharpen() * 100);
                mSbStrength.setProgress(value);
                if (!TextUtils.isEmpty(tmpLookup.getFilterFilePath())) {
                    for (int i = 0; i < infos.size(); i++) {
                        if (tmpLookup.getFilterFilePath().equals(infos.get(i).getLocalPath())) {
                            mLastPageIndex = i;
                            tmpIndex = i;
                            break;
                        }
                    }
                }
            } else {
                mSbStrength.setProgress(100);
            }

            mRvFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            //设置添加或删除item时的动画，这里使用默认动画
            mRvFilter.setItemAnimator(new DefaultItemAnimator());
            mAdapter = new FilterLookupAdapter(this);
            mAdapter.addAll(true, infos, mLastPageIndex);
            mAdapter.setEnableRepeatClick(true);
            mAdapter.setOnItemClickListener(new OnItemClickListener<Object>() {
                @Override
                public void onItemClick(int position, Object item) {
                    onSelectedImp(position);
                    mSbStrength.setEnabled(position > 0);
                }
            });
            mSbStrength.setEnabled(tmpIndex > 0);
            //设置适配器
            mRvFilter.setAdapter(mAdapter);
            //音量
            mSbVolume.setMax(100);
            mSbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (buildMedia != null && fromUser) {
                        buildMedia.setMixFactor(progress);
                    }
                    if (null != mMediaObject && fromUser) {
                        mMediaObject.setMixFactor(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        initView();
        loadVideo();
    }

    private void loadVideo() {
        Intent in = getIntent();
        mIsShowLight = in.getBooleanExtra(PARAM_IS_FIRST, true);
        if (mOb == null) {
            mOb = new VideoOb(mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), mMediaObject.getTrimStart(),
                    mMediaObject.getTrimEnd(), 0, null, VideoOb.DEFAULT_CROP);
        }
        mMediaObject.setTimeRange(mOb.TStart, mOb.TEnd);
        virtualVideo = new VirtualVideo();
        initPlayerData();
    }

    private ExtRangeSeekbarPlus mRangeSeekBar;
    private View btnRotate;
    private VirtualVideo virtualVideo;
    private MediaObject buildMedia;

    private void initPlayerData() {
        mMediaPlayer.reset();
        virtualVideo.reset();
        Scene scene = VirtualVideo.createScene();
        buildMedia = mMediaObject.clone();
        buildMedia.setClipRectF(null); //清理之前的裁剪区域，完全显示 （否则会越裁越小）
        buildMedia.setShowRectF(null);
        buildMedia.setAngle(rotateAngle);
        buildMedia.setClipRect(null);
        buildMedia.setTimeRange(0, buildMedia.getIntrinsicDuration());
        scene.addMedia(buildMedia);
        virtualVideo.addScene(scene);
        mMediaPlayer.setAutoRepeat(true);
        try {
            virtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }

    }

    private void onRotateClick() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        rotateAngle += 90;
        SysAlertDialog.showLoadingDialog(TrimMediaActivity.this, R.string.isloading);
        initPlayerData();
    }

    private void initView() {
        btnRotate = $(R.id.btnRotate);
        if (bRotateVisible) {
            btnRotate.setVisibility(View.VISIBLE);
            btnRotate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRotateClick();
                }
            });
        }
        mSavePath = mTrimConfig.savePath;
        String strTitle = mTrimConfig.title;
        int titleBarColor = mTrimConfig.titleBarColor;
        String cancelText = mTrimConfig.buttonCancelText;
        String confirmText = mTrimConfig.buttonConfirmText;
        int buttonColor = mTrimConfig.buttonColor;

        mSvPlayer = $(R.id.svPlayer);
        mHsvPlayer = $(R.id.hsvPlayer);

        mRlAddTime = $(R.id.rlAddTime);
        mTvAddtime = $(R.id.tvAddTime);
        mTvOldTime = $(R.id.tvOldTime);

        if (mIsLandVideo) {
            mSvPlayer.setVisibility(View.GONE);
            mHsvPlayer.setVisibility(View.VISIBLE);
            mProgressView = $(R.id.progressViewHori);
            //外层框
            mPflVideoPreview = $(R.id.rlPreviewHori);
            //播放器的框
            mPreviewPlayer = $(R.id.rlPreview_playerHori);
            mMediaPlayer = $(R.id.epvPreviewHori);
            cvCropView = $(R.id.cvVideoCropHori);
        } else {
            mSvPlayer.setVisibility(View.VISIBLE);
            mHsvPlayer.setVisibility(View.GONE);
            mPflVideoPreview = $(R.id.rlPreview);
            mPreviewPlayer = $(R.id.rlPreview_player);
            mMediaPlayer = $(R.id.epvPreview);
            cvCropView = $(R.id.cvVideoCrop);
            mProgressView = $(R.id.progressView);
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
                        playOrPause();
                    }

                    @Override
                    public void onMove() {
                    }
                });
            }
        } else {
            cvCropView.setVisibility(View.GONE);
        }

        mHsvPlayer.enableScroll(false);
        mRlTitleBar = $(R.id.rlTitleBar);

        LinearLayout.LayoutParams rlVideolp = new LinearLayout.LayoutParams(
                mPlaybackWidth, mPlaybackWidth);
        mPflVideoPreview.setLayoutParams(rlVideolp);
        mPflVideoPreview.setAspectRatio(1);
        if (bFromAE) {
            mPreviewPlayer.setAspectRatio(mMediaRatio);
            mMediaPlayer.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
        }
        mMediaPlayer.setBackgroundColor(getResources().getColor(R.color.black));
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.preview_trim);
        if (strTitle != null) {
            ((TextView) $(R.id.tvTitle)).setText(strTitle);
            ((TextView) $(R.id.tvBottomTitle)).setText(strTitle);
        }
        if (titleBarColor != 0) {
            mRlTitleBar.setBackgroundColor(titleBarColor);
        }
        if (buttonColor != 0) {
            $(R.id.public_menu_sure).setBackgroundColor(buttonColor);
            $(R.id.public_menu_cancel).setBackgroundColor(buttonColor);
        }
        if (confirmText != null) {
            ExtButton ebtnSure = $(R.id.ebtnSure);
            ebtnSure.setText(confirmText);
            ebtnSure.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (cancelText != null) {
            ExtButton ebtnCancel = $(R.id.ebtnCancel);
            ebtnCancel.setText(cancelText);
            ebtnCancel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        // 切换裁切比例
        mCbSquare = $(R.id.cbTrim1x1);
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

        mMenuGroup = $(R.id.trim_menu_group);
        mTvSingleFixedTime = $(R.id.tv_single_fixed_time);
        mRbShotTime = $(R.id.trim_shot);
        mRbLongTime = $(R.id.trim_long);

        mThumbNailView = $(R.id.split_videoview);
        mRangeSeekBar = $(R.id.m_extRangeSeekBar);

        if (isFromAEPreview()) {
            mMenuGroup.setVisibility(View.INVISIBLE);
        } else {
            if (mTrimType == TrimConfiguration.TRIM_TYPE_SINGLE_FIXED) {
                mMenuGroup.setVisibility(View.INVISIBLE);
                mTvSingleFixedTime.setVisibility(View.VISIBLE);
                mTvSingleFixedTime.setText(getString(R.string.duration_fix_format_f, mSingleFixText));
            } else if (mTrimType == TrimConfiguration.TRIM_TYPE_DOUBLE_FIXED) {
                mMenuGroup.setVisibility(View.VISIBLE);
                mRbShotTime.setText(getString(R.string.duration_fix_format_int, mShotText));
                mRbLongTime.setText(getString(R.string.duration_fix_format_int, mLongText));
                mTvSingleFixedTime.setVisibility(View.INVISIBLE);
            } else {
                mMenuGroup.setVisibility(View.INVISIBLE);
            }
        }

        // 不将边缘变成灰色
        mRangeSeekBar.setHorizontalFadingEdgeEnabled(false);


        if (mTrimType == TrimConfiguration.TRIM_TYPE_FREE) {
            mRangeSeekBar.setMoveMode(true);  //自由截取把手
            mRangeSeekBar.setOnRangSeekBarChangeListener(mRangeSeekBarChangeListener);
        } else {
            mRangeSeekBar.setMoveMode(false);  //定长截取框
            mRangeSeekBar.setItemVideo(mOnVideoTrimListener);
            mMenuGroup.setOnCheckedChangeListener(mOnMenuGroupListener);
        }

        mPreviewPlayer.setOnClickListener(mOnPlayListener);

        mIvVideoPlayState = $(R.id.ivPlayerState);
        mIvVideoPlayState.setOnClickListener(mOnPlayListener);

        mTvFrontTime = $(R.id.tvInterceptFrontTime);
        mTvBehindTime = $(R.id.tvInterceptBehindTime);
        mTvRemainDuration = $(R.id.tvRemainDuration);

        mMediaPlayer.setOnPlaybackListener(mPlayViewListener);
        mMediaPlayer.setOnInfoListener(mPlayerInfoListener);

    }

    /**
     * 来自AE模板预览页（AEPreivewActiviity）
     *
     * @return
     */
    private boolean isFromAEPreview() {
        return bFromAE && mTrimFromEdit;
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
//            mRlTitleBar.setVisibility(View.VISIBLE);
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
                mRbShotTime.setBackgroundResource(R.drawable.menu_item_checked);
            } else { // 长
                if (null != mOb) {
                    int viewDuration = (int) (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed());
                    int nduration = viewDuration < TRIM_LONG ? viewDuration
                            : TRIM_LONG;
                    mRangeSeekBar.setItemDuration(nduration);
                }
                mRbShotTime.setBackgroundResource(0);
                mRbLongTime.setBackgroundResource(R.drawable.menu_item_checked);
            }
            doPrepareTrim();
        }
    };

    private void doPrepareTrim() {
        long nstart = mRangeSeekBar.getSelectedMinValue(), nend = mRangeSeekBar.getSelectedMaxValue();
        mOnVideoTrimListener.onItemVideoChanged(nstart, nend);
    }

    private final int WHAT_PLAYER_PREPARED = 106;
    private final int WHAT_CANCEL_EXPORT = 7;

    private Dialog mCancelLoading;

    /**
     * 准备缩略图
     */
    private void prepareThumb(MediaObject thumbMedia) {
        //专用于封面的对象
        float asp = (mMediaPlayer.getVideoWidth() + 0.0f) / mMediaPlayer.getVideoHeight();
        VirtualVideo vitualVideo = new VirtualVideo();
        Scene scene = VirtualVideo.createScene();
        if (bFromAE) {
            MediaObject object = mMediaObject.clone();
            object.setClipRect(null);
            object.setTimeRange(0, object.getIntrinsicDuration());
            scene.addMedia(object);
            vitualVideo.addScene(scene);
            if (mOb != null) {
                mVtfvFixed.setVirtualVideo(asp, vitualVideo, Utils.s2ms(mSingleFixText), Utils.s2ms(mOb.TStart));
            } else {
                mVtfvFixed.setVirtualVideo(asp, vitualVideo, Utils.s2ms(mSingleFixText));
            }
            mVtfvFixed.setStartThumb();
        } else {
            scene.addMedia(thumbMedia);
            vitualVideo.addScene(scene);
            mThumbNailView.recycle();
            mThumbNailView.setVirtualVideo(asp, vitualVideo);
            mThumbNailView.setStartThumb();
        }
        if (null != mOb) {
            int viewDuration = (int) (Utils.s2ms(mOb.TEnd - mOb.TStart) / mMediaObject.getSpeed());
            int nduration;
            if (mTrimType == TrimConfiguration.TRIM_TYPE_SINGLE_FIXED) {
                nduration = viewDuration < TRIM_SINGLE ? viewDuration : TRIM_SINGLE;
                mRangeSeekBar.setItemDuration(nduration);
            } else if (mTrimType == TrimConfiguration.TRIM_TYPE_DOUBLE_FIXED) {
                nduration = viewDuration < TRIM_SHOT ? viewDuration : TRIM_SHOT;
                mRangeSeekBar.setItemDuration(nduration);
            }
        }
        if (!isFromAEPreview()) {
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
        }
        if (bFromAE) {
            mThumbNailView.setVisibility(View.GONE);
            mRangeSeekBar.setVisibility(View.GONE);
        } else {
            mThumbNailView.setVisibility(View.VISIBLE);
            mRangeSeekBar.setVisibility(View.VISIBLE);
        }
        mMediaPlayer.seekTo((mOb.nStart + 0.25f));//防止预览缩略图黑屏
        doPrepareTrim();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case WHAT_PLAYER_PREPARED: {
                    prepareThumb((MediaObject) msg.obj);
                }
                break;
                case WHAT_CANCEL_EXPORT: {
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
                }
                break;
                default:
                    break;
            }

        }

        ;
    };

    private boolean mIsShowLight = true;

    private VirtualVideo.OnInfoListener mPlayerInfoListener = new VirtualVideo.OnInfoListener() {

        @Override
        public boolean onInfo(int what, int extra, Object obj) {
            if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
            } else if (what == VirtualVideo.INFO_WHAT_GET_VIDEO_HIGHTLIGHTS && obj != null) {
                int[] arrHightLights = (int[]) obj;
                // hightlight时间数组，单位为ms
                if (mIsShowLight) {
                    mRangeSeekBar.setHighLights(arrHightLights);
                    mIsShowLight = false;
                }
            }
            return false;
        }

    };
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

                mIsFirst = false;
            }

            if (buildMedia != null) {
                mSbVolume.setProgress(buildMedia.getMixFactor());
            }
            if (!bFromAE) {
                updatePreviewFrameAspect(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            }
//            Log.e(TAG, "onPlayerPrepared: " + mOb);
            if (mOb != null) {
                //原始视频的duration，不受trim 和speed的影响
                float srcVideoDuration = mMediaObject.getIntrinsicDuration();
                int viewDuration = (int) (Utils.s2ms(srcVideoDuration / mMediaObject.getSpeed()));

                mRangeSeekBar.setDuration(viewDuration);
                mRangeSeekBar.setSeekBarRangeValues(Utils.s2ms(mOb.nStart), Utils.s2ms(mOb.nEnd));
                mRangeSeekBar.setProgress(Utils.s2ms(mOb.nStart));
                mProgressView.setDuration(viewDuration);
                mProgressView.setProgress(Utils.s2ms(mOb.nStart));
                onTrimText(Utils.s2ms(mOb.nStart), Utils.s2ms(mOb.nEnd));
            }

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

            }
            if (!bOnlyLine) {
                cvCropView.initialize(mRectVideoClipBound, videoBound, 0);
                cvCropView.applyAspect(1, 1 / cropAspRatio);
                if (!bCanScaleBigger) {
                    cvCropView.setLockSize(true);
                }
                cvCropView.setCanMove(true);
            }

            SysAlertDialog.cancelLoadingDialog();
            if (bNeedLoadCover) {
                bNeedLoadCover = false;
                //封面
                MediaObject tmp = mMediaObject.clone();
                tmp.setTimeRange(mOb.TStart, mOb.TEnd);
                mHandler.sendMessage(mHandler.obtainMessage(WHAT_PLAYER_PREPARED, tmp));
            }

        }

        private boolean bCanScaleBigger = true;

        @Override
        public boolean onPlayerError(int what, int extra) {
            Log.e(TAG, "Player error:" + what + "," + extra);
            SysAlertDialog.cancelLoadingDialog();
            SysAlertDialog.showAlertDialog(TrimMediaActivity.this,
                    "",
                    getString(R.string.preview_error),
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
            int nPosition = Utils.s2ms(position);
            if (mHasChanged) {
                mRangeSeekBar.setProgress(nPosition);
                mProgressView.setProgress(nPosition);
            } else {
                mRangeSeekBar.setProgress(nPosition);
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
                //mMediaPlayer.pause();
            }
            if (bFromAE) {
                mVtfvFixed.setProgress(nPosition);
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
            if (null != mMediaPlayer) {
                mMediaPlayer.seekTo(Utils.ms2s(np));
            }
            mProgressView.setProgress(np);
            mLastProgress = np;
        }

    }

    private String gettime(int progress) {
        progress = Math.max(0, progress);
        return DateTimeUtils.stringForMillisecondTime(progress, true, true);
    }

    private String gettime(long progress) {
        return gettime((int) progress);
    }

    /**
     * 更新预览视频播放器比例
     */
    private void updatePreviewFrameAspect(int nVideoWidth, int nVideoHeight) {
        mPreviewPlayer.setAspectRatio(nVideoWidth / (nVideoHeight + 0.0f));
    }

    private PreviewFrameLayout mPreviewPlayer;
    private VirtualVideo mVideoSave;

    @Override
    public void onBackPressed() {
        finish();
    }

    private String mStrSaveMp4FileName;


    @Override
    public void clickView(View v) {
        super.clickView(v);
        int id = v.getId();

        if (id == R.id.public_menu_sure || id == R.id.ebtnSure || id == R.id.btnRight
                || id == R.id.ivSure) {
            if (mTrimFromEdit) {
                if (mOb != null) {
                    mMediaObject.setTimeRange(mOb.nStart, mOb.nEnd);
                }
                Intent data = new Intent();
                //修正特效有效时间线
                EffectManager.fixEffect(mMediaObject, mEffectInfos);
                if (bFromAE) {
                    mMediaObject.setClipRectF(new RectF(cvCropView.getCrop()));
                }
                mMediaObject.setAngle(rotateAngle);
                Scene scene = new Scene();
                scene.addMedia(mMediaObject);
                data.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
                data.putExtra(PARAM_IS_FIRST, false);
                setResult(RESULT_OK, data);
                onBackPressed();
            } else {
                //媒体的截取时间线
                float speed = mMediaObject.getSpeed();
                float min = Utils.ms2s(mRangeSeekBar.getSelectedMinValue());
                float max = Utils.ms2s(mRangeSeekBar.getSelectedMaxValue());
                mMediaObject.setTimeRange(min * speed, max * speed);
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
        } else if (id == R.id.public_menu_cancel || id == R.id.ebtnCancel || id == R.id.btnLeft
                || id == R.id.ivCancel) {
            setResult(RESULT_CANCELED);
            onBackPressed();
        }
    }

    private ExportListener mListenerSave = new ExportListener() {


        private HorizontalProgressDialog hpdSave;

        @Override
        public void onExportStart() {
            if (hpdSave == null) {
                String str = getString(R.string.exporting);
                hpdSave = SysAlertDialog.showHoriProgressDialog(
                        TrimMediaActivity.this, str,
                        false, true, new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mVideoSave.cancelExport();
                                hpdSave = null;
                                mHandler.sendEmptyMessage(WHAT_CANCEL_EXPORT);
                            }
                        });
                hpdSave.setCanceledOnTouchOutside(false);
                hpdSave.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                    @Override
                    public void onCancel() {
                        SysAlertDialog.showAlertDialog(
                                TrimMediaActivity.this,
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
                                        if (null != hpdSave) {
                                            hpdSave.cancel();
                                            hpdSave.dismiss();
                                        }
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
                    onToast(strMessage);
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
        super.onPause();
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
        if (null != mThumbNailView) {
            mThumbNailView.recycle();
            mThumbNailView = null;
        }
        mVtfvFixed.recycle();
        mVtfvFixed = null;
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
                mOb.nStart = Utils.ms2s(minValue);
                mOb.nEnd = Utils.ms2s(maxValue);

                float speed = mMediaObject.getSpeed();

                mOb.rStart = Utils.ms2s((int) (minValue * speed));
                mOb.rEnd = Utils.ms2s((int) (maxValue * speed));

                mOb.TStart = Utils.ms2s((int) (minValue * speed));
                mOb.TEnd = Utils.ms2s((int) (maxValue * speed));

                onTrimText(minValue, maxValue);

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
            onTrimText(start, end);
            if (null != mMediaObject) {
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

    };

    /**
     * 滑动把手时需设置对应的时间文本
     *
     * @param min
     * @param max
     */
    private void onTrimText(long min, long max) {
        mTvFrontTime.setText(gettime(min));
        mTvBehindTime.setText(gettime(max));
        mTvRemainDuration.setText(gettime(Math.max(MIN_THUMB_DURATION, max - min)));
    }

    private boolean mRightHandleChanged;

    private ProgressView.onProgressListener mOnProListener = new ProgressView.onProgressListener() {

        boolean isPlaying = false;

        @Override
        public void onStart() {
            isPlaying = mMediaPlayer.isPlaying();
            if (isPlaying) {
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
                long min = mRangeSeekBar.getSelectedMinValue();
                long max = mRangeSeekBar.getSelectedMaxValue();
                if (null != mMediaObject) {
                    if (null != mOb) {
                        mOb.nStart = Utils.ms2s(min);
                        mOb.nEnd = Utils.ms2s(max);
                        mOb.rEnd = mOb.nEnd * mMediaObject.getSpeed();
                        mOb.rStart = mOb.nStart * mMediaObject.getSpeed();
                    }
                }

                onTrimText(min, max);
            }
        }

        @Override
        public void onClick() {
            playOrPause();
        }

        @Override
        public void onSeekbarChanging(int nscale) {
            int scale = nscale;
            long min = mRangeSeekBar.getSelectedMinValue();
            long max = mRangeSeekBar.getSelectedMaxValue();
            if (mRangeSeekBar.mCurHandle == mRangeSeekBar.HANDLE_LEFT) {
                if (mOnChange) {
                    mOnChange = false;
                    mProgressView.setShowTime(false);
                    mOldSeekbarTime = (int) min;
                }

                mRlAddTime.setVisibility(View.VISIBLE);
                mTvOldTime.setText(gettime(mOldSeekbarTime));

                int np = Utils.s2ms(mOb.nStart) + scale;
                if (np < 0) {
                    np = 0;
                    scale = 0 - Utils.s2ms(mOb.nStart);
                }
                if (np > max - MIN_THUMB_DURATION) {
                    np = (int) (max - MIN_THUMB_DURATION);
                    scale = (int) (max - MIN_THUMB_DURATION) - Utils.s2ms(mOb.nStart);
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

                onTrimText(min, max);

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

                onTrimText(min, max);
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

        mStrSaveMp4FileName = PathUtils.getDstFilePath(mSavePath);

        vc.enableHWEncoder(mHWCodecEnabled);
        vc.enableHWDecoder(mHWCodecEnabled);
        mVideoSave.export(this, mStrSaveMp4FileName, vc, mListenerSave);
    }

    /**
     * 滤镜点击
     *
     * @param nItemId
     */
    public void onSelectedImp(int nItemId) {
        mLastPageIndex = nItemId;
        if (nItemId >= 1) {
            if (lastItemId != nItemId) {
                switchFliter(nItemId);
                lastItemId = nItemId;
                mAdapter.onItemChecked(nItemId);
            }
        } else {
            lastItemId = nItemId;
            switchFliter(nItemId);
            mAdapter.onItemChecked(lastItemId);
        }
    }

    /**
     * 切换滤镜效果
     *
     * @param index
     */
    private void switchFliter(int index) {
        tmpIndex = index;
        //lookup滤镜
        if (index > 0) {
            WebFilterInfo info = mAdapter.getItem(index);
            if (info != null) {
                tmpLookup = new VisualFilterConfig(info.getLocalPath());
                //滤镜程度
                tmpLookup.setDefaultValue(mDefaultValue);
            } else {
                tmpIndex = 0;
                tmpLookup = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
            }
        } else {
            //第0个 无滤镜效果
            tmpLookup = new VisualFilterConfig(VisualFilterConfig.FILTER_ID_NORMAL);
        }
        try {
            if (buildMedia != null) {
                buildMedia.changeFilter(tmpLookup);
            }
            if (null != mMediaObject) {
                mMediaObject.changeFilter(tmpLookup);
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

}
