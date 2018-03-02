package com.mobnote.videoedit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.photoalbum.PhotoAlbumPlayer;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.videoedit.adapter.AEMusicAdapter;
import com.mobnote.videoedit.adapter.ChannelLineAdapter;
import com.mobnote.videoedit.bean.ChunkBean;
import com.mobnote.videoedit.bean.DummyFooterBean;
import com.mobnote.videoedit.bean.DummyHeaderBean;
import com.mobnote.videoedit.bean.ProjectItemBean;
import com.mobnote.videoedit.bean.TailBean;
import com.mobnote.videoedit.bean.TransitionBean;
import com.mobnote.videoedit.constant.VideoEditConstant;
import com.mobnote.videoedit.utils.DeviceUtil;
import com.mobnote.videoedit.utils.VideoEditUtils;
import com.mobnote.videoedit.view.VideoEditExportDialog;
import com.mobnote.view.RingViewDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.AfterEffectListener;
import cn.npnt.ae.exceptions.EffectException;
import cn.npnt.ae.exceptions.EffectRuntimeException;
import cn.npnt.ae.exceptions.InvalidLengthException;
import cn.npnt.ae.exceptions.InvalidVideoSourceException;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.Project;
import cn.npnt.ae.model.Transition;
import cn.npnt.ae.model.VideoEncoderCapability;

public class AfterEffectActivity extends BaseActivity implements AfterEffectListener, View.OnClickListener {
    RecyclerView mAERecyclerView;
    LinearLayoutManager mAELayoutManager;
    RecyclerView mAEMusicRecyclerView;
    LinearLayoutManager mAEMusicLayoutManager;

    private GLSurfaceView mGLSurfaceView;
    AfterEffect mAfterEffect;
    Project mProject;
    int mImageHeight;
    int mImageWidth;
    int mTransitionWidth;
    List<ProjectItemBean> mProjectItemList;
    Handler mAfterEffecthandler;
    private ChannelLineAdapter mChannelLineAdapter;
    private FrameLayout mSurfaceLayout;
    private ImageButton mBackBTN;

    ImageView mVideoThumeIv;
    ImageView mVideoPlayIV;
    float mChunksTotalTime;
    RelativeLayout mAEVolumeSettingLayout;
    ImageView mAEVolumeSettingIv;
    TextView mAEVolumePercentTv;
    SeekBar mAEVolumeSeekBar;

    LinearLayout mAEEditController;
    LinearLayout mAESplitAndDeleteLayout;
    LinearLayout mAESplitLayout;
    LinearLayout mAEDeleteLayout;
    LinearLayout mAECutLayout;
    LinearLayout mAEVolumeLayout;
    ImageView mAECutIV;
    TextView mAECutTV;
    ImageView mAEVolumeIV;
    TextView mAEVolumeTV;
    int mDummyHeaderWidth;
    private TextView mNextTV;
    AEMusicAdapter mAEMusicAdapter;
    private String mExportQuality;
    private int mTimeLineLastX;

    private final static String TAG = "AfterEffectActivity";
    private int mCurrentPointedItemIndex;
    float mPlayingChunkPosition = 0f;

    private View mTimeLineGateV;
    private int mGateLocationX;
    private CustomLoadingDialog mFullLoadingDialog;
    private int mTailWidth;
    private View mTimeLineWrapperRL;
    String mVideoPath;
    private RingViewDialogFragment mExportingDialog;
    private AlertDialog.Builder mExportFailDialogBuilder;
    private AlertDialog mExportFailDialog;

    public final static int MSG_AE_PLAY_STARTED = 1001;
    public final static int MSG_AE_PLAY_PROGRESS = 1002;
    public final static int MSG_AE_PLAY_FINISHED = 1003;
    public final static int MSG_AE_PLAY_FAILED = 1004;
    public final static int MSG_AE_EXPORT_STARTED = 1005;
    public final static int MSG_AE_EXPORT_PROGRESS = 1006;
    public final static int MSG_AE_EXPORT_FINISHED = 1007;
    public final static int MSG_AE_EXPORT_FAILED = 1008;
    public final static int MSG_AE_THUMB_GENERATED = 1009;
    public final static int MSG_AE_CHUNK_ADD_FINISHED = 1010;
    public final static int MSG_AE_CHUNK_ADD_FAILED = 1011;
    public final static int MSG_AE_BITMAP_READ_OUT = 1012;
    public final static int MSG_AE_PLAY_PAUSED = 1013;
    public final static int MSG_AE_PLAY_RESUMED = 1014;
    public final static int MSG_AE_CHUNK_PLAY_END = 1015;
    public final static int MSG_AE_BITMAP_READ_FAILED = 1016;
    public final static int MSG_AE_MUSIC_START = 1017;
    public final static int MSG_AE_MUSIC_FAILED = 1018;
    public final static int MSG_AE_MUSIC_STARTED = 1019;

    enum PlayerState {
        INITED,
        STOPPED,
        PLAYING,
        PAUSED,
        RELEASED,
        ERROR
    }

    private PlayerState mPlayerState;
    final String[] mMusicPaths = {
            "none",
            "goluk_music/01-fasion-48khz-128kbps-final.mp3",
            "goluk_music/02-discover-48khz-128kbps-final.mp3",
            "goluk_music/03-no_effect-48khz-128kbps-final.mp3",
            "goluk_music/04-memory-48khz-128kbps-final.mp3",
            "goluk_music/05-street-48khz-128kbps-final.mp3",
            "goluk_music/06-travel-48khz-128kbps-final.mp3",
            "goluk_music/07-fresh-48khz-128kbps-final.mp3",
            "goluk_music/08-crual-48khz-128kbps-final.mp3"};

    String[] mMusicNames;
    final int[] mMusicCoversNormal = {
//            R.drawable.no_music,
//            R.drawable.music_wave,
//            R.drawable.music_discover,
//            R.drawable.music_humor,
//            R.drawable.music_memory,
//            R.drawable.music_crowd,
//            R.drawable.music_travel,
//            R.drawable.music_fresh,
//            R.drawable.music_wild
    };

    final int[] mMusicCoversSelected = {
            R.drawable.ic_ae_cd_selected,
//            R.drawable.music_selected_wave,
//            R.drawable.music_selected_discover,
//            R.drawable.music_selected_humor,
//            R.drawable.music_selected_memory,
//            R.drawable.music_selected_crowd,
//            R.drawable.music_selected_travel,
//            R.drawable.music_selected_fresh,
//            R.drawable.music_selected_wild
    };

    public int getTailWidth() {
        return mTailWidth;
    }

