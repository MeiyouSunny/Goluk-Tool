package com.goluk.videoedit.adapter;

import java.util.ArrayList;
import java.util.List;

import com.goluk.videoedit.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class VideoChooserAdapter extends BaseAdapter{
	private List<String> mFileNameList;

    private LayoutInflater mInflater;
    private Context mContext;
    LinearLayout.LayoutParams params;

    public VideoChooserAdapter(Context context, List<String> mVideoPathList) {
        mFileNameList = mVideoPathList;
        mContext = context;
        mInflater = LayoutInflater.from(context);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); 
        params.gravity = Gravity.CENTER;
    }  

    public int getCount() {
        return mFileNameList.size();
    }

    public Object getItem(int position) {
        return mFileNameList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewTag viewTag;
          
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_video_chooser_list, null);

            viewTag = new ItemViewTag((ImageView) convertView.findViewById(R.id.iv_video_chooser_item));
            convertView.setTag(viewTag);
        } else
        {
            viewTag = (ItemViewTag) convertView.getTag();
        }

        viewTag.mVidThumbIv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.img_ae_trailer));
        //viewTag.mVidThumbIv.setLayoutParams(params);
        return convertView;
    }

    class ItemViewTag  
    {  
        protected ImageView mVidThumbIv;

        public ItemViewTag(ImageView iv )
        {
            this.mVidThumbIv  = iv;
        }
    }
}
