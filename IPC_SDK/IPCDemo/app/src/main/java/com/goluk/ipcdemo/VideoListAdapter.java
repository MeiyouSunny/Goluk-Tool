package com.goluk.ipcdemo;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.goluk.ipcsdk.bean.VideoInfo;

import java.util.ArrayList;

/**
 * Created by hanzheng on 2016/6/4.
 */
public class VideoListAdapter extends BaseAdapter {

    private Context mContext;

    public VideoListAdapter(Context context) {
        mContext = context;
    }

    public ArrayList<VideoInfo> videos;

    public void setData(ArrayList<VideoInfo> data) {
        videos = data;
    }

    @Override
    public int getCount() {
        if(videos!=null && videos.size()>0){
            return videos.size();
        }else{
            return 0;
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
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.video_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.videoname = (TextView) convertView.findViewById(R.id.video_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final VideoInfo videoinfo = videos.get(position);
        viewHolder.videoname.setText(videoinfo.filename);
        viewHolder.videoname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,VideoDetailActivity.class);
                intent.putExtra("filename",videoinfo.filename);
                intent.putExtra("filetime",videoinfo.time);
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }


    public class ViewHolder {
        public TextView videoname;
    }
}
