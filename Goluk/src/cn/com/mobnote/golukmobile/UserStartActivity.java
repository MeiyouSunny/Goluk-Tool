package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.SysApplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * 
 *个人中心启动模块
 *
 *1、我有Goluk——跳转到登陆界面
 *2、随便看看——跳转到app主页
 *
 * @author mobnote
 *
 */
public class UserStartActivity extends Activity implements OnClickListener {

	ImageView mImageViewHave, mImageViewLook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_start);

		SysApplication.getInstance().addActivity(this);
		initView();
	}

	public void initView() {
		mImageViewHave = (ImageView) findViewById(R.id.user_start_have);
		mImageViewLook = (ImageView) findViewById(R.id.user_start_look);
		mImageViewHave.setOnClickListener(this);
		mImageViewLook.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.user_start_have:
			//我有Goluk
			Intent it = new Intent(UserStartActivity.this,UserLoginActivity.class);
			startActivity(it);
			break;

		case R.id.user_start_look:
			//随便看看
			SysApplication.getInstance().exit();//跳转之前杀死前边的所有Activiy。从而实现一键退出
			Intent it2 = new Intent(UserStartActivity.this,MainActivity.class);
			startActivity(it2);
			break;
		}
	}
}
