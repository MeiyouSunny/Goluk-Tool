package cn.com.mobnote.golukmobile.startshare;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

import com.rd.car.editor.Constants;
import com.rd.car.editor.FilterPlaybackView;
import com.rd.car.editor.FilterVideoEditorException;

@SuppressLint("HandlerLeak")
public class VideoEditActivity extends BaseActivity implements OnClickListener, ICreateNewVideoFn, IUploadVideoFn {
	/** 视频编辑页面handler用来接收消息,更新UI */
	public static Handler mVideoEditHandler = null;
	/** 自定义播放器支持特效 */
	public FilterPlaybackView mVVPlayVideo = null;
	/** application */
	private GolukApplication mApp = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
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
	/** loading动画 */
	private AnimationDrawable mLoadingAnimation = null;
	/** 进度条线程 */
	private Thread mProgressThread = null;
	/** 当前编辑的视频类型 3 紧急 2 精彩 */
	private int mCurrentVideoType = 0;
	/** 分享的视频名称 */
	private String videoName = "";
	private boolean isExit = false;

	private FrameLayout mMiddleLayout = null;
	private ShareFilterLayout mFilterLayout = null;
	public ShareTypeLayout mTypeLayout = null;
	public InputLayout mInputLayout = null;
	private CreateNewVideo mCreateNewVideo = null;
	private UploadVideo mUploadVideo = null;
	private ShareDeal mShareDealTool = null;

	private boolean misCurrentType = true;

	private LinearLayout mShareTypeLayout = null;
	private ImageView mShareTypeImg = null;
	private TextView mShareSwitchTypeTv = null;

	private LinearLayout mShareFilterLayout = null;
	private ImageView mShareFilterImg = null;
	private TextView mShareSwitchFilterTv = null;

	private RelativeLayout mRootLayout = null;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mYouMengLayout = null;

	/** 请求分享连接Dialog */
	private ProgressDialog mPdsave = null;

