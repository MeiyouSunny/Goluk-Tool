package cn.com.mobnote.golukmobile.videosuqare;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

import cn.com.mobnote.golukmobile.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.ContactsContract.Contacts.Data;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class VideoSquareListViewAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<VideoSquareInfo> mVideoSquareListData = null;
	private int count = 0;

	public VideoSquareListViewAdapter(Context context) {
		mContext = context;
		mVideoSquareListData = new ArrayList<VideoSquareInfo>();
	}

	public void setData(List<VideoSquareInfo> data) {
		mVideoSquareListData.addAll(data);
		count = mVideoSquareListData.size();
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
	public View getView(int arg0, View convertView, ViewGroup parent) {
		VideoSquareInfo mVideoSquareInfo = mVideoSquareListData.get(arg0);

		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.video_square_list_item, null);
			holder = new ViewHolder();
			holder.username = (TextView) convertView
					.findViewById(R.id.looknumber_text);
			holder.looknumber = (TextView) convertView
					.findViewById(R.id.username);
			holder.userhead = (ImageView) convertView
					.findViewById(R.id.user_head);
			holder.likenumber = (Button) convertView
					.findViewById(R.id.like_btn);
			holder.videotitle = (TextView) convertView
					.findViewById(R.id.video_title);
			holder.sharetime = (TextView) convertView.findViewById(R.id.time);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.username.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.looknumber.setText(mVideoSquareInfo.mVideoEntity.clicknumber);
		holder.likenumber.setText(mVideoSquareInfo.mVideoEntity.praisenumber);
		holder.videotitle.setText(mVideoSquareInfo.mVideoEntity.describe);
		holder.sharetime.setText(this
				.formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));
		return convertView;
	}

	public int getUserHead(String head) {

		return 0;
	}

	public void onDestroy() {

	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yy年MM月dd日 HH时mm分");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(date, pos);
		return strtodate;
	}

	public static class ViewHolder {
		TextView username;
		TextView looknumber;
		ImageView userhead;
		Button likenumber;
		TextView videotitle;
		TextView sharetime;
	}

}
