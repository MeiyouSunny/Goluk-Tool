package com.rd.veuisdk.demo.zishuo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BaseRVAdapter;

import java.util.ArrayList;

/**
 * 字说 文字修改描边适配器
 */
public class ColorAdapter extends BaseRVAdapter<ColorAdapter.ViewHolder> {

    private Context mContext;
    public ArrayList<String> mColors;
    private int mType = 0;//默认是字体颜色  1表示背景

    public ColorAdapter(Context context, int type) {
        mColors = new ArrayList<>();
        this.mContext = context;
        this.mType = type;
        getColor();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mType == 0) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_color, parent, false));
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_color_bg, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String color = mColors.get(position);
        if (mType == 0) {
            holder.mBtnColor.setBackgroundColor(Color.parseColor(color));
            if (position == lastCheck) {
                holder.mLlColorChoose.setBackground(mContext.getResources().getDrawable(R.drawable.item_color_bg));
            } else {
                holder.mLlColorChoose.setBackground(null);
            }
            holder.mBtnColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onItemClick(position, color);
                    }
                }
            });
        } else if (mType == 1) {
            holder.mBtnColorBg.setBackgroundColor(Color.parseColor(color));
            holder.mBtnColorBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onItemClick(position, color);
                    }
                }
            });
            if (position == lastCheck) {
                holder.mLlColorChooseBg.setBackground(mContext.getResources().getDrawable(R.drawable.item_color_bg));
                holder.mIvAlpha.setVisibility(View.VISIBLE);
            } else {
                holder.mLlColorChooseBg.setBackground(null);
                holder.mIvAlpha.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mColors.size();
    }

    public void setChecked(int checkId) {
        lastCheck = checkId;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mLlColorChoose;
        Button mBtnColor;
        RelativeLayout mLlColorChooseBg;
        Button mBtnColorBg;
        ImageView mIvAlpha;

        public ViewHolder(View itemView) {
            super(itemView);
            if (mType == 0) {
                mLlColorChoose = itemView.findViewById(R.id.ll_color_choose);
                mBtnColor = itemView.findViewById(R.id.btn_color);
            } else {
                mLlColorChooseBg = itemView.findViewById(R.id.ll_color_choose);
                mBtnColorBg = itemView.findViewById(R.id.btn_color_bg);
                mIvAlpha = itemView.findViewById(R.id.iv_alpha);
            }
        }
    }

    private void getColor() {
        String[] color = new String[] {
                "#ffffff", "#484848", "#000000", "#e8ce6b", "#f9b73c",
                "#e3573b", "#be213b", "#00ffff", "#5da9cf", "#0695b5",
                "#2791db", "#3564b7", "#e9c930", "#a6b45c", "#87a522",
                "#32b16c", "#017e54", "#fdbacc", "#ff5a85", "#ca4f9b",
                "#71369a", "#6720d4", "#164c6e", "#9f9f9f", "#E5004F",
                "#ffd500", "#a9e38a43", "#13cadb", "#80f87a00", "#FC1C8F",
                "#FFFFCC", "#FFCC00", "#CC9909", "#663300", "#660000",
                "#FF6600", "#663333", "#CC6666", "#FF6666", "#FF0000", "#FFFF99",
                "#FFCC66", "#FF9900", "#FF9966", "#CC3300", "#996666", "#FFCCCC",
                "#FF3300", "#FF6666", "#FFCC33", "#CC6600", "#FF6633", "#996633",
                "#FF3333", "#990000", "#CC9966", "#FFFF33", "#CC9933", "#993300",
                "#330000", "#993333", "#CC3333", "#CC0000", "#FFCC99", "#FFFF00",
                "#996600", "#FF9933", "#CC9999", "#CC6633"
        };
        for (String s : color) {
           mColors.add(s);
        }
    }

}
