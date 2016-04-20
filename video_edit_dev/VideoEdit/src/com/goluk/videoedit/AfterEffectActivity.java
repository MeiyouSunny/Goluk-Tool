package com.goluk.videoedit;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.AfterEffectListener;
import cn.npnt.ae.exceptions.EffectException;
import cn.npnt.ae.exceptions.EffectRuntimeException;
import cn.npnt.ae.exceptions.InvalidLengthException;
import cn.npnt.ae.exceptions.InvalidVideoSourceException;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.Project;
import cn.npnt.ae.model.Transition;

import com.goluk.videoedit.adapter.AEMusicAdapter;
import com.goluk.videoedit.adapter.ChannelLineAdapter;
import com.goluk.videoedit.bean.ChunkBean;
import com.goluk.videoedit.bean.DummyFooterBean;
import com.goluk.videoedit.bean.DummyHeaderBean;
import com.goluk.videoedit.bean.ProjectItemBean;
import com.goluk.videoedit.bean.TailBean;
import com.goluk.videoedit.bean.TransitionBean;
import com.goluk.videoedit.constant.VideoEditConstant;
import com.goluk.videoedit.utils.DeviceUtil;
import com.goluk.videoedit.utils.VideoEditUtils;

public class AfterEffectActivity extends Activity implements AfterEffectListener , View.OnClickListener{
	RecyclerView mAERecyclerView;
	LinearLayoutManager mAELayoutManager;
	RecyclerView mAEMusicRecyclerView;
	LinearLayoutManager mAEMusicLayoutManager;

	Handler mPlaySyncHandler;
	private GLSurfaceView mGLSurfaceView;
	AfterEffect mAfterEffect;
	Project mProject;
	int mImageHeight;
	int mImageWidth;
	int mTransitionWidth;
	List<ProjectItemBean> mProjectItemList;
	Handler handler;
	private ChannelLineAdapter mAdapter;
	private FrameLayout mSurfaceLayout;

	ImageView mVideoThumeIv;
	ImageView mVideoPlayIv;
	float mChunksTotalTime;
	RelativeLayout mAEVolumeSettingLayout;
	ImageView mAEVolumeSettingIv;
	TextView mAEVolumePercentTv;
	SeekBar mAEVolumeSeekBar;

	LinearLayout mAESplitAndDeleteLayout;
	LinearLayout mAESplitLayout;
	LinearLayout mAEDeleteLayout;
	LinearLayout mAECutLayout;
	LinearLayout mAEVolumeLayout;
	int mTimeLineX;
	
	/** 是否为静音 */
	private boolean isMute;
	/** 当前音量 */
	private int mCurrVolumeProgress;

	private final static String TAG = "AfterEffectActivity";
	private float currentPlayPosition = 0f;
	// If the AE value larger than 1, scroll 1 px to reduce it
	private float mCEValue = 0f;

	private View mTimeLineGateV;
	private int mGateLocationX;

	String mVideoPath1 = VideoEditConstant.VIDEO_PATH_1;
	String mVideoPath = VideoEditConstant.VIDEO_PATH;

	String mMusicPath = VideoEditConstant.MUSIC_PATH;

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
	public void removeChunk(int  chunkIndex){
		if(mProjectItemList == null || mProjectItemList.size() <=3){
			return ;
		}

		try{
			mAfterEffect.editRemoveChunk(chunkIndex);
		}catch(EffectRuntimeException e){
			return;
		}
	}

	/**
	 * 
	 * @param chunkIndex
	 * @param position 基于当前chunk的拆分的位置。单位秒。
	 */
	public void splitChunk(int chunkIndex, float position){
		if(mProjectItemList == null || mProjectItemList.size() <=3){
			return ;
		}

		if(!mAfterEffect.canSplit(chunkIndex, position)){
			return;
		}
		try{
			mAfterEffect.editSplitChunk(chunkIndex, position);
		}catch(InvalidLengthException | EffectRuntimeException e){
			if(e instanceof InvalidLengthException){
			}else if(e instanceof EffectRuntimeException){
			}
			return;
		}
		mAdapter.notifyDataSetChanged();
	}

