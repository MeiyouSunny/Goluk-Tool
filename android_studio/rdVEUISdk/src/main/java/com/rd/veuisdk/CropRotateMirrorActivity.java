package com.rd.veuisdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.FlipType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.crop.CropView;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 视频编辑
 *
 * @author abreal
 */
public class CropRotateMirrorActivity extends BaseActivity {
    private static final String TAG = "VideoCropRotateActivity";
    private final int CROP_MODE_NORMAL = 2;
    private final int CROP_MODE_1x1 = 1;
    private final int CROP_MODE_FREE = 0;

    @BindView(R2.id.vvMediaPlayer)
    VirtualVideoView mMediaPlayer;
    @BindView(R2.id.tvTitle)
    TextView mTvTitle;
    @BindView(R2.id.btnLeft)
    ExtButton mBtnLeft;
    @BindView(R2.id.btnRight)
    ExtButton mBtnRight;
    @BindView(R2.id.cvVideoCrop)
    CropView mCvCrop;
    @BindView(R2.id.tvResetAll)
    TextView mTvResetAll;
    @BindView(R2.id.rlVideoCropFramePreview)
    PreviewFrameLayout mPlayout;

    private VirtualVideo mVirtualVideo;
    private Scene mScene;
    private MediaObject mMedia;
    private VideoOb mVideoOb;

