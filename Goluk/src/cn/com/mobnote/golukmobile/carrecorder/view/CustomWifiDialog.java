package cn.com.mobnote.golukmobile.carrecorder.view;


import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomWifiDialog extends Dialog{
	EditText editname;
	EditText psw;

	public CustomWifiDialog(Context context) {
		super(context);
		
		setTitle(this.getContext().getResources().getString(R.string.str_change_wifi_information));
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);
		TextView wifiname = new TextView(context);
		wifiname.setText(this.getContext().getResources().getString(R.string.str_set_wifi_name));
		editname = new EditText(context);
		wifiname.setTextSize(20);
		editname.setTextSize(20);
		
		
		TextView wifipsw = new TextView(context);
		wifipsw.setText(this.getContext().getResources().getString(R.string.str_set_wifi_pwd));
		psw = new EditText(context);
		wifipsw.setTextSize(20);
		psw.setTextSize(20);
		
		
		
		layout.addView(wifiname);
		layout.addView(editname);
		
		layout.addView(wifipsw);
		layout.addView(psw);
		
		Button ok = new Button(context);
		ok.setTextSize(20);
		ok.setText(this.getContext().getResources().getString(R.string.setting_title_text));
		
		Button cancle = new Button(context);
		cancle.setTextSize(20);
		cancle.setText(this.getContext().getResources().getString(R.string.dialog_str_cancel));
		
		layout.addView(ok);
		layout.addView(cancle);
		
		
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();
				String name = editname.getText().toString();
				String password = psw.getText().toString();
				GolukApplication.getInstance().editWifi(name, password);
			}
		});
		cancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();
			}
		});
	}
	

}
