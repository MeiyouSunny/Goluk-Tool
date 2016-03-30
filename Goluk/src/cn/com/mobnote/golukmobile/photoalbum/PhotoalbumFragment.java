/*package cn.com.mobnote.golukmobile.photoalbum;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventIpcConnState;
import cn.com.mobnote.eventbus.EventPhotoUpdateDate;
import cn.com.mobnote.eventbus.EventPhotoUpdateLoginState;
import cn.com.mobnote.eventbus.EventPhotoalbumHandlerMessage;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnRightClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomFormatDialog;
import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import de.greenrobot.event.EventBus;

public class PhotoalbumFragment extends Fragment implements OnClickListener {
	
	public static final int UPDATELOGINSTATE = -1;
	public static final int UPDATEDATE = -2;
	*//** 返回主页的消息 *//*
	private static final int MSG_H_SHOWBACK = 1002;

	*//** 最后统一移除监听标识 *//*
	private final int[] listener = { IPCManagerFn.TYPE_SHORTCUT, IPCManagerFn.TYPE_URGENT, IPCManagerFn.TYPE_CIRCULATE };

	private TextView mTitleName = null;
	private Button mEditBtn = null;
	private ImageView mCloudIcon = null;
	private TextView mCloudText = null;
	private ImageView mLocalIcon = null;
	private TextView mLocalText = null;
	private RelativeLayout mMainLayout = null;
	private String from = null;
	private LocalVideoManager mLocalVideoListView = null;
	private CloudVideoManager mCloudVideoListView = null;
	private boolean editState = false;
	*//** 图片缓存cache *//*
//	private LruCache<String, Bitmap> mLruCache = null;
	*//** 表示当前选中的状态，本地 和 行车记录仪视频 *//*
	private int curId = -1;
	private RelativeLayout bottomLayout = null;
	private ImageButton mBackBtn = null;
	private RelativeLayout mEditLayout = null;
	private LinearLayout mDownLoadBtn = null;
	private List<String> selectedListData = null;
	private ImageView mDownLoadIcon = null;
	private ImageView mDeleteIcon = null;

	*//** 活动分享 *//*
	public static final String ACTIVITY_INFO = "activityinfo";
	private PromotionSelectItem mPromotionSelectItem;

	*//** 标记当前界面是否退出 *//*
	private boolean mIsExit = false;
	private CustomFormatDialog mConnectionDialog;
	private CustomDialog backHomeDialog;
	
	FragmentAlbum photoActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,  Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.photo_album, container, false);  
		selectedListData = new ArrayList<String>();
		initView(view);
		return view;
	}
	
	
	private void initView(View view) {
		bottomLayout = (RelativeLayout) view.findViewById(R.id.bottomLayout);
		mEditLayout = (RelativeLayout) view.findViewById(R.id.mEditLayout);
		mDownLoadBtn = (LinearLayout) view.findViewById(R.id.mDownLoadBtn);
		//mMainLayout = (RelativeLayout) view.findViewById(R.id.mMainLayout);
		mTitleName = (TextView) view.findViewById(R.id.mTitleName);
		//mEditBtn = (Button) view.findViewById(R.id.mEditBtn);
		//mBackBtn = (ImageButton) view.findViewById(R.id.mBackBtn);
		mCloudIcon = (ImageView) view.findViewById(R.id.mCloudIcon);
		mLocalIcon = (ImageView) view.findViewById(R.id.mLocalIcon);
		mCloudText = (TextView) view.findViewById(R.id.mCloudText);
		mLocalText = (TextView) view.findViewById(R.id.mLocalText);
		mDownLoadIcon = (ImageView) view.findViewById(R.id.mDownLoadIcon);
		mDeleteIcon = (ImageView) view.findViewById(R.id.mDeleteIcon);
		setListener(view);//设置监听
		setEditBtnState(false);
		updateBtnState(R.id.mLocalVideoBtn);
		updateLinkState();
	}

	private void setListener(View view) {
		mBackBtn.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);
		view.findViewById(R.id.mLocalVideoBtn).setOnClickListener(this);
		view.findViewById(R.id.mCloudVideoBtn).setOnClickListener(this);
		mDownLoadBtn.setOnClickListener(this);
		view.findViewById(R.id.mDeleteBtn).setOnClickListener(this);
	}
	
	
	*//**
	 * 获取当前选择的是否是本地视频标签
	 * 
	 * @return true/false 本地/远程
	 * @author jyf
	 *//*
	public boolean isLocalSelect() {
		if (curId == R.id.mLocalVideoBtn) {
			return true;
		}
		return false;
	}

	private void updateLinkState() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			mCloudIcon.setBackgroundResource(R.drawable.my_cloud);
			mCloudText.setText(getResources().getString(R.string.photoalbum_cloud_video_text));
		} else {
			mCloudIcon.setBackgroundResource(R.drawable.my_cloud_no_link);
			mCloudText.setText(this.getResources().getString(R.string.str_ipc_no_connect_str));
			mCloudText.setTextColor(getResources().getColor(R.color.photoalbum_icon_color_gray));
		}
	}

	private void updateBtnState(int id) {
		this.curId = id;
		switch (id) {
		case R.id.mLocalVideoBtn:
			mLocalIcon.setBackgroundResource(R.drawable.my_video_press);
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				mCloudIcon.setBackgroundResource(R.drawable.my_cloud);
			} else {
				mCloudIcon.setBackgroundResource(R.drawable.my_cloud_no_link);
			}
			mLocalText.setTextColor(getResources().getColor(R.color.photoalbum_text_color));
			mCloudText.setTextColor(getResources().getColor(R.color.photoalbum_icon_color_gray));

			if (null == mLocalVideoListView) {
				mLocalVideoListView = new LocalVideoManager(this.getContext(), from, mPromotionSelectItem);
				mMainLayout.addView(mLocalVideoListView.getRootView());
			}
			mLocalVideoListView.show();
			mLocalVideoListView.updateEdit();

			if(null != mCloudVideoListView) {
				mCloudVideoListView.hide();
			}
			GlideUtils.clearMemory(this.getContext());
			System.gc();
			break;
		case R.id.mCloudVideoBtn:
			mCloudIcon.setBackgroundResource(R.drawable.my_cloud_press);
			mLocalIcon.setBackgroundResource(R.drawable.my_video);
			mCloudText.setTextColor(getResources().getColor(R.color.photoalbum_text_color));
			mLocalText.setTextColor(getResources().getColor(R.color.photoalbum_icon_color_gray));

			if (null == mCloudVideoListView) {
				mCloudVideoListView = new CloudVideoManager(this.getContext());
				mMainLayout.addView(mCloudVideoListView.getRootView());
			}
			mCloudVideoListView.show();
			mCloudVideoListView.updateEdit();
			if(null != mLocalVideoListView) {
				mLocalVideoListView.hide();
			}
//			if (null != mLocalVideoListView) {
//				mMainLayout.removeView(mLocalVideoListView.getRootView());
//				mLocalVideoListView = null;
//				System.gc();
//			}
			GlideUtils.clearMemory(this.getContext());
			System.gc();
			break;
		default:
			break;
		}
	}

	protected void hMessage(Message msg) {
		if (MSG_H_SHOWBACK == msg.what) {
			showBackHomeDialog();
		}
	}

	public void showBackHomeDialog() {
		if (mIsExit) {
			return;
		}
		GolukApplication.getInstance().isconnection = false;
		// 关闭上一个dialog
		closeConnectionDialog();
		if (backHomeDialog != null && backHomeDialog.isShowing()) {
			return;
		}
		backHomeDialog = new CustomDialog(this.getContext());
		backHomeDialog.setMessage(getResources().getString(R.string.str_ipc_no_connect), Gravity.CENTER);
		backHomeDialog.setLeftButton(this.getResources().getString(R.string.str_button_ok), new OnLeftClickListener() {
			@Override
			public void onClickListener() {
				exit();
				closeBackHomeDialog();
			}
		});
		backHomeDialog.show();
	}

	private void closeBackHomeDialog() {
		if (null != backHomeDialog) {
			backHomeDialog.dismiss();
			backHomeDialog = null;
		}
	}

	private void showConnectionDialog() {
		if (mIsExit) {
			return;
		}
		if (mConnectionDialog != null && mConnectionDialog.isShowing()) {
			return;
		}
		mConnectionDialog = new CustomFormatDialog(this.getContext());
		mConnectionDialog.setCancelable(false);
		mConnectionDialog.setMessage(this.getResources().getString(R.string.str_ipc_disconnect));
		mConnectionDialog.show();
		// 计时10秒后弹出返回主页的对话框
		EventPhotoalbumHandlerMessage ephm = new EventPhotoalbumHandlerMessage();
		ephm.setCode(EventConfig.PHOTO_ALBUM_REMOVE_HANLDER);
		ephm.setMsg(MSG_H_SHOWBACK);
		EventBus.getDefault().post(ephm);
		
		ephm.setCode(EventConfig.PHOTO_ALBUM_DELAYED_HANLDER);
		ephm.setMsg(MSG_H_SHOWBACK);
		EventBus.getDefault().post(ephm);
	}
	
	public void closeConnectionDialog() {
		EventPhotoalbumHandlerMessage ephm = new EventPhotoalbumHandlerMessage();
		ephm.setCode(EventConfig.PHOTO_ALBUM_REMOVE_HANLDER);
		ephm.setMsg(MSG_H_SHOWBACK);
		EventBus.getDefault().post(ephm);
		
		if (mConnectionDialog != null) {
			if (mConnectionDialog.isShowing()) {
				mConnectionDialog.dismiss();
			}
		}
		mConnectionDialog = null;
	}

	public void updateTitleName(String titlename) {
		mTitleName.setText(titlename);
	}

	public void onEventMainThread(EventPhotoUpdateDate event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.PHOTO_ALBUM_UPDATE_DATE:
			if (null != mLocalVideoListView) {
				String filename = event.getMsg();
				if (null != filename && !filename.equals("")) {
					mLocalVideoListView.updateData(filename);
				}
			}
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventPhotoUpdateLoginState event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.PHOTO_ALBUM_UPDATE_LOGIN_STATE:
			updateLinkState();
			break;
		default:
			break;
		}
	}

	*//**
	 * 接受IPC断开或连接成功的消息
	 * 
	 * @param event
	 * @author jyf
	 *//*
	public void onEventMainThread(EventIpcConnState event) {
		if (null == event) {
			return;
		}
		switch (event.getmOpCode()) {
		case EventConfig.IPC_DISCONNECT:
			showConnectionDialog();
			break;
		case EventConfig.IPC_CONNECT:
			closeConnectionDialog();
			break;
		default:
			break;
		}
	}

	public void updateEditBtnState(boolean light) {
		if (light) {
			mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon_press);
			mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_press_icon);
		} else {
			mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon);
			mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_icon);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.mBackBtn:
			exit();
			break;
		case R.id.mEditBtn:
			updateEditState();
			break;
		case R.id.mLocalVideoBtn:
			updateBtnState(R.id.mLocalVideoBtn);
			break;
		case R.id.mCloudVideoBtn:
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				updateBtnState(R.id.mCloudVideoBtn);
			}
			break;
		case R.id.mDownLoadBtn:
			if (selectedListData.size() <= 0) {
				return;
			}

			downloadVideoFlush();
			break;
		case R.id.mDeleteBtn:
			if (selectedListData.size() <= 0) {
				return;
			}

			if (R.id.mCloudVideoBtn == curId) {
				if (!isAllowedDelete()) {
					GolukUtils.showToast(this.getContext(), getResources().getString(R.string.str_photo_downing));
					return;
				}

				if (!GolukApplication.getInstance().getIpcIsLogin()) {
					resetEditState();
					GolukUtils.showToast(this.getContext(),
							getResources().getString(R.string.str_photo_check_ipc_state));
					return;
				}
			}

			CustomDialog mCustomDialog = new CustomDialog(this.getContext());
			mCustomDialog.setMessage(
					getResources().getString(R.string.str_photo_deletepromote_1) + selectedListData.size()
							+ getResources().getString(R.string.str_photo_deletepromote_2), Gravity.CENTER);
			mCustomDialog.setLeftButton(getResources().getString(R.string.str_phote_delete_ok),
					new OnLeftClickListener() {
						@Override
						public void onClickListener() {
							deleteDataFlush();
						}
					});
			mCustomDialog.setRightButton(getResources().getString(R.string.dialog_str_cancel),
					new OnRightClickListener() {
						@Override
						public void onClickListener() {
							resetEditState();
						}
					});
			mCustomDialog.show();
			break;

		default:
			break;
		}
	}

	private boolean isAllowedDelete() {
		boolean downloading = true;
		List<String> dlist = GolukApplication.getInstance().getDownLoadList();
		for (String name : selectedListData) {
			if (dlist.contains(name)) {
				downloading = false;
				break;
			}
		}

		return downloading;
	}

	private void deleteDataFlush() {
		if (R.id.mLocalVideoBtn == curId) {
			mLocalVideoListView.deleteDataFlush(selectedListData);
		} else if (R.id.mCloudVideoBtn == curId) {
			mCloudVideoListView.deleteDataFlush(selectedListData);
		}

		resetEditState();
		GolukUtils.showToast(this.getContext(), getResources().getString(R.string.str_photo_delete_ok));
	}

	private void downloadVideoFlush() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			if (R.id.mCloudVideoBtn == curId) {
				mCloudVideoListView.downloadVideoFlush(selectedListData);
			}
		} else {
			GolukUtils.showToast(this.getContext(), getResources().getString(R.string.str_photo_check_ipc_state));
		}
		resetEditState();
	}

	public boolean getEditState() {
		return editState;
	}

	public List<String> getSelectedList() {
		return selectedListData;
	}

	*//**
	 * 设置“编辑”按钮显示与隐藏, 在没数据时隐藏，有数据时显示
	 * 
	 * @param isShow
	 *            true/false 显示/隐藏
	 * @author jyf
	 *//*
	public void setEditBtnState(boolean isShow) {
		if (null == mEditBtn) {
			return;
		}
		if (isShow) {
			mEditBtn.setVisibility(View.VISIBLE);
		} else {
			mEditBtn.setVisibility(View.GONE);
		}
	}

	private void updateEditState() {
		if (editState) {
			resetEditState();
		} else {
			editState = true;
			mEditBtn.setText(this.getResources().getString(R.string.short_input_cancel));
			mTitleName.setText(this.getResources().getString(R.string.local_video_title_text));

			mBackBtn.setVisibility(View.GONE);
			bottomLayout.setVisibility(View.GONE);
			mEditLayout.setVisibility(View.VISIBLE);
			if (R.id.mLocalVideoBtn == curId) {
				mLocalVideoListView.hideTopLaoyout();
				mDownLoadBtn.setVisibility(View.GONE);
			} else if (R.id.mCloudVideoBtn == curId) {
				mCloudVideoListView.hideTopLaoyout();
				mDownLoadBtn.setVisibility(View.VISIBLE);
			}

			RelativeLayout.LayoutParams mainParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			mainParams.addRule(RelativeLayout.BELOW, R.id.title_layout);
			mainParams.addRule(RelativeLayout.ABOVE, R.id.mEditLayout);
			mMainLayout.setLayoutParams(mainParams);
		}
	}

	private void resetEditState() {
		mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon);
		mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_icon);
		editState = false;
		mEditBtn.setText(this.getResources().getString(R.string.edit_text));
		mTitleName.setText(getResources().getString(R.string.photoalbum_default_title));
		selectedListData.clear();

		mBackBtn.setVisibility(View.VISIBLE);
		bottomLayout.setVisibility(View.VISIBLE);
		mEditLayout.setVisibility(View.GONE);

		if (R.id.mLocalVideoBtn == curId) {
			mLocalVideoListView.showTopLayout();
			mDownLoadBtn.setVisibility(View.GONE);
		} else if (R.id.mCloudVideoBtn == curId) {
			mCloudVideoListView.showTopLayout();
			mDownLoadBtn.setVisibility(View.VISIBLE);
		}

		RelativeLayout.LayoutParams mainParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mainParams.addRule(RelativeLayout.BELOW, R.id.title_layout);
		mainParams.addRule(RelativeLayout.ABOVE, R.id.bottomLayout);
		mMainLayout.setLayoutParams(mainParams);
	}

	private void exit() {
		if (mIsExit) {
			return;
		}
		mIsExit = true;
		
		EventPhotoalbumHandlerMessage ephm = new EventPhotoalbumHandlerMessage();
		ephm.setCode(EventConfig.PHOTO_ALBUM_REMOVE_HANLDER);
		ephm.setMsg(MSG_H_SHOWBACK);
		EventBus.getDefault().post(ephm);
		
		this.closeConnectionDialog();
		closeBackHomeDialog();

		IPCControlManager ipcManageControl = GolukApplication.getInstance().getIPCControlManager();
		if (null != ipcManageControl) {
			final int length = listener.length;
			for (int i = 0; i < length; i++) {
				ipcManageControl.removeIPCManagerListener("filemanager" + listener[i]);
			}
		}
		//finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (null != mLocalVideoListView) {
			mLocalVideoListView.onResume();
		}

		if (null != mCloudVideoListView) {
			mCloudVideoListView.onResume();
		}
		GolukApplication.getInstance().setContext(this.getContext(), "ipcfilemanager");
	}

	@Override
	public void onDestroy() {
		exit();
//		if (null != mLruCache) {
//			mLruCache.evictAll();
//		}
		if (null != mCloudVideoListView) {
			mCloudVideoListView.onDestroy();
		}
		GlideUtils.clearMemory(this.getContext());
		EventBus.getDefault().unregister(this.getContext());
		super.onDestroy();
	}
}
*/