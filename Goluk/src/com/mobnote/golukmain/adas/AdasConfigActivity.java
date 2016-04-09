package com.mobnote.golukmain.adas;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventAdasConfigStatus;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.settings.SettingsActivity;
import com.mobnote.util.GolukUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

public class AdasConfigActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "AdasConfigActivity";

	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;
	private AdasConfigParamterBean mAdasConfigParamter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasconfig);
		EventBus.getDefault().register(this);
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			mAdasConfigParamter = (AdasConfigParamterBean) intent.getSerializableExtra(AdasVerificationActivity.ADASCONFIGDATA);
		} else {
			mAdasConfigParamter = (AdasConfigParamterBean) savedInstanceState.getSerializable(AdasVerificationActivity.ADASCONFIGDATA);
		}
		mApp = (GolukApplication) getApplication();
		initView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		if (mAdasConfigParamter != null) {
			outState.putSerializable(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
		}
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
		findViewById(R.id.layout_selected_car).setOnClickListener(this);
		findViewById(R.id.layout_adjust_camera_angle).setOnClickListener(this);
		findViewById(R.id.btn_next).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mApp.setContext(this, TAG);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (GolukUtils.isFastDoubleClick()) {
			return;
		}
		if (id == R.id.imagebutton_back) {
			// 返回
			finish();
		} else if (id == R.id.layout_selected_car) {
			Intent intent = new Intent(this, AdasSeletedVehicleTypeActivity.class);
			intent.putExtra(AdasSeletedVehicleTypeActivity.FROM, 1);
			intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
			startActivity(intent);
		} else if (id == R.id.layout_adjust_camera_angle) {
			Intent i = new Intent(AdasConfigActivity.this, AdasVerificationActivity.class);
			i.putExtra(AdasVerificationActivity.FROM, 1);
			i.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
			startActivity(i);
		} else if (id == R.id.btn_next) {
			Intent intentGuide = new Intent(this, AdasGuideActivity.class);
			intentGuide.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
			startActivity(intentGuide);
		} else {
			Log.e(TAG, "id = " + id);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(EventAdasConfigStatus event) {
		if (event.getOpCode() == EventConfig.IPC_ADAS_CONFIG_FROM_MODIFY) {
			mAdasConfigParamter = event.getData();
			return;
		}
		finish();
	}
}
