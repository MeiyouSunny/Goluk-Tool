package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;
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
import cn.com.mobnote.util.GolukUtils;
import com.facebook.drawee.view.SimpleDraweeView;

public class WonderfulSelectedAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<JXListItemDataInfo> mDataList = null;
	private int count = 0;
	private int width = 0;
	private Typeface mTypeface = null;

	public WonderfulSelectedAdapter(Context context) {
		mContext = context;
		mDataList = new ArrayList<JXListItemDataInfo>();
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		mTypeface = Typeface.createFromAsset(context.getAssets(), "AdobeHebrew-Bold.otf");
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
			holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
			holder.videoImg = (SimpleDraweeView) convertView.findViewById(R.id.simpledrawee);
			holder.icon = (SimpleDraweeView) convertView.findViewById(R.id.wonderful_icon);
			holder.mTitleName = (TextView) convertView.findViewById(R.id.mTitleName);
			holder.mTagName = (TextView) convertView.findViewById(R.id.mTagName);
			holder.mVideoLayout = (LinearLayout) convertView.findViewById(R.id.mVideoLayout);
			holder.mLookLayout = (LinearLayout) convertView.findViewById(R.id.mLookLayout);
			holder.mVideoNum = (TextView) convertView.findViewById(R.id.mVideoNum);
			holder.mLookNum = (TextView) convertView.findViewById(R.id.mLookNum);
			
			int height = (int) ((float) width / 1.78f);
			RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
			holder.videoImg.setLayoutParams(mPreLoadingParams);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		JXListItemDataInfo info = mDataList.get(arg0);
		holder.mTitleName.setText(getTitleString(info.ztitle));
		holder.mTagName.setVisibility(View.GONE);
		if ("-1".equals(info.clicknumber)) {
			holder.mVideoLayout.setVisibility(View.GONE);
		} else {
			holder.mVideoNum.setText(GolukUtils.getFormatNumber(info.clicknumber));
			holder.mVideoLayout.setVisibility(View.VISIBLE);
		}

		if ("-1".equals(info.videonumber)) {
			holder.mLookLayout.setVisibility(View.GONE);
		} else {
			holder.mLookNum.setText(GolukUtils.getFormatNumber(info.videonumber));
			holder.mLookLayout.setVisibility(View.VISIBLE);
		}

		if (!TextUtils.isEmpty(info.jxdate)) {
			if (0 == arg0) {
				holder.mDate.setVisibility(View.GONE);
			} else {
				holder.mDate.setTypeface(mTypeface);
				holder.mDate.setText(GolukUtils.getTime(info.jxdate));
				holder.mDate.setVisibility(View.VISIBLE);
			}
		} else {
			holder.mDate.setVisibility(View.GONE);
		}
		holder.main.setOnTouchListener(new ClickWonderfulSelectedListener(mContext, info, this));
		loadImage(holder.videoImg, holder.icon, info.jximg, info.jtypeimg);
		return convertView;
	}

	private String getTitleString(String title) {
		String name = "";
		int len = title.length();
		if (len > 15) {
			int size = len / 15 + 1;
			for (int i = 0; i < size; i++) {
				int index = 15 * (i + 1);
				if (index < len) {
					name += title.substring(15 * i, index) + "\n";
				} else {
					name += title.substring(15 * i);
				}
			}
		} else {
			name = title;
		}

		return name;
	}

	private void loadImage(SimpleDraweeView mPlayerLayout, SimpleDraweeView iconView, String url, String iconUrl) {
		mPlayerLayout.setImageURI(Uri.parse(url));
		if (TextUtils.isEmpty(iconUrl)) {
			iconView.setVisibility(View.GONE);
		} else {
			iconView.setVisibility(View.VISIBLE);
			iconView.setImageURI(Uri.parse(iconUrl));
		}
	}

	public static class ViewHolder {
		RelativeLayout main;
		SimpleDraweeView videoImg;
		SimpleDraweeView icon;
		TextView mTitleName;
		TextView mTagName;
		LinearLayout mVideoLayout;
		LinearLayout mLookLayout;
		TextView mVideoNum;
		TextView mLookNum;
		TextView mDate;
	}
}
