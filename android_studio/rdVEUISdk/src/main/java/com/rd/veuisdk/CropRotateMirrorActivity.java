package com.rd.veuisdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.FlipType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.adapter.FilterLookupAdapter;
import com.rd.veuisdk.crop.CropView;
import com.rd.veuisdk.fragment.helper.FilterLookupLocalHandler;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.RCInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;


/**
 * 视频编辑
 *
 * @author abreal
 */
public class CropRotateMirrorActivity extends BaseActivity {
    private VirtualVideoView mMediaPlayer;
    private TextView mTvTitle;
    private CropView mCvCrop;
    private View mTvResetAll;
    private PreviewFrameLayout mPlayout;

    private Scene mScene;
    private MediaObject mMedia;
    private VideoOb mVideoOb;

    private RectF mRectVideoClipBound;

    private RectF mCurDefaultClipBound;
    private int mCurDefaultAngle;
    private double mCurDefaultAspect;
    @VideoOb.CropMode
    private int mCurDefaultCropMode;
    private FlipType mCurDefaultFlipType;

    public static final String PARAM_SHOW_PROPORTION = "show_proportion";
    private static final String PARAM_MEDIA_ASP = "media_asp";
    private static final String PARAM_HIDE_MIRROR = "hide_mirror";
    private static final String PARAM_HIDE_CROPVIEW = "hide_cropview";
    private static final String PARAM_HIDE_MIRROR_MENU = "hide_mirror_menu";
    private static final String PARAM_SHOW_AE_REPLACE = "param_show_ae_replace";
    private static final String PARAM_SHOW_APPLY_ALL = "param_show_apply_all_part";
    private static final String PARAM_CROP_ASP = "param_default_crop_asp";
    private static final String PARAM_TITLE = "title";


    private boolean bShowProportion = true;//显示裁剪比例按钮

    static final int RESULT_AE_REPLACE = -23;

    private boolean mIsPrepared;

    //媒体指定的显示比例
    private float mAsp = -1;
    private View mllRotateMirror;
    private boolean bShowAEReplace = false;
    private CheckBox cbApplyToAll;

    //滤镜
    private RecyclerView mRvFilter;
    private FilterLookupAdapter mAdapter;
    private SeekBar mSbStrength;
    private TextView mTvFilter;
    private int tmpIndex = 0;
    private int lastItemId = 0;
    private int mLastPageIndex = 0;
    protected VisualFilterConfig tmpLookup = null;
    /**
     * 默认的锐度
     */
    protected float mDefaultValue = Float.NaN;

    /**
     * 片段编辑-编辑
     *
     * @param aspCropAsp 片段编辑界面播放器的显示比例
     */
    static void onCropRotate(Context context, Scene scene, float aspCropAsp, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, CropRotateMirrorActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(PARAM_SHOW_APPLY_ALL, true);
        intent.putExtra(PARAM_CROP_ASP, aspCropAsp);
        ((Activity) context).startActivityForResult(intent, requestCode);
        ((Activity) context).overridePendingTransition(0, 0);

    }

    /**
     * @param context
     * @param scene        媒体
     * @param asp          固定裁剪比例
     * @param isHideMirror 是否隐藏镜像布局
     * @param requestCode
     */
    static void onAECropRotateMirror(Context context, Scene scene, float asp, boolean isHideMirror, int requestCode) {
        onAECropRotateMirror(context, scene, asp, isHideMirror, true, requestCode);
    }

