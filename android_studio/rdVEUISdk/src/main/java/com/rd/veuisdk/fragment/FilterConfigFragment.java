package com.rd.veuisdk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;

import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.ExtFilterSeekBar;
import com.rd.veuisdk.utils.IMediaFilter;
import com.rd.veuisdk.utils.IMediaParamImp;
import com.rd.veuisdk.utils.SysAlertDialog;

/**
 * 滤镜调色
 *
 * @create 2018/12/6
 * @Describe
 */
public class FilterConfigFragment extends BaseFragment {
    private IMediaFilter mIMediaFilter;
    private float mBrightness = Float.NaN, mContrast = Float.NaN, mSaturation = Float.NaN, mSharpen = Float.NaN, mWhite = Float.NaN, mVignette = Float.NaN;

    private int vignettId = IMediaParamImp.NO_VIGNETTEDID;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMediaFilter = (IMediaFilter) getActivity();
    }

    public static FilterConfigFragment newInstance() {
        FilterConfigFragment fragment = new FilterConfigFragment();
        return fragment;
    }

    private ExtFilterSeekBar sbar_brightness, sbar_contrast, sbar_saturation, sbar_sharpen, sbar_temperature, sbar_vignette;
    private CheckBox ivBrightness, ivContrast, ivSaturation, ivSharpen, ivTemperature, ivVignette;
    private CheckBox cbAllPart;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_fiter_config_layout, container, false);
        initView();
        return mRoot;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IMediaParamImp tmp = mIMediaFilter.getFilterConfig();
        if (null != tmp) {
            mBrightness = tmp.getBrightness();
            mContrast = tmp.getContrast();
            mSaturation = tmp.getSaturation();
            mSharpen = tmp.getSharpen();
            mVignette = tmp.getVignette();
            mWhite = tmp.getWhite();
            vignettId = tmp.getVignetteId();
            initValue();
        }
    }

    private void initValue() {
        //亮度
        sbar_brightness.setDefaultValue(50);
        float fv = sbar_brightness.getMax() * (mBrightness - (-1.0f)) / 2.0f;
        if (Float.isNaN(fv)) {
            fv = sbar_brightness.getMax() / 2.0f;
            ivBrightness.setChecked(false);
            sbar_brightness.setChangedByHand(false);
        } else {
            ivBrightness.setChecked(true);
            sbar_brightness.setChangedByHand(true);
        }
        sbar_brightness.setProgress((int) fv);

        //对比度
        sbar_contrast.setDefaultValue(25);
        if (Float.isNaN(mContrast)) {
            //    VisualFilterConfig中对比度默认为1.0f(区间：0~4.0f)
            sbar_contrast.setProgress((int) (sbar_contrast.getMax() * 1.0f / 4.0f));
            ivContrast.setChecked(false);
            sbar_contrast.setChangedByHand(false);

        } else {
            sbar_contrast.setChangedByHand(true);
            ivContrast.setChecked(true);
            sbar_contrast.setProgress((int) (sbar_contrast.getMax() * mContrast / 4.0f));
        }

        sbar_saturation.setDefaultValue(50);
        //饱和度
        if (Float.isNaN(mSaturation)) {
            //    VisualFilterConfig中饱和度默认为1.0f(区间：0~2.0f)
            ivSaturation.setChecked(false);
            sbar_saturation.setChangedByHand(false);
            sbar_saturation.setProgress((int) (sbar_saturation.getMax() * 1 / 2.0f));
        } else {
            ivSaturation.setChecked(true);
            sbar_saturation.setChangedByHand(true);
            sbar_saturation.setProgress((int) (sbar_saturation.getMax() * mSaturation / 2.0f));
        }
        //锐度
        if (Float.isNaN(mSharpen)) {
            ivSharpen.setChecked(false);
            sbar_sharpen.setChangedByHand(false);
        } else {
            ivSharpen.setChecked(true);
            sbar_sharpen.setChangedByHand(true);
        }
        sbar_sharpen.setProgress((int) (sbar_sharpen.getMax() * mSharpen));


        //白平衡
        if (Float.isNaN(mWhite)) {
            ivTemperature.setChecked(false);
            sbar_temperature.setChangedByHand(false);
        } else {
            sbar_temperature.setChangedByHand(true);
            ivTemperature.setChecked(true);
        }
        sbar_temperature.setProgress((int) (sbar_temperature.getMax() * mWhite));


        //暗角
        if (IMediaParamImp.NO_VIGNETTEDID != vignettId) {
            sbar_vignette.setChangedByHand(true);
            //已经启用暗角
            ivVignette.setChecked(true);
        } else {
            sbar_vignette.setChangedByHand(false);
            //未启用
            ivVignette.setChecked(false);
        }
        sbar_vignette.setProgress((int) (sbar_vignette.getMax() * mVignette));
    }

    /**
     * 新的调色参数
     */
    private void onConfigChange(float value) {
        onTempSave();
        mIMediaFilter.onProgressChanged(value);
    }

    /**
     * 保存退出
     *
     * @return
     */
    public boolean onSure() {
        onTempSave();
        return cbAllPart.isChecked();

    }

    /**
     * 响应隐藏菜单
     */
    private void onStopTouch() {
        onTempSave();
        mIMediaFilter.onStopTrackingTouch();
    }

    /**
     * 保存当前编辑的参数
     */
    private void onTempSave() {
        IMediaParamImp tmp = mIMediaFilter.getFilterConfig();
        if (null != tmp) {
            tmp.setBrightness(mBrightness);
            tmp.setContrast(mContrast);
            tmp.setSaturation(mSaturation);
            tmp.setSharpen(mSharpen);
            tmp.setWhite(mWhite);
            tmp.setVignette(mVignette);
            tmp.setVignetteId(vignettId);
        }
    }


    private void onResetImp() {
        //全部重置
        mBrightness = Float.NaN;
        mContrast = Float.NaN;
        mSaturation = Float.NaN;
        mSharpen = Float.NaN;
        mVignette = Float.NaN;
        mWhite = Float.NaN;
        vignettId = IMediaParamImp.NO_VIGNETTEDID;
        initValue();
        onConfigChange(0);

    }

    private void onResetDialog() {
        String strMessage = getString(R.string.toning_reset_msg);
        SysAlertDialog.showAlertDialog(getContext(), "", strMessage,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onResetImp();
                    }
                });
    }

    private boolean enableApplyToAll = true;

    /**
     * 是否限制应用到全部
     */
    public void setEnableApplyToAll(boolean enable) {
        enableApplyToAll = enable;
    }

    private void initView() {
        cbAllPart = $(R.id.cbToningAll);
        if (!enableApplyToAll) {
            cbAllPart.setVisibility(View.GONE);
        }
        $(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbAllPart.setChecked(false);
                onResetDialog();
            }
        });


        ivBrightness =$(R.id.tvFilterBrightness);
        ivContrast = $(R.id.tvFilterContrast);
        ivSaturation = $(R.id.tvFilterSaturation);
        ivSharpen = $(R.id.tvFilterSharpen);
        ivTemperature =$(R.id.tvFilterTemperature);
        ivVignette = $(R.id.tvFiltervignette);

        //亮度
        sbar_brightness = $(R.id.sbar_brightness);
        sbar_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    fixBrightness(progress, seekBar.getMax());
                    onConfigChange(mBrightness);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ivBrightness.setChecked(true);
                mIMediaFilter.onStartTrackingTouch(R.string.filter_config_brightness, mBrightness);
                sbar_brightness.setChangedByHand(true);
            }

            void fixBrightness(int progress, int max) {
                mBrightness = (2.0f * progress / max) - 1.0f;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                fixBrightness(seekBar.getProgress(), seekBar.getMax());
                onStopTouch();
            }
        });
        //对比度
        sbar_contrast = (ExtFilterSeekBar) findViewById(R.id.sbar_contrast);
        sbar_contrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            void fixContrast(int progress, int max) {
                mContrast = 4.0f * progress / max;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    fixContrast(progress, seekBar.getMax());
                    onConfigChange(mContrast);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sbar_contrast.setChangedByHand(true);
                ivContrast.setChecked(true);
                mIMediaFilter.onStartTrackingTouch(R.string.filter_config_contrast, mContrast);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                fixContrast(seekBar.getProgress(), seekBar.getMax());
                onStopTouch();
            }
        });
        //饱和度
        sbar_saturation = (ExtFilterSeekBar) findViewById(R.id.sbar_saturation);
        sbar_saturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSaturation = (2.0f * progress / seekBar.getMax());
                    onConfigChange(mSaturation);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ivSaturation.setChecked(true);
                sbar_saturation.setChangedByHand(true);
                mIMediaFilter.onStartTrackingTouch(R.string.filter_config_saturation, mSaturation);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSaturation = (2.0f * seekBar.getProgress() / seekBar.getMax());
                onStopTouch();
            }
        });
        //锐度
        sbar_sharpen = (ExtFilterSeekBar) findViewById(R.id.sbar_sharpen);
        sbar_sharpen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSharpen = (1.0f * progress / seekBar.getMax());
                    onConfigChange(mSharpen);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ivSharpen.setChecked(true);
                sbar_sharpen.setChangedByHand(true);
                mIMediaFilter.onStartTrackingTouch(R.string.filter_config_sharpness, mSharpen);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSharpen = (1.0f * seekBar.getProgress() / seekBar.getMax());
                onStopTouch();
            }
        });
        //色温
        sbar_temperature = (ExtFilterSeekBar) findViewById(R.id.sbar_temperature);
        sbar_temperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mWhite = (1.0f * progress / seekBar.getMax());
                    onConfigChange(mWhite);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ivTemperature.setChecked(true);
                sbar_temperature.setChangedByHand(true);
                mIMediaFilter.onStartTrackingTouch(R.string.filter_config_white_balance, mWhite);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mWhite = (1.0f * seekBar.getProgress() / seekBar.getMax());
                onStopTouch();
            }
        });


        //暗角单独的滤镜
        sbar_vignette = (ExtFilterSeekBar) findViewById(R.id.sbar_vignette);
        sbar_vignette.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    fixVignette(seekBar.getProgress(), seekBar.getMax());
                    onConfigChange(mVignette);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ivVignette.setChecked(true);
                sbar_vignette.setChangedByHand(true);
                mIMediaFilter.onStartTrackingTouch(R.string.filter_config_vignette, mVignette);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                fixVignette(seekBar.getProgress(), seekBar.getMax());
                onStopTouch();

            }

            private void fixVignette(int progress, int max) {
                mVignette = 1.0f * progress / max;
                if (mVignette > 0 && mVignette <= 1) {
                    vignettId = VisualFilterConfig.FILTER_ID_VIGNETTE;
                } else {
                    vignettId = IMediaParamImp.NO_VIGNETTEDID;
                }
            }
        });
    }

    private IMediaParamImp tmp = null;

    /***
     * beginTouch
     */
    public void onDiffBegin() {
        tmp = mIMediaFilter.getFilterConfig().clone();
        onResetImp();

    }

    /**
     * endTouch
     */
    public void onDiffEnd() {
        //restore toning data
        if (null != tmp) {
            mBrightness = tmp.getBrightness();
            mContrast = tmp.getContrast();
            mSaturation = tmp.getSaturation();
            mSharpen = tmp.getSharpen();
            mVignette = tmp.getVignette();
            mWhite = tmp.getWhite();
            vignettId = tmp.getVignetteId();
        }
        initValue();
        onStopTouch();

    }
}