    public void moveChunk2Gate(final int index) {
        ProjectItemBean bean = mProjectItemList.get(index);
        if(!(bean instanceof ChunkBean)) {
            return;
        }

        if(VideoEditUtils.judgeChunkOverlap(mAELayoutManager, mGateLocationX, index)) {
            return;
        }

        mAERecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mAELayoutManager.scrollToPositionWithOffset(index, mDummyHeaderWidth);
            }
        });
    }

    public void addChunk(String videoPath) {
        // always add from end
        int addFlag = -1;
        // if no chunk added, then the init data would be header, footer, tail
//		if(mProjectItemList == null || mProjectItemList.size() <= 3) {
//			addFlag = 0;
//		}

        if (mVideoPath != null) {
            mFullLoadingDialog.show();
            try {
                mAfterEffect.editAddChunk(videoPath, addFlag);
            } catch (Exception e) {
                if(null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
                    mFullLoadingDialog.close();
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除某个chunk片段
     *
     * @param chunkIndex
     */
    public void removeChunk(int chunkIndex) {
        if (mProjectItemList == null || mProjectItemList.size() <= 3) {
            return;
        }

        try {
            mAfterEffect.editRemoveChunk(chunkIndex);
        } catch (EffectRuntimeException e) {
            return;
        }
    }

    public void splitChunk() {
        if (mProjectItemList == null || mProjectItemList.size() <= 3) {
            return;
        }

        int focusIndex = mChannelLineAdapter.getEditIndex();
        if (focusIndex == -1) {
            Toast.makeText(this, getString(R.string.str_ae_no_chunk_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!VideoEditUtils.judgeChunkOverlap(mAELayoutManager, mGateLocationX, mChannelLineAdapter.getEditIndex())) {
            Toast.makeText(this, getString(R.string.str_ae_only_selected_can_split), Toast.LENGTH_SHORT).show();
            return;
        }

        ProjectItemBean bean = mProjectItemList.get(mCurrentPointedItemIndex);
        if (!(bean instanceof ChunkBean)) {
            Toast.makeText(this, getString(R.string.str_ae_selected_chunk_can_not_split), Toast.LENGTH_SHORT).show();
            return;
        }

        View chunkView = mAELayoutManager.findViewByPosition(mCurrentPointedItemIndex);
        float width = chunkView.getWidth();
        float pX = VideoEditUtils.getViewXLocation(chunkView);

        int chunkIndex = VideoEditUtils.mapI2CIndex(mCurrentPointedItemIndex);
        float position = (mGateLocationX - pX) / width * ((ChunkBean) bean).chunk.getDuration();
        if (position == 0f) {
            String org = getString(R.string.str_ae_can_not_split_from);
            String formattedOrg = String.format(org, 0);
            Toast.makeText(this, formattedOrg, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mAfterEffect.canSplit(chunkIndex, position)) {
            DecimalFormat f_num = new DecimalFormat("##0.00");
            String ret = f_num.format(position);
            String org = getString(R.string.str_ae_can_not_split_from);
            String formattedOrg = String.format(org, ret);
            Toast.makeText(this, formattedOrg, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float realPosition = mAfterEffect.editSplitChunk(chunkIndex, position);
            ZhugeUtils.eventChunkSplit(this);

            int itemIndex = VideoEditUtils.mapC2IIndex(chunkIndex);
            List<Chunk> mainChunks = mAfterEffect.getMainChunks();

            Chunk first = mainChunks.get(chunkIndex);
            Chunk second = mainChunks.get(chunkIndex + 1);

            // Get to insert index
            ChunkBean chunkBean1 = new ChunkBean();
            chunkBean1.chunk = first;
            chunkBean1.index_tag = VideoEditUtils.generateIndexTag();
            chunkBean1.width = VideoEditUtils.ChunkTime2Width(first);
            chunkBean1.isEditState = false;
            chunkBean1.curVolume = (int) (first.getVolume() * 100);

            chunkBean1.ct_pair_tag = itemIndex + "chunkIndex";
            mProjectItemList.set(itemIndex, chunkBean1);

            // ignore transition, the same since now

            // insert second chunk
            ChunkBean chunkBean2 = new ChunkBean();
            chunkBean2.chunk = second;
            chunkBean2.index_tag = VideoEditUtils.generateIndexTag();
            chunkBean2.width = VideoEditUtils.ChunkTime2Width(second);
            chunkBean2.isEditState = true;
            chunkBean2.curVolume = (int) (second.getVolume() * 100);

//			int cInsertIndex = mProjectItemList.size() - 2;
            chunkBean2.ct_pair_tag = (itemIndex + 2) + "chunkIndex";
            mProjectItemList.add(itemIndex + 2, chunkBean2);

            // truncate to the end
            Transition transtion = mAfterEffect.getTransition(chunkIndex + 1, true);
//			if(transtion != null) {
            TransitionBean transitionBean = new TransitionBean();
            transitionBean.index_tag = VideoEditUtils.generateIndexTag();
            transitionBean.transiton = transtion;
            transitionBean.ct_pair_tag = (itemIndex + 2) + "chunkIndex";
            mProjectItemList.add(itemIndex + 3, transitionBean);
            mChannelLineAdapter.setEditIndex(itemIndex + 2);

            VideoEditUtils.refreshCTTag(mProjectItemList);
        } catch (EffectRuntimeException e) {
            e.printStackTrace();
            return;
        } catch (InvalidLengthException e) {
            e.printStackTrace();
            return;
        }
        mChannelLineAdapter.notifyDataSetChanged();
    }

    public void playOrPause() {
        if (mPlayerState == PlayerState.PLAYING) {
            mVideoPlayIV.setVisibility(View.VISIBLE);
            mAfterEffect.playPause();
        } else if (mPlayerState == PlayerState.PAUSED) {
            mVideoPlayIV.setVisibility(View.GONE);
            mAfterEffect.playResume();
        } else {
            if (mVideoThumeIv.getVisibility() == View.VISIBLE) {
                mVideoThumeIv.setVisibility(View.GONE);
            }

            if (mVideoPlayIV.getVisibility() == View.VISIBLE) {
                mVideoPlayIV.setVisibility(View.GONE);
            }

            //当前播放进度大于0，则从当前位置开始播放，否则从头开始播放
            try {
                mAfterEffect.play();
            } catch (InvalidVideoSourceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        if(mPlayerState == PlayerState.PLAYING) {
            mVideoPlayIV.setVisibility(View.VISIBLE);
            mAfterEffect.playPause();
        }
    }

    private void initPlayer() {
        mVideoThumeIv = (ImageView) findViewById(R.id.iv_video_thumb);
        mVideoPlayIV = (ImageView) findViewById(R.id.iv_video_play);

        mVideoThumeIv.setOnClickListener(this);
        mVideoPlayIV.setOnClickListener(this);

        mSurfaceLayout = (FrameLayout) findViewById(R.id.fl_video_sur_layout);
        mGLSurfaceView = new GLSurfaceView(this);
        mSurfaceLayout.addView(mGLSurfaceView, 0);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

        int screenWidthSize = DeviceUtil.getScreenWidthSize(this);
        int width = screenWidthSize;
        int height = width / 16 * 9;
        LayoutParams params = mSurfaceLayout.getLayoutParams();
        params.height = height;

        mGLSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPause();
            }
        });

        mAfterEffect = new AfterEffect(this, mGLSurfaceView, this, width, height);

        addTail();
        mProject = mAfterEffect.getProject();

        mAfterEffecthandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    default: {
                        if (msg.what >= 1000 && msg.what < 2000) {
                            handlerAECallBack(msg);
                        }
                        break;
                    }
                }
            }
        };

        addChunk(mVideoPath);
    }

    private void finishAfterEffect() {
        if(null != mAfterEffect) {
            mAfterEffect.playStop();
            mPlayerState = PlayerState.RELEASED;
            mAfterEffect.release();
            mAfterEffect = null;
        }
        finish();
    }

    private void addTail() {
        InputStream istr = null;
        GolukApplication mApp = GolukApplication.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new java.util.Date());
        String nickName = null;
        if (mApp.isUserLoginSucess) {
            UserInfo userInfo = mApp.getMyInfo();
            nickName = userInfo.nickname;
        } else {
            nickName = getString(R.string.str_default_video_edit_user_name);
        }

        try {
            istr = getAssets().open("tailer.png");
            Bitmap bitmap = BitmapFactory.decodeStream(istr);
            Typeface font = Typeface.createFromAsset(this.getAssets(), "PingFang Regular.ttf");
            Bitmap tailerBitmap = mAfterEffect.createTailer(bitmap, nickName, date, font);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (istr != null)
                try {
                    istr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void handlerAECallBack(Message msg) {
        switch (msg.what) {
        case MSG_AE_PLAY_STARTED:
            Log.d(TAG, "MSG_AE_PLAY_STARTED");
            mPlayerState = PlayerState.PLAYING;
            mVideoPlayIV.setVisibility(View.GONE);
            clearChunkFocus();
            break;
        case MSG_AE_PLAY_PROGRESS: {
            ChunkPlayBean playBean = (ChunkPlayBean) msg.obj;
            float currentPos = playBean.currentSec;
            float totalSec = playBean.totalSec;
            float chunkPosition = playBean.position;
            AfterEffect afterEffect = playBean.effect;
            final int chunkIndex = playBean.chunkIndex;

            if(chunkIndex == -1) {
                final int tailOffset = (int)(chunkPosition / VideoEditConstant.VIDEO_TAIL_TIME_DURATION * mTailWidth);
                mAERecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mAELayoutManager.scrollToPositionWithOffset(
                                mProjectItemList.size() - 2, -tailOffset + mDummyHeaderWidth);
                    }
                });
                return;
            }
            Chunk chunk = afterEffect.getMainChunks().get(chunkIndex);

            Log.d(TAG, "MSG_AE_PLAY_PROGRESS " + currentPos + "/" + totalSec + ","
                + chunkIndex + "-" + chunkPosition + ", chunk duration: " +
            chunk.getDuration() + ", mPlayingChunkPosition=" + mPlayingChunkPosition);
            int chunkWidth = VideoEditUtils.ChunkTime2Width(chunk);
//			float chunkOffset = chunkPosition - mPlayingChunkPosition;
            final int moveOffset = (int) (chunkPosition / chunk.getDuration() * chunkWidth);
            mAERecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAELayoutManager.scrollToPositionWithOffset(
                            VideoEditUtils.mapC2IIndex(chunkIndex), -moveOffset + mDummyHeaderWidth);
                }
            });
        }
            break;
        case MSG_AE_PLAY_FINISHED:
            Log.d(TAG, "MSG_AE_PLAY_FINISHED");
            mVideoPlayIV.setVisibility(View.VISIBLE);
            mPlayerState = PlayerState.STOPPED;
            mAERecyclerView.post(new Runnable() {
                @Override
                public void run() {
                mAELayoutManager.scrollToPositionWithOffset(
                        mProjectItemList.size() - 1, mDummyHeaderWidth);
                }
            });
            break;
        case MSG_AE_PLAY_FAILED: {
            Log.d(TAG, "MSG_AE_PLAY_FAILED");
            float currentPos = -1;
            mVideoPlayIV.setVisibility(View.VISIBLE);
            mPlayerState = PlayerState.ERROR;
        }
            break;
        case MSG_AE_EXPORT_STARTED:
            Log.d(TAG, "MSG_AE_EXPORT_STARTED");
            break;
        case MSG_AE_EXPORT_PROGRESS:
            Log.d(TAG, "MSG_AE_EXPORT_PROGRESS: " + msg.arg1);
            String org = getString(R.string.str_video_export_progress);
//            String formattedOrg = String.format(org, msg.arg1);

            int progress = (int) msg.arg1;
            if(null != mExportingDialog) {
                mExportingDialog.setRingViewProgress(progress);
                mExportingDialog.setTextProgress(org);
            }
            break;
        case MSG_AE_EXPORT_FINISHED:
            Log.d(TAG, "MSG_AE_EXPORT_FINISHED");
            ExportRet retBean = (ExportRet)msg.obj;
//            String path = (String)msg.obj;
            if(mExportingDialog != null && mExportingDialog.isVisible()) {
                mExportingDialog.dismissAllowingStateLoss();
            }
            if(retBean.succeed) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.parse("file://" + retBean.path));
                sendBroadcast(intent);
                Toast.makeText(this, getString(R.string.str_video_export_succeed), Toast.LENGTH_SHORT).show();

                {
                    // After Effect export data collection
                    float duration = getChannelDuration();
                    String durationStr = "unsupported length";
                    if(duration >= 10f && duration < 14f) {
                        durationStr = "10~13S";
                    } else if(duration >= 14f && duration <= 30f) {
                        durationStr = "14~30S";
                    } else if(duration > 30f && duration <= 60f) {
                        durationStr = "30~60S";
                    } else if(duration > 60f && duration <= 90f) {
                        durationStr = "60~90S";
                    }
                    int musicIndex = mAEMusicAdapter.getSelectedIndex();
                    String musicName = mMusicNames[musicIndex];

                    ZhugeUtils.eventVideoExport(this, durationStr, musicName, mExportQuality);
                }

                GolukUtils.startVideoShareActivity(AfterEffectActivity.this, PhotoAlbumConfig.PHOTO_BUM_IPC_WND,
                        retBean.path, retBean.path, false, (int)(getChannelDuration() * 1000), mExportQuality,
                        (PromotionSelectItem) getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO));
                finishAfterEffect();
            } else {
                Toast.makeText(this, getString(R.string.str_video_export_failed), Toast.LENGTH_SHORT).show();
            }
            break;
        case MSG_AE_EXPORT_FAILED:
            if(null != mExportingDialog && mExportingDialog.isVisible()) {
                mExportingDialog.dismissAllowingStateLoss();
            }

            mExportFailDialogBuilder.setTitle(getString(R.string.str_video_export_failed));
            mExportFailDialogBuilder.setMessage(getString(R.string.str_video_export_failed_info));

            mExportFailDialogBuilder.setPositiveButton(getString(R.string.str_button_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(null != mExportingDialog && mExportingDialog.isVisible()) {
                        mExportingDialog.dismissAllowingStateLoss();
                    }
                }
            });
            mExportFailDialog = mExportFailDialogBuilder.create();
            mExportFailDialog = mExportFailDialogBuilder.show();
            ZhugeUtils.eventVideoExportFail(this, android.os.Build.MANUFACTURER, android.os.Build.MODEL, android.os.Build.VERSION.RELEASE);
            break;
        case MSG_AE_THUMB_GENERATED: {
            Chunk chunkThumb = (Chunk) msg.obj;
            // if (this.chunkThumbList == null)
            // chunkThumbList = new ArrayList<ChunkThumbs>();
            // chunkThumbList.add(chunkThumbList.size(), chunkThumb);
                // add all get bitmaps to
            break;
        }
        case MSG_AE_CHUNK_ADD_FINISHED: {
            Chunk chunk = (Chunk) msg.obj;
            if (chunk != null) {
                Log.d(TAG, "chunk added:" + chunk.prettyString());
                mChunksTotalTime += chunk.getDuration();
                mAfterEffect.generateThumbAsyn(chunk, VideoEditConstant.BITMAP_TIME_INTERVAL, mImageHeight);
            }

            break;
        }

        case MSG_AE_CHUNK_ADD_FAILED: {
            if (null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
                mFullLoadingDialog.close();
            }
            Toast.makeText(this, getString(R.string.str_ae_add_chunk_failed), Toast.LENGTH_SHORT).show();
            ZhugeUtils.eventAddChunk(this, false);
            if(mProjectItemList == null || mProjectItemList.size() <= 3) {
                finishAfterEffect();
            }
            break;
        }

        case MSG_AE_PLAY_PAUSED: {
            mVideoPlayIV.setVisibility(View.VISIBLE);
            mPlayerState = PlayerState.PAUSED;
        }
            break;

        case MSG_AE_PLAY_RESUMED:
        {
            mVideoPlayIV.setVisibility(View.GONE);
            mPlayerState = PlayerState.PLAYING;
            clearChunkFocus();
        }
            break;

        case MSG_AE_BITMAP_READ_OUT: {
            Chunk chunk = (Chunk) msg.obj;
//            mTailWidth = mAfterEffect.getTailerThumbWidth();
            Log.d(TAG, "tail width from sdk: " + mTailWidth);
            // Get to insert index
            ChunkBean chunkBean = new ChunkBean();
            chunkBean.chunk = chunk;
            chunkBean.index_tag = VideoEditUtils.generateIndexTag();
            chunkBean.width = VideoEditUtils.ChunkTime2Width(chunk);
            chunkBean.isEditState = false;
            chunkBean.curVolume = 100;

            int cInsertIndex = mProjectItemList.size() - 2;
            chunkBean.ct_pair_tag = cInsertIndex + "chunkIndex";
            mProjectItemList.add(cInsertIndex, chunkBean);

            // truncate to the end
            Transition transtion = mAfterEffect.getTransition(
                    VideoEditUtils.mapI2CIndex(cInsertIndex), true);
//			if(transtion != null) {
            TransitionBean transitionBean = new TransitionBean();
            transitionBean.index_tag = VideoEditUtils.generateIndexTag();
            transitionBean.transiton = transtion;
            int tInsertIndex = mProjectItemList.size() - 2;
            transitionBean.ct_pair_tag = cInsertIndex + "chunkIndex";
            mProjectItemList.add(tInsertIndex, transitionBean);
//			}

            mChannelLineAdapter.setData(mProjectItemList);
            mChannelLineAdapter.notifyDataSetChanged();
            if (null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
                mFullLoadingDialog.close();
            }
            final int addIndex = cInsertIndex;
            if (mPlayerState == PlayerState.PAUSED) {
                seekWith(VideoEditUtils.mapI2CIndex(VideoEditUtils.mapI2CIndex(addIndex)));
                mAERecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                        mAELayoutManager.scrollToPositionWithOffset(
                                addIndex, mDummyHeaderWidth);
//						playOrPause();
                    }
                    });
            } else {
                playOrPause();
            }
            clearChunkFocus();
            ZhugeUtils.eventAddChunk(this, true);
        }
            break;
        case MSG_AE_BITMAP_READ_FAILED: {
            Log.d(TAG, "MSG_AE_BITMAP_READ_FAILED");
            ZhugeUtils.eventAddChunk(this, false);
            if(null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
                mFullLoadingDialog.close();
            }
            Toast.makeText(this, getString(R.string.str_ae_add_chunk_failed), Toast.LENGTH_SHORT).show();
            if(mProjectItemList == null || mProjectItemList.size() <= 3) {
                finishAfterEffect();
            }
        }
            break;
        case MSG_AE_CHUNK_PLAY_END: {
            ChunkPlayBean playBean = (ChunkPlayBean) msg.obj;
            float currentPos = playBean.currentSec;
            float totalSec = playBean.totalSec;
            float chunkPosition = playBean.position;
            int chunkIndex = playBean.chunkIndex;
            Chunk chunk = mAfterEffect.getMainChunks().get(chunkIndex);

            Log.d(TAG, "MSG_AE_PLAY_PROGRESS " + currentPos + "/" + totalSec + "," + chunkIndex + "-" + chunkPosition);
            int chunkWidth = VideoEditUtils.ChunkTime2Width(chunk);
        }
            break;
        case MSG_AE_MUSIC_START: {
            if(null != mFullLoadingDialog) {
                mFullLoadingDialog.setTextTitle("Please wait...");
                mFullLoadingDialog.show();
            }
        }
            break;
        case MSG_AE_MUSIC_FAILED: {
            if (null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
                mFullLoadingDialog.close();
            }
        }
            break;
        case MSG_AE_MUSIC_STARTED: {
            if (null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
                mFullLoadingDialog.close();
            }
        }
            break;
        default:
            Log.d(TAG, "unknown operation happened");
            break;
        }
    }

    public void showEditController() {
        mAEEditController.setVisibility(View.VISIBLE);
        mAEMusicRecyclerView.setVisibility(View.GONE);
    }

    public void showMusicController() {
        mAEEditController.setVisibility(View.GONE);
        mAEMusicRecyclerView.setVisibility(View.VISIBLE);
    }

    private void initController() {
        mMusicNames = new String[9];
        mMusicNames[0] = getString(R.string.str_video_music_none);
        mMusicNames[1] = getString(R.string.str_video_music_wave);
        mMusicNames[2] = getString(R.string.str_video_music_discover);
        mMusicNames[3] = getString(R.string.str_video_music_Humor);
        mMusicNames[4] = getString(R.string.str_video_music_Memory);
        mMusicNames[5] = getString(R.string.str_video_music_Crowd);
        mMusicNames[6] = getString(R.string.str_video_music_Travel);
        mMusicNames[7] = getString(R.string.str_video_music_Fresh);
        mMusicNames[8] = getString(R.string.str_video_music_Wild);

        Thread copyMusicThread = new Thread() {
            @Override
            public void run() {
                try {
                    copyBgMusic(mMusicPaths);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        };
        copyMusicThread.start();

        mAEEditController = (LinearLayout) findViewById(R.id.ll_video_edit_controller);
        mAEVolumeSettingLayout = (RelativeLayout) findViewById(R.id.rl_ae_volume_setting);
        mAEVolumeSettingIv = (ImageView) findViewById(R.id.iv_ae_volume_setting);
        mAEVolumePercentTv = (TextView) findViewById(R.id.tv_ae_volume_percent);
        mAEVolumeSeekBar = (SeekBar) findViewById(R.id.seekbar_ae_volume);

        mAESplitAndDeleteLayout = (LinearLayout) findViewById(R.id.ll_ae_split_and_delete);
        mAESplitLayout = (LinearLayout) findViewById(R.id.ll_ae_split);
        mAEDeleteLayout = (LinearLayout) findViewById(R.id.ll_ae_delete);
        mAECutLayout = (LinearLayout) findViewById(R.id.ll_ae_cut);
        mAEVolumeLayout = (LinearLayout) findViewById(R.id.ll_ae_volume);

        mAEMusicLayoutManager = new LinearLayoutManager(this);
        mAEMusicLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mAEMusicRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_ae_music);
        mAEMusicAdapter = new AEMusicAdapter(this, mAfterEffect);
        mAEMusicAdapter.fillupMusicList(mMusicPaths, mMusicNames, mMusicCoversNormal, mMusicCoversSelected);
        mAEMusicRecyclerView.setAdapter(mAEMusicAdapter);
        mAEMusicRecyclerView.setLayoutManager(mAEMusicLayoutManager);

        mAEVolumeSettingIv.setOnClickListener(this);
        mAESplitLayout.setOnClickListener(this);
        mAEDeleteLayout.setOnClickListener(this);
        mAECutLayout.setOnClickListener(this);
        mAEVolumeLayout.setOnClickListener(this);

        mAEVolumeSeekBar.setMax(VideoEditConstant.VIDEO_VOLUME_MAX);
        mAEVolumeSeekBar.setProgress(100);

        mAEVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int index = mChannelLineAdapter.getEditIndex();
                if (fromUser) {
                    recordEditChunkVolume(progress);
                }
                mAEVolumePercentTv.setText(progress + "%");

                if (progress == 0) {
                    mAEVolumeSettingIv.setImageDrawable(
                            AfterEffectActivity.this.getResources().getDrawable(R.drawable.ic_ae_volume_closed));
                    setChunkVolume(index, 0.0f);
                } else {
                    mAEVolumeSettingIv.setImageDrawable(
                            AfterEffectActivity.this.getResources().getDrawable(R.drawable.ic_ae_volume_checked));
                    setChunkVolume(index, progress / 100f);
                }
            }
        });
    }

    private void setChunkVolume(int index, float volume) {
        if(volume > 5f) {
            volume = 5f;
        }

        if(volume < 0f) {
            volume = 0f;
        }
        mAfterEffect.editChunkVolume(VideoEditUtils.mapI2CIndex(index), volume);
    }

    public void recordEditChunkVolume(int volume) {
        int index = mChannelLineAdapter.getEditIndex();
        if(index == -1) {
            return;
        }

        ProjectItemBean itemBean = mProjectItemList.get(index);
        if(itemBean instanceof ChunkBean) {
            ChunkBean chunkBean = (ChunkBean)itemBean;
            chunkBean.curVolume = volume;
            if(volume == 0) {
                chunkBean.isMute = true;
            } else {
                chunkBean.isMute = false;
            }
        }
    }

    // index for item bean
    public void setEditChunkVolume() {
        int index = mChannelLineAdapter.getEditIndex();
        if(index == -1) {
            return;
        }

        ProjectItemBean itemBean = mProjectItemList.get(index);
        if(itemBean instanceof ChunkBean) {
            ChunkBean chunkBean = (ChunkBean)itemBean;

            if(chunkBean.isMute) {
                mAEVolumeSettingIv.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_ae_volume_closed));
                mAEVolumeSeekBar.setProgress(0);
            } else {
                mAEVolumeSettingIv.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_ae_volume_checked));
                mAEVolumeSeekBar.setProgress(chunkBean.curVolume);
            }
        }
    }

    public void muteEditChunkVolume() {
        int index = mChannelLineAdapter.getEditIndex();
        if(index == -1) {
            return;
        }

        ProjectItemBean itemBean = mProjectItemList.get(index);
        if(itemBean instanceof ChunkBean) {
            ChunkBean chunkBean = (ChunkBean)itemBean;

            if(chunkBean.isMute) {
                chunkBean.isMute = false;
                mAEVolumeSettingIv.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_ae_volume_checked));
            } else {
                chunkBean.isMute = true;
                mAEVolumeSettingIv.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_ae_volume_closed));
            }

            if(chunkBean.isMute) {
                mAEVolumeSeekBar.setProgress(0);
                setChunkVolume(index, 0);
            } else {
                mAEVolumeSeekBar.setProgress(chunkBean.curVolume);
                setChunkVolume(index, chunkBean.curVolume);
            }
        }
    }

    private void copyBgMusic(String[] musicFiles) throws IOException {
        // First, make dir music if it not exist
        String dirPath = Environment.getExternalStorageDirectory() + "/" + "goluk_music";

        File dir = new File(dirPath);
        if (!dir.exists()) {
           boolean  b = dir.mkdir();
        } else {
            Log.d(TAG, "create music store path failed");
        }
        for (int i = 1; i < musicFiles.length; i++) {
            String destPath = Environment.getExternalStorageDirectory() + "/" + musicFiles[i];
            File file = new File(destPath);
            if (file.exists())
                continue;

            InputStream myInput;
            OutputStream myOutput = new FileOutputStream(destPath);
            myInput = this.getAssets().open(musicFiles[i]);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }

            myOutput.flush();
            myInput.close();
            myOutput.close();
        }
    }

    // Seek to chunk with specified offset
    public void seekWith(int chunkIndex, int chunkWidth, float delta) {
        if(null == mAfterEffect) {
            return;
        }
        Chunk chunk = mAfterEffect.getMainChunks().get(chunkIndex);
        mAfterEffect.seekTo(chunkIndex, delta / chunkWidth * chunk.getDuration());
    }

    // Seek to chunk with 0 offset
    public void seekWith(int chunkIndex) {
        if(null == mAfterEffect) {
            return;
        }
        mAfterEffect.seekTo(chunkIndex);
    }

    private VideoEditExportDialog mExportDialog;

    private void clearChunkFocusEx() {
        int preFocusIdx = mChannelLineAdapter.getEditIndex();
        // no focus before
        if(-1 == preFocusIdx) {
            return;
        }
        ProjectItemBean bean = mProjectItemList.get(preFocusIdx);
        if(!(bean instanceof ChunkBean)) {
            // something wrong
            return;
        }
        mChannelLineAdapter.setEditIndex(-1);
        ChunkBean chunkBean = (ChunkBean)bean;
        chunkBean.isEditState = false;
        mChannelLineAdapter.notifyItemChanged(preFocusIdx);
        showMusicController();
    }

    private void clearChunkFocus() {
        if(mProjectItemList == null || mProjectItemList.size() == 0) {
            return;
        }

        for(ProjectItemBean bean : mProjectItemList) {
            if(bean instanceof ChunkBean) {
                ChunkBean chunkBean = (ChunkBean)bean;
                chunkBean.isEditState = false;
            }
        }
        mChannelLineAdapter.setEditIndex(-1);
        mChannelLineAdapter.notifyDataSetChanged();
        showMusicController();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_after_effect);
        mPlayerState = PlayerState.INITED;
        mVideoPath = getIntent().getStringExtra("vidPath");

        // default tail and footer
        mProjectItemList = new ArrayList<ProjectItemBean>();
        DummyHeaderBean headerBean = new DummyHeaderBean();
        headerBean.index_tag = VideoEditUtils.generateIndexTag();
        mProjectItemList.add(headerBean);
        TailBean tailBean = new TailBean();
        tailBean.index_tag = VideoEditUtils.generateIndexTag();
        mProjectItemList.add(tailBean);
        DummyFooterBean footerBean = new DummyFooterBean();
        footerBean.index_tag = VideoEditUtils.generateIndexTag();
        mProjectItemList.add(footerBean);
        mBackBTN = (ImageButton) findViewById(R.id.ib_ae_imagebutton_back);
        mBackBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAfterEffect();
            }
        });

        mAELayoutManager = new LinearLayoutManager(this);
        mAELayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mAERecyclerView = (RecyclerView) findViewById(R.id.rv_video_edit_pic_list);
        mExportDialog = new VideoEditExportDialog(this);

        mAECutIV = (ImageView) findViewById(R.id.iv_ae_cut);
        mAECutTV = (TextView) findViewById(R.id.tv_ae_cut);
        mAEVolumeIV = (ImageView) findViewById(R.id.iv_ae_volume);
        mAEVolumeTV = (TextView) findViewById(R.id.tv_ae_volume);
        Window dialogWindow = mExportDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        if(null == mExportFailDialogBuilder) {
            mExportFailDialogBuilder = new AlertDialog.Builder(this);
        }
        if(mExportingDialog == null) {
            mExportingDialog = new RingViewDialogFragment();
        }

        mExportingDialog.setCancelable(false);

        mFullLoadingDialog = new CustomLoadingDialog(this, "");
        mFullLoadingDialog.setCancel(false);
        mTimeLineWrapperRL = findViewById(R.id.rl_ae_time_line_parent_wrapper);

        mTimeLineWrapperRL.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    mAERecyclerView.scrollBy(mTimeLineLastX - (int)event.getX(), 0);
                    mTimeLineLastX = (int)event.getX();
                } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mTimeLineLastX = (int)event.getX();
                    clearChunkFocus();
                    if(mAfterEffect != null)
                        mAfterEffect.playPause();
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    mTimeLineLastX = 0;
                } else {
                    // Nothing needed here till now
                }
                // do not swallow this event
                return false;
            }
        });

        mNextTV = (TextView) findViewById(R.id.tv_ae_next_button);
        mNextTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getChannelDuration() < VideoEditConstant.MIN_VIDEO_DURATION) {
                    String org = AfterEffectActivity.this.getString(R.string.str_video_export_min_limit);
                    String minLimit = String.format(org, (int) VideoEditConstant.MIN_VIDEO_DURATION);
                    Toast.makeText(AfterEffectActivity.this, minLimit, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getChannelDuration() > VideoEditConstant.MAX_VIDEO_DURATION) {
                    String org = AfterEffectActivity.this.getString(R.string.str_video_export_max_limit);
                    String maxLimit = String.format(org, (int) VideoEditConstant.MAX_VIDEO_DURATION);
                    Toast.makeText(AfterEffectActivity.this, maxLimit, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<VideoEncoderCapability> capaList = mAfterEffect.getSuportedCapability();
                if (capaList == null || capaList.size() == 0) {
                    Toast.makeText(AfterEffectActivity.this,
                            AfterEffectActivity.this.getString(R.string.str_ae_no_export_size_supported),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // If only 480p, export it directly
                if(capaList.size() == 1) {
                    VideoEncoderCapability vc = capaList.get(0);
                    int tempW = vc.getWidth();
                    int tempH = vc.getHeight();
                    float tempF = vc.getFps();
                    int tempB = vc.getBitrate();
                    if(tempW == 848 && tempH == 480) {
                        exportAfterEffectVideo(VideoEditConstant.EXPORT_480P_WIDTH, VideoEditConstant.EXPORT_480P_HEIGHT,
                                VideoEditConstant.BITRATE_2M, VideoEditConstant.FPS_25);
                        pause();
                        return;
                    }
                }

                VideoEncoderCapability vc = capaList.get(capaList.size() - 1);
                int width = vc.getWidth();
                int height = vc.getHeight();
                float fps = vc.getFps();
                int bitrate = vc.getBitrate();
                if (width == 1920 && height == 1080) {
                    mExportDialog.setQualityVisibility(true, true, true);
                }

                if (width == 1280 && height == 720) {
                    mExportDialog.setQualityVisibility(true, true, false);
                }

                if (width == 848 && height == 480) {
                    mExportDialog.setQualityVisibility(true, false, false);
                }

                mExportDialog.show();
            }
        });

        mImageHeight = DeviceUtil.dp2px(this, VideoEditConstant.BITMAP_COMMON_HEIGHT);
        mImageWidth = mImageHeight;
        mDummyHeaderWidth = DeviceUtil.dp2px(this, VideoEditConstant.DUMMY_HEADER_WIDTH);
        mTimeLineGateV = findViewById(R.id.v_time_line_gate);
        mTimeLineGateV.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
//                        mTimeLineGateV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        int[] locations = new int[2];
                        mTimeLineGateV.getLocationOnScreen(locations);
                        int x = locations[0];
                        int y = locations[1];
                        mGateLocationX = x;
                        Log.d(TAG, "Base UI data: mGateLocationX=" + mGateLocationX);
                    }
                });
        mTransitionWidth = DeviceUtil.dp2px(this, VideoEditConstant.TRANSITION_COMMON_WIDTH);
        mTailWidth = DeviceUtil.dp2px(this, VideoEditConstant.VIDEO_TAIL_WIDTH);

        mAERecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//				Log.d(TAG, "time line scrolled: dx=" + dx + ", dy=" + dy);

