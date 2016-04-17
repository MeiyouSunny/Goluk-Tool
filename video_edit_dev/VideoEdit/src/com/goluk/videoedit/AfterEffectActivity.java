package com.goluk.videoedit;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.AfterEffectListener;
import cn.npnt.ae.exceptions.EffectException;
import cn.npnt.ae.exceptions.InvalidVideoSourceException;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.ChunkThumbs;
import cn.npnt.ae.model.Project;
import cn.npnt.ae.model.Transition;
import cn.npnt.ae.model.VideoThumb;

import com.goluk.videoedit.adapter.AEMusicAdapter;
import com.goluk.videoedit.adapter.ProjectLineAdapter;
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
	private GLSurfaceView glSurfaceView;
	AfterEffect mAfterEffect;
	Project mProject;
	int imageHeight;
	List<ProjectItemBean> mProjectItemList;
	Handler handler;
	private ProjectLineAdapter mAdapter;
	private FrameLayout mSurfaceLayout;

	ImageView mVideoThumeIv;
	ImageView mVideoPlayIv;

	RelativeLayout mAEVolumeSettingLayout;
	ImageView mAEVolumeSettingIv;
	TextView mAEVolumePercentTv;
	SeekBar mAEVolumeSeekBar;

	LinearLayout mAESplitAndDeleteLayout;
	LinearLayout mAESplitLayout;
	LinearLayout mAEDeleteLayout;
	LinearLayout mAECutLayout;
	LinearLayout mAEVolumeLayout;

