package com.mobnote.golukmain;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
import com.mobnote.application.GlobalWindow;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.thirdshare.FacebookShareHelper;
import com.umeng.analytics.MobclickAgent;

/**
 * 基础Activity
 * <p/>
 * 2015年5月13日
 *
 * @author xuhw
 */
public class BaseActivity extends FragmentActivity {
    // 写死ip,网关
    public static final String DEFAULT_IP = "192.168.1.103";
    public static final String DEFAULT_WAY = "192.168.1.103";

    private boolean m_bJumpActivity = false;
    protected BaseHandler mBaseHandler = new BaseHandler(this);
    protected GolukApplication mBaseApp = null;

    public GolukApplication getApp() {
        return mBaseApp;
    }


    /**
     * 处理Handler消息，子类需要复写此方法
     */
    protected void hMessage(Message msg) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseApp = (GolukApplication) getApplication();
        mBaseApp.initLogic();
    }

    @Override
    protected void onResume() {
        m_bJumpActivity = false;
        super.onResume();
        MobclickAgent.onResume(this);
        GolukApplication.getInstance().setIsBackgroundState(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBackground(this)) {
            GolukApplication.getInstance().setIsBackgroundState(true);
            if (GlobalWindow.getInstance().isShow()) {
                GlobalWindow.getInstance().dimissGlobalWindow();
            }
        }
    }

    @Override
    protected void onDestroy() {
        HttpManager.getInstance().cancelAll(this);
        mBaseHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        m_bJumpActivity = true;
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            showToast(R.string.str_cannot_start_activity);
        }
    }

    public boolean isAllowedClicked() {
        return !m_bJumpActivity;
    }

    /**
     * 标记当前界面跳转到其它界面，用于防止重复点击
     */
    public void setJumpToNext() {
        m_bJumpActivity = true;
    }

    /**
     * 设置界面可以跳转
     */
    public void setCanJump() {
        m_bJumpActivity = false;
    }

    /**
     * 去登录
     */
    public void toLoginBack() {
        Intent intent;
        if (!GolukApplication.getInstance().isMainland()) {
            intent = new Intent(this, InternationUserLoginActivity.class);
        } else {
            intent = new Intent(this, UserLoginActivity.class);
        }
        intent.putExtra("isInfo", "back");
        startActivity(intent);
    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    @SuppressWarnings("deprecation")
    public boolean isBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!FacebookSdk.isInitialized()) {
            return;
        }
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode()) {
            FacebookShareHelper.getInstance().mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void showToast(String hint) {
        showToast(hint, Toast.LENGTH_SHORT);
    }

    protected void showToast(int stringRes) {
        showToast(getString(stringRes), Toast.LENGTH_SHORT);
    }

    protected void showToast(String hint, int duration) {
        Toast.makeText(mBaseApp, hint, duration).show();
    }


    /**
     * handle Memory leak
     * http://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
     */
    protected static class BaseHandler extends Handler {
        private final WeakReference<BaseActivity> mActivityReference;

        public BaseHandler(BaseActivity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseActivity activity = mActivityReference.get();
            if (activity == null) {
                return;
            }
            activity.hMessage(msg);
        }
    }

}
