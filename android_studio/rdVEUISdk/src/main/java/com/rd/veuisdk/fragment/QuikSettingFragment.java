package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.rd.veuisdk.IVideoEditorQuikHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.quik.QuikHandler;

/**
 * quik 预览比列调整
 * @author JIAN
 * @create 2018/10/9
 * @Describe
 */
public class QuikSettingFragment extends BaseFragment {

    /**
     * @return
     */
    public static QuikSettingFragment newInstance() {
        return new QuikSettingFragment();
    }

    private IVideoEditorQuikHandler mHlrVideoEditor;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mHlrVideoEditor = (IVideoEditorQuikHandler) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_quik_setting_layout, null);
        RadioGroup radioGroup = (RadioGroup) mRoot.findViewById(R.id.mProGroup);
        mRoot.findViewById(R.id.tvProportion169).setOnClickListener(mProportionListener);
        mRoot.findViewById(R.id.tvProportion).setOnClickListener(mProportionListener);
        mRoot.findViewById(R.id.tvProportion916).setOnClickListener(mProportionListener);

        if (mHlrVideoEditor.getProportion() == QuikHandler.ASP_1) {
            radioGroup.check(R.id.tvProportion);
        } else if (mHlrVideoEditor.getProportion() == QuikHandler.ASP_916) {
            radioGroup.check(R.id.tvProportion916);
        } else {
            radioGroup.check(R.id.tvProportion169);
        }
        return mRoot;
    }

    private View.OnClickListener mProportionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.tvProportion169) {
                mHlrVideoEditor.onProportion(QuikHandler.ASP_169);
            } else if (id == R.id.tvProportion) {
                mHlrVideoEditor.onProportion(QuikHandler.ASP_1);
            } else {
                mHlrVideoEditor.onProportion(QuikHandler.ASP_916);
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoot = null;
    }

    @Override
    public int onBackPressed() {
        return 1;
    }
}
