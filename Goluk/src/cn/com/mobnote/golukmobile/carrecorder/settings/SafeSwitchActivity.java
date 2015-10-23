package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;

public class SafeSwitchActivity extends BaseActivity implements OnClickListener {

	/** title **/
	private ImageButton mImageBack = null;
	private TextView mTitleText = null;
	/** content **/
	private RelativeLayout mAllLayout, mMoveLayout, mVibrationLayout, mCloseLayout;
	private TextView mAllText, mMoveText, mVibrationText, mCloseText;
	private ImageButton mAllImage, mMoveImage, mVibrationImage, mCloseImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_tcaf_layout);

		initView();
		setListener();
	}

	// 初始化view
	private void initView() {
		mImageBack = (ImageButton) findViewById(R.id.back_btn);
		mTitleText = (TextView) findViewById(R.id.title);

		mAllLayout = (RelativeLayout) findViewById(R.id.all);
		mMoveLayout = (RelativeLayout) findViewById(R.id.move);
		mVibrationLayout = (RelativeLayout) findViewById(R.id.vibration);
		mCloseLayout = (RelativeLayout) findViewById(R.id.close);
		mAllText = (TextView) findViewById(R.id.allText);
		mMoveText = (TextView) findViewById(R.id.moveText);
		mVibrationText = (TextView) findViewById(R.id.vibrationText);
		mCloseText = (TextView) findViewById(R.id.closeText);
		mAllImage = (ImageButton) findViewById(R.id.allRight_image);
		mMoveImage = (ImageButton) findViewById(R.id.moveRight_image);
		mVibrationImage = (ImageButton) findViewById(R.id.vibrationRight_image);
		mCloseImage = (ImageButton) findViewById(R.id.closeRight_image);
	}

	// 监听
	private void setListener() {
		mAllLayout.setOnClickListener(this);
		mMoveLayout.setOnClickListener(this);
		mVibrationLayout.setOnClickListener(this);
		mCloseLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.all:

			break;
		case R.id.move:

			break;
		case R.id.vibration:

			break;
		case R.id.close:

			break;
		default:
			break;
		}
	}

}
