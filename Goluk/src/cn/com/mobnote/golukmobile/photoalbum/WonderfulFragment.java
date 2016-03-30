package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import cn.com.mobnote.eventbus.EventDownloadIpcVid;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;

import de.greenrobot.event.EventBus;

public class WonderfulFragment extends Fragment implements IPCManagerFn {

	private View mWonderfulVideoView;

	/** 列表数据加载中标识 */
	private boolean isGetFileListDataing = false;

	private List<VideoInfo> mDataList = null;
	private List<DoubleVideoInfo> mDoubleDataList = null;

	private float density = 1;

	/** 保存屏幕点击横坐标点 */
	private float screenX = 0;
	private int screenWidth = 0;

	private CustomLoadingDialog mCustomProgressDialog = null;

	private StickyListHeadersListView mStickyListHeadersListView = null;
	private CloudWonderfulVideoAdapter mCloudWonderfulVideoAdapter = null;

	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;

	/** 判断服务端是否还有数据 */
	private boolean isHasData = true;

	/** 数据分页个数 */
	private final int pageCount = 40;

	/** 列表最后的时间戳 */
	private int lastTime = 0;

	/** 列表添加页脚标识 */
	private boolean addFooter = false;

	private FragmentAlbum mFragmentAlbum;

	private boolean isShowPlayer = false;

	/** 添加列表底部加载中布局 */
	private RelativeLayout mBottomLoadingView = null;

	private int timeend = 2147483647;

	private List<String> mGroupListName = null;
	
	private TextView empty = null;
	
	/** 防止重复下载 */
	List<Boolean> exist = new ArrayList<Boolean>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if(mWonderfulVideoView == null){
			EventBus.getDefault().register(this);
			if (mWonderfulVideoView == null) {
				mWonderfulVideoView = inflater.inflate(R.layout.wonderful_listview,(ViewGroup) getActivity().findViewById(R.id.viewpager),false);
			}

			if (null != GolukApplication.getInstance().getIPCControlManager()) {
				GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("filemanager" + IPCManagerFn.TYPE_SHORTCUT, this);
			}

			mFragmentAlbum = (FragmentAlbum) getParentFragment();
			mDataList = new ArrayList<VideoInfo>();
			mDoubleDataList = new ArrayList<DoubleVideoInfo>();
			mGroupListName = new ArrayList<String>();

			// mCloudVideoListView = new CloudVideoManager(this.getContext());
			screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
			mWonderfulVideoView = inflater.inflate(R.layout.wonderful_listview, null, false);
			density = SoundUtils.getInstance().getDisplayMetrics().density;
			initView();
		}
		
		ViewGroup parent = (ViewGroup) mWonderfulVideoView.getParent();
		if(parent != null){
			parent.removeView(mWonderfulVideoView);
		}
		
