package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;

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

	private ImageView mImageViewHave, mImageViewLook;
	//
	private Context mContext = null;
	private GolukApplication mApp = null;
	//如果是注销进来的，需要将手机号填进去
	private SharedPreferences mPreferences = null;
	private String phone = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_start);

		mContext = this;
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, "UserStart");
		
		SysApplication.getInstance().addActivity(this);
		initView();
	}

	public void initView() {
		mImageViewHave = (ImageView) findViewById(R.id.user_start_have);
		mImageViewLook = (ImageView) findViewById(R.id.user_start_look);
		//获取注销成功后传来的信息
		mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		phone = mPreferences.getString("setupPhone", "");//最后一个参数为默认值
		
		mImageViewHave.setOnClickListener(this);
		mImageViewLook.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.user_start_have:
			//我有Goluk
			Intent it = new Intent(UserStartActivity.this,UserLoginActivity.class);
			//登录页回调判断
			it.putExtra("isInfo", "main");
			//在黑页面判断是注销进来的还是首次登录进来的
			if(!mApp.loginoutStatus){//注销
				it.putExtra("startActivity", phone);
				startActivity(it);
			}else{
				startActivity(it);
			}
//			this.finish();
			break;

		case R.id.user_start_look:
			//随便看看
			Intent it2 = new Intent(UserStartActivity.this,MainActivity.class);
			startActivity(it2);
//			this.finish();
			break;
		}
	}
}
