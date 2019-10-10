package com.mobnote.t1sp.ui.setting.selection;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.t1sp.bean.SettingValue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import likly.mvp.MvpBinder;
import likly.view.repeat.OnHolderClickListener;
import likly.view.repeat.RepeatView;

@MvpBinder(
)
public class SelectionActivity extends BackTitleActivity implements OnHolderClickListener<SettingSelectionViewHolder> {

    public static final int TYPE_CAPTURE_TIME = 1;
    public static final int TYPE_G_SENSOR = 2;

    @BindView(R2.id.tv_wonderful_video_capture_hint_desc)
    TextView mTvWonderfulCaptureHint;
    @BindView(R2.id.repeater)
    RepeatView<SettingValue, SettingSelectionViewHolder> mRepeatView;

    private List<SettingValue> mValues;
    private String mSelectedLable;
    private int mType;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_settings_selection;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();

        Intent data = getIntent();
        int titleRes = data.getIntExtra("title", -1);
        if (titleRes != -1)
            setTitle(titleRes);

        String[] lables = getIntent().getStringArrayExtra("values");
        mSelectedLable = getIntent().getStringExtra("selectedLable");
        mValues = new ArrayList<>(lables.length);
        for (int i = 0; i < lables.length; i++) {
            SettingValue settingValue = new SettingValue();
            settingValue.value = i;
            settingValue.description = lables[i];
            if (TextUtils.equals(mSelectedLable, lables[i]))
                settingValue.isSelected = true;
            mValues.add(settingValue);
        }

        if (mValues != null && !mValues.isEmpty()) {
            mRepeatView.viewManager().bind(mValues);
            mRepeatView.layoutAdapterManager().showRepeatView();
            mRepeatView.complete();
        }

        mType = data.getIntExtra("type", -1);
        if (mType != -1) {
            mTvWonderfulCaptureHint.setVisibility(android.view.View.VISIBLE);
            if (mType == TYPE_CAPTURE_TIME) {
                mTvWonderfulCaptureHint.setText(R.string.str_settings_wonderful_video_type_ts1p_hint_text);
            } else if (mType == TYPE_G_SENSOR) {
                mTvWonderfulCaptureHint.setText(R.string.hint_gsensor_t1sp);
            }
        }

        mRepeatView.onClick(this);
    }

    @Override
    public void onHolderClick(SettingSelectionViewHolder holder) {
        final SettingValue selectValue = holder.getData();
        // 没有变化
        if (selectValue.isSelected)
            finish();
        // 更新UI
        for (SettingValue value : mValues)
            value.isSelected = false;
        selectValue.isSelected = true;
        mRepeatView.getAdapter().notifyDataSetChanged();

        // 返回数据
        Intent data = new Intent();
        data.putExtra("value", selectValue);
        setResult(RESULT_OK, data);
        finish();
    }

}
