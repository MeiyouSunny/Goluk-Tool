package cn.com.mobnote.golukmobile;

import java.util.ArrayList;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.video.LocalVideoListAdapter;
import cn.com.mobnote.video.LocalVideoListManage;
import cn.com.mobnote.video.LocalVideoListManage.DoubleVideoData;
import cn.com.mobnote.video.LocalVideoListManage.LocalVideoData;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

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
 * @ 功能描述:Goluk本地视频分享列表页面
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class LocalVideoShareListActivity extends BaseActivity implements  OnClickListener, OnTouchListener {
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	private int emergencyFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	private int emergencyVisibleCount;
	
	
	
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 保存屏幕点击横坐标点 */
	private float screenX = 0;
	/** 返回按钮 */
	private ImageButton mBackBtn=null;
	/** 当前在那个界面，包括循环影像(1) 紧急录像(2) 一键抢拍(3) 三个界面 */
	private int mCurrentType = 2;
	/** 精彩视频切换按钮 */
	private Button mWonderfulVideoBtn = null;
	/** 紧急视频切换按钮 */
	private Button mEmergencyVideoBtn = null;
	/** 标签按钮底部高亮线条 */
	private ImageView mWonderfulVideoLine = null;
	private ImageView mEmergencyVideoLine = null;
	/** 保存选中文件列表数据 */
	private List<String> selectedListData = null;
	/** 获取当前屏幕宽度 */
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	/** 添加页脚标识,为了解决编辑最后一栏显示一半问题 */
	private boolean addWonderfulFooter = false;
	private boolean addEmergencyFooter = false;
	/** 视频列表容器 */
	private StickyListHeadersListView mWonderfulVideoList = null;
	private StickyListHeadersListView mEmergencyVideoList = null;
	/** 视频列表数据 */
	public List<LocalVideoData> mWonderfulVideoData = null;
	public List<LocalVideoData> mEmergencyVideoData = null;
	public List<DoubleVideoData> mDoubleWonderfulVideoData = null;
	public List<DoubleVideoData> mDoubleEmergencyVideoData = null;
	/** 视频列表tab数据 */
	public List<String> mWonderfulGroupName = null;
	public List<String> mEmergencyGroupName = null;
	/** 视频列表管理类 */
	public LocalVideoListManage mLocalWonderfulVideoListManage = null;
	public LocalVideoListManage mLocalEmergencyVideoListManage = null;
	/** 视频列表数据适配器 */
	public LocalVideoListAdapter mWonderfulVideoAdapter = null;
	public LocalVideoListAdapter mEmergencyVideoAdapter = null;
	
	/** 视频列表handler用来接收消息,更新UI*/
	public static Handler mVideoShareListHandler = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_video_share_list);
		
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"LocalVideoShareList");
		
		mContext = this;
		
		initView();
		initSetListener();
		initListManage();
	}
	
	
	/**
	 * 获取编辑文件列表数据
	 * @return
	 * @author xuhw
	 * @date 2015年3月30日
	 */
	public List<String> getSelectedListData(){
		return selectedListData;
	}
	
	
	/**
	 * 初始化控件
	 * @author chenxy
	 * @date 2015年3月25日
	 */
	private void initView(){
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		
		mWonderfulVideoList = (StickyListHeadersListView) findViewById(R.id.mWonderfulVideoList);
		mEmergencyVideoList = (StickyListHeadersListView) findViewById(R.id.mEmergencyVideoList);
		
		mWonderfulVideoBtn = (Button)findViewById(R.id.video_jcsp);
		mEmergencyVideoBtn = (Button)findViewById(R.id.video_jjyx);
		
		mWonderfulVideoLine = (ImageView)findViewById(R.id.line_jcsp);
		mEmergencyVideoLine = (ImageView)findViewById(R.id.line_jjyx);
		
		//视频基础数据,用来实现删除功能
		mWonderfulVideoData = new ArrayList<LocalVideoData>();
		mEmergencyVideoData = new ArrayList<LocalVideoData>();
		
		mDoubleWonderfulVideoData = new ArrayList<DoubleVideoData>();
		mDoubleEmergencyVideoData = new ArrayList<DoubleVideoData>();
		
		mWonderfulGroupName = new ArrayList<String>();
		mEmergencyGroupName = new ArrayList<String>();
		
		selectedListData = new ArrayList<String>();
		
		
		mVideoShareListHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						//获取本地精彩视频成功
						initWonderfulLayout();
					break;
					case 2:
						//获取本地紧急视频成功
						initEmergencyLayout();
					break;
				}
			}
		};
	}
	
	/**
	 * 设置控件监听事件
	 * @author chenxy
	 * @date 2015年3月25日
	 */
	private void initSetListener(){
		mBackBtn.setOnClickListener(this);
		
		mWonderfulVideoBtn.setOnClickListener(this);
		mEmergencyVideoBtn.setOnClickListener(this);
		
		mWonderfulVideoList.setOnTouchListener(this);
		mEmergencyVideoList.setOnTouchListener(this);
	}
	
	/**
	 * 初始化视频列表管理类
	 */
	private void initListManage(){
		mLocalWonderfulVideoListManage = new LocalVideoListManage(mContext,"LocalVideoShareList");
		mLocalWonderfulVideoListManage.getLocalVideoList(1);
		
		mLocalEmergencyVideoListManage = new LocalVideoListManage(mContext,"LocalVideoShareList");
		mLocalEmergencyVideoListManage.getLocalVideoList(2);
	}
	
	/**
	 * 更新标签按钮状态
	 * @param type 视频类型
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void updateButtonState(int type){
		//重置按钮文字颜色
		mWonderfulVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mEmergencyVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		
		mWonderfulVideoLine.setVisibility(View.INVISIBLE);
		mEmergencyVideoLine.setVisibility(View.INVISIBLE);
		
		switch (type) {
			case 2:
				mWonderfulVideoLine.setVisibility(View.VISIBLE);
				mWonderfulVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
			break;
			case 3:
				mEmergencyVideoLine.setVisibility(View.VISIBLE);
				mEmergencyVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
			break;
			default:
			break;
		}
	}
	
	/**
	 * 更新精彩视频列表数据
	 * @param fileList
	 * @author chenxy
	 * @date 2015年4月2日
	 */
	@SuppressLint("InflateParams")
	private void initWonderfulLayout(){
//		mWonderfulVideoList.setVisibility(View.VISIBLE);
//		mEmergencyVideoList.setVisibility(View.GONE);
//		mLoopVideoList.setVisibility(View.GONE);
		
//		mWonderfulGroupName.clear();
//		wonderfulVideoData.clear();
		
//		getGormattedData(fileList);
		
		//列表tab数据
		mWonderfulGroupName = mLocalWonderfulVideoListManage.mTabGroupName;
		//精彩视频数据
		mDoubleWonderfulVideoData = mLocalWonderfulVideoListManage.mDoubleLocalVideoListData;
		mWonderfulVideoData = mLocalWonderfulVideoListManage.mLocalVideoListData;
		
		if(null == mWonderfulVideoAdapter){
			mWonderfulVideoAdapter = new LocalVideoListAdapter(this,"LocalVideoShareList");
		}
		mWonderfulVideoAdapter.setData(mWonderfulGroupName, mDoubleWonderfulVideoData);
		//给类标底部添加一条透明区域,解决编辑看不到最后
		if(!addWonderfulFooter){
			addWonderfulFooter = true;
			LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null);
			mWonderfulVideoList.addFooterView(layout);
		}
		mWonderfulVideoList.setAdapter(mWonderfulVideoAdapter);
		
		mWonderfulVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mWonderfulVideoList.getAdapter().getCount() == (wonderfulFirstVisible+wonderfulVisibleCount)){
//						Toast.makeText(IPCFileManagerActivity.this, "滑动到最后了222", 1000).show();
					}
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				wonderfulFirstVisible = firstVisibleItem;
				wonderfulVisibleCount = visibleItemCount;
			}
		});
		mWonderfulVideoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(arg2 < mDoubleWonderfulVideoData.size()){
					RelativeLayout mTMLayout1 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout1);
					RelativeLayout mTMLayout2 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout2);
					String tag1 = (String)mTMLayout1.getTag();
					String tag2 = (String)mTMLayout2.getTag();
					
					//点击播放
					if((screenX > 0) && (screenX < (screenWidth/2))){
						//点击列表左边项,跳转到视频播放页面
						gotoVideoPlayPage(tag1);
					}
					else{
						//点击列表右边项,跳转到视频播放页面
						gotoVideoPlayPage(tag2);
					}
				}
			}
		});
	}
	
	
	/**
	 * 初始化紧急视频列表
	 * @param fileList
	 * @author chenxy
	 * @date 2015年4月3日
	 */
	@SuppressLint("InflateParams")
	private void initEmergencyLayout(){
//		mWonderfulVideoList.setVisibility(View.GONE);
//		mEmergencyVideoList.setVisibility(View.VISIBLE);
//		mLoopVideoList.setVisibility(View.GONE);
//		
//		mEmergencyGroupName.clear();
//		emergencyVideoData.clear();
		
//		getGormattedData(fileList);
		
		//列表tab数据
		mEmergencyGroupName = mLocalEmergencyVideoListManage.mTabGroupName;
		//紧急列表数据
		mDoubleEmergencyVideoData = mLocalEmergencyVideoListManage.mDoubleLocalVideoListData;
		mEmergencyVideoData = mLocalEmergencyVideoListManage.mLocalVideoListData;
		
		if(null == mEmergencyVideoAdapter){
			mEmergencyVideoAdapter = new LocalVideoListAdapter(this,"LocalVideoShareList");
		}
		mEmergencyVideoAdapter.setData(mEmergencyGroupName, mDoubleEmergencyVideoData);
		
		//给类标底部添加一条透明区域,解决编辑看不到最后
		if(!addEmergencyFooter){
			addEmergencyFooter = true;
			LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null);
			mEmergencyVideoList.addFooterView(layout);
		}
		mEmergencyVideoList.setAdapter(mEmergencyVideoAdapter);
		
		mEmergencyVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mEmergencyVideoList.getAdapter().getCount() == (emergencyFirstVisible + emergencyVisibleCount)){
						System.out.println("TTTTT=====滑动到最后了222");
					}
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				emergencyFirstVisible = firstVisibleItem;
				emergencyVisibleCount = visibleItemCount;
			}
		});
		mEmergencyVideoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(arg2 < mDoubleEmergencyVideoData.size()){
					RelativeLayout mTMLayout1 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout1);
					RelativeLayout mTMLayout2 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout2);
					String tag1 = (String)mTMLayout1.getTag();
					String tag2 = (String)mTMLayout2.getTag();
					
					//点击播放
					if((screenX > 0) && (screenX < (screenWidth/2))){
						//点击列表左边项,跳转到视频播放页面
						gotoVideoPlayPage(tag1);
					}
					else{
						//点击列表右边项,跳转到视频播放页面
						gotoVideoPlayPage(tag2);
					}
				}
			}
		});
	}
	
	/**
	 * 跳转到本地视频播放页面
	 * @param path
	 */
	private void gotoVideoPlayPage(String path){
		if(!TextUtils.isEmpty(path)){
			Intent intent = new Intent(mContext, VideoEditActivity.class);
			//intent.putExtra("from", "local");
			intent.putExtra("type", mCurrentType);
			intent.putExtra("cn.com.mobnote.video.path", path);
			startActivity(intent);
		}
	}
	
	/**
	 * 释放bitmap
	 * @param list
	 */
	private void destroyVideoBitMap(List<DoubleVideoData> list){
		for(int i = 0; i < list.size(); i++){
			DoubleVideoData info = list.get(i);
			LocalVideoData info1 = info.getVideoInfo1();
			LocalVideoData info2 = info.getVideoInfo2();
			if(null != info1.videoBitmap){
				if(!info1.videoBitmap.isRecycled()){
					info1.videoBitmap.recycle();
					info1.videoBitmap=null;
				}
			}
			if(null != info2){
				if(null != info2.videoBitmap){
					if(!info2.videoBitmap.isRecycled()){
						info2.videoBitmap.recycle();
						info2.videoBitmap=null;
					}
				}
			}
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		screenX = arg1.getX();
		return false;
	}
	
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
				finish();
				break;
			case R.id.video_jcsp:
				if(mCurrentType != 2){
					mCurrentType = 2;
					updateButtonState(mCurrentType);
					mWonderfulVideoList.setVisibility(View.VISIBLE);
					mEmergencyVideoList.setVisibility(View.GONE);
				}
			break;
			case R.id.video_jjyx:
				if(mCurrentType != 3){
					mCurrentType = 3;
					updateButtonState(mCurrentType);
					mWonderfulVideoList.setVisibility(View.GONE);
					mEmergencyVideoList.setVisibility(View.VISIBLE);
				}
			break;
			default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		mApp.setContext(this,"LocalVideoShareList");
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//释放bitmap
		if(null != mDoubleWonderfulVideoData){
			destroyVideoBitMap(mDoubleWonderfulVideoData);
		}
		if(null != mDoubleEmergencyVideoData){
			destroyVideoBitMap(mDoubleEmergencyVideoData);
		}
	}
}





