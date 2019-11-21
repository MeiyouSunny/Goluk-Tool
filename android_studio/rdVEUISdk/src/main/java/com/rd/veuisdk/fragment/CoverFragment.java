package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rd.lib.utils.BitmapUtils;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.CropRotateMirrorActivity;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.VideoEditActivity;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.util.ArrayList;

/**
 * 封面
 */
public class CoverFragment extends BaseFragment {

    public static final float DEFAULT_COVER_DURATION = 0.3f; //封面默认时长

    public static CoverFragment newInstance() {

        Bundle args = new Bundle();

        CoverFragment fragment = new CoverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private IVideoEditorHandler mVideoEditorHandler;
    private VirtualVideo mVirtualVideo;
    private float mAsp;
    private int mDuration;
    private View addLayout, menuLayout;
    private IParamData mParamData;
    private CaptionLiteObject mCoverCaptionBackup; //之前设置的封面

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (IVideoEditorHandler) context;
        mParamData = ((IParamHandler) context).getParamData();
        mDuration = mVideoEditorHandler.getDuration();
        mCoverCaptionBackup = mParamData.getCoverCaption();
    }

    private TextView tvProgress, tvDuration;
    private DisplayMetrics mDisplayMetrics;
    private ImageView ivPlayState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_cover, container, false);
        mDisplayMetrics = CoreUtils.getMetrics();
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        addLayout = $(R.id.cover_add_layout);
        menuLayout = $(R.id.add_video_layout);
        ivPlayState = $(R.id.ivPlayerState);
        mTimelineHorizontalScrollView = $(R.id.priview_sticker_line);
        mLinearLayout = $(R.id.subtitleline_media);
        mThumbNailLine = $(R.id.subline_view);
        mVideoEditorHandler.registerEditorPostionListener(mPositionListener);
        tvProgress = $(R.id.split_item_progress);
        tvDuration = $(R.id.tvEnd);
        return mRoot;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reset();
        setProgressText(mVideoEditorHandler.getCurrentPosition());
        setDurationText(mVideoEditorHandler.getDuration());
        $(R.id.recycleParent).setVisibility(View.GONE);
        TextView tvTitle = $(R.id.tvTitle);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.cover);
        initListener();


    }

    private void initListener() {
        ivPlayState.setOnClickListener(onStateChangeListener);
        mTimelineHorizontalScrollView.addScrollListener(mScrollViewListener);
        mTimelineHorizontalScrollView.setViewTouchListener(new IViewTouchListener() {
            @Override
            public void onActionDown() {

            }

            @Override
            public void onActionMove() {

            }

            @Override
            public void onActionUp() {
                mTimelineHorizontalScrollView.resetForce();
                int progress = mTimelineHorizontalScrollView.getProgress();
                setProgressText(progress);
            }
        });
        $(R.id.tvAddVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLayout.setVisibility(View.GONE);
                menuLayout.setVisibility(View.VISIBLE);
                initData();
            }
        });
        $(R.id.tvAddAblum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedia();
            }
        });
        $(R.id.tvAddText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        $(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMenuLayoutVisible()) {
                    getVirtualCover();
                } else {
                    onSure();
                }
            }
        });
    }

    private void onSure() {
        mVideoEditorHandler.onSure();
        mVideoEditorHandler.seekTo(0);
        mVideoEditorHandler.start();
    }

    /**
     * 调整播放那个状态
     */
    private View.OnClickListener onStateChangeListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mVideoEditorHandler.isPlaying()) {
                pauseVideo();
            } else {
                if (Math.abs(mVideoEditorHandler.getCurrentPosition() - mVideoEditorHandler.getDuration()) < 300) {
                    mVideoEditorHandler.seekTo(0);
                }
                playVideo();
            }
        }
    };
    private TimelineHorizontalScrollView mTimelineHorizontalScrollView;
    private LinearLayout mLinearLayout;

    private void initData() {
        onInitThumbTimeLine();
        onInitThumbTimeLine(mVirtualVideo);

    }


    public void initThumbnail(VirtualVideo virtualVideo, float asp) {
        mVirtualVideo = virtualVideo;
        mAsp = asp;
    }

    /**
     * 封面属性
     */
    private void fixCover(String path) {
        try {
            CaptionLiteObject captionLiteObject = new CaptionLiteObject(null, path);
            captionLiteObject.setShowRectF(new RectF(0, 0, 1, 1));
            captionLiteObject.setTimelineRange(0, DEFAULT_COVER_DURATION);
            mParamData.setCoverCaption(captionLiteObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final int MSG_COVER = 1900;
    private final int MSG_ARG1_BUILD = 200;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_COVER: {
                    pauseVideo();
                    fixCover((String) msg.obj);
                    onSure();
                    reset();
                }
                break;
                default:
                    break;
            }
        }
    };


    public static final int REQUST_ABLUM_COVER = 600;
    public static final int REQUST_CROP_COVER = 601;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUST_CROP_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                if (null != scene) {
                    final VirtualVideo virtualVideo = new VirtualVideo();
                    final float asp = getPreviewAsp();
                    scene.setDisAspectRatio(asp);
                    virtualVideo.addScene(scene);
                    final float nTime = scene.getDuration() * 2 / 3;
                    ThreadPoolUtils.executeEx(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = Bitmap.createBitmap(500, (int) (500 / asp), Bitmap.Config.ARGB_8888);
                            if (virtualVideo.getSnapshot(getContext(), nTime, bitmap)) {
                                String path = getCoverPath();
                                try {
                                    BitmapUtils.saveBitmapToFile(bitmap, true, 100, path);
                                    mHandler.obtainMessage(MSG_COVER, MSG_ARG1_BUILD, 0, path).sendToTarget();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            bitmap.recycle();
                        }
                    });
                    //因为onResume 无法实时插入，需要等到播放器prepared，索性 整体reload （防止界面闪烁）
                }
            }
        } else if (requestCode == REQUST_ABLUM_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<String> medias = data.getStringArrayListExtra(SdkEntry.ALBUM_RESULT);
                if (null != medias && medias.size() > 0) {
                    if (getActivity() instanceof VideoEditActivity) {
                        ((VideoEditActivity) getActivity()).setResumeSeekto(false);
                    }
                    try {
                        MediaObject mMediaCover = new MediaObject(medias.get(0));
                        mMediaCover.setClearImageDefaultAnimation(true);
                        Scene scene = VirtualVideo.createScene();
                        scene.addMedia(mMediaCover);
                        CropRotateMirrorActivity.onAECropRotateMirror(getContext(), scene, getPreviewAsp(), true, false, REQUST_CROP_COVER);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private float getPreviewAsp() {
        int videoWidth = mVideoEditorHandler.getEditor().getVideoWidth();
        int videoHeight = mVideoEditorHandler.getEditor().getVideoHeight();
        return videoWidth / (videoHeight + .0f);
    }


    /**
     * 添加媒体
     */
    private void addMedia() {
        SelectMediaActivity.appendMedia(getContext(), true, UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY, 1, REQUST_ABLUM_COVER);
    }

    private boolean isMenuLayoutVisible() {
        return menuLayout.getVisibility() == View.VISIBLE;
    }

    private void reset() {
        menuLayout.setVisibility(View.GONE);
        addLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public int onBackPressed() {
        if (isMenuLayoutVisible()) {
            reset();
            return -1;
        } else {
            mParamData.setCoverCaption(mCoverCaptionBackup);
            mVideoEditorHandler.onBack();
            return super.onBackPressed();
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVideoEditorHandler.unregisterEditorProgressListener(mPositionListener);
    }


    private void playVideo() {
        mVideoEditorHandler.start();
        ivPlayState.setImageResource(R.drawable.edit_music_pause);
    }

    private void pauseVideo() {
        mVideoEditorHandler.pause();
        ivPlayState.setImageResource(R.drawable.edit_music_play);
    }

    private ThumbNailLine mThumbNailLine;
    private int mHalfWidth;
    private int mStateSize = 0;

    private void onInitThumbTimeLine() {
        mHalfWidth = mDisplayMetrics.widthPixels / 2;
        mTimelineHorizontalScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        int duration = Utils.s2ms(mVirtualVideo.getDuration());
        int[] mSizeParams = mThumbNailLine.setDuration(duration, mTimelineHorizontalScrollView.getHalfParentWidth());
        mTimelineHorizontalScrollView.setLineWidth(mSizeParams[0]);
        mTimelineHorizontalScrollView.setDuration(duration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mSizeParams[0]
                + 2 * mThumbNailLine.getpadding(), mSizeParams[1]);

        lp.setMargins(mTimelineHorizontalScrollView.getHalfParentWidth() - mThumbNailLine.getpadding(),
                0, mHalfWidth - mThumbNailLine.getpadding(), 0);

        mThumbNailLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new FrameLayout.LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);
        mLinearLayout.setLayoutParams(lframe);
        $(R.id.word_hint_view).setVisibility(View.GONE);

    }

    /**
     * 初始化缩略图时间轴和恢复数据
     *
     * @param virtualVideo
     */
    private void onInitThumbTimeLine(VirtualVideo virtualVideo) {
        mThumbNailLine.setVirtualVideo(virtualVideo, false);
        mThumbNailLine.prepare(mTimelineHorizontalScrollView.getHalfParentWidth() + mHalfWidth);
        mHandler.postDelayed(resetSubDataRunnable, 100);
    }

    private Runnable resetSubDataRunnable = new Runnable() {
        @Override
        public void run() {
            resetThumbData();
        }
    };

    /**
     * 恢复时间轴的数据
     */
    private void resetThumbData() {
        onScrollProgress(0);
        mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
    }


    private ScrollViewListener mScrollViewListener = new ScrollViewListener() {

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY, boolean appScroll) {

            mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
            if (!appScroll) {
                int progress = mTimelineHorizontalScrollView.getProgress();
                pauseVideo();
                setProgressText(progress);
            }
        }

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY, boolean appScroll) {
            mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
            if (!appScroll) {
                int progress = mTimelineHorizontalScrollView.getProgress();
                mVideoEditorHandler.seekTo(progress);
                setProgressText(progress);
            }
        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY, boolean appScroll) {
            int progress = mTimelineHorizontalScrollView.getProgress();
            mThumbNailLine.setStartThumb(mTimelineHorizontalScrollView.getScrollX());
            if (!appScroll) {
                mVideoEditorHandler.seekTo(progress);
            }
            setProgressText(progress);
        }
    };

    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }


    private int getScrollX(long progress) {
        return (int) (progress * (mThumbNailLine.getThumbWidth() / mDuration));
    }

    /**
     * 播放完成
     */
    private void onScrollCompleted() {
        onScrollTo(0);
        setProgressText(0);
        ivPlayState.setImageResource(R.drawable.edit_music_play);
    }


    /**
     * 设置播放进度
     *
     * @param mScrollX 像素
     */
    private void onScrollTo(int mScrollX) {

        mTimelineHorizontalScrollView.appScrollTo(mScrollX, true);

    }

    private void setProgressText(int progress) {
        tvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
    }

    private void setDurationText(int progress) {
        tvDuration.setText(DateTimeUtils.stringForMillisecondTime(progress));
    }

    private String getCoverPath() {
        return PathUtils.getTempFileNameForSdcard("Temp_virtual_cover", "png");
    }

    /***
     * 从虚拟视频中获取指定时间点的画面作为封面
     */
    private void getVirtualCover() {
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                //必须子线程取图
                Bitmap bitmap = Bitmap.createBitmap(300, (int) (300 / mAsp), Bitmap.Config.ARGB_8888);
                if (mVirtualVideo.getSnapshot(mContext, Utils.ms2s(mVideoEditorHandler.getCurrentPosition()), bitmap)) {
                    String path = getCoverPath();
                    try {
                        com.rd.lib.utils.BitmapUtils.saveBitmapToFile(bitmap, true, 100, path);
                        bitmap.recycle();
                        mHandler.obtainMessage(MSG_COVER, path).sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    bitmap.recycle();
                }
            }
        });
    }

    private IVideoEditorHandler.EditorPreivewPositionListener mPositionListener = new IVideoEditorHandler.EditorPreivewPositionListener() {
        @Override
        public void onEditorPrepred() {
            mDuration = mVideoEditorHandler.getDuration();
            setDurationText(mDuration);
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
        }

        @Override
        public void onEditorPreviewComplete() {
            onScrollCompleted();

        }
    };
}