//	String mVideoPath = "/storage/emulated/0/goluk/video/wonderful/WND_event_20160406121432_1_TX_3_0012.mp4";
//	String mVideoPath1 = "/storage/emulated/0/goluk/video/wonderful/WND_event_20160406204409_1_TX_3_0012.mp4";

	/** htc d820u */
	String mVideoPath = "/storage/emulated/0/goluk/video/wonderful/WND_event_20160331125315_1_TX_3_0012.mp4";
	String mVideoPath1 = "/storage/emulated/0/goluk/video/wonderful/WND_event_20160401124245_1_TX_3_0012.mp4";
	String mMusicPath = "/storage/emulated/0/qqmusic/song/500miles.mp3";
	private void startParse() {
		if (mVideoPath != null) {
			try {
				mAfterEffect.editAddChunk(mVideoPath, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
		glSurfaceView = new GLSurfaceView(this);
		mSurfaceLayout.addView(glSurfaceView, 0);
		glSurfaceView.setEGLContextClientVersion(2);
		glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

		int screenWidthSize = DeviceUtil.getScreenWidthSize(this);
		int width = screenWidthSize;
		int height = width / 16 * 9;
		LayoutParams params = mSurfaceLayout.getLayoutParams();
		params.height = height;

		mAfterEffect = new AfterEffect(this, glSurfaceView, this, width, height);
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

		startParse();
	}

	private final static String TAG = "CK1";
	private float currentPlayPosition = -1;

	private void handlerAECallBack(Message msg) {

		switch (msg.what) {

		case MSG_AE_PLAY_STARTED:
			Log.d(TAG, "MSG_AE_PLAY_STARTED");
			currentPlayPosition = 0;
			break;

		case MSG_AE_PLAY_PROGRESS:
			float currentPos = ((float) msg.arg1) / 100;
			float totalSec = ((float) msg.arg2) / 100;
			Log.d(TAG, "MSG_AE_PLAY_PROGRESS " + currentPos + "/" + totalSec);
			float delta = currentPos - currentPlayPosition;
			final int scrollX = (int)(delta / totalSec * mAERecyclerView.getWidth());
			currentPlayPosition = currentPos;

			mAERecyclerView.post(new Runnable() {
				@Override
				public void run() {
					mAERecyclerView.scrollBy(scrollX, 0);
				}
			});
			break;
		case MSG_AE_PLAY_FINISHED:
			Log.d(TAG, "MSG_AE_PLAY_FINISHED");
			break;
		case MSG_AE_PLAY_FAILED:
			Log.d(TAG, "MSG_AE_PLAY_FAILED");
			currentPos = -1;
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
			Chunk chunk=(Chunk)msg.obj;
			if(chunk!=null){
				Log.d(TAG, "chunk added:" + chunk.prettyString());
				 mAfterEffect.generateThumbAsyn(chunk, VideoEditConstant.BITMAP_TIME_INTERVAL, imageHeight);
			}
			break;
		}

		case MSG_AE_CHUNK_ADD_FAILED: {
			String filePath=(String)msg.obj;
			Log.d(TAG, "chunk added fialed:" + filePath);
			break;
		}

		case MSG_AE_BITMAP_READ_OUT: {
//			String filePath=(String)msg.obj;
//			Log.d(TAG, "chunk added fialed:" + filePath);
			Chunk chunk = (Chunk) msg.obj;
			ChunkThumbs chunkThumbs = chunk.getChunkThumbs();
			ArrayList<VideoThumb> thumbList = chunkThumbs.getThumbs();
			//VideoThumb thumb = thumbList.get(0);
			//thumb.getBitmap();
//			if(thumbList != null && thumbList.size() > 0) {
//				for(int i = 0; i < thumbList.size(); i++) {
//					VideoThumb thumb = thumbList.get(i);
//					BitmapWrapper wrapper = new BitmapWrapper();
//					wrapper.bitmap = thumb.getBitmap();
//					wrapper.index = i;
////					mVideoBitmapList.add(thumb.getBitmap());
//					mVideoBitmapList.add(wrapper);
//				}
//			}

			// Get to insert index
			{
				ChunkBean chunkBean = new ChunkBean();
				chunkBean.chunk = chunk;
				chunkBean.index_tag = VideoEditUtils.generateIndexTag(mProjectItemList);
				int insertIndex = mProjectItemList.size() - 2;
				mProjectItemList.add(insertIndex, chunkBean);
			}

			{
				TransitionBean transitionBean = new TransitionBean();
				Transition transtion = Transition.createNoneTransition();
				transitionBean.index_tag = VideoEditUtils.generateIndexTag(mProjectItemList);
				transitionBean.transiton = transtion;
				int insertIndex = mProjectItemList.size() - 2;
				mProjectItemList.add(insertIndex, transitionBean);
			}
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
		mAEDeleteLayout = (LinearLayout) findViewById(R.id.ll_ae_split_and_delete);
		mAECutLayout = (LinearLayout) findViewById(R.id.ll_ae_cut);
		mAEVolumeLayout = (LinearLayout) findViewById(R.id.ll_ae_volume);
		
		mAEMusicLayoutManager = new LinearLayoutManager(this);
		mAEMusicLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mAEMusicRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_ae_music);
		AEMusicAdapter mAeMusicAdapter = new AEMusicAdapter();
		mAEMusicRecyclerView.setAdapter(mAeMusicAdapter);
		mAEMusicRecyclerView.setLayoutManager(mAEMusicLayoutManager);

		mAEVolumeLayout.setOnClickListener(this);

		mAEVolumeSeekBar.setMax(100);
		mAEVolumeSeekBar.setProgress(100);
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
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				mAEVolumePercentTv.setText(progress + "%");
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_after_effect);

//		List<ProjectItemBean> data = new ArrayList<ProjectItemBean>();
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
		mAdapter = new ProjectLineAdapter(this, mAERecyclerView, mProjectItemList);
//		mProjectItemList = data;
		mAERecyclerView.setAdapter(mAdapter);
		mAERecyclerView.setLayoutManager(mAELayoutManager);
		mAERecyclerView.setItemAnimator(new DefaultItemAnimator());

		imageHeight = DeviceUtil.dp2px(this, 45);
		initPlayer();

		initController();
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
//			startParse();
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

	@Override
	public void onPlaying(AfterEffect afterEffcet, float currentSec, float totalSec) {
		Message msg = handler.obtainMessage(MSG_AE_PLAY_PROGRESS, (int) (currentSec * 100), (int) (totalSec * 100), afterEffcet);
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
			mAESplitAndDeleteLayout.setVisibility(View.GONE);
			mAEVolumeSettingLayout.setVisibility(View.VISIBLE);
		}
	}

}