    /**
     * @param scene         媒体
     * @param asp           固定裁剪比例
     * @param isHideMirror  是否隐藏镜像布局
     * @param isShowReplace 是否显示替换图片
     */
    public static void onAECropRotateMirror(Context context, Scene scene, float asp, boolean isHideMirror, boolean isShowReplace, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, CropRotateMirrorActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(CropRotateMirrorActivity.PARAM_SHOW_PROPORTION, false);
        intent.putExtra(CropRotateMirrorActivity.PARAM_SHOW_AE_REPLACE, isShowReplace);
        intent.putExtra(CropRotateMirrorActivity.PARAM_MEDIA_ASP, asp);
        intent.putExtra(CropRotateMirrorActivity.PARAM_HIDE_MIRROR, isHideMirror);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 导入图片旋转、锁定比例裁剪
     *
     * @param context
     * @param scene
     * @param requestCode
     */
    static void onImportImage(Context context, Scene scene, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, CropRotateMirrorActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(CropRotateMirrorActivity.PARAM_SHOW_PROPORTION, false);
        intent.putExtra(CropRotateMirrorActivity.PARAM_HIDE_MIRROR, false);
        intent.putExtra(CropRotateMirrorActivity.PARAM_HIDE_MIRROR_MENU, true);
        intent.putExtra(CropRotateMirrorActivity.PARAM_HIDE_CROPVIEW, false);
        intent.putExtra(CropRotateMirrorActivity.PARAM_TITLE, context.getString(R.string.preview_rotate));
        ((Activity) context).startActivityForResult(intent, requestCode);
    }


    /**
     * sdk-视频裁剪功能
     */
    static void onCrop(Context context, Scene scene, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(context, CropRotateMirrorActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.INTENT_NEED_EXPORT, true);
        intent.putExtra(CropRotateMirrorActivity.PARAM_SHOW_PROPORTION, true);
        intent.putExtra(CropRotateMirrorActivity.PARAM_SHOW_AE_REPLACE, false);
        intent.putExtra(CropRotateMirrorActivity.PARAM_HIDE_MIRROR, false);
        intent.putExtra(CropRotateMirrorActivity.PARAM_TITLE, context.getString(R.string.preview_crop));
        if (context instanceof Activity && requestCode > 0) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }

    }

    private boolean hideMirrorMenu = false;
    private boolean bHideCropView = false;
    private String mCustomTitlte;
    private RadioButton mRbOriginal, mRbFree, mRb1x1, mRb169, mRb916;
    private RadioGroup mRGCrop;

    private void onOriginalClick() {
        mIsPrepared = false;
        mRectVideoClipBound.setEmpty();
        changeCropMode(VideoOb.CROP_ORIGINAL);
        setResetClickable(true);
    }

