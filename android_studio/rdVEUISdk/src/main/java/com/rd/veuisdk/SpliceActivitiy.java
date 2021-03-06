package com.rd.veuisdk;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MaskObject;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.PermutationMode;
import com.rd.vecore.models.Scene;
import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.fragment.MusicFragmentEx;
import com.rd.veuisdk.fragment.splice.SpliceBorderFragment;
import com.rd.veuisdk.fragment.splice.SpliceEditItemFragment;
import com.rd.veuisdk.fragment.splice.SpliceModeFragment;
import com.rd.veuisdk.fragment.splice.SpliceOrderFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.GridInfo;
import com.rd.veuisdk.model.SpliceGridMediaInfo;
import com.rd.veuisdk.model.SpliceModeInfo;
import com.rd.veuisdk.mvp.model.SpliceModel;
import com.rd.veuisdk.ui.DragZoomImageView;
import com.rd.veuisdk.ui.ExtDragLayout;
import com.rd.veuisdk.ui.VideoPreviewLayout;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamDataImp;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.ISpliceHandler;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static com.rd.vecore.models.AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING;

/**
 * ????????????
 * ???????????????????????????????????????????????????????????? ???????????????????????????????????????  --- 190614
 */
public class SpliceActivitiy extends BaseActivity implements ISpliceHandler, IVideoMusicEditor, IParamHandler {
    private final int REQUESTCODE_FOR_REPLACE = 1001;
    private int mModeIndex = 0;
    private SpliceModel mModel;
    private SpliceModeFragment mModeFragment;
    private SpliceEditItemFragment mSpliceEditItemFragment;
    private SpliceBorderFragment mBorderFragment;
    private SpliceOrderFragment mOrderFragment;
    private MusicFragmentEx mMusicFragmentEx;
    private List<VideoPreviewLayout> listPreview = new ArrayList<>();
    private PreviewFrameLayout mPreviewFrameLayout;  //????????????
    private ExtDragLayout mFrameLayout; //view?????????
    private List<SpliceGridMediaInfo> mSpliceMediaList;
    private View mDragMasking;
    private VirtualVideoView player;
    private TextView tvTitle;
    private float nMaxDuration; //?????????????????????????????????  ???????????????
    private float nCountDuration; //?????????????????????????????????????????????
    private View mProgressBarLayout;
    private TextView currentTv;
    private TextView totalTv;
    private SeekBar mRdSeekBar;
    private CheckedTextView mCheckedTextViewStyle, mCheckedTextViewBorder, mCheckedTextViewOrder, mCheckedTextViewMusic;
    private View mBottomMenu;
    private int nBgColor = Color.parseColor("#FFFFFF");
    private IParamDataImp mParamDataImp = new IParamDataImp();
    private View mTitleBarLayout;
    private ImageView mIvVideoPlayState;
    private boolean bInterceptRepeatClick = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "SpliceActivitiy";
        setContentView(R.layout.activity_splice_layout);
        final List<MediaObject> mediaList = getIntent().getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
        if (null == mediaList || mediaList.size() == 0) {
            Log.e(TAG, "onCreate: mediaList is null");
            onToast(R.string.select_media_hint);
            finish();
            return;
        }
        if (mediaList.size() == 1 || mediaList.size() > 9) {
            Log.e(TAG, "onCreate: mediaList.size() must 1<?< 9  ");
            onToast(getString(R.string.media_num_limit, 1, 9));
            finish();
            return;
        }
        mIvVideoPlayState = findViewById(R.id.ivPlayerState);
        mTitleBarLayout = findViewById(R.id.titlebar_layout);
        mBottomMenu = findViewById(R.id.bottomLayout);
        mSpliceEditItemFragment = SpliceEditItemFragment.newInstance();
        mVirtualVideo = new VirtualVideo();
        player = findViewById(R.id.splicePlayer);
        currentTv = findViewById(R.id.tvCurTime);
        totalTv = findViewById(R.id.tvTotalTime);
        mRdSeekBar = findViewById(R.id.sbEditor);
        mRdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekTo(progress);
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


        mCheckedTextViewStyle = findViewById(R.id.btn_splice_layout);
        mCheckedTextViewBorder = findViewById(R.id.btn_splice_border);
        mCheckedTextViewOrder = findViewById(R.id.btn_splice_order);
        mCheckedTextViewMusic = findViewById(R.id.btn_splice_music);

