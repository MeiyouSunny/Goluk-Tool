package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.tiros.debug.GolukDebugUtils;

@SuppressLint("InflateParams")
public class WonderfulSelectedAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<JXListItemDataInfo> mDataList = null;
	private int count = 0;
	private int width = 0;
	private float density = 0;
	/** 滚动中锁标识 */
	private boolean lock = false;

	public WonderfulSelectedAdapter(Context context) {
		mContext = context;
		mDataList = new ArrayList<JXListItemDataInfo>();
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
	}

	public void setData(List<JXListItemDataInfo> data) {
		mDataList.clear();
		mDataList.addAll(data);
		count = mDataList.size();
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	ViewHolder holder;

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		GolukDebugUtils.e("", "TTTTTTT==wonderful==arg0=="+arg0+"==convertView="+convertView);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.wonderful_selected_item, null);
			holder = new ViewHolder();
			holder.mDate = (TextView)convertView.findViewById(R.id.mDate);
			holder.imageLayout = (RelativeLayout) convertView.findViewById(R.id.imageLayout);
			holder.mTitleName = (TextView) convertView.findViewById(R.id.mTitleName);
			holder.mTagName = (TextView) convertView.findViewById(R.id.mTagName);
			holder.mVideoLayout = (LinearLayout) convertView.findViewById(R.id.mVideoLayout);
			holder.mLookLayout = (LinearLayout) convertView.findViewById(R.id.mLookLayout);
			holder.mVideoNum = (TextView) convertView.findViewById(R.id.mVideoNum);
			holder.mLookNum = (TextView) convertView.findViewById(R.id.mLookNum);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		JXListItemDataInfo info = mDataList.get(arg0);
		holder.mTitleName.getPaint().setFakeBoldText(true);
		holder.mTitleName.setText(info.ztitle);
		if (!TextUtils.isEmpty(info.ztag)) {
			holder.mTagName.setText(info.ztag);
			holder.mTagName.setVisibility(View.VISIBLE);
		}else {
			holder.mTagName.setVisibility(View.GONE);
		}
		
		if ("-1".equals(info.videonumber)) {
			holder.mVideoLayout.setVisibility(View.GONE);
		}else {
			holder.mVideoNum.setText(info.videonumber);
			holder.mVideoLayout.setVisibility(View.VISIBLE);
		}
		
		if ("-1".equals(info.clicknumber)) {
			holder.mLookLayout.setVisibility(View.GONE);
		}else {
			holder.mLookNum.setText(info.clicknumber);
			holder.mLookLayout.setVisibility(View.VISIBLE);
		}
		
		if (!TextUtils.isEmpty(info.jxdate)) {
			holder.mDate.setText(info.jxdate);
			holder.mDate.setVisibility(View.VISIBLE);
		}else {
			holder.mDate.setVisibility(View.GONE);
		}
		
		holder.imageLayout.setOnClickListener(new ClickWonderfulSelectedListener(mContext, info));
		loadImage(holder.imageLayout, info.jximg, info.jtypeimg);
 
		return convertView;
	}
	
	private void loadImage(RelativeLayout mPlayerLayout, String url, String iconUrl) {
        mPlayerLayout.removeAllViews();
        SimpleDraweeView view = new SimpleDraweeView(mContext);
        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
        GenericDraweeHierarchy hierarchy = builder
                        .setFadeDuration(300)
                    .setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
                    .setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
                    .setActualImageScaleType(ScaleType.FIT_XY)
                    .build();
        view.setHierarchy(hierarchy);

        if (!lock) {
        	view.setImageURI(Uri.parse(url));
        }
                
        int height = (int) ((float) width / 1.77f);
        RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
        mPreLoadingParams.addRule(RelativeLayout.BELOW, R.id.mDate);
        mPlayerLayout.addView(view, mPreLoadingParams);
        
        if(!TextUtils.isEmpty(iconUrl)) {
        	SimpleDraweeView icon = new SimpleDraweeView(mContext);
            GenericDraweeHierarchyBuilder iconbuilder = new GenericDraweeHierarchyBuilder(mContext.getResources());
            GenericDraweeHierarchy iconhierarchy = iconbuilder
                            .setFadeDuration(300)
//                            .setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tag_1), ScaleType.FIT_XY)
//                        .setFailureImage(mContext.getResources().getDrawable(R.drawable.tag_1), ScaleType.FIT_XY)
                        .setActualImageScaleType(ScaleType.FIT_XY)
                        .build();
            icon.setHierarchy(iconhierarchy);
            
            if (!lock) {
            	icon.setImageURI(Uri.parse(iconUrl));
            }
            
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams((int)(39*density), (int)(20.33*density));
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mPlayerLayout.addView(icon, iconParams);
        }
      

	}

	public static class ViewHolder {
		RelativeLayout imageLayout;
		TextView mTitleName;
		TextView mTagName;
		LinearLayout mVideoLayout;
		LinearLayout mLookLayout;
		TextView mVideoNum;
		TextView mLookNum;
		TextView mDate;
	}
	
	/**
	 * 锁住后滚动时禁止下载图片
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void lock() {
		lock = true;
	}
	
	/**
	 * 解锁后恢复下载图片功能
	 * @author xuhw
	 * @date 2015年6月8日
	 */
	public void unlock() {
		lock = false;
		this.notifyDataSetChanged();
	}

}

