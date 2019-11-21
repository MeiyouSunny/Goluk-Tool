package com.rd.veuisdk.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.model.OnShowScanFileInterface;

/**
 * 扫描媒体文件
 *
 * @author johnny
 */
public class ExtScanMediaDialog extends Dialog implements
        OnShowScanFileInterface {

    private Context m_context;
    public static final String INTENT_SIGHTSEEING_UPATE = "intent_update";
    public static final String INTENT_SIGHTSEEING_DATA = "siahtseeingUpdate";

    /**
     * 完成扫描
     */
    private ImageView m_imgFinishScan;
    /**
     * 媒体名称路径
     */
    private TextView m_tvMediaPathName;
    /**
     * 取消扫描
     */
    private TextView m_tvCancelScan;

    public ExtScanMediaDialog(Context context) {
        super(context, R.style.dialog);
        this.m_context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取当前加载的布局实例对象
        View dialogLayout = LayoutInflater.from(m_context).inflate(
                R.layout.rdveuisdk_scan_media_dialog, null);
        setContentView(dialogLayout);
        setCancelable(false);
        // 初始化布局
        initView();
    }

    public void initView() {


        m_imgFinishScan = (ImageView) this.findViewById(R.id.iv_finishScan);
        rotateImage();
        m_tvMediaPathName = (TextView) this.findViewById(R.id.tv_mediaPathName);

        m_tvCancelScan = (TextView) this.findViewById(R.id.tv_cancelScan);
        // 点击扫描完成，让对话框消失
        m_tvCancelScan.setOnClickListener(onButtonClick);
    }

    /**
     * 扫描结束
     *
     * @param newFileNum
     */
    public void Finish(int newFileNum) {
        // 让旋转的动画消失
        if (animation != null) {
            animation.cancel();
            m_imgFinishScan.clearAnimation();
        }
        // 设置为完成标志
        m_imgFinishScan.setImageResource(R.drawable.finish_scan);
        String info = null;
        if (newFileNum > 0) {
            if (mIsVideo) {
                info = getContext().getString(R.string.scan_video_file_end_num, newFileNum);
            } else {
                info = getContext().getString(R.string.scan_music_file_end_num, newFileNum);
            }
        } else {
            if (mIsVideo) {
                info = getContext().getString(R.string.scan_video_file_end_num_0);
            } else {
                info = getContext().getString(R.string.scan_music_file_end_num_0);
            }
        }
        m_tvMediaPathName.setText(info);
        // 设置最下面的按钮为完成
        m_tvCancelScan.setText(R.string.sure);
        setCancelable(true);
    }

    private Animation animation;

    /**
     * 旋转动画
     */
    public void rotateImage() {
        ((Activity) m_context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(m_context,
                        R.anim.scan_media);
                LinearInterpolator li = new LinearInterpolator();
                animation.setInterpolator(li);
                m_imgFinishScan.startAnimation(animation);
            }
        });
    }

    @Override
    public void dismiss() {
        if (animation != null) {
            animation.cancel();
            m_imgFinishScan.clearAnimation();
        }
        super.dismiss();
    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (m_scanMusicClickInterface != null) {
                if (m_tvCancelScan.getText().equals(getContext().getString(R.string.cancel))) {
                    m_scanMusicClickInterface.cancel();
                } else {
                    m_scanMusicClickInterface.accomplish();
                }
            }
            // 让对话框消失
            dismiss();
        }
    };

    @Override
    public void scanFilePath(final String path) {
        Message msg = mHandler.obtainMessage();
        msg.obj = path;
        msg.what = 0;
        mHandler.removeMessages(0);
        mHandler.sendMessage(msg);
    }

    @Override
    public void scanNewFileNum(final int newNum) {
        Message msg = mHandler.obtainMessage();
        msg.obj = newNum;
        msg.what = 1;
        mHandler.removeMessages(0);
        mHandler.sendMessage(msg);
    }

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                m_tvMediaPathName.setText(msg.obj.toString());
            } else if (msg.what == 1) {
                Finish((Integer) msg.obj);
            }
        }

        ;
    };

    /**
     * 扫描媒体单击事件的回调接口
     */
    private onScanMusicClickInterface m_scanMusicClickInterface;

    /**
     * 注册扫描媒体单击事件的回调接口
     *
     * @param clickInterface
     */
    public void setonScanMusicClickInterface(
            onScanMusicClickInterface clickInterface) {
        this.m_scanMusicClickInterface = clickInterface;
    }

    /**
     * 扫描媒体单击事件
     *
     * @author johnny
     */
    public interface onScanMusicClickInterface {
        /**
         * 完成
         */
        void accomplish();

        /**
         * 取消
         */
        void cancel();
    }

    private boolean mIsVideo = false;

    public void setVideo() {
        this.mIsVideo = true;
    }

}
