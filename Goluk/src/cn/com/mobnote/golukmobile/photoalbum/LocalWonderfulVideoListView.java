package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.fileinfo.GolukVideoInfoDbManager;
import cn.com.mobnote.golukmobile.player.VideoPlayerActivity;
import cn.com.mobnote.golukmobile.player.VitamioPlayerActivity;
import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.golukmobile.startshare.VideoEditActivity;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

@SuppressLint("InflateParams")
public class LocalWonderfulVideoListView {
	private View mRootLayout = null;
	private Context mContext = null;
	private LocalVideoManager mLocalVideoListView = null;
	private FragmentAlbum mFragment = null;
	private StickyListHeadersListView mStickyListHeadersListView = null;
	private LocalWonderfulVideoAdapter mWonderfulVideoAdapter = null;
	private List<VideoInfo> mDataList = null;
	private List<DoubleVideoInfo> mDoubleDataList = null;
	/** 保存屏幕点击横坐标点 */
	private float screenX = 0;
	private int screenWidth = 0;
	/** 视频类型，精彩/紧急/循环 */
	private int mVideoType;
	//CK Start
//	private CustomLoadingDialog mCustomProgressDialog = null;
	//CK End
	private String from = null;
	private TextView empty = null;
	private float density = 1;
	// private boolean clickLock = false;
	private PromotionSelectItem mPromotionSelectItem;

	public LocalWonderfulVideoListView(Context context, FragmentAlbum fragment, LocalVideoManager localVideoListView, int type, String from,
			PromotionSelectItem item) {
		this.from = from;
		this.mContext = context;
		mLocalVideoListView = localVideoListView;
		this.mFragment = fragment;
		this.mVideoType = type;
		this.mDataList = new ArrayList<VideoInfo>();
		this.mDoubleDataList = new ArrayList<DoubleVideoInfo>();
		this.screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		mPromotionSelectItem = item;
		mRootLayout = LayoutInflater.from(context).inflate(R.layout.wonderful_listview, null, false);
		density = SoundUtils.getInstance().getDisplayMetrics().density;
		initView();
	}

