package com.mobnote.golukmain.praised;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.praised.bean.MyPraisedVideoBean;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.util.List;

public class MyPraisedListAdapter extends BaseAdapter {
	private Context mContext;
	private List<MyPraisedVideoBean> mList;
	private int width;

	private final static String TAG = "MyPraisedListAdapter";

	public MyPraisedListAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	}

	public void setData(List<MyPraisedVideoBean> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<MyPraisedVideoBean> list) {
		mList.addAll(list);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return null == mList ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		if (null == mList || position < 0 || position > mList.size() - 1) {
			return null;
		}
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder viewHolder = null;
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.praised_list_item, null);
			viewHolder.nTimeTV = (TextView) convertView.findViewById(R.id.tv_praised_list_item_time);
			viewHolder.nCoverIV = (ImageView) convertView.findViewById(R.id.tv_praised_list_item_cover);
			viewHolder.nAuthorTV = (TextView) convertView.findViewById(R.id.tv_praised_list_item_author);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		MyPraisedVideoBean praisedVideo = mList.get(position);

		int height = (int) ((float) width / 1.78f);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
		viewHolder.nCoverIV.setLayoutParams(layoutParams);

		GlideUtils.loadImage(
				mContext, viewHolder.nCoverIV, praisedVideo.picture, R.drawable.tacitly_pic);
		final String videoID = praisedVideo.videoid;
		viewHolder.nCoverIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//视频详情页访问
				ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_share_video_network_other));

				Intent intent = new Intent(mContext, VideoDetailActivity.class);
				intent.putExtra(VideoDetailActivity.VIDEO_ID, videoID);
				mContext.startActivity(intent);
			}
		});

		if(!TextUtils.isEmpty(praisedVideo.time)) {
			viewHolder.nTimeTV.setText(
				GolukUtils.getCommentShowFormatTime(mContext, praisedVideo.ts));
		}

		SpannableString spanttt = new SpannableString(praisedVideo.nickname + "  " + praisedVideo.describe);

		ClickableSpan clickName = new AuthorClickableSpan(praisedVideo);
		spanttt.setSpan(clickName, 0, praisedVideo.nickname.length(),
						Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		ClickableSpan clickContent = new ContentClickableSpan(praisedVideo);
		spanttt.setSpan(clickContent, praisedVideo.nickname.length() + 2,
						praisedVideo.nickname.length() + praisedVideo.describe.length() + 2,
						Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		viewHolder.nAuthorTV.setText(spanttt);
		viewHolder.nAuthorTV.setMovementMethod(LinkMovementMethod.getInstance());

		return convertView;
	}

	class AuthorClickableSpan extends ClickableSpan {
		MyPraisedVideoBean nPraisedBean;

		public AuthorClickableSpan(MyPraisedVideoBean praisedBean) {
			super();
			this.nPraisedBean = praisedBean;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(Color.rgb(0x11, 0x63, 0xa2));
		}

		@Override
		public void onClick(View widget) {
			GolukUtils.startUserCenterActivity(mContext,nPraisedBean.uid);
		}
	}

	class ContentClickableSpan extends ClickableSpan {
		MyPraisedVideoBean nPraisedBean;

		public ContentClickableSpan(MyPraisedVideoBean praisedBean) {
			super();
			this.nPraisedBean = praisedBean;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(Color.rgb(0x33, 0x33, 0x33));
		}

		@Override
		public void onClick(View widget) {
			//视频详情页访问
			ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_share_video_network_other));

			Intent intent = new Intent(mContext, VideoDetailActivity.class);
			intent.putExtra(VideoDetailActivity.VIDEO_ID, nPraisedBean.videoid);
			mContext.startActivity(intent);
		}
	}

	static class ViewHolder {
		TextView nTimeTV;
		ImageView nCoverIV;
		TextView nAuthorTV;
	}
}
