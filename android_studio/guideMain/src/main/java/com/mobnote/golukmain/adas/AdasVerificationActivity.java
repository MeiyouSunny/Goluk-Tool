package com.mobnote.golukmain.adas;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventAdasConfigStatus;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.PlayUrlManager;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.rd.car.player.RtspPlayerView;
import com.rd.car.player.RtspPlayerView.RtspPlayerLisener;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class AdasVerificationActivity extends BaseActivity implements OnClickListener, RtspPlayerLisener, IPCManagerFn,
		ForbidBack, OnLeftClickListener {

	private static final String TAG = "AdasVerificationActivity";
	public static final String FROM = "from";
	public static final String ADASCONFIGDATA = "adas_config_data";
	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;
	/** rtsp视频播放器 */
	private RtspPlayerView mRtspPlayerView = null;
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	/** 加载中布局 */
	private LinearLayout mLoadingLayout = null;
	/** 加载中动画显示控件 */
	private ImageView mLoading = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;

	private AdasVerificationFrameLayout mFrameLayoutOverlay;

	private ImageView mLeftImageView;
	private ImageView mRightImageView;
	private ImageView mUpImageView;
	private ImageView mDownImageView;
	private Button mCompleteButton;

	private int mFromType = 0;
	/** 车辆选择跳转：0， 配置页跳转：1 **/
	private AdasConfigParamterBean mAdasConfigParamter = null;
	private CustomLoadingDialog mCustomLoadingDialog = null;
	private CustomDialog mCustomDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasverification);
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			mFromType = intent.getIntExtra(FROM, 0);
			mAdasConfigParamter = (AdasConfigParamterBean) intent.getSerializableExtra(ADASCONFIGDATA);
		} else {
			mFromType = savedInstanceState.getInt(FROM);
			mAdasConfigParamter = (AdasConfigParamterBean) savedInstanceState.getSerializable(ADASCONFIGDATA);
		}
		mApp = (GolukApplication) getApplication();
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener(TAG, this);
		}
		initView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt(FROM, mFromType);
		if (mAdasConfigParamter != null) {
			outState.putSerializable(ADASCONFIGDATA, mAdasConfigParamter);
		}
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
		mCompleteButton = (Button) findViewById(R.id.button_verify_complete);
		mCompleteButton.setOnClickListener(this);
		mFrameLayoutOverlay = (AdasVerificationFrameLayout) findViewById(R.id.framelayout_Overlay);
		if (mAdasConfigParamter.point_x != 0) {
			mFrameLayoutOverlay.setLocation(mAdasConfigParamter.point_x, mAdasConfigParamter.point_y);
		}
		RelativeLayout playerLayout = (RelativeLayout) findViewById(R.id.relativelayout_playerview);
		ViewGroup.LayoutParams lp = playerLayout.getLayoutParams();
		lp.width = screenWidth;
		lp.height = (int) (screenWidth / 1.7833);
		playerLayout.setLayoutParams(lp);
		mLoadingLayout = (LinearLayout) findViewById(R.id.linearlayout_loading);
		mLoading = (ImageView) findViewById(R.id.imageview_loading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		mLeftImageView = (ImageView) findViewById(R.id.imageview_leftmove);
		mLeftImageView.setOnClickListener(this);
		mRightImageView = (ImageView) findViewById(R.id.imageview_rightmove);
		mRightImageView.setOnClickListener(this);
		mUpImageView = (ImageView) findViewById(R.id.imageview_upmove);
		mUpImageView.setOnClickListener(this);
		mDownImageView = (ImageView) findViewById(R.id.imageview_downmove);
		mDownImageView.setOnClickListener(this);
		mRtspPlayerView = (RtspPlayerView) findViewById(R.id.rtspplayerview);
		mRtspPlayerView.setPlayerListener(this);
		mRtspPlayerView.setAudioMute(true);
		mRtspPlayerView.setZOrderMediaOverlay(true);
		mRtspPlayerView.setBufferTime(1000);
		mRtspPlayerView.setConnectionTimeout(30000);
		mRtspPlayerView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.imagebutton_back) {
			// 返回
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			finish();
		} else if (id == R.id.button_verify_complete) {
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			if (!GolukApplication.getInstance().getIpcIsLogin()) {
				if (mCustomDialog == null) {
					mCustomDialog = new CustomDialog(this);
				}

				mCustomDialog.setCancelable(false);
				mCustomDialog.setMessage(this.getResources().getString(R.string.str_ipc_dialog_normal));
				mCustomDialog.setLeftButton(this.getResources().getString(R.string.str_button_ok), new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						//IPC页面访问统计
						ZhugeUtils.eventIpc(AdasVerificationActivity.this);

						Intent it = new Intent(AdasVerificationActivity.this, CarRecorderActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(it);
					}
				});
				mCustomDialog.show();
				return;
			}
			Point point = mFrameLayoutOverlay.getLocation();
			mAdasConfigParamter.point_x = point.x;
			mAdasConfigParamter.point_y = point.y;
			if (mCustomDialog == null) {
				mCustomDialog = new CustomDialog(this);
				mCustomDialog.setMessage(getString(R.string.str_adas_verify_confirm));
				mCustomDialog.setLeftButton(getString(R.string.str_adas_verify_ok), this);
				mCustomDialog.setRightButton(getString(R.string.str_adas_verify_not), null);
			}
			mCustomDialog.show();
		} else if (id == R.id.imageview_leftmove) {
			mFrameLayoutOverlay.setMoving(AdasVerificationFrameLayout.LEFT);
		} else if (id == R.id.imageview_rightmove) {
			mFrameLayoutOverlay.setMoving(AdasVerificationFrameLayout.RIGHT);
		} else if (id == R.id.imageview_upmove) {
			mFrameLayoutOverlay.setMoving(AdasVerificationFrameLayout.UP);
		} else if (id == R.id.imageview_downmove) {
			mFrameLayoutOverlay.setMoving(AdasVerificationFrameLayout.DOWN);
		} else {
			Log.e(TAG, "id = " + id);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mApp.setContext(this, TAG);
		start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		stop();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener(TAG);
		}
		if (null != mRtspPlayerView) {
			mRtspPlayerView.removeCallbacks(retryRunnable);
			mRtspPlayerView.cleanUp();
		}
		closeCustomLoading();
		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
		mCustomDialog = null;
	}

	@Override
	public void onGetCurrentPosition(RtspPlayerView rpv, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlayBuffering(RtspPlayerView rpv, boolean start) {
		// TODO Auto-generated method stub
		if (start) {
			// 缓冲开始
			showLoading();
		} else {
			// 缓冲结束
			hideLoading();
		}
	}

	@Override
	public void onPlayerBegin(RtspPlayerView rpv) {
		// TODO Auto-generated method stub
		hideLoading();
	}

	@Override
	public void onPlayerCompletion(RtspPlayerView rpv) {
		// TODO Auto-generated method stub
		rpv.removeCallbacks(retryRunnable);
		rpv.postDelayed(retryRunnable, 5000);
	}

	@Override
	public boolean onPlayerError(RtspPlayerView rpv, int arg1, int arg2, String strErrorInfo) {
		// TODO Auto-generated method stub
		if (!TextUtils.isEmpty(strErrorInfo)) {
			Toast.makeText(this, this.getResources().getString(R.string.str_video_error) + strErrorInfo,
					Toast.LENGTH_SHORT).show();
		}
		rpv.removeCallbacks(retryRunnable);
		rpv.postDelayed(retryRunnable, 5000); // FIXME:5秒后重连
		return false;
	}

	@Override
	public void onPlayerPrepared(RtspPlayerView arg0) {
		// TODO Auto-generated method stub
		mRtspPlayerView.setHideSurfaceWhilePlaying(true);
	}

	/**
	 * 重连runnable
	 */
	private Runnable retryRunnable = new Runnable() {

		@Override
		public void run() {
			// FIXME:重连
			start();
		}
	};

	/**
	 * 开始加载并播放
	 */
	public void start() {
		if (mRtspPlayerView != null) {
			String url = PlayUrlManager.getRtspUrl();
			mRtspPlayerView.setDataSource(url);
			mRtspPlayerView.start();
		}
		showLoading();
	}

	/**
	 * 停止播放
	 */
	public void stop() {
		if (mRtspPlayerView != null) {
			mRtspPlayerView.stopPlayback();
		}
	}

	private void showMoveController() {
		mLeftImageView.setVisibility(View.VISIBLE);
		mRightImageView.setVisibility(View.VISIBLE);
		mUpImageView.setVisibility(View.VISIBLE);
		mDownImageView.setVisibility(View.VISIBLE);
	}

	private void hideMoveControl() {
		mLeftImageView.setVisibility(View.GONE);
		mRightImageView.setVisibility(View.GONE);
		mUpImageView.setVisibility(View.GONE);
		mDownImageView.setVisibility(View.GONE);
	}

	/**
	 * 显示加载中布局
	 * 
	 */
	private void showLoading() {
		mLoadingLayout.setVisibility(View.VISIBLE);
		mLoading.setVisibility(View.VISIBLE);
		mLoading.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mAnimationDrawable != null) {
					if (!mAnimationDrawable.isRunning()) {
						mAnimationDrawable.start();
					}
				}
			}
		}, 100);
		mFrameLayoutOverlay.setTouchMode(false);
		mFrameLayoutOverlay.setBackgroundColor(Color.BLACK);
		hideMoveControl();
	}

	private void showCustomLoading() {
		if (mCustomLoadingDialog == null) {
			mCustomLoadingDialog = new CustomLoadingDialog(this, getString(R.string.str_adas_loding));
			mCustomLoadingDialog.setCancel(false);
			mCustomLoadingDialog.setListener(this);
		}
		if (!mCustomLoadingDialog.isShowing()) {
			mCustomLoadingDialog.show();
		}
	}

	private void closeCustomLoading() {
		if (mCustomLoadingDialog != null) {
			mCustomLoadingDialog.close();
			mCustomLoadingDialog = null;
		}
	}

	/**
	 * 隐藏加载中显示画面
	 * 
	 */
	private void hideLoading() {
		if (mAnimationDrawable != null) {
			if (mAnimationDrawable.isRunning()) {
				mAnimationDrawable.stop();
			}
		}
		mLoadingLayout.setVisibility(View.GONE);
		mFrameLayoutOverlay.setTouchMode(true);
		mFrameLayoutOverlay.setBackgroundResource(R.drawable.adas_verification_mask);
		showMoveController();
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		// TODO Auto-generated method stub
		if (event == ENetTransEvent_IPC_VDCP_CommandResp && msg == IPC_VDCP_Msg_SetADASConfig) {
			closeCustomLoading();
			if (param1 == RESULE_SUCESS) {
				EventAdasConfigStatus eventAdasConfigStatus = null;
				if (mFromType == 0) {
					eventAdasConfigStatus = new EventAdasConfigStatus(EventConfig.IPC_ADAS_CONFIG_FROM_GUIDE);
					GolukFileUtils.saveInt(GolukFileUtils.ADAS_FLAG, mAdasConfigParamter.enable);
				} else {
					eventAdasConfigStatus = new EventAdasConfigStatus(EventConfig.IPC_ADAS_CONFIG_FROM_MODIFY);
				}
				eventAdasConfigStatus.setData(mAdasConfigParamter);
				EventBus.getDefault().post(eventAdasConfigStatus);
				finish();
			}
		}
	}

	@Override
	public void forbidBackKey(int backKey) {
		// TODO Auto-generated method stub
		if (1 == backKey) {
			finish();
		}
	}

	@Override
	public void onClickListener() {
		// TODO Auto-generated method stub
		showCustomLoading();
		mAdasConfigParamter.enable = 1;
		GolukApplication.getInstance().getIPCControlManager().setT1AdasConfigAll(mAdasConfigParamter);
	}
}
