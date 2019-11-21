package com.rd.veuisdk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.IParamData;
import com.rd.veuisdk.utils.IParamDataImp;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.SysAlertDialog;

/**
 * 媒体-音量大小
 */
public class VolumeFragment extends BaseFragment {
    public static VolumeFragment newInstance() {
        Bundle args = new Bundle();
        VolumeFragment fragment = new VolumeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SeekBar mSbFactor;
    private IVideoEditorHandler mVideoEditorHandler;
    private IParamData mParamData;
    private int mOldVolume;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (IVideoEditorHandler) context;
        mParamData = ((IParamHandler) context).getParamData();
        mOldVolume = mParamData.getFactor();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_volume, container, false);
        mSbFactor = $(R.id.sbFactor);
        mSbFactor.setMax(IParamDataImp.MAX_FACTOR);
        mSbFactor.setProgress(mParamData.getFactor());
        mSbFactor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mParamData.setFactor(progress);
                    mVideoEditorHandler.getEditorVideo().setOriginalMixFactor(mParamData.getFactor());
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        ((TextView) $(R.id.tvFactorType)).setText(R.string.videoVoice);
        ((TextView) $(R.id.tvBottomTitle)).setText(R.string.volume);

        $(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOldVolume != mParamData.getFactor()) {
                    onShowAlert();
                } else {
                    mVideoEditorHandler.onBack();
                }
            }
        });

        $(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoEditorHandler.onSure();
            }
        });

        return mRoot;
    }

    /**
     * 提示是否放弃保存
     */
    public void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.dialog_tips),
                mContext.getString(R.string.cancel_all_changed),
                mContext.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, mContext.getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackToActivity(false);
                        dialog.dismiss();
                    }
                }, false, null).show();
    }

    /**
     * 退出音量
     */
    private void onBackToActivity(boolean save) {
        if (!save) {
            mParamData.setFactor(mOldVolume);
            mVideoEditorHandler.getEditorVideo().setOriginalMixFactor(mParamData.getFactor());
        }
        mVideoEditorHandler.onBack();
    }

}
