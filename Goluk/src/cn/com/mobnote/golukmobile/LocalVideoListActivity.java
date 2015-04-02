package cn.com.mobnote.golukmobile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.Utils;
import cn.com.mobnote.golukmobile.carrecorder.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.VideoPlayerActivity;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.LocalVideoListAdapter;
import cn.com.mobnote.video.LocalVideoListManage;
import cn.com.mobnote.video.LocalVideoListManage.DoubleVideoData;
import cn.com.tiros.api.FileUtils;
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
import android.widget.TextView;
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
 * @ 功能描述:Goluk本地视频列表页面
 * 
 * @author 陈宣宇
 * 
 */
@SuppressLint("HandlerLeak")
public class LocalVideoListActivity extends Activity implements  OnClickListener, IPCManagerFn, OnTouchListener {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	private StickyListHeadersListView mWonderfulVideoList=null;
	private StickyListHeadersListView mEmergencyVideoList=null;
	private StickyListHeadersListView mLoopVideoList=null;
	private LocalVideoListAdapter mWonderfulVideoAdapter=null;
	private LocalVideoListAdapter mEmergencyVideoAdapter=null;
	private LocalVideoListAdapter mLoopVideoAdapter=null;
	
	private List<VideoInfo> mWonderfulVideoData=null;
	private List<VideoInfo> mEmergencyVideoData=null;
	private List<VideoInfo> mLoopVideoData=null;
	
	
	
	
	/** 保存屏幕点击横坐标点 */
	private float screenX=0;
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	private int emergencyFirstVisible;
	private int loopFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	private int emergencyVisibleCount;
	private int loopVisibleCount;
	/** 返回按钮 */
	private ImageButton mBackBtn=null;
	/** 当前在那个界面，包括循环影像(1) 紧急录像(2) 一键抢拍(4) 三个界面 */
	private int mOprateType = 0;
	private int mCurrentType = 0;
	/** 精彩视频切换按钮 */
	private Button mWonderfulVideoBtn=null;
	/** 紧急视频切换按钮 */
	private Button mEmergencyVideoBtn=null;
	/** 循环视频切换按钮 */
	private Button mLoopVideoBtn=null;
	/** 标签底部高亮线条 */
	private ImageView mWonderfulVideoLine=null;
	private ImageView mEmergencyVideoLine=null;
	private ImageView mLoopVideoLine=null;
	/** 保存列表总条数 */
	private int wonderfulTotalCount=0;
	private int emergencyTotalCount=0;
	private int loopTotalCount=0;
	/** 数据分页个数 */
	private int pageCount=40;
	/** 编辑按钮 */
	private Button mEditBtn=null;
	/** 功能按钮布局 */
	private LinearLayout mFunctionLayout=null;
	private LinearLayout mDownloadBtn=null;
	private LinearLayout mDeleteBtn=null;
	/** 保存编辑状态 */
	private boolean isEditState=false;
	/** 保存选中文件列表数据 */
	private List<String> selectedListData=null;
	/** 获取当前屏幕宽度 */
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	/** 获取文件列表中标识 */
	private boolean isGetFileListDataing=false;
	/** 添加页脚标识 */
	private boolean addWonderfulFooter=false;
	private boolean addEmergencyFooter=false;
	private boolean addLoopFooter=false;
	
	
	
