package com.mobnote.videoedit.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mobnote.golukmain.R;
import com.mobnote.util.GlideUtils;

public class VideoChooserAdapter extends BaseAdapter{
	private List<String> mFileNameList;

    private LayoutInflater mInflater;
    private Context mContext;
    LinearLayout.LayoutParams params;

    private final String filePath = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
			+ "goluk_carrecorder" + File.separator + "image";

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
        } else {
            viewTag = (ItemViewTag) convertView.getTag();
        }

        String thumbPath = mFileNameList.get(position).replace(".mp4", ".jpg");
		GlideUtils.loadImage(mContext, viewTag.mVidThumbIv, filePath + File.separator + thumbPath, R.drawable.album_default_img);

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
