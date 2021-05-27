package com.mobnote.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.videosuqare.RingView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by crack on 2016/6/13.
 */
public class RingViewDialogFragment extends DialogFragment {
    private RingView mRingView;
    private TextView mProgressTV;
    private View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog_No_Border);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mRootView = inflater.inflate(R.layout.dialog_ring_view, container, false);
        mRingView = (RingView) mRootView.findViewById(R.id.ringview_loading);
        mProgressTV = (TextView) mRootView.findViewById(R.id.tv_loading_progress);
        return mRootView;
    }

    public void setRingViewProgress(int progress) {
        mRingView.setProcess(progress);
    }

    public void setTextProgress(String str) {
        mProgressTV.setText(str);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
