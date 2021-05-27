package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import androidx.fragment.app.Fragment;
import android.view.View;

import com.rd.veuisdk.utils.SysAlertDialog;

/**
 * 统计V4fragment的基类
 *
 * @author JIAN
 */
public class BaseV4Fragment extends Fragment {

    protected String mPageName = "BaseV4Fragment";
    protected View mRoot;
    protected String TAG = BaseV4Fragment.this.toString();
    protected Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }

    public void showLoadingDialog(@StringRes int strId,boolean cancelable) {
        SysAlertDialog.showLoadingDialog(getContext(), strId, cancelable, null);
    }

    public static void cancelLoadingDialog() {
        SysAlertDialog.cancelLoadingDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 查找fragment内的组件
     *
     * @param resId
     * @param <T>
     * @return
     */
    public <T extends View> T $(int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * 查找fragment内的组件
     *
     * @param id
     * @return
     */
    public View findViewById(int id) {
        return mRoot.findViewById(id);
    }

}
