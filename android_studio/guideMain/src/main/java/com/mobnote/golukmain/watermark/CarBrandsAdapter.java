package com.mobnote.golukmain.watermark;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.Text;
import com.bumptech.glide.Glide;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.watermark.bean.CarBrandBean;

import java.util.List;

/**
 * Created by pavkoo on 2016/7/19.
 * Car brands list adapter
 */
public class CarBrandsAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private List<CarBrandBean> mList;
    private LayoutInflater inflater;
    private Context context;
    private String currentSelected;

    public CarBrandsAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setCurrentSelected(String currentSelected) {
        this.currentSelected = currentSelected;
        notifyDataSetChanged();
    }

    public void setList(List<CarBrandBean> list) {
        this.mList = list;
        notifyDataSetChanged();
    }


    @Override
    public long getHeaderId(int i) {
        return mList == null ? 0 : mList.get(i).alphaName.toUpperCase().subSequence(0, 1).charAt(0);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? 0 : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CarBrandViewHolder holder;

        if (convertView == null) {
            holder = new CarBrandViewHolder();
            convertView = inflater.inflate(R.layout.listview_car_brands_item, parent, false);
            holder.mImgLogo = (ImageView) convertView.findViewById(R.id.iv_car_brand_img);
            holder.mTvName = (TextView) convertView.findViewById(R.id.tv_car_brand_name);
            holder.mImgSelected = (ImageView) convertView.findViewById(R.id.iv_car_brand_selected);
            convertView.setTag(holder);
        } else {
            holder = (CarBrandViewHolder) convertView.getTag();
        }

        holder.mTvName.setText(mList.get(position).name);
        Glide.with(context).load(mList.get(position).logoUrl).into(holder.mImgLogo);
        //Mark Current
        if (!TextUtils.isEmpty(currentSelected) && currentSelected.equals((mList.get(position).code))) {
            holder.mTvName.setTextColor(context.getResources().getColor(R.color.photoalbum_text_color));
            holder.mImgSelected.setVisibility(View.VISIBLE);
        } else {
            holder.mTvName.setTextColor(context.getResources().getColor(R.color.user_personal_homepage_text));
            holder.mImgSelected.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        CarBrandsHeaderViewHolder holder;
        if (convertView == null) {
            holder = new CarBrandsHeaderViewHolder();
            convertView = inflater.inflate(R.layout.listview_car_brands_head, parent, false);
            holder.mTvTitle = (TextView) convertView.findViewById(R.id.tv_car_brand_group);
            convertView.setTag(holder);
        } else {
            holder = (CarBrandsHeaderViewHolder) convertView.getTag();
        }
        String headerText = String.valueOf(mList.get(position).alphaName.toUpperCase().subSequence(0, 1).charAt(0));
        holder.mTvTitle.setText(headerText);
        return convertView;
    }

    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mList.get(i).alphaName.toUpperCase();
            char firstChar = sortStr.charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    class CarBrandsHeaderViewHolder {
        TextView mTvTitle;
    }

    class CarBrandViewHolder {
        ImageView mImgLogo;
        TextView mTvName;
        ImageView mImgSelected;
    }

}