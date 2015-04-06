package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;

public class WatermarkSettingActivity extends BaseActivity implements OnClickListener{
	private Button mLogoBtn=null;
	private Button mTimeBtn=null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_watermark_setting, null)); 
		setTitle("水印设置");
		
		initView();
	}
	
	private void initView(){
		mLogoBtn = (Button)findViewById(R.id.mLogoBtn);
		mTimeBtn = (Button)findViewById(R.id.mTimeBtn);
		mLogoBtn.setOnClickListener(this);
		mTimeBtn.setOnClickListener(this);
		
		
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.mLogoBtn:
				
				break;
			case R.id.mTimeBtn:
				
				break;
	
			default:
				break;
		}
	}

}
