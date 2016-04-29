package com.mobnote.golukmain.live;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.livevideo.AbstractLiveActivity;
import com.mobnote.golukmain.livevideo.BaidumapLiveActivity;
import com.mobnote.golukmain.livevideo.GooglemapLiveActivity;
import com.mobnote.util.GolukUtils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.tiros.debug.GolukDebugUtils;

public class LiveSettingPopWindow implements OnClickListener, OnSeekBarChangeListener {

	/** 进入直播间 */
	public static final int EVENT_ENTER = 1;
	/** 默认直播时长 */
	private final int DEFAULT_SECOND = 30 * 60;
	private final int MAX_SECOND = 30 * 60;

	private Context mContext = null;
	private PopupWindow mPopWindow = null;
	private LayoutInflater mLayoutFlater = null;

	private RelativeLayout mRootLayout = null;
	private ViewGroup mParentLayout = null;
	/** 时长设置滚动 */
	private SeekBar mSeekBar = null;
	/** 视频直播设置时长 */
	private TextView mTimeTv = null;
	/** 预计本次流量 */
	private TextView mFlowTv = null;

	/** 回调对象 */
	private IPopwindowFn mListener = null;
	/** 是否可以支持声音按钮 */
	private Button mSoundBtn = null;
	/** 是否可以对讲按钮 */
	private Button mCanTalkBtn = null;

	private ImageView back = null;

	private boolean mIsCanTalk = false;
	/** 直播时长 */
	private int mCurrentLiveSecond = DEFAULT_SECOND;
	private boolean mIsCanSound = false;
	/** 当前选择视频类型 */
	private int mVideoType = 0;

	private boolean isShow = false;
	/** 是否用户主动点击取消 */
	private boolean isUserDimiss = false;

	/** 音视频配置信息 */
	private VideoConfigState mVideoConfigState = null;

	public void setCallBackNotify(IPopwindowFn fn) {
		this.mListener = fn;
	}

	public interface IPopwindowFn {
		public void callBackPopWindow(int event, Object data);
	}

	public LiveSettingPopWindow(Context context, ViewGroup parentLayout) {
		mContext = context;

		mLayoutFlater = LayoutInflater.from(mContext);
		mParentLayout = parentLayout;
		initLayout();
	}

	private void initLayout() {
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.carrecorder_live_share_setting, null);
		mPopWindow = new PopupWindow(mRootLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		// mEnter = (RelativeLayout) mRootLayout.findViewById(R.id.sysz_line);
		mCanTalkBtn = (Button) mRootLayout.findViewById(R.id.live_talk_btn);
		mSoundBtn = (Button) mRootLayout.findViewById(R.id.voice_switch_btn);
		mTimeTv = (TextView) mRootLayout.findViewById(R.id.live_time);
		// mDescEdit = (EditText) mRootLayout.findViewById(R.id.description);
		mSeekBar = (SeekBar) mRootLayout.findViewById(R.id.progress);
		mFlowTv = (TextView) mRootLayout.findViewById(R.id.live_flowl_txt);

		back = (ImageView) mRootLayout.findViewById(R.id.back_btn);
		mSeekBar.setMax(MAX_SECOND);
		mSeekBar.setProgress(DEFAULT_SECOND);
		mTimeTv.setText(GolukUtils.secondToString(DEFAULT_SECOND));
		if (mIsCanTalk) {
			mCanTalkBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mCanTalkBtn.setBackgroundResource(R.drawable.set_close_btn);
		}

		if (mIsCanSound) {
			mSoundBtn.setBackgroundResource(R.drawable.set_open_btn);
		} else {
			mSoundBtn.setBackgroundResource(R.drawable.set_close_btn);
		}

		mFlowTv.setText(getCurrentFlow(mCurrentLiveSecond));

		// 设置监听
		// mEnter.setOnClickListener(this);
		mSoundBtn.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(this);
		mCanTalkBtn.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	public void show() {
		if (null != mPopWindow) {
			mPopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			mPopWindow.setFocusable(true);
			mPopWindow.setOutsideTouchable(true);
			mPopWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopWindow.showAtLocation(mParentLayout, Gravity.RIGHT | Gravity.BOTTOM, 0, 0);
			mPopWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					GolukDebugUtils.e("", "jyf----20150406----LiveActivity----mPopWindow-----dimiss:");
					mListener.callBackPopWindow(EVENT_ENTER, getCurrentSetting());
					isShow = false;
					if (!isUserDimiss) {
						if (null != mContext && mContext instanceof AbstractLiveActivity) {

							if (GolukApplication.getInstance().isInteral()) {
								((BaidumapLiveActivity) mContext).exit();
							} else {
								((GooglemapLiveActivity) mContext).exit();
							}
						}
					}

				}
			});
			isShow = true;
		}
	}

	public boolean isShowing() {
		return this.isShow;
	}

	public void close() {
		isUserDimiss = true;
		if (null != mPopWindow) {
			isShow = false;
			mPopWindow.dismiss();
			// mPopWindow = null;
		}
	}

	public LiveSettingBean getCurrentSetting() {
		LiveSettingBean bean = new LiveSettingBean();
		bean.vtype = mVideoType;
		// 时长
		bean.duration = mCurrentLiveSecond;
		/*
		 * // 描述 if (null != mDescEdit) { bean.desc =
		 * mDescEdit.getText().toString(); }
		 */
		bean.isCanTalk = false;
		bean.isCanVoice = mIsCanSound;
		bean.netCountStr = getCurrentFlow(mCurrentLiveSecond);
		return bean;
	}

	private void switchTalkState() {
		if (mIsCanTalk) {
			mIsCanTalk = false;
			mCanTalkBtn.setBackgroundResource(R.drawable.set_close_btn);
		} else {
			mIsCanTalk = true;
			mCanTalkBtn.setBackgroundResource(R.drawable.set_open_btn);
		}
	}

	private void switchVoiceState() {
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
		if (null != mVideoConfigState) {
			if (1 == mVideoConfigState.AudioEnabled) {
				if (mIsCanSound) {
					mIsCanSound = false;
					mSoundBtn.setBackgroundResource(R.drawable.set_close_btn);
				} else {
					mIsCanSound = true;
					mSoundBtn.setBackgroundResource(R.drawable.set_open_btn);
				}
			} else {
				mIsCanSound = false;
				mSoundBtn.setBackgroundResource(R.drawable.set_close_btn);
			}

		} else {
			mIsCanSound = false;
			mSoundBtn.setBackgroundResource(R.drawable.set_close_btn);
		}

	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.back_btn) {
			mListener.callBackPopWindow(EVENT_ENTER, getCurrentSetting());
		} else if (id == R.id.live_talk_btn) {
		} else if (id == R.id.voice_switch_btn) {
			switchVoiceState();
		}

	}

	// 计算本次直播所需要的流量
	private String getCurrentFlow(int progress) {
		int size = (int) (mCurrentLiveSecond * 0.1);
		return "" + size + "MB";
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		GolukDebugUtils.e("", "LiveSetting-------onProgressChanged : " + progress + "	fromUser:" + fromUser);
		if (progress < 180) {
			progress = 180;
		}
//		if (progress < (MAX_SECOND / 60)) {
//			progress = MAX_SECOND / 60;
//		}
		mCurrentLiveSecond = progress;
		mTimeTv.setText(GolukUtils.secondToString(progress));
		mFlowTv.setText(getCurrentFlow(progress));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

}
