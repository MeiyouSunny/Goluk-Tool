package com.rd.veuisdk.fragment.splice;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.utils.ISpliceHandler;

/**
 * 拼接-编辑单个画框中的资源
 */
public class SpliceEditItemFragment extends BaseFragment {

    private View mVolume, mRotate, mTrim, mReplace;
    private ISpliceHandler mSpliceHandler;

    public static SpliceEditItemFragment newInstance() {
        Bundle args = new Bundle();

        SpliceEditItemFragment fragment = new SpliceEditItemFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSpliceHandler = (ISpliceHandler) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_splice_edit_item_layout, container, false);
        mVolume = $(R.id.volumeLayout);
        mRotate = $(R.id.edit_rotate);
        mTrim = $(R.id.edit_trim);
        mReplace = $(R.id.edit_replace);

        mSbFactor = $(R.id.sbFactor);
        mSbFactor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser&&null!=mMediaObject) {
                    mMediaObject.setMixFactor(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (null!=mMediaObject) {
                    mMediaObject.setMixFactor(seekBar.getProgress());
                }
            }
        });

        return mRoot;
    }

    private void initMediaUI() {
        if (null != mMediaObject && null != mVolume) {
            if (mMediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                mTrim.setVisibility(View.VISIBLE);
                mSbFactor.setProgress(mMediaObject.getMixFactor());
                mVolume.setVisibility(View.VISIBLE);
            } else {
                mTrim.setVisibility(View.GONE);
                mVolume.setVisibility(View.GONE);
            }
        }
    }

    private MediaObject mMediaObject;
    private SeekBar mSbFactor;

    /**
     * 当前画框绑定的媒体
     *
     * @param media
     */
    public void setCurrentMedia(MediaObject media) {
        mMediaObject = media;
        initMediaUI();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMediaUI();

        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpliceHandler.onRotate();
            }
        });
        mTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSpliceHandler.onTrim();
            }
        });
        mReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSpliceHandler.onReplace();
            }
        });

        $(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSpliceHandler.onExitEdit();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMediaObject = null;
        mVolume = null;
    }
}
