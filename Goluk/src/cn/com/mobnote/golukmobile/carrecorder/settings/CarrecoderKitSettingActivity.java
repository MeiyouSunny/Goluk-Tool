package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;

/**
 * 
 * 遙控器页面
 *
 * 2015年12月14日
 *
 * @author jiayf
 */
@SuppressLint("InflateParams")
public class CarrecoderKitSettingActivity extends CarRecordBaseActivity implements OnClickListener {

	public static final String TAG = "CarrecoderKitSettingActivity";

	/** 视频类型文字显示 */
	private TextView mCloseText = null;
	private TextView mLowText = null;
	/** 视频类型选中高亮 */
	private ImageButton mCloseIcon = null;
	private ImageButton mLowIcon = null;

	/** UI显示 **/
	private String[] mArrayText = null;
	/** 文字显示 **/
	private TextView[] mTextArray = null;

	private int mRecord;
	private int mSnapShot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_kit_setting_quality, null));
		setTitle(this.getResources().getString(R.string.str_carrecoder_setting_kit_title));
		mRecord = getIntent().getIntExtra("record", 0);
		mSnapShot = getIntent().getIntExtra("snapshot", 0);
		initView();
		setListener();
		setData2UI();
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView() {
		mCloseText = (TextView) findViewById(R.id.carrecoder_kti_closeText);
		mCloseIcon = (ImageButton) findViewById(R.id.carrecoder_kti_cRight);

		mLowText = (TextView) findViewById(R.id.carrecoder_kti_lowText);
		mLowIcon = (ImageButton) findViewById(R.id.carrecoder_kti_dRight);

		mArrayText = getResources().getStringArray(R.array.kit_setting_ui);

		mTextArray = new TextView[] { mCloseText, mLowText };

		if (null != mArrayText) {
			int length = mArrayText.length;
			for (int i = 0; i < length; i++) {
				for (int j = i; j < mTextArray.length; j++) {
					mTextArray[j].setText(mArrayText[i]);
				}
			}
		}
	}

	/**
	 * 设置控件监听事件
	 * 
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	private void setListener() {
		findViewById(R.id.carrecoder_kti_close).setOnClickListener(this);
		findViewById(R.id.carrecoder_kti_low).setOnClickListener(this);
	}

	@Override
	protected void subExit() {
		exit();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.carrecoder_kti_close:
			mSnapShot = 1;
			setData2UI();
			break;
		case R.id.carrecoder_kti_low:
			mSnapShot = 0;
			setData2UI();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, TAG);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void exit() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			Intent intent = new Intent();
			intent.putExtra("record", mRecord);
			intent.putExtra("snapshot", mSnapShot);
			setResult(Activity.RESULT_OK, intent);
		}
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	// 遍历分辨率，区分码率，改变UI
	private void setData2UI() {
		if (mRecord == 1 && 1 == mSnapShot) {
			mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
			mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
			mCloseIcon.setVisibility(View.VISIBLE);
			mLowIcon.setVisibility(View.GONE);
		} else {
			mCloseText.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
			mLowText.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
			mCloseIcon.setVisibility(View.GONE);
			mLowIcon.setVisibility(View.VISIBLE);
		}
	}

}