//                clearChunkFocus();
                seekAfterChunkMoved();
            }
        });
        initPlayer();
        mChannelLineAdapter = new ChannelLineAdapter(this, mAERecyclerView, mProjectItemList, mAfterEffect);
        mAERecyclerView.setAdapter(mChannelLineAdapter);
        mAERecyclerView.setLayoutManager(mAELayoutManager);
        mAERecyclerView.setItemAnimator(new DefaultItemAnimator());

        initController();

        mAERecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mAfterEffect.playPause();
                return false;
            }
        });
    }

    public void seekAfterChunkMoved() {
        int firstVisibleIndex = mAELayoutManager.findFirstVisibleItemPosition();
        int lastVisibleIndex = mAELayoutManager.findLastVisibleItemPosition();
        for (int i = firstVisibleIndex; i <= lastVisibleIndex; i++) {
            View view = mAELayoutManager.findViewByPosition(i);

            mCurrentPointedItemIndex = i;
            if (view.getId() == R.id.fl_ae_data_chunk) {
                int pX = VideoEditUtils.getViewXLocation(view);
                if (VideoEditUtils.judgeChunkOverlap(mGateLocationX, pX, view.getWidth())) {
                    if (mPlayerState == PlayerState.PAUSED || mPlayerState == PlayerState.STOPPED) {
                        if(mPlayerState == PlayerState.STOPPED) {
                            mPlayerState = PlayerState.PAUSED;
                        }
                        seekWith(VideoEditUtils.mapI2CIndex(i), view.getWidth(), mGateLocationX - pX);
                    }
                    break;
                }
            }
        }
    }

    public float getChannelDuration() {
        if(null == mAfterEffect) {
            return 0f;
        }
        return mAfterEffect.getDuration();
    }

    public void exportAfterEffectVideo(int exportWidth, int exportHeight, int bitRate, int fps) {
        float duration = mAfterEffect.getDuration();
        if (duration < VideoEditConstant.MIN_VIDEO_DURATION || duration > VideoEditConstant.MAX_VIDEO_DURATION) {
            Toast.makeText(this, "can not export length: " + duration, Toast.LENGTH_SHORT).show();
            return;
        }
        String destPath = Environment.getExternalStorageDirectory() + VideoEditConstant.EXPORT_FOLDER_NAME;
        File dir = new File(destPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String fileName = null;

        Calendar c = Calendar.getInstance();
        String sYear = c.get(Calendar.YEAR) + "";
        int month = c.get(Calendar.MONTH) + 1;
        String sMonth = (month < 10) ? "0" + month : month + "";
        int day = c.get(Calendar.DAY_OF_MONTH);
        String sDay = (day < 10) ? "0" + day : day + "";
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String sHour = (hour < 10) ? "0" + hour : hour + "";
        int minute = c.get(Calendar.MINUTE);
        String sMinute = (minute < 10) ? "0" + minute : minute + "";
        int second = c.get(Calendar.SECOND);
        String sSecond = (second < 10) ? "0" + second : second + "";
        fileName = "MOV" + sYear + sMonth + sDay + sHour + sMinute + sSecond + (int) duration;
        destPath = destPath + "/" + fileName + ".mp4";

        if (exportWidth == 1920 && exportHeight == 1080) {
            mExportQuality = "1080P";
        }

        if (exportWidth == 1280 && exportHeight == 720) {
            mExportQuality = "720P";
        }

        if (exportWidth == 848 && exportHeight == 480) {
            mExportQuality = "480P";
        }

//        mFullLoadingDialog.show();
        if(!mExportingDialog.isAdded()) {
            mExportingDialog.show(getSupportFragmentManager(), "dialog_fragment");
        }
        try {
            mAfterEffect.export(destPath,
                    exportWidth, exportHeight,
                    fps,
                    bitRate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartToPlay(AfterEffect afterEffcet) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_STARTED, afterEffcet);
        mAfterEffecthandler.sendMessage(msg);
    }

    static class ChunkPlayBean {
        int chunkIndex;
        float position;
        AfterEffect effect;
        float currentSec;
        float totalSec;
    }

    @Override
    public void onPlaying(AfterEffect afterEffcet, float currentSec, float totalSec, int chunkIndex, float position) {
        ChunkPlayBean bean = new ChunkPlayBean();
        bean.chunkIndex = chunkIndex;
        bean.position = position;
        bean.effect = afterEffcet;
        bean.currentSec = currentSec;
        bean.totalSec = totalSec;

        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_PROGRESS, 0, 0, bean);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onPlayFinished(AfterEffect afterEffcet) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_FINISHED, afterEffcet);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onPlayingFailed(AfterEffect afterEffcet, EffectException e) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_FAILED, afterEffcet);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onStartToExport(AfterEffect afterEffcet) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_EXPORT_STARTED, afterEffcet);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onExporting(AfterEffect afterEffcet, float rate) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_EXPORT_PROGRESS, (int) (rate * 100), 0, afterEffcet);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onExportFailed(AfterEffect afterEffcet, EffectException e) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_EXPORT_FAILED, afterEffcet);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    protected void onDestroy() {
        if (mAfterEffect != null) {
            mAfterEffect.playStop();
            mPlayerState = PlayerState.RELEASED;
            mAfterEffect.release();
            mAfterEffect = null;
        }

        if (null != mExportDialog && mExportDialog.isShowing()) {
            mExportDialog.dismiss();
            mExportDialog = null;
        }

        if (null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
            mFullLoadingDialog.close();
            mFullLoadingDialog = null;
        }

        if(mExportingDialog != null && mExportingDialog.isVisible()) {
            mExportingDialog.dismissAllowingStateLoss();
            mExportingDialog = null;
        }

        if(null != mExportFailDialog && mExportFailDialogBuilder != null) {
            mExportFailDialog.dismiss();
            mExportFailDialog = null;
            mExportFailDialogBuilder = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAfterEffect();
    }

    @Override
    public void onGeneratedThumbs(AfterEffect ae, Chunk chunk) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_BITMAP_READ_OUT, chunk);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onGeneratedThumbsFailed(AfterEffect ae, Chunk chunk) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_BITMAP_READ_FAILED, chunk);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onChunkAddedFinished(AfterEffect self, Project project,
                                     Chunk chunk) {
        Log.d(TAG, "onChunkAddedFinished");
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_CHUNK_ADD_FINISHED, chunk);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onChunkAddedFailed(AfterEffect self, Project project,
                                   String filePath) {
        Log.d(TAG, "onChunkAddedFinished");
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_CHUNK_ADD_FAILED);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(null != mAfterEffect) {
            mAfterEffect.onActivityPause();
        }
