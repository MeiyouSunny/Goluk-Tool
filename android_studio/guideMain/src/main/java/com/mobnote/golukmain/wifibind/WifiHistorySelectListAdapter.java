package com.mobnote.golukmain.wifibind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;

import java.util.List;

public class WifiHistorySelectListAdapter extends BaseAdapter {

    public Context mContext = null;
    public List<WifiBindHistoryBean> mBindHistoryData = null;
    private WifiHistorySelectListActivity activity;

    public WifiHistorySelectListAdapter(Context context) {
        mContext = context;
        activity = (WifiHistorySelectListActivity) mContext;
    }

    public void setData(List<WifiBindHistoryBean> data) {
        mBindHistoryData = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final WifiBindHistoryBean bindHistoryBean = mBindHistoryData.get(position);
        HistoryConnectViewHolder connectViewHodler;
        if (bindHistoryBean != null) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_history_connection_history_item, parent, false);
                connectViewHodler = new HistoryConnectViewHolder();
                connectViewHodler.historyTxt = (TextView) convertView.findViewById(R.id.history_txt);
                connectViewHodler.golukIcon = (ImageView) convertView.findViewById(R.id.goluk_icon);
                connectViewHodler.golukName = (TextView) convertView.findViewById(R.id.goluk_name);
                connectViewHodler.golukConnLayout = (RelativeLayout) convertView.findViewById(R.id.goluk_conn_layout);
                connectViewHodler.golukPointgreyIcon = (ImageView) convertView.findViewById(R.id.goluk_pointgrey_icon);
                convertView.setTag(connectViewHodler);
            } else {
                connectViewHodler = (HistoryConnectViewHolder) convertView.getTag();
            }

            if (bindHistoryBean.ipcSign != null) {
                switch (bindHistoryBean.ipcSign) {
                    case IPCControlManager.G1_SIGN:
                        connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g1_img);
                        break;
                    case IPCControlManager.G2_SIGN:
                        connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g2_img);
                        break;
                    case IPCControlManager.T1s_SIGN:
                        connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t1s_img);
                        break;
                    case IPCControlManager.T1_SIGN:
                        connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t1_img);
                        break;
                    case IPCControlManager.T2_SIGN:
                        connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t2_img);
                        break;
                }
            } else {
                connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g1_img);
            }
            connectViewHodler.golukName.setText(bindHistoryBean.ipc_ssid);
            connectViewHodler.golukConnLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    activity.click_useIpc(bindHistoryBean,false);
                }
            });
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mBindHistoryData != null ? mBindHistoryData.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }


    public static class HistoryConnectViewHolder {
        TextView historyTxt;
        ImageView golukIcon;
        TextView golukName;
        RelativeLayout golukConnLayout;
        ImageView golukPointgreyIcon;
    }

}
