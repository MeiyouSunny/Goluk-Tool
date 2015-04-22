package cn.com.mobnote.golukmobile.carrecorder.view;

import cn.com.mobnote.golukmobile.R;
import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;

public class CustomProgressDialog extends Dialog{
	
	public CustomProgressDialog(Context context) {
		super(context); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_progress_dialog);
		
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){ 
//        	dismiss();
        	return false; 
        }
        return super.onKeyDown(keyCode, event);
    }

}
