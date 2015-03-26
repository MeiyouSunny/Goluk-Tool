package cn.com.mobnote.golukmobile.carrecorder;

import cn.com.mobnote.golukmobile.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingsActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_settings);
		initView();
		setListener();
		
	}
	
	private void initView(){
			
	}
	
	private void setListener(){
		findViewById(R.id.back_btn).setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
				finish();
				break;

		default:
			break;
		}
	}

}
