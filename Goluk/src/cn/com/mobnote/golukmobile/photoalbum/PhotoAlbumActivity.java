package cn.com.mobnote.golukmobile.photoalbum;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventPhotoUpdateDate;
import cn.com.mobnote.eventbus.EventPhotoUpdateLoginState;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnRightClickListener;
import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.GolukUtils;
import de.greenrobot.event.EventBus;

@SuppressLint("HandlerLeak")
public class PhotoAlbumActivity extends BaseActivity implements OnClickListener {
	public static final int UPDATELOGINSTATE = -1;
	public static final int UPDATEDATE = -2;

	private TextView mTitleName = null;
	private Button mEditBtn = null;
	private ImageView mCloudIcon = null;
	private TextView mCloudText = null;
	private ImageView mLocalIcon = null;
	private TextView mLocalText = null;
	private RelativeLayout mMainLayout = null;
	private String from = null;
	private LocalVideoListView mLocalVideoListView = null;
	private CloudVideoListView mCloudVideoListView = null;
	private boolean editState = false;
	/** 图片缓存cache */
	private LruCache<String, Bitmap> mLruCache = null;
	/** 表示当前选中的状态，本地 和 行车记录仪视频 */
	private int curId = -1;
	private RelativeLayout bottomLayout = null;
	private ImageButton mBackBtn = null;
	private RelativeLayout mEditLayout = null;
	private LinearLayout mDownLoadBtn = null;
	private List<String> selectedListData = null;
	private ImageView mDownLoadIcon = null;
	private ImageView mDeleteIcon = null;

