package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.AppConfigInfo;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.IParamHandler;

public class ProportionFragment extends BaseFragment {

    private RadioGroup mRgProportion1;
    private RadioGroup mRgProportion2;
    private boolean isFirst;

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
        mRoot = inflater.inflate(R.layout.fragment_proportion, container, false);
        initView();
        return mRoot;
    }

    private void initView() {
        isFirst = true;
        mRgProportion1 = $(R.id.rgProportionLine1);
        mRgProportion2 = $(R.id.rgProportionLine2);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.more_orientation);
        $(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRgProportion1.getCheckedRadioButtonId() != -1) {
                    defaultCheckedId = mRgProportion1.getCheckedRadioButtonId();
                } else {
                    defaultCheckedId = mRgProportion2.getCheckedRadioButtonId();
                }
                mVideoEditorHandler.onSure();
            }
        });
        $(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        mRgProportion1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (isFirst || checkedId == -1) {
                    isFirst = false;
                    return;
                }
                setUnable();
                if (((RadioButton) $(checkedId)).isChecked()) {
                    mRgProportion2.clearCheck();
                    setProportion(checkedId);
                }
                mRgProportion1.postDelayed(restoreUIRunnable, AppConfiguration.REPEAT_DELAY);
            }
        });
        mRgProportion2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (isFirst || checkedId == -1) {
                    isFirst = false;
                    return;
                }
                setUnable();
                if (((RadioButton) $(checkedId)).isChecked()) {
                    mRgProportion1.clearCheck();
                    setProportion(checkedId);
                }
                mRgProportion2.postDelayed(restoreUIRunnable, AppConfiguration.REPEAT_DELAY);
            }
        });

        float aspect = mParamHandler.getParamData().getProportionAsp();

        if (aspect == 0) {
            defaultCheckedId = R.id.rbProportionOri;
        } else if (aspect == 1) {
            defaultCheckedId = R.id.rbProportion1x1;
        } else if (aspect == 16f / 9) {
            defaultCheckedId = R.id.rbProportion169;
        } else if (aspect == 9f / 16f) {
            defaultCheckedId = R.id.rbProportion916;
        } else if (aspect == 3f / 4) {
            defaultCheckedId = R.id.rbProportion34;
        } else if (aspect == 4f / 3) {
            defaultCheckedId = R.id.rbProportion43;
        } else {
            defaultCheckedId = R.id.rbProportionOri;
        }
        ((RadioButton) $(defaultCheckedId)).setChecked(true);
    }

    private int defaultCheckedId = R.id.rbProportionOri;

    private void setUnable() {
        mRgProportion1.setEnabled(false);
        mRgProportion2.setEnabled(false);
    }

    private Runnable restoreUIRunnable = new Runnable() {
        @Override
        public void run() {
            mRgProportion1.setEnabled(true);
            mRgProportion2.setEnabled(true);
        }
    };


    public void onBack() {
        ((RadioButton) $(defaultCheckedId)).setChecked(true);
        mVideoEditorHandler.onBack();
    }

    /**
     * 设置比例
     */
    private void setProportion(int checkedId) {
        float aspect = 0;
        if (checkedId == R.id.rbProportionOri) {
            aspect = 0;
        } else if (checkedId == R.id.rbProportion1x1) {
            aspect = 1;
        } else if (checkedId == R.id.rbProportion169) {
            aspect = 16f / 9;
        } else if (checkedId == R.id.rbProportion916) {
            aspect = 9f / 16;
        } else if (checkedId == R.id.rbProportion34) {
            aspect = 3f / 4;
        } else if (checkedId == R.id.rbProportion43) {
            aspect = 4f / 3;
        }
        mVideoEditorHandler.onProportionChanged(aspect);
        //设置新状态到的全局配置
        AppConfigInfo appConfigInfo = AppConfiguration.getAppConfig();
        appConfigInfo.setProportionAsp(aspect);
        AppConfiguration.saveAppConfig();


    }

}
