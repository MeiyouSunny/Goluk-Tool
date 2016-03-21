package cn.com.mobnote.golukmobile.photoalbum;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class PhotoAlbumActivity extends FragmentActivity implements OnClickListener{
	
	/** 活动分享 */
	public static final String ACTIVITY_INFO = "activityinfo";
	
	private ViewPager mViewPager;
	private LocalFragment mLocalFragment;
	private WonderfulFragment mWonderfulFragment;
	private LoopFragment mLoopFragment;
	private UrgentFragment mUrgentFragment;

	// 四个tab
	private TextView mTabLocal;
	private TextView mTabWonderful;
	private TextView mTabUrgent;
	private TextView mTabLoop;
	
	private ImageView mEditBtn;

	private int mCurrentType = 0;
	

	// 页面列表
	private ArrayList<Fragment> fragmentList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_album);

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setOffscreenPageLimit(1);
		mLocalFragment = new LocalFragment();
		mWonderfulFragment = new WonderfulFragment(); //WonderfulFragment.newInstance(IPCManagerFn.TYPE_SHORTCUT, IPCManagerFn.TYPE_SHORTCUT);
		mLoopFragment = new  LoopFragment();//newInstance(IPCManagerFn.TYPE_CIRCULATE, IPCManagerFn.TYPE_CIRCULATE);
		mUrgentFragment = new UrgentFragment(); //newInstance(IPCManagerFn.TYPE_URGENT, IPCManagerFn.TYPE_URGENT);

		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(mLocalFragment);
		fragmentList.add(mWonderfulFragment);
		fragmentList.add(mUrgentFragment);
		fragmentList.add(mLoopFragment);
		

		initView();
		mViewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
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
	}

	public void initView() {

		mTabLocal = (TextView) findViewById(R.id.tab_local);
		mTabWonderful = (TextView) findViewById(R.id.tab_wonderful);
		mTabUrgent = (TextView) findViewById(R.id.tab_urgent);
		mTabLoop = (TextView) findViewById(R.id.tab_loop);
		mEditBtn = (ImageView) findViewById(R.id.edit_btn);

		mTabLocal.setOnClickListener(this);
		mTabWonderful.setOnClickListener(this);
		mTabUrgent.setOnClickListener(this);
		mTabLoop.setOnClickListener(this);
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
			mTabLocal.setTextColor(this.getResources().getColor(R.color.wonderful_item_sel_color));
		} else if (position == 1) {
			mWonderfulFragment.loadData(true);
			mTabWonderful.setTextColor(this.getResources().getColor(R.color.wonderful_item_sel_color));
		} else if (position == 2) {
			mUrgentFragment.loadData(true);
			mTabUrgent.setTextColor(this.getResources().getColor(R.color.wonderful_item_sel_color));
		} else if (position == 3) {
			mLoopFragment.loadData(true);
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
		default:
			break;
		}
	}

	public boolean getEditState() {
		return false;
	}
	
	public List<String> getSelectedList() {
		return null;
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
//		mTitleName.setText(titlename);
	}
	
	public void setEditBtnState(boolean isShow) {
//		if (null == mEditBtn) {
//			return;
//		}
//		if (isShow) {
//			mEditBtn.setVisibility(View.VISIBLE);
//		} else {
//			mEditBtn.setVisibility(View.GONE);
//		}
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
