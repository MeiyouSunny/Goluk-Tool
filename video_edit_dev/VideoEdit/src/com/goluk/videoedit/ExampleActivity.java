package com.goluk.videoedit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import utils.DeviceUtil;
import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.AfterEffectListener;
import cn.npnt.ae.exceptions.EffectException;
import cn.npnt.ae.exceptions.InvalidVideoSourceException;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.ChunkThumbs;
import cn.npnt.ae.model.Project;
import cn.npnt.ae.model.VideoThumb;

import com.makeramen.dragsortadapter.example.ExampleAdapter;

public class ExampleActivity extends Activity implements AfterEffectListener {
	RecyclerView mRecyclerView;
	LinearLayoutManager mLayoutManager;
	Handler mPlaySyncHandler;
//	private FrameLayout videoSurLayout;
	private GLSurfaceView glSurfaceView;
	AfterEffect afterEffect;
	Project project;
	int imageHeight;
	List<Bitmap> mVideoBitmapList;
	Handler handler;
	private ExampleAdapter mAdapter;
	private FrameLayout mSurfaceLayout;

	String mVideoPath = "/storage/emulated/0/goluk/video/urgent/URG_event_20160218175931_1_TX_1_0015.mp4";
	String mVideoPath1 = "/storage/emulated/0/goluk/video/wonderful/WND3_160104165107_0012.mp4";
	private void startParse() {
		if (mVideoPath != null) {
			try {
				afterEffect.editAddChunk(mVideoPath, 0);

//				afterEffect.generateThumbAsyn(chunk, 2, imageHeight);
//				if (this.chunkThumbList == null)
//					chunkThumbList = new ArrayList<ChunkThumbs>();
//				chunkThumbList.add(chunkThumb);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void play() {
		try {
			afterEffect.play();
		} catch (InvalidVideoSourceException e) {
			e.printStackTrace();
		}
	}

	private void pause() {
		afterEffect.playPause();
	}
	private void initPlayer() {
		mSurfaceLayout = (FrameLayout)findViewById(R.id.fl_video_sur_layout);
		glSurfaceView = new GLSurfaceView(this);
		mSurfaceLayout.addView(glSurfaceView);
		glSurfaceView.setEGLContextClientVersion(2);
		glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

		int screenWidthSize = DeviceUtil.getScreenWidthSize(this);
		int width = screenWidthSize;
		int height = width / 16 * 9;
		LayoutParams params = mSurfaceLayout.getLayoutParams();
		params.height = height;

		afterEffect = new AfterEffect(this, glSurfaceView, this, width, height);
		project = afterEffect.getProject();

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
//				case PRO:
//					if (progress >= MAX_PROGRESS) {
//						// 重新设置
//						progress = 0;
//						progressDialog.dismiss();// 销毁对话框
//					} else {
//						progress++;
//						progressDialog.setProgress(progress);
//						// 延迟发送消息
//						handler.sendEmptyMessageDelayed(PRO, 100);
//					}
//					break;
//				case UPDATE_VIEW:
//					if (canUpdateView) {
//
//						// 更新视图
//						updateView(msg.arg1);
//
//						canUpdateView = false;
//						canTimer = true;
//					} else {
//						if (canTimer) {
//							// 写一个定时器，定时器唯一
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
		mVideoBitmapList = new ArrayList<Bitmap>();
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
			final int scrollX = (int)(delta / totalSec * mRecyclerView.getWidth());
			currentPlayPosition = currentPos;

			mRecyclerView.post(new Runnable() {
				@Override
				public void run() {
					mRecyclerView.scrollBy(scrollX, 0);
//					mRecyclerView.scrollTo(2, 2);
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
				 afterEffect.generateThumbAsyn(chunk, 2, imageHeight);
				
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
			if(thumbList != null && thumbList.size() > 0) {
				for(int i = 0; i < thumbList.size(); i++) {
					VideoThumb thumb = thumbList.get(i);
					mVideoBitmapList.add(thumb.getBitmap());
				}
			}
			mAdapter.setData(mVideoBitmapList);
			mAdapter.notifyDataSetChanged();
			break;

		}

		default:
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_example);

		int dataSize = 100;
		List<Bitmap> data = new ArrayList<Bitmap>(dataSize);
//		for (int i = 1; i < dataSize + 1; i++) {
//			data.add(i);
//		}

		mLayoutManager = new LinearLayoutManager(this);
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecyclerView = (RecyclerView) findViewById(R.id.rv_video_edit_pic_list);
		mAdapter = new ExampleAdapter(mRecyclerView, data);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setLayoutManager(mLayoutManager);
//		recyclerView.setItemAnimator(new DefaultItemAnimator());

//		mPlaySyncHandler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//				case 3003:
//					mRecyclerView.post(new Runnable() {
//					@Override
//					public void run() {
//						mRecyclerView.scrollBy(20, 0);
//					}
//				});
//					break;
//				default:
//					break;
//				}
//				super.handleMessage(msg);
//			}
//		};

//		Thread thread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				for(int i = 0; i < 1000; i++) {
//					mRecyclerView.post(new Runnable() {
//						@Override
//						public void run() {
//							mRecyclerView.scrollBy(20, 0);
//						}
//					});
////					mPlaySyncHandler.removeMessages(3003);
////					Message msg = mPlaySyncHandler.obtainMessage(3003);
////					mPlaySyncHandler.sendMessage(msg);
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		});
//		thread.start();
		imageHeight = DeviceUtil.dp2px(this, 40);
		initPlayer();
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
//		case R.id.action_layout_staggered:
//			item.setChecked(true);
//			recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,
//					StaggeredGridLayoutManager.VERTICAL));
//			break;
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
		if(afterEffect==null){
			afterEffect.release();
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
		Log.d("CK1", "onChunkAddedFinished");
		Message msg = handler.obtainMessage(MSG_AE_CHUNK_ADD_FINISHED, chunk);
		handler.sendMessage(msg);
	}

	@Override
	public void onChunkAddedFailed(AfterEffect self, Project project,
			String filePath) {
		// TODO Auto-generated method stub
	}
}
