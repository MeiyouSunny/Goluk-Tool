package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

@SuppressLint("InflateParams")
public class NewestAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<JXListItemDataInfo> mDataList = null;
	private int count = 0;
	private int width = 0;
	private float density = 0;
	/** 滚动中锁标识 */
	private boolean lock = false;
	private final int FIRST_TYPE = 0;
	private final int OTHERS_TYPE = 1;
	
	public NewestAdapter(Context context) {
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
	
	@Override
	public int getViewTypeCount() {
		return 2;
	};

	@Override
	public int getItemViewType(int position) {
		if (position==0) {  
            return FIRST_TYPE;  
        } else {  
            return OTHERS_TYPE;  
        }  
	};

	ViewHolder holder;

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		int type = getItemViewType(arg0);
		if (FIRST_TYPE == type) {	
			convertView = getHeadView();
		}else {
			if(null == convertView) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.newest_list_item, null);
				holder.imageLayout = (RelativeLayout)convertView.findViewById(R.id.imageLayout);
				holder.headimg = (ImageView)convertView.findViewById(R.id.headimg);
				holder.nikename = (TextView)convertView.findViewById(R.id.nikename);
				holder.time = (TextView)convertView.findViewById(R.id.time);
				holder.function = (ImageView)convertView.findViewById(R.id.function);
				
				holder.praiseLayout = (LinearLayout)convertView.findViewById(R.id.praiseLayout);
				holder.zanIcon = (ImageView)convertView.findViewById(R.id.zanIcon);
				holder.zanText = (TextView)convertView.findViewById(R.id.zanText);
				
				holder.commentLayout = (LinearLayout)convertView.findViewById(R.id.commentLayout);
				holder.commentIcon = (ImageView)convertView.findViewById(R.id.commentIcon);
				holder.commentText = (TextView)convertView.findViewById(R.id.commentText);
				
				holder.shareLayout = (LinearLayout)convertView.findViewById(R.id.shareLayout);
				holder.shareIcon = (ImageView)convertView.findViewById(R.id.shareIcon);
				holder.shareText = (TextView)convertView.findViewById(R.id.shareText);
				
				holder.zText = (TextView)convertView.findViewById(R.id.zText);
				holder.weiguan = (TextView)convertView.findViewById(R.id.weiguan);
				holder.weiguan = (TextView)convertView.findViewById(R.id.weiguan);
				holder.totalcomments = (TextView)convertView.findViewById(R.id.totalcomments);
				
				holder.comment1 = (TextView)convertView.findViewById(R.id.comment1);
				holder.comment2 = (TextView)convertView.findViewById(R.id.comment2);
				holder.comment3 = (TextView)convertView.findViewById(R.id.comment3);
				
				
				int height = (int) ((float) width / 1.77f);
				RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(width, height);
				mPlayerLayoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
				holder.imageLayout.setLayoutParams(mPlayerLayoutParams);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			initView();
			
		}
		
		
		
		
	
 
		return convertView;
	}
	
	private void initView() {
		loadImage(holder.imageLayout, "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
		
		String nikename = "chradrse";
		String t_str = nikename+" "+"所得到的卡卡阿拉开口说道的";
		SpannableStringBuilder style=new SpannableStringBuilder(t_str);      
        style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikename.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);        
        holder.comment1.setText(style);  
		
        String nikename2 = "shhdhdhd";
		String t_str2 = nikename2+" "+"所三十三岁搜索得到的卡卡阿拉开口说道的";
		SpannableStringBuilder style2=new SpannableStringBuilder(t_str2);      
        style2.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikename2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);        
        holder.comment2.setText(style2);  
        
        String nikename3 = "kkkhh";
		String t_str3 = nikename3+" "+"所得到的卡卡阿ksksddh生活设施时候都会的回答很的拉开口说道的";
		SpannableStringBuilder style3=new SpannableStringBuilder(t_str3);      
         style3.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikename3.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);        
         holder.comment3.setText(style3);  
    	holder.comment3.setVisibility(View.VISIBLE);
    	
    	
    	
    	
    	
	}
	
	private View getHeadView() {
		int imagewidth = (int)((width - 10*density)/2);
		int imageheight = (int)(imagewidth * 0.56);
		RelativeLayout view = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.category_layout, null);
		RelativeLayout main = (RelativeLayout)view.findViewById(R.id.main);
		RelativeLayout liveLayout = (RelativeLayout)view.findViewById(R.id.liveLayout);
		
		int height = (int) ((float) width / 1.77f);
		RelativeLayout.LayoutParams liveLayoutParams = new RelativeLayout.LayoutParams(width, height);
		liveLayoutParams.addRule(RelativeLayout.BELOW, R.id.main);
		liveLayout.setLayoutParams(liveLayoutParams);
		RelativeLayout imagelayout = (RelativeLayout)view.findViewById(R.id.imagelayout);
		loadImage(imagelayout, "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png");
				
		for (int i=0; i<4; i++) {
			RelativeLayout item = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.category_item, null);
			main.setPadding(0, (int)(10*density), 0, 0);
			int iid = i+1111;
			item.setId(iid);
			
			RelativeLayout imageLayout = (RelativeLayout)item.findViewById(R.id.imageLayout);
			TextView mTitleName = (TextView)item.findViewById(R.id.mTitleName);
			TextView mUpdateTime = (TextView)item.findViewById(R.id.mUpdateTime);
			
			RelativeLayout.LayoutParams itemparams = new RelativeLayout.LayoutParams(imagewidth, imageheight);
			
			mTitleName.setText("＃曝光台"+i);
			mUpdateTime.setText("一分钟前更新");
			loadImage(imageLayout, "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
			
			int id = i+1111-2;
			if (i%2 == 0) {				
				itemparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				itemparams.setMargins(0, 0, (int)(10*density), (int)(10*density));
				itemparams.addRule(RelativeLayout.BELOW, id);
			}else {
				itemparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				itemparams.setMargins(0, 0, 0, (int)(10*density));
				itemparams.addRule(RelativeLayout.BELOW, id);
			}
			
			main.addView(item, itemparams);
			
		}
		
		
		
		
		
		
		
		
		return view;
	}
	
	private void loadImage(RelativeLayout layout, String url) {
		layout.removeAllViews();
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
        layout.addView(view, mPreLoadingParams);
        
	}

	public static class ViewHolder {
		RelativeLayout imageLayout;
		ImageView headimg;
		TextView nikename;
		TextView time;
		ImageView function;
		
		LinearLayout praiseLayout;
		ImageView zanIcon;
		TextView zanText;
		
		LinearLayout commentLayout;
		ImageView commentIcon;
		TextView commentText;
		
		LinearLayout shareLayout;
		ImageView shareIcon;
		TextView shareText;
		
		TextView zText;
		TextView weiguan;
		TextView detail;
		TextView totalcomments;
		
		TextView comment1;
		TextView comment2;
		TextView comment3;
		
		
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

	public void onResume() {
		
	}

	public void onStop() {
	
	}

	public void onDestroy() {
		
	}

}

