package cn.com.mobnote.golukmobile.videosuqare;


import cn.com.mobnote.golukmobile.R;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class VideoSquareActivity extends Activity implements OnClickListener{
	private VideoSquareAdapter mVideoSquareAdapter=null;
	private ViewPager mViewPager=null;
	private ImageView hot = null;
	private ImageView square = null;
	
	private Button mVideoList = null;
	private Button mTypeList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_main);
		
		mViewPager = (ViewPager)findViewById(R.id.mViewpager);
		mVideoSquareAdapter = new VideoSquareAdapter(this);
		mViewPager.setAdapter(mVideoSquareAdapter);
		mViewPager.setOnPageChangeListener(opcl);
		init();
		setListener();
	}
	
	private void init(){
		hot = (ImageView) findViewById(R.id.line_hot);
		square = (ImageView) findViewById(R.id.line_square);
		mVideoList = (Button) findViewById(R.id.mVideoList);
		mTypeList = (Button) findViewById(R.id.mTypeList);
	}
	
	private void setListener(){
		findViewById(R.id.mVideoList).setOnClickListener(this);
		findViewById(R.id.mTypeList).setOnClickListener(this);
		
	}
	
	private OnPageChangeListener opcl = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			updateState(arg0);
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void updateState(int type){
		
		if(0 == type){
			hot.setVisibility(View.VISIBLE);
			square.setVisibility(View.INVISIBLE);
			
			mVideoList.setTextColor(getResources().getColor(R.color.textcolor_select));
			mTypeList.setTextColor(getResources().getColor(R.color.textcolor_qx));
		}else{
			hot.setVisibility(View.INVISIBLE);
			square.setVisibility(View.VISIBLE);
			
			mVideoList.setTextColor(getResources().getColor(R.color.textcolor_qx));
			mTypeList.setTextColor(getResources().getColor(R.color.textcolor_select));
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.mVideoList:
				mViewPager.setCurrentItem(0);
				this.updateState(0);
				break;
			case R.id.mTypeList:
				mViewPager.setCurrentItem(1);
				this.updateState(1);
				break;
	
			default:
				break;
		}
	}
	
}