    private void initProportion() {
        mRGCrop = $(R.id.rgCropProportionLine);
        mRbOriginal = $(R.id.rbCropOriginal);
        mRbFree = $(R.id.rbCropFree);
        mRb1x1 = $(R.id.rbProportion1x1);
        mRb169 = $(R.id.rbProportion169);
        mRb916 = $(R.id.rbProportion916);

        mRbOriginal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onOriginalClick();
            }
        });
        mRbFree.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPrepared = false;
                mRectVideoClipBound.setEmpty();
                changeCropMode(VideoOb.CROP_FREE);
            }
        });
        mRb1x1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPrepared = false;
                mRectVideoClipBound.setEmpty();
                changeCropMode(VideoOb.CROP_1);
                setResetClickable(true);
            }
        });
        mRb169.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPrepared = false;
                mRectVideoClipBound.setEmpty();
                changeCropMode(VideoOb.CROP_169);
            }
        });
        mRb916.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsPrepared = false;
                mRectVideoClipBound.setEmpty();
                changeCropMode(VideoOb.CROP_916);
            }
        });

    }

    private View mProportionLayout;

    private boolean bNeedExport = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.preview_edit_pic);
        setContentView(R.layout.activity_video_rotate_crop);
        Intent intent = getIntent();
        mScene = intent.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        bNeedExport = intent.getBooleanExtra(IntentConstants.INTENT_NEED_EXPORT, false);

        mAsp = intent.getFloatExtra(PARAM_MEDIA_ASP, -1);
        //片段编辑的播放器比例
        float nLastPreviewAsp = intent.getFloatExtra(PARAM_CROP_ASP, -1);
        bShowAEReplace = intent.getBooleanExtra(PARAM_SHOW_AE_REPLACE, false);
        bShowProportion = intent.getBooleanExtra(PARAM_SHOW_PROPORTION, true);
        boolean hideMirror = intent.getBooleanExtra(PARAM_HIDE_MIRROR, false);
        bHideCropView = intent.getBooleanExtra(PARAM_HIDE_CROPVIEW, false);
        boolean showApplyAll = intent.getBooleanExtra(PARAM_SHOW_APPLY_ALL, false);
        mCustomTitlte = intent.getStringExtra(PARAM_TITLE);
        initProportion();
        if (!hideMirror) {
            hideMirrorMenu = intent.getBooleanExtra(PARAM_HIDE_MIRROR_MENU, false);
            if (hideMirrorMenu) {
                $(R.id.ivMirrorLeftright).setVisibility(View.GONE);
                $(R.id.ivMirrorUpdown).setVisibility(View.GONE);
            }
        }
        mProportionLayout = $(R.id.ivProportionLayout);
        if (!bShowProportion) {
            mProportionLayout.setVisibility(View.GONE);
        }
        if (null == mScene) {
            finish();
            return;
        }
        mMedia = mScene.getAllMedia().get(0);
        mVideoOb = (VideoOb) mMedia.getTag();
        if (null == mVideoOb) {
            mVideoOb = VideoOb.createVideoOb(mMedia.getMediaPath());
            mMedia.setTag(mVideoOb);
        }

        float offPx = 1.0f;
        if (nLastPreviewAsp > 0 && (mVideoOb.getCropMode() == VideoOb.DEFAULT_CROP || mVideoOb.getCropMode() == VideoOb.CROP_FREE || mVideoOb.getCropMode() == VideoOb.CROP_ORIGINAL)
                && (mMedia.getClipRectF() == null || mMedia.getClipRectF().isEmpty() ||
                (Math.abs(mMedia.getClipRectF().width() - mMedia.getWidthInternal()) < offPx && Math.abs(mMedia.getClipRectF().height() - mMedia.getHeightInternal()) < offPx) ||
                (Math.abs(mMedia.getClipRectF().width() - mMedia.getHeightInternal()) < offPx && Math.abs(mMedia.getClipRectF().height() - mMedia.getWidthInternal()) < offPx)
        )) {
            float off = 0.01f; //容错
            //没有指定裁剪比例
            if (Math.abs(nLastPreviewAsp - 1) < off) {
                mVideoOb.setCropMode(VideoOb.CROP_1);
            } else if (Math.abs(nLastPreviewAsp - (9 / 16.0f)) < off) {
                mVideoOb.setCropMode(VideoOb.CROP_916);
            } else if (Math.abs(nLastPreviewAsp - (16 / 9.0f)) < off) {
                mVideoOb.setCropMode(VideoOb.CROP_169);
            } else if (Math.abs(nLastPreviewAsp - (mMedia.getWidth() / ((float) mMedia.getHeight()))) < off) {
                //原始
                mVideoOb.setCropMode(VideoOb.CROP_ORIGINAL);
            } else {
                //自由
                mVideoOb.setCropMode(VideoOb.CROP_FREE);
                if (nLastPreviewAsp > 0) {
                    { //3:4  | 4:3
                        mRectVideoClipBound = new RectF();
                        Rect rect = new Rect();
                        if (mMedia.getAngle() == 270 || mMedia.getAngle() == 90) {
                            MiscUtils.fixClipRect(nLastPreviewAsp, mMedia.getHeight(), mMedia.getWidth(), rect);
                        } else {
                            MiscUtils.fixClipRect(nLastPreviewAsp, mMedia.getWidth(), mMedia.getHeight(), rect);
                        }
                        mMedia.setClipRectF(new RectF(rect));
                    }
                }
            }
        }


        if (-1 != mAsp) {
            //ae图片指定裁剪了比例
            mRectVideoClipBound = new RectF();
            Rect rect = new Rect();
            MiscUtils.fixClipRect(mAsp, mMedia.getWidth(), mMedia.getHeight(), rect);
            mMedia.setClipRectF(new RectF(rect));
        }
        mllRotateMirror = $(R.id.llRotateMirror);
        if (hideMirror) {
            mllRotateMirror.setVisibility(View.GONE);
        }
        cbApplyToAll = $(R.id.cbApplyToAll);
        if (bShowAEReplace) {
            $(R.id.llReplace).setVisibility(View.VISIBLE);
            //滤镜
            mRvFilter = $(R.id.recyclerViewFilter);
            mSbStrength = $(R.id.sbarStrength);
            mTvFilter = $(R.id.tvFilterValue);
            $(R.id.strengthLayout).setVisibility(View.VISIBLE);
            mSbStrength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mDefaultValue = progress / 100.0f;
                    mTvFilter.setText(progress + "%");
                    if (fromUser) {
                        if (null != tmpLookup) {
                            tmpLookup.setDefaultValue(mDefaultValue);
                            try {
                                mMedia.changeFilter(tmpLookup);
                            } catch (InvalidArgumentException e) {
                                e.printStackTrace();
                            }
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

            ArrayList<WebFilterInfo> infos = (new FilterLookupLocalHandler(this)).getArrayList();
            mSbStrength.setMax(100);
            if (mMedia.getFilterList() != null && mMedia.getFilterList().size() > 0) {
                tmpLookup = mMedia.getFilterList().get(0);
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
        } else {
            $(R.id.mRCLayout).setVisibility(View.VISIBLE);
            cbApplyToAll.setVisibility(showApplyAll ? View.VISIBLE : View.GONE);
        }
        initViews();
        initPlayer();
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
            mMedia.changeFilter(tmpLookup);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private RectF mCropF = null;

    @Override
    protected void onPause() {
        if (mCvCrop != null) {
            mCropF = mCvCrop.getCrop();
            if (!mBackClick) {
                mCvCrop.setVisibility(View.INVISIBLE);
            }
        }
        videoPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mCropF != null) {
            RectF videoBound = new RectF(mCropF);
            mRectVideoClipBound = videoBound;
            mCvCrop.initialize(videoBound, videoBound, 0);
        }
        videoPlay();
        super.onResume();
    }

    private void videoPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mMediaPlayer.stop();
        finish();
        overridePendingTransition(0, 0);
    }

    private void initViews() {
        mMediaPlayer = $(R.id.vvMediaPlayer);
        mTvTitle = $(R.id.tvBottomTitle);
        mCvCrop = $(R.id.cvVideoCrop);

        mTvResetAll = $(R.id.tvResetAll);
        mPlayout = $(R.id.rlVideoCropFramePreview);

        if (!TextUtils.isEmpty(mCustomTitlte)) {
            mTvTitle.setText(mCustomTitlte);
        } else {
            mTvTitle.setText(mStrActivityPageName);

        }

        //该控件使用硬件加速
        mCvCrop.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mCvCrop.setIcropListener(new CropView.ICropListener() {

            @Override
            public void onPlayState() {

                if (mMediaPlayer.isPlaying()) {
                    videoPause();
                    mCvCrop.setStatebmp(BitmapFactory.decodeResource(
                            getResources(), R.drawable.btn_play));
                } else {
                    mMediaPlayer.start();
                    mCvCrop.setStatebmp(BitmapFactory.decodeResource(
                            getResources(), R.drawable.btn_pause));
                    mCvCrop.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mCvCrop.setStatebmp(null);
                        }
                    }, 500);
                }
            }

            @Override
            public void onMove() {
                if (!mTvResetAll.isClickable()) {
                    if (mCvCrop.getCrop().width() != mMedia.getWidth()
                            || mCvCrop.getCrop().height() != mMedia.getHeight()) {
                        setResetClickable(true);
                    }
                }
            }
        });

        if (checkIsLandRotate()) {
            mPlayout.setAspectRatio((double) mMedia.getHeight() / mMedia.getWidth());
            mCurDefaultAspect = (double) mMedia.getHeight() / mMedia.getWidth();
        } else {
            mCurDefaultAspect = (double) mMedia.getWidth() / mMedia.getHeight();
            mPlayout.setAspectRatio(mCurDefaultAspect);
        }


        mPlayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    videoPause();
                    mCvCrop.setStatebmp(BitmapFactory.decodeResource(
                            getResources(), R.drawable.btn_play));
                } else {
                    mMediaPlayer.start();
                    mCvCrop.setStatebmp(BitmapFactory.decodeResource(
                            getResources(), R.drawable.btn_pause));
                    mCvCrop.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mCvCrop.setStatebmp(null);
                        }
                    }, 500);
                }
            }
        });

    }

    private boolean mBackClick = false;

    private boolean checkIsLandRotate() {
        return mMedia.getAngle() % 180 != 0;
    }

    @Override
    public void clickView(View v) {
        int id = v.getId();
        if (id == R.id.ivCancel) {
            mIsPrepared = false;
            setResult(RESULT_CANCELED);
            mBackClick = true;
            onBackPressed();
            return;
        }
        if (!mIsPrepared) {
            return;
        }
        videoPause();
        if (id == R.id.tvResetAll) {
            mIsPrepared = false;
            mRectVideoClipBound = new RectF(mCurDefaultClipBound);
            mMedia.setAngle(mCurDefaultAngle);
            mMedia.setFlipType(mCurDefaultFlipType);
            changeCropMode(mCurDefaultCropMode);

            mMedia.setShowRectF(null);
            mMedia.setClipRectF(null);

            mPlayout.setAspectRatio(mCurDefaultAspect);
            setResetClickable(false);
            reload();
            videoPlay();
        } else if (id == R.id.ivRotate) {
            mIsPrepared = false;
            onSetRotate(false);
            reload();
            videoPlay();
            setResetClickable(true);
        } else if (id == R.id.ivMirrorUpdown) {
            mIsPrepared = false;
            setVideoMirror(true);
            setResetClickable(true);
        } else if (id == R.id.ivMirrorLeftright) {
            mIsPrepared = false;
            setVideoMirror(false);
            setResetClickable(true);
        } else if (id == R.id.ivSure) {
            RectF crop = mCvCrop.getCrop();
            RectF rcCrop;
            VideoConfig vc = new VideoConfig();
            Utils.fixVideoSize(vc, mMedia);
            int tmpW = vc.getVideoWidth();
            int tmpH = vc.getVideoHeight();

            if ((mMedia.getAngle() == 90 || mMedia.getAngle() == 270) &&
                    (mMedia.getFlipType() == FlipType.FLIP_TYPE_HORIZONTAL || mMedia.getFlipType() == FlipType.FLIP_TYPE_VERTICAL)) {
                rcCrop = new RectF(tmpW - crop.right, tmpH - crop.bottom, tmpW - crop.left, tmpH - crop.top);
            } else {
                rcCrop = new RectF(crop.left, crop.top, crop.right, crop.bottom);
            }

            mMedia.setClipRectF(rcCrop);
            mMedia.setShowRectF(null);


            if (bNeedExport) {
                //sdk裁切功能
                onExport(mMedia);
            } else {
                Intent intent = new Intent();
                intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mScene);
                boolean isApplyToAll = cbApplyToAll.isChecked();
                if (isApplyToAll) {
                    intent.putExtra(IntentConstants.INTENT_ALL_APPLY, isApplyToAll);
                    FlipType flipType = mMedia.getFlipType();
                    RectF clipRectF = null;
                    final int MIN_OFF_PX = 3;
                    if (mRbOriginal.isChecked() && Math.abs(rcCrop.width() - mMedia.getWidth()) < MIN_OFF_PX && Math.abs(rcCrop.height() - mMedia.getHeight()) < MIN_OFF_PX) {
                        //原比例且没有裁剪，其他视频也遵循原始比例且无裁剪
                        clipRectF = null;
                    } else {
                        clipRectF = new RectF(rcCrop.left / tmpW, rcCrop.top / tmpH,
                                rcCrop.right / tmpW, rcCrop.bottom / tmpH);
                    }
                    RCInfo param = new RCInfo(mMedia.getAngle(), clipRectF, flipType);
                    intent.putExtra(IntentConstants.INTENT_ALL_APPLY_PARAM, param);
                }
                setResult(RESULT_OK, intent);
                mBackClick = true;
                onBackPressed();
            }
        }
    }

    private void onExport(final MediaObject mediaObject) {
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                Scene scene = VirtualVideo.createScene();
                scene.addMedia(mediaObject);
                virtualVideo.addScene(scene);
            }
        });

        RectF rectF = mediaObject.getClipRectF();
        float asp = 0;
        if (null == rectF || rectF.isEmpty()) {
            asp = 0;
        } else {
            asp = rectF.width() / (rectF.height());
        }
        exportHandler.onExport(asp, true);
    }


    /**
     * * 旋转90、270 且镜像时，要修正宽高
     *
     * @return
     */
    private boolean needFixVideoSize() {
        return (mMedia.getAngle() == 90 || mMedia.getAngle() == 270)
                && (mMedia.getFlipType() == FlipType.FLIP_TYPE_HORIZONTAL || mMedia
                .getFlipType() == FlipType.FLIP_TYPE_VERTICAL);
    }


    private VirtualVideo.OnInfoListener mInfoListener = new VirtualVideo.OnInfoListener() {

        @Override
        public boolean onInfo(int what, int extra, Object obj) {
            if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
                SysAlertDialog.showLoadingDialog(CropRotateMirrorActivity.this,
                        R.string.isloading, false, null);
            }
            return false;
        }
    };


    @SuppressLint("ResourceType")
    private void setResetClickable(boolean clickable) {
        if (clickable) {
            mTvResetAll.setClickable(true);
        } else {
            mTvResetAll.setClickable(false);
        }
    }

    private void initPlayer() {
        mMediaPlayer.setClearFirst(true);
        mMediaPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {

            @Override
            public void onPlayerPrepared() {
                mProportionLayout.setClickable(true);
                SysAlertDialog.cancelLoadingDialog();
                if (!bHideCropView) {
                    mCvCrop.setVisibility(View.VISIBLE);
                    mCvCrop.setUnAbleBorder();
                }
                onVideoViewPrepared();

            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                onToast(R.string.preview_error);
                return true;
            }

            @Override
            public void onPlayerCompletion() {
            }

            @Override
            public void onGetCurrentPosition(float position) {
            }
        });
        boolean re = needFixVideoSize();

        if (re) {
            VideoConfig vc = new VideoConfig();
            Utils.fixVideoSize(vc, mMedia);
            int tmpW = vc.getVideoWidth();
            int tmpH = vc.getVideoHeight();

            RectF rcCrop = mMedia.getClipRectF();

            RectF srcClip = new RectF(tmpW - rcCrop.right,
                    tmpH - rcCrop.bottom,
                    tmpW - rcCrop.left,
                    tmpH - rcCrop.top);
            mRectVideoClipBound = new RectF(srcClip);
        } else {
            mRectVideoClipBound = new RectF(mMedia.getClipRectF());
        }

        mCurDefaultClipBound = new RectF(mRectVideoClipBound);
        setResetClickable(false);

        mCurDefaultAngle = mMedia.getAngle();
        mCurDefaultCropMode = mVideoOb.getCropMode();
        mCurDefaultFlipType = mMedia.getFlipType();
        mMedia.setClipRectF(null);
        mMedia.setShowRectF(null);

        reload();
        mMediaPlayer.setAutoRepeat(true);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.start();
    }

    /**
     * 加载媒体资源
     */
    private void reload() {
        mMediaPlayer.reset();
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMedia);
        mMediaPlayer.getVirtualVideo().addScene(scene);
        mMediaPlayer.build();
    }

    private void restoreUI() {
        if (mVideoOb.getCropMode() == VideoOb.CROP_FREE) {
            mRGCrop.check(R.id.rbCropFree);
        } else if (mVideoOb.getCropMode() == VideoOb.CROP_1) {
            mRGCrop.check(R.id.rbProportion1x1);
        } else if (mVideoOb.getCropMode() == VideoOb.CROP_169) {
            mRGCrop.check(R.id.rbProportion169);
        } else if (mVideoOb.getCropMode() == VideoOb.CROP_916) {
            mRGCrop.check(R.id.rbProportion916);
        } else {
            mRGCrop.check(R.id.rbCropOriginal);
        }

    }

    private void onVideoViewPrepared() {
        restoreUI();
        changeCropMode(mVideoOb.getCropMode());
        View frame = $(R.id.ivVideoCover);
        //淡出遮罩
        frame.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_out));
        frame.setVisibility(View.GONE);
    }

    private void changeCropMode(@VideoOb.CropMode int mode) {
        RectF videoBound = null;
        if (checkIsLandRotate()) {
            videoBound = new RectF(0, 0, mMedia.getHeight(), mMedia.getWidth());
        } else {
            videoBound = new RectF(0, 0, mMedia.getWidth(), mMedia.getHeight());
        }
        mVideoOb.setCropMode(mode);
        if (mRectVideoClipBound.isEmpty()) {
            mRectVideoClipBound = new RectF(videoBound);
        }
        mCvCrop.initialize(mRectVideoClipBound, videoBound, 0);
        mIsPrepared = true;
        if (bShowProportion) {
            mCvCrop.applyAspectText(getText(R.string.preview_crop).toString());
            if (mode == VideoOb.CROP_1) {
                mCvCrop.applySquareAspect(); // 方格，1:1
            } else if (mode == VideoOb.CROP_ORIGINAL) {
                mCvCrop.applyAspect(1, 1 / (videoBound.width() / (videoBound.height())));
            } else if (mode == VideoOb.CROP_169) {
                mCvCrop.applyAspect(1, 9f / 16);
            } else if (mode == VideoOb.CROP_916) {
                mCvCrop.applyAspect(1, 16 / 9.0f);
            } else {
                mCvCrop.applyFreeAspect();
            }
        } else {
            mCvCrop.applyAspect(1, 1 / (mRectVideoClipBound.width() / mRectVideoClipBound.height()));
            mCvCrop.setCanMove(true);
        }
    }

    private void videoPlay() {
        mMediaPlayer.start();
        mCvCrop.setStatebmp(BitmapFactory.decodeResource(getResources(), R.drawable.btn_pause));
        mCvCrop.postDelayed(new Runnable() {

            @Override
            public void run() {
                mCvCrop.setStatebmp(null);
            }
        }, 500);
    }

    /**
     * 响应设置媒体旋转
     */
    private void onSetRotate(boolean updown) {
        /** 媒体对象的子类，图片对象，拥有一系列对图片的操作 */
        /** 旋转角度 */
        if (mMedia == null) {
            return;
        }
        int rotateAngle = mMedia.getAngle();
        // 如果是横屏对象，要将显示区域和裁剪区域清空
        mMedia.setShowRectF(null);
        mMedia.setClipRectF(null);

        $(R.id.ivVideoCover).setVisibility(View.VISIBLE);
        if (updown) {
            mMedia.setAngle(rotateAngle += 180);
        } else {
            mCvCrop.setVisibility(View.INVISIBLE);
            rotateAngle -= 90;
            mMedia.setAngle(rotateAngle);
            //旋转之后，clip区域全部清掉，重新设置 （ 不然界面逻辑有问题，比如：横屏视频时，  按16:9crop， 旋转90度 ，视频变为竖排， 16:9crop的区域旋转后就变成9:16（与预期不符）  ）
            mRectVideoClipBound = mCvCrop.getCrop();
            mRectVideoClipBound.setEmpty();
        }
        if (checkIsLandRotate()) {
            mPlayout.setAspectRatio((double) mMedia.getHeight() / mMedia.getWidth());
        } else {
            mPlayout.setAspectRatio((double) mMedia.getWidth() / mMedia.getHeight());
        }
    }

    private void setVideoMirror(boolean updown) {
        if (updown) {
            if (FlipType.FLIP_TYPE_VERTICAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
            } else if (FlipType.FLIP_TYPE_HORIZONTAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
                onSetRotate(true);
            } else {
                mMedia.setFlipType(FlipType.FLIP_TYPE_VERTICAL);
            }
            mRectVideoClipBound = mCvCrop.getCrop();
            if (checkIsLandRotate()) {
                mRectVideoClipBound.set(mRectVideoClipBound.left, mMedia.getWidth() - mRectVideoClipBound.bottom,
                        mRectVideoClipBound.right, mMedia.getWidth() - mRectVideoClipBound.top);
            } else {
                mRectVideoClipBound.set(mRectVideoClipBound.left, mMedia.getHeight() - mRectVideoClipBound.bottom,
                        mRectVideoClipBound.right, mMedia.getHeight() - mRectVideoClipBound.top);
            }

        } else {
            if (FlipType.FLIP_TYPE_HORIZONTAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
            } else if (FlipType.FLIP_TYPE_VERTICAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
                onSetRotate(true);
            } else {
                mMedia.setFlipType(FlipType.FLIP_TYPE_HORIZONTAL);
            }
            mRectVideoClipBound = mCvCrop.getCrop();
            if (checkIsLandRotate()) {
                mRectVideoClipBound.set(mMedia.getHeight() - mRectVideoClipBound.right, mRectVideoClipBound.top,
                        mMedia.getHeight() - mRectVideoClipBound.left, mRectVideoClipBound.bottom);
            } else {
                mRectVideoClipBound.set(mMedia.getWidth() - mRectVideoClipBound.right, mRectVideoClipBound.top,
                        mMedia.getWidth() - mRectVideoClipBound.left, mRectVideoClipBound.bottom);
            }
        }
        reload();
        videoPlay();
    }

}
