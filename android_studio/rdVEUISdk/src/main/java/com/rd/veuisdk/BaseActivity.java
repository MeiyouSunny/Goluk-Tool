package com.rd.veuisdk;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.utils.DeviceUtils;
import com.rd.veuisdk.manager.ChangeLanguageHelper;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

public abstract class BaseActivity extends FragmentActivity {

    protected boolean isRunning = false;

    /**
     * 视频时长
     *
     * @param ms
     * @return
     */
    public String getTime(int ms) {
        return DateTimeUtils.stringForMillisecondTime(ms, true, true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ChangeLanguageHelper.attachBaseContext(newBase, ChangeLanguageHelper.getAppLanguage(newBase)));
    }

    /**
     * 保留刘海区域
     */
    public void showNotchScreen() {
        if (!DeviceUtils.hasNotchScreen(this)) {
            //没有刘海，全屏显示
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        } else {
            //有刘海，显示刘海栏
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRunning = true;
    }

    /**
     * 获取统计页名称
     *
     * @return
     */
    protected String mStrActivityPageName = "baseActivity";
    protected String TAG = "baseActivity";

    /**
     * 通用响应Click事件
     *
     * @param v
     */
    public void clickView(View v) {

    }

    /**
     * 文本框设置文本
     *
     * @param nTextViewId 文本框资源Id
     * @param strText     文本
     */
    protected void setText(@IdRes int nTextViewId, String strText) {
        ((TextView) findViewById(nTextViewId)).setText(strText);
    }

    /**
     * 设置文本
     *
     * @param nTextViewId
     * @param strId
     */

    protected void setText(@IdRes int nTextViewId, @StringRes int strId) {
        ((TextView) findViewById(nTextViewId)).setText(strId);
    }

    /**
     * 设置View是否显示
     *
     * @param nViewId
     * @param bVisiable
     */
    public void setViewVisibility(int nViewId, boolean bVisiable) {
        setViewVisibility(nViewId, bVisiable ? View.VISIBLE : View.GONE, 0);
    }

    /**
     * 设置图片资源
     *
     * @param nImageViewId 图片View资源Id
     * @param nImageResId  图片资源Id(drawable)
     */
    protected void setImageViewSrc(int nImageViewId, int nImageResId) {
        ((ImageView) findViewById(nImageViewId)).setImageResource(nImageResId);
    }

    /**
     * 文本框设置文本
     *
     * @param v           fragment
     * @param nTextViewId
     * @param strText
     */
    protected void setText(View v, int nTextViewId, String strText) {
        ((TextView) v.findViewById(nTextViewId)).setText(strText);
    }

    /**
     * 设置fragment中的控件
     *
     * @param parent
     * @param nViewId
     * @param bVisiable
     */
    protected void setViewVisibility(View parent, int nViewId, boolean bVisiable) {
        setViewVisibility(parent, nViewId, bVisiable, 0);
    }

    protected <T extends View> T $(@IdRes int viewId) {
        return findViewById(viewId);
    }

    /**
     * 设置fragment中的控件可见
     *
     * @param parent
     * @param nViewId
     * @param bVisiable
     * @param nAnimationResId
     */

    protected void setViewVisibility(View parent, int nViewId,
                                     boolean bVisiable, int nAnimationResId) {
        View v = parent.findViewById(nViewId);
        int nSetVisibility = bVisiable ? View.VISIBLE : View.GONE;
        v.clearAnimation();
        if (nAnimationResId > 0 && v.getVisibility() != nSetVisibility) {
            v.setAnimation(AnimationUtils.loadAnimation(this, nAnimationResId));
        }
        v.setVisibility(nSetVisibility);
    }

    /**
     * 设置View是否显示
     *
     * @param nViewId    View id
     * @param visibility 是否显示
     */
    protected void setViewVisibility(int nViewId, int visibility,
                                     int nAnimationResId) {
        View v = this.findViewById(nViewId);
        if (null != v) {
            v.clearAnimation();
            if (nAnimationResId > 0 && v.getVisibility() != visibility) {
                v.setAnimation(AnimationUtils.loadAnimation(this,
                        nAnimationResId));
            }
            v.setVisibility(visibility);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        System.gc();
        System.runFinalization();
    }

    /**
     * @param containerViewId
     * @param fragment
     */
    protected void changeFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(containerViewId, fragment);
        ft.commitAllowingStateLoss();
    }

    /**
     * 移除
     *
     * @param fragment
     */
    protected void removeFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fragment).commitAllowingStateLoss();
    }


    protected void onToast(String strMessage) {
        SysAlertDialog.showAutoHideDialog(this, null, strMessage,
                Toast.LENGTH_SHORT);
    }

    protected void onToast(@StringRes int resId) {
        onToast(getString(resId));
    }


    /**
     * 放弃编辑
     *
     * @return
     */
    protected final Dialog showCancelEditDialog() {
        return SysAlertDialog.showAlertDialog(this, "", getString(R.string.quit_edit),
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }

}