	private void initView() {
		empty = (TextView) mRootLayout.findViewById(R.id.empty);
//		mCustomProgressDialog = new CustomLoadingDialog(mActivity, null);
		mStickyListHeadersListView = (StickyListHeadersListView) mRootLayout
				.findViewById(R.id.mStickyListHeadersListView);
		mWonderfulVideoAdapter = new LocalWonderfulVideoAdapter(mContext, mFragment, mStickyListHeadersListView, mVideoType, from);
		loadData(mVideoType, true);
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
					if (mFragment.getEditState()) {
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
							gotoVideoPlayPage(mVideoType, info1.videoPath, info1.videoCreateDate, info1.videoHP, info1.videoSize);
							String filename = d.getVideoInfo1().filename;
							updateNewState(filename);

							mDoubleDataList.get(arg2).getVideoInfo1().isNew = false;
							mWonderfulVideoAdapter.notifyDataSetChanged();
						} else {
							// 点击列表右边项,跳转到视频播放页面
							VideoInfo info2 = d.getVideoInfo2();
							if (null == info2)
								return;
							gotoVideoPlayPage(mVideoType, info2.videoPath, info2.videoCreateDate, info2.videoHP, info2.videoSize);
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
	 * 跳转到本地视频播放页面
	 * 
	 * @param path
	 */
	private void gotoVideoPlayPage(int type, String path, String createTime, String videoHP, String size) {
		// if (getClickLock()) {
		// return;
		// }

		// setClickLock(true);
		if (!TextUtils.isEmpty(path)) {
			if ("cloud".equals(from)) {
				if (1 != type) {
					Intent intent = new Intent(mContext, VideoEditActivity.class);

					int tempType = 2;
					if (type == IPCManagerFn.TYPE_URGENT) {
						tempType = 3;
					}

					if (mPromotionSelectItem != null) {
						intent.putExtra(FragmentAlbum.ACTIVITY_INFO, mPromotionSelectItem);
					}
					intent.putExtra("type", tempType);
					intent.putExtra("cn.com.mobnote.video.path", path);
					mContext.startActivity(intent);
					return;
				}
			}

			Intent intent = null;
			if (1 == type) {
				intent = new Intent(mContext, VitamioPlayerActivity.class);
			} else {
				intent = new Intent(mContext, VideoPlayerActivity.class);
			}
			intent.putExtra("from", "local");
			intent.putExtra("path", path);
//			intent = new Intent(mContext, PhotoAlbumPlayer.class);
//			intent.putExtra(PhotoAlbumPlayer.VIDEO_FROM, "local");
//			intent.putExtra(PhotoAlbumPlayer.PATH, path);
//			intent.putExtra(PhotoAlbumPlayer.DATE, createTime);
//			intent.putExtra(PhotoAlbumPlayer.HP, videoHP);
//			intent.putExtra(PhotoAlbumPlayer.SIZE, size);
//			intent.putExtra(PhotoAlbumPlayer.TYPE, type);
			mContext.startActivity(intent);
		}
	}

	/**
	 * 选择视频item
	 * 
	 * @param tag1
	 * @param mTMLayout1
	 */
	private void selectedVideoItem(String tag1, RelativeLayout mTMLayout1) {
		List<String> selectedListData = mFragment.getSelectedList();
		if (!TextUtils.isEmpty(tag1)) {
			if (selectedListData.contains(tag1)) {
				selectedListData.remove(tag1);
				mTMLayout1.setVisibility(View.GONE);
			} else {
				selectedListData.add(tag1);
				mTMLayout1.setVisibility(View.VISIBLE);
			}

			if (selectedListData.size() == 0) {
				mFragment.updateTitleName(mContext.getResources().getString(R.string.local_video_title_text));
				mFragment.updateEditBtnState(false);
			} else {
				mFragment.updateEditBtnState(true);
				mFragment.updateTitleName(mContext.getResources().getString(R.string.str_photo_select1)
						+ selectedListData.size() + mContext.getResources().getString(R.string.str_photo_select2));
			}
		}
	}

	private void loadData(int type, boolean flag) {
		if (flag) {
//			if (IPCManagerFn.TYPE_SHORTCUT == type) {
//				if (!mCustomProgressDialog.isShowing()) {
//					mCustomProgressDialog.show();
//				}
//			}
		}
		LocalDataLoadAsyncTask task = new LocalDataLoadAsyncTask(type, new DataCallBack() {
			@Override
			public void onSuccess(int type, List<VideoInfo> mLocalListData, List<String> mGroupListName) {
				mDataList.clear();
				mDoubleDataList.clear();
				mDataList.addAll(mLocalListData);
				mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mLocalListData);
				mWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
				mStickyListHeadersListView.setAdapter(mWonderfulVideoAdapter);
//				try {
//					if (mCustomProgressDialog.isShowing()) {
//						mCustomProgressDialog.close();
//					}
//				} catch (Exception e) {
//
//				}

				checkListState();
			}
		});
		task.execute("");
	}

	public boolean isHasData() {
		if (mDataList.size() <= 0) {
			return false;
		} else {
			return true;
		}
	}

	public void updateData() {
		loadData(mVideoType, false);
	}

	public View getRootView() {
		return mRootLayout;
	}

	public void flushList() {
		mWonderfulVideoAdapter.notifyDataSetChanged();
	}

	public void deleteListData(List<String> deleteData) {
		final String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		for (String path : deleteData) {
			for (VideoInfo info : mDataList) {
				if (info.videoPath.equals(path)) {
					// 删除视频文件
					mDataList.remove(info);
					File mp4file = new File(path);
					if (mp4file.exists()) {
						mp4file.delete();
					}
					String filename = path.substring(path.lastIndexOf("/") + 1);
					// 删除数据库中的数据
					GolukVideoInfoDbManager.getInstance().delVideoInfo(filename);
					// 删除视频对应的图片
					filename = filename.replace(".mp4", ".jpg");
					File imgfile = new File(filePath + File.separator + filename);
					if (imgfile.exists()) {
						imgfile.delete();
					}
					SettingUtils.getInstance().putBoolean(filename, true);
					break;
				}
			}
		}

		List<String> mGroupListName = new ArrayList<String>();
		for (VideoInfo info : mDataList) {
			String time = info.videoCreateDate;
			String tabTime = time.substring(0, 10);
			if (!mGroupListName.contains(tabTime)) {
				mGroupListName.add(tabTime);
			}
		}

		mDoubleDataList.clear();
		mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mDataList);
		mWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
		checkListState();
	}

	private void checkListState() {
		GolukDebugUtils.e("", "Album------WondowvideoListView------checkListState");
		if (mDataList.size() <= 0) {
			empty.setVisibility(View.VISIBLE);
			mStickyListHeadersListView.setVisibility(View.GONE);
			updateEditState(false);
		} else {
			updateEditState(true);
			empty.setVisibility(View.GONE);
			mStickyListHeadersListView.setVisibility(View.VISIBLE);
		}
	}

	private void updateEditState(boolean isHasData) {
		GolukDebugUtils.e("", "Album------WondowvideoListView------updateEditState" + isHasData);
		if (null == mLocalVideoListView) {
			return;
		}
		mLocalVideoListView.updateEdit(mVideoType, isHasData);
	}

	// public synchronized boolean getClickLock() {
	// return clickLock;
	// }
	//
	// public synchronized void setClickLock(boolean lock) {
	// clickLock = lock;
	// }

	public void onResume() {
		// setClickLock(false);
	}

}
