package cn.com.mobnote.golukmobile.adas;

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
import cn.com.mobnote.util.GolukUtils;

public class AdasConfigActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "AdasConfigActivity";

	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasconfig);
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, TAG);
		initView();
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
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
		default:
			Log.e(TAG, "id = " + id);
			break;
		}
	}
}