	private int resTypeSelectColor = 0;
	private int resTypeUnSelectColor = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mLayoutFlater = LayoutInflater.from(this);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.video_edit, null);
		mYouMengLayout = (RelativeLayout) mRootLayout.findViewById(R.id.shortshare_youmeng_layout);
		setContentView(mRootLayout);
		// 获取视频路径
		Intent intent = getIntent();
		mFilePath = intent.getStringExtra("cn.com.mobnote.video.path");
		interceptVideoName(mFilePath);// 拿到视频名称
		mCurrentVideoType = intent.getIntExtra("type", 2);

		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "VideoEdit");

		mShareDealTool = new ShareDeal(this, mYouMengLayout);

		mFilterLayout = new ShareFilterLayout(this);
		mTypeLayout = new ShareTypeLayout(this);
		mInputLayout = new InputLayout(this, mRootLayout);

		loadRes();
		// 页面初始化
		init();
		// 视频初始化
		videoInit();

		mCreateNewVideo = new CreateNewVideo(this, mVVPlayVideo, this);
		mUploadVideo = new UploadVideo(this, mApp);
		mUploadVideo.setListener(this);
		mBaseHandler.sendEmptyMessageDelayed(100, 100);
	}

	private void loadRes() {
		resTypeSelectColor = this.getResources().getColor(R.color.share_type_select);
		resTypeUnSelectColor = this.getResources().getColor(R.color.share_type_unselect);
	}

	@Override
	protected void hMessage(Message msg) {
		if (null == msg) {
			return;
		}
		final int what = msg.what;
		switch (what) {
		case 100:
			switchMiddleLayout(true, true);
			break;
		default:
			break;
		}
	}

	private void switchTypeUI(boolean isType) {
		if (isType) {
			mShareSwitchTypeTv.setTextColor(resTypeSelectColor);
			mShareSwitchFilterTv.setTextColor(resTypeUnSelectColor);
			mShareTypeImg.setBackgroundResource(R.drawable.share_type_press_icon);
			mShareFilterImg.setBackgroundResource(R.drawable.share_filter_icon);
		} else {
			mShareSwitchTypeTv.setTextColor(resTypeUnSelectColor);
			mShareSwitchFilterTv.setTextColor(resTypeSelectColor);
			mShareTypeImg.setBackgroundResource(R.drawable.share_type_icon);
			mShareFilterImg.setBackgroundResource(R.drawable.share_filter_press_icon);
		}
	}

	private void switchMiddleLayout(final boolean isFirst, final boolean isType) {
		if (!isFirst) {
			if (misCurrentType == isType) {
				return;
			}
		}

		switchTypeUI(isType);

		misCurrentType = isType;
		mMiddleLayout.removeAllViews();
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		if (isType) {
			mMiddleLayout.addView(mTypeLayout.getRootLayout(), lp);
			mTypeLayout.show();
		} else {
			mMiddleLayout.addView(mFilterLayout.getRootLayout(), lp);
		}
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
		mPlayLayout = (RelativeLayout) findViewById(R.id.play_layout);
		mPlayStatusImage = (ImageView) findViewById(R.id.play_image);
		mVideoLoadingLayout = (RelativeLayout) findViewById(R.id.video_loading_layout);
		mLoadingImage = (ImageView) findViewById(R.id.loading_img);
		mLoadingText = (TextView) findViewById(R.id.loading_text);
		mLoadingAnimation = (AnimationDrawable) mLoadingImage.getBackground();

		mVideoProgressBar = (ProgressBar) findViewById(R.id.video_progress_bar);

		// 注册事件
		mBackBtn.setOnClickListener(this);
		mPlayLayout.setOnClickListener(this);

		mMiddleLayout = (FrameLayout) findViewById(R.id.shortshare_operateroot);

		mShareTypeLayout = (LinearLayout) findViewById(R.id.share_type_layout);
		mShareTypeImg = (ImageView) findViewById(R.id.share_type_img);
		mShareSwitchTypeTv = (TextView) findViewById(R.id.share_switch_type);

		mShareFilterLayout = (LinearLayout) findViewById(R.id.share_filter_layout);
		mShareFilterImg = (ImageView) findViewById(R.id.share_filter_img);
		mShareSwitchFilterTv = (TextView) findViewById(R.id.share_switch_filter);

		mShareTypeLayout.setOnClickListener(this);
		mShareFilterLayout.setOnClickListener(this);

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
		if (2 == mCurrentVideoType && 0 == mFilterLayout.mMVListAdapter.getCurrentResIndex()) {
			// 直接跳转，不需要加滤镜
			// toShareActivity(mFilePath);
			this.createNewFileSucess(mFilePath);
			return;
		}

		mCreateNewVideo.onSaveVideo();

		// 保存编辑视频到本地
		// onSaveVideo();
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

	public void shareClick(final String type) {
		GolukDebugUtils.e("", "jyf-----shortshare---VideoEditActivity---------------shareClick---: " + type);
		click_next();
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			// 返回
			 exit();
			break;
		case R.id.play_layout:
			click_play();
			break;
		case R.id.share_type_layout:
			switchMiddleLayout(false, true);
			break;
		case R.id.share_filter_layout:
			switchMiddleLayout(false, false);
			break;
		}
	}

	private void createNewFileSucess(String filePath) {
		GolukUtils.showToast(this, "添加滤镜成功,文件上传");
		GolukDebugUtils.e("", "jyf-----shortshare---VideoEditActivity---------------createNewFileSucess--filePath-: "
				+ filePath);
		this.mUploadVideo.setUploadInfo(filePath, mCurrentVideoType, videoName);
	}

	public void videoUploadCallBack(int success, Object param1, Object param2) {
		if (null != mUploadVideo) {
			mUploadVideo.videoUploadCallBack(success, param1, param2);
		}
	}

	@Override
	public void CallBack_CreateNewVideoFn(int event, Object obj1, Object obj2, Object obj3) {
		switch (event) {
		case EVENT_START:
			showLoadingView();
			break;
		case EVENT_SAVING:
			int progress = (Integer) obj1;
			if (progress > 0) {
				mLoadingText.setText("视频生成中" + progress + "%");
			}
			break;
		case EVENT_END:
			hideLoadingView();
			boolean bSuccess = (Boolean) obj1;
			boolean bCancel = (Boolean) obj2;
			if (bCancel) {
				// strInfo = "已取消视频保存！";
			} else if (bSuccess) {
				// 视频保存成功,跳转到分享页面
				String newFilePath = (String) obj3;
				createNewFileSucess(newFilePath);
				// toShareActivity(newFilePath);
			}

			if (null != mVVPlayVideo && mVVPlayVideo.needReload()) {
				try {
					mVVPlayVideo.reload();
				} catch (FilterVideoEditorException e) {
					GolukUtils.showToast(VideoEditActivity.this, "重加载视频失败，" + e.getMessage());
				}
			}
			break;
		case EVENT_ERROR:
			String errorInfo = "";
			if (null != obj3) {
				errorInfo = (String) obj3;
			}
			GolukUtils.showToast(VideoEditActivity.this, "保存视频失败，" + errorInfo);
			hideLoadingView();
			break;
		default:
			break;
		}

	}

	@Override
	public void CallBack_UploadVideo(int event, Object obj) {
		GolukDebugUtils.e("", "jyf-----shortshare---VideoEditActivity---------------CallBack_UploadVideo--event-: "
				+ event);
		switch (event) {
		case EVENT_EXIT:
			this.finish();
			break;
		case EVENT_UPLOAD_SUCESS:
			// 　文件上传成功，请求分享连接
			GolukUtils.showToast(this, "文件上传成功，去请求分享连接");

			requestShareInfo();
			break;
		}
	}

	// 请求分享信息
	private void requestShareInfo() {

		final String t_vid = this.mUploadVideo.getVideoId();
		final String t_type = "" + (mCurrentVideoType == 2 ? 2 : 1);
		final String selectTypeJson = JsonUtil.createShareType("" + mTypeLayout.getCurrentSelectType());
		final String desc = mTypeLayout.getCurrentDesc();
		final String isSeque = this.mTypeLayout.isOpenShare() ? "1" : "0";
		final String t_thumbPath = mUploadVideo.getThumbPath();

		final String json = JsonUtil.createShareJson(t_vid, t_type, selectTypeJson, desc, isSeque, t_thumbPath);

		GolukDebugUtils.e("", "jyf-----shortshare---VideoEditActivity-----------------click_shares json:" + json);

		boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Share,
				json);
		GolukDebugUtils.e("", "jyf-----VideoShareActivity -----click_shares---b :  " + b);

		if (!b) {
			GolukUtils.showToast(this, "分享失败");
			return;
		}
		showRequestShareDialog();
		GolukDebugUtils.e("", "chxy____VideoShareActivity share11" + json);
	}

	/**
	 * 本地视频分享回调
	 * 
	 * @param json
	 *            ,分享数据
	 */
	public void videoShareCallBack(int success, String json) {
		dimissRequestShareDialog();
		if (1 != success) {
			GolukUtils.showToast(this, "获取视频分享地址失败");
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			System.out.println("分享地址回调:" + json.toString());
			boolean isSucess = obj.getBoolean("success");
			if (!isSucess) {
				GolukUtils.showToast(this, "获取视频分享地址失败");
				return;
			}

			JSONObject dataObj = obj.getJSONObject("data");
			final String shortUrl = dataObj.getString("shorturl");
			final String coverUrl = dataObj.getString("coverurl");

			final String title = "极路客精彩视频分享";
			String describe = mTypeLayout.getCurrentDesc();
			if (describe == null || "".equals(describe)) {
				describe = "#极路客精彩视频#";
			}
			GolukDebugUtils.e("", "视频上传返回id--VideoShareActivity-videoUploadCallBack---调用第三方分享---: " + shortUrl);
			this.mShareDealTool.toShare(shortUrl, coverUrl, describe, title);

			// // 设置分享内容
			// // sharePlatform.setShareContent(shortUrl, coverUrl,
			// // mDesEdit.getText().toString());
			// CustomShareBoard shareBoard = new CustomShareBoard(this,
			// sharePlatform, shortUrl, coverUrl, describe, title);
			// shareBoard.showAtLocation(this.getWindow().getDecorView(),
			// Gravity.BOTTOM, 0, 0);

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void showRequestShareDialog() {
		dimissRequestShareDialog();
		mPdsave = ProgressDialog.show(this, "", "请求分享链接...");
		mPdsave.setCancelable(true);
		mPdsave.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				if (null != mPdsave) {
					mPdsave.dismiss();
					mPdsave = null;
				}
				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Share,
						JsonUtil.getCancelJson());
			}
		});

	}

	private void dimissRequestShareDialog() {
		if (null != mPdsave) {
			mPdsave.dismiss();
			mPdsave = null;
		}
	}

}
