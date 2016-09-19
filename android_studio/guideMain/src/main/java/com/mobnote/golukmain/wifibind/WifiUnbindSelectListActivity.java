package com.mobnote.golukmain.wifibind;

import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindFinish;
import com.mobnote.eventbus.EventBinding;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.eventbus.EventWifiAuto;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.wifibind.WifiUnbindSelectListAdapter.HeadViewHodler;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class WifiUnbindSelectListActivity extends BaseActivity implements OnClickListener {

    /**
     * 关闭按钮
     **/
    private ImageView mCloseBtn;
    /**
     * 数据列表
     **/
    private ListView mListView;
    /**
     * 没有数据时的默认布局
     **/
    private RelativeLayout mEmptyLayout;
    /**
     * 编辑按钮
     **/
    private Button mEditBtn;
    /**
     * 连接中headView
     **/
    public View mHeadView = null;
    public HeadViewHodler mHeadData = null;
    private WifiBindHistoryBean mWifiBindConnectData = null;
    private WifiUnbindSelectListAdapter mListAdapter;
    private CustomLoadingDialog mLoadingDialog = null;
    public GolukApplication mApp = null;
    private boolean isCanReceiveFailed = true;

    private int mHeadContentHeight = (int) (88 * GolukUtils.mDensity);

    /**
     * 控制是否可以接受连接信息
     */
    // private boolean isCanAcceptMsg = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unbind_connection_list);
        EventBus.getDefault().register(this);
        mApp = (GolukApplication) getApplication();
        initView();
        initLisenner();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.setBinding(false);
    }

    /**
     * 初始化view
     */
    private void initView() {
        mListView = (ListView) findViewById(R.id.listView);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.emptyLayout);
        mCloseBtn = (ImageView) findViewById(R.id.close_btn);
        mEditBtn = (Button) findViewById(R.id.edit_btn);
        mHeadView = LayoutInflater.from(this).inflate(R.layout.unbind_connection_head, null);
        findViewById(R.id.addMoblieBtn).setOnClickListener(this);

    }

    /**
     * 初始化view的监听
     */
    private void initLisenner() {
        mCloseBtn.setOnClickListener(this);
        mEditBtn.setOnClickListener(this);
    }

    /**
     * 初始化数据
     **/
    private void initData() {
        mListView.setEmptyView(mEmptyLayout);
        mListView.addHeaderView(mHeadView);
        mListAdapter = new WifiUnbindSelectListAdapter(this);
        getBindHistoryData();
    }

    /**
     * 获取最新的bind数据
     */
    public void getBindHistoryData() {
        GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------getBindHistoryData--1");
        List<WifiBindHistoryBean> binds = WifiBindDataCenter.getInstance().getAllBindData();
        if (binds != null) {
            mEditBtn.setVisibility(View.VISIBLE);
            if (isCanShowListViewHead()) {
                for (int i = 0; i < binds.size(); i++) {
                    WifiBindHistoryBean bind = binds.get(i);
                    if (bind.state == WifiBindHistoryBean.CONN_USE) {
                        mWifiBindConnectData = bind;
                        refreshHeadData();
                        if (binds.size() > 1) {
                            binds.remove(i);
                            binds.add(mWifiBindConnectData);
                        }
                        break;
                    }
                }
            } else {
                mHeadView.setPadding(0, -1 * mHeadContentHeight, 0, 0);
                mHeadView.setVisibility(View.GONE);

            }
        } else {
            mEditBtn.setText(this.getResources().getString(R.string.edit_text));// 编辑
            mEditBtn.setVisibility(View.GONE);
        }
        mListView.setAdapter(mListAdapter);
        mListAdapter.setData(binds);
        mListAdapter.notifyDataSetChanged();
    }

    public boolean isCanShowListViewHead() {
        WifiBindHistoryBean temp = WifiBindDataCenter.getInstance().getCurrentUseIpc();
        if (mApp.isIpcLoginSuccess || (mApp.mWiFiStatus != MainActivity.WIFI_STATE_FAILED && null != temp)) {
            return true;
        }
        return false;
    }

    private void refreshHeadData() {
        mHeadView.setPadding(0, 0, 0, 0);
        mHeadView.setVisibility(View.VISIBLE);
        if (mHeadData == null) {
            mHeadData = new HeadViewHodler();
            mHeadData.connHeadIcon = (ImageView) mHeadView.findViewById(R.id.conn_head_icon);
            mHeadData.connTxt = (TextView) mHeadView.findViewById(R.id.conn_txt);
            mHeadData.golukDelIcon = (ImageView) mHeadView.findViewById(R.id.goluk_del_icon);
            mHeadData.golukIcon = (ImageView) mHeadView.findViewById(R.id.goluk_icon);
            mHeadData.golukName = (TextView) mHeadView.findViewById(R.id.goluk_name);
            mHeadData.golukPointgreyIcon = (ImageView) mHeadView.findViewById(R.id.goluk_pointgrey_icon);
            mHeadData.golukConnLayout = (RelativeLayout) mHeadView.findViewById(R.id.goluk_conn_layout);
        }
        if (IPCControlManager.G1_SIGN.equals(mWifiBindConnectData.ipcSign)) {
            mHeadData.golukIcon.setImageResource(R.drawable.connect_g1_img);
        } else if (IPCControlManager.G2_SIGN.equals(mWifiBindConnectData.ipcSign)) {
            mHeadData.golukIcon.setImageResource(R.drawable.connect_g2_img);
        } else if (IPCControlManager.T1s_SIGN.equals(mWifiBindConnectData.ipcSign)) {
            mHeadData.golukIcon.setImageResource(R.drawable.connect_t1s_img);
        } else if (IPCControlManager.T1_SIGN.equals(mWifiBindConnectData.ipcSign)) {
            mHeadData.golukIcon.setImageResource(R.drawable.connect_t1_img);
        } else if (IPCControlManager.T2_SIGN.equals(mWifiBindConnectData.ipcSign)) {
            mHeadData.golukIcon.setImageResource(R.drawable.connect_t2_img);
        } else if (IPCControlManager.T3_SIGN.equals(mWifiBindConnectData.ipcSign)) {
            mHeadData.golukIcon.setImageResource(R.drawable.connect_t3_img);
        }
        // 设备连接成功
        if (mApp.isIpcLoginSuccess) {
            mHeadData.golukPointgreyIcon.setBackgroundResource(R.drawable.connect_pointgreen_icon);
            mHeadData.connTxt.setText(this.getResources().getString(R.string.unbind_select_connect_yes));
        } else {
            if (mApp.mWiFiStatus == MainActivity.WIFI_STATE_SUCCESS) {
                mHeadData.golukPointgreyIcon.setBackgroundResource(R.drawable.connect_pointgreen_icon);
                mHeadData.connTxt.setText(this.getResources().getString(R.string.unbind_select_connect_yes));
            } else {
                mHeadData.golukPointgreyIcon.setBackgroundResource(R.anim.wifi_connect_animation);
                // 获取AnimationDrawable对象
                AnimationDrawable animationDrawable = (AnimationDrawable) mHeadData.golukPointgreyIcon.getBackground();
                animationDrawable.start();

                // 显示加载中
                // mHeadData.golukPointgreyIcon.setBackgroundResource(R.drawable.connect_pointgrey_icon);
                mHeadData.connTxt.setText(this.getResources().getString(R.string.unbind_select_connect_ing));
            }
        }

        mHeadData.golukConnLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mApp.isIpcLoginSuccess) {
                    Intent intent = new Intent(WifiUnbindSelectListActivity.this, CarRecorderActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
        mHeadData.golukName.setText(mWifiBindConnectData.ipc_ssid);
        mHeadData.golukDelIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                final AlertDialog confirmation = new AlertDialog.Builder(WifiUnbindSelectListActivity.this,
                        R.style.CustomDialog).create();
                confirmation.show();
                confirmation.getWindow().setContentView(R.layout.unbind_dialog_confirmation);
                confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBindFinish eventFnish = new EventBindFinish(EventConfig.BIND_LIST_DELETE_CONFIG);
                        EventBus.getDefault().post(eventFnish);
                        mApp.setIpcDisconnect();
                        mListView.removeHeaderView(mHeadView);
                        WifiBindDataCenter.getInstance().deleteBindData(mWifiBindConnectData.ipc_ssid);
                        getBindHistoryData();
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
        });
    }

    /**
     * 创建wifi热点loading框
     *
     * @author jyf
     */
    public void showLoading(boolean isSingle) {
        isCanReceiveFailed = false;
        dimissLoading();
        String msg = "";
        if (isSingle) {
            msg = getResources().getString(R.string.unbind_loading_dialog_txt2);
        } else {
            msg = getResources().getString(R.string.unbind_loading_dialog_txt);
        }
        mLoadingDialog = new CustomLoadingDialog(this, msg);
        mLoadingDialog.setCancel(false);
        mLoadingDialog.show();
    }

    /**
     * 隐藏loading框
     *
     * @author jyf
     */
    private void dimissLoading() {
        if (null != mLoadingDialog) {
            mLoadingDialog.close();
            mLoadingDialog = null;
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.close_btn) {
            this.finish();
        } else if (id == R.id.edit_btn) {
            click_Edit();
        } else if (id == R.id.addMoblieBtn) {
            click_AddIpc();
        } else {
        }
    }

    public void onEventMainThread(EventWifiConnect event) {
        if (null == event) {
            return;
        }
        GolukDebugUtils.e("",
                "wifilist----WifiUnbindSelectListActivity----onEventMainThread----EventWifiConnect----state :  "
                        + event.getOpCode());
        switch (event.getOpCode()) {
            case EventConfig.WIFI_STATE_FAILED:
                ipcConnFailed();
                break;
            case EventConfig.WIFI_STATE_CONNING:
                if (mApp.isBindSucess()) {
                    ipcConnecting();
                }
                break;
            case EventConfig.WIFI_STATE_SUCCESS:
                if (mApp.isBindSucess()) {
                    ipcConnSucess();
                }
                break;
            default:
                break;
        }
    }

    private void ipcConnFailed() {
        if (!isCanReceiveFailed) {
            isCanReceiveFailed = true;
            return;
        }
        dimissLoading();
        getBindHistoryData();
        // GolukUtils.showToast(this, "conn failed");
    }

    private void ipcConnecting() {

    }

    private void ipcConnSucess() {
        getBindHistoryData();
        // GolukUtils.showToast(this, "conn success");
    }

    /**
     * 接受创建热点的消息
     *
     * @param event
     * @author jyf
     */
    public void onEventMainThread(EventWifiAuto event) {
        if (null == event || !mApp.isBindSucess()) {
            return;
        }
        if (event.eCode == EventConfig.CAR_RECORDER_RESULT) {
            if (0 != event.state) {
                // 连接失败
                return;
            }
            if (0 == event.process) {
                // 创建热点成功
                dimissLoading();
            }
        }
    }

    public void onEventMainThread(EventFinishWifiActivity event) {
        GolukDebugUtils.e("", "completeSuccess-------------SelectList");
        finish();
    }

    public void onEventMainThread(EventBinding event) {
        GolukDebugUtils.e("", "wifilist----WifiUnbindSelectListActivity----onEventMainThread----EventBinding----1");
        if (null == event) {
            return;
        }
        if (EventConfig.BINDING == event.getCode()) {
            // isCanAcceptMsg = event.getBinding();
        }
    }

    private void setNotEditState() {
        mListAdapter.mEditState = false;
        mEditBtn.setText(this.getResources().getString(R.string.edit_text));// 编辑
        if (mHeadData != null) {
            mHeadData.golukDelIcon.setVisibility(View.GONE);
            mHeadData.golukPointgreyIcon.setVisibility(View.VISIBLE);
        }
    }

    private void setEditState() {
        mListAdapter.mEditState = true;
        mEditBtn.setText(this.getResources().getString(R.string.short_input_ok));// 完成
        if (mHeadData != null) {
            mHeadData.golukDelIcon.setVisibility(View.VISIBLE);
            mHeadData.golukPointgreyIcon.setVisibility(View.GONE);
        }
    }

    private void click_Edit() {
        if (mListAdapter.mEditState) {
            setNotEditState();
        } else {
            setEditState();
        }
        getBindHistoryData();
        // mListAdapter.notifyDataSetChanged();
    }

    /**
     * 进入选择设备类型界面
     *
     * @author jyf
     */
    private void click_AddIpc() {
        // 还原状态
        if (mListAdapter.mEditState) {
            setNotEditState();
            mListAdapter.notifyDataSetChanged();
        }
        Intent intent = new Intent(this, WifiUnbindSelectTypeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dimissLoading();
        EventBus.getDefault().unregister(this);
    }
}
