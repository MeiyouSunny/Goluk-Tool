package com.mobnote.golukmain.carrecorder.settings;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 
 * 图片质量设置页面
 *
 * 2015年12月14日
 *
 * @author jiayf
 */
@SuppressLint("InflateParams")
public class PhotoQualityActivity extends CarRecordBaseActivity implements OnClickListener {

	public static final String TAG = "PhotoQualityActivity";

	/** 视频类型文字显示 */
	private TextView mCloseText = null;
	private TextView mLowText = null;
	private TextView mMiddleText = null;
	/** 视频类型选中高亮 */
	private ImageButton mCloseIcon = null;
	private ImageButton mLowIcon = null;
	private ImageButton mMiddleIcon = null;

	/** UI显示 **/
	private String[] mArrayText = null;
	private String[] mArrayResolution = null;
	/** 文字显示 **/
	private TextView[] mText = null;
	/** 按钮显示 **/
	private ImageButton[] mImageIcon = null;
	private String mCurrentResolution;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_video_photo_quality, null));
		setTitle(this.getResources().getString(R.string.str_carrecoder_setting_photo_title));
		mCurrentResolution = getIntent().getStringExtra("photoselect");
		initView();
		setListener();
		setData2UI();
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年4月6日
	 */
	private void initView() {
		mCloseText = (TextView) findViewById(R.id.photo_quality_closeText);
		mCloseIcon = (ImageButton) findViewById(R.id.photo_quality_cRight);

		mLowText = (TextView) findViewById(R.id.photo_quality_lowText);
		mLowIcon = (ImageButton) findViewById(R.id.photo_quality_dRight);

		mMiddleText = (TextView) findViewById(R.id.photo_quality_middleText);
		mMiddleIcon = (ImageButton) findViewById(R.id.photo_quality_zRight);

		mArrayText = getResources().getStringArray(R.array.list_photo_quality_ui);
		mArrayResolution = getResources().getStringArray(R.array.list_photo_quality_list);

		mText = new TextView[] { mCloseText, mLowText, mMiddleText };
		mImageIcon = new ImageButton[] { mCloseIcon, mLowIcon, mMiddleIcon };

		if (null != mArrayText) {
			int length = mArrayText.length;
			for (int i = 0; i < length; i++) {
				for (int j = i; j < mText.length; j++) {
					mText[j].setText(mArrayText[i]);
				}
			}
		}
	}

	/**
	 * 设置控件监听事件
	 * 
	 * @author xuhw
	 * @date 2015年4月7日
	 */
	private void setListener() {
		findViewById(R.id.photo_quality_close).setOnClickListener(this);
		findViewById(R.id.photo_quality_low).setOnClickListener(this);
		findViewById(R.id.photo_quality_middle).setOnClickListener(this);

	}

	@Override
	protected void subExit() {
		exit();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		int id = v.getId();
		if (id == R.id.photo_quality_close) {
			this.mCurrentResolution = mArrayResolution[0];
			setData2UI();
		} else if (id == R.id.photo_quality_low) {
			this.mCurrentResolution = mArrayResolution[1];
			setData2UI();
		} else if (id == R.id.photo_quality_middle) {
			this.mCurrentResolution = mArrayResolution[2];
			setData2UI();
		} else {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukApplication.getInstance().setContext(this, TAG);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void exit() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			Intent intent = new Intent();
			intent.putExtra("photoselect", mCurrentResolution);
			setResult(Activity.RESULT_OK, intent);
		}
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	// 遍历分辨率，区分码率，改变UI
	private void setData2UI() {
		final int length = mArrayResolution.length;
		for (int i = 0; i < length; i++) {
			mImageIcon[i].setVisibility(View.GONE);
			mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_nor));
			if (mCurrentResolution.equals(mArrayResolution[i])) {
				mImageIcon[i].setVisibility(View.VISIBLE);
				mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_sel));
			}
		}
	}

}
