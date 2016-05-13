package com.mobnote.videoedit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.live.UserInfo;
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

import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.AfterEffectListener;
import cn.npnt.ae.exceptions.EffectException;
import cn.npnt.ae.exceptions.EffectRuntimeException;
import cn.npnt.ae.exceptions.InvalidLengthException;
import cn.npnt.ae.exceptions.InvalidVideoSourceException;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.Project;
import cn.npnt.ae.model.Transition;
import com.mobnote.application.GolukApplication;
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
	int mDummyHeaderWidth;
	private TextView mNextTV;

	/** 是否为静音 */
	private boolean mIsMute;
	/** 当前音量 */
	private int mCurrVolumeProgress;

	private final static String TAG = "AfterEffectActivity";
//	private float mCurrentPlayPosition = 0f;
	private int mCurrentPointedItemIndex;
	float mPlayingChunkPosition = 0f;

	private View mTimeLineGateV;
	private int mGateLocationX;
    private CustomLoadingDialog mFullLoadingDialog;
    private int mTailWidth;
    private View mTimeLineWrapperRL;

//	String mVideoPath1 = VideoEditConstant.VIDEO_PATH;
	String mVideoPath;// = VideoEditConstant.VIDEO_PATH_1;
//	String mMusicPath = VideoEditConstant.MUSIC_PATH;

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

