package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import com.airtalkee.sdk.util.Log;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.console;
import cn.com.mobonote.golukmobile.comm.GolukMobile;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 注册 
 * 
 * 1、注册手机号密码 
 * 2、获取验证码
 *  3、登陆
 * 
 * @author mobnote
 *
 */
public class UserRegistActivity extends Activity implements OnClickListener {

	// 注册title
	private Button mBackButton;
	private TextView mTextViewTitle;
	// 手机号、密码、注册按钮
	private EditText mEditTextPhone, mEditTextPwd;
	private Button mBtnRegist;
	// 验证码
	private Button mBtnIdentify;
	private EditText mEditTextIdentify;
	// 登陆
	private TextView mTextViewLogin;
	//手机号、密码、验证码文本框中的值
	private String number;
	private String pwd;
	private String identify;
	
	private Context mContext = null;
	private GolukApplication mApplication = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_regist);

		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserRegist");
		
		initView();
		// title
		mTextViewTitle.setText("注册");

	}

	public void initView() {
		// title
		mBackButton = (Button) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号、密码、注册按钮
		mEditTextPhone = (EditText) findViewById(R.id.user_regist_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_regist_pwd);
		mBtnRegist = (Button) findViewById(R.id.user_regist_btn);
		// 验证码
		mBtnIdentify = (Button) findViewById(R.id.user_regist_identify_btn);
		mEditTextIdentify = (EditText) findViewById(R.id.user_regist_identify);
		// 登陆
		mTextViewLogin = (TextView) findViewById(R.id.user_regist_login);
		
		number = mEditTextPhone.getText().toString();
		pwd = mEditTextPwd.getText().toString();
		identify = mEditTextIdentify.getText().toString();

		/**
		 * 监听绑定
		 */
		// 返回
		mBackButton.setOnClickListener(this);
		// 手机号、密码、注册按钮
		mBtnRegist.setOnClickListener(this);
		
		// 验证码不可点击状态:手机号为空或者不为 11 位时
		if (number.isEmpty() || number.length() != 11) {
			mBtnIdentify.setEnabled(false);
		} else {
			mBtnIdentify.setEnabled(true);
			mBtnIdentify.setOnClickListener(this);
		}
		mEditTextIdentify.setOnClickListener(this);
		// 登陆
		mTextViewLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			finish();
			break;
		// 注册按钮
		case R.id.user_regist_btn:
			//点按钮后,弹出登录中的提示,样式使用系统 loading 样式,文字描述:注册中
			//注册成功:弹出系统短提示:注册成功,以登录状态进入 Goluk 首页
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
			break;
		// 获取验证码按钮
		case R.id.user_regist_identify_btn:
			
			break;
		// 验证码EditText
		case R.id.user_regist_identify:

			break;
		// 登陆
		case R.id.user_regist_login:
			Intent itLogin = new Intent(UserRegistActivity.this,UserLoginActivity.class);
			startActivity(itLogin);
			break;
		}
	}
	
	/**
	 * 注册
	 */
	public void regist(){
		if(!"".equals(number) ){
			if(number.startsWith("1") && number.length() == 11){
				if(!"".equals(pwd)){
					if(pwd.length()>=6 && pwd.length()<=16){
						/**
						 * 验证码判断{PNumber：“13054875692”，type：“1”}    1代表注册
						 */
						String isIdentify = "{\"PNumber\":\"" + number  + "\",\"type\":\"1\"}";
						boolean boo = mApplication.mGoluk.GoLuk_CommonGetPage(GolukMobile.PageType_GetVCode, isIdentify);
						
						//{PNumber：“13054875692”，Password：“XXX”，VCode：“1234”，tag：“XXX”}
//						String condi = "{\"PNumber\":\"" + number + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
						String condi    = "{\"PNumber\":\"" + number + "\",\"Password\":\""+pwd+"\",\"VCode\":\""+identify+"\",\"tag\":\"android\"}";
						boolean b = mApplication.mGoluk.GoLuk_CommonGetPage(GolukMobile.PageType_Register,condi);
						android.util.Log.i("aaa", b+"");
						if(b){
							console.log("回调成功");
						}
					}else{
						new AlertDialog.Builder(this)
						.setTitle("错误信息提示")
						.setMessage("密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小写!")
						.setPositiveButton("确定", null)
						.create().show();
					}
				}else{
//					mEditTextPwd.setError("密码不能为空");
				}
			}else{
				new AlertDialog.Builder(this)
				.setTitle("错误信息提示")
				.setMessage("手机号格式错误,请重新输入！")
				.setPositiveButton("确定", null)
				.create().show();
			}
		}else{
//			mEditTextPhoneNumber.setError("手机号不能为空");
		}
	}
	
	/**
	 * 验证码回调
	 */
	public void identifyCallback(int success,Object obj){
		console.log("验证码获取回调---identifyCallBack---" + success + "---" + obj);
		if(1 == success){
			try{
				String data = (String)obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				if(code == 200){
					//验证码获取成功
				}else{
					new AlertDialog.Builder(this)
					.setTitle("错误信息提示")
					.setMessage("请输入正确的验证码！")
					.setPositiveButton("确定", null)
					.create().show();
				}
			}
			catch(Exception ex){}
		}
		else{
			console.toast("验证码获取失败", mContext);
		}
	}
	
	/**
	 * 注册回调
	 */
	public void registCallback(int success,Object obj){
		console.log("注册回调---registCallback---"+success+"---"+obj);
		if(1 == success){
			try{
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				if(code == 200){
					//注册成功
				}else{
					new AlertDialog.Builder(this)
					.setTitle("错误信息提示")
					.setMessage("注册失败")
					.create().show();
				}
			}catch(Exception e){}
		}else{
			console.log("注册失败");
		}
	}
	
}