        mProgressBarLayout = findViewById(R.id.progressBarLayout);
        mDragMasking = findViewById(R.id.drag_masking);
        mPreviewFrameLayout = findViewById(R.id.previewFrame);
        mFrameLayout = findViewById(R.id.spliceParent);
        mFrameLayout.setBackgroundColor(nBgColor);
        findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.preview);
        tvTitle.setBackgroundResource(R.drawable.shape_rb_button);
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPreviewClick();
            }
        });

        Button btnRight = findViewById(R.id.btnRight);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setTextColor(getResources().getColor(R.color.main_orange));
        btnRight.setText(R.string.export);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExport();
            }
        });

        mModel = new SpliceModel();


        List<SpliceModeInfo> list = mModel.getSpliceList(this, mediaList.size());
        final SpliceModeInfo mSpliceModeInfo = list.get(0);
        mModeFragment = SpliceModeFragment.newInstance();
        mModeFragment.setList(list);
        changeFragment(R.id.fl_fragment_container, mModeFragment);
        SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading), false, null);

        // ????????????
        mPreviewFrameLayout.setAspectRatio(mAsp);

        initPlayer();

        initThumb(mediaList, new IThumb() {
            @Override
            public void onThumbPrepared() {
                initSpliceMedia(mSpliceModeInfo);
                mHandler.post(new PreparedRunnable());
            }
        });

    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!bInterceptRepeatClick) {
                //??????????????????
                bInterceptRepeatClick = true;
                player.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bInterceptRepeatClick = false;
                    }
                }, 500);
                if (player.isPlaying()) {
                    pause();
                } else {
                    start();
                }
            }
        }
    };

    private class PreparedRunnable implements Runnable {
        @Override
        public void run() {
            initGridLayout();
            onMaskAnimGoneImp();
        }
    }

    private int nThumbPrepared = 0;

    private interface IThumb {
        /**
         * ?????????????????????
         */
        void onThumbPrepared();
    }

    /**
     * ????????????????????????UI??????
     */
    private void initThumb(final List<MediaObject> mMediaList, final IThumb iThumb) {
        final int len = mMediaList.size();
        nThumbPrepared = 0;
        mSpliceMediaList = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final int index = i;
            //???????????????
            ThreadPoolUtils.executeEx(new Runnable() {
                @Override
                public void run() {
                    SpliceGridMediaInfo info = new SpliceGridMediaInfo();
                    mModel.initItemMedia(null, mMediaList.get(index), info);
                    mSpliceMediaList.add(info);
                    synchronized (this) {
                        if (nThumbPrepared == (len - 1)) {
                            initFixDuration();
                            if (null != iThumb) {
                                iThumb.onThumbPrepared();
                            }
                        } else {
                            nThumbPrepared++;
                        }
                    }
                }
            });
        }
    }

    private final float MIN_DURATION = 4.0f;

    /**
     * build??????????????????????????????????????????duration,?????????????????????????????????  (????????????||????????????  ?????????????????????)
     */
    private void initFixDuration() {
        int len = mSpliceMediaList.size();
        nMaxDuration = 0;
        nCountDuration = 0;
        for (int i = 0; i < len; i++) {
            MediaObject tmp = mSpliceMediaList.get(i).getMediaObject();
            if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                //???????????????????????????
                nMaxDuration = Math.max(tmp.getDuration(), nMaxDuration);
                nCountDuration += tmp.getDuration();
            }
        }
        if (nMaxDuration == 0) {
            //????????????????????????4???
            nMaxDuration = MIN_DURATION;
        }
        if (nCountDuration < nMaxDuration) {
            nCountDuration = nMaxDuration;
        }
    }


    /**
     * ??????spliceMixMedia  ,????????????????????????????????????????????????
     *
     * @param modeInfo ??????????????????
     */
    private void initSpliceMedia(@Nullable SpliceModeInfo modeInfo) {
        int len = mSpliceMediaList.size();
        for (int i = 0; i < len; i++) {
            SpliceGridMediaInfo info = mSpliceMediaList.get(i);
            if (null != modeInfo) {
                info.setGridInfo(modeInfo.getGridInfoList().get(i));
            }
            //?????????????????????clip
            info.setClipValue(null);
            info.setClipRectF(null, null);
            MediaObject mediaObject = info.getMediaObject();
            if (null != mediaObject) {
                mediaObject.setClipRect(null);
            }
        }
    }


    private float lastProgress = 0f;
    private boolean lastIsPlaying = false;
    private boolean isPreviewUI = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (isPreviewUI) {
            if (lastIsPlaying) {
                player.seekTo(lastProgress);
                player.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPreviewUI) {
            lastIsPlaying = player.isPlaying();
            if (lastIsPlaying) {
                lastProgress = player.getCurrentPosition();
                player.pause();
            }
        }
    }

    private void onPreparedUI() {
        mDragMasking.setVisibility(View.GONE);
        SysAlertDialog.cancelLoadingDialog();
    }

    private void initPlayer() {
        player.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {
                if (what == VirtualVideo.INFO_WHAT_PLAYBACK_FIRST_FRAME) {
                    //??????????????????????????????
                    onPreparedUI();
                    return true;
                }
                return false;
            }
        });
        player.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {

            @Override
            public void onPlayerPrepared() {
                int ms = Utils.s2ms(player.getDuration());
                mRdSeekBar.setMax(ms);
                totalTv.setText(getFormatTime(ms));
                onSeekTo(0);
            }

            @Override
            public boolean onPlayerError(int what, int extra) {
                Log.e(TAG, "onPlayerError: " + extra + "..." + what);
                onToast(getString(R.string.preview_error));
                onPreparedUI();
                return false;
            }

            @Override
            public void onPlayerCompletion() {
                player.seekTo(0);
                exitPlayerMode();
            }

            @Override
            public void onGetCurrentPosition(float position) {
                onSeekTo(Utils.s2ms(position));
            }
        });
    }

    @Override
    public void seekTo(int msec) {
        player.seekTo(Utils.ms2s(msec));
        onSeekTo(msec);
    }

    @Override
    public int getDuration() {
        return MiscUtils.s2ms(player.getDuration());
    }

    @Override
    public int getCurrentPosition() {
        return MiscUtils.s2ms(player.getCurrentPosition());
    }

    private void onSeekTo(int progress) {
        currentTv.setText(getFormatTime(progress));
        mRdSeekBar.setProgress(progress);
    }

    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    private void onPreviewClick() {
        if (!isPreviewUI) {
            isPreviewUI = true;
            tvTitle.setText(R.string.stop);
            getClip();
            mDragMasking.setVisibility(View.VISIBLE);
            mFrameLayout.setVisibility(View.GONE);
            mProgressBarLayout.setVisibility(View.VISIBLE);
            SysAlertDialog.showLoadingDialog(this, R.string.isloading);
            initPlayerData();
        } else {
            //???????????????????????????
            exitPlayerMode();
        }
    }

    //???????????????????????????
    private void exitPlayerMode() {
        isPreviewUI = false;
        player.stop();
        tvTitle.setText(R.string.preview);
        mFrameLayout.setVisibility(View.VISIBLE);
        mProgressBarLayout.setVisibility(View.GONE);
    }


    private VirtualVideo mVirtualVideo = null;

    private void initPlayerData() {
        mVirtualVideo.reset();
        try {
            player.reset();
            player.setPreviewAspectRatio(mAsp);
            player.setBackgroundColor(nBgColor);
            onSeekTo(0);
            //??????????????????
            reload(mVirtualVideo);
            addMusic(mVirtualVideo);
            mVirtualVideo.build(player);
            player.start();
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private void onMaskAnimGoneImp() {
        mDragMasking.startAnimation(AnimationUtils.loadAnimation(mDragMasking.getContext(), R.anim.fade_out));
        mDragMasking.setVisibility(View.GONE);
        SysAlertDialog.cancelLoadingDialog();
    }

    /**
     * ????????????
     */
    private void onMaskAnimGone() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onMaskAnimGoneImp();
            }
        }, 100);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: {

                }
                break;
                default:
                    break;
            }
        }
    };

    private VideoPreviewLayout mCurrentEdit;

    /**
     * ??????????????????????????????
     *
     * @param item ?????????????????????
     */
    private void onItemGridEditClick(final VideoPreviewLayout item) {
        mTitleBarLayout.setVisibility(View.INVISIBLE);
        mBottomMenu.setVisibility(View.GONE);
        mDragMasking.setVisibility(View.VISIBLE);
        mCurrentEdit = item;
        int len = listPreview.size();
        for (int i = 0; i < len; i++) {
            VideoPreviewLayout tmp = listPreview.get(i);
            tmp.getDragZoomImageView().setShadowMode(tmp != item);
        }
        mDragMasking.setVisibility(View.GONE);
        mSpliceEditItemFragment.setCurrentMedia(item.getBindGrid().getMediaObject());

        //????????????
        changeFragment(R.id.fl_fragment_container, mSpliceEditItemFragment);
    }

    /**
     * ??????????????????
     */
    private void initGridLayout() {
        listPreview.clear();
        mFrameLayout.removeAllViews();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        int len = mSpliceMediaList.size();
        int pW = mPreviewFrameLayout.getWidth(), pH = mPreviewFrameLayout.getHeight();

        float borderWidth = getBorderWidth(pW);

        for (int i = 0; i < len; i++) {
            SpliceGridMediaInfo info = mSpliceMediaList.get(i);
            final VideoPreviewLayout itemVideoParent = new VideoPreviewLayout(this, null);
            itemVideoParent.setId(i);
            RectF rectF = mModel.getScaledRectF(pW, pH, info.getGridInfo().getRectF(), borderWidth, info.getGridInfo().isAlien());
            itemVideoParent.setCustomRect(rectF);
            mFrameLayout.addView(itemVideoParent, lp);
            {

                DragZoomImageView dragZoomImageView = new DragZoomImageView(this);
                dragZoomImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemGridEditClick(itemVideoParent);
                    }
                });
                String assetPath = info.getGridInfo().getTransPath();

                dragZoomImageView.setPointFList(info.getGridInfo().getPointFList());
                dragZoomImageView.setBitmap(info.getThumbBmp(), info.getMediaObject().getAngle(), mModel.getTransBmp(this, assetPath));

                itemVideoParent.setBindGrid(info);
                itemVideoParent.setDragZoomImageView(dragZoomImageView);
                itemVideoParent.addView(dragZoomImageView, lp);
            }

            listPreview.add(itemVideoParent);
        }

        mFrameLayout.setListener(new ExtDragLayout.IDragChangeListener() {
            @Override
            public void onChangeItem(VideoPreviewLayout from, VideoPreviewLayout to) {

                SpliceGridMediaInfo tmp = from.getBindGrid();

                //?????????????????????
                MediaObject mediaObject = tmp.getMediaObject();
                Bitmap cover = tmp.getThumbBmp();
                String thumbPath = tmp.getThumbPath();
                Rect rect = tmp.getSize();

                SpliceGridMediaInfo newGrid = to.getBindGrid();


                //????????????????????????
                tmp.updateMedia(newGrid.getMediaObject(), newGrid.getThumbBmp(), newGrid.getThumbPath());
                tmp.setSize(newGrid.getSize());
                //
                newGrid.updateMedia(mediaObject, cover, thumbPath);
                newGrid.setSize(rect);

            }
        });
    }


    @Override
    public void clickView(View v) {

        int id = v.getId();
        mCheckedTextViewStyle.setChecked(id == R.id.btn_splice_layout);
        mCheckedTextViewBorder.setChecked(id == R.id.btn_splice_border);
        mCheckedTextViewOrder.setChecked(id == R.id.btn_splice_order);
        mCheckedTextViewMusic.setChecked(id == R.id.btn_splice_music);


        if (id == R.id.btn_splice_layout) {
            changeFragment(R.id.fl_fragment_container, mModeFragment);
        } else if (id == R.id.btn_splice_border) {
            if (null == mBorderFragment) {
                mBorderFragment = SpliceBorderFragment.newInstance();
            }
            changeFragment(R.id.fl_fragment_container, mBorderFragment);
            if (isPreviewUI) {
                exitPlayerMode();
            }
        } else if (id == R.id.btn_splice_order) {
            if (null == mOrderFragment) {
                mOrderFragment = SpliceOrderFragment.newInstance();
            }
            changeFragment(R.id.fl_fragment_container, mOrderFragment);
        } else if (id == R.id.btn_splice_music) {
            onMusicClick();
        }
    }

    /**
     * ??????????????????
     */
    private void onMusicClick() {
        if (!isPreviewUI) {
            onPreviewClick();
        }

        mTitleBarLayout.setVisibility(View.INVISIBLE);
        mBottomMenu.setVisibility(View.GONE);
        if (null == mMusicFragmentEx) {
            mMusicFragmentEx = MusicFragmentEx.newInstance();
        }

        UIConfiguration mUIConfig = SdkEntry.getSdkService().getUIConfig();

        //??????????????????
        String musicUrl = mUIConfig.newMusicUrl;
        boolean useNewMusicUrl = true;
        if (TextUtils.isEmpty(musicUrl)) {
            useNewMusicUrl = false;
            musicUrl = mUIConfig.musicUrl;
        }
        String typeUrl = "", cloudUrl = "";
        if (!TextUtils.isEmpty(typeUrl = mUIConfig.newCloudMusicTypeUrl) && !TextUtils.isEmpty(cloudUrl = mUIConfig.newCloudMusicUrl)) {
            //????????????????????? (???????????????????????????)
        } else if (!TextUtils.isEmpty(typeUrl = mUIConfig.soundTypeUrl) && !TextUtils.isEmpty(cloudUrl = mUIConfig.soundUrl)) {
            //?????????????????????
        }
        if (!TextUtils.isEmpty(typeUrl) && !TextUtils.isEmpty(cloudUrl)) {
            //??????????????????
            mMusicFragmentEx.init(useNewMusicUrl, 0, musicUrl, mUIConfig.voiceLayoutTpye,
                    mMusicListener, typeUrl, cloudUrl, true, mUIConfig.enableLocalMusic, mUIConfig.isHideDubbing(),
                    mUIConfig.mCloudAuthorizationInfo);
        } else {
            //????????????????????? 190625
            cloudUrl = mUIConfig.newCloudMusicUrl;
            boolean bNewCloud = true;
            if (TextUtils.isEmpty(cloudUrl)) {
                bNewCloud = false;
                cloudUrl = mUIConfig.cloudMusicUrl;
            }
            mMusicFragmentEx.init(useNewMusicUrl, 0, musicUrl, mUIConfig.voiceLayoutTpye,
                    mMusicListener, "", cloudUrl, bNewCloud, mUIConfig.enableLocalMusic, mUIConfig.isHideDubbing(), mUIConfig.mCloudAuthorizationInfo);
        }


        player.setOnClickListener(mOnClickListener);
        changeFragment(R.id.fl_fragment_container, mMusicFragmentEx);
    }

    @Override
    public int getCheckedModeIndex() {
        return mModeIndex;
    }

    @Override
    public void onMode(int index, SpliceModeInfo info) {
        mModeIndex = index;
        mDragMasking.setVisibility(View.VISIBLE);
        if (player.isPlaying()) {
            exitPlayerMode();
        }
        initSpliceMedia(info);
        initGridLayout();
        onMaskAnimGone();
    }

    private float mAsp = SpliceModeFragment.ASP_1;

    @Override
    public void onProportion(float asp) {
        mAsp = asp;
        mDragMasking.setVisibility(View.VISIBLE);
        int len = listPreview.size();
        for (int i = 0; i < len; i++) {
            listPreview.get(i).setIsFirst(true);
        }
        if (isPlaying()) {
            exitPlayerMode();
        }
        mPreviewFrameLayout.setAspectRatio(asp);
        initSpliceMedia(null);

        initGridLayout();
        onMaskAnimGone();

    }

    @Override
    public float getProportion() {
        return mAsp;
    }

    //??????????????????
    private boolean isModeByOrder = false;

    @Override
    public void onSpliceOrder(boolean isOrder) {
        isModeByOrder = isOrder;
        if (player.isPlaying()) {
            exitPlayerMode();
        }
    }

    @Override
    public boolean isOrderPlay() {
        return isModeByOrder;
    }

    @Override
    public List<SpliceGridMediaInfo> getMediaList() {
        return mSpliceMediaList;
    }

    @Override
    public void onRotate() {
        float angle = mCurrentEdit.getDragZoomImageView().getRotateAngle();
        mCurrentEdit.getDragZoomImageView().setRotateAngle(angle + 90);
    }


    private final float MIN_TRIM_LIMIT = 4f;

    @Override
    public void onTrim() {

        //???????????????????????????
        onSaveItem(mCurrentEdit);


        MediaObject mediaObject = mCurrentEdit.getBindGrid().getMediaObject();
        Scene scene = VirtualVideo.createScene();
        //???????????????????????????????????????????????????
        if (mediaObject.getAngle() == 90 || mediaObject.getAngle() == 270) {
            mediaObject.setClipRectF(new RectF(0, 0, mediaObject.getHeightInternal(), mediaObject.getWidthInternal()));
        } else {
            mediaObject.setClipRectF(new RectF(0, 0, mediaObject.getWidthInternal(), mediaObject.getHeightInternal()));
        }
        mediaObject.setShowRectF(new RectF(0, 0, 1, 1));
        scene.addMedia(mediaObject);
        if (mediaObject.getDuration() < MIN_TRIM_LIMIT) {
            Utils.autoToastNomal(this, getString(R.string.video_duration_too_short_to_trim, MIN_TRIM_LIMIT));
        } else {
            Intent intent = new Intent(this, TrimMediaActivity.class);
            intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
            intent.putExtra(IntentConstants.TRIM_FROM_EDIT, true);
            startActivityForResult(intent, EditPreviewActivity.REQUESTCODE_FOR_TRIM);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onReplace() {
        //??????AE??????
        SelectMediaActivity.appendMedia(this, true, UIConfiguration.ALBUM_SUPPORT_DEFAULT, 1, REQUESTCODE_FOR_REPLACE);
    }

    @Override
    public void onExitEdit() {
        mCheckedTextViewStyle.setChecked(true);
        mCheckedTextViewBorder.setChecked(false);
        mCheckedTextViewOrder.setChecked(false);
        mCheckedTextViewMusic.setChecked(false);
        int len = listPreview.size();
        for (int i = 0; i < len; i++) {
            listPreview.get(i).getDragZoomImageView().setShadowMode(false);
        }
        changeFragment(R.id.fl_fragment_container, mModeFragment);
        mBottomMenu.setVisibility(View.VISIBLE);
        mTitleBarLayout.setVisibility(View.VISIBLE);
        mCurrentEdit = null;
    }

    private float mScale = SpliceBorderFragment.MAX_SCALE;

    @Override
    public float getScale() {
        return mScale;
    }


    @Override
    public void setScale(float scale) {
        mScale = scale;
        int pW = mPreviewFrameLayout.getWidth(), pH = mPreviewFrameLayout.getHeight();
        //scale????????????????????????size ????????????????????????????????????
        float borderWidth = getBorderWidth(pW);
        int len = listPreview.size();
        for (int i = 0; i < len; i++) {
            VideoPreviewLayout item = listPreview.get(i);
            GridInfo gridInfo = item.getBindGrid().getGridInfo();
            RectF rectF = gridInfo.getRectF();
            RectF showRectF = mModel.getScaledRectF(pW, pH, rectF, borderWidth, gridInfo.isAlien());
            item.getDragZoomImageView().onBackupMatrixValue();
            item.resetChildSize(showRectF);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        nBgColor = color;
        if (isPlaying()) {
            player.setBackgroundColor(nBgColor);
        }
        mFrameLayout.setBackgroundColor(nBgColor);
    }

    @Override
    public int getBgColor() {
        return nBgColor;
    }

    /**
     * ?????????????????????
     *
     * @param pW ????????????????????????
     * @return ???????????? ( ??????????????????
     */
    private float getBorderWidth(int pW) {
        return pW * (1 - mScale) / 2.0f;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FOR_REPLACE) {
                ArrayList<String> medias = data.getStringArrayListExtra(SdkEntry.ALBUM_RESULT);
                if (medias.size() > 0) {
                    try {
                        final MediaObject mediaObject = new MediaObject(medias.get(0));
                        if (null != mCurrentEdit) {
                            SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading), false, null);
                            ThreadPoolUtils.executeEx(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap oldThumb = mCurrentEdit.getBindGrid().getThumbBmp();
                                    mModel.initItemMedia(SpliceActivitiy.this, mediaObject, mCurrentEdit.getBindGrid());
                                    mHandler.post(new ReplaceItemRunnable(oldThumb, 0));
                                }
                            });
                        }
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == VideoEditActivity.REQUSET_MUSICEX) {
                if (null != mMusicFragmentEx) {
                    mVirtualVideo.reset();
                    if (resultCode == RESULT_OK) {
                        player.reset();
                    }
                    mMusicFragmentEx.onActivityResult(requestCode, resultCode, data);
                }
            } else if (requestCode == EditPreviewActivity.REQUESTCODE_FOR_TRIM) {
                Scene newScene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                final MediaObject mediaObject = new MediaObject(newScene.getAllMedia().get(0));
                if (null != mCurrentEdit && null != mediaObject) {
                    SysAlertDialog.showLoadingDialog(this, getString(R.string.isloading), false, null);
                    ThreadPoolUtils.executeEx(new Runnable() {
                        @Override
                        public void run() {
                            int angle = mediaObject.getAngle();
                            Bitmap oldThumb = mCurrentEdit.getBindGrid().getThumbBmp();
                            mModel.initItemMedia(SpliceActivitiy.this, mediaObject, mCurrentEdit.getBindGrid());
                            mHandler.post(new ReplaceItemRunnable(oldThumb, angle));
                        }
                    });
                }
            }
        }
    }

    /**
     * ????????????
     */
    private class ReplaceItemRunnable implements Runnable {

        private Bitmap oldThumb = null;
        private int angle = 0;

        public ReplaceItemRunnable(Bitmap bmp, int angle) {
            oldThumb = bmp;
            this.angle = angle;
        }

        @Override
        public void run() {
            {
                //???????????????clip ????????????????????????
                mCurrentEdit.getBindGrid().setClipValue(null);
                Bitmap bmp = mCurrentEdit.getBindGrid().getThumbBmp();
                mCurrentEdit.getDragZoomImageView().setBitmap(bmp, angle, mModel.getTransBmp(SpliceActivitiy.this, mCurrentEdit.getBindGrid().getGridInfo().getTransPath()));
                mCurrentEdit.getDragZoomImageView().setDefaultMatrix();
            }
            mSpliceEditItemFragment.setCurrentMedia(mCurrentEdit.getBindGrid().getMediaObject());
            initFixDuration();

            if (null != oldThumb) {
                oldThumb.recycle();
                oldThumb = null;
            }
            SysAlertDialog.cancelLoadingDialog();
        }
    }

    ;

    @Override
    public void onBackPressed() {
        if (null != mSpliceEditItemFragment && mSpliceEditItemFragment.isVisible()) {
            //??????????????????
            onExitEdit();
            return;
        }

        String strMessage = getString(R.string.quit_edit);
        SysAlertDialog.showAlertDialog(this, "", strMessage,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (null != player) {
            player.setOnPlaybackListener(null);
            player.stop();
            player.cleanUp();
        }
        mVirtualVideo = null;
        if (null != mSpliceMediaList) {
            int len = mSpliceMediaList.size();
            for (int i = 0; i < len; i++) {
                mSpliceMediaList.get(i).recycle();
            }
            mSpliceMediaList.clear();
            mSpliceMediaList = null;
        }
        if (null != listPreview) {
            listPreview.clear();
            listPreview = null;
        }
        TempVideoParams.getInstance().recycle();

        mSpliceEditItemFragment = null;
        mMusicFragmentEx = null;
        if (null != mModeFragment) {
            mModeFragment.recycle();
            mModeFragment = null;
        }
        mOrderFragment = null;
        mBorderFragment = null;
        mCurrentEdit = null;
        mModel = null;
        mMusicListener = null;
        super.onDestroy();
    }


    /**
     * ??????????????????
     */
    private void reload(VirtualVideo virtualVideo) {
        Scene scene = VirtualVideo.createScene();
        int len = mSpliceMediaList.size();
        float timeLineFrom = 0;
        int pW = mPreviewFrameLayout.getWidth(), pH = mPreviewFrameLayout.getHeight();
        float borderWidth = getBorderWidth(pW);
        //mSpliceMediaList ????????? ????????????????????????????????????
        for (int i = 0; i < len; i++) {
            SpliceGridMediaInfo info = mSpliceMediaList.get(i);
            MediaObject media = info.getMediaObject().clone();
            media.setAspectRatioFitMode(KEEP_ASPECTRATIO_EXPANDING);
            GridInfo gridInfo = info.getGridInfo();
            RectF rectF = mModel.getScaledRectF(pW, pH, gridInfo.getRectF(), borderWidth, gridInfo.isAlien());
            media.setShowRectF(rectF);
            if (null != info.getClipRectF() && !info.getClipRectF().isEmpty()) {
                media.setClipRectF(info.getClipRectF());
            }
            String mask = info.getGridInfo().getGrayPath();
            if (!TextUtils.isEmpty(mask)) {//?????????????????????
                MaskObject maskObject = new MaskObject();
                maskObject.setMediaPath("asset:///" + mask);
                media.setMaskObject(maskObject);
            } else { //????????????????????????
                media.setMaskObject(null);
            }


            if (isModeByOrder) {
                //????????????
                if (media.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                    media.setIntrinsicDuration(nCountDuration);
                    media.setTimelineRange(0, nCountDuration);
                    scene.addMedia(media);
                } else {
                    //??????
                    media.setTimelineRange(timeLineFrom, timeLineFrom + media.getDuration());

                    if (timeLineFrom > 0) {
                        //??????-??????
                        initThumb(scene, info, media, mask, 0, timeLineFrom);
                    }

                    float tmp = timeLineFrom + media.getDuration();
                    //??????
                    media.setTimelineRange(timeLineFrom, tmp);
                    scene.addMedia(media);
                    if (tmp < nCountDuration) {
                        //??????-??????
                        initThumb(scene, info, media, mask, tmp, nCountDuration);
                    }
                    timeLineFrom += media.getDuration();
                }
            } else {
                //????????????
                if (media.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                    media.setIntrinsicDuration(nMaxDuration);
                }
                media.setTimelineRange(0, media.getDuration());
                scene.addMedia(media);
                float duration = (null != media) ? media.getDuration() : 0;
                initThumb(scene, info, media, mask, duration, nMaxDuration);
            }
        }
        scene.setPermutationMode(PermutationMode.COMBINATION_MODE);
        scene.setDisAspectRatio(mAsp);
        virtualVideo.addScene(scene);

    }

    /**
     * ?????????????????? ?????????mVirualVideo
     */
    private void addMusic(VirtualVideo virtualVideo) {
        virtualVideo.clearMusic();
        Music music = TempVideoParams.getInstance().getMusic();
        if (null != music) {
            music.setMixFactor(mParamDataImp.getMusicFactor());
            try {
                virtualVideo.addMusic(music);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param timeLineFrom ??????????????????????????????
     * @param timeLineTo
     */
    private void initThumb(Scene scene, SpliceGridMediaInfo info, MediaObject media, String mask, float timeLineFrom, float timeLineTo) {
        //????????????
        if (!TextUtils.isEmpty(info.getThumbPath())) {
            float tmp = timeLineTo - timeLineFrom;
            if (tmp > 0) {
                //????????????????????????
                try {
                    MediaObject thumb = new MediaObject(info.getThumbPath());
                    thumb.setIntrinsicDuration(timeLineTo - timeLineFrom);
                    //????????????????????????*******??????
                    thumb.setTimelineRange(timeLineFrom, timeLineTo);
                    thumb.setShowRectF(media.getShowRectF());
                    thumb.setAngle(media.getAngle());
                    RectF thumbClipRectF = info.getClipRectFThumb();

                    if (thumbClipRectF != null && !thumbClipRectF.isEmpty()) {
                        //?????????????????????????????????????????????????????????????????? ??????????????????????????????
                        thumb.setClipRectF(thumbClipRectF);
                    }
                    if (!TextUtils.isEmpty(mask)) {
                        MaskObject maskObject = new MaskObject();
                        maskObject.setMediaPath("asset:///" + mask);
                        thumb.setMaskObject(maskObject);
                    } else { //????????????????????????
                        media.setMaskObject(null);
                    }
                    thumb.setAspectRatioFitMode(media.getAspectRatioFitMode());
                    scene.addMedia(thumb);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * ??????clip??????
     */
    private void getClip() {
        for (VideoPreviewLayout item : listPreview) {
            onSaveItem(item);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param item
     */
    private void onSaveItem(VideoPreviewLayout item) {
        SpliceGridMediaInfo info = item.getBindGrid();
        if (item.getDragZoomImageView() != null) {
            RectF clipRectF = item.getDragZoomImageView().getClip();
            int angle = (int) item.getDragZoomImageView().getRotateAngle();
            info.getMediaObject().setAngle(angle);
            if (null != clipRectF && !clipRectF.isEmpty()) {
                info.setClipValue(item.getDragZoomImageView().getMatrixValue());
                RectF showRectF = item.getVideoRectF();
                Rect size = info.getSize();
                RectF clipVideo = mModel.fixClipRectF(size.left, size.top, clipRectF, showRectF, angle);
                RectF clipThumb = mModel.fixClipRectF(size.right, size.bottom, clipRectF, showRectF, angle);
                info.setClipRectF(clipVideo, clipThumb);
            } else {
                info.setClipRectF(null, null);
                info.setClipValue(null);
            }
        } else {
            info.setClipRectF(null, null);
            info.setClipValue(null);
        }
    }

    /**
     * ??????
     */
    private void onExport() {

        if (isPreviewUI) {
            //?????????
            if (player.isPlaying()) {
                player.pause();
            }
        } else {
            //?????????????????????
            getClip();
        }

        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                reload(virtualVideo);
                addMusic(virtualVideo);
            }
        });
        exportHandler.onExport(mAsp, true, nBgColor);
    }


    @Override
    public boolean isMediaMute() {
        return mParamDataImp.isMediaMute();
    }

    @Override
    public void reload(boolean onlyMusic) {
        if (onlyMusic) {
            pause();
            addMusic(mVirtualVideo);
            mVirtualVideo.updateMusic(player);
            start();
        } else {
            initPlayerData();
        }
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void start() {
        player.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(this, mIvVideoPlayState);
    }

    @Override
    public void pause() {
        player.pause();
        if (mCheckedTextViewMusic.isChecked()) {
            //???????????????????????????????????????
            mIvVideoPlayState.clearAnimation();
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public VirtualVideo getEditorVideo() {
        return mVirtualVideo;
    }

    @Override
    public void removeMvMusic(boolean remove) {
        mVirtualVideo.removeMVMusic(remove);
    }

    @Override
    public void onBack() {
        resetUI();
    }

    @Override
    public void onSure() {
        resetUI();
    }

    private void resetUI() {
        mTitleBarLayout.setVisibility(View.VISIBLE);
        mBottomMenu.setVisibility(View.VISIBLE);
        mCheckedTextViewMusic.setChecked(false);
        mCheckedTextViewStyle.setChecked(true);
        player.setOnClickListener(null);
        mIvVideoPlayState.setVisibility(View.GONE);
        changeFragment(R.id.fl_fragment_container, mModeFragment);
    }

    @Override
    public IParamData getParamData() {
        return mParamDataImp;
    }

    /*
     * ?????????????????????
     */
    private MusicFragmentEx.IMusicListener mMusicListener = new MusicFragmentEx.IMusicListener() {

        @Override
        public void onVoiceChanged(boolean isChecked) {
            if (isChecked) {
                mParamDataImp.setMediaMute(false);
            } else {
                mParamDataImp.setMediaMute(true);
            }
            int len = mSpliceMediaList.size();
            for (int i = 0; i < len; i++) { //??????????????????
                MediaObject item = mSpliceMediaList.get(i).getMediaObject();
                if (item.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    item.setAudioMute(mParamDataImp.isMediaMute());
                }
            }
            initPlayerData();
        }

        @Override
        public void onVoiceClick(View v) {
        }

    };

}
