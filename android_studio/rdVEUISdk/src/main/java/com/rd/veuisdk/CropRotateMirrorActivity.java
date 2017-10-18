package com.rd.veuisdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.FlipType;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.crop.CropView;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;


/**
 * 视频编辑
 *
 * @author abreal
 */
public class CropRotateMirrorActivity extends BaseActivity {
    private static final String TAG = "CropRotateMirrorActivity";
    private final int CROP_MODE_NORMAL = 2;
    private final int CROP_MODE_1x1 = 1;
    private final int CROP_MODE_FREE = 0;

    private VirtualVideoView mMediaPlayer;
    private TextView mTvTitle;
    private ExtButton mBtnLeft;
    private ExtButton mBtnRight;
    private CropView mCvCrop;
    private TextView mTvResetAll;
    private PreviewFrameLayout mPlayout;

    private Scene mScene;
    private MediaObject mMedia;
    private VideoOb mVideoOb;

    private RectF mRectVideoClipBound;


    public static final String SHOW_CROP = "bgonecrop";
    private boolean bCropShow = true;//显示裁剪功能


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.preview_edit_pic);
        setContentView(R.layout.activity_video_rotate_crop);

        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        bCropShow = getIntent().getBooleanExtra(SHOW_CROP, true);
        if (!bCropShow) {
            findViewById(R.id.ivProportion).setVisibility(View.GONE);
        }
        if (null == mScene) {
            finish();
            return;
        }
        mMedia = mScene.getAllMedia().get(0);
        mVideoOb = (VideoOb) mMedia.getTag();
        if (null == mVideoOb) {
            mVideoOb = VideoOb.createVideoOb(mMedia.getMediaPath());
            if (null != mVideoOb) {
                mMedia.setTag(mVideoOb);
            } else {
                finish();
                return;
            }
        }
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
        mMediaPlayer = (VirtualVideoView) findViewById(R.id.vvMediaPlayer);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        mBtnLeft = (ExtButton) findViewById(R.id.btnLeft);
        mBtnRight = (ExtButton) findViewById(R.id.btnRight);
        mCvCrop = (CropView) findViewById(R.id.cvVideoCrop);

        if (!bCropShow) {
            mCvCrop.setVisibility(View.GONE);
        }
        mTvResetAll = (TextView) findViewById(R.id.tvResetAll);
        mPlayout = (PreviewFrameLayout) findViewById(R.id.rlVideoCropFramePreview);
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
                                if (bCropShow) {
                                    mRectVideoClipBound.setEmpty();
                                }
                                setResetClickable(true);
                            } else if (which == 1) {
                                changeCropMode(CROP_MODE_1x1);
                                if (bCropShow) {
                                    mRectVideoClipBound.setEmpty();
                                }
                                setResetClickable(true);
                            } else if (which == 2) {
                                changeCropMode(CROP_MODE_FREE);
                                if (bCropShow) {
                                    mRectVideoClipBound.setEmpty();
                                }
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


                VideoConfig vc = new VideoConfig();
                fixVideoSize(vc);

                int tmpW = vc.getVideoWidth();
                int tmpH = vc.getVideoHeight();

                rcCrop = new RectF(tmpW - crop.right,
                        tmpH - crop.bottom,
                        tmpW - crop.left,
                        tmpH - crop.top);
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

    /**
     * 旋转90、270 且镜像时，要修正宽高
     *
     * @param vc
     */
    private void fixVideoSize(VideoConfig vc) {
        VirtualVideo.getMediaInfo(mMedia.getMediaPath(), vc);
        int tmpW = vc.getVideoWidth();
        int tmpH = vc.getVideoHeight();
        if (mMedia.getAngle() == 90 || mMedia.getAngle() == 270) {
            int tmp = tmpW;
            tmpW = tmpH;
            tmpH = tmp;
        }
        //旋转90、270 且镜像时，要修正宽高
        vc.setVideoSize(tmpW, tmpH);
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


        if (needFixVideoSize()) {
            VideoConfig vc = new VideoConfig();
            fixVideoSize(vc);
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
        if (mRectVideoClipBound.isEmpty()) {
            setResetClickable(false);
        } else {
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

        reload();
        mMediaPlayer.setAutoRepeat(true);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.start();
    }

    /**
     * 加载媒体资源
     */
    private void reload() {
        VirtualVideo mVirtualVideo = mMediaPlayer.getVirtualVideo();
        mVirtualVideo.reset();
        Scene scene = VirtualVideo.createScene();
        scene.addMedia(mMedia);
        mVirtualVideo.addScene(scene);
        try {
            mMediaPlayer.build();
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    protected void onVideoViewPrepared() {
        changeCropMode(mVideoOb.getCropMode());
        if (bCropShow) {
            mRectVideoClipBound.setEmpty();
        }
        View frame = findViewById(R.id.ivVideoConver);
        if (null != frame) {
            //淡出遮罩
            frame.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_out));
            frame.setVisibility(View.GONE);
        }
    }

    private void changeCropMode(int nCropMode) {
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
        if (bCropShow) {
            mCvCrop.applyAspectText(getText(R.string.preview_crop).toString());
            if (nCropMode == CROP_MODE_1x1) {
                mCvCrop.applySquareAspect(); // 方格，1:1
            } else if (nCropMode == CROP_MODE_NORMAL) {
                mCvCrop.applyAspect(mMedia.getWidth(), mMedia.getHeight());
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

}
