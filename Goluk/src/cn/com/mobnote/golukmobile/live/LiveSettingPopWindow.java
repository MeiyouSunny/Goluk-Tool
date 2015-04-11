package cn.com.mobnote.golukmobile.live;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import cn.com.mobnote.golukmobile.R;

public class LiveSettingPopWindow implements OnClickListener {

	/** 进入直播间 */
	public static final int EVENT_ENTER = 1;

	private Context mContext = null;
	private PopupWindow mPopWindow = null;
	LayoutInflater mLayoutFlater = null;

	private RelativeLayout mRootLayout = null;
	private ViewGroup mParentLayout = null;

	private RelativeLayout mEnter = null;
	/** 回调对象 */
	private IPopwindowFn mListener = null;

	private Button mCanTalkBtn = null;

	private boolean mIsCanTalk = true;

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

		mEnter = (RelativeLayout) mRootLayout.findViewById(R.id.sysz_line);
		mEnter.setOnClickListener(this);

		mCanTalkBtn = (Button) mRootLayout.findViewById(R.id.car_talk);
		mCanTalkBtn.setOnClickListener(this);
		if (mIsCanTalk) {
			mCanTalkBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
		} else {
			mCanTalkBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
		}
	}

	public void show() {
		if (null != mPopWindow) {
			mPopWindow.showAtLocation(mParentLayout, Gravity.RIGHT | Gravity.BOTTOM, 0, 0);
		}
	}

	public void close() {
		if (null != mPopWindow) {
			mPopWindow.dismiss();
			mPopWindow = null;
		}
	}

	private LiveSettingBean getCurrentSetting() {
		LiveSettingBean bean = new LiveSettingBean();
		bean.duration = 600;
		bean.isCanTalk = mIsCanTalk;
		return bean;
	}

	private void switchTalkState() {
		if (mIsCanTalk) {
			mIsCanTalk = false;
			mCanTalkBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_off);
		} else {
			mIsCanTalk = true;
			mCanTalkBtn.setBackgroundResource(R.drawable.carrecorder_setup_option_on);
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.sysz_line:
			mListener.callBackPopWindow(EVENT_ENTER, getCurrentSetting());
			break;
		case R.id.car_talk:
			switchTalkState();
			break;
		}

	}
}
