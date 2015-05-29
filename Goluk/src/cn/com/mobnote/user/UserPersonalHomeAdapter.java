package cn.com.mobnote.user;

import java.util.List;
import cn.com.mobnote.golukmobile.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class UserPersonalHomeAdapter extends BaseAdapter {

	private Context context ;
	private List<UserHomeManage> listHome;
	private ViewHolder holder;
	
	public UserPersonalHomeAdapter(Context context, List<UserHomeManage> listHome) {
		super();
		this.context = context;
		this.listHome = listHome;
	}

	@Override
	public int getCount() {
		return listHome.size();
	}

	@Override
	public Object getItem(int arg0) {
		return listHome.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.user_personal_homepage_item,null);
			holder = new ViewHolder();
			holder.mTextDetail = (TextView) convertView.findViewById(R.id.user_personal_homepage_item_text);
			holder.mTextCountWatch = (TextView) convertView.findViewById(R.id.user_personal_homepage_item_count_watch);
			holder.mTextCountZan = (TextView) convertView.findViewById(R.id.user_personal_homepage_item_count_zan);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		UserHomeManage manage = (UserHomeManage)listHome.get(arg0);
		holder.mTextDetail.setText(manage.getHomeContent());
		holder.mTextCountWatch.setText(manage.getHomeCountWatch());
		holder.mTextCountZan.setText(manage.getHomeCountZan());
		
		return convertView;
	}
	class ViewHolder{
		TextView mTextDetail,mTextCountWatch,mTextCountZan;
		
	}

}