	private void play() {
		try {
			mAfterEffect.play();
		} catch (InvalidVideoSourceException e) {
			e.printStackTrace();
		}
	}

	private void pause() {
		mAfterEffect.playPause();
	}

	private void initPlayer() {
		mVideoThumeIv = (ImageView) findViewById(R.id.iv_video_thumb);
		mVideoPlayIv = (ImageView) findViewById(R.id.iv_video_play);

		mVideoThumeIv.setOnClickListener(this);
		mVideoPlayIv.setOnClickListener(this);

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

		mAfterEffect = new AfterEffect(this, mGLSurfaceView, this, width, height);
		mProject = mAfterEffect.getProject();

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
//				case PRO:
//					if (progress >= MAX_PROGRESS) {
//						// 锟斤拷锟斤拷锟斤拷锟斤拷
//						progress = 0;
//						progressDialog.dismiss();// 锟斤拷俣曰锟斤拷锟�//					} else {
//						progress++;
//						progressDialog.setProgress(progress);
//						// 锟接迟凤拷锟斤拷锟斤拷息
//						handler.sendEmptyMessageDelayed(PRO, 100);
//					}
//					break;
//				case UPDATE_VIEW:
//					if (canUpdateView) {
//
//						// 锟斤拷锟斤拷锟斤拷图
//						updateView(msg.arg1);
//
//						canUpdateView = false;
//						canTimer = true;
//					} else {
//						if (canTimer) {
//							// 写一锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷时锟斤拷唯一
//							new Thread(new Runnable() {
//
//								@Override
//								public void run() {
//									try {
//										canTimer = false;
//										Thread.sleep(50);
//										canUpdateView = true;
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}
//								}
//							}).start();
//						}
//					}
//					break;
//				case SRCOVIEW_STOP: {
//					int lastX = (int) msg.obj;
//					if (lastX == contentScrollView.getScrollX()) {
//						handleStop(contentScrollView);
//					} else {
//						handler.sendMessageDelayed(handler.obtainMessage(SRCOVIEW_STOP, contentScrollView.getScrollX()), 5);
//
//					}
//				}
//					break;
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

