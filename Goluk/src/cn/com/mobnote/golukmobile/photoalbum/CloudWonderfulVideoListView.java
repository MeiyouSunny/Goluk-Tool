package cn.com.mobnote.golukmobile.photoalbum;

import java.util.List;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import java.io.File;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.VideoPlayerActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.player.VideoPlayerView;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Intent;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class CloudWonderfulVideoListView implements IPCManagerFn{
	private View mRootLayout = null;
	private Context mContext = null;
	private PhotoAlbumActivity mActivity = null;
	private StickyListHeadersListView mStickyListHeadersListView = null;
	private CloudWonderfulVideoAdapter mCloudWonderfulVideoAdapter = null;
	private List<VideoInfo> mDataList = null;
	private List<DoubleVideoInfo> mDoubleDataList = null;
	private List<String> mGroupListName=null;
	/** 保存屏幕点击横坐标点 */
	private float screenX = 0;
	private int screenWidth = 0;
	private CustomLoadingDialog mCustomProgressDialog = null;
	/** 列表数据加载中标识 */
	private boolean isGetFileListDataing=false;
	/** 数据分页个数 */
	private int pageCount = 40;
	/** 当前在那个界面，包括循环影像(1) 紧急录像(2) 一键抢拍(4) 三个界面 */
	private int mCurrentType = IPCManagerFn.TYPE_SHORTCUT;
	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;
	/** 列表添加页脚标识 */
	private boolean addFooter=false;
	/** 列表最后的时间戳 */
	private int lastTime = 0;
	/** 添加列表底部加载中布局 */
	private RelativeLayout mBottomLoadingView = null;
	private int timeend = 2147483647;
	private boolean isShowPlayer = false;
	
	public CloudWonderfulVideoListView(Context context, int type) {
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("filemanager"+type,this);
		}
		this.mContext = context;
		this.mCurrentType = type;
		this.mActivity = (PhotoAlbumActivity)context;
		this.mDataList = new ArrayList<VideoInfo>();
		this.mDoubleDataList = new ArrayList<DoubleVideoInfo>();
		this.mGroupListName = new ArrayList<String>();
		this.screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		this.mRootLayout = LayoutInflater.from(context).inflate(R.layout.wonderful_listview, null, false);
		initView();
	}
	
	public void loadData(boolean flag) {
		if (isGetFileListDataing || (mDataList.size() != 0)) {
			return;
		}
		
		if (flag) {
			if (!mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.show();
			}
		}
		
		isGetFileListDataing = true;
		boolean isSucess = GolukApplication.getInstance().getIPCControlManager().queryFileListInfo(mCurrentType, pageCount, 0, timeend);
		GolukDebugUtils.e("", "YYYYYY=====queryFileListInfo====isSucess="+isSucess);
		if(!isSucess){
			isGetFileListDataing = false;
		}
		
	}
	
	private void initView() {
		this.mCustomProgressDialog = new CustomLoadingDialog(mActivity, null);
		mStickyListHeadersListView = (StickyListHeadersListView)mRootLayout.findViewById(R.id.mStickyListHeadersListView);
		mCloudWonderfulVideoAdapter = new CloudWonderfulVideoAdapter(mContext, mStickyListHeadersListView);
		
		setListener();
	}
	
	private void setListener() {
		mStickyListHeadersListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				screenX = arg1.getX();
				return false;
			}
		});
		
		mStickyListHeadersListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					mCloudWonderfulVideoAdapter.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					mCloudWonderfulVideoAdapter.unlock();
					GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====11111111111=");
					if (mStickyListHeadersListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
						GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====22222222=");
						if (mDataList.size() > 0 && (mDataList.size()%pageCount) == 0) {
							GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====33333=isGetFileListDataing="+isGetFileListDataing+"====mDataList.size()="+mDataList.size());
							if (isGetFileListDataing) {
								return;
							}
							GolukDebugUtils.e("", "YYYYYY=====SCROLL_STATE_IDLE====44444=");
							isGetFileListDataing = true;
							boolean isSucess = GolukApplication.getInstance().getIPCControlManager().queryFileListInfo(mCurrentType, pageCount, 0, lastTime);
							GolukDebugUtils.e("", "YYYYYY=====queryFileListInfo====isSucess="+isSucess);
							if(!isSucess){
								isGetFileListDataing = false;
							}
							
						}
					}
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mCloudWonderfulVideoAdapter.lock();
					break;
					
				default:
					break;
				}
				
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				firstVisible = firstVisibleItem;
				visibleCount = visibleItemCount;
			}
		});
		
		mStickyListHeadersListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (arg2 < mDoubleDataList.size()) {
					RelativeLayout mTMLayout1 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout1);
					RelativeLayout mTMLayout2 = (RelativeLayout)arg1.findViewById(R.id.mTMLayout2);
					String tag1 = (String)mTMLayout1.getTag();
					String tag2 = (String)mTMLayout2.getTag();
					if (mActivity.getEditState()) {
						if ((screenX > 0) && (screenX < (screenWidth/2))) {
							selectedVideoItem(tag1,mTMLayout1);
						}else{
							selectedVideoItem(tag2,mTMLayout2);
						}
					}else {
						DoubleVideoInfo d = mDoubleDataList.get(arg2);
						//点击播放
						if((screenX > 0) && (screenX < (screenWidth/2))) {
							//点击列表左边项,跳转到视频播放页面
							VideoInfo info1 = d.getVideoInfo1();
							gotoVideoPlayPage(2, info1.videoPath);
							String filename = d.getVideoInfo1().filename;
							updateNewState(filename);
							
							mDoubleDataList.get(arg2).getVideoInfo1().isNew = false;
							mCloudWonderfulVideoAdapter.notifyDataSetChanged();
						}else {
							//点击列表右边项,跳转到视频播放页面
							VideoInfo info2 = d.getVideoInfo2();
							if(null == info2)
								return;
							gotoVideoPlayPage(2, info2.videoPath);
							String filename = info2.filename;
							updateNewState(filename);
							
							mDoubleDataList.get(arg2).getVideoInfo2().isNew = false;
							mCloudWonderfulVideoAdapter.notifyDataSetChanged();
						}
					}
				}
			}
		});
		
	}
	
	private void updateNewState(String filename){
		SettingUtils.getInstance().putBoolean("Cloud_"+filename, false);
		for (int i=0; i < mDataList.size(); i++) {
			VideoInfo info = mDataList.get(i);
			if (info.filename.equals(filename)) {
				mDataList.get(i).isNew = false;
				break;
			}
		}
	}
	
	/**
	 * 跳转到本地视频播放页面
	 * @param path
	 */
	private void gotoVideoPlayPage(int from, String path) {
		if (!isShowPlayer) {
			isShowPlayer = true;
			if (null == VideoPlayerActivity.mHandler) {
				Intent intent = null;
				if (1 == from) {
					intent = new Intent(mContext, VideoPlayerActivity.class);
				} else {
					intent = new Intent(mContext, VideoPlayerView.class);
				}
				intent.putExtra("from", "ipc");
				intent.putExtra("type", mCurrentType);
				intent.putExtra("filename", path);
				mContext.startActivity(intent);
			}
		}
	}
	
	/**
	 * 选择视频item
	 * @param tag1
	 * @param mTMLayout1
	 */
	private void selectedVideoItem(String tag1,RelativeLayout mTMLayout1){
		List<String> selectedListData = mActivity.getSelectedList();
		if(!TextUtils.isEmpty(tag1)){
			if(selectedListData.contains(tag1)){
				selectedListData.remove(tag1);
				mTMLayout1.setVisibility(View.GONE);
			}else {
				selectedListData.add(tag1);
				mTMLayout1.setVisibility(View.VISIBLE);
			}
			
			if(selectedListData.size() == 0) {
				mActivity.updateTitleName("选择视频");
				mActivity.updateEditBtnState(false);
			}else {
				mActivity.updateEditBtnState(true);
				mActivity.updateTitleName("已选择"+selectedListData.size()+"个视频");
			}
		}
	}
	
	public void deleteListData(List<String> deleteData) {
		for (String path : deleteData) {
			for (VideoInfo info : mDataList) {
				if (info.filename.equals(path)) {
					GolukApplication.getInstance().getIPCControlManager().deleteFile(path);
					mDataList.remove(info);
					
					String filename = path.replace(".mp4", ".jpg");
//					String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
//					File imgfile = new File(filePath+ File.separator +filename);
//					if (imgfile.exists()) {
//						imgfile.delete();
//					}
					
					SettingUtils.getInstance().putBoolean("Cloud_"+filename, true);
					break;
				}
			}
		}
		
		List<String> mGroupListName = new ArrayList<String>();
		for(VideoInfo info : mDataList) {
			String time = info.videoCreateDate;
			String tabTime = time.substring(0,10);
			if(!mGroupListName.contains(tabTime)){
				mGroupListName.add(tabTime);
			}
		}
		
		mDoubleDataList.clear();
		mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mDataList);
		mCloudWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
	}
	
	public void downloadVideoFlush(List<String> selectedListData) {
		for(String filename : selectedListData) {
			String videoSavePath="fs1:/video/";
			if(IPCManagerFn.TYPE_SHORTCUT == mCurrentType){
				videoSavePath="fs1:/video/wonderful/";
			}else if(IPCManagerFn.TYPE_URGENT == mCurrentType){
				videoSavePath="fs1:/video/urgent/";
			}else{
				videoSavePath="fs1:/video/loop/";
			}
			
			String fileName = filename.replace(".mp4", ".jpg");
			String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
			File imgfile = new File(filePath + File.separator + fileName);
			if (!imgfile.exists()) {
				GolukApplication.getInstance().getIPCControlManager().downloadFile(fileName, "download", FileUtils.javaToLibPath(filePath), findtime(filename));
			}
			
			String mp4 = FileUtils.libToJavaPath(videoSavePath+filename);
			File file = new File(mp4);
			if(!file.exists()){
				boolean a = GolukApplication.getInstance().getIPCControlManager().querySingleFile(filename);
				GolukDebugUtils.e("xuhw", "YYYYYY===a="+a+"==querySingleFile======filename="+filename);
			}else{
				GolukDebugUtils.e("xuhw", "YYYYYY====querySingleFile==文件已存在===filename="+filename);
			}
			
		}
	}
	
	/**
	 * 查询文件录制起始时间
	 * @param filename　文件名
	 * @return 文件录制起始时间
	 * @author xuhw
	 * @date 2015年5月5日
	 */
	private long findtime(String filename) {
		long time=0;
		if (null != mDataList) {
			for (int i=0; i<mDataList.size(); i++) {
				if (filename.equals(mDataList.get(i).filename)) {
					return mDataList.get(i).time;
				}
			}
		}
		
		return time;
	}

	public View getRootView() {
		return mRootLayout;
	}
	
	public void flushList() {
		mCloudWonderfulVideoAdapter.notifyDataSetChanged();
	}
	
	private void updateData(ArrayList<VideoInfo> fileList) {
		if (!addFooter) {
			addFooter=true;
			mBottomLoadingView = (RelativeLayout) LayoutInflater.from(mActivity)
					.inflate(R.layout.video_square_below_loading, null);
			mStickyListHeadersListView.addFooterView(mBottomLoadingView);
		}
		
		if(mDataList.size() == 0) {
			mStickyListHeadersListView.setAdapter(mCloudWonderfulVideoAdapter);
		}
		
		mDataList.addAll(fileList);
		if (fileList.size() < pageCount) {
			if (addFooter) {
				addFooter = false;
				mStickyListHeadersListView.removeFooterView(mBottomLoadingView);
			}
		}
		
		mDoubleDataList.clear();
		mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mDataList);
		mGroupListName = VideoDataManagerUtils.getGroupName(mDataList);
		mCloudWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
	}
	
	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		switch (event) {
		case ENetTransEvent_IPC_VDCP_CommandResp:
			if (IPC_VDCP_Msg_Query == msg) {
				if (mCustomProgressDialog.isShowing()) {
					mCustomProgressDialog.close();
				}
				isGetFileListDataing = false;
				GolukDebugUtils.e("xuhw","YYYYYY=======获取文件列表===@@@======param1="+ param1 + "=====param2=" + param2);
				if (RESULE_SUCESS == param1) {
					if (TextUtils.isEmpty((String)param2)) {
						return;
					}
					
					ArrayList<VideoInfo> fileList = IpcDataParser.parseVideoListData((String) param2);
					if (null != fileList && fileList.size() > 0) {
						int type = IpcDataParser.parseVideoFileType(fileList.get(0).filename);
						if (type != mCurrentType) {
							return;
						}
						
						VideoInfo vfi=null;
						if(fileList.size() > 0){
							vfi = fileList.get(fileList.size() - 1);
							lastTime =  (int) vfi.time - 1;
						}
						
						updateData(fileList);
					}
				} else {
					GolukDebugUtils.e("xuhw","YYYYYY=======获取文件列表====fail==@@@======param1="+ param1 );
					// 命令发送失败
				}
			}
			break;
		// IPC下载结果应答
		case ENetTransEvent_IPC_VDTP_Resp:
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
							String filename = json.optString("filename");
							String tag = json.optString("tag");
							
							if(tag.contains("IPC_IMAGE")) {
								int type = IpcDataParser.parseVideoFileType(filename);
								if (type != mCurrentType) {
									return;
								}
								
								if (null != mCloudWonderfulVideoAdapter) {
									mCloudWonderfulVideoAdapter.updateImage(filename);
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
	
	public void onResume() {
		isShowPlayer = false;
	}
	
	public void onDestory() {
		
	}

}
