package cn.com.mobnote.golukmobile.msg;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.cluster.ClusterActivity;
import cn.com.mobnote.golukmobile.msg.bean.MessageMsgsBean;
import cn.com.mobnote.golukmobile.special.SpecialListActivity;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.util.GolukUtils;

public class OfficialMessageListAdapter extends BaseAdapter {
	private Context mContext;
	private List<MessageMsgsBean> mList;
	private final static String PURE_PIC = "0";
	private final static String VIDEO_DETAIL = "1";
	private final static String SPECIAL_LIST = "2";
	private final static String LIVE_VIDEO = "3";
	private final static String ACTIVITY_TOGETHER = "4";
	private final static String H5_PAGE = "5";
	private final static String SPECIAL_SOLO = "6";
	private final static String HOME_PAGE = "9";

	private final static String TAG = "OfficialMessageListAdapter";

	public OfficialMessageListAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}
	
	public void setData(List<MessageMsgsBean> list) {
		this.mList = list;
		notifyDataSetChanged();
	}
	
	public void appendData(List<MessageMsgsBean> list) {
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.official_message_list_item, null);
			viewHolder.nTextContent = (TextView) convertView.findViewById(R.id.tv_official_message_list_item_content);
			viewHolder.nTextTime = (TextView) convertView.findViewById(R.id.tv_official_message_list_item_time);
			viewHolder.nOfficialMsgLL = convertView.findViewById(R.id.ll_official_message_list_item);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		MessageMsgsBean bean = mList.get(position);

		if(null == bean) {
			return convertView;
		}

		if(null != bean.content && null != bean.content.anycast) {
			if(!TextUtils.isEmpty(bean.content.anycast.text)) {
				viewHolder.nTextContent.setText(mList.get(position).content.anycast.text);
			}
			if(!TextUtils.isEmpty(bean.content.time)) {
				viewHolder.nTextTime.setText(
					GolukUtils.getCommentShowFormatTime(bean.content.time));
			}
		} else {
			viewHolder.nTextContent.setText("");
			viewHolder.nTextTime.setText("");
		}

		final MessageMsgsBean finalBean = bean;

		viewHolder.nOfficialMsgLL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null == finalBean || null == finalBean.content) {
					return;
				}

				String type = finalBean.content.type;
				if (TextUtils.isEmpty(type)) {
					return;
				}

				String accessId = finalBean.content.access;
				if (TextUtils.isEmpty(accessId)) {
					return;
				}

				Intent intent = null;

				if (PURE_PIC.equals(type)) {
					// do nothing
					Log.d(TAG, "pure picture clicked");
				} else if (VIDEO_DETAIL.equals(type)) {
					// launch video detail
					intent = new Intent(mContext, VideoDetailActivity.class);
					intent.putExtra(VideoDetailActivity.VIDEO_ID, accessId);
					intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
					mContext.startActivity(intent);
				} else if (SPECIAL_LIST.equals(type)) {
					// launch special list
					intent = new Intent(mContext, SpecialListActivity.class);
					intent.putExtra("ztid", accessId);
					if(null != finalBean.content.anycast) {
						if (!TextUtils.isEmpty(finalBean.content.anycast.title)) {
							intent.putExtra("title", finalBean.content.anycast.title);
						}
					}
					mContext.startActivity(intent);
				} else if (LIVE_VIDEO.equals(type)) {
					// TODO: This should proceed in future
					// intent = new Intent(mContext, LiveActivity.class);
					// intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
					// intent.putExtra(LiveActivity.KEY_GROUPID, "");
					// intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
					// intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
					// intent.putExtra(LiveActivity.KEY_USERINFO, user);
					// mContext.startActivity(intent);
				} else if (ACTIVITY_TOGETHER.equals(type)) {
					// launch topic
					intent = new Intent(mContext, ClusterActivity.class);
					intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID,
							accessId);
					// intent.putExtra(ClusterActivity.CLUSTER_KEY_UID, "");
					if(null != finalBean.content.anycast) {
						String topName = "#" + finalBean.content.anycast.title + "#";
						intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE,
								topName);
					}
					mContext.startActivity(intent);
				} else if (H5_PAGE.equals(type)) {
					// launch h5 page
					intent = new Intent(mContext, UserOpenUrlActivity.class);
					intent.putExtra("url", accessId);
					if(null != finalBean.content.anycast) {
						if (!TextUtils.isEmpty(finalBean.content.anycast.title)) {
							intent.putExtra("slide_h5_title",
								finalBean.content.anycast.title);
						}
					}
					mContext.startActivity(intent);
				} else if (SPECIAL_SOLO.equals(type)) {
					intent = new Intent(mContext, VideoDetailActivity.class);
					intent.putExtra(VideoDetailActivity.VIDEO_ID, accessId);
					intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
					mContext.startActivity(intent);
				}else if(HOME_PAGE.equals(type)) {
					UCUserInfo user = new UCUserInfo();
					user.uid = accessId;
					user.nickname = "";
					user.headportrait = "";//clusterInfo.mUserEntity.headportrait;
					user.introduce = "";
					user.sex = "";//clusterInfo.mUserEntity.sex;
					user.customavatar = "";//clusterInfo.mUserEntity.mCustomAvatar;
					user.praisemenumber = "0";
					user.sharevideonumber = "0";
					intent = new Intent(mContext, UserCenterActivity.class);
					intent.putExtra("userinfo", user);
					intent.putExtra("type", 0);
					mContext.startActivity(intent);
				}
			}
		});

		return convertView;
	}

	static class ViewHolder {
		TextView nTextContent;
		TextView nTextTime;
		View nOfficialMsgLL;
	}
}
