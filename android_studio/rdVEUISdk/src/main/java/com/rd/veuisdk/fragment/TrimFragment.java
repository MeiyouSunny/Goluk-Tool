package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.veuisdk.R;

/**
 * AE 截取，支持原音調整
 *
 */
public class TrimFragment extends BaseFragment {
    private TextView tvMediaVolumeTitle;
    private SeekBar sbar;


    public static TrimFragment newInstance() {

        Bundle args = new Bundle();

        TrimFragment fragment = new TrimFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setCallBack(ICallBack callBack) {
        mCallBack = callBack;
    }

    private ICallBack mCallBack;

    public void setFactor(int factor) {
        mFactor = factor;
    }

    private int mFactor = 50;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.filter_sbar_layout, null);
        $(R.id.strengthLayout).setVisibility(View.VISIBLE);
        initMediaVolume();
        return mRoot;
    }

    public static interface ICallBack {

        public void onMixFactor(int factor);

        public void onMixSure(int factor);
    }

    /**
     * AE 截取时，选择原音比例
     */
    private void initMediaVolume() {
        tvMediaVolumeTitle = $(R.id.tvDialogTitle);
        tvMediaVolumeTitle.setText(R.string.media_volume);
        sbar = $(R.id.sbarStrength);
        sbar.setProgress(mFactor);
        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser && null != mCallBack) {
                    mCallBack.onMixFactor(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        $(R.id.btnDialogSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (null != mCallBack) {
                    mCallBack.onMixSure(sbar.getProgress());
                }

            }
        });

    }
}
