package cn.com.mobnote.user;

import android.annotation.SuppressLint;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 注册管理类
 * @author mobnote
 *
 */
public class UserRegistManage {

	private GolukApplication mApp = null;
	private UserRegistInterface mRegistInterface = null;
	private UserIdentifyInterface mIdentifyInterface = null;
	
	public UserRegistManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
//		mApp.initLogic();
	}
	
	public void setIdentifyInterface(UserIdentifyInterface identifyInterface){
		this.mIdentifyInterface = identifyInterface;
	}
	public void setRegistInterface(UserRegistInterface mInterface){
		this.mRegistInterface = mInterface;
	}
	//验证码状态的判断
	public void identifyStatusChange(int mIdentifyStatus){
		mApp.identifyStatus = mIdentifyStatus;
		if(mIdentifyInterface != null){
			mIdentifyInterface.identifyCallbackInterface();
		}
	}
	//注册状态的判断
	public void registStatusChange(int mStatus)
	{
		mApp.registStatus = mStatus;
		if(mRegistInterface != null)
		{
			mRegistInterface.registStatusChange();
		}
	}

	/**
	 * 获取验证码
	 */
	@SuppressLint("HandlerLeak")
	public boolean getIdentify(String phone,String password){
		//获取验证码
		boolean bIndentify = false;
		String isIdentify = "{\"PNumber\":\"" + phone + "\",\"type\":\"1\"}";
		GolukDebugUtils.e("",isIdentify);
		bIndentify = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_GetVCode, isIdentify);
		return bIndentify;
	}
	
	/**
	 * 验证码回调
	 */
	/*public void identifyCallback(int success,Object obj){
		GolukDebugUtils.e("","验证码获取回调---identifyCallBack---" + success + "---" + obj);
		//点击验证码按钮手机号、密码不可被修改
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		mIdentifyLoading.setVisibility(View.GONE);
		identifyStatusChange(0);
		if(1 == success){
			try{
				String data = (String)obj;
				GolukDebugUtils.e("",data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				switch (code) {
				case 200:
					console.toast("验证码已经发送，请查收短信", mApp.getContext());
					identifyStatusChange(1);
					//验证码获取成功
					*//**
					 * 点击获取验证码的时候进行倒计时
					 *//*
					mEditTextPhone.setEnabled(false);
					mCountDownhelper = new CountDownButtonHelper(mBtnIdentify, 60, 1);
					mCountDownhelper.setOnFinishListener(new OnFinishListener() {
						@Override
						public void finish() {
							mBtnIdentify.setText("再次发送");
							//倒计时结束后手机号、密码可以更改
							mEditTextPhone.setEnabled(true);
						}
					});
					mCountDownhelper.start();
					break;
				case 201:
					UserUtils.showDialog(mApp.getContext(), "该手机号1小时内下发5次以上验证码");
					identifyStatusChange(2);
					break;

				case 500:
					UserUtils.showDialog(mApp.getContext(), "服务端程序异常");
					identifyStatusChange(2);
					break;

				case 405:
					identifyStatusChange(3);
					new AlertDialog.Builder(mApp.getContext())
					.setMessage("此手机号已经被注册啦!")
					.setNegativeButton("取消", null)
					.setPositiveButton("立即登录", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Intent itRegist = new Intent(UserRegistActivity.this,UserLoginActivity.class);
							itRegist.putExtra("intentRegist", mEditTextPhone.getText().toString());
							startActivity(itRegist);
							finish();
						}
					}).create().show();
					break;

				case 440:
					UserUtils.showDialog(mApp.getContext(), "输入手机号异常");
					identifyStatusChange(2);
					break;
				case 480:
					UserUtils.showDialog(mApp.getContext(), "验证码获取失败");
					identifyStatusChange(2);
					break;
				default:
					break;
				}
				unregisterReceiver(smsReceiver);
				click = 2;
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}else{
			console.toast("验证码获取失败", mApp.getContext());
			identifyStatusChange(2);
			//网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时
			GolukDebugUtils.i("outtime", "-----网络链接超时超时超时-------xxxx---"+codeOut);
			switch (codeOut) {
			case 700:
				loginStatusChange(4);
				break;
			case 600:
				//网络未链接
				loginStatusChange(4);
			case 601:
				//http封装错误
				loginStatusChange(4);
				break;
			default:
				break;
			}
		}
	}
	
	*//**
	 * 注册
	 *//*
	public void regist(){
		String phone = mEditTextPhone.getText().toString();
		String password = mEditTextPwd.getText().toString();
		String identify = mEditTextIdentify.getText().toString();
		if(!"".equals(password) && !"".equals(identify)){
			mBtnRegist.setFocusable(true);
			if(password.length()>=6 && password.length()<=16){
				if(!UserUtils.isNetDeviceAvailable(this)){
					console.toast("当前网络状态不佳，请检查网络后重试", mContext);
				}else{
					//初始化定时器
				initTimer();
				handler1.postDelayed(runnable, 3000);//三秒执行一次runnable.
				//{PNumber：“13054875692”，Password：“xxx”，VCode：“1234”}
				String isRegist = "{\"PNumber\":\"" + phone + "\",\"Password\":\""+password+"\",\"VCode\":\""+identify+ "\",\"tag\":\"android\"}";
				GolukDebugUtils.e("",isRegist);
				boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_Register, isRegist);
				
				GolukDebugUtils.e("",b+"");
				if(b){
					//隐藏软件盘
					UserUtils.hideSoftMethod(this);
					mLoading.setVisibility(View.VISIBLE);
					mEditTextPhone.setEnabled(false);
					mEditTextIdentify.setEnabled(false);
					mEditTextPwd.setEnabled(false);
				}
		}
			}else{
			mBtnRegist.setFocusable(false);
		}
		}
	}
	
	*//**
	 * 注册回调
	 *//*
	public void registCallback(int success,Object obj){
		handler1.removeCallbacks(runnable);
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		GolukDebugUtils.e("","注册回调---registCallback---"+success+"---"+obj);
		mApplication.registStatus = 1;//注册中……
		if(1 == success){
			try{
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				GolukDebugUtils.e("",code+"");
				
				mLoading.setVisibility(View.GONE);
				switch (code) {
				case 200:
					//注册成功
					SysApplication.getInstance().exit();//杀死之前的所有activity，实现一键退出
					console.toast("注册成功", mContext);
					mApplication.registStatus = 2;//注册成功的状态
					GolukDebugUtils.i("registLogin", "-------"+mApplication.registStatus);
					//注册成功后再次调用登录的接口
					registLogin();
					Intent it = new Intent(UserRegistActivity.this,MainActivity.class);
					startActivity(it);
					finish();
					break;
				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;
				case 405:
					UserUtils.showDialog(this, "用户已注册");
					break;
				case 406:
					if(identifyClick){
						UserUtils.showDialog(this, "请输入正确的验证码");
					}else{
						console.toast("请先获取验证码", mContext);
					}
					break;
				case 407:
					String phone = mEditTextPhone.getText().toString();
					if(UserUtils.isMobileNO(phone)){
						if(identifyClick){
							UserUtils.showDialog(this, "输入验证码超时");
						}else{
							console.toast("请先获取验证码", mContext);
						}
					}else{
						UserUtils.showDialog(this, "手机格式输入错误,请重新输入");
					}
					break;
				case 480:
					if(identifyClick){
						UserUtils.showDialog(this, "验证码获取失败");
					}else{
						console.toast("请先获取验证码", mContext);
					}
					break;

				default:
					break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			GolukDebugUtils.e("","注册失败");
			mApp.registStatus = 3;//注册失败的状态
		}
	}*/
	
	/*final Handler handler1=new Handler();
	private Runnable runnable;
	private void initTimer(){
		runnable=new Runnable(){
		@Override
		public void run() {
			console.toast("当前网络状态不佳，请检查网络后重试", mApp.getContext());
//			mLoading.setVisibility(View.GONE);
			}
		};
	}*/
}
