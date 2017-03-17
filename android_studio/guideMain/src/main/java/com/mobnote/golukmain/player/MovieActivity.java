/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobnote.golukmain.player;

import java.io.File;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.util.GlideUtils;

import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

/**
 * This activity plays a video from a specified URI.
 *
 * The client of this activity can pass a logo bitmap in the intent
 * (KEY_LOGO_BITMAP) to set the action bar logo so the playback process looks
 * more seamlessly integrated with the original activity.
 */
public class MovieActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "MovieActivity";
	public static final String KEY_LOGO_BITMAP = "logo-bitmap";
	public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";

	private MoviePlayer mPlayer;
	private boolean mFinishOnCompletion;
	private Uri mUri;
	private boolean mTreatUpAsBack;
	/** 来源标志 */
	private String from;
	/** 视频第一帧图片地址 */
	private String mImageAddress = "";
	/** 文件名字 */
	private String filename = "";

	private String mVideoUrl = "";
	private ImageView mCoverImg = null;
	private CustomDialog mCustomDialog;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setSystemUiVisibility(View rootView) {
		if (ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) {
			rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.movie_view);
		View rootView = findViewById(R.id.movie_view_root);

		setSystemUiVisibility(rootView);

		Intent intent = getIntent();
		mFinishOnCompletion = intent.getBooleanExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, false);
		mTreatUpAsBack = intent.getBooleanExtra(KEY_TREAT_UP_AS_BACK, false);
		getPlayAddr();
		mCoverImg = (ImageView) findViewById(R.id.ImageView_cover);
		GlideUtils.loadImage(this, mCoverImg, mImageAddress, R.drawable.tacitly_pic);
		mPlayer = new MoviePlayer(rootView, this, Uri.parse(mVideoUrl), savedInstanceState, !mFinishOnCompletion) {

			@Override
			public void exit() {
				// TODO Auto-generated method stub
				finish();
			}

			@Override
			public boolean onError(int arg1) {
				// TODO Auto-generated method stub
				String msg = getString(R.string.str_play_error);
				switch (arg1) {
				case 1:
				case -1010:
					msg = getString(R.string.str_play_video_error);
					break;
				case -110:
					msg = getString(R.string.str_play_video_network_error);
					break;

				default:
					break;
				}

				if (!from.equals("local")) {
					if (!isNetworkConnected()) {
						msg = getString(R.string.str_play_video_network_error);
					}
				}

				dialog(msg);
				return true;
			}
		};
		if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
			int orientation = intent.getIntExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
					ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			if (orientation != getRequestedOrientation()) {
				setRequestedOrientation(orientation);
			}
		}
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
		winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		win.setAttributes(winParams);

		// We set the background in the theme to have the launching animation.
		// But for the performance (and battery), we remove the background here.
		win.setBackgroundDrawable(null);
	}

	/**
	 * 获取播放地址
	 * 
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void getPlayAddr() {
		from = getIntent().getStringExtra("from");
		mImageAddress = getIntent().getStringExtra("image");
		filename = getIntent().getStringExtra("filename");
		GolukDebugUtils.e(TAG, "YYYYYY==VideoPlayerActivity==2222===filename=" + filename + "===from=" + from);
		String ip = SettingUtils.getInstance().getString("IPC_IP");

		if (TextUtils.isEmpty(from)) {
			return;
		}

		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
				+ "goluk_carrecorder";
		GFileUtils.makedir(path);
		String filePath = path + File.separator + "image";
		GolukDebugUtils.e(TAG, "YYYYYY==VideoPlayerActivity==filePath=" + filePath);
		if (from.equals("local")) {
			mVideoUrl = getIntent().getStringExtra("path");
			String fileName = mVideoUrl.substring(mVideoUrl.lastIndexOf("/") + 1);
			fileName = fileName.replace(".mp4", ".jpg");
			mImageAddress = filePath + File.separator + fileName;
			GolukDebugUtils.e(TAG, "YYYYYY==VideoPlayerActivity==image=" + mImageAddress);
		} else if (from.equals("suqare")) {
			mVideoUrl = getIntent().getStringExtra("vurl");
		} else if (from.equals("ipc")) {
			if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)
					|| IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)) {
				String fileName = filename;
				String[] names = fileName.split("_");
				if (names.length > 3) {
					if (names[0].equals("NRM")) {
						fileName = names[0] + "_" + names[1];
					} else {
						fileName = names[0] + "_" + names[2];
					}
				}
				mVideoUrl = "http://" + ip + "/api/video?id=" + fileName;
				mImageAddress = "http://" + ip + "/api/thumb?id=" + fileName;
			} else {
				String fileName = filename;
				fileName = fileName.replace(".mp4", ".jpg");
				mImageAddress = filePath + File.separator + fileName;
				int type = getIntent().getIntExtra("type", -1);
				if (4 == type) {
					mVideoUrl = "http://" + ip + ":5080/rec/wonderful/" + filename;
				} else if (2 == type) {
					mVideoUrl = "http://" + ip + ":5080/rec/urgent/" + filename;
				} else {
					mVideoUrl = "http://" + ip + ":5080/rec/normal/" + filename;
				}
			}
		}

		GolukDebugUtils.e(TAG, "YYYYYY==VideoPlayerActivity==vurl=" + mVideoUrl);
	}

	@Override
	public void onStart() {
		((AudioManager) getSystemService(AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		super.onStart();
	}

	@Override
	protected void onStop() {
		((AudioManager) getSystemService(AUDIO_SERVICE)).abandonAudioFocus(null);
		super.onStop();
	}

	@Override
	public void onPause() {
		mPlayer.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		mPlayer.onResume();
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mPlayer.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		mPlayer.onDestroy();
		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
		mCustomDialog = null;
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mPlayer.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mPlayer.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
	}

	/**
	 * 提示对话框
	 * 
	 * @param msg
	 *            提示信息
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void dialog(String msg) {

		if (mCustomDialog == null) {
			mCustomDialog = new CustomDialog(this);
			mCustomDialog.setCancelable(true);
			mCustomDialog.setMessage(msg, Gravity.CENTER);
			mCustomDialog.setLeftButton(this.getString(R.string.str_button_ok), new OnLeftClickListener() {
				@Override
				public void onClickListener() {
					finish();
				}
			});
			mCustomDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface arg0) {
					finish();
				}
			});
		}
		mCustomDialog.show();
	}

	/**
	 * 检查是否有可用网络
	 * 
	 * @return
	 * @date 2015年6月5日
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}
}
