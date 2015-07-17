package cn.com.mobnote.golukmobile.photoalbum;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CloudVideoListView implements OnClickListener{
	private View mRootLayout = null;
	private Context mContext = null;
	private String from = null;
	
	private TextView mWonderfulText = null;
	private TextView mEmergencyText = null;
	private TextView mLoopText = null;
	private ImageView mWonderfulLine = null;
	private ImageView mEmergencyLine = null;
	private ImageView mLoopLine = null;
	
	private CustomViewPager mViewPager = null;
	private CloudVideoAdapter mCloudVideoAdapter = null;
	private LinearLayout functionLayout = null;
	
	public CloudVideoListView(Context context, String from) {
		this.mContext = context;
		this.from = from;
		mRootLayout = LayoutInflater.from(context).inflate(R.layout.local_video_layout, null, false);
		initView();
	}
	
	private void initView() {
		functionLayout = (LinearLayout)mRootLayout.findViewById(R.id.functionLayout);
		mWonderfulText = (TextView)mRootLayout.findViewById(R.id.mWonderfulText);
		mEmergencyText = (TextView)mRootLayout.findViewById(R.id.mEmergencyText);
		mLoopText = (TextView)mRootLayout.findViewById(R.id.mLoopText);
		mWonderfulLine = (ImageView)mRootLayout.findViewById(R.id.mWonderfulLine);
		mEmergencyLine = (ImageView)mRootLayout.findViewById(R.id.mEmergencyLine);
		mLoopLine = (ImageView)mRootLayout.findViewById(R.id.mLoopLine);
		
		mViewPager = (CustomViewPager)mRootLayout.findViewById(R.id.mViewPager);
		mViewPager.setOffscreenPageLimit(3);
		mCloudVideoAdapter = new CloudVideoAdapter(mContext);
		mViewPager.setAdapter(mCloudVideoAdapter);

		setListener();
	}
	
	private void setListener() {
		mRootLayout.findViewById(R.id.mWonderfulVideo).setOnClickListener(this);
		mRootLayout.findViewById(R.id.mEmergencyVideo).setOnClickListener(this);
		mRootLayout.findViewById(R.id.mLoopVideo).setOnClickListener(this);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				if(0 == arg0) {
					updateTableState(R.id.mWonderfulVideo);
				}else if(1 == arg0) {
					updateTableState(R.id.mEmergencyVideo);
				}else {
					updateTableState(R.id.mLoopVideo);
				}
			}
				
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
				
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.mWonderfulVideo:
			mViewPager.setCurrentItem(0);
			updateTableState(R.id.mWonderfulVideo);
			break;
		case R.id.mEmergencyVideo:
			mViewPager.setCurrentItem(1);
			updateTableState(R.id.mEmergencyVideo);
			break;
		case R.id.mLoopVideo:
			mViewPager.setCurrentItem(2);
			updateTableState(R.id.mLoopVideo);
			break;

		default:
			break;
		}
	}
	
	private void updateTableState(int id) {
		mWonderfulText.setTextColor(mContext.getResources().getColor(R.color.photoalbum_title_bg_color));
		mEmergencyText.setTextColor(mContext.getResources().getColor(R.color.photoalbum_title_bg_color));
		mLoopText.setTextColor(mContext.getResources().getColor(R.color.photoalbum_title_bg_color));
		mWonderfulLine.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_title_bg_color));
		mEmergencyLine.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_title_bg_color));
		mLoopLine.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_title_bg_color));
		mWonderfulLine.setVisibility(View.INVISIBLE);
		mEmergencyLine.setVisibility(View.INVISIBLE);
		mLoopLine.setVisibility(View.INVISIBLE);
		
		switch (id) {
		case R.id.mWonderfulVideo:
			mWonderfulText.setTextColor(mContext.getResources().getColor(R.color.photoalbum_text_color));
			mWonderfulLine.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_text_color));
			mWonderfulLine.setVisibility(View.VISIBLE);
			break;
		case R.id.mEmergencyVideo:
			mEmergencyText.setTextColor(mContext.getResources().getColor(R.color.photoalbum_text_color));
			mEmergencyLine.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_text_color));
			mEmergencyLine.setVisibility(View.VISIBLE);
			break;
		case R.id.mLoopVideo:
			mLoopText.setTextColor(mContext.getResources().getColor(R.color.photoalbum_text_color));
			mLoopLine.setBackgroundColor(mContext.getResources().getColor(R.color.photoalbum_text_color));
			mLoopLine.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
	}
	
	public View getRootView() {
		return mRootLayout;
	}
	
	public void show() {
		mRootLayout.setVisibility(View.VISIBLE);
	}
	
	public void hide() {
		mRootLayout.setVisibility(View.GONE);
	}

	public void hideTopLaoyout() {
		mViewPager.setCanScroll(false);
		functionLayout.setVisibility(View.GONE);
	}
	
	public void showTopLayout() {
		mViewPager.setCanScroll(true);
		functionLayout.setVisibility(View.VISIBLE);
	}
	
	
	
}

