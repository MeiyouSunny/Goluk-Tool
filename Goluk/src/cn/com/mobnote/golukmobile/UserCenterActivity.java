package cn.com.mobnote.golukmobile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.SmsShareContent;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.MVListAdapter;
import cn.com.mobnote.video.MVManage;
import cn.com.mobnote.video.MVManage.MVEditData;
import cn.com.mobnote.view.MyGridView;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
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
 * @ 功能描述:Goluk个人中心
 * 
 * @author 陈宣宇
 * 
 */

public class UserCenterActivity extends BaseActivity implements OnClickListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private Button mBackBtn = null;
	/** 设置item */
	private RelativeLayout mSetupItem = null;
	
	/** 个人中心页面handler用来接收消息,更新UI*/
	public static Handler mUserCenterHandler = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		mContext = this;
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"UserCenter");
		
		//页面初始化
		init();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		mSetupItem = (RelativeLayout) findViewById(R.id.setup_item);
//		mNextBtn = (Button)findViewById(R.id.next_btn);
//		mPlayLayout = (RelativeLayout)findViewById(R.id.play_layout);
//		mPlayStatusImage = (ImageView)findViewById(R.id.play_image);
//		mPlayBtn.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				mMedioPlayer.start();
//			}
//		});
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mSetupItem.setOnClickListener(this);
		
		//更新UI handler
		mUserCenterHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
			}
		};
	}
	
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"UserCenter");
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				finish();
			break;
			case R.id.setup_item:
				//跳转到设置页面
				console.log("onclick---setup--item");
				Intent setup = new Intent(UserCenterActivity.this,UserSetupActivity.class);
				startActivity(setup);
			break;
		}
	}
	
}
