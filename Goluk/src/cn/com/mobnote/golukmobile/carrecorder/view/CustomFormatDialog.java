package cn.com.mobnote.golukmobile.carrecorder.view;

import cn.com.mobnote.golukmobile.R;
import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

public class CustomFormatDialog extends Dialog{
	private TextView mMessage=null;

	public CustomFormatDialog(Context context) {
		super(context, R.style.CustomDialog);
		
		setContentView(R.layout.format_dialog);
		mMessage = (TextView)findViewById(R.id.message);
	}
	
	public void setMessage(String message){
		mMessage.setText(message);
	}

}
