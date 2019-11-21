package com.rd.veuisdk.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.rd.veuisdk.R;

/***
 * 显示PopupWindow辅助
 */
public class PopViewUtil {

    public static interface CallBack {
        public void onClick();
    }

    private static PopupWindow popupWindow;


    /**
     * 支持以父容器中得X点居中
     *
     * @param view     父容器
     * @param upOdown  箭头向上或向下
     * @param bleft    左对齐
     * @param x        箭头距离最左边的距离像素值
     * @param pleft    popwindow左对齐or右对齐 默认true
     * @param px       window距离父容器的左边像素(支持负值-1080)
     * @param mback    回调
     * @param strId    显示文本内容Id
     * @param pCenterX 箭头是否位居中间 (取值范围0--1024),(0--1 取比例) 、 默认0
     */
    public static void showPopupWindowStyle(final View view, boolean upOdown,
                                            boolean bleft, int x, boolean pleft, double px, CallBack mback,
                                            int strId, double pCenterX) {
        showPopupWindowBase(view, upOdown, bleft, x, pleft, px, mback, strId, pCenterX, true);
    }

    /**
     * 取消
     */
    public static void cancelPopWind() {
        if (null != myPopRunnable) {
            mHandler.removeCallbacks(myPopRunnable);
            myPopRunnable = null;
        }
        if (null != popupWindow) {
            popupWindow.dismiss();
            popupWindow = null;
        }

    }

    private static MyPopRunnable myPopRunnable;
    private static Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0: {

                }
                break;
                default: {
                }
                break;
            }
        }
    };


    private static void showPopupWindowBase(final View view,
                                            final boolean upOdown, final boolean bleft, final int x,
                                            final boolean pleft, final double px, final CallBack mback,
                                            int strId, double pCenterX, final boolean isInStyle) {

        if (null == popupWindow || !popupWindow.isShowing()) {

            View v = view.getRootView();
            if (v instanceof ViewGroup) {

                ViewGroup vp = (ViewGroup) v;
                // 一个自定义的布局，作为显示的内容
                View contentView = LayoutInflater.from(view.getContext())
                        .inflate(R.layout.popwind_layout, null);
                AutoView autoview = (AutoView) contentView
                        .findViewById(R.id.auto_listview_drag);
                autoview.setUpOrDown(upOdown, x, strId, bleft, pCenterX);
                contentView.setVisibility(View.INVISIBLE);
                vp.addView(contentView);

                myPopRunnable = new MyPopRunnable(vp, contentView, autoview, mback, view, upOdown, x, pleft, px, isInStyle);
                mHandler.removeCallbacks(myPopRunnable);
                mHandler.postDelayed(myPopRunnable, 500);
            }
        }
    }

    static class MyPopRunnable implements Runnable {

        public MyPopRunnable(ViewGroup vp, View contentView, AutoView autoview, CallBack mback, View view, boolean upOdown, int x, boolean pleft, double px, boolean isInStyle) {

            this.vp = vp;
            this.contentView = contentView;
            this.autoview = autoview;
            this.mback = mback;
            this.view = view;
            this.upOdown = upOdown;
            this.x = x;
            this.pleft = pleft;
            this.px = px;
            this.isInStyle = isInStyle;
        }

        private ViewGroup vp;
        // 一个自定义的布局，作为显示的内容
        private View contentView;
        private AutoView autoview;

        private CallBack mback;
        private View view;
        private boolean upOdown;
        private int x;
        private boolean pleft;
        private double px;
        private boolean isInStyle;


        @Override
        public void run() {
            int[] size = autoview.setLocation();
            vp.removeView(contentView);
            contentView.setVisibility(View.VISIBLE);
            popupWindow = new PopupWindow(contentView, size[0],
                    size[1], true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    cancelPopWind();
                    mback.onClick();

                }
            });
            contentView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mback.onClick();
                    cancelPopWind();
                    return false;

                }
            });

            // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
            // 我觉得这里是API的一个bug
            // popupWindow.setBackgroundDrawable(view.getResources()
            // .getDrawable(R.color.transparent));
            try {
                if (upOdown) {
                    if (pleft) {
                        double mpx = px;
                        if (Math.abs(mpx) == 0.5) {
                            mpx = -(size[0] - view.getWidth()) / 2;
                        }
                        if (isInStyle) {
                            popupWindow.showAsDropDown(view,
                                    (int) mpx - size[0] / 2, 0);
                        } else {
                            popupWindow.showAsDropDown(view,
                                    (int) mpx, 0);
                        }
                    } else {
                        popupWindow.showAsDropDown(view,
                                view.getWidth() - size[0], 0);
                    }
                } else {
                    if (pleft) {
                        double mpx = px;
                        if (Math.abs(mpx) == 0.5) {
                            mpx = -(size[0] - view.getWidth()) / 2;
                        }
                        popupWindow.showAsDropDown(view, (int) mpx,
                                -(size[1] + view.getHeight()));
                    } else {
                        popupWindow.showAsDropDown(view,
                                view.getWidth() - size[0],
                                -(size[1] + view.getHeight()));
                    }
                }
            } catch (Exception ex) {
            }
        }
    }


}
