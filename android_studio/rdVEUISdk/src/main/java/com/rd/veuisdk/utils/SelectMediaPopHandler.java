package com.rd.veuisdk.utils;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

/**
 * 选择媒体-弹窗
 */
public class SelectMediaPopHandler {
    private Activity mActivity;
    private Callback mCallback;

    public SelectMediaPopHandler(@NonNull Activity activity, @NonNull Callback callback) {
        mActivity = activity;
        mCallback = callback;
    }

    /**
     * 弹出拍照录像选择窗口
     *
     * @param view
     * @return
     */
    public void showPopupWindow(View view) {
        animUtil = new AnimUtil();
        View contentView = LayoutInflater.from(mActivity).inflate(
                R.layout.camera_popup, null);

        //设置pop获取焦点，如果为false点击返回按钮会退出当前Activity，如果pop中有Editor的话，focusable必须要为true
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        TextView tvPhoto = (TextView) contentView.findViewById(R.id.tvPhoto);
        TextView tvVideo = (TextView) contentView.findViewById(R.id.tvVideo);

        tvPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                mCallback.onPhoto();
            }
        });

        tvVideo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                mCallback.onVideo();
            }
        });


        // 设置pop可点击，为false点击事件无效，默认为true
        popupWindow.setTouchable(true);
        // 设置点击pop外侧消失，默认为false；在focusable为true时点击外侧始终消失
        popupWindow.setOutsideTouchable(true);

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 设置pop透明效果
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置pop出入动画
        popupWindow.setAnimationStyle(R.style.pop_wind_style);
        popupWindow.showAsDropDown(view, -CoreUtils.dpToPixel(25), 10);

        // 设置pop关闭监听，用于改变背景透明度
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                toggleBright();
            }
        });

    }

    private AnimUtil animUtil;
    private float bgAlpha = 1f;
    private boolean bright = false;

    private static final long DURATION = 500;
    private static final float START_ALPHA = 0.8f;
    private static final float END_ALPHA = 1f;

    public void toggleBright() {
        // 三个参数分别为：起始值 结束值 时长，那么整个动画回调过来的值就是从0.5f--1f的
        animUtil.setValueAnimator(START_ALPHA, END_ALPHA, DURATION);
        animUtil.addUpdateListener(new AnimUtil.UpdateListener() {
            @Override
            public void progress(float progress) {
                // 此处系统会根据上述三个值，计算每次回调的值是多少，我们根据这个值来改变透明度
                bgAlpha = bright ? progress : (START_ALPHA + END_ALPHA - progress);
                backgroundAlpha(bgAlpha);
            }
        });
        animUtil.addEndListner(new AnimUtil.EndListener() {
            @Override
            public void endUpdate(Animator animator) {
                // 在一次动画结束的时候，翻转状态
                bright = !bright;
            }
        });
        animUtil.startAnimator();
    }

    /**
     * 此方法用于改变背景的透明度，从而达到“变暗”的效果
     */
    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        mActivity.getWindow().setAttributes(lp);
        // everything behind this window will be dimmed.
        // 此方法用来设置浮动层，防止部分手机变暗无效
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public static interface Callback {

        /**
         * 图片
         */
        void onPhoto();

        /**
         * 视频
         */
        void onVideo();
    }

}
