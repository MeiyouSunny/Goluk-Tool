package com.rd.veuisdk.fragment.splice;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.ui.ColorDragScrollView;
import com.rd.veuisdk.ui.ColorPicker;
import com.rd.veuisdk.utils.ISpliceHandler;

/**
 * 拼接-描边
 */
public class SpliceBorderFragment extends BaseFragment {


    public static SpliceBorderFragment newInstance() {
        Bundle args = new Bundle();

        SpliceBorderFragment fragment = new SpliceBorderFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private ISpliceHandler mSpliceHandler;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSpliceHandler = (ISpliceHandler) getActivity();
    }

    private SeekBar mSeekBar;
    private ColorDragScrollView mDragScrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_splice_border_layout, container, false);
        mSeekBar = $(R.id.sbBorder);
        mDragScrollView = $(R.id.scrollColorPicker);
        return mRoot;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        float scale = mSpliceHandler.getScale();
        int progress = (int) ((1 - (scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE)) * 100);
        mSeekBar.setProgress(progress);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mDragScrollView.setColorChangedListener(new ColorPicker.IColorListener() {
            @Override
            public void getColor(int color, int position) {
                mSpliceHandler.setBackgroundColor(color);
            }
        });
        mDragScrollView.setChecked(mSpliceHandler.getBgColor());
        $(R.id.ivColorDefault).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = Color.parseColor("#FFFFFF");
                mDragScrollView.setChecked(color);
                mSpliceHandler.setBackgroundColor(color);
            }
        });

    }

    public static final float MAX_SCALE = 1f;
    private final float MIN_SCALE = 0.85f;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mSpliceHandler.setScale(MAX_SCALE - (MAX_SCALE - MIN_SCALE) * progress / 100);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
