package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.AppConfigInfo;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.IParamHandler;

/**
 * 图片运动界面
 */
public class AniTypeFragment extends BaseFragment {
    /**
     * 视频编辑预览处理器
     */
    private IVideoEditorHandler mVideoEditorHandler;
    private IParamHandler mParamHandler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (IVideoEditorHandler) context;
        mParamHandler = (IParamHandler) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_ani_type, container, false);
        initView();
        return mRoot;
    }

    private Switch swZoom;
    private boolean backupZoomOut;

    private void initView() {
        swZoom = $(R.id.swAniTypeZoomOut);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.ani_type_zoom_out);
        backupZoomOut = mParamHandler.getParamData().isZoomOut();
        $(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveState();
                mVideoEditorHandler.onSure();
            }
        });
        swZoom.setChecked(mParamHandler.getParamData().isZoomOut());
        $(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backupZoomOut != swZoom.isChecked()) {
                    swZoom.setChecked(backupZoomOut);
                }
                saveState();
                mVideoEditorHandler.onBack();
            }
        });

        swZoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                swZoom.setEnabled(false);
                saveState();
                mVideoEditorHandler.pause();
                mVideoEditorHandler.reload(false);
                mVideoEditorHandler.start();
                swZoom.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swZoom.setEnabled(true);
                    }
                }, AppConfiguration.REPEAT_DELAY);
            }
        });
    }

    private void saveState() {
        mParamHandler.getParamData().setZoomOut(swZoom.isChecked());
        //设置新状态到的全局配置
        AppConfigInfo appConfigInfo = AppConfiguration.getAppConfig();
        appConfigInfo.setZoomOut(mParamHandler.getParamData().isZoomOut());
        AppConfiguration.saveAppConfig();
    }
}
