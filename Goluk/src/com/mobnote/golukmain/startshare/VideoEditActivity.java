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
import cn.com.mobnote.eventbus.EventLocationFinish;
import cn.com.mobnote.eventbus.EventShortLocationFinish;
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

import de.greenrobot.event.EventBus;

@SuppressLint("HandlerLeak")
public class VideoEditActivity extends BaseActivity implements OnClickListener, ICreateNewVideoFn, IUploadVideoFn,
		IRequestResultListener {
	public static final int EVENT_COMM_EXIT = 0;
	/** ?????????????????????????????? */
	public FilterPlaybackView mVVPlayVideo = null;
	/** application */
	private GolukApplication mApp = null;
	/** ???????????? */
	private ImageButton mBackBtn = null;
	/** ???????????? */
	private String mFilePath = "";
	/** ???????????? */
	private RelativeLayout mPlayLayout = null;
	/** ?????????????????? */
	private ImageView mPlayStatusImage = null;
	/** ????????? */
	private ProgressBar mVideoProgressBar = null;
	/** ??????????????? */
	private Thread mProgressThread = null;
	/** ??????????????????????????? 3 ?????? 2 ?????? */
	private int mCurrentVideoType = 0;
	/** ????????????????????? */
	private String videoName = "";

	private String videoFrom = "";
	/** ??????????????????????????? **/
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

	/** ???????????????????????? */
	private boolean isBack = false;
	private RelativeLayout mPlayImgLayout = null;

	/** ?????? */
	private PromotionSelectItem mPromotionSelectItem;

	public static final int PROMOTION_ACTIVITY_BACK = 110;
	private boolean mIsResume = false;
	private boolean mIsFirstLoad = true;
	private boolean mIsT1Video = false;
	/** ?????????????????? */
	private boolean isSharing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
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

		// ??????GolukApplication??????
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "VideoEdit");

		mShareDealTool = new ShareDeal(this, mYouMengLayout);

		mFilterLayout = new ShareFilterLayout(this);
		mTypeLayout = new ShareTypeLayout(this, mPromotionSelectItem);
		mInputLayout = new InputLayout(this, mRootLayout);
		interceptVideoName(mFilePath);// ??????????????????
		loadRes();
		// ???????????????
		initView();
		// ???????????????
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

	public void onEventMainThread(EventShortLocationFinish event) {
		if (null == event) {
			return;
		}

		if(mTypeLayout != null && event.getShortAddress() != null){
			mTypeLayout.setLocationAddress(event.getShortAddress());
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		EventBus.getDefault().unregister(this);
		super.onDestroy();
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
	 * ????????????
	 * 
	 * @author jyf
	 * @date 2015???8???13???
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
	 * @author ??????
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
			// ????????????
			GolukDebugUtils.e("", "----------------------------VideoEditActivity-----videoName???" + videoName);
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
	 * ???????????????
	 */
	private void initView() {
		// ??????????????????
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mPlayLayout = (RelativeLayout) findViewById(R.id.play_layout);
		mPlayStatusImage = (ImageView) findViewById(R.id.play_image);
		mVideoProgressBar = (ProgressBar) findViewById(R.id.video_progress_bar);
		// ????????????
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
	 * ?????????????????????
	 */
	private void videoInit() {
		mVVPlayVideo = (FilterPlaybackView) this.findViewById(R.id.vvPlayVideo);
		// ??????????????????id???Constants.FILTER_ID_WARM
		int nFilterId = Constants.FILTER_ID_WARM + 1;
		// ??????????????????????????????Constants.*????????????
		// ?????????????????????
		mVVPlayVideo.addFilter(nFilterId++, R.raw.gutongse);
		mVVPlayVideo.addFilter(nFilterId++, R.raw.lanseshike);
		mVVPlayVideo.addFilter(nFilterId++, R.raw.youge);

		// ????????????????????????????????????
		mVVPlayVideo.setPlaybackListener(new FilterPlaybackView.FilterPlaybackViewListener() {
			@Override
			public void onPrepared(MediaPlayerControl mpc) {
				// ?????????????????????
				GolukDebugUtils.e("", "VideoEditActivity----onPrepared---video---????????????");
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
				// ??????????????????
				GolukUtils.showToast(VideoEditActivity.this, getString(R.string.str_video_play_error) + nErrorNo
						+ getString(R.string.str_video_error_info) + strErrInfo);
				return false;
			}

			@Override
			public void onCompletion(MediaPlayerControl mpc) {
				// ??????????????????
				mVideoProgressBar.setProgress(mVVPlayVideo.getDuration());

				GolukDebugUtils.e("", "VideoEditActivity----onCompletion---");
			}
		});

		try {
			// ???????????????
			mVVPlayVideo.setVideoPath(mFilePath);
			mVVPlayVideo.switchFilterId(0);
		} catch (FilterVideoEditorException e) {
			e.printStackTrace();
			GolukUtils.showToast(this, e.getMessage());
		}
	}

	/**
	 * ????????????????????????
	 */
	private void changeVideoPlayState() {
		if (mVVPlayVideo.isPlaying()) {
			// ??????????????????
			mPlayStatusImage.setVisibility(View.VISIBLE);
			// ??????????????????????????????????????????
			mVVPlayVideo.pause();
		}
	}

	private void showLoadingView() {
		mShareLoading.showLoadingLayout();
		mShareLoading.switchState(ShareLoading.STATE_CREATE_VIDEO);
	}

	/**
	 * ?????????????????????
	 */
	private void stopProgressThread() {
		Thread tmpThread = mProgressThread;
		mProgressThread = null;
		if (tmpThread != null) {
			tmpThread.interrupt();
		}
	}

	/**
	 * ??????????????????
	 * 
	 */
	private void updateVideoProgress() {
		// ??????????????????????????????????????????
		mProgressThread = new Thread() {
			public void run() {
				// ??????????????????
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
	 * ?????????????????????
	 * 
	 * @author jyf
	 */
	private void updatePlayerProgress() {
		int maxDuration = mVVPlayVideo.getDuration();
		// ?????????????????????????????????????????????
		mVideoProgressBar.setMax(maxDuration);
		while (null != mProgressThread && null != mVVPlayVideo) {
			if (isExit) {
				break;
			}
			// ??????????????????????????????????????????????????????
			if (mVVPlayVideo.isPlaying()) {
				// ????????????????????????????????????????????????????????????
				int position = mVVPlayVideo.getCurrentPosition();
				mVideoProgressBar.setProgress(position);
				GolukDebugUtils.e("jyf", "VideoEditActivity----thread----getCurrent----position:" + position);
			}
			GolukDebugUtils.e("jyf", "VideoEditActivity----thread----getCurrent----max:" + maxDuration);
			try {
				// ??????500??????
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
				// ????????????????????????
				// ????????????????????????????????????????????????????????????.
				// ??????????????????????????????,chenxy 5.11
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
		// // ??????????????????
		// if (mShareLoading.getCurrentState() == ShareLoading.STATE_GET_SHARE)
		// {
		// // ???????????????????????????????????????????????????
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
				// ????????????
				mPlayStatusImage.setVisibility(View.VISIBLE);
			}
			mVVPlayVideo.onPause();
		}
		// ?????????????????????
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
	 * ?????????????????????
	 * 
	 * @author jyf
	 * @date 2015???6???10???
	 */
	private void click_next() {
		// ??????????????????
		// ?????????????????????
		stopProgressThread();
		// ???????????????
		changeVideoPlayState();

		// ???????????????????????????????????????????????????????????????
		if (2 == mCurrentVideoType && 0 == mFilterLayout.mMVListAdapter.getCurrentResIndex()) {
			// ?????????????????????????????????
			mShareLoading.showLoadingLayout();
			this.createNewFileSucess(mFilePath);
			return;
		}

		if (mFilterLayout.mMVListAdapter.getCurrentResIndex() == mFilterLayout.mMVListAdapter.getSucessIndex()) {
			// ????????????????????????????????????
			mShareLoading.showLoadingLayout();
			this.createNewFileSucess(this.mCreateNewVideo.getNewFilePath());
			return;
		}
		// ????????????????????????
		mCreateNewVideo.onSaveVideo();
	}

	/**
	 * ?????????????????? ??? ????????????
	 * 
	 * @author jyf
	 * @date 2015???6???10???
	 */
	private void click_play() {
		// ??????/??????
		if (mVVPlayVideo.isPlaying()) {
			// ?????????????????????
			stopProgressThread();
			mVVPlayVideo.pause();
			// ????????????
			mPlayStatusImage.setVisibility(View.VISIBLE);
		} else {
			// ?????????????????????
			updateVideoProgress();
			mVVPlayVideo.start();
			// ????????????
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
			// ??????
			if (isBack) {
				return;
			}
			isBack = true;
			if (ShareLoading.STATE_CREATE_VIDEO == mShareLoading.getCurrentState()) {
				// ????????????????????????
				// ???????????????????????????????????????ANR
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
		// ????????????????????????
		mFilterLayout.mMVListAdapter.setSucessIndex(mFilterLayout.mMVListAdapter.getCurrentResIndex());
		// ??????????????????,????????????
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
				// ?????????????????????
				mShareLoading.switchState(ShareLoading.STATE_NONE);
				exit();
			} else if (bSuccess) {
				// ??????????????????,?????????????????????
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
			// ??????????????????????????????????????????
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

	// ??????????????????
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
	 * ????????????????????????
	 * 
	 * @param json
	 *            ,????????????
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
		bean.mShareType = "1";
		bean.filePath = mFilePath;
		mShareDealTool.toShare(bean);
	}

	/**
	 * ????????????????????????
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

	// ???????????????????????????????????????????????????????????????????????????????????????????????????
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