	/** 视频列表数据 */
	public List<DoubleVideoData> mDoubleLoopVideoData = null;
	public List<DoubleVideoData> wonderfulVideoData = null;
	public List<DoubleVideoData> emergencyVideoData = null;
	/** 视频列表tab数据 */
	public List<String> mLoopGroupName = null;
	public List<String> mWonderfulGroupName = null;
	public List<String> mEmergencyGroupName = null;
	
	
	/** 视频列表管理类 */
	public LocalVideoListManage mLocalLoopVideoListManage = null;
	public LocalVideoListManage mLocalWonderfulVideoListManage = null;
	public LocalVideoListManage mLocalEmergencyVideoListManage = null;
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
		
//		
//		ArrayList<VideoFileInfo> fileList = new ArrayList<VideoFileInfo>();
//		mCurrentType = 4;
//		updateButtonState(mCurrentType);
//		wonderfulTotalCount = 10;
//		for(int i = 0; i < 10; i++){
//			VideoFileInfo f1 = new VideoFileInfo();
//			f1.id = i;
//			f1.time = i;
//			f1.period = i;
//			f1.type = 1;
//			f1.size = i * 1000;
//			f1.location = i + "WND1_150312161511_0012.mp4";
//			f1.resolution = i;
//			f1.withSnapshot = 0;
//			f1.withGps = 0;
//			fileList.add(f1);
//		}
//		initWonderfulLayout(fileList);
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
		return isEditState;
	}
	/**
	 * 将原有数据格式化成列表需要的数据
	 * @param fileList 原IPC列表数据
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void getGormattedData(ArrayList<VideoFileInfo> fileList){
		//初始化分组名称
		for (VideoFileInfo info : fileList) {
			String name = Utils.getTimeStr(info.time * 1000).substring(0, 10);
			if(name.length() >= 10){
				if(TYPE_SHORTCUT == mCurrentType){
					if(!mWonderfulGroupName.contains(name.substring(0, 10))){
						mWonderfulGroupName.add(name.substring(0, 10));
					}
				}else if(TYPE_URGENT == mCurrentType){
					if(!mEmergencyGroupName.contains(name.substring(0, 10))){
						mEmergencyGroupName.add(name.substring(0, 10));
					}
				}else{
					if(!mLoopGroupName.contains(name.substring(0, 10))){
						mLoopGroupName.add(name.substring(0, 10));
					}
				}
			}
		}
		
		for (VideoFileInfo info : fileList) {
			if(TYPE_SHORTCUT == mCurrentType){
				mWonderfulVideoData.add(getVideoInfo(info));
			}else if(TYPE_URGENT == mCurrentType){
				mEmergencyVideoData.add(getVideoInfo(info));
			}else{
				mLoopVideoData.add(getVideoInfo(info));
			}
		}
		
	}
	
	/**
	 * 单个视频数据对象转双个
	 * @param datalist
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private List<DoubleVideoInfo> videoInfo2Double(List<VideoInfo> datalist){
		List<DoubleVideoInfo> doublelist = new ArrayList<DoubleVideoInfo>();
		int i=0;
		while(i<datalist.size()){
			String groupname1 = "";
			String groupname2 = "";
			VideoInfo _videoInfo1 = null;
			VideoInfo _videoInfo2 = null;
			_videoInfo1 = datalist.get(i);
			groupname1 = _videoInfo1.videoCreateDate.substring(0, 10);
			
			if((i+1) < datalist.size()){
				_videoInfo2 = datalist.get(i+1);
				groupname2 = _videoInfo2.videoCreateDate.substring(0, 10);
			}
			
			if(groupname1.equals(groupname2)){
				i += 2;
			}else{
				i++;
				_videoInfo2=null;
			}
			
			DoubleVideoInfo dub = new DoubleVideoInfo(_videoInfo1, _videoInfo2);
			doublelist.add(dub);
		}
		
		return doublelist;
	}
	
	/**
	 * 更新精彩视频列表数据
	 * @param fileList
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void initWonderfulLayout(){
		mWonderfulVideoList.setVisibility(View.VISIBLE);
		mEmergencyVideoList.setVisibility(View.GONE);
		mLoopVideoList.setVisibility(View.GONE);
		
		mWonderfulGroupName.clear();
		wonderfulVideoData.clear();
		
//		getGormattedData(fileList);
		wonderfulVideoData = mLocalWonderfulVideoListManage.mLocalVideoListData;
		
		
		if(null == mWonderfulVideoAdapter){
			mWonderfulVideoAdapter = new LocalVideoListAdapter(this);
		}
		mWonderfulVideoAdapter.setData(mWonderfulGroupName, wonderfulVideoData);
		
		if(!addWonderfulFooter){
			addWonderfulFooter=true;
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
						System.out.println("TTTTT=====滑动到最后了222");
					}
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				wonderfulFirstVisible=firstVisibleItem;
				wonderfulVisibleCount=visibleItemCount;
			}
		});
		mWonderfulVideoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(arg2 < wonderfulVideoData.size()){
					RelativeLayout mTMLayout1 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout1);
					RelativeLayout mTMLayout2 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout2);
					String tag1 = (String)mTMLayout1.getTag();
					String tag2 = (String)mTMLayout2.getTag();
					if(isEditState){
						if((screenX > 0) && (screenX < (screenWidth/2))){
							
							
							if(!TextUtils.isEmpty(tag1)){
								selectedListData.remove(tag1);
								mTMLayout1.setVisibility(View.GONE);
								if(selectedListData.size()==0){//说明没有任何item被选中
									updateDelandEditBg(false);
								}
							}else{
								selectedListData.add(tag1);
								mTMLayout1.setVisibility(View.VISIBLE);
								if(selectedListData.size()>0){
									updateDelandEditBg(true);
								}
							}
							
							
						}else{
							
							
							if(!TextUtils.isEmpty(tag2)){
								if(selectedListData.contains(tag2)){
									selectedListData.remove(tag2);
									mTMLayout2.setVisibility(View.GONE);
									if(selectedListData.size()==0){//说明没有任何item被选中
										updateDelandEditBg(false);
									}
								}else{
									selectedListData.add(tag2);
									mTMLayout2.setVisibility(View.VISIBLE);
									if(selectedListData.size()>0){
										updateDelandEditBg(true);
									}
								}
							}
							
							
						}
					}else{
						
						
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							if(!TextUtils.isEmpty(tag1)){
//								Intent intent = new Intent(IPCFileManagerActivity.this, VideoPlayerActivity.class);
//								intent.putExtra("from", "ipc");
//								intent.putExtra("type", mCurrentType);
//								intent.putExtra("filename", tag1);
//								startActivity(intent);
							}
						}else{
							if(!TextUtils.isEmpty(tag2)){
//								Intent intent = new Intent(IPCFileManagerActivity.this, VideoPlayerActivity.class);
//								intent.putExtra("from", "ipc");
//								intent.putExtra("type", mCurrentType);
//								intent.putExtra("filename", tag2);
//								startActivity(intent);
							}
						}
						
						
					}
				}
				
				
				 
				}
			});
		
	}
	
	
	/**
	 * 改变删除按钮和下载按钮的背景
	  * @Title: updateDelandEditBg 
	  * @Description: TODO
	  * @param flog void 
	  * @author 曾浩 
	  * @throws
	 */
	public void updateDelandEditBg(boolean flog){
		TextView deltv = null;
		TextView downloadtv = null;
		if(flog){
			findViewById(R.id.video_delete_img).setBackgroundResource(R.drawable.carrecorder_icon_del);
			deltv = (TextView) findViewById(R.id.video_delete_txt);
			deltv.setTextColor(this.getResources().getColor(R.color.carrecorder_del_def_bg));
			
			findViewById(R.id.video_download_img).setBackgroundResource(R.drawable.carrecorder_icon_download);
			downloadtv = (TextView) findViewById(R.id.video_download_txt);
			downloadtv.setTextColor(this.getResources().getColor(R.color.carrecorder_del_def_bg));
			
		}else{
			findViewById(R.id.video_delete_img).setBackgroundResource(R.drawable.carrecorder_icon_del_grey);
			deltv = (TextView) findViewById(R.id.video_delete_txt);
			deltv.setTextColor(this.getResources().getColor(R.color.carrecorder_del_bg));
			
			findViewById(R.id.video_download_img).setBackgroundResource(R.drawable.carrecorder_icon_download_grey);
			downloadtv = (TextView) findViewById(R.id.video_download_txt);
			downloadtv.setTextColor(this.getResources().getColor(R.color.carrecorder_del_bg));
		}
	}
	
	/**
	 * 初始化紧急视频列表
	 * @param fileList
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void initEmergencyLayout(){
		mWonderfulVideoList.setVisibility(View.GONE);
		mEmergencyVideoList.setVisibility(View.VISIBLE);
		mLoopVideoList.setVisibility(View.GONE);
		
		mEmergencyGroupName.clear();
		emergencyVideoData.clear();
		
//		getGormattedData(fileList);
		emergencyVideoData = mLocalEmergencyVideoListManage.mLocalVideoListData;
		
		if(null == mEmergencyVideoAdapter){
			mEmergencyVideoAdapter = new LocalVideoListAdapter(this);
		}
		mEmergencyVideoAdapter.setData(mEmergencyGroupName, emergencyVideoData);
		if(!addEmergencyFooter){
			addEmergencyFooter=true;
			LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null); 
			mEmergencyVideoList.addFooterView(layout);
		}
		mEmergencyVideoList.setAdapter(mEmergencyVideoAdapter);
		
		mEmergencyVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mEmergencyVideoList.getAdapter().getCount() == (emergencyFirstVisible + emergencyVisibleCount)){
						
//						Toast.makeText(IPCFileManagerActivity.this, "滑动到最后了222", 1000).show();
						System.out.println("TTTTT=====滑动到最后了222");
					}
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				emergencyFirstVisible=firstVisibleItem;
				emergencyVisibleCount=visibleItemCount;
			}
		});
		mEmergencyVideoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(arg2 < emergencyVideoData.size()){
					RelativeLayout mTMLayout1 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout1);
					RelativeLayout mTMLayout2 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout2);
					String tag1 = (String)mTMLayout1.getTag();
					String tag2 = (String)mTMLayout2.getTag();
					if(isEditState){
						if((screenX > 0) && (screenX < (screenWidth/2))){
							if(!TextUtils.isEmpty(tag1)){
								if(selectedListData.contains(tag1)){
									selectedListData.remove(tag1);
									mTMLayout1.setVisibility(View.GONE);
									if(selectedListData.size()==0){//说明没有任何item被选中
										updateDelandEditBg(false);
									}
								}else{
									selectedListData.add(tag1);
									mTMLayout1.setVisibility(View.VISIBLE);
									if(selectedListData.size()>0){//说明有item被选中
										updateDelandEditBg(true);
									}
								}
							}
						}else{
							
							if(!TextUtils.isEmpty(tag2)){
								if(selectedListData.contains(tag2)){
									selectedListData.remove(tag2);
									mTMLayout2.setVisibility(View.GONE);
									if(selectedListData.size()==0){//说明没有任何item被选中
										updateDelandEditBg(false);
									}
								}else{
									selectedListData.add(tag2);
									mTMLayout2.setVisibility(View.VISIBLE);
									if(selectedListData.size()>0){//说明有item被选中
										updateDelandEditBg(true);
									}
								}
							}
							
						}
					}else{
					
					
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							if(!TextUtils.isEmpty(tag1)){
//								Intent intent = new Intent(IPCFileManagerActivity.this, VideoPlayerActivity.class);
//								intent.putExtra("from", "ipc");
//								intent.putExtra("type", mCurrentType);
//								intent.putExtra("filename", tag1);
//								startActivity(intent);
							}
						}else{
							if(!TextUtils.isEmpty(tag2)){
//								Intent intent = new Intent(IPCFileManagerActivity.this, VideoPlayerActivity.class);
//								intent.putExtra("from", "ipc");
//								intent.putExtra("type", mCurrentType);
//								intent.putExtra("filename", tag2);
//								startActivity(intent);
							}
						}
					
					
					}
				
				
				
				
				}
				
			}
		});
	}
	
	
	
	/**
	 * 更新标签按钮状态
	 * @param type 视频类型
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void updateButtonState(int type){
		mWonderfulVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mEmergencyVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mLoopVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mWonderfulVideoLine.setVisibility(View.INVISIBLE);
		mEmergencyVideoLine.setVisibility(View.INVISIBLE);
		mLoopVideoLine.setVisibility(View.INVISIBLE);
		
		switch (type) {
			case TYPE_SHORTCUT:
				mWonderfulVideoLine.setVisibility(View.VISIBLE);
				mWonderfulVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				break;
			case TYPE_URGENT:
				mEmergencyVideoLine.setVisibility(View.VISIBLE);
				mEmergencyVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				break;
			case TYPE_CIRCULATE:
				mLoopVideoLine.setVisibility(View.VISIBLE);
				mLoopVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				break;
			default:
				break;
		}
	}

	/**
	 * 获取文件列表信息
	 * @param type 1:循环影像 2:紧急录像 4:精彩视频
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void getRecorderFileFromLocal(int type) {
		isGetFileListDataing=true;
		mOprateType = type;
		updateButtonState(type);
		boolean isSucess = GolukApplication.getInstance().getIPCControlManager().queryFileListInfo(type, pageCount, 0);
		GFileUtils.writeIPCLog("===========获取文件列表===1111===================isSucess=="+isSucess);
		if(!isSucess){
			isGetFileListDataing=false;
		}
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
		
		mFunctionLayout = (LinearLayout)findViewById(R.id.mFunctionLayout);
		mDownloadBtn = (LinearLayout)findViewById(R.id.mDownloadBtn);
		mDeleteBtn = (LinearLayout)findViewById(R.id.mDeleteBtn);
		
		
		mDoubleLoopVideoData = new ArrayList<DoubleVideoData>();
		wonderfulVideoData = new ArrayList<DoubleVideoData>();
		emergencyVideoData = new ArrayList<DoubleVideoData>();
		
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
						initWonderfulLayout();
					break;
					case 2:
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
		mEmergencyVideoBtn.setOnClickListener(this);
		mWonderfulVideoBtn.setOnClickListener(this);
		mLoopVideoBtn.setOnClickListener(this);
		mWonderfulVideoList.setOnTouchListener(this);
		mEmergencyVideoList.setOnTouchListener(this);
		mLoopVideoList.setOnTouchListener(this);
		mEditBtn.setOnClickListener(this);
		mDownloadBtn.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);
	}
	
	/**
	 * 初始化视频列表管理类
	 */
	private void initListManage(){
		mLocalLoopVideoListManage = new LocalVideoListManage(mContext);
		mLocalLoopVideoListManage.getLocalVideoList(0);
		
//		mLocalWonderfulVideoListManage = new LocalVideoListManage(mContext);
//		mLocalWonderfulVideoListManage.getLocalVideoList(1);
//		
//		mLocalEmergencyVideoListManage = new LocalVideoListManage(mContext);
//		mLocalEmergencyVideoListManage.getLocalVideoList(2);
	}
	
	/**
	 * 初始化循环视频
	 * @param fileList
	 * @author chenxy
	 * @date 2015年4月2日
	 */
	private void initLoopLayout(){
		//mWonderfulVideoList.setVisibility(View.GONE);
		//mEmergencyVideoList.setVisibility(View.GONE);
		//mLoopVideoList.setVisibility(View.VISIBLE);
		
		//loopGroupName.clear();
		//mDoubleLoopVideoData.clear();
		
		//列表tab数据
		mLoopGroupName = mLocalLoopVideoListManage.mTabGroupName;
		//获取视频数据
		mDoubleLoopVideoData = mLocalLoopVideoListManage.mLocalVideoListData;
		if(null == mLoopVideoAdapter){
			mLoopVideoAdapter = new LocalVideoListAdapter(this);
		}
		mLoopVideoAdapter.setData(mLoopGroupName,mDoubleLoopVideoData);
		//先注释
//		if(!addLoopFooter){
//			addLoopFooter=true;
//			LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null); 
//			mLoopVideoList.addFooterView(layout);
//		}
		mLoopVideoList.setAdapter(mLoopVideoAdapter);
		
		mLoopVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mLoopVideoList.getAdapter().getCount() == (loopFirstVisible + loopVisibleCount)){
						console.log("循环视频列表---mLoopVideoList---滑动到最后了");
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
					if(isEditState){
						if((screenX > 0) && (screenX < (screenWidth/2))){
							if(!TextUtils.isEmpty(tag1)){
								if(selectedListData.contains(tag1)){
									selectedListData.remove(tag1);
									mTMLayout1.setVisibility(View.GONE);
									if(selectedListData.size()==0){//说明没有任何item被选中
										updateDelandEditBg(false);
									}
								}else{
									selectedListData.add(tag1);
									mTMLayout1.setVisibility(View.VISIBLE);
									if(selectedListData.size()>0){//说明有item被选中
										updateDelandEditBg(true);
									}
								}
							}
						}else{
							if(!TextUtils.isEmpty(tag2)){
								if(selectedListData.contains(tag2)){
									selectedListData.remove(tag2);
									mTMLayout2.setVisibility(View.GONE);
									if(selectedListData.size()==0){//说明没有任何item被选中
										updateDelandEditBg(false);
									}
								}else{
									selectedListData.add(tag2);
									mTMLayout2.setVisibility(View.VISIBLE);
									if(selectedListData.size()>0){//说明有item被选中
										updateDelandEditBg(true);
									}
								}
							}
							}
					}else{
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							if(!TextUtils.isEmpty(tag1)){
								Intent intent = new Intent(mContext, VideoPlayerActivity.class);
								intent.putExtra("from", "localVideoList");
								intent.putExtra("type", mCurrentType);
								intent.putExtra("filename", tag1);
								startActivity(intent);
							}
						}else{
							if(!TextUtils.isEmpty(tag2)){
								Intent intent = new Intent(mContext, VideoPlayerActivity.class);
								intent.putExtra("from", "localVideoList");
								intent.putExtra("type", mCurrentType);
								intent.putExtra("filename", tag2);
								startActivity(intent);
							}
						}
					}
				}
			}
		});
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		screenX = arg1.getX();
		System.out.println("QQQ===============screenX="+screenX);
		return false;
	}
	
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
				finish();
				break;
			case R.id.video_jcsp:
				if(!isGetFileListDataing){
					if(!isEditState){
						if(TYPE_SHORTCUT != mCurrentType){
							mOprateType = TYPE_SHORTCUT;
							if(null == mWonderfulVideoAdapter){
								getRecorderFileFromLocal(TYPE_SHORTCUT);
							}else{
								mCurrentType = mOprateType;
								updateButtonState(mCurrentType);
								mWonderfulVideoList.setVisibility(View.VISIBLE);
								mEmergencyVideoList.setVisibility(View.GONE);
								mLoopVideoList.setVisibility(View.GONE);
							}
						}
					}
				}
				
				break;
			case R.id.video_jjyx:
				if(!isGetFileListDataing){
					if(!isEditState){
						if(TYPE_URGENT != mCurrentType){
							mOprateType = TYPE_URGENT;
							if(null == mEmergencyVideoAdapter){
								getRecorderFileFromLocal(TYPE_URGENT);
							}else{
								mCurrentType = mOprateType;
								updateButtonState(mCurrentType);
								mWonderfulVideoList.setVisibility(View.GONE);
								mEmergencyVideoList.setVisibility(View.VISIBLE);
								mLoopVideoList.setVisibility(View.GONE);
							}
						}			
					}
				}
				
				break;
			case R.id.video_xhyx:
				if(!isGetFileListDataing){
					if(!isEditState){
						if(TYPE_CIRCULATE != mCurrentType){
							mOprateType = TYPE_CIRCULATE;
							if(null == mLoopVideoAdapter){
								getRecorderFileFromLocal(TYPE_CIRCULATE);
							}else{
								mCurrentType = mOprateType;
								updateButtonState(mCurrentType);
								mWonderfulVideoList.setVisibility(View.GONE);
								mEmergencyVideoList.setVisibility(View.GONE);
								mLoopVideoList.setVisibility(View.VISIBLE);
							}
						}
					}
				}
				
				break;
			case R.id.mEditBtn:
				if(!isEditState){
					mEditBtn.setText("取消");
					isEditState=true;
					selectedListData.clear();
					mFunctionLayout.setVisibility(View.VISIBLE);
				}else{
					mEditBtn.setText("编辑");
					isEditState=false;
					selectedListData.clear();
					updateDelandEditBg(false);//把下载和删除按钮的北京颜色还原回去
					mFunctionLayout.setVisibility(View.GONE);
				}
				
				if(TYPE_SHORTCUT == mCurrentType){
					mWonderfulVideoAdapter.notifyDataSetChanged();
				}else if(TYPE_URGENT == mCurrentType){
					mEmergencyVideoAdapter.notifyDataSetChanged();
				}else{
					mLoopVideoAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.mDownloadBtn:
				isEditState=false;
				mEditBtn.setText("编辑");
				mFunctionLayout.setVisibility(View.GONE);
				for(String filename : selectedListData){
					System.out.println("TTT======1111=filename="+filename);
					GolukApplication.getInstance().getIPCControlManager().downloadFile(filename, "", "fs1:/video/");
				}
				
				if(TYPE_SHORTCUT == mCurrentType){
					mWonderfulVideoAdapter.notifyDataSetChanged();
				}else if(TYPE_URGENT == mCurrentType){
					mEmergencyVideoAdapter.notifyDataSetChanged();
				}else{
					mLoopVideoAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.mDeleteBtn:
				isEditState=false;
				mEditBtn.setText("编辑");
				mFunctionLayout.setVisibility(View.GONE);
				for(String filename : selectedListData){
					GolukApplication.getInstance().getIPCControlManager().deleteFile(filename);
					
					
					if(TYPE_SHORTCUT == mCurrentType){
						for(VideoInfo info : mWonderfulVideoData){
							if(info.videoPath.equals(filename)){
								mWonderfulVideoData.remove(info);
								break;
							}
						}
					}else if(TYPE_URGENT == mCurrentType){
						for(VideoInfo info : mEmergencyVideoData){
							if(info.videoPath.equals(filename)){
								mEmergencyVideoData.remove(info);
								break;
							}
						}
					}else{
						for(VideoInfo info : mLoopVideoData){
							if(info.videoPath.equals(filename)){
								mLoopVideoData.remove(info);
								break;
							}
						}
					}
					
				}
				
				if(TYPE_SHORTCUT == mCurrentType){
					mWonderfulGroupName.clear();
					for (VideoInfo info : mWonderfulVideoData) {
						String name = info.videoCreateDate.substring(0, 10);
						if(name.length() >= 10){
							if(!mWonderfulGroupName.contains(name)){
								mWonderfulGroupName.add(name);
							}
						}
					}

//					wonderfulVideoData = videoInfo2Double(mWonderfulVideoData);
//					mWonderfulVideoAdapter.setData(wonderfulGroupName, wonderfulVideoData);
					mWonderfulVideoAdapter.notifyDataSetChanged();
				}else if(TYPE_URGENT == mCurrentType){
					mEmergencyGroupName.clear();
					for (VideoInfo info : mEmergencyVideoData) {
						String name = info.videoCreateDate.substring(0, 10);
						if(name.length() >= 10){
							if(!mEmergencyGroupName.contains(name)){
								mEmergencyGroupName.add(name);
							}
						}
					}

//					emergencyVideoData = videoInfo2Double(mEmergencyVideoData);
//					mEmergencyVideoAdapter.setData(emergencyGroupName, emergencyVideoData);
					mEmergencyVideoAdapter.notifyDataSetChanged();
				}else{
					mLoopGroupName.clear();
					for (VideoInfo info : mLoopVideoData) {
						String name = info.videoCreateDate.substring(0, 10);
						if(name.length() >= 10){
							if(!mLoopGroupName.contains(name)){
								mLoopGroupName.add(name);
							}
						}
					}

//					loopVideoData = videoInfo2Double(mLoopVideoData);
					mLoopVideoAdapter.setData(mLoopGroupName, mDoubleLoopVideoData);
					mLoopVideoAdapter.notifyDataSetChanged();
				}
				
				break;
				
	
			default:
				break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("filemanager");
		
		if(null != wonderfulVideoData){
			for(int i=0;i<wonderfulVideoData.size();i++){
//				DoubleVideoInfo info = wonderfulVideoData.get(i);
//				VideoInfo info1 = info.getVideoInfo1();
//				VideoInfo info2 = info.getVideoInfo2();
//				if(null != info1.videoBitmap){
//					if(!info1.videoBitmap.isRecycled()){
//						info1.videoBitmap.recycle();
//						info1.videoBitmap=null;
//					}
//				}
//				if(null != info2){
//					if(null != info2.videoBitmap){
//						if(!info2.videoBitmap.isRecycled()){
//							info2.videoBitmap.recycle();
//							info2.videoBitmap=null;
//						}
//					}
//				}
				
			}
		}
		
		if(null != emergencyVideoData){
			for(int i=0;i<emergencyVideoData.size();i++){
//				DoubleVideoInfo info = emergencyVideoData.get(i);
//				VideoInfo info1 = info.getVideoInfo1();
//				VideoInfo info2 = info.getVideoInfo2();
//				if(null != info1.videoBitmap){
//					if(!info1.videoBitmap.isRecycled()){
//						info1.videoBitmap.recycle();
//						info1.videoBitmap=null;
//					}
//				}
//				if(null != info2){
//					if(null != info2.videoBitmap){
//						if(!info2.videoBitmap.isRecycled()){
//							info2.videoBitmap.recycle();
//							info2.videoBitmap=null;
//						}
//					}
//				}
				
			}
		}
		
		if(null != mDoubleLoopVideoData){
			for(int i=0;i<mDoubleLoopVideoData.size();i++){
//				DoubleVideoInfo info = loopVideoData.get(i);
//				VideoInfo info1 = info.getVideoInfo1();
//				VideoInfo info2 = info.getVideoInfo2();
//				if(null != info1.videoBitmap){
//					if(!info1.videoBitmap.isRecycled()){
//						info1.videoBitmap.recycle();
//						info1.videoBitmap=null;
//					}
//				}
//				if(null != info2){
//					if(null != info2.videoBitmap){
//						if(!info2.videoBitmap.isRecycled()){
//							info2.videoBitmap.recycle();
//							info2.videoBitmap=null;
//						}
//					}
//				}
				
			}
		}
			
	}

	/**
	 * IPC视频文件信息转列表显示视频信息
	 * @param mVideoFileInfo IPC视频文件信息
	 * @return 列表显示视频信息
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private VideoInfo getVideoInfo(VideoFileInfo mVideoFileInfo) {
		VideoInfo info = new VideoInfo();
		// 文件选择状态
		info.isSelect = false;
		info.id = mVideoFileInfo.id;
		info.videoSize = Utils.getSizeShow(mVideoFileInfo.size);
		info.countTime = Utils.minutesTimeToString(mVideoFileInfo.period);
		if (1 == mVideoFileInfo.resolution) {
			info.videoHP = 1080;
		} else {
			info.videoHP = 720;
		}
		info.videoCreateDate = Utils.getTimeStr(mVideoFileInfo.time * 1000);
		 info.videoPath=mVideoFileInfo.location;

		String fileName = mVideoFileInfo.location;
		fileName = fileName.substring(0, fileName.length() - 4) + ".jpg";
		String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		GFileUtils.makedir(filePath);
		File file = new File(filePath + File.separator + fileName);
		if (file.exists()) {
			info.videoBitmap = ImageManager.getBitmapFromCache(filePath + File.separator + fileName, 194, 109);
		} else {
			GolukApplication.getInstance().getIPCControlManager().downloadFile(fileName, "" + mVideoFileInfo.id, FileUtils.javaToLibPath(filePath));
			System.out.println("TTT====111111=====filename="+fileName+"===tag="+mVideoFileInfo.id);
		}
		
		return info;
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
									for(int i=0; i<wonderfulVideoData.size(); i++){
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
										
									}
									
									mWonderfulVideoAdapter.notifyDataSetChanged();
								}
							}else if(TYPE_URGENT == mCurrentType){//紧急视频
								if (null != mEmergencyVideoAdapter) {
									for(int i=0; i<emergencyVideoData.size(); i++){
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
