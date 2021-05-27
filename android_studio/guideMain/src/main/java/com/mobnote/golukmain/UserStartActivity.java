package com.mobnote.golukmain;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventStartApp;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomVideoView;
import com.mobnote.golukmain.xdpush.StartAppBean;
import com.mobnote.golukmobile.GuideActivity;
import com.mobnote.permission.GolukPermissionUtils;
import com.mobnote.permission.PrivacyDialog;

import java.util.List;

import androidx.annotation.NonNull;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 个人中心启动模块
 * <p>
 * 1、我有Goluk——跳转到登陆界面 2、随便看看——跳转到app主页
 *
 * @author mobnote
 */
public class UserStartActivity extends BaseActivity implements OnClickListener, OnErrorListener, PrivacyDialog.OnPrivacySelectListener, EasyPermissions.PermissionCallbacks {

    private Button mImageViewHave, mImageViewLook;
    //
    private Context mContext = null;
    private GolukApplication mApp = null;
    /**
     * 如果是注销进来的，需要将手机号填进去
     **/
    private SharedPreferences mPreferences = null;
    private String phone = null;

    public static final int EXIT = -1;
    private Editor mEditor = null;
    private Bitmap mBGBitmap = null;
    private CustomVideoView videoStart = null;
    private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
    private int screenHeight = SoundUtils.getInstance().getDisplayMetrics().heightPixels;
    /**
     * 我有Goluk和随便看看两个按钮
     **/
    private LinearLayout mClickLayout = null;
    /**
     * 欢迎页右上角关闭按钮
     **/
    private ImageView mImageClose = null;
    /**
     * true欢迎页 false开屏页
     **/
    private boolean judge = false;
    private StartAppBean mStartAppBean = null;

    public String VIDEO_URL;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.user_start);
        mBGBitmap = ImageManager.getBitmapFromResource(R.drawable.guide_page, screenWidth, screenHeight);
        RelativeLayout main = (RelativeLayout) findViewById(R.id.main);
        main.setBackgroundDrawable(new BitmapDrawable(mBGBitmap));

        EventBus.getDefault().register(this);

        mContext = this;
        mApp = (GolukApplication) getApplication();
        VIDEO_URL = "android.resource://" + getPakageName() + "/";

        // SysApplication.getInstance().addActivity(this);

        initView();
        // true ----欢迎页 false开屏页
        Intent it = getIntent();
        mStartAppBean = (StartAppBean) it.getSerializableExtra(GuideActivity.KEY_WEB_START);
        judge = it.getBooleanExtra("judgeVideo", false);
        GolukDebugUtils.e("lily", judge + "--------judgeVideo-----");
        if (judge) {
            mClickLayout.setVisibility(View.GONE);
            mImageClose.setVisibility(View.VISIBLE);
        } else {
            mClickLayout.setVisibility(View.VISIBLE);
            mImageClose.setVisibility(View.GONE);
        }
        videoStart = (CustomVideoView) findViewById(R.id.videoStart);
        videoStart.setVideoURI(Uri.parse(VIDEO_URL + R.raw.start_video));
        videoStart.start();
        videoStart.setOnErrorListener(this);
        videoStart.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);

            }
        });

        videoStart.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                videoStart.setVideoPath(VIDEO_URL + R.raw.start_video);
                videoStart.start();

            }
        });

        // 隐私政策Dialog
        new PrivacyDialog().showClauseDialog(this, this);
    }

    private void addWebStartData(Intent intent) {
        if (null != this.mStartAppBean) {
            intent.putExtra(GuideActivity.KEY_WEB_START, mStartAppBean);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.setContext(mContext, "UserStart");
    }

    public void initView() {
        mClickLayout = (LinearLayout) findViewById(R.id.user_start_click);
        mImageViewHave = (Button) findViewById(R.id.user_start_have);
        mImageViewLook = (Button) findViewById(R.id.user_start_look);
        mImageClose = (ImageView) findViewById(R.id.click_close_btn);
        // 获取注销成功后传来的信息
        mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
        phone = mPreferences.getString("setupPhone", "");// 最后一个参数为默认值

        mImageViewHave.setOnClickListener(this);
        mImageViewLook.setOnClickListener(this);
        mImageClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.user_start_have) {
            // 我有Goluk
            Intent itHave = new Intent(UserStartActivity.this, MainActivity.class);
            itHave.putExtra("userstart", "start_have");
            startActivity(itHave);
            this.stopVideo();
            this.finish();
        } else if (id == R.id.user_start_look) {
            // 随便看看
            Intent itLook = new Intent(UserStartActivity.this, MainActivity.class);
            GolukDebugUtils.i("lily", "======MainActivity==UserStartActivity====");
            addWebStartData(itLook);
            startActivity(itLook);
            this.stopVideo();
            this.finish();
        } else if (id == R.id.click_close_btn) {
            this.stopVideo();
            finish();
        }
    }

    /**
     * 停止video的播放
     */
    private void stopVideo() {
        if (videoStart != null) {
            if (videoStart.isPlaying()) {
                videoStart.stopPlayback();
                videoStart = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (null != mBGBitmap) {
            if (!mBGBitmap.isRecycled()) {
                mBGBitmap.recycle();
                mBGBitmap = null;
            }
        }
    }

    public void onEventMainThread(EventStartApp app) {
        if (100 == app.mCode) {
            exit();
        }
    }

    private void exit() {
        this.stopVideo();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!judge) {
                if (null != mBaseApp) {
                    mBaseApp.setExit(true);
                    mBaseApp.destroyLogic();
                    mBaseApp.appFree();
                }
            }
            finish();
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
        return true;
    }

    private String getPakageName() {
        try {
            String pkName = this.getPackageName();

            return this.getPackageName();
        } catch (Exception e) {
        }
        return "";
    }

    @Override
    public void onAgreePrivacy() {
        if (shouldRequestUserPermission()) {
            requestUserPermission();

            SharedPreferences sp = mApp.getContext().getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
            mEditor = sp.edit();
            mEditor.putBoolean("FirstLogin", false);
            // 提交
            mEditor.commit();
            return;
        }
    }

    private boolean shouldRequestUserPermission() {
        return !GolukPermissionUtils.hasIndispensablePermission(this);
    }

    private void requestUserPermission() {
        GolukPermissionUtils.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
//        GolukPermissionUtils.requestPermissions(this, new String[]{
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.READ_PHONE_STATE,
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == GolukPermissionUtils.CODE_REQUEST_PERMISSION && !shouldRequestUserPermission()) {
            // 同意
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        GolukPermissionUtils.handlePermissionPermanentlyDenied(this, perms);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GolukPermissionUtils.CODE_REQUEST_PERMISSION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
                if (shouldRequestUserPermission()) {
                    finish();
                } else {
                    // 同意
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
