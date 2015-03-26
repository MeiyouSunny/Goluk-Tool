package cn.com.mobnote.golukmobile.carrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.tachograph.comm.IPCManagerFn;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
public class IPCFileManagerActivity extends Activity implements OnClickListener, IPCManagerFn, OnTouchListener{
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
	private Button mBackBtn=null;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_videolist);
		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("filemanager",this);
				
		initView();
		setListener();
		
		getRecorderFileFromLocal(TYPE_SHORTCUT);
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
					if(!wonderfulGroupName.contains(name.substring(0, 10))){
						wonderfulGroupName.add(name.substring(0, 10));
					}
				}else if(TYPE_URGENT == mCurrentType){
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
	private void initWonderfulLayout(ArrayList<VideoFileInfo> fileList){
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
		mWonderfulVideoList.setAdapter(mWonderfulVideoAdapter);
		
		mWonderfulVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mWonderfulVideoList.getAdapter().getCount() == (wonderfulFirstVisible+wonderfulVisibleCount)){
						
						Toast.makeText(IPCFileManagerActivity.this, "滑动到最后了222", 1000).show();
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
				Toast.makeText(IPCFileManagerActivity.this, ""+arg2+"==精彩视频===x = "+screenX, 1000).show();
			}
		});
		
	}
	
	/**
	 * 初始化紧急视频列表
	 * @param fileList
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void initEmergencyLayout(ArrayList<VideoFileInfo> fileList){
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
		mEmergencyVideoList.setAdapter(mEmergencyVideoAdapter);
		
		mEmergencyVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mEmergencyVideoList.getAdapter().getCount() == (emergencyFirstVisible + emergencyVisibleCount)){
						
						Toast.makeText(IPCFileManagerActivity.this, "滑动到最后了222", 1000).show();
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
				Toast.makeText(IPCFileManagerActivity.this, ""+arg2+"==紧急视频===x = "+screenX, 1000).show();
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
		mLoopVideoList.setAdapter(mLoopVideoAdapter);
		
		mLoopVideoList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mLoopVideoList.getAdapter().getCount() == (loopFirstVisible + loopVisibleCount)){
						
						Toast.makeText(IPCFileManagerActivity.this, "循环视频　滑动到最后了222", 1000).show();
						System.out.println("TTTTT=====滑动到最后了222");
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
				Toast.makeText(IPCFileManagerActivity.this, ""+arg2+"==循环视频===x = "+screenX, 1000).show();
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
		mWonderfulVideoLine.setBackgroundColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mEmergencyVideoLine.setBackgroundColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		mLoopVideoLine.setBackgroundColor(getResources().getColor(R.color.carrecorder_tab_nor_color));
		
		switch (type) {
			case TYPE_SHORTCUT:
				mWonderfulVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				mWonderfulVideoLine.setBackgroundColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				break;
			case TYPE_URGENT:
				mEmergencyVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				mEmergencyVideoLine.setBackgroundColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				break;
			case TYPE_CIRCULATE:
				mLoopVideoBtn.setTextColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
				mLoopVideoLine.setBackgroundColor(getResources().getColor(R.color.carrecorder_tab_sel_color));
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
		mOprateType = type;
		updateButtonState(type);
		boolean isSucess = GolukApplication.getInstance().getIPCControlManager().queryFileListInfo(type, 20, 0);
		GFileUtils.writeIPCLog("===========获取文件列表===1111===================isSucess=="+isSucess);
	}
	
	/**
	 * 初始化控件
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	private void initView(){
		mBackBtn = (Button)findViewById(R.id.back_btn);
		mWonderfulVideoList = (StickyListHeadersListView) findViewById(R.id.mWonderfulVideoList);
		mEmergencyVideoList = (StickyListHeadersListView) findViewById(R.id.mEmergencyVideoList);
		mLoopVideoList = (StickyListHeadersListView) findViewById(R.id.mLoopVideoList);
		mWonderfulVideoBtn = (Button)findViewById(R.id.video_jcsp);
		mEmergencyVideoBtn = (Button)findViewById(R.id.video_jjyx);
		mLoopVideoBtn = (Button)findViewById(R.id.video_xhyx);
		mWonderfulVideoLine = (ImageView)findViewById(R.id.line_jcsp);
		mEmergencyVideoLine = (ImageView)findViewById(R.id.line_jjyx);
		mLoopVideoLine = (ImageView)findViewById(R.id.line_xhyx);
		
		wonderfulVideoData = new ArrayList<DoubleVideoInfo>();
		emergencyVideoData = new ArrayList<DoubleVideoInfo>();
		loopVideoData = new ArrayList<DoubleVideoInfo>();
		wonderfulGroupName = new ArrayList<String>();
		emergencyGroupName = new ArrayList<String>();
		loopGroupName = new ArrayList<String>();
		
		mWonderfulVideoData=new ArrayList<VideoInfo>();
		mEmergencyVideoData=new ArrayList<VideoInfo>();
		mLoopVideoData=new ArrayList<VideoInfo>();
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
		
	}

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
				break;
			case R.id.video_jjyx:
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
				break;
			case R.id.video_xhyx:
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
				break;
	
			default:
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("filemanager");
		
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
			GolukApplication.getInstance().getIPCControlManager().downloadFile(fileName, "" + mVideoFileInfo.id, filePath);
			System.out.println("TTT====111111=====filename="+fileName+"===tag="+mVideoFileInfo.id);
		}
		
		return info;
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		switch (event) {
		case ENetTransEvent_IPC_VDCP_CommandResp:
			if (IPC_VDCP_Msg_Query == msg) {
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
							initWonderfulLayout(fileList);
						}else if(TYPE_URGENT == mOprateType){//紧急视频
							emergencyTotalCount = total;
							initEmergencyLayout(fileList);
						}else{//循环视频
							loopVisibleCount = total;
							initLoopLayout(fileList);
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
							String tag = json.optString("tag");
		System.out.println("TTT=====22222====filename="+filename+"===tag="+tag);
							if(TYPE_SHORTCUT == mCurrentType){//精彩视频
								if (null != mWonderfulVideoAdapter) {
									for(int i=0; i<wonderfulVideoData.size(); i++){
										DoubleVideoInfo info =  wonderfulVideoData.get(i);
										String id1 = info.getVideoInfo1().id + "";
										if (tag.equals(id1)) {
											System.out.println("TTT===wonderful=3333=====filename="+filename+"===tag="+tag);
											wonderfulVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
													.getBitmapFromCache(filePath
															+ File.separator
															+ filename, 194, 109);
										}
										
										if(null != info.getVideoInfo2()){
											String id2 = info.getVideoInfo2().id + "";
											if(!TextUtils.isEmpty(id2)){
												if(tag.equals(id2)){
													System.out.println("TTT===wonderful=4444=====filename="+filename+"===tag="+tag);
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
							}else if(TYPE_URGENT == mCurrentType){//紧急视频
								if (null != mEmergencyVideoAdapter) {
									for(int i=0; i<emergencyVideoData.size(); i++){
										DoubleVideoInfo info =  emergencyVideoData.get(i);
										String id1 = info.getVideoInfo1().id + "";
										if (tag.equals(id1)) {
											System.out.println("TTT==emergency==3333=====filename="+filename+"===tag="+tag);
											emergencyVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
													.getBitmapFromCache(filePath
															+ File.separator
															+ filename, 194, 109);
										}
										
										if(null != info.getVideoInfo2()){
											String id2 = info.getVideoInfo2().id + "";
											if(!TextUtils.isEmpty(id2)){
												if(tag.equals(id2)){
													System.out.println("TTT==emergency==4444=====filename="+filename+"===tag="+tag);
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
										String id1 = info.getVideoInfo1().id + "";
										if (tag.equals(id1)) {
											System.out.println("TTT==loop==3333=====filename="+filename+"===tag="+tag);
											loopVideoData.get(i).getVideoInfo1().videoBitmap = ImageManager
													.getBitmapFromCache(filePath
															+ File.separator
															+ filename, 194, 109);
										}
										
										if(null != info.getVideoInfo2()){
											String id2 = info.getVideoInfo2().id + "";
											if(!TextUtils.isEmpty(id2)){
												if(tag.equals(id2)){
													System.out.println("TTT===loop=4444=====filename="+filename+"===tag="+tag);
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
