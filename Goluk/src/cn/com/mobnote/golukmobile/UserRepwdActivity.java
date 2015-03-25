package cn.com.mobnote.golukmobile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.mobnote.golukmobile.R.id;
import cn.com.mobnote.user.UserUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 重置密码
 * 
 * 1、输入手机号、密码
 * 2、验证码的获取和判断
 * 3、短信验证
 * 
 * @author mobnote
 *
 */
public class UserRepwdActivity extends Activity implements OnClickListener{

	//title
	private Button mBtnBack;
	private TextView mTextViewTitle;
	//手机号、密码、验证码
	private EditText mEditTextPhone,mEditTextPwd,mEditTextIdentify;
	private Button mBtnIdentity,mBtnOK;
	//手机号、新密码
	private String number,pwd,identify;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_repwd);
		
		initView();
		/*Intent intent = getIntent();
		String intentPhone = intent.getExtras().getString("user_login_forget");
		mEditTextPhone.setText(intentPhone);*/
		//title
		mTextViewTitle.setText("重设密码");
		
	}
	public void initView(){
		mBtnBack = (Button) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		mEditTextPhone = (EditText) findViewById(R.id.user_repwd_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_repwd_pwd);
		mEditTextIdentify = (EditText) findViewById(R.id.user_repwd_identify);
		mBtnIdentity = (Button) findViewById(R.id.user_repwd_identify_btn);
		mBtnOK = (Button) findViewById(R.id.user_repwd_ok_btn);
		
		//手机号、新密码
		number = mEditTextPhone.getText().toString();
		pwd = mEditTextPwd.getText().toString();
		identify = mEditTextIdentify.getText().toString();
		
		/**
		 * 绑定监听
		 */
		mBtnBack.setOnClickListener(this);
		mEditTextPhone.setOnClickListener(this);
		mEditTextPwd.setOnClickListener(this);
		mEditTextIdentify.setOnClickListener(this);
		//获取验证码按钮：不可点击状态:手机号为空或者不为 11 位时
		if(number.isEmpty() || number.length()!=11){
			mBtnIdentity.setEnabled(false);
		}else{
			mBtnIdentity.setEnabled(true);
			mBtnIdentity.setOnClickListener(this);
		}
		//OK按钮：当帐号或密码或验证码输入框没有内容时,为不可点击状态
		if(number.isEmpty() || pwd.isEmpty() || identify.isEmpty()){
			mBtnOK.setEnabled(false);
		}else{
			mBtnOK.setEnabled(true);
			mBtnOK.setOnClickListener(this);
		}
		
	}
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		//返回
		case R.id.back_btn:
			finish();
			break;
		//手机号
		case R.id.user_repwd_phonenumber:
			//手机号格式错误，弹出对话框
			UserUtils.isPhoneNumber(number, UserRepwdActivity.this);
			break;
			//密码
		case R.id.user_repwd_pwd:
			//密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小
			UserUtils.isPwd(number, UserRepwdActivity.this);
			break;
			//获取验证码按钮
		case R.id.user_repwd_identify_btn:
			//点击状态:点击后弹出系统短提示:发送中,请稍后;发送后弹出系统短提示:验证码已经发送,请查收短信。
			
			break;
			//验证码
		case R.id.user_repwd_identify:
			
			break;
			//重设按钮
		case R.id.user_repwd_ok_btn:
			//点按钮后,弹出重置密码中的提示,样式使用系统 loading 样式,文字描述:正在重置
			//重置密码成功,弹出系统短提示:重置密码成功。同时跳转至登录页面。
			Intent intent = new Intent(this,UserLoginActivity.class);
			startActivity(intent);
			break;
		}
	}
}
