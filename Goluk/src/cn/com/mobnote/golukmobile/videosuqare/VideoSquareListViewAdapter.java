package cn.com.mobnote.golukmobile.videosuqare;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
		mVideoSquareListData.clear();
		mVideoSquareListData.addAll(data);
		count = mVideoSquareListData.size();
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
	public View getView(int arg0, View convertView, ViewGroup parent) {
		VideoSquareInfo mVideoSquareInfo = mVideoSquareListData.get(arg0);
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.video_square_list_item, null);
			holder = new ViewHolder();
			holder.username = (TextView) convertView
					.findViewById(R.id.username);
			holder.looknumber = (TextView) convertView
					.findViewById(R.id.looknumber_text);
			holder.userhead = (ImageView) convertView
					.findViewById(R.id.user_head);
			holder.likenumber = (Button) convertView
					.findViewById(R.id.like_btn);
			holder.videotitle = (TextView) convertView
					.findViewById(R.id.video_title);
			holder.sharetime = (TextView) convertView.findViewById(R.id.time);
			holder.mPlayerLayout = (RelativeLayout) convertView
					.findViewById(R.id.mPlayerLayout);
			holder.mSurfaceView = (SurfaceView) convertView
					.findViewById(R.id.mSurfaceView);
			holder.reporticon = (ImageView) convertView
					.findViewById(R.id.report_icon);
			holder.liveicon = (ImageView) convertView
					.findViewById(R.id.live_icon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if("1".equals(mVideoSquareInfo.mVideoEntity.type)){//直播
			holder.reporticon.setVisibility(View.GONE);
			holder.liveicon.setVisibility(View.VISIBLE);
		}else{//点播
			holder.reporticon.setVisibility(View.VISIBLE);
			holder.liveicon.setVisibility(View.GONE);
		}

		holder.username.setText(mVideoSquareInfo.mUserEntity.nickname);
		holder.looknumber.setText(mVideoSquareInfo.mVideoEntity.clicknumber);
		holder.likenumber.setText(mVideoSquareInfo.mVideoEntity.praisenumber);
		holder.videotitle.setText(mVideoSquareInfo.mVideoEntity.describe);
		holder.sharetime.setText(this.formatTime(mVideoSquareInfo.mVideoEntity.sharingtime));

		int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		int height = (int) ((float) width / 1.77f);

		SurfaceHolder mSurfaceHolder = holder.mSurfaceView.getHolder();
		LinearLayout.LayoutParams mPlayerLayoutParams = new LinearLayout.LayoutParams(
				width, height);
		holder.mPlayerLayout.setLayoutParams(mPlayerLayoutParams);

		mSurfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				LogUtils.d("SSS============surfaceDestroyed==========");
			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				LogUtils.d("SSS============surfaceCreated==========");
			}

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
					int arg3) {

			}
		});

		return convertView;
	}

	public int getUserHead(String head) {
		return 0;
	}

	public void onDestroy() {

	}

	@SuppressLint("SimpleDateFormat")
	public String formatTime(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(date, pos);
		
		formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
		return formatter.format(strtodate);
		
	}

	public static class ViewHolder {
		TextView username;
		TextView looknumber;
		ImageView userhead;
		Button likenumber;
		TextView videotitle;
		TextView sharetime;
		RelativeLayout mPlayerLayout;
		SurfaceView mSurfaceView;
		ImageView liveicon;
		ImageView reporticon;
	}

}
