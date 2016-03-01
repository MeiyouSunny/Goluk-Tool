package cn.com.mobnote.golukmobile;

import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.util.GolukConfig;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoSyncSettingActivity extends BaseActivity implements OnClickListener {
	private TextView mSync5TV;
	private TextView mSync20TV;
	private TextView mSyncCloseTV;
	private ImageView mBackIV;
	private static final int SYNC_CLOSE = 0;
	private static final int SYNC_5 = 5;
	private static final int SYNC_20 = 20;
	private int mCurSyncMode = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_sync_setting);

		mSync5TV = (TextView)findViewById(R.id.tv_video_sync_setting_sync5);
		mSync5TV.setOnClickListener(this);
		mSync20TV = (TextView)findViewById(R.id.tv_video_sync_setting_sync20);
		mSync20TV.setOnClickListener(this);
		mSyncCloseTV = (TextView)findViewById(R.id.tv_video_sync_setting_sync_close);
		mSyncCloseTV.setOnClickListener(this);
		mBackIV = (ImageView)findViewById(R.id.iv_video_sync_setting_back_btn);
		mBackIV.setOnClickListener(this);

		mCurSyncMode = SettingUtils.getInstance().getInt(UserSetupActivity.MANUAL_SWITCH, -1);
		switch (mCurSyncMode) {
		case SYNC_5:
			refreshSelection(R.id.tv_video_sync_setting_sync5);
			break;
		case SYNC_20:
			refreshSelection(R.id.tv_video_sync_setting_sync20);
			break;
		case SYNC_CLOSE:
			refreshSelection(R.id.tv_video_sync_setting_sync_close);
			break;
		default:
			break;
		}
	}

	private void refreshSelection(int id) {
		mSync5TV.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		mSync20TV.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		mSyncCloseTV.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		if(id == R.id.tv_video_sync_setting_sync_close) {
			mSyncCloseTV.setCompoundDrawablesWithIntrinsicBounds(
					0, 0, R.drawable.setup_icon_hook, 0);
		}

		if(id == R.id.tv_video_sync_setting_sync5) {
			mSync5TV.setCompoundDrawablesWithIntrinsicBounds(
					0, 0, R.drawable.setup_icon_hook, 0);
		}

		if(id == R.id.tv_video_sync_setting_sync20) {
			mSync20TV.setCompoundDrawablesWithIntrinsicBounds(
					0, 0, R.drawable.setup_icon_hook, 0);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.iv_video_sync_setting_back_btn:
		{
			Intent intent = new Intent();
			intent.putExtra(GolukConfig.STRING_VIDEO_SYNC_SETTING_VALUE, mCurSyncMode);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
			break;
		case R.id.tv_video_sync_setting_sync20:
		{
			mCurSyncMode = SYNC_20;
			refreshSelection(R.id.tv_video_sync_setting_sync20);
			Intent intent = new Intent();
			intent.putExtra(GolukConfig.STRING_VIDEO_SYNC_SETTING_VALUE, mCurSyncMode);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
			break;
		case R.id.tv_video_sync_setting_sync5:
		{
			mCurSyncMode = SYNC_5;
			refreshSelection(R.id.tv_video_sync_setting_sync5);
			Intent intent = new Intent();
			intent.putExtra(GolukConfig.STRING_VIDEO_SYNC_SETTING_VALUE, mCurSyncMode);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
			break;
		case R.id.tv_video_sync_setting_sync_close:
		{
			mCurSyncMode = SYNC_CLOSE;
			refreshSelection(R.id.tv_video_sync_setting_sync_close);
			Intent intent = new Intent();
			intent.putExtra(GolukConfig.STRING_VIDEO_SYNC_SETTING_VALUE, mCurSyncMode);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent();
		intent.putExtra(GolukConfig.STRING_VIDEO_SYNC_SETTING_VALUE, mCurSyncMode);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}
