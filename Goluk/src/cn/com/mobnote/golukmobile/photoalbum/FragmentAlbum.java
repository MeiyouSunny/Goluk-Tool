package cn.com.mobnote.golukmobile.photoalbum;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnRightClickListener;
import cn.com.mobnote.golukmobile.promotion.PromotionSelectItem;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.GolukUtils;

public class FragmentAlbum extends Fragment implements OnClickListener{
	
	/** 活动分享 */
	public static final String ACTIVITY_INFO = "activityinfo";
	
	private CustomViewPager mViewPager;
	private LocalFragment mLocalFragment;
	private WonderfulFragment mWonderfulFragment;
	private LoopFragment mLoopFragment;
	private UrgentFragment mUrgentFragment;
	
	private LinearLayout mLinearLayoutTab;

	// 四个tab
	private TextView mTabLocal;
	private TextView mTabWonderful;
	private TextView mTabUrgent;
	private TextView mTabLoop;
	
	private Button mCancelBtn;

	private ImageView mEditBtn;
	private RelativeLayout mEditLayout = null;
	private TextView mTitleName = null;
	
	private ImageView mDownLoadIcon = null;
	private ImageView mDeleteIcon = null;
	private ImageView mBackBtn = null;

	public int mCurrentType = 0;

	// 页面列表
	private ArrayList<Fragment> fragmentList;

	private View mAlbumRootView;
	
	private boolean editState = false;
	
	private LinearLayout mDownLoadBtn = null;
	
	private List<String> selectedListData = null;
	
	public String mPlatform = null;
	
	public PromotionSelectItem mPromotionSelectItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		Bundle bundle = getArguments();
		if(bundle != null){
			mPlatform = bundle.getString("platform");
		}
		
		if (savedInstanceState == null) {
			mPromotionSelectItem = (PromotionSelectItem) getActivity().getIntent().getSerializableExtra(ACTIVITY_INFO);
		} else {
			mPromotionSelectItem = (PromotionSelectItem) savedInstanceState.getSerializable(ACTIVITY_INFO);
		}
		