		return mWonderfulVideoView;
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
	}
	
	
	/**
	 * 删除本地视频event
	 * @param event
	 */
	public void onEventMainThread(EventDeletePhotoAlbumVid event){
		if(event!=null&&event.getType() == PhotoAlbumConfig.PHOTO_BUM_IPC_WND){
			
			List<String> list = new ArrayList<String>();
			list.add(event.getVidPath());
			deleteListData(list);
		}
	}
	
	/**
	 * 从设备上下载视频到本地
	 * @param event
	 */
	public void onEventMainThread(EventDownloadIpcVid event){
		if(event!=null&&event.getType() == PhotoAlbumConfig.PHOTO_BUM_IPC_WND){
			
			List<String> list = new ArrayList<String>();
			list.add(event.getVidPath());
			downloadVideoFlush(list);
		}
	}
	
	public void downloadVideoFlush(List<String> selectedListData) {
		exist.clear();
		for (String filename : selectedListData) {
			// 下载视频对应的图片
			String imgFileName = filename.replace(".mp4", ".jpg");
			String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
			File imgfile = new File(filePath + File.separator + imgFileName);
			if (!imgfile.exists()) {
				GolukApplication.getInstance().getIPCControlManager()
						.downloadFile(imgFileName, "download", FileUtils.javaToLibPath(filePath), PhotoAlbumUtils.findtime(filename,mDataList));
			}
			
			// 下载视频文件
			String mp4 = FileUtils.libToJavaPath(PhotoAlbumConfig.LOCAL_WND_VIDEO_PATH + filename);
			
			File file = new File(mp4);
			if (!file.exists()) {
				List<String> downloadlist = GolukApplication.getInstance().getDownLoadList();
				if (!downloadlist.contains(filename)) {
					exist.add(false);
					Log.i("download", "download:" + mp4);
					boolean a = GolukApplication.getInstance().getIPCControlManager().querySingleFile(filename);
					GolukDebugUtils.e("xuhw", "YYYYYY===a=" + a + "==querySingleFile======filename=" + filename);
				}
			} else {
				exist.add(true);
			}

		}

		boolean isshow = false;
		for (boolean flag : exist) {
			if (!flag) {
				isshow = false;
				break;
			} else {
				isshow = true;
			}
		}

		if (isshow) {
			GolukUtils.showToast(getContext(), getContext().getString(R.string.str_synchronous_video_to_local));
		}

	}


	public void deleteListData(List<String> deleteData) {
		for (String path : deleteData) {
			for (VideoInfo info : mDataList) {
				if (info.filename.equals(path)) {
					GolukApplication.getInstance().getIPCControlManager().deleteFile(path);
					mDataList.remove(info);
					String filename = path.replace(".mp4", ".jpg");
					SettingUtils.getInstance().putBoolean("Cloud_" + filename, true);
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
		mCloudWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
		checkListState();
	}

	// @Override
	// public void setUserVisibleHint(boolean isVisibleToUser) {
	// super.setUserVisibleHint(isVisibleToUser);
	//
	// if(getUserVisibleHint()) {
	// isVisible = true;
	// onVisible();
	// } else {
	// isVisible = false;
	// onInvisible();
	// }
	// }

	// /**
	// * 可见
	// */
	// protected void onVisible() {
	// lazyLoad();
	// }
	//
	// /**
	// * 不可见
	// */
	// protected void onInvisible() {
	//
	// }
	//
	// protected void lazyLoad() {
	// if (!isPrepared || !isVisible || mHasLoadedOnce) {
	// return;
	// }
	//
	// new AsyncTask<Void,Boolean,String>() {
	//
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// //显示加载进度对话框
	// // UIHelper.showDialogForLoading(getActivity(), "正在加载...", true);
	// }
	//
	// @Override
	// protected Boolean doInBackground(Void... params) {
	// try {
	// Thread.sleep(2000);
	// //在这里添加调用接口获取数据的代码
	// //doSomething()
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return true;
	// }
	//
	// @Override
	// protected void onPostExecute(Boolean isSuccess) {
	// if (isSuccess) {
	// // 加载成功
	// mHasLoadedOnce = true;
	// } else {
	// // 加载失败
	// }
	// }
	// }.execute();
	// }

	private void initView() {
		empty = (TextView) mWonderfulVideoView.findViewById(R.id.empty);
		this.mCustomProgressDialog = new CustomLoadingDialog(this.getContext(),null);
		mStickyListHeadersListView = (StickyListHeadersListView) mWonderfulVideoView.findViewById(R.id.mStickyListHeadersListView);
		mCloudWonderfulVideoAdapter = new CloudWonderfulVideoAdapter(this.getContext(), (FragmentAlbum) getParentFragment(),mStickyListHeadersListView);
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
					// mCloudWonderfulVideoAdapter.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					// mCloudWonderfulVideoAdapter.unlock();
					GolukDebugUtils.e("","YYYYYY=====SCROLL_STATE_IDLE====11111111111=");
					if (mStickyListHeadersListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
						GolukDebugUtils.e("","YYYYYY=====SCROLL_STATE_IDLE====22222222=");
						final int size = mDataList.size();
						if (size > 0 && isHasData) {
							GolukDebugUtils.e("","YYYYYY=====SCROLL_STATE_IDLE====33333=isGetFileListDataing="
											+ isGetFileListDataing
											+ "====mDataList.size()="
											+ mDataList.size());
							if (isGetFileListDataing) {
								return;
							}
							GolukDebugUtils.e("",
									"YYYYYY=====SCROLL_STATE_IDLE====44444=");
							isGetFileListDataing = true;
							boolean isSucess = GolukApplication
									.getInstance()
									.getIPCControlManager()
									.queryFileListInfo(
											IPCManagerFn.TYPE_SHORTCUT,
											pageCount, 0, lastTime);
							GolukDebugUtils.e("",
									"YYYYYY=====queryFileListInfo====isSucess="
											+ isSucess);
							if (!isSucess) {
								isGetFileListDataing = false;
							} else {
								addFooterView();
							}

						}
					}
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					// mCloudWonderfulVideoAdapter.lock();
					break;

				default:
					break;
				}

			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int arg3) {
				firstVisible = firstVisibleItem;
				visibleCount = visibleItemCount;
			}
		});

		mStickyListHeadersListView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						if (screenX < (30 * density)) {
							return;
						}

						if (arg2 < mDoubleDataList.size()) {
							RelativeLayout mTMLayout1 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout1);
							RelativeLayout mTMLayout2 = (RelativeLayout) arg1.findViewById(R.id.mTMLayout2);
							String tag1 = (String) mTMLayout1.getTag();
							String tag2 = (String) mTMLayout2.getTag();
							if (mFragmentAlbum.getEditState()) {
								if ((screenX > 0)&& (screenX < (screenWidth / 2))) {
									selectedVideoItem(tag1, mTMLayout1);
								} else {
									selectedVideoItem(tag2, mTMLayout2);
								}
							} else {
								DoubleVideoInfo d = mDoubleDataList.get(arg2);
								// 点击播放
								if ((screenX > 0)&& (screenX < (screenWidth / 2))) {
									// 点击列表左边项,跳转到视频播放页面
									VideoInfo info1 = d.getVideoInfo1();
									gotoVideoPlayPage(IPCManagerFn.TYPE_SHORTCUT,info1.videoPath, info1.videoCreateDate, info1.videoHP, info1.videoSize);
									String filename = d.getVideoInfo1().filename;
									updateNewState(filename);

									mDoubleDataList.get(arg2).getVideoInfo1().isNew = false;
									mCloudWonderfulVideoAdapter.notifyDataSetChanged();
								} else {
									// 点击列表右边项,跳转到视频播放页面
									VideoInfo info2 = d.getVideoInfo2();
									if (null == info2)
										return;
									gotoVideoPlayPage(4, info2.videoPath, info2.videoCreateDate, info2.videoHP, info2.videoSize);
									String filename = info2.filename;
									updateNewState(filename);

									mDoubleDataList.get(arg2).getVideoInfo2().isNew = false;
									mCloudWonderfulVideoAdapter.notifyDataSetChanged();
								}
							}
						}
					}
				});
		
		empty.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(GolukApplication.getInstance().isIpcLoginSuccess == false){
					if(!"0".equals(mFragmentAlbum.mPlatform)){
						getActivity().finish();
					}else{
						Intent intent = new Intent(getActivity(),CarRecorderActivity.class);
						startActivity(intent);
					}
				}
			}
		});

	}

	private void updateNewState(String filename) {
		SettingUtils.getInstance().putBoolean("Cloud_" + filename, false);
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
	private void gotoVideoPlayPage(int from, String path, String createTime, String videoHP, String size) {
		if (!isShowPlayer) {
			isShowPlayer = true;
			// if (null == VitamioPlayerActivity.mHandler) {
//			Intent intent = null;
//			if (1 == from) {
//				intent = new Intent(this.getContext(),VitamioPlayerActivity.class);
//			} else {
//				intent = new Intent(this.getContext(),VideoPlayerActivity.class);
//			}
//			intent.putExtra("from", "ipc");
//			intent.putExtra("type", IPCManagerFn.TYPE_SHORTCUT);
//			intent.putExtra("filename", path);
//			this.getContext().startActivity(intent);

			Intent intent = new Intent(getContext(), PhotoAlbumPlayer.class);
			intent.putExtra(PhotoAlbumPlayer.VIDEO_FROM, "ipc");
			intent.putExtra(PhotoAlbumPlayer.FILENAME, path);
			intent.putExtra(PhotoAlbumPlayer.DATE, createTime);
			intent.putExtra(PhotoAlbumPlayer.HP, videoHP);
			intent.putExtra(PhotoAlbumPlayer.TYPE, IPCManagerFn.TYPE_SHORTCUT);
			intent.putExtra(PhotoAlbumPlayer.SIZE, size);
			getContext().startActivity(intent);
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
				mFragmentAlbum.updateTitleName(this.getContext().getString(R.string.local_video_title_text));
				mFragmentAlbum.updateEditBtnState(false);
			} else {
				mFragmentAlbum.updateEditBtnState(true);
				mFragmentAlbum.updateTitleName(this.getContext().getString(
						R.string.str_photo_select1)
						+ selectedListData.size()
						+ this.getContext().getString(R.string.str_photo_select2));
			}
		}
	}

	/**
	 * 添加加载loading
	 * 
	 * @author jyf
	 */
	private void addFooterView() {
		if (!addFooter) {
			addFooter = true;
			mBottomLoadingView = (RelativeLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.video_square_below_loading, null);
			mStickyListHeadersListView.addFooterView(mBottomLoadingView);
		}
	}

	public void loadData(boolean flag) {
		if (isGetFileListDataing) {
			return;
		}

		if (flag) {
			if (!mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.show();
			}
			empty.setVisibility(View.GONE);
			mStickyListHeadersListView.setVisibility(View.VISIBLE);
			isGetFileListDataing = true;
			mDataList.clear();
			boolean isSucess = GolukApplication
					.getInstance()
					.getIPCControlManager()
					.queryFileListInfo(IPCManagerFn.TYPE_SHORTCUT, pageCount, 0,timeend);
			GolukDebugUtils.e("", "YYYYYY=====queryFileListInfo====isSucess="+ isSucess);
			if (!isSucess) {
				isGetFileListDataing = false;
			}
		}else{
			mFragmentAlbum.setEditBtnState(false);
			empty.setText(getActivity().getResources().getString(R.string.photoalbum_no_ipc_connect_text));
			empty.setVisibility(View.VISIBLE);
			mStickyListHeadersListView.setVisibility(View.GONE);
		}
	

	}

	private void updateEditState(boolean isHasData) {
		mFragmentAlbum.setEditBtnState(isHasData);
		/*
		 * GolukDebugUtils.e("",
		 * "Album------WondowvideoListView------updateEditState" + isHasData);
		 * if (null == mCloudVideoListView) { return; }
		 * mCloudVideoListView.updateEdit(4, isHasData);
		 */
	}

	private void checkListState() {
		if (mDataList.size() <= 0) {
			empty.setVisibility(View.VISIBLE);
			empty.setText(getActivity().getResources().getString(R.string.photoalbum_no_video_text));
			mStickyListHeadersListView.setVisibility(View.GONE);
			updateEditState(false);
		} else {
			empty.setVisibility(View.GONE);
			mStickyListHeadersListView.setVisibility(View.VISIBLE);
			updateEditState(true);
		}
	}

	private void updateData(ArrayList<VideoInfo> fileList) {
		addFooterView();
		if (mDataList.size() == 0) {
			mStickyListHeadersListView.setAdapter(mCloudWonderfulVideoAdapter);
		}
		mDataList.addAll(fileList);
		if (fileList.size() < pageCount) {
			isHasData = false;
			removeFooterView();
		}
		mDoubleDataList.clear();
		mDoubleDataList = VideoDataManagerUtils.videoInfo2Double(mDataList);
		mGroupListName = VideoDataManagerUtils.getGroupName(mDataList);
		mCloudWonderfulVideoAdapter.setData(mGroupListName, mDoubleDataList);
	}

	/**
	 * 移除loading
	 * 
	 * @author jyf
	 */
	private void removeFooterView() {
		if (addFooter) {
			addFooter = false;
			mStickyListHeadersListView.removeFooterView(mBottomLoadingView);
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		switch (event) {
		case ENetTransEvent_IPC_VDCP_CommandResp:
			if (IPC_VDCP_Msg_Query == msg) {
				if (mCustomProgressDialog != null && mCustomProgressDialog.isShowing()) {
					mCustomProgressDialog.close();
				}
				isGetFileListDataing = false;
				GolukDebugUtils.e("xuhw","YYYYYY=======获取文件列表===@@@======param1=" + param1+ "=====param2=" + param2);
				if (RESULE_SUCESS == param1) {
					if (TextUtils.isEmpty((String) param2)) {
						return;
					}

					ArrayList<VideoInfo> fileList = IpcDataParser.parseVideoListData((String) param2);
					if (null != fileList && fileList.size() > 0) {
						int type = IpcDataParser.parseVideoFileType(fileList.get(0).filename);
						if (type != IPCManagerFn.TYPE_SHORTCUT) {
							return;
						}

						VideoInfo vfi = null;
						if (fileList.size() > 0) {
							vfi = fileList.get(fileList.size() - 1);
							lastTime = (int) vfi.time - 1;
						}

						updateData(fileList);
					} else {
						isHasData = false;
						removeFooterView();
					}
				} else {
					GolukDebugUtils.e("xuhw",
							"YYYYYY=======获取文件列表====fail==@@@======param1="+ param1);
					// 命令发送失败
					this.removeFooterView();
					GolukUtils.showToast(this.getContext(), this.getContext().getString(R.string.str_inquiry_fail));
				}
				checkListState();
			}
			break;
			
		// IPC下载结果应答
		case ENetTransEvent_IPC_VDTP_Resp:
			// 文件传输消息
			if (IPC_VDTP_Msg_File == msg) {
				// 文件下载成功
				
				if (RESULE_SUCESS == param1) {
					if (TextUtils.isEmpty((String) (param2))) {
						return;
					}
					try {
						JSONObject json = new JSONObject((String) param2);
						if (null != json) {
							String filename = json.optString("filename");
							String tag = json.optString("tag");

							if (tag.contains("IPC_IMAGE")) {
								int type = IpcDataParser.parseVideoFileType(filename);
								if (type != IPCManagerFn.TYPE_SHORTCUT) {
									return;
								}

								if (null != mCloudWonderfulVideoAdapter) {
									mCloudWonderfulVideoAdapter.updateImage(filename);
								}

							} else {
								GolukDebugUtils.e("xuhw","TTT======no filelist  file======filename="+ filename);
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