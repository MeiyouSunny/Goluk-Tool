package cn.com.mobnote.golukmobile.adas;

import java.util.ArrayList;

import com.alibaba.fastjson.JSON;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.util.GolukFileUtils;
import cn.com.mobnote.util.GolukUtils;

public class AdasVehicleConfigActivity extends BaseActivity implements OnClickListener, OnFocusChangeListener {

	private static final String TAG = "AdasVehicleConfigActivity";
	public static final String CUSTOMDATA = "custom_data";
	public static final String CUSTOMINDEX = "custom_index";
	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;

	private EditText mCarName = null;
	private EditText mHeightOffset = null;
	private EditText mWheelOffset = null;
	private EditText mHeadOffset = null;
	private EditText mLeftOffset = null;
	private EditText mRightOffset = null;
	private ArrayList<VehicleParamterBean> mCustomVehicleList = null;
	private int mIndex = 0;
	private VehicleParamterBean mParamter;
	private CustomDialog mCustomDialog;
	private InputMethodManager mImManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasvehicleconfig);
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			mCustomVehicleList = (ArrayList<VehicleParamterBean>) intent.getSerializableExtra(CUSTOMDATA);
			mIndex = intent.getIntExtra(CUSTOMINDEX, 0);
		} else {
			mCustomVehicleList = (ArrayList<VehicleParamterBean>) savedInstanceState.get(CUSTOMDATA);
			mIndex = savedInstanceState.getInt(CUSTOMINDEX);
		}
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, TAG);
		initView();
		loadData();
		refreshUI();

