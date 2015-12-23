package cn.com.mobnote.golukmobile.wifibind;

import cn.com.mobnote.golukmobile.R;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiUnbindSelectListAdapter extends BaseAdapter{
	
	public Context mContext = null;
	
	public JSONArray mJsons = null;
	
	public View mHeadView = null;
	
	public HeadViewHodler  mHeadData = null;
	
	/**编辑按钮的状态 默认是不编辑的**/
	public boolean mEditState = false;
	
	public WifiUnbindSelectListAdapter (Context context){
		mContext = context;
	}
	
	public void setData(JSONArray data){
		mJsons = data;
	}

	@Override
	public int getCount() {
		if(mJsons!=null){
			return mJsons.size();
		}else{
			return 0;
		}
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
	public View getView(int position, View convertView, ViewGroup parent) {
		final JSONObject json = mJsons.getJSONObject(position);
		
		ConnectViewHodler connectViewHodler = null;
		if(json != null){
			if(convertView == null){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.unbind_connection_history_item, null);
				connectViewHodler = new ConnectViewHodler();
				connectViewHodler.historyTxt = (TextView) convertView.findViewById(R.id.history_txt);
				connectViewHodler.golukDelIcon = (ImageView) convertView.findViewById(R.id.goluk_del_icon);
				connectViewHodler.golukIcon = (ImageView) convertView.findViewById(R.id.goluk_icon);
				connectViewHodler.golukName = (TextView) convertView.findViewById(R.id.goluk_name);
				connectViewHodler.golukConnLayout = (RelativeLayout) convertView.findViewById(R.id.goluk_conn_layout);
				convertView.setTag(connectViewHodler);
			}else{
				connectViewHodler = (ConnectViewHodler) convertView.getTag();
			}
			if(json.getString("type").equals("G1")){
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g1_img);
			}else if(json.getString("type").equals("G2")){
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g2_img);
			}else if(json.getString("type").equals("G1S")){
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g1_img);
			}else if(json.getString("type").equals("T1")){
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t1_img);
			}
			
			connectViewHodler.golukName.setText(json.getString("name"));
			
			if(mEditState == false){
				connectViewHodler.golukDelIcon.setVisibility(View.GONE);
			}else{
				connectViewHodler.golukDelIcon.setVisibility(View.VISIBLE);
			}
			if(position ==0){
				connectViewHodler.historyTxt.setVisibility(View.VISIBLE);
			}else{
				connectViewHodler.historyTxt.setVisibility(View.GONE);
			}
			
			connectViewHodler.golukDelIcon.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					mJsons.remove(json);
					notifyDataSetChanged();
				}
			});
			
			connectViewHodler.golukConnLayout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(mEditState == false){
						if(mHeadView == null){
							mHeadView = LayoutInflater.from(mContext).inflate(R.layout.unbind_connection_head,null);
							WifiUnbindSelectListActivity wsla = (WifiUnbindSelectListActivity) mContext;
							wsla.addListViewHead(mHeadView);
						}
						
						if(mHeadData == null){
							mHeadData = new HeadViewHodler();
							mHeadData.connHeadIcon = (ImageView) mHeadView.findViewById(R.id.conn_head_icon);
							mHeadData.connTxt = (TextView) mHeadView.findViewById(R.id.conn_txt);
							mHeadData.golukDelIcon =(ImageView) mHeadView.findViewById(R.id.goluk_del_icon);
							mHeadData.golukIcon  = (ImageView) mHeadView.findViewById(R.id.goluk_icon);
							mHeadData.golukName = (TextView) mHeadView.findViewById(R.id.goluk_name);
						}
						
						mHeadData.golukName.setText(json.getString("name"));
						mHeadView.setTag(mHeadData);
						
						mJsons.remove(json);
						notifyDataSetChanged();
					}
					
				}
			});
		}
		return convertView;
	}
	
	
	public static class ConnectViewHodler{
		 TextView historyTxt;
		 ImageView golukDelIcon;
		 ImageView golukIcon;
		 TextView golukName; 
		 RelativeLayout golukConnLayout;
	}
	
	public static class HeadViewHodler{
		 TextView connTxt;
		 ImageView connHeadIcon;
		 ImageView golukIcon;
		 TextView golukName; 
		 ImageView golukDelIcon;
	}


}
