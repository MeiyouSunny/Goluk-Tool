package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.console;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

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
 * @ 功能描述:wifi连接疑难问题解决
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkHelpActivity extends Activity implements OnClickListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 更多按钮 */
	private Button mMoreBtn = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_help);
		mContext = this;
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"WiFiLinkHelp");
		
		//页面初始化
		init();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		mMoreBtn = (Button)findViewById(R.id.more_btn);
		
		//注册事件
		mBackBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
		
	}
	
	
	@Override
	protected void onResume(){
		mApp.setContext(this,"WiFiLinkHelp");
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
			case R.id.more_btn:
				console.toast("更多帮助", mContext);
			break;
		}
	}
	
}