//		mVideoPlayIV.setVisibility(View.VISIBLE);
//		mIsPlaying = false;
//		mPlayerState = PlayerState.PAUSED;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null != mAfterEffect) {
            mAfterEffect.onActivityResume();
        }
//		mVideoPlayIV.setVisibility(View.VISIBLE);
//		mIsPlaying = false;
//		mPlayerState = PlayerState.PLAYING;
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.iv_video_play) {
            playOrPause();
        } else if (vId == R.id.iv_video_thumb) {
//			mVideoPlayIv.setVisibility(View.GONE);
//			mVideoThumeIv.setVisibility(View.GONE);
        } else if (vId == R.id.ll_ae_volume) {
            mAEVolumeLayout.setBackgroundColor(getResources().getColor(R.color.ae_controller_pressed));
            mAECutLayout.setBackgroundResource(R.drawable.ae_controller_bg);
            mAEVolumeIV.setImageResource(R.drawable.ic_ae_volume_checked);
            mAEVolumeTV.setTextColor(getResources().getColor(R.color.white));

            mAECutIV.setImageResource(R.drawable.ic_ae_cut_unchecked);
            mAECutTV.setTextColor(getResources().getColor(R.color.color_ae_function_pressed));
            mAESplitAndDeleteLayout.setVisibility(View.GONE);
            mAEVolumeSettingLayout.setVisibility(View.VISIBLE);
        } else if (vId == R.id.ll_ae_cut) {
            mAEVolumeLayout.setBackgroundResource(R.drawable.ae_controller_bg);
            mAECutLayout.setBackgroundColor(getResources().getColor(R.color.ae_controller_pressed));

            mAEVolumeIV.setImageResource(R.drawable.ic_ae_volume_unchecked);
            mAEVolumeTV.setTextColor(getResources().getColor(R.color.color_ae_function_pressed));

            mAECutIV.setImageResource(R.drawable.ic_ae_cut_checked);
            mAECutTV.setTextColor(getResources().getColor(R.color.white));
            if (mAEVolumeSettingLayout.getVisibility() == View.VISIBLE) {
                mAEVolumeSettingLayout.setVisibility(View.GONE);
                mAESplitAndDeleteLayout.setVisibility(View.VISIBLE);
            }
        } else if (vId == R.id.ll_ae_split) {
            splitChunk();
        } else if (vId == R.id.ll_ae_delete) {
            int index = mChannelLineAdapter.getEditIndex();
            if(index != -1) {
                if(mProjectItemList == null || mProjectItemList.size() <= 5) {
                    finishAfterEffect();
                    return;
                }
                boolean overlap = VideoEditUtils.judgeChunkOverlap(mAELayoutManager, mGateLocationX, index);
                VideoEditUtils.removeChunk(mAfterEffect, mProjectItemList, index);
                ZhugeUtils.eventChunkRemove(this);
//                mChannelLineAdapter.setEditIndex(-1);
                clearEditController();
                mChannelLineAdapter.notifyDataSetChanged();
                if(mProjectItemList == null || mProjectItemList.size() <= 3) {
                    finishAfterEffect();
                } else {
                    if(overlap) {
                        // Need additional seek to neighbour chunk
//                        seekWith(VideoEditUtils.mapI2CIndex(index));
                        mAELayoutManager.scrollToPositionWithOffset(index, mDummyHeaderWidth);
                        seekWith(VideoEditUtils.mapI2CIndex(index));
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.str_ae_select_chunk_to_remove), Toast.LENGTH_SHORT).show();
            }
        } else if (vId == R.id.iv_ae_volume_setting) {
 //           setEditChunkVolume();
            muteEditChunkVolume();
        }
    }

    public void clearEditController() {
        mChannelLineAdapter.setEditIndex(-1);
        showMusicController();
    }

    @Override
    public void onPlayPaused(AfterEffect afterEffect) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_PAUSED, afterEffect);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onPlayResume(AfterEffect afterEffect) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_RESUMED, afterEffect);
        mAfterEffecthandler.sendMessage(msg);
    }

    public void goToChooseVideo() {
        Intent videoChooseIntent = new Intent();
        videoChooseIntent.setClass(this, VideoChooserActivity.class);
        startActivityForResult(videoChooseIntent, VideoEditConstant.VIDEO_EDIT_ADD_REQ_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == VideoEditConstant.VIDEO_EDIT_ADD_REQ_CODE) {
            Bundle b = data.getExtras(); // data为B中回传的Intent
            String vidPath = b.getString("vidPath");// str即为回传的值
            if (vidPath != null) {
                mChannelLineAdapter.addChunk(vidPath);
            }
        }
    }

    static class ExportRet {
        String path;
        boolean succeed;
    }

    @Override
    public void onExportFinished(AfterEffect afterEffcet, String path, boolean successful) {
        Log.d(TAG, "onExportFinished, path=" + path + ", successful=" + successful);
        ExportRet retBean = new ExportRet();
        retBean.path = path;
        retBean.succeed = successful;

        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_EXPORT_FINISHED, 0, 0, retBean);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onplayingChunkEnd(float currentSec, float totalSec,
                                  int chunkIndex, float currentChunkSec) {
        ChunkPlayBean bean = new ChunkPlayBean();
        bean.chunkIndex = chunkIndex;
        bean.position = currentChunkSec;
        bean.currentSec = currentSec;
        bean.totalSec = totalSec;

        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_CHUNK_PLAY_END, 0, 0, bean);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onReplayWithMusicStrarting(AfterEffect afterEffect) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_MUSIC_START);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onReplayWithMusicStrarted(AfterEffect afterEffect) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_MUSIC_STARTED);
        mAfterEffecthandler.sendMessage(msg);
    }

    @Override
    public void onReplayWithMusicFailed(AfterEffect afterEffect) {
        Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_MUSIC_FAILED);
        mAfterEffecthandler.sendMessage(msg);
    }

    // return 0 for no more scroll, 1 for right scroll, -1 for left scroll
    public int needMusicMoreScroll(int index) {
        if(-1 == index) {
            return 0;
        }

        int last = mAEMusicLayoutManager.findLastCompletelyVisibleItemPosition();
        int first = mAEMusicLayoutManager.findFirstCompletelyVisibleItemPosition();
        if(index >= last) {
            return 1;
        } else if(index <= first) {
            return -1;
        }


        return 0;
    }

    public void moreMusicScrollRight() {
        int first = mAEMusicLayoutManager.findFirstCompletelyVisibleItemPosition();
        mAEMusicLayoutManager.scrollToPositionWithOffset(++first, 0);
    }

    public void moreMusicScrollLeft() {
        int first = mAEMusicLayoutManager.findFirstCompletelyVisibleItemPosition();
        if(first > 0) {
            first--;
        }
        mAEMusicLayoutManager.scrollToPositionWithOffset(first, 0);
    }
}
