package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomFormatDialog;

public class FormatSDCardActivity extends BaseActivity implements OnClickListener{
	private CustomFormatDialog mCustomFormatDialog=null;
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_format_sd_card, null)); 
		setTitle("格式化SD卡");
		
		findViewById(R.id.mFormat).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.mFormat:
				if(null == mCustomFormatDialog){
					mCustomFormatDialog = new CustomFormatDialog(this);
//					mCustomFormatDialog.cancel();
					mCustomFormatDialog.setMessage("正在格式化SD卡，请稍候...");
					mCustomFormatDialog.show();
				}
				break;
	
			default:
				break;
		}
	}

}