	/** 活动分享 */
	public static final String ACTIVITY_INFO = "activityinfo";
	private PromotionSelectItem mPromotionSelectItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_album);

		if (savedInstanceState == null) {
			from = getIntent().getStringExtra("from");
			mPromotionSelectItem = (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO);
		} else {
			from = savedInstanceState.getString("from");
			mPromotionSelectItem = (PromotionSelectItem) savedInstanceState.getSerializable(ACTIVITY_INFO);
		}
		selectedListData = new ArrayList<String>();
		initCache();
		initView();
		setListener();
		mIsExit = false;
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mPromotionSelectItem != null) {
			outState.putSerializable(ACTIVITY_INFO, mPromotionSelectItem);
		}
		if (from != null) {
			outState.putSerializable("from", from);
		}
		super.onSaveInstanceState(outState);
	}

	private void initCache() {
		int maxSize = (int) (Runtime.getRuntime().maxMemory() / 6);
		mLruCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				if (bitmap == null) {
					return 0;
				}
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	public Bitmap getBitmap(String filename) {
		return mLruCache.get(filename);
	}

	public void putBitmap(String filename, Bitmap mBitmap) {
		mLruCache.put(filename, mBitmap);
	}

	private void initView() {
		bottomLayout = (RelativeLayout) findViewById(R.id.bottomLayout);
		mEditLayout = (RelativeLayout) findViewById(R.id.mEditLayout);
		mDownLoadBtn = (LinearLayout) findViewById(R.id.mDownLoadBtn);
		mMainLayout = (RelativeLayout) findViewById(R.id.mMainLayout);
		mTitleName = (TextView) findViewById(R.id.mTitleName);
		mEditBtn = (Button) findViewById(R.id.mEditBtn);
		mBackBtn = (ImageButton) findViewById(R.id.mBackBtn);
		mCloudIcon = (ImageView) findViewById(R.id.mCloudIcon);
		mLocalIcon = (ImageView) findViewById(R.id.mLocalIcon);
		mCloudText = (TextView) findViewById(R.id.mCloudText);
		mLocalText = (TextView) findViewById(R.id.mLocalText);
		mDownLoadIcon = (ImageView) findViewById(R.id.mDownLoadIcon);
		mDeleteIcon = (ImageView) findViewById(R.id.mDeleteIcon);
		setEditBtnState(false);
		updateBtnState(R.id.mLocalVideoBtn);
		updateLinkState();
	}

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
			mCloudText.setText("未连接记录仪");
			mCloudText.setTextColor(getResources().getColor(R.color.photoalbum_icon_color_gray));
		}
	}

	private void setListener() {
		findViewById(R.id.mBackBtn).setOnClickListener(this);
		findViewById(R.id.mEditBtn).setOnClickListener(this);
		findViewById(R.id.mLocalVideoBtn).setOnClickListener(this);
		findViewById(R.id.mCloudVideoBtn).setOnClickListener(this);
		findViewById(R.id.mDownLoadBtn).setOnClickListener(this);
		findViewById(R.id.mDeleteBtn).setOnClickListener(this);
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
				mLocalVideoListView = new LocalVideoListView(this, from, mPromotionSelectItem);
				mMainLayout.addView(mLocalVideoListView.getRootView());
			}
			mLocalVideoListView.show();
			mLocalVideoListView.updateEdit();
			if (null != mCloudVideoListView) {
				mMainLayout.removeView(mCloudVideoListView.getRootView());
				mCloudVideoListView = null;
				System.gc();
			}
			break;
		case R.id.mCloudVideoBtn:
			mCloudIcon.setBackgroundResource(R.drawable.my_cloud_press);
			mLocalIcon.setBackgroundResource(R.drawable.my_video);
			mCloudText.setTextColor(getResources().getColor(R.color.photoalbum_text_color));
			mLocalText.setTextColor(getResources().getColor(R.color.photoalbum_icon_color_gray));

			if (null == mCloudVideoListView) {
				mCloudVideoListView = new CloudVideoListView(this);
				mMainLayout.addView(mCloudVideoListView.getRootView());
			}
			mCloudVideoListView.show();
			mCloudVideoListView.updateEdit();
			if (null != mLocalVideoListView) {
				mMainLayout.removeView(mLocalVideoListView.getRootView());
				mLocalVideoListView = null;
				System.gc();
			}
			break;

		default:
			break;
		}
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
					GolukUtils.showToast(PhotoAlbumActivity.this, "视频正在下载，无法删除");
					return;
				}

				if (!GolukApplication.getInstance().getIpcIsLogin()) {
					resetEditState();
					GolukUtils.showToast(PhotoAlbumActivity.this, "请检查摄像头连接状态");
					return;
				}
			}

			CustomDialog mCustomDialog = new CustomDialog(this);
			mCustomDialog.setMessage("是否删除" + selectedListData.size() + "个视频？", Gravity.CENTER);
			mCustomDialog.setLeftButton("确认", new OnLeftClickListener() {
				@Override
				public void onClickListener() {
					deleteDataFlush();
				}
			});
			mCustomDialog.setRightButton("取消", new OnRightClickListener() {
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
		GolukUtils.showToast(PhotoAlbumActivity.this, "删除视频成功");
	}

	private void downloadVideoFlush() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			if (R.id.mCloudVideoBtn == curId) {
				mCloudVideoListView.downloadVideoFlush(selectedListData);
			}
		} else {
			GolukUtils.showToast(PhotoAlbumActivity.this, "请检查摄像头连接状态");
		}
		resetEditState();
	}

	public boolean getEditState() {
		return editState;
	}

	public List<String> getSelectedList() {
		return selectedListData;
	}

	/**
	 * 设置“编辑”按钮显示与隐藏, 在没数据时隐藏，有数据时显示
	 * 
	 * @param isShow
	 *            true/false 显示/隐藏
	 * @author jyf
	 */
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
			mEditBtn.setText("取消");
			mTitleName.setText("选择视频");

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
		mEditBtn.setText("编辑");
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

	/** 最后统一移除监听标识 */
	private int[] listener = { IPCManagerFn.TYPE_SHORTCUT, IPCManagerFn.TYPE_URGENT, IPCManagerFn.TYPE_CIRCULATE };
	/** 标记当前界面是否退出 */
	private boolean mIsExit = false;

	private void exit() {
		if (mIsExit) {
			return;
		}
		mIsExit = true;
		IPCControlManager ipcManageControl = GolukApplication.getInstance().getIPCControlManager();
		if (null != ipcManageControl) {
			final int length = listener.length;
			for (int i = 0; i < length; i++) {
				ipcManageControl.removeIPCManagerListener("filemanager" + listener[i]);
			}
		}
		finish();
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
	protected void onResume() {
		super.onResume();
		if (null != mLocalVideoListView) {
			mLocalVideoListView.onResume();
		}

		if (null != mCloudVideoListView) {
			mCloudVideoListView.onResume();
		}
		GolukApplication.getInstance().setContext(this, "ipcfilemanager");
	}

	@Override
	protected void onDestroy() {
		exit();
		if (null != mLruCache) {
			mLruCache.evictAll();
		}
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

}
