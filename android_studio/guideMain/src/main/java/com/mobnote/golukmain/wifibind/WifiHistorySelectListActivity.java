package com.mobnote.golukmain.wifibind;

import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindFinish;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.util.GolukUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class WifiHistorySelectListActivity extends BaseActivity implements OnClickListener {

    private ImageView mCloseBtn;
    private ListView mListView;
    private Button mbtnConnectGoluk;
    private TextView mtvChooseGoluk;
    private WifiHistorySelectListAdapter mListAdapter;
    private CustomLoadingDialog mLoadingDialog = null;

    public GolukApplication mApp = null;
    private boolean isCanReceiveFailed = false;
    private boolean mReturnToMainAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_connection_list);
        EventBus.getDefault().register(this);
        mApp = (GolukApplication) getApplication();
        initView();
        initListener();
        initData();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.listView);
        mCloseBtn = (ImageView) findViewById(R.id.close_btn);
        mbtnConnectGoluk = (Button) findViewById(R.id.btn_connect_my_goluk);
        mtvChooseGoluk = (TextView) findViewById(R.id.tv_choose_goluk_title);
        findViewById(R.id.addMoblieBtn).setOnClickListener(this);
    }

    private void initListener() {
        mCloseBtn.setOnClickListener(this);
        mbtnConnectGoluk.setOnClickListener(this);
    }

    private void initData() {
        mReturnToMainAlbum = getIntent().getBooleanExtra(MainActivity.INTENT_ACTION_RETURN_MAIN_ALBUM, false);
        mListAdapter = new WifiHistorySelectListAdapter(this);
        getBindHistoryData();
    }

    /**
     * 获取bind历史数据
     */
    public void getBindHistoryData() {
        GolukDebugUtils.e("", "select wifi ---history---WifiHistorySelectListActivity ------getBindHistoryData--1");
        List<WifiBindHistoryBean> binds = WifiBindDataCenter.getInstance().getAllBindData();
        /*根据需求，界面需要适配：
        1. 历史纪录多余1个的情况，适配选择列表
        2.只有1个历史纪录的情况
        */
        if (binds != null && binds.size() > 1) {
            mtvChooseGoluk.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.VISIBLE);
            mbtnConnectGoluk.setVisibility(View.GONE);
            mListView.setAdapter(mListAdapter);
            mListAdapter.setData(binds);
            mListAdapter.notifyDataSetChanged();
        } else {
            mtvChooseGoluk.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            mbtnConnectGoluk.setVisibility(View.VISIBLE);
            if (binds != null && binds.size() == 1) {
                mbtnConnectGoluk.setText(String.format(getResources().getString(R.string.start_connect_ipc_device), binds.get(0).ipc_ssid));
                mbtnConnectGoluk.setTag(binds.get(0));
            } else {
                GolukDebugUtils.e("", "select wifi ---history---WifiHistorySelectListActivity  ------getBindHistoryData--output---have no history");
            }
        }
    }

    /**
     * 创建wifi热点loading框
     */
    public void showLoading(boolean isSingle) {
        isCanReceiveFailed = false;
        dismissLoading();
        String msg;
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
     */
    private void dismissLoading() {
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
        } else if (id == R.id.addMoblieBtn) {
            click_AddIpc();
        } else if (id == R.id.btn_connect_my_goluk) {
            WifiBindHistoryBean bean = (WifiBindHistoryBean) mbtnConnectGoluk.getTag();
            if (bean == null) {
                //bean should never be null . this code should never execute.
                GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------getBindHistoryData--output---mbtnConnectGoluk.getTag is Null");
                return;
            }
            click_useIpc(bean, true);
        }
    }


    private void ipcConnFailed() {
        if (!isCanReceiveFailed) {
            isCanReceiveFailed = true;
            return;
        }
        if (mLoadingDialog != null) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.wifi_link_conn_failed));
            dismissLoading();
        }
    }

    private void ipcConnecting() {

    }

    private void ipcConnSuccess() {
        GolukUtils.showToast(this, getResources().getString(R.string.str_wifi_connect_success));
        if (mReturnToMainAlbum) {
            Intent it = new Intent(WifiHistorySelectListActivity.this, MainActivity.class);
            startActivity(it);
            finish();
        } else {
            Intent it = new Intent(WifiHistorySelectListActivity.this, CarRecorderActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(it);
            finish();
        }
    }

    /**
     * 进入选择设备类型界面
     */
    private void click_AddIpc() {
        Intent intent = new Intent(this, WiFiLinkListActivity.class);
        intent.putExtra(MainActivity.INTENT_ACTION_RETURN_MAIN_ALBUM, mReturnToMainAlbum);
        startActivity(intent);
        finish();
    }


    /**
     * 点击连接某个设备
     */
    public void click_useIpc(WifiBindHistoryBean bindHistoryBean, boolean isSingle) {
        if (null == bindHistoryBean) {
            return;
        }
        showLoading(isSingle);
        WifiBindDataCenter.getInstance().editBindStatus(bindHistoryBean.ipc_ssid, WifiBindHistoryBean.CONN_USE);
        EventBindFinish eventFnish = new EventBindFinish(EventConfig.CAR_RECORDER_BIND_CREATEAP);
        eventFnish.bean = bindHistoryBean;
        EventBus.getDefault().post(eventFnish);
    }


    public void onEventMainThread(EventWifiConnect event) {
        if (null == event) {
            return;
        }
        GolukDebugUtils.e("", "wifilist----WifiUnbindSelectListActivity----onEventMainThread----EventWifiConnect----state :  " + event.getOpCode());
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
                    ipcConnSuccess();
                }
                break;
            default:
                break;
        }
    }

    //TODO event执行的顺序有点乱 , 注释如下代码可能导致主界面成功连接之后，正在连接的窗口没有关闭
//    /**
//     * 接受创建热点的消息
//     */
//    public void onEventMainThread(EventWifiAuto event) {
//        if (null == event || !mApp.isBindSucess()) {
//            return;
//        }
//        if (event.eCode == EventConfig.CAR_RECORDER_RESULT) {
//            if (0 != event.state) {
//                // 连接失败
//                return;
//            }
//            if (0 == event.process) {
//                // 创建热点成功
//                //dismissLoading();
//            }
//        }
//    }

    public void onEventMainThread(EventFinishWifiActivity event) {
        GolukDebugUtils.e("", "completeSuccess-------------SelectList -------" + event.toString());
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mApp.setBinding(false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoading();
        EventBus.getDefault().unregister(this);
    }
}