    private RectF mRectVideoClipBound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.preview_edit_pic);
        setContentView(R.layout.activity_video_rotate_crop);
        ButterKnife.bind(this);

        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        if (null == mScene) {
            finish();
            return;
        }
        mMedia = mScene.getAllMedia().get(0);
        mVideoOb = (VideoOb) mMedia.getTag();
        initViews();
        initPlayer();
    }

    private RectF mCropF = null;

    @Override
    protected void onPause() {
        if (mCvCrop != null) {
            mCropF = mCvCrop.getCrop();
        }
        if (!mBackClick) {
            mCvCrop.setVisibility(View.INVISIBLE);
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
        // m_tvpMain.start();
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
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                CropRotateMirrorActivity.this.finish();
                CropRotateMirrorActivity.this.overridePendingTransition(0, 0);
            }
        }, 200);

    }

    private void initViews() {
        mBtnLeft.setVisibility(View.INVISIBLE);
        mBtnRight.setVisibility(View.INVISIBLE);

//        if (SdkEntry.getCustomUI() == 1) {
//            if (mVideoOb.getCropMode() == CROP_MODE_FREE) {
//                mTvTitle.setText(R.string.crop_free_title);
//            } else if (mVideoOb.getCropMode() == CROP_MODE_1x1) {
//                mTvTitle.setText(R.string.crop_1x1_title);
//            } else if (mVideoOb.getCropMode() == CROP_MODE_NORMAL) {
//                mTvTitle.setText(R.string.crop_normal_title);
//            }
//        } else {
        mTvTitle.setText(mStrActivityPageName);
//        }

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
                if (mTvResetAll.isClickable() == false) {
                    if (mCvCrop.getCrop().width() != mMedia.getWidth()
                            || mCvCrop.getCrop().height() != mMedia.getHeight()) {
                        setResetClickable(true);
                    }
                }
            }
        });

        if (checkIsLandRotate()) {
            mPlayout.setAspectRatio((double) mMedia.getHeight() / mMedia.getWidth());
        } else {
            mPlayout.setAspectRatio((double) mMedia.getWidth() / mMedia.getHeight());
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
        if (mMedia.getAngle() % 180 == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void clickView(View v) {
        int id = v.getId();

        if (id == R.id.tvResetAll) {
            mMedia.setAngle(0);
            mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
            changeCropMode(CROP_MODE_FREE);


            mMedia.setShowRectF(null);
            mMedia.setClipRectF(null);

            mPlayout.setAspectRatio((double) mMedia.getWidth() / mMedia.getHeight());
            setResetClickable(false);
            reload();
            videoPlay();
        } else if (id == R.id.ivRotateCounterClock) {
            onSetRotate(false, false);
            setResetClickable(true);
        } else if (id == R.id.ivMirrorUpdown) {
            setVideoMirror(true);
            setResetClickable(true);
        } else if (id == R.id.ivMirrorLeftright) {
            setVideoMirror(false);
            setResetClickable(true);
        } else if (id == R.id.ivProportion) {
            String[] menu = getResources().getStringArray(R.array.crop_menu);

            SysAlertDialog.showListviewAlertMenu(this, "", menu,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                changeCropMode(CROP_MODE_NORMAL);
                                mRectVideoClipBound.setEmpty();
                                setResetClickable(true);
                            } else if (which == 1) {
                                changeCropMode(CROP_MODE_1x1);
                                mRectVideoClipBound.setEmpty();
                                setResetClickable(true);
                            } else if (which == 2) {
                                changeCropMode(CROP_MODE_FREE);
                                mRectVideoClipBound.setEmpty();
                            }
                        }
                    });
        } else if (id == R.id.public_menu_sure) {
            Intent intent = new Intent();
            RectF crop = mCvCrop.getCrop();
            RectF rcCrop;
            if ((mMedia.getAngle() == 90 || mMedia.getAngle() == 270)
                    && (mMedia.getFlipType() == FlipType.FLIP_TYPE_HORIZONTAL || mMedia
                    .getFlipType() == FlipType.FLIP_TYPE_VERTICAL)) {
                rcCrop = new RectF(mMedia.getWidth() - crop.right,
                        mMedia.getHeight() - crop.bottom,
                        mMedia.getWidth() - crop.left,
                        mMedia.getHeight() - crop.top);
            } else {
                rcCrop = new RectF(crop.left, crop.top, crop.right, crop.bottom);
            }

            mMedia.setClipRectF(rcCrop);
            mMedia.setShowRectF(null);

            intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mScene);
            setResult(RESULT_OK, intent);
            mBackClick = true;
            onBackPressed();
        } else if (id == R.id.public_menu_cancel) {
            setResult(RESULT_CANCELED);
            mBackClick = true;
            onBackPressed();
        }
    }

    private VirtualVideo.OnInfoListener mInfoListener = new VirtualVideo.OnInfoListener() {

        @Override
        public boolean onInfo(int what, int extra, Object obj) {
            if (what == VirtualVideoView.INFO_WHAT_PLAYBACK_PREPARING) {
                SysAlertDialog.showLoadingDialog(CropRotateMirrorActivity.this,
                        R.string.isloading, false, null);
            }
            return false;
        }
    };


    private void setResetClickable(boolean clickable) {
        if (clickable) {
            mTvResetAll.setClickable(true);
            mTvResetAll.setTextColor(getResources().getColor(
                    R.drawable.main_orange_button));
        } else {
            mTvResetAll.setTextColor(getResources().getColor(
                    R.color.border_no_checked));
            mTvResetAll.setClickable(false);
        }
    }

    private void initPlayer() {
        mMediaPlayer.setClearFirst(true);
        mMediaPlayer.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {

            @Override
            public void onPlayerPrepared() {
                onVideoViewPrepared();
                mCvCrop.setVisibility(View.VISIBLE);
                SysAlertDialog.cancelLoadingDialog();
                mCvCrop.setUnAbleBorder();
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                SysAlertDialog.showAutoHideDialog(
                        CropRotateMirrorActivity.this, R.string.string_null,
                        R.string.error_preview_retry, Toast.LENGTH_SHORT);
                return true;
            }

            @Override
            public void onPlayerCompletion() {
            }

            @Override
            public void onGetCurrentPosition(float position) {
            }
        });
        mRectVideoClipBound = new RectF(mMedia.getClipRectF());
        if (mRectVideoClipBound.isEmpty()) {
            setResetClickable(false);
        } else {
            if ((mMedia.getAngle() == 90 || mMedia.getAngle() == 270)
                    && (mMedia.getFlipType() == FlipType.FLIP_TYPE_HORIZONTAL ||
                    mMedia.getFlipType() == FlipType.FLIP_TYPE_VERTICAL)) {
                RectF rcCrop = new RectF(Math.round(mMedia.getWidth() - mRectVideoClipBound.right),
                        Math.round(mMedia.getHeight() - mRectVideoClipBound.bottom),
                        Math.round(mMedia.getWidth() - mRectVideoClipBound.left),
                        Math.round(mMedia.getHeight() - mRectVideoClipBound.top));
                mRectVideoClipBound = new RectF(rcCrop);
            }
            if (Math.abs(mRectVideoClipBound.width() - mMedia.getWidth()) >= 0.05
                    || Math.abs(mRectVideoClipBound.height() - mMedia.getHeight()) >= 0.05) {
                setResetClickable(true);
            } else {
                setResetClickable(false);
            }
        }

        if (mMedia.getAngle() != 0 || mMedia.getFlipType() != FlipType.FLIP_TYPE_NONE) {
            setResetClickable(true);
        }

        if (mVideoOb.getCropMode() != CROP_MODE_FREE) {
            setResetClickable(true);
        }

        mMedia.setClipRectF(null);
        mMedia.setShowRectF(null);

        mVirtualVideo = new VirtualVideo();
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMedia);
        mVirtualVideo.addScene(scene);
        try {
            mVirtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }

        mMediaPlayer.setAutoRepeat(true);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.start();
    }

    protected void onVideoViewPrepared() {
        changeCropMode(mVideoOb.getCropMode());
        mRectVideoClipBound.setEmpty();
        setViewVisibility(R.id.ivVideoConver, false);
    }

    void changeCropMode(int nCropMode) {
        int width = 0;
        int height = 0;
        if (checkIsLandRotate()) {
            width = mMedia.getHeight();
            height = mMedia.getWidth();
        } else {
            width = mMedia.getWidth();
            height = mMedia.getHeight();
        }
        RectF videoBound = new RectF(0, 0, width, height);

        if (nCropMode == 0) {
            mTvTitle.setText(R.string.crop_free_title);
        } else if (nCropMode == 1) {
            mTvTitle.setText(R.string.crop_1x1_title);
        } else if (nCropMode == 2) {
            mTvTitle.setText(R.string.crop_normal_title);
        }

        mVideoOb.setCropMode(nCropMode);
        if (mRectVideoClipBound.isEmpty()) {
            mRectVideoClipBound = videoBound;
        }
        mCvCrop.initialize(mRectVideoClipBound, videoBound, 0);
        mCvCrop.applyAspectText(getText(R.string.preview_crop).toString());
        if (nCropMode == CROP_MODE_1x1) {
            mCvCrop.applySquareAspect(); // 方格，1:1
        } else if (nCropMode == CROP_MODE_NORMAL) {
            mCvCrop.applyAspect(mMedia.getWidth(), mMedia.getHeight());
        } else {
            mCvCrop.applyFreeAspect();
        }
    }

    private void videoPlay() {
        mMediaPlayer.start();
        mCvCrop.setStatebmp(BitmapFactory.decodeResource(getResources(),
                R.drawable.btn_pause));
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
    private void onSetRotate(boolean clockwise, boolean updown) {
        /** 媒体对象的子类，图片对象，拥有一系列对图片的操作 */
        /** 旋转角度 */

        int nRotateAngle = 0;
        if (mMedia == null) {
            return;
        }
        mCvCrop.setVisibility(View.INVISIBLE);
        nRotateAngle = mMedia.getAngle();
        // 如果是横屏对象，要将显示区域和裁剪区域清空
        mMedia.setShowRectF(null);
        mMedia.setClipRectF(null);

        if (clockwise) {
            mMedia.setAngle(nRotateAngle += 90);
        } else if (updown) {
            mMedia.setAngle(nRotateAngle += 180);
        } else {
            mMedia.setAngle(nRotateAngle += 270);
        }
        if (checkIsLandRotate()) {
            mPlayout.setAspectRatio((double) mMedia.getHeight() / mMedia.getWidth());
        } else {
            mPlayout.setAspectRatio((double) mMedia.getWidth() / mMedia.getHeight());
        }
        reload();
        videoPlay();
    }

    private void setVideoMirror(boolean updown) {
        mCvCrop.setVisibility(View.INVISIBLE);
        if (updown) {
            if (FlipType.FLIP_TYPE_VERTICAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
            } else if (FlipType.FLIP_TYPE_HORIZONTAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
                onSetRotate(false, true);
            } else {
                mMedia.setFlipType(FlipType.FLIP_TYPE_VERTICAL);
            }
        } else {
            if (FlipType.FLIP_TYPE_HORIZONTAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
            } else if (FlipType.FLIP_TYPE_VERTICAL == mMedia.getFlipType()) {
                mMedia.setFlipType(FlipType.FLIP_TYPE_NONE);
                onSetRotate(false, true);
            } else {
                mMedia.setFlipType(FlipType.FLIP_TYPE_HORIZONTAL);
            }
        }
        reload();
        videoPlay();
    }

    private void reload() {
        mMediaPlayer.reset();
        try {
            mVirtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }
}
