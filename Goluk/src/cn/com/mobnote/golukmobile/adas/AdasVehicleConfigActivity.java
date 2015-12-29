package cn.com.mobnote.golukmobile.adas;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukUtils;

public class AdasVehicleConfigActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "AdasVehicleConfigActivity";

	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;

	private EditText mCarName = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasvehicleconfig);
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, TAG);
		initView();
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
		mCarName = (EditText) findViewById(R.id.edittext_car_name);
		findViewById(R.id.layout_car_name).setOnClickListener(this);
		
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
		case R.id.layout_car_name:
			mCarName.requestFocus();
			break;
		case R.id.layout_front_wheelbase:
			break;
		case R.id.layout_height_distance:
			break;
		case R.id.layout_headway_distance:
			break;
		case R.id.layout_left_wheelbase:
			break;
		case R.id.layout_right_wheelbase:
			break;
		default:
			Log.e(TAG, "id = " + id);
			break;
		}
	}
}
