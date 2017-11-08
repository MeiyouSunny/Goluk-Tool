package com.mobnote.t1sp.ui.setting.selection;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.t1sp.base.ui.BaseViewHolder;
import com.mobnote.t1sp.bean.SettingValue;

import butterknife.BindView;

public class SettingSelectionViewHolder extends BaseViewHolder<SettingValue> {

    @BindView(R2.id.ic_selected)
    ImageView mSelected;
    @BindView(R2.id.value)
    TextView mValue;

    @Override
    protected void onBindData(SettingValue settingValue) {
        super.onBindData(settingValue);

        mValue.setText(settingValue.description);
        final boolean isSelected = settingValue.isSelected;
        int fontColor = isSelected ? R.color.font_blue : R.color.setting_black_color;
        mValue.setTextColor(getView().getResources().getColor(fontColor));
        mSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
    }

    @Override
    protected int getViewHolderLayout() {
        return R.layout.viewholder_setting_selection_item;
    }

}
