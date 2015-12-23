package cn.com.mobnote.golukmobile.wifibind;

import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindDataCenter;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindHistoryBean;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiUnbindSelectListAdapter extends BaseAdapter {

	public Context mContext = null;

	public List<WifiBindHistoryBean> mBindHistoryData = null;

	/** 编辑按钮的状态 默认是不编辑的 **/
	public boolean mEditState = false;

	public WifiUnbindSelectListAdapter(Context context) {
		mContext = context;
	}

	public void setData(List<WifiBindHistoryBean> data) {
		mBindHistoryData = data;
	}

	@Override
	public int getCount() {
		if (mBindHistoryData != null) {
			return mBindHistoryData.size();
		} else {
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
		final WifiBindHistoryBean bindHistoryBean = mBindHistoryData.get(position);

		ConnectViewHodler connectViewHodler = null;
		if (bindHistoryBean != null) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.unbind_connection_history_item, null);
				connectViewHodler = new ConnectViewHodler();
				connectViewHodler.historyTxt = (TextView) convertView.findViewById(R.id.history_txt);
				connectViewHodler.golukDelIcon = (ImageView) convertView.findViewById(R.id.goluk_del_icon);
				connectViewHodler.golukIcon = (ImageView) convertView.findViewById(R.id.goluk_icon);
				connectViewHodler.golukName = (TextView) convertView.findViewById(R.id.goluk_name);
				connectViewHodler.golukConnLayout = (RelativeLayout) convertView.findViewById(R.id.goluk_conn_layout);
				convertView.setTag(connectViewHodler);
			} else {
				connectViewHodler = (ConnectViewHodler) convertView.getTag();
			}
			if (bindHistoryBean.ipcSign.equals(IPCControlManager.G1_SIGN)) {
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g1_img);
			} else if (bindHistoryBean.ipcSign.equals(IPCControlManager.G2_SIGN)) {
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g2_img);
			} else if (bindHistoryBean.ipcSign.equals(IPCControlManager.G1s_SIGN)) {
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t1_img);
			} else if (bindHistoryBean.ipcSign.equals(IPCControlManager.T1_SIGN)) {
				connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t1_img);
			}

			connectViewHodler.golukName.setText(bindHistoryBean.ipc_ssid);

			if (mEditState == false) {
				connectViewHodler.golukDelIcon.setVisibility(View.GONE);
			} else {
				connectViewHodler.golukDelIcon.setVisibility(View.VISIBLE);
			}
			if (position == 0) {
				connectViewHodler.historyTxt.setVisibility(View.VISIBLE);
			} else {
				connectViewHodler.historyTxt.setVisibility(View.GONE);
			}
			
			if(bindHistoryBean.state == WifiBindHistoryBean.CONN_USE){
				convertView.setVisibility(View.GONE);
			}else{
				convertView.setVisibility(View.VISIBLE);
			}

			connectViewHodler.golukDelIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					WifiBindDataCenter.getInstance().deleteBindData(bindHistoryBean.ipc_ssid);
					mBindHistoryData.remove(bindHistoryBean);
					notifyDataSetChanged();
				}
			});

			connectViewHodler.golukConnLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (mEditState == false) {
						WifiBindDataCenter.getInstance().editBindStatus(bindHistoryBean.ipc_ssid,WifiBindHistoryBean.CONN_USE);
						
						WifiUnbindSelectListActivity wsla = (WifiUnbindSelectListActivity) mContext;
						wsla.getBindHistoryData();
					}
				}
			});
		}
		return convertView;
	}

	public static class ConnectViewHodler {
		TextView historyTxt;
		ImageView golukDelIcon;
		ImageView golukIcon;
		TextView golukName;
		RelativeLayout golukConnLayout;
	}

	public static class HeadViewHodler {
		TextView connTxt;
		ImageView connHeadIcon;
		ImageView golukIcon;
		TextView golukName;
		ImageView golukDelIcon;
	}

}
