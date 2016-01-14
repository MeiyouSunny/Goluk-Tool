package cn.com.mobnote.golukmobile.msg;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.msg.bean.MessageAnycastBean;
import cn.com.mobnote.golukmobile.msg.bean.MessageMsgsBean;
import cn.com.mobnote.util.GolukUtils;

public class OfficialMessageListAdapter extends BaseAdapter {
	private Context mContext;
	private List<MessageMsgsBean> mList;

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

		return convertView;
	}

	static class ViewHolder {
		TextView nTextContent;
		TextView nTextTime;
	}
}