	private void handlerAECallBack(Message msg) {

		switch (msg.what) {

		case MSG_AE_PLAY_STARTED:
			Log.d(TAG, "MSG_AE_PLAY_STARTED");
			currentPlayPosition = 0;
			break;

		case MSG_AE_PLAY_PROGRESS:
		{
			ChunkPlayBean playBean = (ChunkPlayBean)msg.obj;
			float currentPos = playBean.currentSec;
			float totalSec = playBean.totalSec;
			float chunkPosition = playBean.position;
			AfterEffect afterEffect = playBean.effect;
			int chunkIndex = playBean.chunkIndex;

			Log.d(TAG, "MSG_AE_PLAY_PROGRESS " + currentPos + "/" + totalSec + "/" + currentPlayPosition);
			int totalWidth = VideoEditUtils.ChunkTime2Width(totalSec, mImageWidth);

			float delta = currentPos - currentPlayPosition;
			currentPlayPosition = currentPos;

			float offset = delta * totalWidth / totalSec;
			final float realOff = offset + mCEValue;
			if((int)realOff >= 1) {
				// Worth scroll
				mAERecyclerView.post(new Runnable() {
					@Override
					public void run() {
						mAERecyclerView.scrollBy((int)realOff, 0);
					}
				});
				// restore and remember new CE
				mCEValue = (realOff - (int)realOff);
			} else {
				mCEValue = realOff;
			}

			List<Chunk> chunkList = afterEffect.getMainChunks();
			Chunk chunk = chunkList.get(chunkIndex);

			Log.d(TAG, "time line scroll params: offset=" +
					offset + ", realOff=" + realOff +
					", mCEValue=" + mCEValue + ", chunkPosition=" + chunkPosition +
					", chunk duration=" + chunk.getDuration());

			if(chunk.getDuration() - chunkPosition < 0.001) {
				mAERecyclerView.post(new Runnable() {
					@Override
					public void run() {
						mAERecyclerView.scrollBy(mTransitionWidth, 0);
					}
				});
				mCEValue = 0f;
			}
		}
			break;
		case MSG_AE_PLAY_FINISHED:
			Log.d(TAG, "MSG_AE_PLAY_FINISHED");
			break;
		case MSG_AE_PLAY_FAILED:
		{
			Log.d(TAG, "MSG_AE_PLAY_FAILED");
			float currentPos = -1;
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
			break;
		case MSG_AE_EXPORT_FAILED:
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
			String filePath=(String)msg.obj;
			Log.d(TAG, "chunk added fialed:" + filePath);
			break;
		}

		case MSG_AE_BITMAP_READ_OUT: {
			Chunk chunk = (Chunk) msg.obj;

			// Get to insert index
			ChunkBean chunkBean = new ChunkBean();
			chunkBean.chunk = chunk;
			chunkBean.index_tag = VideoEditUtils.generateIndexTag(mProjectItemList);
			chunkBean.width = VideoEditUtils.ChunkTime2Width(chunk.getDuration(), mImageWidth);
			chunkBean.isEditState = false;

			int cInsertIndex = mProjectItemList.size() - 2;
			chunkBean.ct_pair_tag = cInsertIndex + "chunkIndex";
			mProjectItemList.add(cInsertIndex, chunkBean);

			// truncate to the end
			Transition transtion = mAfterEffect.getTransition(
					VideoEditUtils.mapI2CIndex(cInsertIndex), true);
//			if(transtion != null) {
			TransitionBean transitionBean = new TransitionBean();
			transitionBean.index_tag = VideoEditUtils.generateIndexTag(mProjectItemList);
			transitionBean.transiton = transtion;
			int tInsertIndex = mProjectItemList.size() - 2;
			transitionBean.ct_pair_tag = cInsertIndex + "chunkIndex";
			mProjectItemList.add(tInsertIndex, transitionBean);
//			}

			mAdapter.setData(mProjectItemList);
			mAdapter.notifyDataSetChanged();
			break;
		}

		default:
			Log.d(TAG, "unknown operation happened");
			break;
		}
	}

	private void initController(){
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
		AEMusicAdapter mAeMusicAdapter = new AEMusicAdapter();
		mAEMusicRecyclerView.setAdapter(mAeMusicAdapter);
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
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

				/** 如果来自用户对seekbar的操作，则记录progress。如果来自代码调用setProgress()则不记录 */
				if(fromUser){
					mCurrVolumeProgress = progress;
				}
				mAEVolumePercentTv.setText(progress + "%");

				if(progress == 0){
					mAEVolumeSettingIv.setImageDrawable(AfterEffectActivity.this.getResources().getDrawable(R.drawable.ic_ae_volume_closed));
					isMute = true;
				}else{
					mAEVolumeSettingIv.setImageDrawable(AfterEffectActivity.this.getResources().getDrawable(R.drawable.ic_ae_volume));
					isMute = false;
				}

				
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_after_effect);

		// default tail and footer
		mProjectItemList = new ArrayList<ProjectItemBean>();
		DummyHeaderBean headerBean = new DummyHeaderBean();
		headerBean.index_tag = VideoEditUtils.generateIndexTag(mProjectItemList);
		mProjectItemList.add(headerBean);
		TailBean tailBean = new TailBean();
		tailBean.index_tag = VideoEditUtils.generateIndexTag(mProjectItemList);
		mProjectItemList.add(tailBean);
		DummyFooterBean footerBean = new DummyFooterBean();
		footerBean.index_tag = VideoEditUtils.generateIndexTag(mProjectItemList);
		mProjectItemList.add(footerBean);

