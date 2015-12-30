package cn.com.mobnote.golukmobile.carrecorder.settings;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;

public class AdasSensibilityActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "AdasSensibilityActivity";

	private GolukApplication mApp = null;
	public static final String FROM_TYPE = "from";
	public static final String SENSIBILITY_DATA = "sensibility_data";

	private TextView mTitle = null;
	private TextView mCloseText = null;
	private TextView mLowText = null;
	private TextView mMiddleText = null;
	private TextView mHighText = null;
	/** 视频类型选中高亮 */
	private ImageButton mCloseIcon = null;
	private ImageButton mLowIcon = null;
	private ImageButton mMiddleIcon = null;
	private ImageButton mHighIcon = null;
	
	private TextView mSelectedTextView;
	private ImageButton mSelectedIcon;
	private int mType = 0; /**0:向前距离  1:跑偏**/
	private int mSensibilityData = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			mType = getIntent().getIntExtra(FROM_TYPE, 0);
			mSensibilityData = getIntent().getIntExtra(SENSIBILITY_DATA, 0);
		} else {
			mType = savedInstanceState.getInt(FROM_TYPE);
			mSensibilityData = savedInstanceState.getInt(SENSIBILITY_DATA);
		}
		setContentView(R.layout.activity_adassensibility);
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, TAG);
		initView();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt(FROM_TYPE, mType);
		outState.putInt(SENSIBILITY_DATA, mSensibilityData);
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		mTitle = (TextView) findViewById(R.id.title);
		mCloseText = (TextView) findViewById(R.id.textview_close);
		mLowText = (TextView) findViewById(R.id.textview_low);
		mMiddleText = (TextView) findViewById(R.id.textview_middle);
		mHighText = (TextView) findViewById(R.id.textview_high);
		mCloseIcon = (ImageButton) findViewById(R.id.btn_close_right);
		mLowIcon = (ImageButton) findViewById(R.id.btn_low_right);
		mMiddleIcon = (ImageButton) findViewById(R.id.btn_middle_right);
		mHighIcon = (ImageButton) findViewById(R.id.btn_high_right);
		
		if (mType == 0) {
			mTitle.setText(R.string.str_adas_forward_warning_sensibility);
		} else {
			mTitle.setText(R.string.str_adas_road_offset_sensibility);
		}
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.layout_close).setOnClickListener(this);
		findViewById(R.id.layout_low).setOnClickListener(this);
		findViewById(R.id.layout_middle).setOnClickListener(this);
		findViewById(R.id.layout_high).setOnClickListener(this);
		/**初始化当前状态**/
		switch (mSensibilityData) {
		case 0:
			mSelectedTextView = mLowText;
			mSelectedIcon = mLowIcon;
			break;
		case 1:
			mSelectedTextView = mMiddleText;
			mSelectedIcon = mMiddleIcon;
			break;
		case 2:
			mSelectedTextView = mHighText;
			mSelectedIcon = mHighIcon;
			break;
		case 3:
			mSelectedTextView = mCloseText;
			mSelectedIcon = mCloseIcon;
			break;
		default:
			Log.e(TAG, "mSensibilityData = " + mSensibilityData);
			break;
		}
		if (mSelectedTextView != null) {
			mSelectedTextView.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		}
		
		if (mSelectedIcon != null) {
			mSelectedIcon.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				Intent intent = new Intent();
				intent.putExtra(SENSIBILITY_DATA, mSensibilityData);
				setResult(Activity.RESULT_OK, intent);
			}
			finish();
			break;
		case R.id.layout_close:
			mSensibilityData = 3;
			switchSelected(mCloseText, mCloseIcon);
			break;
		case R.id.layout_low:
			mSensibilityData = 0;
			switchSelected(mLowText, mLowIcon);
			break;
		case R.id.layout_middle:
			mSensibilityData = 1;
			switchSelected(mMiddleText, mMiddleIcon);
			break;
		case R.id.layout_high:
			mSensibilityData = 2;
			switchSelected(mHighText, mHighIcon);
			break;
		default:
			Log.e(TAG, "id = " + id);
			break;
		}
	}

	private void switchSelected(TextView selectedTx, ImageButton selectedBtn) {
		if (mSelectedTextView != null) {
			mSelectedTextView.setTextColor(getResources().getColor(R.color.setting_text_color_nor));
		}
		
		if (mSelectedIcon != null) {
			mSelectedIcon.setVisibility(View.GONE);
		}
		mSelectedTextView = selectedTx;
		mSelectedIcon = selectedBtn;
		mSelectedTextView.setTextColor(getResources().getColor(R.color.setting_text_color_sel));
		mSelectedIcon.setVisibility(View.VISIBLE);
	}

}
