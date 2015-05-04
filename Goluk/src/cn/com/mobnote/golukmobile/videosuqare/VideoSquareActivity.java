package cn.com.mobnote.golukmobile.videosuqare;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.socialize.sso.UMSsoHandler;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class VideoSquareActivity extends Activity implements OnClickListener {
	private VideoSquareAdapter mVideoSquareAdapter = null;
	private ViewPager mViewPager = null;
	private ImageView hot = null;
	private ImageView square = null;
	private Button mVideoList = null;
	private Button mTypeList = null;
	public CustomLoadingDialog mCustomProgressDialog;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;

	SharePlatformUtil sharePlatform;

	public String shareVideoId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_main);

		mViewPager = (ViewPager) findViewById(R.id.mViewpager);
		mVideoSquareAdapter = new VideoSquareAdapter(this);
		mViewPager.setAdapter(mVideoSquareAdapter);
		mViewPager.setOnPageChangeListener(opcl);
		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数
		init();
		setListener();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = sharePlatform.mController.getConfig()
				.getSsoHandler(requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	private void init() {
		hot = (ImageView) findViewById(R.id.line_hot);
		square = (ImageView) findViewById(R.id.line_square);
		mVideoList = (Button) findViewById(R.id.mVideoList);
		mTypeList = (Button) findViewById(R.id.mTypeList);
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);

	}

	private void setListener() {
		mVideoList.setOnClickListener(this);
		mTypeList.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);

	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			Toast.makeText(VideoSquareActivity.this, "第三方分享失败",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(VideoSquareActivity.this, "开始第三方分享:" + channel,
				Toast.LENGTH_SHORT).show();
		
		System.out.println("shareid"+shareVideoId);
		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.shareVideoUp(channel,shareVideoId);
		System.out.println("shareid"+result);
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

	private void updateState(int type) {

		if (0 == type) {
			hot.setVisibility(View.VISIBLE);
			square.setVisibility(View.INVISIBLE);

			mVideoList.setTextColor(getResources().getColor(
					R.color.textcolor_select));
			mTypeList.setTextColor(getResources()
					.getColor(R.color.textcolor_qx));
		} else {
			hot.setVisibility(View.INVISIBLE);
			square.setVisibility(View.VISIBLE);

			mVideoList.setTextColor(getResources().getColor(
					R.color.textcolor_qx));
			mTypeList.setTextColor(getResources().getColor(
					R.color.textcolor_select));
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
		case R.id.back_btn:
			exit();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onResume();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onStop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void exit(){
		if (null != mVideoSquareAdapter) {
			mVideoSquareAdapter.onDestroy();
		}
		
		ImageLoader.getInstance().clearMemoryCache();
		
		finish();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode==KeyEvent.KEYCODE_BACK){
    		exit(); 
        	return true;
        }else
        	return super.onKeyDown(keyCode, event); 
	}

}
