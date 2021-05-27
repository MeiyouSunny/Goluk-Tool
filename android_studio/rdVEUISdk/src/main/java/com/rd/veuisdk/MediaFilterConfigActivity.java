package com.rd.veuisdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.fragment.FilterConfigFragment;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IMediaFilter;
import com.rd.veuisdk.utils.IMediaParamImp;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 片段编辑->调色
 */
public class MediaFilterConfigActivity extends BaseActivity implements IMediaFilter {

    /**
     * 片段编辑调色调色
     */
    static void onFilterConfig(Context context, Scene scene, int requestCode) {
        onTon(context, scene, false, requestCode);
    }


    /**
     * sdk-调色
     *
     * @param needExport 是否直接导出视频
     */
    public static void onTon(Context context, Scene scene, boolean needExport, int requestCode) {
        Intent intent = new Intent(context, MediaFilterConfigActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
        intent.putExtra(IntentConstants.INTENT_NEED_EXPORT, needExport);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    private PreviewFrameLayout mPflVideoPreview;
    private TextView mTvVideoDuration;
    private ImageView mIvVideoPlayState;
    private VirtualVideoView mMediaPlayer;
    private RdSeekBar mPbPreview;
    private int mLastPlayPostion;
    private boolean mIsAutoRepeat = true;
    private VirtualVideo virtualVideo;
    private FilterConfigFragment mFilterConfigFragment;
    private TextView tvFilterName;
    private IMediaParamImp mMediaParamImp;
    private Scene mScene;
    private boolean bExportVideo = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_filter_config);
        tvFilterName = $(R.id.tvFilterName);
        TAG = "MediaFilterConfigActivity";
        mStrActivityPageName = getString(R.string.toning);
        virtualVideo = new VirtualVideo();
        bExportVideo = getIntent().getBooleanExtra(IntentConstants.INTENT_NEED_EXPORT, false);
        mScene = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
        Object tmp = mScene.getAllMedia().get(0).getTag();
        if (null != tmp && tmp instanceof VideoOb) {
            IMediaParamImp tmpImp = ((VideoOb) tmp).getMediaParamImp();
            if (null != tmpImp) {
                mMediaParamImp = tmpImp.clone();
            } else {
                mMediaParamImp = new IMediaParamImp();
            }
        } else {
            mMediaParamImp = new IMediaParamImp();
        }
        mFilterConfigFragment = FilterConfigFragment.newInstance();
        if (bExportVideo) {
            mFilterConfigFragment.setEnableApplyToAll(false);
        }
        initView();
        onLoad();
        playVideo();
        changeFragment(mFilterConfigFragment);

        $(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        $(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseVideo();
                onSure();
            }
        });


    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_fragment_container, fragment);
        ft.commit();
    }

    /**
     * 加载媒体资源
     */
    private void onLoad() {
        mPbPreview.setHighLights(null);
        virtualVideo.reset();
        try {
            virtualVideo.addScene(mScene);
            virtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener mOnPlayerClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    };

    private VirtualVideoView.VideoViewListener mPlayerListener = new VirtualVideoView.VideoViewListener() {
        private float lastPosition;

        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            lastPosition = -1;
            int ms = Utils.s2ms(mMediaPlayer.getDuration());
            mPbPreview.setMax(ms);
            mTvVideoDuration.setText(DateTimeUtils.stringForMillisecondTime(
                    ms, true, true));
            updatePreviewFrameAspect(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            onToast(getString(R.string.preview_error));
            onBackPressed();
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            onComplete();
        }

        @Override
        public void onGetCurrentPosition(float position) {
            if (position < lastPosition && mIsAutoRepeat) {
                lastPosition = -1;
                mIsAutoRepeat = false;
                pauseVideo();
                onComplete();
            } else {
                lastPosition = position;
                mPbPreview.setProgress(Utils.s2ms(position));
            }
        }
    };

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0: {

                }
                break;
                default: {
                }
                break;
            }
        }
    };
    private Runnable mTouchEndRunnable = new Runnable() {
        @Override
        public void run() {
            if (null != tvFilterName) {
                tvFilterName.setText("");
                tvFilterName.setVisibility(View.GONE);
            }
            if (null != mFilterConfigFragment) {
                mFilterConfigFragment.onDiffEnd();
            }
        }
    };

    private void initView() {
        mPflVideoPreview = $(R.id.rlPreview);
        mTvVideoDuration = $(R.id.tvEditorDuration);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mMediaPlayer = $(R.id.epvPreview);


        TextView title = $(R.id.tvBottomTitle);
        title.setText(mStrActivityPageName);
        $(R.id.btnDiff).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mHandler.removeCallbacks(mTouchEndRunnable);
                    mFilterConfigFragment.onDiffBegin();
                    tvFilterName.setText(R.string.toning_diff_msg);
                    tvFilterName.setVisibility(View.VISIBLE);
                } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    mHandler.removeCallbacks(mTouchEndRunnable);
                    mHandler.postDelayed(mTouchEndRunnable, 200);
                }
                return true;
            }
        });

        mPflVideoPreview.setClickable(true);
        mLastPlayPostion = -1;

        mMediaPlayer = $(R.id.epvPreview);
        mMediaPlayer.setClearFirst(true);
        mMediaPlayer.setAutoRepeat(mIsAutoRepeat);
        mMediaPlayer.setOnClickListener(mOnPlayerClickListener);
        mMediaPlayer.setOnPlaybackListener(mPlayerListener);
        mMediaPlayer.setOnInfoListener(new VirtualVideo.OnInfoListener() {
            @Override
            public boolean onInfo(int what, int extra, Object obj) {

                if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
                } else if (what == VirtualVideo.INFO_WHAT_GET_VIDEO_HIGHTLIGHTS) {
                    int[] ls = (int[]) obj;
                    mPbPreview.setHighLights(ls);
                }
                return false;
            }
        });
        mPbPreview = (RdSeekBar) $(R.id.pbPreview);
        mPbPreview.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean mLastPlaying;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(Utils.ms2s(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if ((mLastPlaying = mMediaPlayer.isPlaying())) {
                    mMediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mLastPlaying) {
                    mMediaPlayer.start();
                }
            }
        });
    }


    private void onComplete() {
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
        mMediaPlayer.seekTo(0);
        mPbPreview.setProgress(0);
        mMediaPlayer.setAutoRepeat(mIsAutoRepeat);
    }

    /**
     * 更新预览视频播放器比例
     */
    protected void updatePreviewFrameAspect(int nVideoWidth, int nVideoHeight) {

        mPflVideoPreview.setAspectRatio((float) nVideoWidth / nVideoHeight);

    }


    @Override
    public void clickView(View v) {
        super.clickView(v);
        int id = v.getId();
        if (id == R.id.rlPreview) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    }

    /**
     * 确认
     */
    private void onSure() {
        MediaObject src = mScene.getAllMedia().get(0);
        Object object = src.getTag();
        VideoOb tmp = null;
        if (object instanceof VideoOb) {
            tmp = (VideoOb) object;
        } else {
            tmp = new VideoOb(src);
        }
        tmp.setMediaParamImp(mMediaParamImp);

        MediaObject mMedia = src.clone();
        try {
            mMedia.changeFilterList(Utils.getFilterList(mMediaParamImp));
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

        mMedia.setTag(tmp);


        Scene scene = mScene.clone();
        scene.getAllMedia().clear();
        scene.addMedia(mMedia);
        if (bExportVideo) {
            onExport(scene);
        } else {
            Intent data = new Intent();
            //必须创建一个新的对象
            data.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
            /**
             * 是否应用于全部片段
             */
            boolean isAllParet = mFilterConfigFragment.onSure();
            if (isAllParet) {
                //所有片段都要使用这个调色效果
                data.putExtra(IntentConstants.INTENT_ALL_APPLY, isAllParet);
            }
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * 导出视频
     */
    private void onExport(final Scene scene) {
        ExportHandler exportHandler = new ExportHandler(this, new ExportHandler.IExport() {
            @Override
            public void addData(VirtualVideo virtualVideo) {
                virtualVideo.addScene(scene);
            }
        });
        exportHandler.onExport(mMediaPlayer.getWidth() / (float) mMediaPlayer.getHeight(), true);
    }

    @Override
    public void onBackPressed() {
        if (hasChanged) {
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
                            //返回值统一
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    });
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mTouchEndRunnable);
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
            mLastPlayPostion = Utils.s2ms(mMediaPlayer.getCurrentPosition());
            pauseVideo();
        }
    }

    @Override
    protected void onDestroy() {
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        if (null != virtualVideo) {
            virtualVideo.release();
        }
        super.onDestroy();
        mFilterConfigFragment = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayer.start();
        // 从后台切换到前台，并且已经成功打开正常的视频，进行继续播放
        if (mLastPlayPostion == 0) {
            mMediaPlayer.seekTo(0);
        }
        if (mLastPlayPostion > 0) {
            if (null != mMediaPlayer) {
                mMediaPlayer.seekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
                playVideo();
            }
        }
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
        mIvVideoPlayState.clearAnimation();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        mIvVideoPlayState.setVisibility(View.VISIBLE);
    }


    public void start() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.start();

        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        ViewUtils.fadeOut(this, mIvVideoPlayState);
    }


    @Override
    public IMediaParamImp getFilterConfig() {
        return mMediaParamImp;
    }

    private String filterName;

    @Override
    public void onStartTrackingTouch(int textId, float value) {
        tvFilterName.setVisibility(View.VISIBLE);
        filterName = getString(textId);
        String p = decimalFormat.format(value);//format 返回的是字符串
        tvFilterName.setText(filterName + " " + p);
    }

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
    private boolean hasChanged = false;  // 是否有改动

    @Override
    public void onProgressChanged(float value) {
        hasChanged = true;
        String p = decimalFormat.format(value);//format 返回的是字符串
        tvFilterName.setText(filterName + " " + p);
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                start();
            }
            try {
                List<VisualFilterConfig> tmp = Utils.getFilterList(mMediaParamImp);
                mScene.getAllMedia().get(0).changeFilterList(tmp);
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStopTrackingTouch() {
        tvFilterName.setVisibility(View.GONE);
        if (!mMediaPlayer.isPlaying()) {
            start();
        }
        try {
            List<VisualFilterConfig> tmp = Utils.getFilterList(mMediaParamImp);
            mScene.getAllMedia().get(0).changeFilterList(tmp);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


}
