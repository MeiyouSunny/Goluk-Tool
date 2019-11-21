package com.rd.veuisdk.listener;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

import com.rd.veuisdk.R;

/**
 * 解决输入法造成整体界面向上推，解决方案：输入框对应的fragment单独作为一个Frame  ，只推这一个frame， 播放器所在的Frame不动
 *
 * @author JIAN
 * @create 2019/4/24
 * @Describe
 */
public class OnGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private View root;
    private View scrollToView;
    private Rect mRootRect = new Rect();//Activity根布局的显示区域
    private Rect rectVisible = new Rect();//可见的区域（打开输入法后，此区域会变矮）
    private final String TAG = "OnGlobalLayoutListener";
    private View mLlWordEditer;
    private int mEditParentHeight;

    /**
     * @param root          Activity 的根节点
     * @param scrollToView  当前fragment 对应的容器的父节点 <R.id.rlEditorMenuAndSubLayout>
     * @param mLlWordEditer 输入框的父容器   <R.id.thelocation>
     */
    public OnGlobalLayoutListener(@NonNull View root, @NonNull View scrollToView, @NonNull View mLlWordEditer) {
        this.root = root;
        this.scrollToView = scrollToView;
        this.root.getGlobalVisibleRect(mRootRect);
        this.mLlWordEditer = mLlWordEditer;
        bInputOpenEd = false;
        mEditParentHeight = root.getResources().getDimensionPixelSize(R.dimen.input_edit_parent_height);
        defaultY = mRootRect.bottom - scrollToView.getHeight();
    }

    //输入法是否打开
    private boolean bInputOpenEd = false;
    private int defaultY = 0;//没打开键盘时的Y

    public void setEditHeight(int height) {
        mEditParentHeight = height;
    }

    @Override
    public void onGlobalLayout() {
        root.getWindowVisibleDisplayFrame(rectVisible);
        int rHeight = root.getRootView().getHeight();
        int rootInvisibleHeight = rHeight - rectVisible.height();
//        Log.e(TAG, "onGlobalLayout: " + this + " mRootRect:" + mRootRect + "  rectVisible：" + rectVisible + " " +
//                " rootInvisibleHeight(输入法高度)：" + rootInvisibleHeight + "  rHeight:" + rHeight + " bInputOpenEd:" + bInputOpenEd);

        // 若不可视区域高度大于200，则键盘显示
        if (rootInvisibleHeight > 200) {
            mLlWordEditer.setVisibility(View.VISIBLE);
            // rootInvisibleHeight值为输入法Frame的高度
            int[] location = new int[2];
            // 获取scrollToView在窗体的坐标
            mLlWordEditer.getLocationInWindow(location);

            int tY = mRootRect.bottom - rootInvisibleHeight - mEditParentHeight;
//            Log.e(TAG, "onGlobalLayout:   打开键盘:" + tY + "  " + Arrays.toString(location) + "  mEditParentHeight:" + mEditParentHeight + "  mLlWordEditer:" + mLlWordEditer.getHeight());
            if (scrollToView != null) {
                if (location[1] > tY && !bInputOpenEd) {
                    // 输入法打开对于目标区域有遮挡
                    bInputOpenEd = true;
                    scrollToView.setY(tY);
                }
            }
        } else {
            // 键盘隐藏
            mLlWordEditer.setVisibility(View.GONE);
            if (scrollToView != null && bInputOpenEd) {
//                int re = mRootRect.bottom - scrollToView.getHeight();
//                Rect viewPRect = new Rect();
//                scrollToView.getWindowVisibleDisplayFrame(viewPRect);
//                Log.e(TAG, "onGlobalLayout: yincang  " + re + " defaultY:" + defaultY + "   viewPRect:" + viewPRect);
                scrollToView.setY(defaultY);
                bInputOpenEd = false;
            }
        }
    }

    //主动恢复到默认UI状态
    public void resetUI() {
        if (defaultY > 0) {
            // 键盘隐藏
            mLlWordEditer.setVisibility(View.GONE);
            if (scrollToView != null) {
//                int re = mRootRect.bottom - scrollToView.getHeight();
//                Rect viewPRect = new Rect();
//                scrollToView.getWindowVisibleDisplayFrame(viewPRect);
//                Log.e(TAG, "resetUI: yincang  " + re + " defaultY:" + defaultY + "   viewPRect:" + viewPRect);
                scrollToView.setY(defaultY);
                bInputOpenEd = false;
            }
        }
    }
}

