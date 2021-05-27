package com.rd.veuisdk.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.rd.veuisdk.utils.SysAlertDialog;

/**
 * @author JIAN
 * @date 2019/2/21
 * @Description
 */
public abstract class BaseActivity extends FragmentActivity {
    protected boolean isRunning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        isRunning = true;
    }

    /**
     * 查找fragment内的组件
     *
     * @param resId
     * @param <T>
     * @return
     */
    public <T extends View> T $(int resId) {
        return findViewById(resId);
    }

    /**
     * @param containerViewId
     * @param fragment
     */
    protected void changeFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(containerViewId, fragment);
        ft.commit();
    }


    protected void onToast(String strMessage) {
        SysAlertDialog.showAutoHideDialog(this, null, strMessage,
                Toast.LENGTH_SHORT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        System.gc();
        System.runFinalization();
    }

    /**
     * 设置布局
     *
     * @return
     */
    public abstract int getLayoutId();


}
