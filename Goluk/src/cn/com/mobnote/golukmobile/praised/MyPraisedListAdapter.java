package cn.com.mobnote.golukmobile.praised;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.praised.bean.MyPraisedVideoBean;
import cn.com.mobnote.golukmobile.usercenter.NewUserCenterActivity;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

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
				Intent intent = new Intent(mContext, VideoDetailActivity.class);
				intent.putExtra(VideoDetailActivity.VIDEO_ID, videoID);
				mContext.startActivity(intent);
			}
		});

		if(!TextUtils.isEmpty(praisedVideo.time)) {
			viewHolder.nTimeTV.setText(
				GolukUtils.getCommentShowFormatTime(mContext, praisedVideo.time));
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
//			Intent intent = new Intent(mContext, UserCenterActivity.class);
			Intent intent = new Intent(mContext, NewUserCenterActivity.class);
			UCUserInfo user = new UCUserInfo();
			user.uid = nPraisedBean.uid;
			user.nickname = nPraisedBean.nickname;
			user.headportrait = "";
			user.introduce = "";
			user.sex = "";
			user.customavatar = "";
			user.praisemenumber = "0";
			user.sharevideonumber = "0";

			intent.putExtra("userinfo", user);
			intent.putExtra("type", 0);
			mContext.startActivity(intent);
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
