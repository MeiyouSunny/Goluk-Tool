package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;

public class VersionActivity extends BaseActivity{
	private TextView mDeviceId=null;
	private TextView mVersion=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_version, null)); 
		setTitle("版本信息");
		
		mDeviceId = (TextView)findViewById(R.id.mDeviceId);
		mVersion = (TextView)findViewById(R.id.mVersion);
		
		mDeviceId.setText("123.234.234");
		mVersion.setText("345.968.23");
	}

}
