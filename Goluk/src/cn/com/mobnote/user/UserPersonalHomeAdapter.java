package cn.com.mobnote.user;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class UserPersonalHomeAdapter extends BaseAdapter {

	private Context context;
	private List<UserHomeManage> listHome;
	
	public UserPersonalHomeAdapter(Context context, List<UserHomeManage> listHome) {
		super();
		this.context = context;
		this.listHome = listHome;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listHome.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return listHome.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		
		return null;
	}
	class ViewHolder{
		TextView mTextDetail,mTextCountWatch,mTextCountZan;
		
	}

}
