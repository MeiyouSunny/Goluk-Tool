package com.rd.veuisdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rd.veuisdk.R;

import java.util.List;

public class BucketListAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mBucketList;
    private boolean mIsVideo;

    public BucketListAdapter(Context context, List<String> list, boolean isVideo) {
        mContext = context;
        this.mBucketList = list;
        this.mIsVideo = isVideo;
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
    public Object getItem(int position) {
        return null;
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

        TextView tv = (TextView) convertView.findViewById(R.id.tvBucketItem);
        if (mIsVideo) {
            if (position == 0) {
                tv.setText(R.string.allvideo);
            } else {
                tv.setText(mBucketList.get(position - 1));
            }
        } else {
            if (position == 0) {
                tv.setText(R.string.allphoto);
            } else if (position == 1) {
                tv.setText(R.string.ablum);
            } else {
                tv.setText(mBucketList.get(position - 2));
            }
        }

        return convertView;
    }

}
