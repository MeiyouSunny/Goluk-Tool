package com.mobnote.t1sp.ui.setting.selection;

import android.content.Intent;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.t1sp.bean.SettingValue;

import java.util.List;

import butterknife.BindView;
import likly.mvp.MvpBinder;
import likly.view.repeat.OnHolderClickListener;
import likly.view.repeat.RepeatView;

@MvpBinder(
)
public class SelectionActivity extends BackTitleActivity implements OnHolderClickListener<SettingSelectionViewHolder> {

    @BindView(R2.id.title)
    TextView mTitle;
    @BindView(R2.id.repeater)
    RepeatView<SettingValue, SettingSelectionViewHolder> mRepeatView;

    private List<SettingValue> mValues;

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

        mValues = getIntent().getParcelableArrayListExtra("values");
        if (mValues != null && !mValues.isEmpty()) {
            mRepeatView.viewManager().bind(mValues);
            mRepeatView.layoutAdapterManager().showRepeatView();
            mRepeatView.complete();
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
