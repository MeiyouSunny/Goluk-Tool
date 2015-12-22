package cn.com.mobnote.golukmobile.wifibind;

import cn.com.mobnote.golukmobile.R;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiUnbindSelectListAdapter extends BaseAdapter{
	
	public Context mContext = null;
	
	public JSONArray mJsons = null;
	
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
		JSONObject json = mJsons.getJSONObject(position);
		
		ConnectViewHodler connectViewHodler = null;
		if(json != null){
			if(convertView == null){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.unbind_connection_history_item, null);
				connectViewHodler = new ConnectViewHodler();
				connectViewHodler.historyTxt = (TextView) convertView.findViewById(R.id.history_txt);
				connectViewHodler.golukDelIcon = (ImageView) convertView.findViewById(R.id.goluk_del_icon);
				connectViewHodler.golukIcon = (ImageView) convertView.findViewById(R.id.goluk_icon);
				connectViewHodler.golukName = (TextView) convertView.findViewById(R.id.goluk_name);
			}else{
				connectViewHodler = (ConnectViewHodler) convertView.getTag();
			}
			
			
		}
		return null;
	}
	
	
	public static class ConnectViewHodler{
		 TextView historyTxt;
		 ImageView golukDelIcon;
		 ImageView golukIcon;
		 TextView golukName; 
	}


}
