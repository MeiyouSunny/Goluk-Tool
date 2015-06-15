package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.video.MVListAdapter;
import cn.com.mobnote.video.MVManage;
import cn.com.mobnote.video.MVManage.MVEditData;
import cn.com.mobnote.view.MyGridView;
import cn.com.tiros.debug.GolukDebugUtils;

import com.rd.car.editor.Constants;
import com.rd.car.editor.EditorParam;
import com.rd.car.editor.FilterPlaybackView;
import com.rd.car.editor.FilterVideoEditorException;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:Goluk视频编辑页面
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class VideoEditActivity extends BaseActivity implements OnClickListener {
	/** 视频编辑页面handler用来接收消息,更新UI */
	public static Handler mVideoEditHandler = null;
	/** mv滤镜appter */
	public MVListAdapter mMVListAdapter = null;
	/** 自定义播放器支持特效 */
	public FilterPlaybackView mVVPlayVideo = null;
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	/** 视频路径 */
	private String mFilePath = "";
	/** 播放按钮 */
	private RelativeLayout mPlayLayout = null;
	/** 播放状态图片 */
	private ImageView mPlayStatusImage = null;
	/** loading布局 */
	private RelativeLayout mVideoLoadingLayout = null;
	/** loading图片 */
	private ImageView mLoadingImage = null;
	/** loading文本 */
	private TextView mLoadingText = null;
	/** 进度条 */
	private ProgressBar mVideoProgressBar = null;
	/** mv列表layout */
	private LinearLayout mMVListLayout = null;
	/** loading动画 */
	private AnimationDrawable mLoadingAnimation = null;
	/** 滤镜保存视频路径 */
	private String mVideoSavePath = null;
	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = android.os.Environment.getExternalStorageDirectory().getPath();
	private String mNewVideoFilePath = APP_FOLDER + "/" + "goluk/";
	/** 进度条线程 */
	private Thread mProgressThread = null;
	/** 当前编辑的视频类型 3 紧急 2 精彩 */
	private int mCurrentVideoType = 0;
	/** 分享的视频名称 */
	private String videoName = "";
	private boolean isExit = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_edit);

		mContext = this;
		// 获取视频路径
		Intent intent = getIntent();
		mFilePath = intent.getStringExtra("cn.com.mobnote.video.path");
		interceptVideoName(mFilePath);// 拿到视频名称
		mCurrentVideoType = intent.getIntExtra("type", 2);

		mMVListLayout = (LinearLayout) findViewById(R.id.mvlistlayout);

		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "VideoEdit");

		// 页面初始化
		init();
		// 视频初始化
		videoInit();
		// 编辑选项表格
		initVideoEditList();
	}

	/**
	 * 
	 * @Title: interceptVideoName
	 * @Description:
	 * @param videopath
	 *            void
	 * @author 曾浩
	 * @throws
	 */
	private void interceptVideoName(String videopath) {
		if (videopath != null && !"".equals(videopath)) {
			String[] strs = videopath.split("/");
			videoName = strs[strs.length - 1];
			videoName = videoName.replace("mp4", "jpg");
		}
	}

	/**
	 * 页面初始化
	 */
	private void init() {
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mNextBtn = (Button) findViewById(R.id.next_btn);
		mPlayLayout = (RelativeLayout) findViewById(R.id.play_layout);
		mPlayStatusImage = (ImageView) findViewById(R.id.play_image);
		mVideoLoadingLayout = (RelativeLayout) findViewById(R.id.video_loading_layout);
		mLoadingImage = (ImageView) findViewById(R.id.loading_img);
		mLoadingText = (TextView) findViewById(R.id.loading_text);
		mLoadingAnimation = (AnimationDrawable) mLoadingImage.getBackground();

		mVideoProgressBar = (ProgressBar) findViewById(R.id.video_progress_bar);

		// 注册事件
		mBackBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		mPlayLayout.setOnClickListener(this);

	}

	/**
	 * 初始化滤镜布局
	 */
	private void initVideoEditList() {
		MyGridView gridView = createMVGridView();
		MVManage mvManage = new MVManage(mContext);
		ArrayList<MVEditData> list = mvManage.getLocalVideoList();
		mMVListAdapter = new MVListAdapter(mContext, list);
		gridView.setAdapter(mMVListAdapter);
		mMVListLayout.addView(gridView);
	}

	/**
	 * 创建本地滤镜列表布局
	 * 
	 * @return
	 */
	private MyGridView createMVGridView() {
		MyGridView gridLayout = new MyGridView(mContext, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gridLayout.setLayoutParams(lp);
		gridLayout.setBackgroundColor(Color.rgb(237, 237, 237));
		gridLayout.setNumColumns(4);
		gridLayout.setPadding(16, 30, 16, 30);
		gridLayout.setVerticalSpacing(30);
		gridLayout.setHorizontalSpacing(16);
		return gridLayout;
	}

	/**
	 * 视频播放初始化
	 */
	private void videoInit() {
		mVVPlayVideo = (FilterPlaybackView) this.findViewById(R.id.vvPlayVideo);
		// 内置滤镜最大id为Constants.FILTER_ID_WARM
		int nFilterId = Constants.FILTER_ID_WARM + 1;
		// 添加内置滤镜名称，与Constants.*定义对应
		// 添加扩展的滤镜
		mVVPlayVideo.addFilter(nFilterId++, R.raw.gutongse);
		mVVPlayVideo.addFilter(nFilterId++, R.raw.lanseshike);
		mVVPlayVideo.addFilter(nFilterId++, R.raw.youge);

		// 播放器准备过程的回调接口
		mVVPlayVideo.setPlaybackListener(new FilterPlaybackView.FilterPlaybackViewListener() {
			@Override
			public void onPrepared(MediaPlayerControl mpc) {
				// 视频播放已就绪
				GolukDebugUtils.e("", "onPrepared---video---加载完成");
				updateVideoProgress();
			}

			@Override
			public boolean onError(MediaPlayerControl mpc, int nErrorNo, String strErrInfo) {
				// 视频播放出错
				GolukUtils.showToast(VideoEditActivity.this, "视频播放出错,errorNo: " + nErrorNo + ",info: " + strErrInfo);
				return false;
			}

			@Override
			public void onCompletion(MediaPlayerControl mpc) {
				// 视频播放完成
				mVideoProgressBar.setProgress(mVVPlayVideo.getDuration());
			}
		});

		try {
			// 设置视频源
			mVVPlayVideo.setVideoPath(mFilePath);
			mVVPlayVideo.switchFilterId(0);
			mVVPlayVideo.start();

		} catch (FilterVideoEditorException e) {
			e.printStackTrace();
			GolukUtils.showToast(this, e.getMessage());
		}
	}

	/**
	 * 重置视频播放状态
	 */
	private void changeVideoPlayState() {
		if (mVVPlayVideo.isPlaying()) {
			// 显示播放图片
			mPlayStatusImage.setVisibility(View.VISIBLE);
			// 如果正在播放视频，则暂停播放
			mVVPlayVideo.pause();
		}
	}

	/**
	 * 保存视频
	 */
	private void onSaveVideo() {
		try {
			// 创建保存视频参数，默认参数为 输出size为480*480,码率为512k，帧率为21的视频
			EditorParam editorParam = new EditorParam();
			// 高清
			editorParam.nVideoWidth = 854;
			editorParam.nVideoHeight = 480;
			// //分辨率 帧率 码率 480*270 30fps 1400kbps
			editorParam.nVideoBitrate = 1500 * 1024;
			editorParam.nFps = 15;

			mVideoSavePath = mNewVideoFilePath + "newvideo.mp4";
			mVVPlayVideo.saveVideo(mVideoSavePath, editorParam, new FilterPlaybackView.FilterVideoEditorListener() {

				@Override
				public void onFilterVideoSaveStart() {
					showLoadingView();
				}

				@Override
				public boolean onFilterVideoSaving(int nProgress, int nMax) {
					if (nProgress > 0) {
						mLoadingText.setText("视频生成中" + nProgress + "%");
					}
					// 返回false代表取消保存。。。
					return true;
				}

				@Override
				public void onFilterVideoEnd(boolean bSuccess, boolean bCancel) {

					GolukDebugUtils.e("", "VideoEditActivity---------onFilterVideoEnd- sucess:" + bSuccess
							+ "  cancel:" + bCancel);

					hideLoadingView();
					if (bCancel) {
						// strInfo = "已取消视频保存！";
					} else if (bSuccess) {
						// 视频保存成功,跳转到分享页面
						toShareActivity(mVideoSavePath);
					}

					if (null != mVVPlayVideo && mVVPlayVideo.needReload()) {
						try {
							mVVPlayVideo.reload();
						} catch (FilterVideoEditorException e) {
							GolukUtils.showToast(VideoEditActivity.this, "重加载视频失败，" + e.getMessage());
						}
					}
				}

				@Override
				public void onFilterVideoSaveError(int nErrorType, int nErrorNo, String strErrorInfo) {
					GolukUtils.showToast(VideoEditActivity.this, "保存视频失败，" + strErrorInfo);
					hideLoadingView();
				}
			});
		} catch (FilterVideoEditorException e) {
			GolukUtils.showToast(this, "保存视频失败，" + e.getMessage());
			hideLoadingView();
		}
	}

	private void showLoadingView() {
		// 显示视频导出loading
		mVideoLoadingLayout.setVisibility(View.VISIBLE);
		// 启动loading动画
		mLoadingAnimation.start();
	}

	private void hideLoadingView() {
		mVideoLoadingLayout.setVisibility(View.GONE);
		mLoadingText.setText("视频生成中" + 0 + "%");
		// 停止loading动画
		mLoadingAnimation.stop();
	}

	/**
	 * 停止进度条线程
	 */
	private void stopProgressThread() {
		Thread tmpThread = mProgressThread;
		mProgressThread = null;
		if (tmpThread != null) {
			tmpThread.interrupt();
		}
	}

	/**
	 * 更新播放进度
	 * 
	 */
	private void updateVideoProgress() {
		// 启动一个新线程用于更新进度条
		mProgressThread = new Thread() {
			public void run() {
				// 视频的总长度
				if (isExit) {
					return;
				}
				int maxDuration = mVVPlayVideo.getDuration();
				while (null != mProgressThread && null != mVVPlayVideo) {
					if (isExit) {
						break;
					}
					// 设置进度条的长度为视频的总长度
					mVideoProgressBar.setMax(maxDuration);
					// 如果视频正在播放而且进度条没有被拖动
					if (mVVPlayVideo.isPlaying()) {
						// 设置进度条的当前进度为视频已经播放的长度
						int position = mVVPlayVideo.getCurrentPosition();
						mVideoProgressBar.setProgress(position);
					}
					try {
						// 休眠50毫秒
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		mProgressThread.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void exit() {
		isExit = true;
		if (null != mVideoLoadingLayout) {
			// 判断是否正在上传
			int t = mVideoLoadingLayout.getVisibility();
			if (t == 0) {
				// 正在上传
				mVideoLoadingLayout.setVisibility(View.GONE);

				// 为了修复上传的是时返回几率崩溃控制针问题.
				// 感觉可能崩溃到这里了,chenxy 5.11
				if (null != mVVPlayVideo) {
					mVVPlayVideo.cancelSave();
				}
			}
		}
		if (null != mVVPlayVideo) {
			mVVPlayVideo.cleanUp();
			mVVPlayVideo = null;
		}
		finish();
	}

	@Override
	protected void onPause() {
		if (mVVPlayVideo != null) {
			if (mVVPlayVideo.isPlaying()) {
				mVVPlayVideo.stop();
				// 显示图片
				mPlayStatusImage.setVisibility(View.VISIBLE);
			}
			mVVPlayVideo.onPause();
		}
		// 停止进度条线程
		stopProgressThread();
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mVVPlayVideo != null) {
			mVVPlayVideo.onResume();
		}
		if (mLoadingText != null) {
			mLoadingText.setText("视频生成中0%");
		}
		mApp.setContext(this, "VideoEdit");
		super.onResume();
	}

	/**
	 * 跳转到分享界面
	 * 
	 * @param filePath
	 *            文件路径，有可能为原始路径，有可能有添加滤镜后的路径
	 * @author jyf
	 * @date 2015年6月10日
	 */
	private void toShareActivity(String filePath) {
		Intent videoShare = new Intent(mContext, VideoShareActivity.class);
		videoShare.putExtra("cn.com.mobnote.golukmobile.videopath", filePath);
		videoShare.putExtra("type", mCurrentVideoType);
		videoShare.putExtra("videoName", videoName);
		startActivity(videoShare);
	}

	/**
	 * 点击“下一步”
	 * 
	 * @author jyf
	 * @date 2015年6月10日
	 */
	private void click_next() {
		// 下一步,导出视频编码
		// 停止进度条线程
		stopProgressThread();
		// 暂停播放器
		changeVideoPlayState();
		// 如果是精彩视频，并且不添加滤镜，則直接跳转
		if (2 == mCurrentVideoType && 0 == mMVListAdapter.getCurrentResIndex()) {
			// 直接跳转，不需要加滤镜
			toShareActivity(mFilePath);
			return;
		}
		// 保存编辑视频到本地
		onSaveVideo();
	}

	/**
	 * 点击“播放” 或 “暂停”
	 * 
	 * @author jyf
	 * @date 2015年6月10日
	 */
	private void click_play() {
		// 暂停/播放
		if (mVVPlayVideo.isPlaying()) {
			// 停止进度条线程
			stopProgressThread();
			mVVPlayVideo.pause();
			// 显示图片
			mPlayStatusImage.setVisibility(View.VISIBLE);
		} else {
			// 启动进度条线程
			updateVideoProgress();
			mVVPlayVideo.start();
			// 隐藏图片
			mPlayStatusImage.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			// 返回
			exit();
			break;
		case R.id.next_btn:
			click_next();
			break;
		case R.id.play_layout:
			click_play();
			break;
		}
	}
}
