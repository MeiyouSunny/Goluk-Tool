package cn.com.mobnote.golukmobile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.VideoPlayerActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.video.LocalVideoListAdapter;
import cn.com.mobnote.video.LocalVideoListManage;
import cn.com.mobnote.video.LocalVideoListManage.DoubleVideoData;
import cn.com.mobnote.video.LocalVideoListManage.LocalVideoData;
import cn.com.tiros.debug.GolukDebugUtils;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

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
public class LocalVideoListActivity extends BaseActivity implements  OnClickListener, IPCManagerFn, OnTouchListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 保存屏幕点击横坐标点 */
	private float screenX = 0;
	
	
	
	
	
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	private int emergencyFirstVisible;
	private int loopFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	private int emergencyVisibleCount;
	private int loopVisibleCount;
	
	
	
	
	
	
	
	
	/** 保存列表总条数 */
	private int wonderfulTotalCount=0;
	private int emergencyTotalCount=0;
	private int loopTotalCount=0;
	/** 数据分页个数 */
	private int pageCount=40;
	/** 获取文件列表中标识 */
	private boolean isGetFileListDataing=false;
	
	
	
	
	
	
	
	
	
	/** 返回按钮 */
	private ImageButton mBackBtn=null;
	/** 当前在那个界面，包括循环影像(1) 紧急录像(2) 一键抢拍(3) 三个界面 */
	private int mOprateType = 2;
	private int mCurrentType = 2;
	/** 精彩视频切换按钮 */
	private Button mWonderfulVideoBtn = null;
	/** 紧急视频切换按钮 */
	private Button mEmergencyVideoBtn = null;
	/** 循环视频切换按钮 */
	private Button mLoopVideoBtn = null;
	/** 标签按钮底部高亮线条 */
	private ImageView mWonderfulVideoLine = null;
	private ImageView mEmergencyVideoLine = null;
	private ImageView mLoopVideoLine = null;
	/** 编辑按钮 */
	private Button mEditBtn = null;
	/** 功能按钮布局 */
	private RelativeLayout mFunctionLayout = null;
	private ImageView mDeleteImage = null;
	private TextView mDeleteText = null;
	/** 保存编辑状态 */
	private boolean mIsEditState = false;
	/** 保存选中文件列表数据 */
	private List<String> selectedListData = null;
	/** 获取当前屏幕宽度 */
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	
	
	
	
	/** 添加页脚标识,为了解决编辑最后一栏显示一半问题 */
	private boolean addLoopFooter = false;
	private boolean addWonderfulFooter = false;
	private boolean addEmergencyFooter = false;
	/** 视频列表容器 */
	private StickyListHeadersListView mWonderfulVideoList = null;
	private StickyListHeadersListView mEmergencyVideoList = null;
	private StickyListHeadersListView mLoopVideoList = null;
	/** 视频列表数据 */
	public List<LocalVideoData> mLoopVideoData = null;
	public List<LocalVideoData> mWonderfulVideoData = null;
	public List<LocalVideoData> mEmergencyVideoData = null;
	public List<DoubleVideoData> mDoubleLoopVideoData = null;
	public List<DoubleVideoData> mDoubleWonderfulVideoData = null;
	public List<DoubleVideoData> mDoubleEmergencyVideoData = null;
	/** 视频列表tab数据 */
	public List<String> mLoopGroupName = null;
	public List<String> mWonderfulGroupName = null;
	public List<String> mEmergencyGroupName = null;
	/** 视频列表管理类 */
	public LocalVideoListManage mLocalLoopVideoListManage = null;
	public LocalVideoListManage mLocalWonderfulVideoListManage = null;
	public LocalVideoListManage mLocalEmergencyVideoListManage = null;
	/** 视频列表数据适配器 */
	public LocalVideoListAdapter mWonderfulVideoAdapter = null;
	public LocalVideoListAdapter mEmergencyVideoAdapter = null;
	public LocalVideoListAdapter mLoopVideoAdapter = null;
	
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
	 * 获取当前是否处于编辑状态
	 * @return
	 * @author xuhw
	 * @date 2015年3月27日
	 */
	public boolean getIsEditState(){
		return mIsEditState;
	}
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 初始化控件
	 * @author chenxy
	 * @date 2015年3月25日
	 */
	private void initView(){
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		
		mLoopVideoList = (StickyListHeadersListView) findViewById(R.id.mLoopVideoList);
		mWonderfulVideoList = (StickyListHeadersListView) findViewById(R.id.mWonderfulVideoList);
		mEmergencyVideoList = (StickyListHeadersListView) findViewById(R.id.mEmergencyVideoList);
		
		mLoopVideoBtn = (Button)findViewById(R.id.video_xhyx);
		mWonderfulVideoBtn = (Button)findViewById(R.id.video_jcsp);
		mEmergencyVideoBtn = (Button)findViewById(R.id.video_jjyx);
		
		mLoopVideoLine = (ImageView)findViewById(R.id.line_xhyx);
		mWonderfulVideoLine = (ImageView)findViewById(R.id.line_jcsp);
		mEmergencyVideoLine = (ImageView)findViewById(R.id.line_jjyx);
		
		mEditBtn = (Button)findViewById(R.id.mEditBtn);
		
		mFunctionLayout = (RelativeLayout)findViewById(R.id.mFunctionLayout);
		mDeleteImage = (ImageView)findViewById(R.id.video_delete_img);
		mDeleteText = (TextView) findViewById(R.id.video_delete_txt);
		
		
		//视频基础数据,用来实现删除功能
		mLoopVideoData = new ArrayList<LocalVideoData>();
		mWonderfulVideoData = new ArrayList<LocalVideoData>();
		mEmergencyVideoData = new ArrayList<LocalVideoData>();
		
		mDoubleLoopVideoData = new ArrayList<DoubleVideoData>();
		mDoubleWonderfulVideoData = new ArrayList<DoubleVideoData>();
		mDoubleEmergencyVideoData = new ArrayList<DoubleVideoData>();
		
		mLoopGroupName = new ArrayList<String>();
		mWonderfulGroupName = new ArrayList<String>();
		mEmergencyGroupName = new ArrayList<String>();
//		
//		mLoopVideoData=new ArrayList<VideoInfo>();
//		mWonderfulVideoData=new ArrayList<VideoInfo>();
//		mEmergencyVideoData=new ArrayList<VideoInfo>();
		
		selectedListData = new ArrayList<String>();
		
		
		mVideoListHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 0:
						//获取本地循环视频成功
						initLoopLayout();
					break;
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
		
		mLoopVideoBtn.setOnClickListener(this);
		mWonderfulVideoBtn.setOnClickListener(this);
		mEmergencyVideoBtn.setOnClickListener(this);
		
		mWonderfulVideoList.setOnTouchListener(this);
		mEmergencyVideoList.setOnTouchListener(this);
		mLoopVideoList.setOnTouchListener(this);
		
		mEditBtn.setOnClickListener(this);
		
		mFunctionLayout.setOnClickListener(this);
//		mDownloadBtn.setOnClickListener(this);
//		mDeleteBtn.setOnClickListener(this);
	}
	
	/**
	 * 初始化视频列表管理类
	 */
	private void initListManage(){
//		mLocalLoopVideoListManage = new LocalVideoListManage(mContext,"LocalVideoList");
//		mLocalLoopVideoListManage.getLocalVideoList(0);
		
		mLocalWonderfulVideoListManage = new LocalVideoListManage(mContext,"LocalVideoList");
		mLocalWonderfulVideoListManage.getLocalVideoList(1);
		
		mLocalEmergencyVideoListManage = new LocalVideoListManage(mContext,"LocalVideoList");
		mLocalEmergencyVideoListManage.getLocalVideoList(2);
	}
	
	/**
	 * 改变删除按钮和下载按钮的背景
	  * @Title: updateDelandEditBg
	  * @Description: TODO
	  * @param flog void
	  * @author chenxy
	  * @throws
	 */
	private void updateDelandEditBg(boolean flog){
		if(flog){
			mDeleteImage.setBackgroundResource(R.drawable.carrecorder_icon_del);
			mDeleteText.setTextColor(this.getResources().getColor(R.color.carrecorder_del_def_bg));
		}
		else{
			mDeleteImage.setBackgroundResource(R.drawable.carrecorder_icon_del_grey);
			mDeleteText.setTextColor(this.getResources().getColor(R.color.carrecorder_del_bg));
		}
	}
	
	/**
	 * 更新标签按钮状态
	 * @param type 视频类型
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void updateButtonState(int type){
		//重置按钮文字颜色
		mLoopVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mWonderfulVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mEmergencyVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		
		mLoopVideoLine.setVisibility(View.GONE);
		mWonderfulVideoLine.setVisibility(View.INVISIBLE);
		mEmergencyVideoLine.setVisibility(View.INVISIBLE);
		
		switch (type) {
			case 1:
				mLoopVideoLine.setVisibility(View.VISIBLE);
				mLoopVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
			break;
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
	 * 初始化循环视频
	 * @param fileList
	 * @author chenxy
	 * @date 2015年4月2日
	 */
	@SuppressLint("InflateParams")
	private void initLoopLayout(){
		//mWonderfulVideoList.setVisibility(View.GONE);
		//mEmergencyVideoList.setVisibility(View.GONE);
		//mLoopVideoList.setVisibility(View.VISIBLE);
		
		//loopGroupName.clear();
		//mDoubleLoopVideoData.clear();
		
		//列表tab数据
		mLoopGroupName = mLocalLoopVideoListManage.mTabGroupName;
		//获取视频数据
		mDoubleLoopVideoData = mLocalLoopVideoListManage.mDoubleLocalVideoListData;
		mLoopVideoData = mLocalLoopVideoListManage.mLocalVideoListData;
		
		if(null == mLoopVideoAdapter){
			mLoopVideoAdapter = new LocalVideoListAdapter(this,"LocalVideoList");
		}
		mLoopVideoAdapter.setData(mLoopGroupName,mDoubleLoopVideoData);
		//给类标底部添加一条透明区域,解决编辑看不到最后
		if(!addLoopFooter){
			addLoopFooter = true;
			LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null);
			mLoopVideoList.addFooterView(layout);
		}
		mLoopVideoList.setAdapter(mLoopVideoAdapter);
		
		mLoopVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mLoopVideoList.getAdapter().getCount() == (loopFirstVisible + loopVisibleCount)){
						GolukDebugUtils.e("","循环视频列表---mLoopVideoList---滑动到最后了");
					}
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				//还不知道干啥用的...
				loopFirstVisible = firstVisibleItem;
				loopVisibleCount = visibleItemCount;
			}
		});
		mLoopVideoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(arg2 < mDoubleLoopVideoData.size()){
					RelativeLayout mTMLayout1 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout1);
					RelativeLayout mTMLayout2 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout2);
					String tag1 = (String)mTMLayout1.getTag();
					String tag2 = (String)mTMLayout2.getTag();
					
					//判断是否编辑状态
					if(mIsEditState){
						if((screenX > 0) && (screenX < (screenWidth/2))){
							selectedVideoItem(tag1,mTMLayout1);
						}else{
							selectedVideoItem(tag2,mTMLayout2);
						}
					}else{
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							//点击列表左边项,跳转到视频播放页面
							gotoVideoPlayPage(tag1);
						}else{
							//点击列表右边项,跳转到视频播放页面
							gotoVideoPlayPage(tag2);
						}
					}
				}
			}
		});
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
			mWonderfulVideoAdapter = new LocalVideoListAdapter(this,"LocalVideoList");
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
					if(mIsEditState){
						if((screenX > 0) && (screenX < (screenWidth/2))){
							selectedVideoItem(tag1,mTMLayout1);
						}else{
							selectedVideoItem(tag2,mTMLayout2);
						}
					}
					else{
						DoubleVideoData d = mDoubleWonderfulVideoData.get(arg2);
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							//点击列表左边项,跳转到视频播放页面
							gotoVideoPlayPage(tag1);
							String filename = d.getVideoInfo1().filename;
							updateNewState(filename, mLocalWonderfulVideoListManage.mLocalVideoListData);
							
							mDoubleWonderfulVideoData.get(arg2).getVideoInfo1().isNew = false;
							mLocalWonderfulVideoListManage.mDoubleLocalVideoListData.get(arg2).getVideoInfo1().isNew = false;
							mWonderfulVideoAdapter.notifyDataSetChanged();
						}
						else{
							//点击列表右边项,跳转到视频播放页面
							gotoVideoPlayPage(tag2);
							LocalVideoData info2 = d.getVideoInfo2();
							if(null == info2)
								return;
							String filename = info2.filename;
							updateNewState(filename, mLocalWonderfulVideoListManage.mLocalVideoListData);
							
							mDoubleWonderfulVideoData.get(arg2).getVideoInfo2().isNew = false;
							mLocalWonderfulVideoListManage.mDoubleLocalVideoListData.get(arg2).getVideoInfo2().isNew = false;
							mWonderfulVideoAdapter.notifyDataSetChanged();
						}
					}
				}
			}
		});
	}
	
	private void updateNewState(String filename, ArrayList<LocalVideoData> mLocalVideoListData){
		SettingUtils.getInstance().putBoolean(filename, false);
		for (int i=0; i < mLocalVideoListData.size(); i++) {
			LocalVideoData info = mLocalVideoListData.get(i);
			if (info.filename.equals(filename)) {
				mLocalVideoListData.get(i).isNew = false;
				break;
			}
		}
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
			mEmergencyVideoAdapter = new LocalVideoListAdapter(this,"LocalVideoList");
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
					if(mIsEditState){
						if((screenX > 0) && (screenX < (screenWidth/2))){
							selectedVideoItem(tag1,mTMLayout1);
						}else{
							selectedVideoItem(tag2,mTMLayout2);
						}
					}else{
						DoubleVideoData d = mDoubleEmergencyVideoData.get(arg2);
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							//点击列表左边项,跳转到视频播放页面
							gotoVideoPlayPage(tag1);
							
							String filename = d.getVideoInfo1().filename;
							updateNewState(filename, mLocalEmergencyVideoListManage.mLocalVideoListData);
							
							mDoubleEmergencyVideoData.get(arg2).getVideoInfo1().isNew = false;
							mLocalEmergencyVideoListManage.mDoubleLocalVideoListData.get(arg2).getVideoInfo1().isNew = false;
							mEmergencyVideoAdapter.notifyDataSetChanged();
						}
						else{
							//点击列表右边项,跳转到视频播放页面
							gotoVideoPlayPage(tag2);
							
							LocalVideoData info2 = d.getVideoInfo2();
							if(null == info2)
								return;
							String filename = d.getVideoInfo2().filename;
							updateNewState(filename, mLocalEmergencyVideoListManage.mLocalVideoListData);
							
							mDoubleEmergencyVideoData.get(arg2).getVideoInfo2().isNew = false;
							mLocalEmergencyVideoListManage.mDoubleLocalVideoListData.get(arg2).getVideoInfo2().isNew = false;
							mEmergencyVideoAdapter.notifyDataSetChanged();
						}
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
			Intent intent = new Intent(mContext, VideoPlayerActivity.class);
			intent.putExtra("from", "local");
			//intent.putExtra("type", mCurrentType);
			intent.putExtra("path", path);
			startActivity(intent);
		}
	}
	
	/**
	 * 选择视频item
	 * @param tag1
	 * @param mTMLayout1
	 */
	private void selectedVideoItem(String tag1,RelativeLayout mTMLayout1){
		if(!TextUtils.isEmpty(tag1)){
			if(selectedListData.contains(tag1)){
				selectedListData.remove(tag1);
				mTMLayout1.setVisibility(View.GONE);
				if(selectedListData.size() == 0){
					//说明没有任何item被选中
					updateDelandEditBg(false);
				}
			}
			else{
				selectedListData.add(tag1);
				mTMLayout1.setVisibility(View.VISIBLE);
				if(selectedListData.size() > 0){
					//说明有item被选中
					updateDelandEditBg(true);
				}
			}
		}
	}
	
	/**
	 * 删除选择的视频item
	 */
	private void deleteSelectItemData(){
		//保存要删除的文件路径
		ArrayList<String> filesPath = new ArrayList<String>();
		//保存要删除的视频截图路径
		ArrayList<String> videoImagesPath = new ArrayList<String>();
		
		for(String filename : selectedListData){
			if(1 == mCurrentType){
				for(LocalVideoData info : mLoopVideoData){
					if(info.videoPath.equals(filename)){
						mLoopVideoData.remove(info);
						//保存要删除的文件路径
						filesPath.add(info.videoPath);
						String imagePath = info.videoImagePath;
						if(!imagePath.equals("") && imagePath != null){
							videoImagesPath.add(imagePath);
						}
						break;
					}
				}
			}
			else if(2 == mCurrentType){
				for(LocalVideoData info : mWonderfulVideoData){
					if(info.videoPath.equals(filename)){
						mWonderfulVideoData.remove(info);
						//保存要删除的文件路径
						filesPath.add(info.videoPath);
						String imagePath = info.videoImagePath;
						if(!imagePath.equals("") && imagePath != null){
							videoImagesPath.add(imagePath);
						}
						break;
					}
				}
			}
			else{
				for(LocalVideoData info : mEmergencyVideoData){
					if(info.videoPath.equals(filename)){
						mEmergencyVideoData.remove(info);
						//保存要删除的文件路径
						filesPath.add(info.videoPath);
						String imagePath = info.videoImagePath;
						if(!imagePath.equals("") && imagePath != null){
							videoImagesPath.add(imagePath);
						}
						break;
					}
				}
			}
			
			String fileName = filename.replace(".mp4", ".jpg");
			String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
			File file = new File(filePath + File.separator + fileName);
			if (file.exists()) {
				file.delete();
			}
		}
		
		if(1 == mCurrentType){
			//重组group tab数据
			mLocalLoopVideoListManage.setGroupTabData(mLoopVideoData);
			//重组视频列表数据
			mLocalLoopVideoListManage.videoInfo2Double(mLoopVideoData);
			mLoopVideoAdapter.setData(mLoopGroupName,mDoubleLoopVideoData);
			mLoopVideoAdapter.notifyDataSetChanged();
			
			//删除选择的文件路径
			mLocalLoopVideoListManage.deleteLocalVideoData(filesPath,videoImagesPath,0);
		}else if(2 == mCurrentType){
			//重组group tab数据
			mLocalWonderfulVideoListManage.setGroupTabData(mWonderfulVideoData);
			//重组视频列表数据
			mLocalWonderfulVideoListManage.videoInfo2Double(mWonderfulVideoData);
			mWonderfulVideoAdapter.setData(mWonderfulGroupName,mDoubleWonderfulVideoData);
			mWonderfulVideoAdapter.notifyDataSetChanged();
			
			//删除选择的文件路径
			mLocalWonderfulVideoListManage.deleteLocalVideoData(filesPath,videoImagesPath,1);
		}else{
			//重组group tab数据
			mLocalEmergencyVideoListManage.setGroupTabData(mEmergencyVideoData);
			//重组视频列表数据
			mLocalEmergencyVideoListManage.videoInfo2Double(mEmergencyVideoData);
			mEmergencyVideoAdapter.setData(mEmergencyGroupName,mDoubleEmergencyVideoData);
			mEmergencyVideoAdapter.notifyDataSetChanged();
			
			//删除选择的文件路径
			mLocalEmergencyVideoListManage.deleteLocalVideoData(filesPath,videoImagesPath,2);
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
			case R.id.video_xhyx:
				if(!mIsEditState){
					if(mCurrentType != 1){
						mCurrentType = 1;
						updateButtonState(mCurrentType);
						mLoopVideoList.setVisibility(View.VISIBLE);
						mWonderfulVideoList.setVisibility(View.GONE);
						mEmergencyVideoList.setVisibility(View.GONE);
						
					}
				}
			break;
			case R.id.video_jcsp:
				if(!mIsEditState){
					if(mCurrentType != 2){
						mCurrentType = 2;
						updateButtonState(mCurrentType);
						mLoopVideoList.setVisibility(View.GONE);
						mWonderfulVideoList.setVisibility(View.VISIBLE);
						mEmergencyVideoList.setVisibility(View.GONE);
					}
				}
			break;
			case R.id.video_jjyx:
				if(!mIsEditState){
					if(mCurrentType != 3){
						mCurrentType = 3;
						updateButtonState(mCurrentType);
						mLoopVideoList.setVisibility(View.GONE);
						mWonderfulVideoList.setVisibility(View.GONE);
						mEmergencyVideoList.setVisibility(View.VISIBLE);
					}
				}
			break;
			case R.id.mEditBtn:
				if(!mIsEditState){
					mEditBtn.setText("取消");
					mIsEditState = true;
					selectedListData.clear();
					mFunctionLayout.setVisibility(View.VISIBLE);
				}else{
					mEditBtn.setText("编辑");
					mIsEditState = false;
					selectedListData.clear();
					//把下载和删除按钮的北京颜色还原回去
					updateDelandEditBg(false);
					mFunctionLayout.setVisibility(View.GONE);
				}
				
				if(1 == mCurrentType){
					if(null != mLoopVideoAdapter){
						mLoopVideoAdapter.notifyDataSetChanged();
					}
				}
				else if(2 == mCurrentType){
					if(null != mWonderfulVideoAdapter){
						mWonderfulVideoAdapter.notifyDataSetChanged();
					}
				}
				else{
					if(null != mEmergencyVideoAdapter){
						mEmergencyVideoAdapter.notifyDataSetChanged();
					}
				}
			break;
			
			case R.id.mFunctionLayout:
				//删除选择视频
				mIsEditState = false;
				mEditBtn.setText("编辑");
				mFunctionLayout.setVisibility(View.GONE);
				
				if(null != selectedListData){
					if(selectedListData.size() > 0){
						//删除选择视频数据
						deleteSelectItemData();
					}
				}
			break;
			default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		mApp.setContext(this,"LocalVideoList");
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
		if(null != mDoubleLoopVideoData){
			destroyVideoBitMap(mDoubleLoopVideoData);
		}
		if(null != mDoubleWonderfulVideoData){
			destroyVideoBitMap(mDoubleWonderfulVideoData);
		}
		if(null != mDoubleEmergencyVideoData){
			destroyVideoBitMap(mDoubleEmergencyVideoData);
		}
	}


	
	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		switch (event) {
		case ENetTransEvent_IPC_VDCP_CommandResp:
			if (IPC_VDCP_Msg_Query == msg) {
				isGetFileListDataing=false;
				GFileUtils.writeIPCLog("===========获取文件列表===3333=============param1="+ param1 + "=====param2=" + param2);
				if (RESULE_SUCESS == param1) {
					ArrayList<VideoFileInfo> fileList = IpcDataParser.parseMoreFile((String) param2);
					int total = IpcDataParser.getFileListCount((String) param2);
					if (null != fileList) {
						GFileUtils.writeIPCLog("===========获取文件列表===44444============get data success=========");
						mCurrentType = mOprateType;
						updateButtonState(mCurrentType);
						if(TYPE_SHORTCUT == mOprateType){//精彩视频
							wonderfulTotalCount = total;
//							initWonderfulLayout(fileList);
						}else if(TYPE_URGENT == mOprateType){//紧急视频
							emergencyTotalCount = total;
//							initEmergencyLayout(fileList);
						}else{//循环视频
							loopVisibleCount = total;
//							initLoopLayout(fileList);
						}
					} else {
						// 列表数据空
						GFileUtils
								.writeIPCLog("===========获取文件列表===5555============ data null=========");
					}
				} else {
					// 命令发送失败
					GFileUtils
							.writeIPCLog("===========获取文件列表===6666============  not success =========");
				}
			}else if(IPC_VDCPCmd_TriggerRecord == msg){
				GFileUtils
				.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord==========222222222222222222 =========");
			//文件删除
			}else if(IPC_VDCPCmd_Erase == msg){
				System.out.println("QQQ==========param1="+param1+"===param2="+param2);
			}
			break;
		// IPC下载结果应答
		case ENetTransEvent_IPC_VDTP_Resp:
			GFileUtils
					.writeIPCLog("===========下载文件===2222222=============param1="
							+ param1 + "=====param2=" + param2);
			// 文件传输消息
			if (IPC_VDTP_Msg_File == msg) {
				// 文件下载成功
				if (RESULE_SUCESS == param1) {
					try {
						JSONObject json = new JSONObject((String) param2);
						if (null != json) {
							String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
							String filename = json.optString("filename");
							if(filename.contains(".jpg")){
							String tag = json.optString("tag");
		System.out.println("TTT=====22222====filename="+filename+"===tag="+tag);
							if(TYPE_SHORTCUT == mCurrentType){//精彩视频
								if (null != mWonderfulVideoAdapter) {
//									for(int i=0; i<mWonderfulVideoData.size(); i++){
//										DoubleVideoInfo info =  wonderfulVideoData.get(i);
//										String id1 = info.getVideoInfo1().id + "";
//										if (tag.equals(id1)) {
//											System.out.println("TTT===wonderful=3333=====filename="+filename+"===tag="+tag);
//											wonderfulVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
//													.getBitmapFromCache(filePath
//															+ File.separator
//															+ filename, 194, 109);
//										}
//										
//										if(null != info.getVideoInfo2()){
//											String id2 = info.getVideoInfo2().id + "";
//											if(!TextUtils.isEmpty(id2)){
//												if(tag.equals(id2)){
//													System.out.println("TTT===wonderful=4444=====filename="+filename+"===tag="+tag);
//													wonderfulVideoData.get(i).getVideoInfo2().videoBitmap = ImageManager
//															.getBitmapFromCache(filePath
//																	+ File.separator
//																	+ filename, 194, 109);
//												}
//											}
//										}
										
//									}
									
									mWonderfulVideoAdapter.notifyDataSetChanged();
								}
							}else if(TYPE_URGENT == mCurrentType){//紧急视频
								if (null != mEmergencyVideoAdapter) {
									for(int i=0; i<mDoubleEmergencyVideoData.size(); i++){
//										DoubleVideoInfo info =  emergencyVideoData.get(i);
//										String id1 = info.getVideoInfo1().id + "";
//										if (tag.equals(id1)) {
//											System.out.println("TTT==emergency==3333=====filename="+filename+"===tag="+tag);
//											emergencyVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
//													.getBitmapFromCache(filePath
//															+ File.separator
//															+ filename, 194, 109);
//										}
//										
//										if(null != info.getVideoInfo2()){
//											String id2 = info.getVideoInfo2().id + "";
//											if(!TextUtils.isEmpty(id2)){
//												if(tag.equals(id2)){
//													System.out.println("TTT==emergency==4444=====filename="+filename+"===tag="+tag);
//													emergencyVideoData.get(i).getVideoInfo2().videoBitmap = ImageManager
//															.getBitmapFromCache(filePath
//																	+ File.separator
//																	+ filename, 194, 109);
//												}
//											}
//										}
										
									}
									
									mEmergencyVideoAdapter.notifyDataSetChanged();
								}
							}else{//循环视频
								if (null != mLoopVideoAdapter) {
									for(int i=0; i<mDoubleLoopVideoData.size(); i++){
//										DoubleVideoInfo info =  loopVideoData.get(i);
//										String id1 = info.getVideoInfo1().id + "";
//										if (tag.equals(id1)) {
//											System.out.println("TTT==loop==3333=====filename="+filename+"===tag="+tag);
//											loopVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
//													.getBitmapFromCache(filePath
//															+ File.separator
//															+ filename, 194, 109);
//										}
										
//										if(null != info.getVideoInfo2()){
//											String id2 = info.getVideoInfo2().id + "";
//											if(!TextUtils.isEmpty(id2)){
//												if(tag.equals(id2)){
//													System.out.println("TTT===loop=4444=====filename="+filename+"===tag="+tag);
//													loopVideoData.get(i).getVideoInfo2().videoBitmap = ImageManager
//															.getBitmapFromCache(filePath
//																	+ File.separator
//																	+ filename, 194, 109);
//												}
//											}
//										}
//										
									}
									
									mLoopVideoAdapter.notifyDataSetChanged();
								}
							}
							}else{
								System.out.println("TTT======2222=filename="+filename);
							}
							
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					// 文件下载中进度
				} else if (1 == param1) {
					// param1为文件下载进度
				} else {
					// 其他下载失败
				}
			}
			break;
			
		default:
			break;
		}

	}

}
