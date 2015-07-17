package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.VideoPlayerActivity;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.player.VideoPlayerView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class WonderfulVideoListView {
	private View mRootLayout = null;
	private Context mContext = null;
	private PhotoAlbumActivity mActivity = null;
	private StickyListHeadersListView mStickyListHeadersListView = null;
	private WonderfulVideoAdapter mWonderfulVideoAdapter = null;
	private List<VideoInfo> mDataList = null;
	private List<DoubleVideoInfo> mDoubleDataList = null;
	/** 保存屏幕点击横坐标点 */
	private float screenX = 0;
	private int screenWidth = 0;
	private int type;
	private CustomLoadingDialog mCustomProgressDialog = null;
	
	public WonderfulVideoListView(Context context, int type) {
		this.mContext = context;
		this.mActivity = (PhotoAlbumActivity)context;
		this.type = type;
		this.mDataList = new ArrayList<VideoInfo>();
		this.mDoubleDataList = new ArrayList<DoubleVideoInfo>();
		this.screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		mRootLayout = LayoutInflater.from(context).inflate(R.layout.wonderful_listview, null, false);
		initView();
	}
	
	private void initView() {
		mStickyListHeadersListView = (StickyListHeadersListView)mRootLayout.findViewById(R.id.mStickyListHeadersListView);
		mWonderfulVideoAdapter = new WonderfulVideoAdapter(mContext, mStickyListHeadersListView);
		mStickyListHeadersListView.setAdapter(mWonderfulVideoAdapter);
		loadData(type);
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
					mWonderfulVideoAdapter.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					mWonderfulVideoAdapter.unlock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mWonderfulVideoAdapter.lock();
					break;
					
				default:
					break;
				}
				
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				
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
							mWonderfulVideoAdapter.notifyDataSetChanged();
						}else {
							//点击列表右边项,跳转到视频播放页面
							VideoInfo info2 = d.getVideoInfo2();
							if(null == info2)
								return;
							gotoVideoPlayPage(2, info2.videoPath);
							String filename = info2.filename;
							updateNewState(filename);
							
							mDoubleDataList.get(arg2).getVideoInfo2().isNew = false;
							mWonderfulVideoAdapter.notifyDataSetChanged();
						}
					}
				}
			}
		});
		
	}
	
	private void updateNewState(String filename){
		SettingUtils.getInstance().putBoolean(filename, false);
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
	private void gotoVideoPlayPage(int from, String path){
		if(!TextUtils.isEmpty(path)){
			Intent intent = null;
			if(1 == from) {
				intent = new Intent(mContext, VideoPlayerActivity.class);
			}else {
				intent = new Intent(mContext, VideoPlayerView.class);
			}
			intent.putExtra("from", "local");
			intent.putExtra("path", path);
			mActivity.startActivity(intent);
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
	
	private void loadData(int type) {
		if(null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mActivity, null);
			if (!mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.show();
			}
		}
		LocalDataLoadAsyncTask task = new LocalDataLoadAsyncTask(type, new DataCallBack() {
			@Override
			public void onSuccess(int type, List<VideoInfo> mLocalListData, List<String> mGroupListName) {
				mDataList.clear();
				mDoubleDataList.clear();
				mDataList.addAll(mLocalListData);
				mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mLocalListData);
				mWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
				if (mCustomProgressDialog.isShowing()) {
					mCustomProgressDialog.close();
				}
			}
		});
		task.execute("");
	}
	
	public View getRootView() {
		return mRootLayout;
	}
	
	public void flushList() {
		mWonderfulVideoAdapter.notifyDataSetChanged();
	}
	
	public void deleteListData(List<String> deleteData) {
		for (String path : deleteData) {
			for (VideoInfo info : mDataList) {
				if (info.videoPath.equals(path)) {
					mDataList.remove(info);
					File mp4file = new File(path);
					if (mp4file.exists()) {
						mp4file.delete();
					}
					
					String filename = path.substring(path.lastIndexOf("/")+1);
					filename = filename.replace(".mp4", ".jpg");
					String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
					File imgfile = new File(filePath+ File.separator +filename);
					if (imgfile.exists()) {
						imgfile.delete();
					}
					
					SettingUtils.getInstance().putBoolean(filename, true);
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
		mWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
	}

}
