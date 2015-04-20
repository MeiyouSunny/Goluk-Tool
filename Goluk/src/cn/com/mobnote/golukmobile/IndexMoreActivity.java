package cn.com.mobnote.golukmobile;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.console;
/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:Goluk首页更多页面
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("HandlerLeak")
public class IndexMoreActivity extends Activity implements OnClickListener,UserInterface {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
//	private ImageButton mBackBtn = null;
	private RelativeLayout mLayoutBack =  null;
	
	/** 本地视频item */
	private RelativeLayout mLocalVideoItem = null;
	/** 视频广场item */
//	private RelativeLayout mVideoSquareItem = null;
	/**草稿箱item */
	private RelativeLayout mDraftItem = null;
	/** 去商店item */
	private RelativeLayout mShoppingItem = null;
	/** Goluk学堂item */
	private RelativeLayout mGolukSchoolItem = null;
	/** 设置item */
	private RelativeLayout mSetupItem = null;
	
	/** 个人中心页面handler用来接收消息,更新UI*/
	public static Handler mUserCenterHandler = null;
	
	/**个人中心点击进入我的主页*/
	private RelativeLayout mLayoutHome;
	
	/**个人中心的头像、性别、昵称*/
	private ImageView mImageHead,mImageSex;
	private TextView mTextName;
	
	/**自动登录中的loading提示框**/
	private Builder mBuilder = null;
	private SharedPreferences mPreferences = null;
	private boolean isFirstLogin ;
	/**头部有无信息替换**/
	private LinearLayout mLayoutHasInfo = null;
	private LinearLayout mLayoutNoInfo = null;
	private boolean isHasInfo = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index_more);
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		mPreferences= getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);
		
		mContext = this;
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"IndexMore");
		
		//页面初始化
		init();
//		initData();
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
//		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		mLayoutBack = (RelativeLayout) findViewById(R.id.back_btn_layout);
		
		mLocalVideoItem = (RelativeLayout) findViewById(R.id.local_video_item);
//		mVideoSquareItem = (RelativeLayout) findViewById(R.id.video_square_item);
		mDraftItem = (RelativeLayout) findViewById(R.id.draft_item);
		mShoppingItem = (RelativeLayout) findViewById(R.id.shopping_item);
		mGolukSchoolItem = (RelativeLayout) findViewById(R.id.goluk_item);
		mSetupItem = (RelativeLayout) findViewById(R.id.setup_item);
		//进入我的主页
		mLayoutHome = (RelativeLayout) findViewById(R.id.head_layout);		
		//头像、性别、昵称
		mImageHead = (ImageView) findViewById(R.id.photo_img);
		mImageSex = (ImageView) findViewById(R.id.user_sex_image);
		mTextName = (TextView) findViewById(R.id.user_name_text);
		//头部有无信息的布局替换
		mLayoutHasInfo = (LinearLayout) findViewById(R.id.index_more_hasinfo);
		mLayoutNoInfo = (LinearLayout) findViewById(R.id.index_more_noinfo);
		if(!isFirstLogin || mApp.isUserLoginSucess == true){//登录过
		//更多页面
		if(mApp.autoLoginStatus == 3 || mApp.autoLoginStatus == 4){//没有用户信息
			mLayoutHasInfo.setVisibility(View.GONE);
			mLayoutNoInfo.setVisibility(View.VISIBLE);
			mImageHead.setImageResource(R.drawable.more_head_no_log_in);
			isHasInfo = false;
		}else{//有用户信息
			mLayoutHasInfo.setVisibility(View.VISIBLE);
			mLayoutNoInfo.setVisibility(View.GONE);
			initData();
			isHasInfo = true;
		}
	}else{
		//未登录
		mLayoutHasInfo.setVisibility(View.GONE);
		mLayoutNoInfo.setVisibility(View.VISIBLE);
		mImageHead.setImageResource(R.drawable.more_head_no_log_in);
	}
		
		//注册事件
//		mBackBtn.setOnClickListener(this);
		mLayoutBack.setOnClickListener(this);
		mLocalVideoItem.setOnClickListener(this);
