package com.mobnote.golukmain.startshare;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
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
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.golukmain.promotion.PromotionItem;
import com.mobnote.golukmain.promotion.PromotionListRequest;
import com.mobnote.golukmain.promotion.PromotionModel;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.startshare.bean.ShareDataBean;
import com.mobnote.golukmain.startshare.bean.ShareDataFullBean;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.rd.car.editor.Constants;
import com.rd.car.editor.FilterPlaybackView;
import com.rd.car.editor.FilterVideoEditorException;

@SuppressLint("HandlerLeak")
public class VideoEditActivity extends BaseActivity implements OnClickListener, ICreateNewVideoFn, IUploadVideoFn,
		IRequestResultListener {
	public static final int EVENT_COMM_EXIT = 0;
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
	/** 进度条 */
	private ProgressBar mVideoProgressBar = null;
	/** 进度条线程 */
	private Thread mProgressThread = null;
	/** 当前编辑的视频类型 3 紧急 2 精彩 */
	private int mCurrentVideoType = 0;
	/** 分享的视频名称 */
	private String videoName = "";

	private String videoFrom = "";
	/** 分享的视频创建时间 **/
	private String videoCreateTime = "";
	private boolean isExit = false;

	private FrameLayout mMiddleLayout = null;
	private ShareFilterLayout mFilterLayout = null;
	public ShareTypeLayout mTypeLayout = null;
	public InputLayout mInputLayout = null;
	private CreateNewVideo mCreateNewVideo = null;
	private UploadVideo mUploadVideo = null;
	private ShareDeal mShareDealTool = null;
	private ShareLoading mShareLoading = null;

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

	private int resTypeSelectColor = 0;
	private int resTypeUnSelectColor = 0;

	/** 防止重复点击退出 */
	private boolean isBack = false;
	private RelativeLayout mPlayImgLayout = null;

	/** 活动 */
	private PromotionSelectItem mPromotionSelectItem;

	public static final int PROMOTION_ACTIVITY_BACK = 110;
	private boolean mIsResume = false;
	private boolean mIsFirstLoad = true;
	private boolean mIsT1Video = false;
	/** 防止重复点击 */
	private boolean isSharing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mLayoutFlater = LayoutInflater.from(this);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.video_edit, null);
		mYouMengLayout = (RelativeLayout) mRootLayout.findViewById(R.id.shortshare_youmeng_layout);
		setContentView(mRootLayout);

		if (savedInstanceState == null) {
			getIntentData();
		} else {
			mFilePath = savedInstanceState.getString("cn.com.mobnote.video.path");
			mCurrentVideoType = savedInstanceState.getInt("type", 2);
			mPromotionSelectItem = (PromotionSelectItem) savedInstanceState
					.getSerializable(FragmentAlbum.ACTIVITY_INFO);
		}

		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "VideoEdit");

		mShareDealTool = new ShareDeal(this, mYouMengLayout);

		mFilterLayout = new ShareFilterLayout(this);
		mTypeLayout = new ShareTypeLayout(this, mPromotionSelectItem);
		mInputLayout = new InputLayout(this, mRootLayout);
		interceptVideoName(mFilePath);// 拿到视频名称
		loadRes();
		// 页面初始化
		initView();
		// 视频初始化
		videoInit();

		mCreateNewVideo = new CreateNewVideo(this, mVVPlayVideo, this);
		mUploadVideo = new UploadVideo(this, mApp, videoName);
		mUploadVideo.setListener(this);
		mShareLoading = new ShareLoading(this, mRootLayout);
		mBaseHandler.sendEmptyMessageDelayed(100, 100);

		if (mUploadVideo.getThumbBitmap() != null) {
			mPlayImgLayout.setBackgroundDrawable(new BitmapDrawable(mUploadVideo.getThumbBitmap()));
			mPlayImgLayout.setVisibility(View.VISIBLE);
		} else {
			mPlayImgLayout.setVisibility(View.GONE);
		}
		loadData();
	}

	private void loadData() {
		PromotionListRequest request = new PromotionListRequest(IPageNotifyFn.PageType_GetPromotion, this);
		request.get();
	}

	private void getIntentData() {
		Intent intent = getIntent();
		mFilePath = intent.getStringExtra("cn.com.mobnote.video.path");
		mCurrentVideoType = intent.getIntExtra("type", 2);
		mPromotionSelectItem = (PromotionSelectItem) intent.getSerializableExtra(FragmentAlbum.ACTIVITY_INFO);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mPromotionSelectItem != null) {
			outState.putSerializable(FragmentAlbum.ACTIVITY_INFO, mPromotionSelectItem);
		}
		outState.putString("cn.com.mobnote.video.path", mFilePath);
		outState.putInt("type", mCurrentVideoType);
		super.onSaveInstanceState(outState);
	}

	/**
	 * 加载资源
	 * 
	 * @author jyf
	 * @date 2015年8月13日
	 */
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
		case 101:
			if (mTypeLayout != null) {
				mTypeLayout.showPopUp();
			}
			break;
		case 105:
			mPlayImgLayout.setVisibility(View.GONE);
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
			if (mTypeLayout.getPopupFlag()) {
				mBaseHandler.sendEmptyMessageDelayed(101, 100);
			}
		} else {
			mMiddleLayout.addView(mFilterLayout.getRootLayout(), lp);
		}
	}

	GolukVideoInfoDbManager mGolukVideoInfoDbManager = GolukVideoInfoDbManager.getInstance();

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
			if (mGolukVideoInfoDbManager != null) {
				VideoFileInfoBean videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(videoName);
				if (videoFileInfoBean != null) {
					videoCreateTime = videoFileInfoBean.timestamp + "000";
					videoFrom = videoFileInfoBean.devicename;
					if (IPCControlManager.T1_SIGN.equalsIgnoreCase(videoFrom)) {
						mIsT1Video = true;
					}
				}
			}
			videoName = videoName.replace("mp4", "jpg");
			// 分享时间
			GolukDebugUtils.e("", "----------------------------VideoEditActivity-----videoName：" + videoName);
			if (TextUtils.isEmpty(videoCreateTime)) {
				if (videoName.contains("_")) {
					String[] videoTimeArray = videoName.split("_");
					if (videoTimeArray.length == 3) {
						videoCreateTime = "20" + videoTimeArray[1] + "000";
					} else if (videoTimeArray.length == 7) {
						videoCreateTime = videoTimeArray[2] + "000";
						mIsT1Video = true;
					} else if (videoTimeArray.length == 8) {
						videoCreateTime = videoTimeArray[1] + "000";
						mIsT1Video = true;
					}
				} else {
					videoCreateTime = "";
				}
			}
		}
	}

	/**
	 * 页面初始化
	 */
	private void initView() {
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mPlayLayout = (RelativeLayout) findViewById(R.id.play_layout);
		mPlayStatusImage = (ImageView) findViewById(R.id.play_image);
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
		mPlayImgLayout = (RelativeLayout) findViewById(R.id.edit_play_img);
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
				GolukDebugUtils.e("", "VideoEditActivity----onPrepared---video---加载完成");
				if (mIsResume && mIsFirstLoad) {
					mVVPlayVideo.start();
					mIsFirstLoad = false;
				} else {
					mPlayStatusImage.setVisibility(View.VISIBLE);
				}
				updateVideoProgress();
				if (mPlayImgLayout.getVisibility() == View.VISIBLE) {
					mBaseHandler.sendEmptyMessageDelayed(105, 800);
				}
			}

			@Override
			public boolean onError(MediaPlayerControl mpc, int nErrorNo, String strErrInfo) {
				// 视频播放出错
				GolukUtils.showToast(VideoEditActivity.this, getString(R.string.str_video_play_error) + nErrorNo
						+ getString(R.string.str_video_error_info) + strErrInfo);
				return false;
			}

			@Override
			public void onCompletion(MediaPlayerControl mpc) {
				// 视频播放完成
				mVideoProgressBar.setProgress(mVVPlayVideo.getDuration());

				GolukDebugUtils.e("", "VideoEditActivity----onCompletion---");
			}
		});

		try {
			// 设置视频源
			mVVPlayVideo.setVideoPath(mFilePath);
			mVVPlayVideo.switchFilterId(0);
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
		mShareLoading.showLoadingLayout();
		mShareLoading.switchState(ShareLoading.STATE_CREATE_VIDEO);
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
				try {
					updatePlayerProgress();
				} catch (Exception e) {
					GolukDebugUtils.e("jyf", "jyf------ViewEditActivity-----updateVideoProgress-----Error!");
					e.printStackTrace();
				}
			};
		};
		mProgressThread.start();
	}

	/**
	 * 更新播放器进度
	 * 
	 * @author jyf
	 */
	private void updatePlayerProgress() {
		int maxDuration = mVVPlayVideo.getDuration();
		// 设置进度条的长度为视频的总长度
		mVideoProgressBar.setMax(maxDuration);
		while (null != mProgressThread && null != mVVPlayVideo) {
			if (isExit) {
				break;
			}
			// 如果视频正在播放而且进度条没有被拖动
			if (mVVPlayVideo.isPlaying()) {
				// 设置进度条的当前进度为视频已经播放的长度
				int position = mVVPlayVideo.getCurrentPosition();
				mVideoProgressBar.setProgress(position);
				GolukDebugUtils.e("jyf", "VideoEditActivity----thread----getCurrent----position:" + position);
			}
			GolukDebugUtils.e("jyf", "VideoEditActivity----thread----getCurrent----max:" + maxDuration);
			try {
				// 休眠500毫秒
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isBack) {
				return true;
			}
			isBack = true;
			if (ShareLoading.STATE_CREATE_VIDEO == mShareLoading.getCurrentState()) {
				// 判断是否正在上传
				// 为了修复上传的是时返回几率崩溃控制针问题.
				// 感觉可能崩溃到这里了,chenxy 5.11
				if (null != mVVPlayVideo) {
					mVVPlayVideo.cancelSave();
				}
			} else {
				exit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void exit() {
		if (isExit) {
			return;
		}
		isExit = true;
		isBack = true;
		mBaseHandler.removeCallbacksAndMessages(null);
		stopProgressThread();
		mTypeLayout.setExit();
		mTypeLayout = null;
		mInputLayout.setExit();
		mInputLayout = null;
		mCreateNewVideo.setExit();
		mCreateNewVideo = null;
		mUploadVideo.setExit();
		mUploadVideo = null;
		mShareDealTool.setExit();
		mShareDealTool = null;
		mFilterLayout.setExit();
		mFilterLayout = null;

		this.toInitState();
		if (null != mVVPlayVideo) {
			mVVPlayVideo.cleanUp();
			mVVPlayVideo = null;
		}
		// // 正在获取连接
		// if (mShareLoading.getCurrentState() == ShareLoading.STATE_GET_SHARE)
		// {
		// // 如果正在获取分享连接状态，則要取消
		// mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
		// IPageNotifyFn.PageType_Share,
		// JsonUtil.getCancelJson());
		// }
		mShareLoading = null;
		mYouMengLayout = null;
		finish();
	}

	@Override
	protected void onPause() {
		mIsResume = false;
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
		mIsResume = true;
		if (mVVPlayVideo != null) {
			mVVPlayVideo.onResume();
		}
		mApp.setContext(this, "VideoEdit");

		if (ShareLoading.STATE_SHAREING == mShareLoading.getCurrentState()) {
			toInitState();
		}

		super.onResume();
	}

	@Override
	protected void onStop() {
		if (mTypeLayout != null) {
			mTypeLayout.dismissPopup();
		}
		super.onStop();
	}

	/**
	 * 点击“下一步”
	 * 
	 * @author jyf
	 * @date 2015年6月10日
	 */
	private void click_next() {
		// 导出视频编码
		// 停止进度条线程
		stopProgressThread();
		// 暂停播放器
		changeVideoPlayState();

		// 如果是精彩视频，并且不添加滤镜，則直接跳转
		if (2 == mCurrentVideoType && 0 == mFilterLayout.mMVListAdapter.getCurrentResIndex()) {
			// 直接跳转，不需要加滤镜
			mShareLoading.showLoadingLayout();
			this.createNewFileSucess(mFilePath);
			return;
		}

		if (mFilterLayout.mMVListAdapter.getCurrentResIndex() == mFilterLayout.mMVListAdapter.getSucessIndex()) {
			// 上次添加过，不再重新添加
			mShareLoading.showLoadingLayout();
			this.createNewFileSucess(this.mCreateNewVideo.getNewFilePath());
			return;
		}
		// 重新产生新的视频
		mCreateNewVideo.onSaveVideo();
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
		if (isSharing) {
			return;
		}
		isSharing = true;
		click_next();
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.back_btn) {
			// 返回
			if (isBack) {
				return;
			}
			isBack = true;
			if (ShareLoading.STATE_CREATE_VIDEO == mShareLoading.getCurrentState()) {
				// 判断是否正在上传
				// 为了修复上传的是时返回几率ANR
				if (null != mVVPlayVideo) {
					mVVPlayVideo.cancelSave();
				}
			} else {
				exit();
			}
		} else if (id == R.id.play_layout) {
			if (!isExit) {
				click_play();
			}
		} else if (id == R.id.share_type_layout) {
			if (!isExit) {
				switchMiddleLayout(false, true);
			}
		} else if (id == R.id.share_filter_layout) {
			if (!isExit) {
				if (mIsT1Video) {
					GolukUtils.showToast(this, this.getString(R.string.str_video_filter_online_soon));
				} else {
					switchMiddleLayout(false, false);
				}
			}
		}
	}

	private void createNewFileSucess(String filePath) {
		// 把成功的保存起来
		mFilterLayout.mMVListAdapter.setSucessIndex(mFilterLayout.mMVListAdapter.getCurrentResIndex());
		// 添加滤镜成功,文件上传
		GolukDebugUtils.e("", "jyf-----shortshare---VideoEditActivity---------------createNewFileSucess--filePath-: "
				+ filePath);
		this.mUploadVideo.setUploadInfo(filePath, mCurrentVideoType, videoName);
		mShareLoading.switchState(ShareLoading.STATE_UPLOAD);
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
				mShareLoading.setProcess(progress);
			}
			break;
		case EVENT_END:
			boolean bSuccess = (Boolean) obj1;
			boolean bCancel = (Boolean) obj2;
			if (bCancel) {
				// 已取消视频保存
				mShareLoading.switchState(ShareLoading.STATE_NONE);
				exit();
			} else if (bSuccess) {
				// 视频保存成功,跳转到分享页面
				String newFilePath = (String) obj3;
				createNewFileSucess(newFilePath);
			}

			if (null != mVVPlayVideo && mVVPlayVideo.needReload()) {
				try {
					mVVPlayVideo.reload();
				} catch (FilterVideoEditorException e) {
					GolukUtils.showToast(VideoEditActivity.this,
							this.getString(R.string.str_reload_video_failure) + e.getMessage());
				}
			}
			break;
		case EVENT_ERROR:
			String errorInfo = "";
			if (null != obj3) {
				errorInfo = (String) obj3;
			}
			toInitState();
			GolukUtils.showToast(VideoEditActivity.this, this.getString(R.string.str_save_video_fail) + errorInfo);
			break;
		default:
			break;
		}

	}

	public void CallBack_Comm(int event, Object obj) {
		if (EVENT_COMM_EXIT == event) {
			this.exit();
		}
	}

	@Override
	public void CallBack_UploadVideo(int event, Object obj) {
		GolukDebugUtils.e("", "jyf-----shortshare---VideoEditActivity---------------CallBack_UploadVideo--event-: "
				+ event);
		if (isExit) {
			return;
		}
		switch (event) {
		case EVENT_EXIT:
			exit();
			break;
		case EVENT_UPLOAD_SUCESS:
			// 　文件上传成功，请求分享连接
			requestShareInfo();
			break;
		case EVENT_PROCESS:
			if (null != obj && null != mShareLoading) {
				final int process = (Integer) obj;
				mShareLoading.setProcess(process);
			}
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PROMOTION_ACTIVITY_BACK) {
			if (mTypeLayout != null) {
				mTypeLayout.onActivityResult(resultCode, data);
				mPromotionSelectItem = mTypeLayout.getPromotionSelectItem();
			}
			return;
		}
		if (this.mShareDealTool != null) {
			this.mShareDealTool.onActivityResult(requestCode, resultCode, data);
		}
	}

	// 请求分享信息
	private void requestShareInfo() {
		mShareLoading.switchState(ShareLoading.STATE_GET_SHARE);
		final String t_vid = this.mUploadVideo.getVideoId();
		final String t_signTime = this.mUploadVideo.getSignTime();
		final String t_type = "" + (mCurrentVideoType == 2 ? 2 : 1);
		final String selectTypeJson = JsonUtil.createShareType("" + mTypeLayout.getCurrentSelectType());
		final String desc = mTypeLayout.getCurrentDesc();
		final String isSeque = this.mTypeLayout.isOpenShare() ? "1" : "0";
		final String t_location = mTypeLayout.getCurrentLocation();
		PromotionSelectItem item = mTypeLayout.getPromotionSelectItem();
		String channelid = "";
		String activityid = "";
		String activityname = "";

		if (item != null) {
			channelid = item.channelid;
			activityid = item.activityid;
			activityname = item.activitytitle;
		}
		GetShareAddressRequest request = new GetShareAddressRequest(IPageNotifyFn.PageType_Share, this);
		request.get(t_vid, t_type, desc, selectTypeJson, isSeque, videoCreateTime, t_signTime, channelid, activityid,
				activityname, t_location, videoFrom);
	}

	private void getShareFailed() {
		GolukUtils.showToast(this, this.getString(R.string.str_get_share_address_fail));
		toInitState();
	}

	/**
	 * 本地视频分享回调
	 * 
	 * @param json
	 *            ,分享数据
	 */
	public void videoShareCallBack(ShareDataBean shareData) {
		if (mShareLoading == null || mUploadVideo == null || mShareDealTool == null) {
			return;
		}
		mShareLoading.switchState(ShareLoading.STATE_SHAREING);
		if (shareData == null) {
			getShareFailed();
			return;
		}

		final String title = this.getString(R.string.str_video_edit_share_title);
		final String describe = getShareDesc();
		final String sinaTxt = this.getString(R.string.str_share_board_real_desc);

		ThirdShareBean bean = new ThirdShareBean();
		bean.surl = shareData.shorturl;
		bean.curl = shareData.coverurl;
		bean.db = describe;
		bean.tl = title;
		bean.bitmap = mUploadVideo.getThumbBitmap();
		bean.realDesc = sinaTxt;
		bean.videoId = this.mUploadVideo.getVideoId();

		mShareDealTool.toShare(bean);
	}

	/**
	 * 获取视频分享描述
	 * 
	 * @return
	 * @author jyf
	 */
	private String getShareDesc() {
		String describe = mTypeLayout.getCurrentDesc();
		if (describe == null || "".equals(describe)) {
			describe = this.getString(R.string.str_share_describe);
		}

		UserInfo info = mApp.getMyInfo();
		if (null != info) {
			describe = info.nickname + this.getString(R.string.str_colon) + describe;
		}

		return describe;
	}

	public void shareCallBack(boolean isSucess) {
		toInitState();
	}

	// 当分享成功，失败　或某一环节出现失败后，还原到原始状态，再进行分享
	private void toInitState() {
		isSharing = false;
		if (isExit) {
			return;
		}
		if (null != mShareLoading) {
			mShareLoading.hide();
			mShareLoading.switchState(ShareLoading.STATE_NONE);
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		switch (requestType) {
		case IPageNotifyFn.PageType_GetPromotion:
			PromotionModel data = (PromotionModel) result;
			if (data != null && data.success) {
				if (mTypeLayout == null) {
					return;
				}
				ArrayList<PromotionSelectItem> list = new ArrayList<PromotionSelectItem>(2);
				if (data.data.priorityacts != null) {
					for (PromotionItem item : data.data.priorityacts) {
						PromotionSelectItem promotionSelectItem = new PromotionSelectItem();
						promotionSelectItem.activityid = item.id;
						promotionSelectItem.activitytitle = item.name;
						list.add(promotionSelectItem);
					}
				}
				mTypeLayout.setPromotionList(data.data.PromotionList, list);
			}
			break;
		case IPageNotifyFn.PageType_Share:
			ShareDataFullBean shareDataFull = (ShareDataFullBean) result;
			if (shareDataFull != null && shareDataFull.success) {
				videoShareCallBack(shareDataFull.data);
			} else {
				getShareFailed();
			}
			break;
		}
	}

}
