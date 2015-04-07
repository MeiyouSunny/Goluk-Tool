package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;

public class RestoreFactorySettingsActivity extends BaseActivity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_restore_factory_settings, null)); 
		setTitle("恢复出厂设置");
		
		findViewById(R.id.mFormat).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.mFormat:
					CustomDialog mCustomDialog = new CustomDialog(this);
					mCustomDialog.setMessage("是否确认恢复Goluk出厂设置", Gravity.CENTER);
					mCustomDialog.setLeftButton("确认", new OnLeftClickListener() {
						@Override
						public void onClickListener() {
							
						}
					});
					mCustomDialog.setRightButton("取消", null);
					mCustomDialog.show();
				break;
	
			default:
				break;
		}
	}

}