		mAELayoutManager = new LinearLayoutManager(this);
		mAELayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mAERecyclerView = (RecyclerView) findViewById(R.id.rv_video_edit_pic_list);

		mImageHeight = DeviceUtil.dp2px(this, VideoEditConstant.BITMAP_COMMON_WIDTH);
		mImageWidth = mImageHeight;
		mTimeLineGateV = findViewById(R.id.v_time_line_gate);
		mTimeLineGateV.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					public void onGlobalLayout() {
						mTimeLineGateV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

						int[] locations = new int[2];
						mTimeLineGateV.getLocationOnScreen(locations);
						int x = locations[0];
						int y = locations[1];
						mGateLocationX = x;
						Log.d(TAG, "Base UI data: mGateLocationX=" + mGateLocationX);
					}
				});
		mTransitionWidth = DeviceUtil.dp2px(this, VideoEditConstant.TRANSITION_COMMON_WIDTH);
		mAERecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//				Log.d(TAG, "time line scrolled: dx=" + dx + ", dy=" + dy);

				int firstVisibleIndex = mAELayoutManager.findFirstVisibleItemPosition();
				int lastVisibleIndex = mAELayoutManager.findLastVisibleItemPosition();
//				Log.d(TAG, "first visible index=" + firstVisibleIndex + ", lastVisibleIndex=" + lastVisibleIndex);
				for(int i = firstVisibleIndex; i <= lastVisibleIndex; i++) {
					View view = mAELayoutManager.findViewByPosition(i);

					if(view.getId() == R.id.ll_ae_data_transition) {
						int pX = VideoEditUtils.getViewXLocation(view);
						Log.d(TAG, "Transition scrolled to: pX=" + pX);

						if(VideoEditUtils.judgeGateOverlap(mGateLocationX, pX, mTransitionWidth)) {
							// Skip seek player
						}
					}
				}
			}
		});
		initPlayer();
		mAdapter = new ChannelLineAdapter(this, mAERecyclerView, mProjectItemList, mAfterEffect);
		mAERecyclerView.setAdapter(mAdapter);
		mAERecyclerView.setLayoutManager(mAELayoutManager);
		mAERecyclerView.setItemAnimator(new DefaultItemAnimator());

		initController();
	}

	public float getChannelDuration() {
		return mAfterEffect.getDuration();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.example, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_layout_grid:
			item.setChecked(true);
			play();
			break;
		case R.id.action_layout_linear:
			item.setChecked(true);
			pause();
			break;
		case R.id.action_layout_staggered:
			item.setChecked(true);
			break;
		case R.id.action_layout_tail:
			mAfterEffect.setDateString("2016.04.15");
			break;
		case R.id.action_layout_music:
			mAfterEffect.editBackgroundMusic(mMusicPath);
			item.setChecked(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

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

	@Override
	public void onStartToPlay(AfterEffect afterEffcet) {
		Message msg = handler.obtainMessage(MSG_AE_PLAY_STARTED, afterEffcet);
		handler.sendMessage(msg);
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

		Message msg = handler.obtainMessage(MSG_AE_PLAY_PROGRESS, 0, 0, bean);
		handler.sendMessage(msg);
	}

	@Override
	public void onPlayFinished(AfterEffect afterEffcet) {
		Message msg = handler.obtainMessage(MSG_AE_PLAY_FINISHED, afterEffcet);
		handler.sendMessage(msg);
	}

	@Override
	public void onPlayingFailed(AfterEffect afterEffcet, EffectException e) {
		Message msg = handler.obtainMessage(MSG_AE_PLAY_FAILED, afterEffcet);
		handler.sendMessage(msg);
	}

	@Override
	public void onStartToExport(AfterEffect afterEffcet) {
		Message msg = handler.obtainMessage(MSG_AE_EXPORT_STARTED, afterEffcet);
		handler.sendMessage(msg);
	}

	@Override
	public void onExporting(AfterEffect afterEffcet, float rate) {
		Message msg = handler.obtainMessage(MSG_AE_EXPORT_PROGRESS, (int) (rate * 100), 0, afterEffcet);
		handler.sendMessage(msg);
	}

	@Override
	public void onExportFinished(AfterEffect afterEffcet) {
		Message msg = handler.obtainMessage(MSG_AE_EXPORT_FINISHED, afterEffcet);
		handler.sendMessage(msg);
	}

	@Override
	public void onExportFailed(AfterEffect afterEffcet, EffectException e) {
		Message msg = handler.obtainMessage(MSG_AE_EXPORT_FAILED, afterEffcet);
		handler.sendMessage(msg);
	}
	
	@Override
	protected void onDestroy() {
		if(mAfterEffect==null){
			mAfterEffect.release();
		}
		super.onDestroy();
	}

	@Override
	public void onGeneratedThumbs(AfterEffect ae, Chunk chunk) {
		// TODO Auto-generated method stub
		Message msg = handler.obtainMessage(MSG_AE_BITMAP_READ_OUT, chunk);
		handler.sendMessage(msg);
	}

	@Override
	public void onChunkAddedFinished(AfterEffect self, Project project,
			Chunk chunk) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onChunkAddedFinished");
		Message msg = handler.obtainMessage(MSG_AE_CHUNK_ADD_FINISHED, chunk);
		handler.sendMessage(msg);
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
	}

	@Override
	public void onResume() {
		super.onResume();
		mAfterEffect.onActivityResume();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int vId = v.getId();
		if(vId == R.id.iv_video_play){
			mVideoPlayIv.setVisibility(View.GONE);
			mVideoThumeIv.setVisibility(View.GONE);
			play();
		}else if (vId == R.id.iv_video_thumb){
//			mVideoPlayIv.setVisibility(View.GONE);
//			mVideoThumeIv.setVisibility(View.GONE);	
		}else if (vId == R.id.ll_ae_volume){

			mAEVolumeLayout.setBackgroundColor(AfterEffectActivity.this.getResources().getColor(R.color.ae_controller_pressed));
			mAECutLayout.setBackgroundResource(R.drawable.ae_controller_bg);
			//(AfterEffectActivity.this.getResources().getColor(R.color.ae_controller_normal));

			mAESplitAndDeleteLayout.setVisibility(View.GONE);
			mAEVolumeSettingLayout.setVisibility(View.VISIBLE);

		}else if(vId == R.id.ll_ae_cut){

			mAEVolumeLayout.setBackgroundResource(R.drawable.ae_controller_bg);
			mAECutLayout.setBackgroundColor(AfterEffectActivity.this.getResources().getColor(R.color.ae_controller_pressed));

			if(mAEVolumeSettingLayout.getVisibility() == View.VISIBLE){
				mAEVolumeSettingLayout.setVisibility(View.GONE);
				mAESplitAndDeleteLayout.setVisibility(View.VISIBLE);
			}

		}else if(vId == R.id.ll_ae_split){

			splitChunk(0,1);

		}else if(vId == R.id.ll_ae_delete){
			removeChunk(0);
		}else if(vId == R.id.iv_ae_volume_setting){
			if(isMute){
				mAEVolumeSeekBar.setProgress(mCurrVolumeProgress);
			}else{
				mAEVolumeSeekBar.setProgress(0);
			}
		}

	}

	@Override
	public void onPlayPaused(AfterEffect afterEffcet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayResume(AfterEffect afterEffect) {
		// TODO Auto-generated method stub
		
	}

	public void goToChooseVideo(){
		Intent videoChooseIntent = new Intent();
		videoChooseIntent.setClass(this, VideoChooserActivity.class);
		startActivityForResult(videoChooseIntent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode == RESULT_OK){
			Bundle b = data.getExtras(); //data为B中回传的Intent
		    String vidPath = b.getString("vidPath");//str即为回传的值
		    if(vidPath != null){
		    	mAdapter.addChunk(vidPath);
		    }
		}
	}

}
