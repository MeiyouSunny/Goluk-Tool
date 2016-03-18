package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.tiros.debug.GolukDebugUtils;

public class SettingsItemActivity extends CarRecordBaseActivity implements OnClickListener {

	private RelativeLayout mFirstLayout, mSecondLayout, mThirdLayout;
	private TextView mFirstText, mSecondText, mThirdText;
	private ImageButton mFirstBtn, mSecondBtn, mThirdBtn;
	public static final String TYPE = "type";
	public static final String TYPE_WONDERFUL_VIDEO_QUALITY = "wonderful_video_quality";
	public static final String TYPE_TONE_VOLUME = "tone_volume";
	public static final String TYPE_SHUTDOWN_TIME = "shutdown_time";
	public static final String TYPE_LANGUAGE = "language";
	private TextView[] mTextViewList = null;
	private String[] mWonderfulList, mToneList, mShutdownList, mLanguageList;
	private ImageButton[] mImageList = null;
	private String mCurrentItem = "";
	private String mType = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.activity_settings_item_layout, null));

		initView();
		initData();
		setData2UI(getBtnList(), getTextList(), getTypeList());
	}

	private void initView() {
		mFirstLayout = (RelativeLayout) findViewById(R.id.rl_settings_first_item);
		mSecondLayout = (RelativeLayout) findViewById(R.id.rl_settings_second_item);
		mThirdLayout = (RelativeLayout) findViewById(R.id.rl_settings_third_item);
		mFirstText = (TextView) findViewById(R.id.tv_settings_first);
		mSecondText = (TextView) findViewById(R.id.tv_settings_second);
		mThirdText = (TextView) findViewById(R.id.tv_settings_third);
		mFirstBtn = (ImageButton) findViewById(R.id.ib_settings_first);
		mSecondBtn = (ImageButton) findViewById(R.id.ib_settings_second);
		mThirdBtn = (ImageButton) findViewById(R.id.ib_settings_third);

		mFirstLayout.setOnClickListener(this);
		mSecondLayout.setOnClickListener(this);
		mThirdLayout.setOnClickListener(this);

		mWonderfulList = getResources().getStringArray(R.array.list_wonderful_video_quality);
		mToneList = getResources().getStringArray(R.array.list_tone_volume);
		mShutdownList = getResources().getStringArray(R.array.list_shutdown_time);
		mLanguageList = getResources().getStringArray(R.array.list_language);
	}

	private void initData() {
		Intent it = getIntent();
		mType = it.getStringExtra(TYPE);
		getArrays(getTypeList());
	}

	// 为UI赋值
	private void getArrays(String[] mArrayText) {
		if (null != mArrayText) {
			int length = mArrayText.length;
			for (int i = 0; i < length; i++) {
				for (int j = i; j < mTextViewList.length; j++) {
					mTextViewList[j].setText(mArrayText[i]);
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			exit();
			break;
		case R.id.rl_settings_first_item:
			this.mCurrentItem = getTypeList()[0];
			setData2UI(getBtnList(), getTextList(), getTypeList());
			break;
		case R.id.rl_settings_second_item:
			this.mCurrentItem = getTypeList()[1];
			setData2UI(getBtnList(), getTextList(), getTypeList());
			break;
		case R.id.rl_settings_third_item:
			this.mCurrentItem = getTypeList()[2];
			setData2UI(getBtnList(), getTextList(), getTypeList());
			break;

		default:
			break;
		}
	}
	
	// 遍历分辨率，区分码率，改变UI
	private void setData2UI(ImageButton[] mImageIcon, TextView[] mText, String[] mArray) {
		final int length = mArray.length;
		for (int i = 0; i < length; i++) {
			mImageIcon[i].setVisibility(View.GONE);
			mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_nor));
			if (mCurrentItem.equals(mArray[i])) {
				mImageIcon[i].setVisibility(View.VISIBLE);
				mText[i].setTextColor(getResources().getColor(R.color.setting_text_color_sel));
			}
		}
	}
	
	private String[] getTypeList() {
		String[] arrayList;
		if (TYPE_WONDERFUL_VIDEO_QUALITY.equals(mType)) {
			mThirdLayout.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_wonderful_video_quality_title));
			arrayList = mWonderfulList;
		} else if (TYPE_TONE_VOLUME.equals(mType)) {
			mThirdLayout.setVisibility(View.VISIBLE);
			setTitle(this.getResources().getString(R.string.str_settings_tone_title));
			arrayList = mToneList;
		} else if (TYPE_SHUTDOWN_TIME.equals(mType)) {
			mThirdLayout.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_settings_shutdown_title));
			arrayList = mShutdownList;
		} else {
			mThirdLayout.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_settings_language_title));
			arrayList = mLanguageList;
		}
		getTextList();
		return arrayList;
	}
	
	private TextView[] getTextList() {
		if (TYPE_WONDERFUL_VIDEO_QUALITY.equals(mType)) {
			mTextViewList = new TextView[] { mFirstText, mSecondText };
		} else if (TYPE_TONE_VOLUME.equals(mType)) {
			mTextViewList = new TextView[] { mFirstText, mSecondText, mThirdText };
		} else if (TYPE_SHUTDOWN_TIME.equals(mType)) {
			mTextViewList = new TextView[] { mFirstText, mSecondText };
		} else {
			mTextViewList = new TextView[] { mFirstText, mSecondText };
		}
		return mTextViewList;
	}
	
	private ImageButton[] getBtnList() {
		if (TYPE_WONDERFUL_VIDEO_QUALITY.equals(mType)) {
			mImageList = new ImageButton[] { mFirstBtn, mSecondBtn };
		} else if (TYPE_TONE_VOLUME.equals(mType)) {
			mImageList = new ImageButton[] { mFirstBtn, mSecondBtn, mThirdBtn };
		} else if (TYPE_SHUTDOWN_TIME.equals(mType)) {
			mImageList = new ImageButton[] { mFirstBtn, mSecondBtn };
		} else {
			mImageList = new ImageButton[] { mFirstBtn, mSecondBtn };
		}
		return mImageList;
	}
		
	private void exit() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			Intent intent = new Intent();
			intent.putExtra("photoselect", mCurrentItem);
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
	
}
