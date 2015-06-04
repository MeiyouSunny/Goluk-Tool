package cn.com.mobnote.golukmobile.carrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.Utils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomProgressDialog;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

@SuppressLint("ClickableViewAccessibility")
public class IPCFileManagerActivity extends BaseActivity implements OnClickListener, IPCManagerFn, OnTouchListener{
	private StickyListHeadersListView mWonderfulVideoList=null;
	private StickyListHeadersListView mEmergencyVideoList=null;
	private StickyListHeadersListView mLoopVideoList=null;
	private IPCFileAdapter mWonderfulVideoAdapter=null;
	private IPCFileAdapter mEmergencyVideoAdapter=null;
	private IPCFileAdapter mLoopVideoAdapter=null;
	private List<VideoInfo> mWonderfulVideoData=null;
	private List<VideoInfo> mEmergencyVideoData=null;
	private List<VideoInfo> mLoopVideoData=null;
	
	private List<DoubleVideoInfo> wonderfulVideoData=null;
	private List<DoubleVideoInfo> emergencyVideoData=null;
	private List<DoubleVideoInfo> loopVideoData=null;
	private List<String> wonderfulGroupName=null;
	private List<String> emergencyGroupName=null;
	private List<String> loopGroupName=null;
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
	private int mOprateType = IPCManagerFn.TYPE_CIRCULATE;
	private int mCurrentType = IPCManagerFn.TYPE_CIRCULATE;
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
	private boolean addWonderfulEmptyFooter=false;
	private boolean addEmergencyEmptyFooter=false;
	private boolean addLoopEmptyFooter=false;
	private int cycleListTime = 0;//循环列表最后的时间戳
	private int marvellousListTime = 0;//精彩列表最后的时间戳
	private int emergencyListTime = 0;//紧急列表最后的时间戳
	/** 添加列表底部加载中布局 */
	private RelativeLayout wonderfulLoading;
	private RelativeLayout emergencyLoading;
	private RelativeLayout loopLoading;
	private boolean ishaveData = false;
	private boolean isbelow = true;
	private CustomLoadingDialog mCustomProgressDialog=null;
	private int timeend = 2147483647;
	private boolean isShowPlayer=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_videolist);
		mCustomProgressDialog = new CustomLoadingDialog(this,null);
		// 注册回调监听
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("filemanager",this);
		}
				
		initView();
		setListener();
		
		getRecorderFileFromLocal(true, IPCManagerFn.TYPE_CIRCULATE, timeend);//初始化
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
				if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
					if(!wonderfulGroupName.contains(name.substring(0, 10))){
						wonderfulGroupName.add(name.substring(0, 10));
					}
				}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
					if(!emergencyGroupName.contains(name.substring(0, 10))){
						emergencyGroupName.add(name.substring(0, 10));
					}
				}else{
					if(!loopGroupName.contains(name.substring(0, 10))){
						loopGroupName.add(name.substring(0, 10));
					}
				}
			}
		}
		
		for (VideoFileInfo info : fileList) {
			if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
				mWonderfulVideoData.add(getVideoInfo(info));
			}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
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
	private void initWonderfulLayout(ArrayList<VideoFileInfo> fileList){
		if(!addWonderfulFooter){
			addWonderfulFooter=true;
			wonderfulLoading = (RelativeLayout) LayoutInflater.from(this)
					.inflate(R.layout.video_square_below_loading, null);
			mWonderfulVideoList.addFooterView(wonderfulLoading);
		}

		if(fileList.size() < pageCount){
			if(addWonderfulFooter){
				addWonderfulFooter=false;
				mWonderfulVideoList.removeFooterView(wonderfulLoading);
				
				if(!addWonderfulEmptyFooter){
					addWonderfulEmptyFooter=true;
					LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null); 
					mWonderfulVideoList.addFooterView(layout);
				}
			}
		}
		
		updateButtonState(IPCManagerFn.TYPE_SHORTCUT);
		mWonderfulVideoList.setVisibility(View.VISIBLE);
		mEmergencyVideoList.setVisibility(View.GONE);
		mLoopVideoList.setVisibility(View.GONE);
		
		wonderfulGroupName.clear();
		wonderfulVideoData.clear();
		
		getGormattedData(fileList);
		wonderfulVideoData = videoInfo2Double(mWonderfulVideoData);
		
		
		if(null == mWonderfulVideoAdapter){
			mWonderfulVideoAdapter = new IPCFileAdapter(this);
		}
		mWonderfulVideoAdapter.setData(wonderfulGroupName, wonderfulVideoData);
		
		if(mWonderfulVideoData.size() <= 40){
			mWonderfulVideoList.setAdapter(mWonderfulVideoAdapter);
		}
		mWonderfulVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mWonderfulVideoList.getAdapter().getCount() == (wonderfulFirstVisible+wonderfulVisibleCount)){
						GolukDebugUtils.e("xuhw", "fuckingAction===="+marvellousListTime);
						if(mWonderfulVideoData.size() > 0 &&(mWonderfulVideoData.size()%pageCount) == 0){
							getRecorderFileFromLocal(false, IPCManagerFn.TYPE_SHORTCUT,marvellousListTime);//初始化
						}
//						Toast.makeText(IPCFileManagerActivity.this, "滑动到最后了222", 1000).show();
						GolukDebugUtils.e("xuhw", "TTTTT=====滑动到最后了222 最后时间"+marvellousListTime);
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
								if(selectedListData.contains(tag1)){
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
						if(!GolukApplication.getInstance().getIpcIsLogin()){
							dialog();
							return;
						}
						
						
						Message msg = mHandler.obtainMessage(1);
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							if(!TextUtils.isEmpty(tag1)){
								msg.obj = tag1;
								mHandler.removeMessages(1);
								mHandler.sendMessage(msg);
							}
						}else{
							if(!TextUtils.isEmpty(tag2)){
								msg.obj = tag2;
								mHandler.removeMessages(1);
								mHandler.sendMessage(msg);
							}
						}
						
					}
				}
				 
				}
			});
		
	}
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				mHandler.removeMessages(1);
				playerVideo((String)msg.obj);
				break;

			default:
				break;
			}
		};
	};
	
	/**
	 * 播放IPC视频
	 * @param filename 视频名称
	 * @author xuhw
	 * @date 2015年4月30日
	 */
	private void playerVideo(String filename){
		if(!isShowPlayer){
			isShowPlayer=true;
			if(null == VideoPlayerActivity.mHandler){
				Intent intent = new Intent(IPCFileManagerActivity.this, VideoPlayerActivity.class);
				intent.putExtra("from", "ipc");
				intent.putExtra("type", mCurrentType);
				intent.putExtra("filename", filename);
				startActivity(intent);
			}
		}
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
			
//			if(IPCManagerFn.TYPE_CIRCULATE == mCurrentType){
//				findViewById(R.id.video_download_img).setBackgroundResource(R.drawable.carrecorder_icon_download_grey);
//				return;
//			}
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
	private void initEmergencyLayout(ArrayList<VideoFileInfo> fileList){
		if(!addEmergencyFooter){
			addEmergencyFooter=true;
			emergencyLoading = (RelativeLayout) LayoutInflater.from(this)
					.inflate(R.layout.video_square_below_loading, null);
			mEmergencyVideoList.addFooterView(emergencyLoading);
		}
		
		if(fileList.size() < pageCount){
			if(addEmergencyFooter){
				addEmergencyFooter=false;
				mEmergencyVideoList.removeFooterView(emergencyLoading);
				
				if(!addEmergencyEmptyFooter){
					addEmergencyEmptyFooter=true;
					LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null); 
					mEmergencyVideoList.addFooterView(layout);
				}
			}
		}
		
		
		updateButtonState(IPCManagerFn.TYPE_URGENT);
		mWonderfulVideoList.setVisibility(View.GONE);
		mEmergencyVideoList.setVisibility(View.VISIBLE);
		mLoopVideoList.setVisibility(View.GONE);
		
		emergencyGroupName.clear();
		emergencyVideoData.clear();
		
		getGormattedData(fileList);
		emergencyVideoData = videoInfo2Double(mEmergencyVideoData);
		
		if(null == mEmergencyVideoAdapter){
			mEmergencyVideoAdapter = new IPCFileAdapter(this);
		}
		mEmergencyVideoAdapter.setData(emergencyGroupName, emergencyVideoData);
		
		if(mEmergencyVideoData.size() <= 40){
			mEmergencyVideoList.setAdapter(mEmergencyVideoAdapter);
		}
		
		mEmergencyVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mEmergencyVideoList.getAdapter().getCount() == (emergencyFirstVisible + emergencyVisibleCount)){
						if(mEmergencyVideoData.size() > 0 && (mEmergencyVideoData.size()%pageCount) == 0){
							getRecorderFileFromLocal(false, IPCManagerFn.TYPE_URGENT,emergencyListTime);//初始化
						}
//						Toast.makeText(IPCFileManagerActivity.this, "滑动到最后了222", 1000).show();
						GolukDebugUtils.e("xuhw", "TTTTT=====滑动到最后了222 最后时间"+emergencyListTime);
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
						if(!GolukApplication.getInstance().getIpcIsLogin()){
							dialog();
							return;
						}
					
						Message msg = mHandler.obtainMessage(1);
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))){
							if(!TextUtils.isEmpty(tag1)){
								msg.obj = tag1;
								mHandler.removeMessages(1);
								mHandler.sendMessage(msg);
							}
						}else{
							if(!TextUtils.isEmpty(tag2)){
								msg.obj = tag2;
								mHandler.removeMessages(1);
								mHandler.sendMessage(msg);
							}
						}
						
					
					}
				
				
				
				
				}
				
			}
		});

	}
	
	/**
	 * 初始化循环视频
	 * @param fileList
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void initLoopLayout(ArrayList<VideoFileInfo> fileList){
		if(!addLoopFooter){
			addLoopFooter=true;
			loopLoading = (RelativeLayout) LayoutInflater.from(this)
					.inflate(R.layout.video_square_below_loading, null);
			mLoopVideoList.addFooterView(loopLoading);
		}
		
		if(fileList.size() < pageCount){
			if(addLoopFooter){
				addLoopFooter=false;
				mLoopVideoList.removeFooterView(loopLoading);
				
				if(!addLoopEmptyFooter){
					addLoopEmptyFooter=true;
					LinearLayout layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.carrecorder_videolist_footer, null); 
					mLoopVideoList.addFooterView(layout);
				}
			}
		}
		
		updateButtonState(IPCManagerFn.TYPE_CIRCULATE);
		mWonderfulVideoList.setVisibility(View.GONE);
		mEmergencyVideoList.setVisibility(View.GONE);
		mLoopVideoList.setVisibility(View.VISIBLE);
		
		loopGroupName.clear();
		loopVideoData.clear();
		
		getGormattedData(fileList);
		loopVideoData = videoInfo2Double(mLoopVideoData);
		
		if(null == mLoopVideoAdapter){
			mLoopVideoAdapter = new IPCFileAdapter(this);
		}
		mLoopVideoAdapter.setData(loopGroupName, loopVideoData);
		
		mLoopVideoAdapter.notifyDataSetChanged();

		if(mLoopVideoData.size() <= 40){
			mLoopVideoList.setAdapter(mLoopVideoAdapter);
		}
		
		mLoopVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mLoopVideoList.getAdapter().getCount() == (loopFirstVisible + loopVisibleCount)){
						if(mLoopVideoData.size() > 0 &&(mLoopVideoData.size()%pageCount) == 0){
							getRecorderFileFromLocal(false, IPCManagerFn.TYPE_CIRCULATE,cycleListTime);//初始化
						}
//						Toast.makeText(IPCFileManagerActivity.this, "循环视频　滑动到最后了222", 1000).show();
						GolukDebugUtils.e("xuhw", "TTTTT=====滑动到最后了222 endtime="+cycleListTime);
					}
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				loopFirstVisible=firstVisibleItem;
				loopVisibleCount=visibleItemCount;
			}
		});
		
		
		mLoopVideoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(arg2 < loopVideoData.size()){
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
					if(!GolukApplication.getInstance().getIpcIsLogin()){
						dialog();
						return;
					}
					
					Message msg = mHandler.obtainMessage(1);
					//点击播放
					if((screenX > 0) && (screenX < (screenWidth/2))){
						if(!TextUtils.isEmpty(tag1)){
							msg.obj = tag1;
							mHandler.removeMessages(1);
							mHandler.sendMessage(msg);
						}
					}else{
						if(!TextUtils.isEmpty(tag2)){
							msg.obj = tag2;
							mHandler.removeMessages(1);
							mHandler.sendMessage(msg);
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
			case IPCManagerFn.TYPE_SHORTCUT:
				mWonderfulVideoLine.setVisibility(View.VISIBLE);
				mWonderfulVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				break;
			case IPCManagerFn.TYPE_URGENT:
				mEmergencyVideoLine.setVisibility(View.VISIBLE);
				mEmergencyVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				break;
			case IPCManagerFn.TYPE_CIRCULATE:
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
	private void getRecorderFileFromLocal(boolean flag, int type, int timeend) {
		if(flag){
			if(!mCustomProgressDialog.isShowing()){
				mCustomProgressDialog.show();
			}
		}
		isGetFileListDataing=true;
		mOprateType = type;
		updateButtonState(type);
		GolukDebugUtils.e("xuhw", "YYYYYY=====queryFileListInfo===timeend="+timeend);
		boolean isSucess = GolukApplication.getInstance().getIPCControlManager().queryFileListInfo(type, pageCount, 0, timeend);
		if(!isSucess){
			isGetFileListDataing=false;
		}
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void initView(){
		mBackBtn = (ImageButton)findViewById(R.id.back_btn);
		mWonderfulVideoList = (StickyListHeadersListView) findViewById(R.id.mWonderfulVideoList);
		mEmergencyVideoList = (StickyListHeadersListView) findViewById(R.id.mEmergencyVideoList);
		mLoopVideoList = (StickyListHeadersListView) findViewById(R.id.mLoopVideoList);
		mWonderfulVideoBtn = (Button)findViewById(R.id.video_jcsp);
		mEmergencyVideoBtn = (Button)findViewById(R.id.video_jjyx);
		mLoopVideoBtn = (Button)findViewById(R.id.video_xhyx);
		mWonderfulVideoLine = (ImageView)findViewById(R.id.line_jcsp);
		mEmergencyVideoLine = (ImageView)findViewById(R.id.line_jjyx);
		mLoopVideoLine = (ImageView)findViewById(R.id.line_xhyx);
		
		mEditBtn = (Button)findViewById(R.id.mEditBtn);
		mFunctionLayout = (LinearLayout)findViewById(R.id.mFunctionLayout);
		mDownloadBtn = (LinearLayout)findViewById(R.id.mDownloadBtn);
		mDeleteBtn = (LinearLayout)findViewById(R.id.mDeleteBtn);
		
		 
		
		
		
		wonderfulVideoData = new ArrayList<DoubleVideoInfo>();
		emergencyVideoData = new ArrayList<DoubleVideoInfo>();
		loopVideoData = new ArrayList<DoubleVideoInfo>();
		wonderfulGroupName = new ArrayList<String>();
		emergencyGroupName = new ArrayList<String>();
		loopGroupName = new ArrayList<String>();
		
		mWonderfulVideoData=new ArrayList<VideoInfo>();
		mEmergencyVideoData=new ArrayList<VideoInfo>();
		mLoopVideoData=new ArrayList<VideoInfo>();
		selectedListData=new ArrayList<String>();
	}
	
	/**
	 * 设置控件监听事件
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void setListener(){
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

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		screenX = arg1.getX();
		GolukDebugUtils.e("xuhw", "QQQ===============screenX="+screenX);
		return false;
	}
	
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.back_btn:
				exit();
				break;
			case R.id.video_jcsp:
				if(!isGetFileListDataing){
					if(!isEditState){
						if(IPCManagerFn.TYPE_SHORTCUT != mCurrentType){
							mOprateType = IPCManagerFn.TYPE_SHORTCUT;
							if(0 == mWonderfulVideoData.size()){
								getRecorderFileFromLocal(true, IPCManagerFn.TYPE_SHORTCUT, timeend);
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
						if(IPCManagerFn.TYPE_URGENT != mCurrentType){
							mOprateType = IPCManagerFn.TYPE_URGENT;
							if(0 == mEmergencyVideoData.size()){
								getRecorderFileFromLocal(true, IPCManagerFn.TYPE_URGENT, timeend);
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
						if(IPCManagerFn.TYPE_CIRCULATE != mCurrentType){
							mOprateType = IPCManagerFn.TYPE_CIRCULATE;
							if(0 == mLoopVideoData.size()){
								getRecorderFileFromLocal(true, IPCManagerFn.TYPE_CIRCULATE, timeend);
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
				if(!isGetFileListDataing){
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
					
					if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
						if(null != mWonderfulVideoAdapter){
							mWonderfulVideoAdapter.notifyDataSetChanged();
						}
					}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
						if(null != mEmergencyVideoAdapter){
							mEmergencyVideoAdapter.notifyDataSetChanged();
						}
					}else{
						if(null != mLoopVideoAdapter){
							mLoopVideoAdapter.notifyDataSetChanged();
						}
					}
				}
				break;
			case R.id.mDownloadBtn:
//				if(IPCManagerFn.TYPE_CIRCULATE == mCurrentType){
//					return;
//				}
				
				isEditState=false;
				mEditBtn.setText("编辑");
				mFunctionLayout.setVisibility(View.GONE);
				for(String filename : selectedListData){
					GolukDebugUtils.e("xuhw", "TTT======1111=filename="+filename);
					String videoSavePath="fs1:/video/";
					if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
						videoSavePath="fs1:/video/wonderful/";
					}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
						videoSavePath="fs1:/video/urgent/";
					}else{
						videoSavePath="fs1:/video/loop/";
					}
					
					if(filename.length() > 10){
						String fileName = filename.substring(0, filename.length() - 4) + ".jpg";
						String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
						File file = new File(filePath + File.separator + fileName);
						if (!file.exists()) {
							GolukApplication.getInstance().getIPCControlManager().downloadFile(fileName, "download", FileUtils.javaToLibPath(filePath), findtime(fileName));
						}
					}
					
					String mp4 = FileUtils.libToJavaPath(videoSavePath+filename);
					File file = new File(mp4);
					if(!file.exists()){
//						GolukApplication.getInstance().getIPCControlManager().downloadFile(filename, "videodownload", videoSavePath, findtime(filename));
						boolean a = GolukApplication.getInstance().getIPCControlManager().querySingleFile(filename);
						GFileUtils.writeIPCLog("YYYYYY===a="+a+"==querySingleFile======filename="+filename);
						GolukDebugUtils.e("xuhw", "YYYYYY===a="+a+"==querySingleFile======filename="+filename);
					}else{
							
					}
					
				}
				
				selectedListData.clear();
				if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
					if(null != mWonderfulVideoAdapter){
						mWonderfulVideoAdapter.notifyDataSetChanged();
					}
				}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
					if(null != mEmergencyVideoAdapter){
						mEmergencyVideoAdapter.notifyDataSetChanged();
					}
				}else{
					if(null != mLoopVideoAdapter){
						mLoopVideoAdapter.notifyDataSetChanged();
					}
				}
				break;
			case R.id.mDeleteBtn:
				isEditState=false;
				mEditBtn.setText("编辑");
				mFunctionLayout.setVisibility(View.GONE);
				for(String filename : selectedListData){
					GolukApplication.getInstance().getIPCControlManager().deleteFile(filename);
					
					
					if(filename.length() > 10){
						String fileName = filename.substring(0, filename.length() - 4) + ".jpg";
						String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
						File file = new File(filePath + File.separator + fileName);
						if (file.exists()) {
							file.delete();
						}
					}
					
					
					if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
						for(VideoInfo info : mWonderfulVideoData){
							if(info.videoPath.equals(filename)){
								mWonderfulVideoData.remove(info);
								break;
							}
						}
					}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
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
				
				selectedListData.clear();
				if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
					wonderfulGroupName.clear();
					for (VideoInfo info : mWonderfulVideoData) {
						String name = info.videoCreateDate.substring(0, 10);
						if(name.length() >= 10){
							if(!wonderfulGroupName.contains(name)){
								wonderfulGroupName.add(name);
							}
						}
					}

					wonderfulVideoData = videoInfo2Double(mWonderfulVideoData);
					if(null != mWonderfulVideoAdapter){
						mWonderfulVideoAdapter.setData(wonderfulGroupName, wonderfulVideoData);
						mWonderfulVideoAdapter.notifyDataSetChanged();
					}
				}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
					emergencyGroupName.clear();
					for (VideoInfo info : mEmergencyVideoData) {
						String name = info.videoCreateDate.substring(0, 10);
						if(name.length() >= 10){
							if(!emergencyGroupName.contains(name)){
								emergencyGroupName.add(name);
							}
						}
					}

					emergencyVideoData = videoInfo2Double(mEmergencyVideoData);
					if(null != mEmergencyVideoAdapter){
						mEmergencyVideoAdapter.setData(emergencyGroupName, emergencyVideoData);
						mEmergencyVideoAdapter.notifyDataSetChanged();
					}
				}else{
					loopGroupName.clear();
					for (VideoInfo info : mLoopVideoData) {
						String name = info.videoCreateDate.substring(0, 10);
						if(name.length() >= 10){
							if(!loopGroupName.contains(name)){
								loopGroupName.add(name);
							}
						}
					}

					loopVideoData = videoInfo2Double(mLoopVideoData);
					if(null != mLoopVideoAdapter){
						mLoopVideoAdapter.setData(loopGroupName, loopVideoData);
						mLoopVideoAdapter.notifyDataSetChanged();
					}
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
	
	/**
	 * 查询文件录制起始时间
	 * @param filename　文件名
	 * @return 文件录制起始时间
	 * @author xuhw
	 * @date 2015年5月5日
	 */
	private long findtime(String filename){
		long time=0;
		List<VideoInfo> datalist = null;
		if(filename.contains("WND")){
			datalist = mWonderfulVideoData;
		}else if(filename.contains("URG")){
			datalist = mEmergencyVideoData;
		}else if(filename.contains("NRM")){
			datalist = mLoopVideoData;
		}
		
		if(null != datalist){
			for(int i=0;i<datalist.size();i++){
				if(filename.equals(datalist.get(i).videoPath)){
					return datalist.get(i).time;
				}
			}
		}
		
		return time;
	}
	
	/**
	 * 退出
	 * @author xuhw
	 * @date 2015年5月4日
	 */
	public void exit(){
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("filemanager");
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
		if(null != wonderfulVideoData){
			for(int i=0;i<wonderfulVideoData.size();i++){
				DoubleVideoInfo info = wonderfulVideoData.get(i);
				VideoInfo info1 = info.getVideoInfo1();
				VideoInfo info2 = info.getVideoInfo2();
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
		
		if(null != emergencyVideoData){
			for(int i=0;i<emergencyVideoData.size();i++){
				DoubleVideoInfo info = emergencyVideoData.get(i);
				VideoInfo info1 = info.getVideoInfo1();
				VideoInfo info2 = info.getVideoInfo2();
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
		
		if(null != loopVideoData){
			for(int i=0;i<loopVideoData.size();i++){
				DoubleVideoInfo info = loopVideoData.get(i);
				VideoInfo info1 = info.getVideoInfo1();
				VideoInfo info2 = info.getVideoInfo2();
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
		info.videoHP = mVideoFileInfo.resolution;
		info.videoCreateDate = Utils.getTimeStr(mVideoFileInfo.time * 1000);
		 info.videoPath=mVideoFileInfo.location;
		 info.time=mVideoFileInfo.time;
		
		String fileName = mVideoFileInfo.location;
		fileName = fileName.substring(0, fileName.length() - 4) + ".jpg";
		String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		GFileUtils.makedir(filePath);
		File file = new File(filePath + File.separator + fileName);
		if (file.exists()) {
			info.videoBitmap = ImageManager.getBitmapFromCache(filePath + File.separator + fileName, 194, 109);
		} else {
			 if(1 == mVideoFileInfo.withSnapshot){
				 GolukApplication.getInstance().getIPCControlManager().downloadFile(fileName, "IPC_IMAGE" + mVideoFileInfo.id, FileUtils.javaToLibPath(filePath), mVideoFileInfo.time);
				 GolukDebugUtils.e("xuhw", "TTT====111111=====filename="+fileName+"===tag="+mVideoFileInfo.id);
			 }
		}
		
		return info;
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		switch (event) {
		case ENetTransEvent_IPC_VDCP_CommandResp:
			if (IPC_VDCP_Msg_Query == msg) {
				if(mCustomProgressDialog.isShowing()){
					mCustomProgressDialog.close();
				}
				isGetFileListDataing=false;
				GolukDebugUtils.e("xuhw","YYYYYY=======获取文件列表===@@@======param1="+ param1 + "=====param2=" + param2);
//				GFileUtils.writeIPCLog("===========获取文件列表===3333=============param1="+ param1 + "=====param2=" + param2);
				if (RESULE_SUCESS == param1) {
					if(TextUtils.isEmpty((String)param2)){
						return;
					}
					ArrayList<VideoFileInfo> fileList = IpcDataParser.parseMoreFile((String) param2);
					int total = IpcDataParser.getFileListCount((String) param2);
					if (null != fileList) {
						VideoFileInfo vfi=null;
						if(fileList.size() > 0){
							vfi = fileList.get(fileList.size() - 1);
						}
						
//						GFileUtils.writeIPCLog("===========获取文件列表===44444============get data success=========");
						if(fileList.size()<pageCount){
							ishaveData = false;
						}else{
							ishaveData = true;
						}
						mCurrentType = mOprateType;
						if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){//精彩视频
							if(null != vfi){
								marvellousListTime = (int) vfi.time - 1;
							}
							wonderfulTotalCount = total;
							initWonderfulLayout(fileList);
						}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){//紧急视频
							if(null != vfi){
								emergencyListTime = (int) vfi.time - 1;						
							}
							emergencyTotalCount = total;
							initEmergencyLayout(fileList);
						}else{//循环视频
							if(null != vfi){
								cycleListTime = (int) vfi.time - 1;
							}
							loopVisibleCount = total;
							initLoopLayout(fileList);
						}
					} else {
						// 列表数据空
//						GFileUtils.writeIPCLog("===========获取文件列表===5555============ data null=========");
						ishaveData = false;
					}
				} else {
					// 命令发送失败
//					GFileUtils
//							.writeIPCLog("===========获取文件列表===6666============  not success =========");
				}
			}else if(IPC_VDCPCmd_TriggerRecord == msg){
//				GFileUtils
//				.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord==========222222222222222222 =========");
			//文件删除
			}else if(IPC_VDCPCmd_Erase == msg){
				GolukDebugUtils.e("xuhw", "QQQ==========param1="+param1+"===param2="+param2);
			}
			break;
		// IPC下载结果应答
		case ENetTransEvent_IPC_VDTP_Resp:
//			GFileUtils
//					.writeIPCLog("===========下载文件===2222222=============param1="
//							+ param1 + "=====param2=" + param2);
			// 文件传输消息
			if (IPC_VDTP_Msg_File == msg) {
				// 文件下载成功
				if (RESULE_SUCESS == param1) {
					if(TextUtils.isEmpty((String)(param2))){
						return;
					}
					try {
						JSONObject json = new JSONObject((String) param2);
						if (null != json) {
							String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
							String filename = json.optString("filename");
							String tag = json.optString("tag");
							GolukDebugUtils.e("xuhw", "TTT=======1111111==================tag="+tag);
							if(tag.contains("IPC_IMAGE")){
							if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){//精彩视频
								if (null != mWonderfulVideoAdapter) {
									for(int i=0; i<wonderfulVideoData.size(); i++){
										DoubleVideoInfo info =  wonderfulVideoData.get(i);
										String id1 = "IPC_IMAGE"+info.getVideoInfo1().id;
										GolukDebugUtils.e("xuhw", "TTT=======222222==================id1="+id1);
										if (tag.equals(id1)) {
											wonderfulVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
													.getBitmapFromCache(filePath
															+ File.separator
															+ filename, 194, 109);
										}
										
										if(null != info.getVideoInfo2()){
											String id2 = "IPC_IMAGE"+info.getVideoInfo2().id;
											if(!TextUtils.isEmpty(id2)){
												if(tag.equals(id2)){
													GolukDebugUtils.e("xuhw", "TTT===wonderful=4444=====filename="+filename+"===tag="+tag);
													wonderfulVideoData.get(i).getVideoInfo2().videoBitmap = ImageManager
															.getBitmapFromCache(filePath
																	+ File.separator
																	+ filename, 194, 109);
												}
											}
										}
										
									}
									
									mWonderfulVideoAdapter.notifyDataSetChanged();
								}
							}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){//紧急视频
								if (null != mEmergencyVideoAdapter) {
									for(int i=0; i<emergencyVideoData.size(); i++){
										DoubleVideoInfo info =  emergencyVideoData.get(i);
										String id1 = "IPC_IMAGE"+info.getVideoInfo1().id;
										if (tag.equals(id1)) {
											GolukDebugUtils.e("xuhw", "TTT==emergency==3333=====filename="+filename+"===tag="+tag);
											emergencyVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
													.getBitmapFromCache(filePath
															+ File.separator
															+ filename, 194, 109);
										}
										
										if(null != info.getVideoInfo2()){
											String id2 = "IPC_IMAGE"+info.getVideoInfo2().id;
											if(!TextUtils.isEmpty(id2)){
												if(tag.equals(id2)){
													GolukDebugUtils.e("xuhw", "TTT==emergency==4444=====filename="+filename+"===tag="+tag);
													emergencyVideoData.get(i).getVideoInfo2().videoBitmap = ImageManager
															.getBitmapFromCache(filePath
																	+ File.separator
																	+ filename, 194, 109);
												}
											}
										}
										
									}
									
									mEmergencyVideoAdapter.notifyDataSetChanged();
								}
							}else{//循环视频
								if (null != mLoopVideoAdapter) {
									for(int i=0; i<loopVideoData.size(); i++){
										DoubleVideoInfo info =  loopVideoData.get(i);
										String id1 = "IPC_IMAGE"+info.getVideoInfo1().id;
										if (tag.equals(id1)) {
											GolukDebugUtils.e("xuhw", "TTT==loop==3333=====filename="+filename+"===tag="+tag);
											loopVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
													.getBitmapFromCache(filePath
															+ File.separator
															+ filename, 194, 109);
										}
										
										if(null != info.getVideoInfo2()){
											String id2 = "IPC_IMAGE"+info.getVideoInfo2().id;
											if(!TextUtils.isEmpty(id2)){
												if(tag.equals(id2)){
													GolukDebugUtils.e("xuhw", "TTT===loop=4444=====filename="+filename+"===tag="+tag);
													loopVideoData.get(i).getVideoInfo2().videoBitmap = ImageManager
															.getBitmapFromCache(filePath
																	+ File.separator
																	+ filename, 194, 109);
												}
											}
										}
										
									}
									
									mLoopVideoAdapter.notifyDataSetChanged();
								}
							}
							}else{
								GolukDebugUtils.e("xuhw", "TTT======no filelist  file======filename="+filename);
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
	
	@Override
	protected void onResume() {
		super.onResume();
		isShowPlayer=false;
		GolukApplication.getInstance().setContext(this, "ipcfilemanager");
	}
	
	/**
	 * 摄像头未连接提示
	 * 
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	private void dialog() {
		CustomDialog d = new CustomDialog(this);
		d.setMessage("请检查摄像头是否正常连接", Gravity.CENTER);
		d.setLeftButton("确定", null);
		d.show();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		exit(); 
        	return true;
        }else
        	return super.onKeyDown(keyCode, event); 
	}

}
