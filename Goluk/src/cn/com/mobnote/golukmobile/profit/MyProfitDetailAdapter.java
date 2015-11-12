package cn.com.mobnote.golukmobile.profit;

import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyProfitDetailAdapter extends BaseAdapter {

	private Context mContext;
	private List<ProfitDetailResult> mIncomeList;
	private int width = 0;
	
	public MyProfitDetailAdapter(Context context) {
		super();
		this.mContext = context;
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	}
	
	public void setData(List<ProfitDetailResult> incomeList) {
		this.mIncomeList = incomeList;
	}
	
	public void appendData(List<ProfitDetailResult> incomeList) {
		mIncomeList.addAll(incomeList);
		this.notifyDataSetChanged();
	}


	@Override
	public int getCount() {
		return null == mIncomeList ? 0 : mIncomeList.size();
	}

	@Override
	public Object getItem(int arg0) {
		if (null == mIncomeList || arg0 < 0 || arg0 > mIncomeList.size() - 1) {
			return null;
		}
		return mIncomeList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		
		return 0;
	}
	
	// 获取最后一条数据的时间戳
	public String getLastDataTime() {
		if (null == mIncomeList || mIncomeList.size() <= 0) {
			return "";
		}
		return mIncomeList.get(mIncomeList.size() - 1).timestamp;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if(null == convertView) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.my_profit_detail_item, null);
			holder.mImagePic = (ImageView) convertView.findViewById(R.id.my_profit_detail_item_videopic);
			holder.mTextCount = (TextView) convertView.findViewById(R.id.my_profit_detail_item_count);
			holder.mTextTime = (TextView) convertView.findViewById(R.id.my_profit_time);
			
			int nheight = (int) ((float) width / 1.77f);
			int nwidth = (int) (GolukUtils.mDensity * 95);
			RelativeLayout.LayoutParams mPlayerLayoutParams = new RelativeLayout.LayoutParams(nwidth, nheight);
			mPlayerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			mPlayerLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			mPlayerLayoutParams.leftMargin = (int) (GolukUtils.mDensity * 5);
			mPlayerLayoutParams.topMargin = (int) (GolukUtils.mDensity * 5);
			mPlayerLayoutParams.bottomMargin = (int) (GolukUtils.mDensity * 5);
			holder.mImagePic.setLayoutParams(mPlayerLayoutParams);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ProfitDetailResult result = mIncomeList.get(arg0);
		GolukDebugUtils.e("", "==========imageUrl========="+result.url);
		GlideUtils.loadImage(mContext, holder.mImagePic, result.url, R.drawable.tacitly_pic);
		holder.mTextCount.setText("+"+UserUtils.formatNumber(result.gold)+"Ｇ币");
		holder.mTextTime.setText(GolukUtils.getCommentShowFormatTime(result.time));
		
		return convertView;
	}
	
	class ViewHolder {
		ImageView mImagePic;
		TextView mTextCount,mTextTime;
	}

}
