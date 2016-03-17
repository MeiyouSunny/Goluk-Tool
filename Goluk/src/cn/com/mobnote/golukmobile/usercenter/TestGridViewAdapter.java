package cn.com.mobnote.golukmobile.usercenter;

import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.usercenter.TestGridViewActivity.Data;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TestGridViewAdapter extends BaseAdapter {

	private Context mContext = null;
	private List<Data> mList = null;

	public TestGridViewAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public void setData(List<Data> list) {
		this.mList = list;
		GolukDebugUtils.e("", "-------------setData---------mListData: " + mList.size());
	}

	@Override
	public int getCount() {
		GolukDebugUtils.e("", "-------------getCount---------mListData: " + mList.size());
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.testgridview_item, null);
			viewHolder.nameText = (TextView) convertView.findViewById(R.id.tv_testgridview_name);
			viewHolder.contentText = (TextView) convertView.findViewById(R.id.tv_testgridview_content);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Data data = mList.get(arg0);
		viewHolder.nameText.setText(data.name);
		viewHolder.contentText.setText(data.content);
		return convertView;
	}

	public static class ViewHolder {
		TextView nameText;
		TextView contentText;
	}

}
