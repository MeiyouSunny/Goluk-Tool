package com.rd.veuisdk.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.FilterFragment;

public class MyFilterHorizontalListItem extends HorizontalListItem {

    /**
     * 缩略图
     */
    private ImageView imageThumbnail;

    /**
     * 描述信息
     */
    private TextView descriptionInfo;

    /**
     * 选中标示
     */
    private ImageView selectedFlage;

    /**
     * 用户数据
     */
    private CustomData1 data;

    /**
     * 相应点击乐库弹出配乐界面的接口
     */
    private OnFilterClickListener mFilterListener;

    private boolean isChildGroup = true;

    /**
     * 初始化时需要传入的资源
     *
     * @param context
     * @param listener
     * @param data
     * @param itemId
     * @param ischildgroup 区别滤镜 （原图 午茶（暖风吹...））默认传true
     */
    public MyFilterHorizontalListItem(Context context,
                                      OnFilterClickListener listener, CustomData1 data, int itemId,
                                      boolean ischildgroup) {
        super(context, itemId);

        this.isChildGroup = ischildgroup;
        this.data = data;
        this.mFilterListener = listener;
        initData();
    }

    /**
     * 支持主题
     *
     * @param context
     * @param listener
     * @param data
     * @param itemId
     */
    public MyFilterHorizontalListItem(Context context,
                                      OnFilterClickListener listener, CustomData1 data, int itemId) {
        super(context, itemId);
        this.data = data;
        this.mFilterListener = listener;
        initData();
    }

    @SuppressLint("InflateParams")
    @Override
    public void initContentView() {
        contentView = inflater.inflate(
                R.layout.rdveuisdk_filter_horizontal_item_layout, null);

        imageThumbnail = (ImageView) contentView
                .findViewById(R.id.iv_item_thubmnail);

        descriptionInfo = (TextView) contentView
                .findViewById(R.id.tv_item_text);

        selectedFlage = (ImageView) contentView
                .findViewById(R.id.iv_item_selected);
    }

    /**
     * 为布局中个各个控件设置相对应的值
     */
    private void initData() {

        ImageCacheUtils.getInstance(_context).loadBitmap(
                data.getImageViewResourceId(), imageThumbnail);

        descriptionInfo.setText(data.getDescriptionInfo());

        selectedFlage.setImageResource(data.getSelectedFlagResourceId());

        // 处理点击事件
        imageThumbnail.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (listItemClickListener != null) {
                    listItemClickListener.setOnListItemClick(itemId);
                }

                if (mFilterListener != null) {
                    mFilterListener.onSwitchFilterClick(itemId,
                            MyFilterHorizontalListItem.this);
                }
            }
        });

    }

    @Override
    public void showSelectedFlag(int strId) {
        // Log.e(TAG, "showSelectedFlag" + "....strId" + strId + "....itemId"
        // + itemId);
        selectedFlage.setVisibility(View.VISIBLE);
        descriptionInfo.setBackgroundColor(_context.getResources().getColor(
                R.color.music_text_selected));
    }

    @Override
    public void hideSelectedFlag() {
        // Log.e(TAG, "hideSelectedFlag" + "....strId" + "....itemId" + itemId
        // + "..........................FilterFragment.checkFilterId."
        // + FilterFragment.checkFilterId);
        if (isChildGroup) {
            selectedFlage.setVisibility(View.GONE);
            descriptionInfo.setBackgroundColor(_context.getResources()
                    .getColor(R.color.music_text_background));
        } else {
            if (FilterFragment.checkFilterId != 0) {
                selectedFlage.setVisibility(View.GONE);
                descriptionInfo.setBackgroundColor(_context.getResources()
                        .getColor(R.color.music_text_background));
            }

        }

    }

}