//		mImManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//		mImManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mApp.setContext(this, TAG);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putSerializable(CUSTOMDATA, mCustomVehicleList);
		outState.putInt(CUSTOMINDEX, mIndex);
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
		mCarName = (EditText) findViewById(R.id.edittext_car_name);
		mCarName.setOnFocusChangeListener(this);
		findViewById(R.id.layout_car_name).setOnClickListener(this);
		mWheelOffset = (EditText) findViewById(R.id.edittext_front_wheelbase);
		mWheelOffset.setOnFocusChangeListener(this);
		findViewById(R.id.layout_front_wheelbase).setOnClickListener(this);
		mHeadOffset = (EditText) findViewById(R.id.edittext_headway_distance);
		mHeadOffset.setOnFocusChangeListener(this);
		findViewById(R.id.layout_headway_distance).setOnClickListener(this);
		mHeightOffset = (EditText) findViewById(R.id.edittext_height_distance);
		mHeightOffset.setOnFocusChangeListener(this);
		findViewById(R.id.layout_height_distance).setOnClickListener(this);
		mLeftOffset = (EditText) findViewById(R.id.edittext_left_wheelbase);
		mLeftOffset.setOnFocusChangeListener(this);
		findViewById(R.id.layout_left_wheelbase).setOnClickListener(this);
		mRightOffset = (EditText) findViewById(R.id.edittext_right_wheelbase);
		mRightOffset.setOnFocusChangeListener(this);
		findViewById(R.id.layout_right_wheelbase).setOnClickListener(this);
		findViewById(R.id.textview_complete).setOnClickListener(this);
	}

	private void loadData() {
		mParamter = mCustomVehicleList.get(mIndex);
	}

	private void refreshUI() {
		if (mParamter == null) {
			return;
		}
		mCarName.setText(mParamter.name);
		if (mParamter.wheel_offset != 0) {
			mWheelOffset.setText("" + mParamter.wheel_offset);
			mHeadOffset.setText("" + mParamter.head_offset);
			mHeightOffset.setText("" + mParamter.height_offset);
			mLeftOffset.setText("" + mParamter.left_offset);
			mRightOffset.setText("" + mParamter.right_offset);
		}
	}

	private boolean verifyData() {
		String wheelOffsetStr = mWheelOffset.getText().toString();
		if (TextUtils.isEmpty(wheelOffsetStr) || !TextUtils.isDigitsOnly(wheelOffsetStr)) {
			return false;
		}
		int wheelOffset = Integer.valueOf(wheelOffsetStr);
		if (wheelOffset < 50 || wheelOffset > 350) {
			return false;
		}
		String headOffsetsetStr = mHeadOffset.getText().toString();
		if (TextUtils.isEmpty(headOffsetsetStr) || !TextUtils.isDigitsOnly(headOffsetsetStr)) {
			return false;
		}
		int headOffset = Integer.valueOf(headOffsetsetStr);
		if (headOffset < 50 || headOffset > 400) {
			return false;
		}
		String heightOffsetStr = mHeightOffset.getText().toString();
		if (TextUtils.isEmpty(heightOffsetStr) || !TextUtils.isDigitsOnly(heightOffsetStr)) {
			return false;
		}
		int heightOffset = Integer.valueOf(heightOffsetStr);
		if (heightOffset < 70 || heightOffset > 400) {
			return false;
		}
		String leftOffsetStr = mLeftOffset.getText().toString();
		if (TextUtils.isEmpty(leftOffsetStr) || !TextUtils.isDigitsOnly(leftOffsetStr)) {
			return false;
		}
		int leftOffset = Integer.valueOf(leftOffsetStr);
		if (leftOffset < 50 || leftOffset > 350) {
			return false;
		}
		String rightOffsetStr = mRightOffset.getText().toString();
		if (TextUtils.isEmpty(rightOffsetStr) || !TextUtils.isDigitsOnly(rightOffsetStr)) {
			return false;
		}
		int rightOffset = Integer.valueOf(rightOffsetStr);
		if (rightOffset < 50 || rightOffset > 350) {
			return false;
		}
		mParamter.head_offset = headOffset;
		mParamter.height_offset = heightOffset;
		mParamter.left_offset = leftOffset;
		mParamter.right_offset = rightOffset;
		mParamter.wheel_offset = wheelOffset;
		String name = mCarName.getText().toString().trim();
		if (!TextUtils.isEmpty(name)) {
			mParamter.name = name;
		}
		String jsonString = JSON.toJSONString(mCustomVehicleList);
		GolukFileUtils.saveString(GolukFileUtils.ADAS_CUSTOM_VEHICLE, jsonString);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
		mCustomDialog = null;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.imagebutton_back:
			// 返回
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			finish();
			break;
		case R.id.textview_complete:
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			boolean b = verifyData();
			if (!b) {
				if (mCustomDialog == null) {
					mCustomDialog = new CustomDialog(this);
				}
				mCustomDialog.setMessage(getString(R.string.str_adas_paramter_error), Gravity.CENTER);
				mCustomDialog.setLeftButton(getString(R.string.str_button_ok), null);
				mCustomDialog.show();
				return;
			}
			Intent intent = new Intent();
			intent.putExtra(CUSTOMDATA, mParamter);
			setResult(Activity.RESULT_OK, intent);
			finish();
			break;
		case R.id.layout_car_name:
			mCarName.requestFocus();
			break;
		case R.id.layout_front_wheelbase:
			mWheelOffset.requestFocus();
			break;
		case R.id.layout_height_distance:
			mHeightOffset.requestFocus();
			break;
		case R.id.layout_headway_distance:
			mHeadOffset.requestFocus();
			break;
		case R.id.layout_left_wheelbase:
			mLeftOffset.requestFocus();
			break;
		case R.id.layout_right_wheelbase:
			mRightOffset.requestFocus();
			break;
		default:
			Log.e(TAG, "id = " + id);
			break;
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.edittext_front_wheelbase:
		case R.id.edittext_headway_distance:
		case R.id.edittext_height_distance:
		case R.id.edittext_left_wheelbase:
		case R.id.edittext_right_wheelbase:
			EditText textView = (EditText) v;
			String hint;
			if (hasFocus) {
				hint = textView.getHint().toString();
				textView.setTag(hint);
				textView.setHint("");
			} else {
				hint = textView.getTag().toString();
				textView.setHint(hint);
			}
			break;
		}
	}
}