//	private boolean mIsPlaying;
//	private boolean mIsPlayFinished;
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
		"music/01-fasion-48khz-128kbps-final.mp3",
		"music/02-discover-48khz-128kbps-final.mp3",
		"music/03-no_effect-48khz-128kbps-final.mp3",
		"music/04-memory-48khz-128kbps-final.mp3",
		"music/05-street-48khz-128kbps-final.mp3",
		"music/06-travel-48khz-128kbps-final.mp3",
		"music/07-fresh-48khz-128kbps-final.mp3",
		"music/08-crual-48khz-128kbps-final.mp3"};

	final String[] mMusicNames = {
			"none",
			"fasion",
			"discover",
			"default",
			"memory",
			"street",
			"travel",
			"fresh",
			"crual"};

    public int getTailWidth() {
        return mTailWidth;
    }

    public void moveChunk2Gate(final int index) {
        ProjectItemBean bean = mProjectItemList.get(index);
        if(!(bean instanceof ChunkBean)) {
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
			try {
				mAfterEffect.editAddChunk(videoPath, addFlag);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除某个chunk片段
	 * @param chunkIndex
	 */
	public void removeChunk(int chunkIndex) {
		if(mProjectItemList == null || mProjectItemList.size() <= 3) {
			return ;
		}

		try {
			mAfterEffect.editRemoveChunk(chunkIndex);
		} catch(EffectRuntimeException e) {
			return;
		}
	}

	public void splitChunk() {
		if(mProjectItemList == null || mProjectItemList.size() <= 3) {
			return ;
		}

		ProjectItemBean bean = mProjectItemList.get(mCurrentPointedItemIndex);
		if(!(bean instanceof ChunkBean)) {
            Toast.makeText(this, "此条目不能拆分", Toast.LENGTH_SHORT).show();
			return;
		}

		View chunkView = mAELayoutManager.findViewByPosition(mCurrentPointedItemIndex);
		float width = chunkView.getWidth();
		float pX = VideoEditUtils.getViewXLocation(chunkView);

		int chunkIndex = VideoEditUtils.mapI2CIndex(mCurrentPointedItemIndex);
		float position = (mGateLocationX - pX) / width * ((ChunkBean)bean).chunk.getDuration();
		if(position == 0f) {
			return;
		}

		if(!mAfterEffect.canSplit(chunkIndex, position)) {
			Toast.makeText(this, position + "长度不能拆分", Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			float realPosition = mAfterEffect.editSplitChunk(chunkIndex, position);

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

			chunkBean1.ct_pair_tag = itemIndex + "chunkIndex";
			mProjectItemList.set(itemIndex, chunkBean1);

			// ignore transition, the same since now

			// insert second chunk
			ChunkBean chunkBean2 = new ChunkBean();
			chunkBean2.chunk = second;
			chunkBean2.index_tag = VideoEditUtils.generateIndexTag();
			chunkBean2.width = VideoEditUtils.ChunkTime2Width(second);
			chunkBean2.isEditState = true;

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
		} catch(EffectRuntimeException e) {
			e.printStackTrace();
			return;
		} catch(InvalidLengthException e) {
			e.printStackTrace();
			return;
		}
		mChannelLineAdapter.notifyDataSetChanged();
	}

	public void playOrPause() {
		if(mPlayerState == PlayerState.PLAYING) {
			mVideoPlayIV.setVisibility(View.VISIBLE);
			mAfterEffect.playPause();
		} else if(mPlayerState == PlayerState.PAUSED) {
			mVideoPlayIV.setVisibility(View.GONE);
			mAfterEffect.playResume();
		} else {
			if(mVideoThumeIv.getVisibility() == View.VISIBLE) {
				mVideoThumeIv.setVisibility(View.GONE);
			}

			if(mVideoPlayIV.getVisibility() == View.VISIBLE) {
				mVideoPlayIV.setVisibility(View.GONE);
			}

			//如果当前是播放完成状态，则重置数据
//			if(mPlayerState == PlayerState.STOPPED) {
//				mAERecyclerView.smoothScrollToPosition(0);
//				mCurrentPlayPosition = 0f;
//			}

			//当前播放进度大于0，则从当前位置开始播放，否则从头开始播放
			try {
				mAfterEffect.play();
			} catch (InvalidVideoSourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//
//			if(mCurrentPlayPosition > 0f) {
//				mAfterEffect.playResume();
//			} else {
//				try {
//					mAfterEffect.play();
//				} catch (InvalidVideoSourceException e) {
//					e.printStackTrace();
//				}
//			}
		}
	}

	private void pause() {
		mAfterEffect.playPause();
	}

	private void initPlayer() {
		mVideoThumeIv = (ImageView) findViewById(R.id.iv_video_thumb);
		mVideoPlayIV = (ImageView) findViewById(R.id.iv_video_play);

		mVideoThumeIv.setOnClickListener(this);
		mVideoPlayIV.setOnClickListener(this);

		mSurfaceLayout = (FrameLayout)findViewById(R.id.fl_video_sur_layout);
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
				// TODO Auto-generated method stub
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

	private void addTail() {
		InputStream istr = null;
		GolukApplication mApp = GolukApplication.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(new java.util.Date());
		String nickName = null;
		if(mApp.isUserLoginSucess) {
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
//			mCurrentPlayPosition = 0;
//			mIsPlayFinished = false;
//			if(!mIsPlaying){
//				mVideoPlayIv.setVisibility(View.GONE);
//				mIsPlaying = true;
//			}
			mPlayerState = PlayerState.PLAYING;
			mVideoPlayIV.setVisibility(View.GONE);
            clearChunkFocus();
			break;
		case MSG_AE_PLAY_PROGRESS:
		{
			ChunkPlayBean playBean = (ChunkPlayBean)msg.obj;
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
			final int moveOffset = (int)(chunkPosition / chunk.getDuration() * chunkWidth);
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
		case MSG_AE_PLAY_FAILED:
		{
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
			break;
		case MSG_AE_EXPORT_FINISHED:
            Log.d(TAG, "MSG_AE_EXPORT_FINISHED");
            String path = (String)msg.obj;
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.parse("file://" + path));
            sendBroadcast(intent);
            if(null != mFullLoadingDialog) {
                mFullLoadingDialog.close();
            }
            break;
        case MSG_AE_EXPORT_FAILED:
            if(null != mFullLoadingDialog) {
                mFullLoadingDialog.close();
            }
            break;
		case MSG_AE_THUMB_GENERATED: {
			 Chunk chunkThumb = (Chunk) msg.obj;
			// if (this.chunkThumbList == null)
			// chunkThumbList = new ArrayList<ChunkThumbs>();
			// chunkThumbList.add(chunkThumbList.size(), chunkThumb);
//			addChildLayoutForLayout();
			// add all get bitmaps to
			break;
		}
		case MSG_AE_CHUNK_ADD_FINISHED: {
			Chunk chunk = (Chunk)msg.obj;
			if(chunk != null) {
				Log.d(TAG, "chunk added:" + chunk.prettyString());
				mChunksTotalTime += chunk.getDuration();
				mAfterEffect.generateThumbAsyn(chunk, VideoEditConstant.BITMAP_TIME_INTERVAL, mImageHeight);
			}

			break;
		}

		case MSG_AE_CHUNK_ADD_FAILED: {
			String filePath = (String)msg.obj;
			Log.d(TAG, "chunk added fialed:" + filePath);
			break;
		}

		case MSG_AE_PLAY_PAUSED:
		{
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
			final int addIndex = cInsertIndex;
			if(mPlayerState == PlayerState.PAUSED) {
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
			break;
		}

		case MSG_AE_CHUNK_PLAY_END:
		{
			ChunkPlayBean playBean = (ChunkPlayBean)msg.obj;
			float currentPos = playBean.currentSec;
			float totalSec = playBean.totalSec;
			float chunkPosition = playBean.position;
//			AfterEffect afterEffect = playBean.effect;
			int chunkIndex = playBean.chunkIndex;
			Chunk chunk = mAfterEffect.getMainChunks().get(chunkIndex);

//			Log.d("CK1", "MSG_AE_PLAY_PROGRESS " + currentChunkPosition + "/" + chunkPosition + "/" + currentPlayPosition);
			Log.d(TAG, "MSG_AE_PLAY_PROGRESS " + currentPos + "/" + totalSec + "," + chunkIndex + "-" + chunkPosition);
			int chunkWidth = VideoEditUtils.ChunkTime2Width(chunk);
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
		AEMusicAdapter mAEMusicAdapter = new AEMusicAdapter(this, mAfterEffect);
		mAEMusicAdapter.fillupMusicList(mMusicPaths, mMusicNames);
		mAEMusicRecyclerView.setAdapter(mAEMusicAdapter);
		mAEMusicRecyclerView.setLayoutManager(mAEMusicLayoutManager);

		mAEVolumeSettingIv.setOnClickListener(this);
		mAESplitLayout.setOnClickListener(this);
		mAEDeleteLayout.setOnClickListener(this);
		mAECutLayout.setOnClickListener(this);
		mAEVolumeLayout.setOnClickListener(this);

		mAEVolumeSeekBar.setMax(100);
		mCurrVolumeProgress = 100;
		mAEVolumeSeekBar.setProgress(mCurrVolumeProgress);

		mAEVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				/** 如果来自用户对seekbar的操作，则记录progress。如果来自代码调用setProgress()则不记录 */
				if(fromUser) {
					mCurrVolumeProgress = progress;
				}
				mAEVolumePercentTv.setText(progress + "%");

				if(progress == 0) {
					mAEVolumeSettingIv.setImageDrawable(
							AfterEffectActivity.this.getResources().getDrawable(R.drawable.ic_ae_volume_closed));
					mIsMute = true;
					mAfterEffect.editChunkVolume(VideoEditUtils.mapI2CIndex(mCurrentPointedItemIndex), 0.0f);
				} else {
					mAEVolumeSettingIv.setImageDrawable(
							AfterEffectActivity.this.getResources().getDrawable(R.drawable.ic_ae_volume));
					mIsMute = false;
					mAfterEffect.editChunkVolume(VideoEditUtils.mapI2CIndex(mCurrentPointedItemIndex), (float)(progress * 5) / 100f);
				}
			}
		});

		try {
			copyBgMusic(mMusicPaths);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyBgMusic(String[] musicFiles) throws IOException {
		for(int i = 1; i < musicFiles.length; i++){
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
		Chunk chunk = mAfterEffect.getMainChunks().get(chunkIndex);
		mAfterEffect.seekTo(chunkIndex, delta / chunkWidth * chunk.getDuration());
	}

	// Seek to chunk with 0 offset
	public void seekWith(int chunkIndex) {
		mAfterEffect.seekTo(chunkIndex);
	}

    private VideoEditExportDialog mExportDialog;

    private void clearChunkFocus() {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		mBackBTN = (ImageButton)findViewById(R.id.ib_ae_imagebutton_back);
		mBackBTN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AfterEffectActivity.this.finish();
			}
		});

		mAELayoutManager = new LinearLayoutManager(this);
		mAELayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mAERecyclerView = (RecyclerView) findViewById(R.id.rv_video_edit_pic_list);
		mExportDialog = new VideoEditExportDialog(this);
		Window dialogWindow = mExportDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        mFullLoadingDialog = new CustomLoadingDialog(this, "正在导出...");
        mTimeLineWrapperRL = findViewById(R.id.rl_ae_time_line_parent_wrapper);
        mTimeLineWrapperRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearChunkFocus();
            }
        });

		mNextTV = (TextView)findViewById(R.id.tv_ae_next_button);
		mNextTV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<VideoEncoderCapability> capaList = mAfterEffect.getSuportedCapability();
				if (capaList == null || capaList.size() == 0) {
					Toast.makeText(AfterEffectActivity.this, "手机不支持合适的分辨率", Toast.LENGTH_SHORT).show();
					return;
				}

				VideoEncoderCapability vc = capaList.get(capaList.size() - 1);
				int width = vc.getWidth();
				int height = vc.getHeight();
				float fps = vc.getFps();
				int bitrate = vc.getBitrate();
				if(width == 1920 && height == 1080) {
					mExportDialog.setQualityVisibility(true, true, true);
				}

				if(width == 1280 && height == 720) {
					mExportDialog.setQualityVisibility(true, true, false);
				}

				if(width == 848 && height == 480) {
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
//						mTimeLineGateV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

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

				int firstVisibleIndex = mAELayoutManager.findFirstVisibleItemPosition();
				int lastVisibleIndex = mAELayoutManager.findLastVisibleItemPosition();
				for(int i = firstVisibleIndex; i <= lastVisibleIndex; i++) {
					View view = mAELayoutManager.findViewByPosition(i);

                    mCurrentPointedItemIndex = i;
					if(view.getId() == R.id.fl_ae_data_chunk) {
						int pX = VideoEditUtils.getViewXLocation(view);
						if(VideoEditUtils.judgeChunkOverlap(mGateLocationX, pX, view.getWidth())) {
							if(mPlayerState == PlayerState.PAUSED) {
								seekWith(VideoEditUtils.mapI2CIndex(i), view.getWidth(), mGateLocationX - pX);
							}
							break;
						}
                    }
				}
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

	public float getChannelDuration() {
		return mAfterEffect.getDuration();
	}

	public void exportAfterEffectVideo(int exportWidth, int exportHeight) {
		float duration= mAfterEffect.getDuration();
		if(duration < VideoEditConstant.MIN_VIDEO_DURATION || duration > VideoEditConstant.MAX_VIDEO_DURATION) {
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
		fileName = "MOV" + sYear + sMonth + sDay + sHour + sMinute + sSecond + (int)duration;
		destPath = destPath + "/" + fileName + ".mp4";
        mFullLoadingDialog.show();
		try {
			mAfterEffect.export(destPath,
//					VideoEditConstant.DEFAULT_EXPORT_WIDTH,
//					VideoEditConstant.DEFAULT_EXPORT_HEIGHT,
					exportWidth, exportHeight,
					(int) VideoEditConstant.DEFAULT_FPS,
					VideoEditConstant.DEFAULT_EXPORT_BITRATE);
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
		if(mAfterEffect==null){
			mAfterEffect.release();
		}

		if(null != mExportDialog && mExportDialog.isShowing()) {
			mExportDialog.dismiss();
		}

        if(null != mFullLoadingDialog && mFullLoadingDialog.isShowing()) {
            mFullLoadingDialog.close();
        }
		super.onDestroy();
	}

	@Override
	public void onGeneratedThumbs(AfterEffect ae, Chunk chunk) {
		// TODO Auto-generated method stub
		Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_BITMAP_READ_OUT, chunk);
		mAfterEffecthandler.sendMessage(msg);
	}

	@Override
	public void onChunkAddedFinished(AfterEffect self, Project project,
			Chunk chunk) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onChunkAddedFinished");
		Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_CHUNK_ADD_FINISHED, chunk);
		mAfterEffecthandler.sendMessage(msg);
	}

	@Override
	public void onChunkAddedFailed(AfterEffect self, Project project,
			String filePath) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPause() {
		super.onPause();
		mAfterEffect.onActivityPause();
//		mVideoPlayIV.setVisibility(View.VISIBLE);
//		mIsPlaying = false;
//		mPlayerState = PlayerState.PAUSED;
	}

	@Override
	public void onResume() {
		super.onResume();
		mAfterEffect.onActivityResume();
//		mVideoPlayIV.setVisibility(View.VISIBLE);
//		mIsPlaying = false;
//		mPlayerState = PlayerState.PLAYING;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int vId = v.getId();
		if(vId == R.id.iv_video_play) {
			playOrPause();
		} else if (vId == R.id.iv_video_thumb) {
//			mVideoPlayIv.setVisibility(View.GONE);
//			mVideoThumeIv.setVisibility(View.GONE);	
		} else if (vId == R.id.ll_ae_volume) {
			mAEVolumeLayout.setBackgroundColor(AfterEffectActivity.this.getResources().getColor(R.color.ae_controller_pressed));
			mAECutLayout.setBackgroundResource(R.drawable.ae_controller_bg);
			//(AfterEffectActivity.this.getResources().getColor(R.color.ae_controller_normal));

			mAESplitAndDeleteLayout.setVisibility(View.GONE);
			mAEVolumeSettingLayout.setVisibility(View.VISIBLE);
		} else if(vId == R.id.ll_ae_cut) {
			mAEVolumeLayout.setBackgroundResource(R.drawable.ae_controller_bg);
			mAECutLayout.setBackgroundColor(AfterEffectActivity.this.getResources().getColor(R.color.ae_controller_pressed));

			if(mAEVolumeSettingLayout.getVisibility() == View.VISIBLE) {
				mAEVolumeSettingLayout.setVisibility(View.GONE);
				mAESplitAndDeleteLayout.setVisibility(View.VISIBLE);
			}
		} else if(vId == R.id.ll_ae_split) {
			splitChunk();
		} else if(vId == R.id.ll_ae_delete) {
			VideoEditUtils.removeChunk(mAfterEffect, mProjectItemList, mChannelLineAdapter.getEditIndex());
			mChannelLineAdapter.notifyDataSetChanged();
		} else if(vId == R.id.iv_ae_volume_setting) {
			if(mIsMute) {
				mAEVolumeSeekBar.setProgress(mCurrVolumeProgress);
			} else {
				mAEVolumeSeekBar.setProgress(0);
			}
		}
	}

	@Override
	public void onPlayPaused(AfterEffect afterEffect) {
		// TODO Auto-generated method stub
		Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_PAUSED, afterEffect);
		mAfterEffecthandler.sendMessage(msg);
	}

	@Override
	public void onPlayResume(AfterEffect afterEffect) {
		// TODO Auto-generated method stub
		Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_PLAY_RESUMED, afterEffect);
		mAfterEffecthandler.sendMessage(msg);
	}

	public void goToChooseVideo() {
		Intent videoChooseIntent = new Intent();
		videoChooseIntent.setClass(this, VideoChooserActivity.class);
		startActivityForResult(videoChooseIntent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle b = data.getExtras(); // data为B中回传的Intent
			String vidPath = b.getString("vidPath");// str即为回传的值
			if (vidPath != null) {
				mChannelLineAdapter.addChunk(vidPath);
			}
		}
	}

	@Override
	public void onExportFinished(AfterEffect afterEffcet, String path, boolean successful) {
		Log.d("CK1", "onExportFinished, path=" + path + ", successful=" + successful);
		Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_EXPORT_FINISHED, path);
		mAfterEffecthandler.sendMessage(msg);
	}

	@Override
	public void onplayingChunkEnd(float currentSec, float totalSec,
			int chunkIndex, float currentChunkSec) {
		ChunkPlayBean bean = new ChunkPlayBean();
		bean.chunkIndex = chunkIndex;
		bean.position = currentChunkSec;
//		bean.effect = afterEffcet;
		bean.currentSec = currentSec;
		bean.totalSec = totalSec;

		Message msg = mAfterEffecthandler.obtainMessage(MSG_AE_CHUNK_PLAY_END, 0, 0, bean);
		mAfterEffecthandler.sendMessage(msg);
	}
}