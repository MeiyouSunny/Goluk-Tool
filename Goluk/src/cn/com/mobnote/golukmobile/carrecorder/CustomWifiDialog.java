package cn.com.mobnote.golukmobile.carrecorder;


import cn.com.mobnote.application.GolukApplication;
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
		
		setTitle("修改wifi热点信息");
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);
		TextView wifiname = new TextView(context);
		wifiname.setText("设置wifi名称：");
		editname = new EditText(context);
		wifiname.setTextSize(20);
		editname.setTextSize(20);
		
		
		TextView wifipsw = new TextView(context);
		wifipsw.setText("设置wifi密码：");
		psw = new EditText(context);
		wifipsw.setTextSize(20);
		psw.setTextSize(20);
		
		
		
		layout.addView(wifiname);
		layout.addView(editname);
		
		layout.addView(wifipsw);
		layout.addView(psw);
		
		Button ok = new Button(context);
		ok.setTextSize(20);
		ok.setText("设置");
		
		Button cancle = new Button(context);
		cancle.setTextSize(20);
		cancle.setText("取消");
		
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
