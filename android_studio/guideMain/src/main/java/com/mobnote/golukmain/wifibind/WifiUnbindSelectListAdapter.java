package com.mobnote.golukmain.wifibind;

import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindFinish;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class WifiUnbindSelectListAdapter extends BaseAdapter {

    public Context mContext = null;
    public List<WifiBindHistoryBean> mBindHistoryData = null;
    /**
     * 编辑按钮的状态 默认是不编辑的
     **/
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
                connectViewHodler.golukPointgreyIcon = (ImageView) convertView.findViewById(R.id.goluk_pointgrey_icon);
                convertView.setTag(connectViewHodler);
            } else {
                connectViewHodler = (ConnectViewHodler) convertView.getTag();
            }

            if (bindHistoryBean.ipcSign != null) {
                if (bindHistoryBean.ipcSign.equals(IPCControlManager.G1_SIGN)) {
                    connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g1_img);
                } else if (bindHistoryBean.ipcSign.equals(IPCControlManager.G2_SIGN)) {
                    connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g2_img);
                } else if (bindHistoryBean.ipcSign.equals(IPCControlManager.T1s_SIGN)) {
                    connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t1s_img);
                } else if (bindHistoryBean.ipcSign.equals(IPCControlManager.T1_SIGN)) {
                    connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t1_img);
                } else if (bindHistoryBean.ipcSign.equals(IPCControlManager.T2_SIGN)) {
                    connectViewHodler.golukIcon.setImageResource(R.drawable.connect_t2_img);
                }
            } else {
                connectViewHodler.golukIcon.setImageResource(R.drawable.connect_g1_img);
            }

            connectViewHodler.golukName.setText(bindHistoryBean.ipc_ssid);

            if (mEditState == false) {
                connectViewHodler.golukDelIcon.setVisibility(View.GONE);
                connectViewHodler.golukPointgreyIcon.setVisibility(View.VISIBLE);
            } else {
                connectViewHodler.golukDelIcon.setVisibility(View.VISIBLE);
                connectViewHodler.golukPointgreyIcon.setVisibility(View.GONE);
            }
            if (position == 0) {
                connectViewHodler.historyTxt.setVisibility(View.VISIBLE);
            } else {
                connectViewHodler.historyTxt.setVisibility(View.GONE);
            }

            WifiUnbindSelectListActivity wsla = (WifiUnbindSelectListActivity) mContext;
            GolukDebugUtils.e(
                    "",
                    "select wifibind---WifiUnbindSelectListAdapter ------getView  isShow : "
                            + wsla.isCanShowListViewHead());
            if (wsla.isCanShowListViewHead()) {
                if (bindHistoryBean.state == WifiBindHistoryBean.CONN_USE) {
                    convertView.setVisibility(View.GONE);
                } else {
                    convertView.setVisibility(View.VISIBLE);
                }
            } else {
                convertView.setVisibility(View.VISIBLE);
            }

            connectViewHodler.golukDelIcon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    showDelDialog(bindHistoryBean);
                }
            });

            connectViewHodler.golukConnLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    click_useIpc(bindHistoryBean);
                }
            });
        }
        return convertView;
    }

    private void showDelDialog(final WifiBindHistoryBean bindHistoryBean) {
        final AlertDialog confirmation = new AlertDialog.Builder(mContext, R.style.CustomDialog).create();
        confirmation.show();
        confirmation.getWindow().setContentView(R.layout.unbind_dialog_confirmation);
        confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteIpc(bindHistoryBean);
                confirmation.dismiss();
            }
        });
        confirmation.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmation.dismiss();
            }
        });
    }

    /**
     * 点击连接某个设备
     *
     * @param bindHistoryBean
     * @author jyf
     */
    private void click_useIpc(WifiBindHistoryBean bindHistoryBean) {
        if (null == bindHistoryBean) {
            return;
        }
        if (mEditState == false) {
            WifiUnbindSelectListActivity wsla = (WifiUnbindSelectListActivity) mContext;
            List<WifiBindHistoryBean> list = WifiBindDataCenter.getInstance().getAllBindData();
            if (null == list || list.size() <= 1) {
                wsla.showLoading(true);
            } else {
                wsla.showLoading(false);
            }

            WifiBindDataCenter.getInstance().editBindStatus(bindHistoryBean.ipc_ssid, WifiBindHistoryBean.CONN_USE);

            GolukDebugUtils.e("", "wifibind----WifiUnbindSelect  OnClick--------ssid:" + bindHistoryBean.ipc_ssid);

            EventBindFinish eventFnish = new EventBindFinish(EventConfig.CAR_RECORDER_BIND_CREATEAP);
            eventFnish.bean = bindHistoryBean;
            EventBus.getDefault().post(eventFnish);
            wsla.getBindHistoryData();

        }
    }

    /**
     * 删除设备
     *
     * @param bindHistoryBean
     * @author jyf
     */
    private void deleteIpc(WifiBindHistoryBean bindHistoryBean) {
        if (bindHistoryBean.state == WifiBindHistoryBean.CONN_USE) {
            EventBindFinish eventFnish = new EventBindFinish(EventConfig.BIND_LIST_DELETE_CONFIG);
            EventBus.getDefault().post(eventFnish);
            WifiUnbindSelectListActivity wsla = (WifiUnbindSelectListActivity) mContext;
            wsla.mApp.setIpcDisconnect();
        }
        WifiBindDataCenter.getInstance().deleteBindData(bindHistoryBean.ipc_ssid);
        for (int i = 0; i < mBindHistoryData.size(); i++) {
            WifiBindHistoryBean wbhb = mBindHistoryData.get(i);
            if (bindHistoryBean.ipc_ssid.equals(wbhb.ipc_ssid)) {
                mBindHistoryData.remove(i);
            }
        }

        notifyDataSetChanged();
        // 如果本地没有历史数据，则设置连接失败
        List<WifiBindHistoryBean> list = WifiBindDataCenter.getInstance().getAllBindData();
        if (null == list || list.size() <= 0) {
            GolukApplication.getInstance().setIpcDisconnect();
        }
    }

    public static class ConnectViewHodler {
        TextView historyTxt;
        ImageView golukDelIcon;
        ImageView golukIcon;
        TextView golukName;
        RelativeLayout golukConnLayout;
        ImageView golukPointgreyIcon;
    }

    public static class HeadViewHodler {
        TextView connTxt;
        ImageView connHeadIcon;
        ImageView golukIcon;
        TextView golukName;
        ImageView golukDelIcon;
        ImageView golukPointgreyIcon;
        RelativeLayout golukConnLayout;
    }

}
