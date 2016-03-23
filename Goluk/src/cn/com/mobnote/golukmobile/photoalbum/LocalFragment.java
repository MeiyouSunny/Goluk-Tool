package cn.com.mobnote.golukmobile.photoalbum;
import java.util.ArrayList;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import cn.com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import cn.com.mobnote.eventbus.EventMessageUpdate;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.player.VideoPlayerActivity;
import cn.com.mobnote.golukmobile.player.VitamioPlayerActivity;
import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.golukmobile.startshare.VideoEditActivity;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class LocalFragment extends Fragment{
	private View mLocalVideoView;
	
	private CustomLoadingDialog mCustomProgressDialog = null;
	
	private StickyListHeadersListView mStickyListHeadersListView = null;
	
	private LocalWonderfulVideoAdapter mWonderfulVideoAdapter = null;
	
	private List<VideoInfo> mDataList = null;
	private List<DoubleVideoInfo> mDoubleDataList = null;
	
	/** 保存屏幕点击横坐标点 */
	private float screenX = 0;
	private int screenWidth = 0;
	
	private float density = 1;
	
	private FragmentAlbum mFragmentAlbum;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		EventBus.getDefault().register(this);
        
        mFragmentAlbum = (FragmentAlbum)getParentFragment();
		mLocalVideoView = inflater.inflate(R.layout.wonderful_listview, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);
		density = SoundUtils.getInstance().getDisplayMetrics().density;
		
		this.mDataList = new ArrayList<VideoInfo>();
		this.mDoubleDataList = new ArrayList<DoubleVideoInfo>();
		
		initView();
		if(mFragmentAlbum.mCurrentType == 0){
			loadData(IPCManagerFn.TYPE_SHORTCUT, true);
		}else{
			loadData(IPCManagerFn.TYPE_SHORTCUT, false);
		}
		
		return mLocalVideoView;
	}
	
	

	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
	}
	
	public void onEventMainThread(EventDeletePhotoAlbumVid event){
		if(event!=null&&event.getmType() == PhotoAlbumConfig.PHOTO_BUM_LOCAL){
			
		}
	}




	private void initView(){
		mCustomProgressDialog = new CustomLoadingDialog(this.getContext(), null);
		mStickyListHeadersListView = (StickyListHeadersListView) mLocalVideoView.findViewById(R.id.mStickyListHeadersListView);
		
		mWonderfulVideoAdapter = new LocalWonderfulVideoAdapter(this.getContext(), mFragmentAlbum, mStickyListHeadersListView, IPCManagerFn.TYPE_CIRCULATE, "local");
		setListener();
	}
	
	private void setListener() {
		// 屏蔽某些机型的下拉悬停操作
		// mStickyListHeadersListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
					// mWonderfulVideoAdapter.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					// mWonderfulVideoAdapter.unlock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					// mWonderfulVideoAdapter.lock();
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
				if (screenX < (30 * density)) {
					return;
				}

				if (arg2 < mDoubleDataList.size()) {
					RelativeLayout mTMLayout1 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout1);
					RelativeLayout mTMLayout2 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout2);
					String tag1 = (String) mTMLayout1.getTag();
					String tag2 = (String) mTMLayout2.getTag();
					if (mFragmentAlbum.getEditState()) {
						if ((screenX > 0) && (screenX < (screenWidth / 2))) {
							selectedVideoItem(tag1, mTMLayout1);
						} else {
							selectedVideoItem(tag2, mTMLayout2);
						}
					} else {
						DoubleVideoInfo d = mDoubleDataList.get(arg2);
						// 点击播放
						if ((screenX > 0) && (screenX < (screenWidth / 2))) {
							// 点击列表左边项,跳转到视频播放页面
							VideoInfo info1 = d.getVideoInfo1();
							gotoVideoPlayPage(2, info1.videoPath, info1.videoCreateDate, info1.videoHP, info1.videoSize);
							String filename = d.getVideoInfo1().filename;
							updateNewState(filename);

							mDoubleDataList.get(arg2).getVideoInfo1().isNew = false;
							mWonderfulVideoAdapter.notifyDataSetChanged();
						} else {
							// 点击列表右边项,跳转到视频播放页面
							VideoInfo info2 = d.getVideoInfo2();
							if (null == info2)
								return;
							//--------------------------------------------------以此标记  type 零时给 1   等主题逻辑调试通了 再去更具文件名称取类型
							gotoVideoPlayPage(1, info2.videoPath, info2.videoCreateDate, info2.videoHP, info2.videoSize);
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
	
	public void loadData(int type, boolean flag) {
		if (flag) {
			if (IPCManagerFn.TYPE_SHORTCUT == type) {
				if (!mCustomProgressDialog.isShowing()) {
					mCustomProgressDialog.show();
				}
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
				if(mDoubleDataList == null || mDoubleDataList.size() ==0){
					View empty = PhotoAlbumUtile.getInstall().getEmptyView(getActivity(),0);
					((ViewGroup)mStickyListHeadersListView.getParent()).addView(empty); 
				}
				
				mStickyListHeadersListView.setAdapter(mWonderfulVideoAdapter);
				try {
					if (mCustomProgressDialog.isShowing()) {
						mCustomProgressDialog.close();
					}
				} catch (Exception e) {

				}

				checkListState();
			}
		});
		task.execute("");
	}
	
	private void checkListState() {
		GolukDebugUtils.e("", "Album------WondowvideoListView------checkListState");
		if (mDataList.size() <= 0) {
			mStickyListHeadersListView.setVisibility(View.GONE);
			updateEditState(false);
		} else {
			updateEditState(true);
			mStickyListHeadersListView.setVisibility(View.VISIBLE);
		}
	}
	
	private void updateEditState(boolean isHasData) {
		GolukDebugUtils.e("", "Album------WondowvideoListView------updateEditState" + isHasData);
//		if (null == mLocalVideoListView) {
//			return;
//		}
		
//		if (null == mLocalVideoListView) {
//			mLocalVideoListView = new LocalVideoManager(this.getContext(),"local", mPromotionSelectItem);
//			mMainLayout.addView(mLocalVideoListView.getRootView());
//		}
//		mLocalVideoListView.updateEdit(mVideoType, isHasData);
	}
	
	private void updateNewState(String filename) {
		SettingUtils.getInstance().putBoolean("Local_" + filename, false);
		for (int i = 0; i < mDataList.size(); i++) {
			VideoInfo info = mDataList.get(i);
			if (info.filename.equals(filename)) {
				mDataList.get(i).isNew = false;
				break;
			}
		}
	}
	
	/**
	 * 选择视频item
	 * 
	 * @param tag1
	 * @param mTMLayout1
	 */
	private void selectedVideoItem(String tag1, RelativeLayout mTMLayout1) {
		List<String> selectedListData = mFragmentAlbum.getSelectedList();
		if (!TextUtils.isEmpty(tag1)) {
			if (selectedListData.contains(tag1)) {
				selectedListData.remove(tag1);
				mTMLayout1.setVisibility(View.GONE);
			} else {
				selectedListData.add(tag1);
				mTMLayout1.setVisibility(View.VISIBLE);
			}

			if (selectedListData.size() == 0) {
				mFragmentAlbum.updateTitleName(this.getContext().getResources().getString(R.string.local_video_title_text));
				mFragmentAlbum.updateEditBtnState(false);
			} else {
				mFragmentAlbum.updateEditBtnState(true);
				mFragmentAlbum.updateTitleName(this.getContext().getResources().getString(R.string.str_photo_select1)
						+ selectedListData.size() + this.getContext().getResources().getString(R.string.str_photo_select2));
			}
		}
	}
	
	/**
	 * 跳转到本地视频播放页面
	 * 
	 * @param path
	 */
	private void gotoVideoPlayPage(int type, String path, String createTime, String videoHP, String size) {
		if (!TextUtils.isEmpty(path)) {

			Intent intent = null;
//			if (1 == type) {
//				intent = new Intent(this.getContext(), VitamioPlayerActivity.class);
//			} else {
//				intent = new Intent(this.getContext(), VideoPlayerActivity.class);
//			}
//			intent.putExtra("from", "local");
//			intent.putExtra("path", path);
//			this.getContext().startActivity(intent);
			intent = new Intent(getContext(), PhotoAlbumPlayer.class);
			intent.putExtra(PhotoAlbumPlayer.VIDEO_FROM, "local");
			intent.putExtra(PhotoAlbumPlayer.PATH, path);
			intent.putExtra(PhotoAlbumPlayer.DATE, createTime);
			intent.putExtra(PhotoAlbumPlayer.HP, videoHP);
			intent.putExtra(PhotoAlbumPlayer.SIZE, size);
			intent.putExtra(PhotoAlbumPlayer.TYPE, type);
			getContext().startActivity(intent);
		}
	}
	

}