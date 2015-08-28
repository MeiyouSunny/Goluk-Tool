package cn.com.mobnote.golukmobile.newest;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
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
	private Typeface mTypeface = null;

	public WonderfulSelectedAdapter(Context context) {
		mContext = context;
		mDataList = new ArrayList<JXListItemDataInfo>();
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		density = SoundUtils.getInstance().getDisplayMetrics().density;
		mTypeface = Typeface.createFromAsset (context.getAssets() , "AdobeHebrew-Bold.otf" ); 
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
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.wonderful_selected_item, null);
			holder = new ViewHolder();
			holder.main = (RelativeLayout) convertView.findViewById(R.id.main);
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
//		holder.mTitleName.getPaint().setFakeBoldText(true);
		holder.mTitleName.setText( getTitleString( info.ztitle ) );
//		if (!TextUtils.isEmpty(info.ztag)) {
//			holder.mTagName.setText(info.ztag);
//			holder.mTagName.setVisibility(View.VISIBLE);
//		}else {
			holder.mTagName.setVisibility(View.GONE);
//		}
		
		if ("-1".equals(info.clicknumber)) {
			holder.mVideoLayout.setVisibility(View.GONE);
		}else {
			holder.mVideoNum.setText(getFormatNumber(info.clicknumber));
			holder.mVideoLayout.setVisibility(View.VISIBLE);
		}
		
		GolukDebugUtils.e("", "BBBBBBB===1111===videonumber="+info.videonumber+"===="+info.ztitle);
		if ("-1".equals(info.videonumber)) {
			holder.mLookLayout.setVisibility(View.GONE);
		}else {
			holder.mLookNum.setText(getFormatNumber(info.videonumber));
			holder.mLookLayout.setVisibility(View.VISIBLE);
		}
		
		if (!TextUtils.isEmpty(info.jxdate)) {
			if(0 == arg0) {
				holder.mDate.setVisibility(View.GONE);
			}else {
				holder.mDate.setTypeface (mTypeface);
				holder.mDate.setText(getTime(info.jxdate));
				holder.mDate.setVisibility(View.VISIBLE);
			}
		}else {
			holder.mDate.setVisibility(View.GONE);
		}
		
		holder.main.setOnTouchListener(new ClickWonderfulSelectedListener(mContext, info, this));
		loadImage(holder.imageLayout, info.jximg, info.jtypeimg);
 
		return convertView;
	}
	
	private String getTitleString(String title) {
		String name = "";
		int len = title.length();
		if (len > 15) {
			int size = len/15 + 1;
			for (int i=0; i<size; i++) {
				int index = 15*(i + 1);
				if(index < len) {
					name += title.substring(15*i, index) + "\n";	
				}else {
					name += title.substring(15*i);
				}
			}
		}else {
			name = title;
		}
		
		return name;
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getTime(String date) {
		String time = null;
		try {
			long curTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date strtodate = formatter.parse(date);

			Date curDate = new Date(curTime);
			int curYear = curDate.getYear();
			int history = strtodate.getYear();
			
			if (curYear == history) {
				SimpleDateFormat jn = new SimpleDateFormat("-MM.dd-");
				return jn.format(strtodate);// 今年内：月日更新
			}else {
				SimpleDateFormat jn = new SimpleDateFormat("-yyyy.MM.dd-");
				return jn.format(strtodate);// 非今年：年月日更新
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return time;
	}
	
	private void loadImage(RelativeLayout mPlayerLayout, String url, String iconUrl) {
		final int id = 3123;
		final int iconId = 3124;
		SimpleDraweeView view, icon;
		int count = mPlayerLayout.getChildCount();
		if (0 == count) {
			mPlayerLayout.removeAllViews();
			view = new SimpleDraweeView(mContext);
			view.setId(id);
			int height = (int) ((float) width / 1.77f);
			RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
			mPlayerLayout.addView(view, mPreLoadingParams);
			
			icon = new SimpleDraweeView(mContext);
			icon.setId(iconId);
			RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams((int)(39*density), (int)(20.33*density));
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mPlayerLayout.addView(icon, iconParams);
		}else {
			view = (SimpleDraweeView)mPlayerLayout.findViewById(id);
			icon = (SimpleDraweeView)mPlayerLayout.findViewById(iconId);
		}
		
		GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(mContext.getResources());
		GenericDraweeHierarchy mGenericDraweeHierarchy = builder.setFadeDuration(300)
				.setPlaceholderImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setFailureImage(mContext.getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
				.setActualImageScaleType(ScaleType.FIT_XY).build();
		view.setHierarchy(mGenericDraweeHierarchy);
		view.setImageURI(Uri.parse(url));
		
		if (TextUtils.isEmpty(iconUrl)) {
			icon.setVisibility(View.GONE);
		}else {
			icon.setVisibility(View.VISIBLE);
			GenericDraweeHierarchyBuilder iconbuilder = new GenericDraweeHierarchyBuilder(mContext.getResources());
	        GenericDraweeHierarchy iconhierarchy = iconbuilder
	                        .setFadeDuration(300)
	                    .setActualImageScaleType(ScaleType.FIT_XY)
	                    .build();
	        icon.setHierarchy(iconhierarchy);
	        icon.setImageURI(Uri.parse(iconUrl));
		}

	}

	public static class ViewHolder {
		RelativeLayout main;
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
//		this.notifyDataSetChanged();
	}
	
	private String getFormatNumber(String fmtnumber) {
		String number;

		int wg = Integer.parseInt(fmtnumber);

		if (wg < 100000) {
			DecimalFormat df = new DecimalFormat("#,###");
			number = df.format(wg);
		} else {
			number = "100,000+";
		}
		return number;
	}

}

