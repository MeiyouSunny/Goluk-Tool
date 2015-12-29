package cn.com.mobnote.golukmobile.adas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventAdasConfigStatus;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukUtils;
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
		mApp.setContext(this, TAG);
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
		case R.id.button_next:
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			Intent intent = new Intent(this, AdasSeletedVehicleTypeActivity.class);
			intent.putExtra(AdasSeletedVehicleTypeActivity.FROM, 0);
			intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
			startActivity(intent);
			break;
		default:
			Log.e(TAG, "id = " + id);
			break;
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
