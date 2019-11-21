package com.rd.veuisdk.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.AppConfigInfo;
import com.rd.veuisdk.ui.ColorPicker;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.Utils;

/**
 * 背景界面
 */
public class BackgroundFragment extends BaseFragment {

    /**
     * 视频编辑预览处理器
     */
    private IVideoEditorHandler mVideoEditorHandler;
    private IParamHandler mParamHandler;
    private ColorPicker bgColorPicker;
    private LinearLayout llBackgroundColor;
    private Switch swNoBackgroundMode;
    private boolean isBulr = true;
    private boolean isNoBackground = false;
    private int defaultColor = -1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (IVideoEditorHandler) context;
        mParamHandler = (IParamHandler) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_background, container, false);
        isBulr = !mParamHandler.getParamData().isEnableBackground(); //是否有背景
        initView();
        return mRoot;
    }

    public void onBack() {
        if (isBulr) {
            ((RadioButton) $(R.id.rbBgBlur)).setChecked(true);
        } else {
            if (defaultColor == -1) {
                mVideoEditorHandler.onBackgroundColorChanged(Color.BLACK);
            } else {
                mVideoEditorHandler.onBackgroundColorChanged(defaultColor);
            }
        }
        swNoBackgroundMode.setChecked(isNoBackground);
        mVideoEditorHandler.onBack();
    }

    private void initView() {
        llBackgroundColor = $(R.id.llBackgroundColor);
        bgColorPicker = $(R.id.backgroundPicker);
        final RadioGroup rgBackgroundColor = $(R.id.rgBackgroundColor);
        bgColorPicker.setColorListener(new ColorPicker.IColorListener() {
            @Override
            public void getColor(int color, int position) {
                rgBackgroundColor.clearCheck();
                mVideoEditorHandler.onBackgroundColorChanged(color);
                saveBgColor(color);

            }
        });
        bgColorPicker.setColumnNum(6);
        bgColorPicker.setColorArr(new int[]{Color.BLACK, Color.parseColor("#f9edb1"),
                Color.parseColor("#ffa078"), Color.parseColor("#fe6c6c"),
                Color.parseColor("#fe4241"), Color.parseColor("#7cddfe"),
                Color.parseColor("#42c5dc"), Color.parseColor("#0695b5"),
                Color.parseColor("#2791db"), Color.parseColor("#0271fe"),
                Color.parseColor("#dcffa3"), Color.parseColor("#c7fe64"),
                Color.parseColor("#82e23a"), Color.parseColor("#25ba66"),
                Color.parseColor("#017e54"), Color.parseColor("#fdbacc"),
                Color.parseColor("#ff5a85"), Color.parseColor("#ff5ab0"),});


        if (isBulr) {
            ((RadioButton) $(R.id.rbBgBlur)).setChecked(true);
            bgColorPicker.clearChecked();
        } else {
            defaultColor = mVideoEditorHandler.getEditor().getBackgroundColor();
            if (defaultColor != -1) {
                bgColorPicker.checkColor(defaultColor);
            }
        }
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.background);
        $(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoEditorHandler.onSure();
            }
        });
        $(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        swNoBackgroundMode = $(R.id.swNoBackgroundMode);
        isNoBackground = !mParamHandler.getParamData().isEnableBackground();
        swNoBackgroundMode.setChecked(isNoBackground);
        if (isNoBackground) {
            llBackgroundColor.setVisibility(View.INVISIBLE);
        }
        swNoBackgroundMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                swNoBackgroundMode.setEnabled(false);
                mParamHandler.getParamData().enableBackground(!isChecked);

                //设置新状态到的全局配置
                AppConfigInfo appConfigInfo = AppConfiguration.getAppConfig();
                appConfigInfo.setEnableBGMode(mParamHandler.getParamData().isEnableBackground());
                AppConfiguration.saveAppConfig();

                if (isChecked) {
                    mVideoEditorHandler.onBackgroundModeChanged(!isChecked);
                } else {
                    //如果切换为有背景模式 ,重置为高斯效果
                    rgBackgroundColor.check(R.id.rbBgBlur);
                    int color = VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR;
                    mVideoEditorHandler.onBackgroundColorChanged(color);
                    saveBgColor(color);
                }
                llBackgroundColor.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);
                swNoBackgroundMode.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swNoBackgroundMode.setEnabled(true);
                    }
                }, AppConfiguration.REPEAT_DELAY);


            }
        });

        rgBackgroundColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = Utils.$(group, checkedId);
                if (radioButton != null && radioButton.isChecked()) {
                    int color = Color.RED;
                    if (checkedId == R.id.rbBgBlur) {
                        color = VisualFilterConfig.FILTER_ID_GAUSSIAN_BLUR;
                    } else if (checkedId == R.id.rbBgBlack) {
                        color = Color.BLACK;
                    } else if (checkedId == R.id.rbBgWhite) {
                        color = Color.WHITE;
                    }
                    if (Color.RED != color) {
                        mVideoEditorHandler.onBackgroundColorChanged(color);
                        saveBgColor(color);
                    }
                    bgColorPicker.clearChecked();
                }
            }
        });
    }

    private void saveBgColor(int color) {
        //设置新状态到的全局配置
        AppConfigInfo appConfigInfo = AppConfiguration.getAppConfig();
        appConfigInfo.setBgColor(color);
        AppConfiguration.saveAppConfig();
    }
}
