package com.mobnote.golukmain.adas;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventAdasConfigStatus;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.util.GolukUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

public class AdasGuideActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "AdasGuideActivity";

	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;
	
	private TextView mNextTextview;
	private AdasConfigParamterBean mAdasConfigParamter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasguide);
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			mAdasConfigParamter = (AdasConfigParamterBean) intent.getSerializableExtra(AdasVerificationActivity.ADASCONFIGDATA);
		} else {
			mAdasConfigParamter = (AdasConfigParamterBean) savedInstanceState.getSerializable(AdasVerificationActivity.ADASCONFIGDATA);
		}
		EventBus.getDefault().register(this);
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
		mNextTextview = (TextView) findViewById(R.id.button_next);
		mNextTextview.setOnClickListener(this);
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
		if (id == R.id.imagebutton_back) {
			// 返回
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			finish();
		} else if (id == R.id.button_next) {
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			Intent intent = new Intent(this, AdasSeletedVehicleTypeActivity.class);
			intent.putExtra(AdasSeletedVehicleTypeActivity.FROM, 0);
			intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
			startActivity(intent);
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
		finish();
	}
}