//		mVideoSquareItem.setOnClickListener(this);
		mDraftItem.setOnClickListener(this);
		mShoppingItem.setOnClickListener(this);
		mGolukSchoolItem.setOnClickListener(this);
		mSetupItem.setOnClickListener(this);
		
		mLayoutHome.setOnClickListener(this);
		
		//更新UI handler
		mUserCenterHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
			}
		};
	}
	
	AlertDialog 	dialog = null;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		Intent intent = null;
		switch(id){
			case R.id.back_btn_layout:
				//返回
				//自动登录失败，显示未登录用户的头像等信息
//				if(mApp.autoLoginStatus == 3){
//					intent = new Intent(IndexMoreActivity.this,IndexMoreNoLoginActivity.class);
//					startActivity(intent);
//					this.finish();
//				}
				SysApplication.getInstance().exit();
				Intent it = new Intent(IndexMoreActivity.this,MainActivity.class);
				startActivity(it);
				this.finish();
			break;
			case R.id.local_video_item:
				intent = new Intent(IndexMoreActivity.this,LocalVideoListActivity.class);
				startActivity(intent);
			break;
			case R.id.draft_item:
				console.toast("草稿箱", mContext);
			break;
			case R.id.shopping_item:
				console.toast("去商店", mContext);
			break;
			case R.id.goluk_item:
				console.toast("Goluk学堂", mContext);
			break;
			case R.id.setup_item:
				//跳转到设置页面
				console.log("onclick---setup--item");
				intent = new Intent(IndexMoreActivity.this,UserSetupActivity.class);
				startActivity(intent);
				if(mApp.loginoutStatus == true){
					SysApplication.getInstance().exit();
				}
			break;
			//点击跳转到我的主页
			case R.id.head_layout:
				//自动登录中，成功，失败，超时
				Log.i("autostatus", "-----autoLoginStatus-----"+mApp.autoLoginStatus+"------isUserLoginSuccess------"+mApp.isUserLoginSucess);
				if(isHasInfo){
					mApp.mUser.setUserInterface(this);
					if(mApp.autoLoginStatus == 1){
						mBuilder = new AlertDialog.Builder(mContext);
						 dialog = mBuilder.setMessage("正在为您登录，请稍候……")
						.setCancelable(false)
						.setOnKeyListener(new OnKeyListener() {
							@Override
							public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
								// TODO Auto-generated method stub
								if(keyCode == KeyEvent.KEYCODE_BACK){
									return true;
								}
								return false;
							}
						}).create();
						
						dialog	.show();
					}else if(mApp.autoLoginStatus == 2){
						intent = new Intent(IndexMoreActivity.this,UserPersonalHomeActivity.class);
						startActivity(intent);
						if(mApp.loginoutStatus == true){
							this.finish();
						}
					}
				}else{
					Intent itNo = new Intent(IndexMoreActivity.this,UserLoginActivity.class);
					// 判断是否为自动登录失败或超时请求的登录功能
					if (mApp.autoLoginStatus == 3 || mApp.autoLoginStatus == 4) {
						mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
						String phone = mPreferences.getString("setupPhone", "");
						itNo.putExtra("autoPhone", phone);
					}
					startActivity(itNo);
					finish();
				}
				break;
		}
	}
	
	private void dismissDialog() {
		if (null != dialog){
			dialog.dismiss();
			dialog = null;
		}
	}
	
	/**
	 * 个人资料信息
	 */
	public void initData(){
		String info = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try{
			JSONObject json = new JSONObject(info);
			String head = json.getString("head");
			String name = json.getString("nickname");
			String sex = json.getString("sex");
	
			mTextName.setText(name);
			Log.i("more", head);
			UserUtils.focusHead(head, mImageHead);
			if(sex.equals("1")){
				mImageSex.setImageResource(R.drawable.more_man);
			}else if(sex.equals("2")){
				mImageSex.setImageResource(R.drawable.more_girl);
			}else if(sex.equals("0")){
				mImageSex.setImageResource(R.drawable.more_no_log_in_icon);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 对话框消失
	 */
	@Override
	public void statusChange() {
		// TODO Auto-generated method stub
		Log.i("lastTest", "-------dismiss-----"+mApp.autoLoginStatus);
		if(mApp.autoLoginStatus == 2){
			dismissDialog();
		
			Log.i("lastTest", "-------dismiss-----"+mApp.autoLoginStatus+"------ok-----dismiss---");
			Intent it = new Intent(IndexMoreActivity.this,UserPersonalHomeActivity.class);
			startActivity(it);
		}else if(mApp.autoLoginStatus == 3 || mApp.isUserLoginSucess == false){
			dismissDialog();
			console.toast("登录失败", mContext);
			mLayoutHasInfo.setVisibility(View.GONE);
			mLayoutNoInfo.setVisibility(View.VISIBLE);
		}
	}
	
}
