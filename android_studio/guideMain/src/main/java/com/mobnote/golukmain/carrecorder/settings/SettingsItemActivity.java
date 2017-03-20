package com.mobnote.golukmain.carrecorder.settings;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;

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
import cn.com.tiros.debug.GolukDebugUtils;

public class SettingsItemActivity extends CarRecordBaseActivity implements OnClickListener {

	private RelativeLayout mFirstLayout, mSecondLayout, mThirdLayout;
	private TextView mFirstText, mSecondText, mThirdText;
	private ImageButton mFirstBtn, mSecondBtn, mThirdBtn;
	private TextView mWonderfulVideoTypeText;
	public static final String TYPE = "type";
	public static final String PARAM = "param";
	public static final String TYPE_WONDERFUL_VIDEO_QUALITY = "wonderful_video_quality";
	public static final String TYPE_TONE_VOLUME = "tone_volume";
	public static final String TYPE_SHUTDOWN_TIME = "shutdown_time";
	public static final String TYPE_LANGUAGE = "language";
	public static final String TYPE_LANGUAGE_T = "language_t";
	public static final String TYPE_WONDERFUL_VIDEO_TYPE = "wonderful_video_type";
	public static final String TYPE_ANTI_FLICKER = "anti_flicker";
	private TextView[] mTextViewList = null;
	private String[] mWonderfulList, mVolumeList, mShutdownList, mLanguageList;
	private String[] mVolumeValue, mWonderfulValue,mAntiFlickerList;
	private ImageButton[] mImageList = null;
	private String mCurrentItem = "";
	private String mType = "";
	/**精彩视频类型**/
	private String[] mWonderfulVideoTypeList, mWonderfulVideoTypeValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.activity_settings_item_layout, null));

		initView();
		initData();
		setData2UI(getBtnList(), getTextList(), getTypeList(0));
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
		mWonderfulVideoTypeText = (TextView) findViewById(R.id.tv_settings_wonderful_video_type_hint_desc);

		mFirstLayout.setOnClickListener(this);
		mSecondLayout.setOnClickListener(this);
		mThirdLayout.setOnClickListener(this);

		mWonderfulList = getResources().getStringArray(R.array.list_wonderful_video_quality);
		mVolumeList = getResources().getStringArray(R.array.list_tone_volume);
		mShutdownList = getResources().getStringArray(R.array.list_shutdown_time);
		mLanguageList = getResources().getStringArray(R.array.list_language);
		
		mWonderfulValue = getResources().getStringArray(R.array.list_wonderful_video_quality_value);
		mVolumeValue = getResources().getStringArray(R.array.list_tone_volume_value);
		
		mWonderfulVideoTypeList = getResources().getStringArray(R.array.list_wonderful_video_type);
		mWonderfulVideoTypeValue = getResources().getStringArray(R.array.list_wonderful_video_type_value);
		mAntiFlickerList = getResources().getStringArray(R.array.list_anti_flicker_value);
	}

	private void initData() {
		Intent it = getIntent();
		mType = it.getStringExtra(TYPE);
		mCurrentItem = it.getStringExtra(PARAM);
		GolukDebugUtils.e("", "------------SettingsItemActivity----------mCurrentItem: "+mCurrentItem);
		getArrays(getTypeList(1));
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
		int id = view.getId();
		if (id == R.id.back_btn) {
			exit();
		} else if (id == R.id.rl_settings_first_item) {
			this.mCurrentItem = getTypeList(0)[0];
			setData2UI(getBtnList(), getTextList(), getTypeList(0));
		} else if (id == R.id.rl_settings_second_item) {
			this.mCurrentItem = getTypeList(0)[1];
			setData2UI(getBtnList(), getTextList(), getTypeList(0));
		} else if (id == R.id.rl_settings_third_item) {
			this.mCurrentItem = getTypeList(0)[2];
			setData2UI(getBtnList(), getTextList(), getTypeList(0));
		}
		GolukDebugUtils.e("", "------------SettingsItemActivity-----onClick-----mCurrentItem: "+mCurrentItem);
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
	
	private String[] getTypeList(int valueType) {
		String[] arrayList;
		if (TYPE_WONDERFUL_VIDEO_QUALITY.equals(mType)) {
			mThirdLayout.setVisibility(View.GONE);
			mWonderfulVideoTypeText.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_wonderful_video_quality_title));
			if (0 == valueType) {
				arrayList = mWonderfulValue;
			} else {
				arrayList = mWonderfulList;
			}
		} else if (TYPE_TONE_VOLUME.equals(mType)) {
			mThirdLayout.setVisibility(View.VISIBLE);
			mWonderfulVideoTypeText.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_settings_tone_title));
			if (0 == valueType) {
				arrayList = mVolumeValue;
			} else {
				arrayList = mVolumeList;
			}
		} else if (TYPE_SHUTDOWN_TIME.equals(mType)) {
			mThirdLayout.setVisibility(View.GONE);
			mWonderfulVideoTypeText.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_settings_shutdown_title));
			if (0 == valueType) {
				arrayList = new String[] { "10", "60" };
			} else {
				arrayList = mShutdownList;
			}
		} else if (TYPE_LANGUAGE.equals(mType)) {
			mThirdLayout.setVisibility(View.GONE);
			mWonderfulVideoTypeText.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_settings_language_title));
			if (0 == valueType) {
				arrayList = new String[] { "0", "1" };
			} else {
				arrayList = mLanguageList;
			}
		} else if (TYPE_LANGUAGE_T.equals(mType)) {
			mThirdLayout.setVisibility(View.VISIBLE);
			mWonderfulVideoTypeText.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.str_settings_language_title));
			if (0 == valueType) {
				arrayList = new String[] { "0", "1" ,"2"};
			} else {
				arrayList = getResources().getStringArray(R.array.list_language_t);
			}
		} else if (TYPE_ANTI_FLICKER.equals(mType)) {
			mThirdLayout.setVisibility(View.GONE);
			mWonderfulVideoTypeText.setVisibility(View.GONE);
			setTitle(this.getResources().getString(R.string.setting_anti_flicker));
			if (0 == valueType) {
				arrayList = new String[] { "0", "1" };
			} else {
				arrayList = mAntiFlickerList;
			}
		} else {
			mThirdLayout.setVisibility(View.GONE);
			mWonderfulVideoTypeText.setVisibility(View.VISIBLE);
			setTitle(this.getResources().getString(R.string.str_wonderful_video_type_title));
			if (0 == valueType) {
				arrayList = mWonderfulVideoTypeValue;
			} else {
				arrayList = mWonderfulVideoTypeList;
			}
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
		} else if (TYPE_LANGUAGE.equals(mType)) {
			mTextViewList = new TextView[] { mFirstText, mSecondText };
		} else if (TYPE_LANGUAGE_T.equals(mType)) {
			mTextViewList = new TextView[] { mFirstText, mSecondText, mThirdText };
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
		} else if (TYPE_LANGUAGE.equals(mType)) {
			mImageList = new ImageButton[] { mFirstBtn, mSecondBtn };
		} else if (TYPE_LANGUAGE_T.equals(mType)) {
			mImageList = new ImageButton[] { mFirstBtn, mSecondBtn, mThirdBtn };
		} else {
			mImageList = new ImageButton[] { mFirstBtn, mSecondBtn };
		}
		return mImageList;
	}
		
	private void exit() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			Intent intent = new Intent();
			intent.putExtra("params", mCurrentItem);
			setResult(Activity.RESULT_OK, intent);
			GolukDebugUtils.e("", "------------SettingsItemActivity-----exit-----mCurrentItem: "+mCurrentItem);
			
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
