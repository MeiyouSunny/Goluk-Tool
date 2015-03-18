package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.video.LocalVideoListAdapter;
import cn.com.mobnote.video.LocalVideoManage;
import cn.com.mobnote.video.LocalVideoManage.LocalVideoData;
import cn.com.mobnote.view.MyGridView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
 * @ 功能描述:Goluk本地视频列表页面
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class LocalVideoListActivity extends Activity implements OnClickListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 本地视频列表layout */
	private LinearLayout mLocalVideoListLayout = null;
	//private ScrollView musicView = null;
	
	/** 本地视频管理类 */
	public LocalVideoManage mLocalVideoManage = null;
	/** 本地视频列表数据适配器 */
	public LocalVideoListAdapter mLocalVideoListAdapter = null;
	/** 本地视频无数据显示提示 */
	private RelativeLayout mDefaultTipLayout = null;
	/** 本地视频列表 */
	private MyGridView mLocalVideoGridView = null;
	
	/** 返回按钮 */
	private Button mBackBtn = null;
	/** 视频列表handler用来接收消息,更新UI*/
	public static Handler mVideoListHandler = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_video_list);
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"LocalVideoList");
		
		mContext = this;
		//mLayoutInflater = LayoutInflater.from(mContext);
		mLocalVideoListLayout = (LinearLayout) findViewById(R.id.localvideolistlayout);
		
		init();
		localVideoListInit();
	}
	
	/**
	 * 页面初始化
	 */
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		mDefaultTipLayout = (RelativeLayout) findViewById(R.id.defaulttiplayout);
		//注册点击事件
		mBackBtn.setOnClickListener(this);
		
		//更新UI handler
		mVideoListHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						//获取视频第一针完成
						mLocalVideoListAdapter.notifyDataSetChanged();
					break;
				}
			}
		};
	}
	
	/**
	 * 初始化本地视频列表
	 */
	private void localVideoListInit(){
		mLocalVideoGridView = createLocalVideoGridView();
		mLocalVideoManage = new LocalVideoManage(mContext,"LocalVideoList");
		ArrayList<LocalVideoData> list = mLocalVideoManage.getLocalVideoList(true);
		if(list.size() > 0){
			mDefaultTipLayout.setVisibility(View.GONE);
		}
		else{
			mDefaultTipLayout.setVisibility(View.VISIBLE);
		}
		mLocalVideoListAdapter = new LocalVideoListAdapter(mContext,list,"LocalVideoList");
		mLocalVideoGridView.setAdapter(mLocalVideoListAdapter);
		mLocalVideoListLayout.addView(mLocalVideoGridView);
	}
	
	/**
	 * 创建本地视频列表
	 * @return
	 */
	private MyGridView createLocalVideoGridView(){
		MyGridView gridLayout = new MyGridView(mContext, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gridLayout.setLayoutParams(lp);
		gridLayout.setBackgroundColor(Color.rgb(31,31,31));
		gridLayout.setHorizontalSpacing(6);
		gridLayout.setVerticalSpacing(10);
		gridLayout.setNumColumns(2);
		gridLayout.setPadding(6,10,6,10);
		//设置grid item点击效果为透明
		//gridLayout.setSelector(new ColorDrawable(Color.TRANSPARENT));
		return gridLayout;
	}
	
	/**
	 * 更新视频view
	 */
	public void videoAnalyzeComplete(){
		mLocalVideoManage.videoUploadComplete();
		mLocalVideoListAdapter.notifyDataSetChanged();
		
		//隐藏默认提示
		if(null != mDefaultTipLayout){
			mDefaultTipLayout.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onResume() {
		mApp.setContext(this,"LocalVideoList");
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				LocalVideoListActivity.this.finish();
			break;
		}
	}

}