		if(mAlbumRootView == null){
			mAlbumRootView = inflater.inflate(R.layout.photo_album, null);

			editState = false;
			mViewPager = (CustomViewPager) mAlbumRootView.findViewById(R.id.viewpager);
			mViewPager.setOffscreenPageLimit(1);
			mLocalFragment = new LocalFragment();
			mWonderfulFragment = new WonderfulFragment(); //WonderfulFragment.newInstance(IPCManagerFn.TYPE_SHORTCUT, IPCManagerFn.TYPE_SHORTCUT);
			mLoopFragment = new  LoopFragment();//newInstance(IPCManagerFn.TYPE_CIRCULATE, IPCManagerFn.TYPE_CIRCULATE);
			mUrgentFragment = new UrgentFragment(); //newInstance(IPCManagerFn.TYPE_URGENT, IPCManagerFn.TYPE_URGENT);
			selectedListData = new ArrayList<String>();

			fragmentList = new ArrayList<Fragment>();
			fragmentList.add(mLocalFragment);
			fragmentList.add(mWonderfulFragment);
			fragmentList.add(mUrgentFragment);
			fragmentList.add(mLoopFragment);
			initView();
			mViewPager.setAdapter(new MyViewPagerAdapter(getChildFragmentManager()));
			mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int position) {
					mCurrentType = position;
					setItemLineState(position);
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {

				}
			});
			
			mLocalFragment.loadData(false);
		}
		
		ViewGroup parent = (ViewGroup) mAlbumRootView.getParent();
		if(parent != null){
			parent.removeView(mAlbumRootView);
		}

		return mAlbumRootView;
	}

	public void initView() {
		mTabLocal = (TextView) mAlbumRootView.findViewById(R.id.tab_local);
		mTabWonderful = (TextView) mAlbumRootView.findViewById(R.id.tab_wonderful);
		mTabUrgent = (TextView) mAlbumRootView.findViewById(R.id.tab_urgent);
		mTabLoop = (TextView) mAlbumRootView.findViewById(R.id.tab_loop);
		mEditBtn = (ImageView) mAlbumRootView.findViewById(R.id.edit_btn);
		mEditLayout = (RelativeLayout) mAlbumRootView.findViewById(R.id.mEditLayout);
		mTitleName = (TextView) mAlbumRootView.findViewById(R.id.video_title_text);
		mLinearLayoutTab = (LinearLayout) mAlbumRootView.findViewById(R.id.tab_type);
		mDownLoadBtn = (LinearLayout) mAlbumRootView.findViewById(R.id.mDownLoadBtn);
		
		mDownLoadIcon = (ImageView) mAlbumRootView.findViewById(R.id.mDownLoadIcon);
		mDeleteIcon = (ImageView) mAlbumRootView.findViewById(R.id.mDeleteIcon);
		mCancelBtn = (Button) mAlbumRootView.findViewById(R.id.cancel_btn);
		mBackBtn  = (ImageView) mAlbumRootView.findViewById(R.id.back_btn);
		
		if("0".equals(mPlatform)){
			mBackBtn.setVisibility(View.GONE);
		}else{
			mBackBtn.setVisibility(View.VISIBLE);
		}

		mCancelBtn.setOnClickListener(this);
		mDownLoadBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		mAlbumRootView.findViewById(R.id.mDeleteBtn).setOnClickListener(this);
		mTabLocal.setOnClickListener(this);
		mTabWonderful.setOnClickListener(this);
		mTabUrgent.setOnClickListener(this);
		mTabLoop.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		GolukApplication.getInstance().setContext(getContext(), "ipcfilemanager");
	}
	/**
	 * 设置tab页的下划线显示和隐藏
	 * 
	 * @param position
	 */
	public void setItemLineState(int position) {
		mTabLocal.setTextColor(this.getResources().getColor(R.color.wonderful_item_nor_color));
		mTabWonderful.setTextColor(this.getResources().getColor(R.color.wonderful_item_nor_color));
		mTabUrgent.setTextColor(this.getResources().getColor(R.color.wonderful_item_nor_color));
		mTabLoop.setTextColor(this.getResources().getColor(R.color.wonderful_item_nor_color));
		if (position == 0) {
			mLocalFragment.loadData(true);
			mTabLocal.setTextColor(this.getResources().getColor(R.color.wonderful_item_sel_color));
		} else if (position == 1) {
			mWonderfulFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
			mTabWonderful.setTextColor(this.getResources().getColor(R.color.wonderful_item_sel_color));
		} else if (position == 2) {
			mUrgentFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
			mTabUrgent.setTextColor(this.getResources().getColor(R.color.wonderful_item_sel_color));
		} else if (position == 3) {
			mLoopFragment.loadData(GolukApplication.getInstance().isIpcLoginSuccess);
			mTabLoop.setTextColor(this.getResources().getColor(R.color.wonderful_item_sel_color));
		}
	}

	public class MyViewPagerAdapter extends FragmentPagerAdapter {
		public MyViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}

	}
	
	private void downloadVideoFlush() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			if (mCurrentType != 0) {
				if(mCurrentType == 1){
					mWonderfulFragment.downloadVideoFlush(selectedListData);
				}else if(mCurrentType == 2){
					mUrgentFragment.downloadVideoFlush(selectedListData);
				}else if(mCurrentType == 3){
					mLoopFragment.downloadVideoFlush(selectedListData);
				}
				
			}
		} else {
			GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_check_ipc_state));
		}
		resetEditState();
	}

	private void resetEditState() {
		mViewPager.setCanScroll(true);
		mEditBtn.setVisibility(View.VISIBLE);
		mCancelBtn.setVisibility(View.GONE);
		mLinearLayoutTab.setVisibility(View.VISIBLE);
		mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon);
		mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_icon);
		editState = false;
		mTitleName.setVisibility(View.GONE);
		mEditLayout.setVisibility(View.GONE);
		selectedListData.clear();

	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.tab_local:
			mViewPager.setCurrentItem(0);
			mCurrentType = 0;
			break;
		case R.id.tab_wonderful:
			mViewPager.setCurrentItem(1);
			mCurrentType = 1;
			break;
		case R.id.tab_urgent:
			mViewPager.setCurrentItem(2);
			mCurrentType = 2;
			break;
		case R.id.tab_loop:
			mViewPager.setCurrentItem(3);
			mCurrentType = 3;
			break;
		case R.id.edit_btn:
			mViewPager.setCanScroll(false);
			updateEditState();
			break;
		case R.id.mDownLoadBtn:
			if (selectedListData.size() <= 0) {
				return;
			}

			downloadVideoFlush();
			break;
		case R.id.cancel_btn:
			resetEditState();
			break;
		case R.id.back_btn:
			getActivity().finish();
			break;
		case R.id.mDeleteBtn:
			if (selectedListData.size() <= 0) {
				return;
			}

			if (mCurrentType != 0) {
				if (!isAllowedDelete()) {
					GolukUtils.showToast(getActivity(), getResources().getString(R.string.str_photo_downing));
					return;
				}

				if (!GolukApplication.getInstance().getIpcIsLogin()) {
					resetEditState();
					GolukUtils.showToast(getActivity(),
							getResources().getString(R.string.str_photo_check_ipc_state));
					return;
				}
			}

			CustomDialog mCustomDialog = new CustomDialog(getActivity());
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
	
	private void deleteDataFlush() {
		
		if (mCurrentType != 0) {
			if(mCurrentType == 1){
				mWonderfulFragment.deleteListData(selectedListData);
			}else if(mCurrentType == 2){
				mUrgentFragment.deleteListData(selectedListData);
			}else if(mCurrentType == 3){
				mLoopFragment.deleteListData(selectedListData);
			}
		}else{
			mLocalFragment.deleteListData(selectedListData);
		}

		resetEditState();
		GolukUtils.showToast(this.getActivity(), getResources().getString(R.string.str_photo_delete_ok));
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
	
	private void updateEditState() {
		if (editState == false) {
			editState = true;
			
			//mEditBtn.setText(this.getResources().getString(R.string.short_input_cancel));
			mTitleName.setVisibility(View.VISIBLE);
			mCancelBtn.setVisibility(View.VISIBLE);
			mEditBtn.setVisibility(View.GONE);
			
			mTitleName.setText(this.getResources().getString(R.string.local_video_title_text));

			mLinearLayoutTab.setVisibility(View.GONE);
			mEditLayout.setVisibility(View.VISIBLE);
			
			if(mCurrentType == 0){
				mDownLoadBtn.setVisibility(View.GONE);
			}else if(mCurrentType == 1){
				mDownLoadBtn.setVisibility(View.VISIBLE);
			}else if(mCurrentType == 2){
				mDownLoadBtn.setVisibility(View.VISIBLE);
			}else if(mCurrentType == 3){
				mDownLoadBtn.setVisibility(View.VISIBLE);
			}
			/*if (R.id.mLocalVideoBtn == curId) {
				mLocalVideoListView.hideTopLaoyout();
				
			} else if (R.id.mCloudVideoBtn == curId) {
				mCloudVideoListView.hideTopLaoyout();
				
			}

			RelativeLayout.LayoutParams mainParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			mainParams.addRule(RelativeLayout.BELOW, R.id.title_layout);
			mainParams.addRule(RelativeLayout.ABOVE, R.id.mEditLayout);
			mMainLayout.setLayoutParams(mainParams);*/
		}
	}

	public boolean getEditState() {
		return editState;
	}
	
	public List<String> getSelectedList() {
		return selectedListData;
	}
	
	public void updateEditBtnState(boolean light) {
		/*if (light) {
			mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon_press);
			mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_press_icon);
		} else {
			mDownLoadIcon.setBackgroundResource(R.drawable.photo_download_icon);
			mDeleteIcon.setBackgroundResource(R.drawable.select_video_del_icon);
		}*/
	}
	
	
	public void updateTitleName(String titlename) {
		mTitleName.setText(titlename);
	}
	
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
	
	/**
	 * 获取当前选择的是否是本地视频标签
	 * 
	 * @return true/false 本地/远程
	 * @author jyf
	 */
	public boolean isLocalSelect() {
		/*if (curId == R.id.mLocalVideoBtn) {
			return true;
		}*/
		return false;
	}

}
