package com.rd.veuisdk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class BucketListAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mBucketList;
    private boolean mIsVideo;

    public BucketListAdapter(Context context, boolean isVideo) {
        mContext = context;
        this.mBucketList = new ArrayList<>();
        this.mIsVideo = isVideo;
    }

    public void update(List<String> bucketList) {
        mBucketList.clear();
        if (null != bucketList && bucketList.size() > 0) {
            mBucketList.addAll(bucketList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mBucketList == null) {
            return 0;
        } else {
            if (mIsVideo) {
                return mBucketList.size() + 1;
            } else {
                return mBucketList.size() + 2;
            }
        }
    }

    @Override
    public String getItem(int position) {
        String result= mBucketList.get(position);
        if (TextUtils.isEmpty(result)) {
            result = "n/a";
        }
        return result;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.dcimbucket_item, null);
        }
        TextView tv = Utils.$(convertView, R.id.tvBucketItem);
        if (mIsVideo) {
            if (position == 0) {
                tv.setText(R.string.allvideo);
            } else {
                tv.setText(getItem(position - 1));
            }
        } else {
            if (position == 0) {
                tv.setText(R.string.allphoto);
            } else if (position == 1) {
                tv.setText(R.string.album);
            } else {
                tv.setText(getItem(position - 2));
            }
        }
        return convertView;
    }

}
