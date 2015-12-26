package cn.com.mobnote.golukmobile.wifibind;

import java.util.List;

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
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventBinding;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventFinishWifiActivity;
import cn.com.mobnote.eventbus.EventWifiAuto;
import cn.com.mobnote.eventbus.EventWifiConnect;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.wifibind.WifiUnbindSelectListAdapter.HeadViewHodler;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindDataCenter;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindHistoryBean;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class WifiUnbindSelectListActivity extends BaseActivity implements OnClickListener {

	/** 关闭按钮 **/
	private ImageView mCloseBtn;
	/** 数据列表 **/
	private ListView mListView;
	/** 没有数据时的默认布局 **/
	private RelativeLayout mEmptyLayout;
	/** 编辑按钮 **/
	private Button mEditBtn;
	/** 连接中headView **/
	public View mHeadView = null;
	public HeadViewHodler mHeadData = null;
	private WifiBindHistoryBean mWifiBindConnectData = null;
	private WifiUnbindSelectListAdapter mListAdapter;
	private CustomLoadingDialog mLoadingDialog = null;
	private GolukApplication mApp = null;
	private boolean isCanReceiveFailed = true;
	/** 控制ListView Header的显示与删除 */
	private boolean isHasHeaderView = false;
	/** 控制是否可以接受连接信息 */
	private boolean isCanAcceptMsg = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unbind_connection_list);
		EventBus.getDefault().register(this);
		mApp = (GolukApplication) getApplication();
		isCanAcceptMsg = true;
		initView();
		initLisenner();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isCanAcceptMsg = true;
		mApp.isBinding = false;
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		mListView = (ListView) findViewById(R.id.listView);
		mEmptyLayout = (RelativeLayout) findViewById(R.id.emptyLayout);
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
		mEditBtn = (Button) findViewById(R.id.edit_btn);
		findViewById(R.id.addMoblieBtn).setOnClickListener(this);
	}

	/**
	 * 初始化view的监听
	 */
	private void initLisenner() {
		mCloseBtn.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);
	}

	/** 初始化数据 **/
	private void initData() {
		mListView.setEmptyView(mEmptyLayout);
		mListAdapter = new WifiUnbindSelectListAdapter(this);
		mListView.setAdapter(mListAdapter);
		getBindHistoryData();
	}

	/**
	 * 获取最新的bind数据
	 * 
	 */
	public void getBindHistoryData() {
		GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------getBindHistoryData--1");
		List<WifiBindHistoryBean> binds = WifiBindDataCenter.getInstance().getAllBindData();
		if (binds == null) {
			return;
		}
		if (isCanShowListViewHead()) {
			GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------getBindHistoryData--size: "
					+ binds.size());
			for (int i = 0; i < binds.size(); i++) {
				WifiBindHistoryBean bind = binds.get(i);
				if (bind.state == WifiBindHistoryBean.CONN_USE) {
					mWifiBindConnectData = bind;
					GolukDebugUtils.e("",
							"select wifibind---WifiUnbindSelectListActivity ------getBindHistoryData--select ssid: "
									+ mWifiBindConnectData.ipc_ssid);
					refreshHeadData();
					if (binds.size() > 1) {
						binds.remove(i);
						binds.add(bind);
					}
					break;
				}

			}
		} else {
			GolukDebugUtils
					.e("", "select wifibind---WifiUnbindSelectListActivity ------getBindHistoryData--not show: ");
			if (null != mHeadView && isHasHeaderView) {
				this.removeListViewHead(mHeadView);
			}
		}
		GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------getBindHistoryData--setData: "
				+ binds.size());
		mListAdapter.setData(binds);
		mListAdapter.notifyDataSetChanged();

	}

	public boolean isCanShowListViewHead() {
		WifiBindHistoryBean temp = WifiBindDataCenter.getInstance().getCurrentUseIpc();
		if (mApp.isIpcLoginSuccess || (mApp.mWiFiStatus != MainActivity.WIFI_STATE_FAILED && null != temp)) {
			GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------isCanShowListViewHead--true");
			return true;
		}
		GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------isCanShowListViewHead--false");
		return false;
	}

	private void refreshHeadData() {
		if (mHeadView == null) {
			mHeadView = LayoutInflater.from(this).inflate(R.layout.unbind_connection_head, null);
		}
		if (!isHasHeaderView) {
			addListViewHead(mHeadView);
		}

		mListView.setAdapter(mListAdapter);

		mHeadView.setVisibility(View.VISIBLE);
		if (mHeadData == null) {
			mHeadData = new HeadViewHodler();
			mHeadData.connHeadIcon = (ImageView) mHeadView.findViewById(R.id.conn_head_icon);
			mHeadData.connTxt = (TextView) mHeadView.findViewById(R.id.conn_txt);
			mHeadData.golukDelIcon = (ImageView) mHeadView.findViewById(R.id.goluk_del_icon);
			mHeadData.golukIcon = (ImageView) mHeadView.findViewById(R.id.goluk_icon);
			mHeadData.golukName = (TextView) mHeadView.findViewById(R.id.goluk_name);
			mHeadData.golukPointgreyIcon = (ImageView) mHeadView.findViewById(R.id.goluk_pointgrey_icon);
		}
		if (IPCControlManager.G1_SIGN.equals(mWifiBindConnectData.ipcSign)) {
			mHeadData.golukIcon.setImageResource(R.drawable.connect_g1_img);
		} else if (IPCControlManager.G2_SIGN.equals(mWifiBindConnectData.ipcSign)) {
			mHeadData.golukIcon.setImageResource(R.drawable.connect_g2_img);
		} else if (IPCControlManager.G1s_SIGN.equals(mWifiBindConnectData.ipcSign)) {
			mHeadData.golukIcon.setImageResource(R.drawable.connect_t1_img);
		} else if (IPCControlManager.T1_SIGN.equals(mWifiBindConnectData.ipcSign)) {
			mHeadData.golukIcon.setImageResource(R.drawable.connect_t1_img);
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
	public void showLoading() {
		isCanReceiveFailed = false;
		dimissLoading();
		mLoadingDialog = new CustomLoadingDialog(this, "正在创建热点");
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

	/**
	 * 添加头部
	 * 
	 * @param view
	 */
	public void addListViewHead(View view) {
		GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------addListViewHead: ");
		isHasHeaderView = true;
		mListView.addHeaderView(view);
	}

	public void removeListViewHead(View view) {
		GolukDebugUtils.e("", "select wifibind---WifiUnbindSelectListActivity ------removeListViewHead: ");
		isHasHeaderView = false;
		mListView.removeHeaderView(view);
		mListView.setAdapter(mListAdapter);

		// view.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close_btn:
			this.finish();
			break;
		case R.id.edit_btn:
			click_Edit();
			break;
		case R.id.addMoblieBtn:
			click_AddIpc();
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventWifiConnect event) {
		if (null == event || !isCanAcceptMsg) {
			return;
		}
		switch (event.getOpCode()) {
		case EventConfig.WIFI_STATE_FAILED:
			ipcConnFailed();
			break;
		case EventConfig.WIFI_STATE_CONNING:
			ipcConnecting();
			break;
		case EventConfig.WIFI_STATE_SUCCESS:
			ipcConnSucess();
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
		GolukUtils.showToast(this, "conn failed");
	}

	private void ipcConnecting() {

	}

	private void ipcConnSucess() {
		getBindHistoryData();
		GolukUtils.showToast(this, "conn success");
	}

	/**
	 * 接受创建热点的消息
	 * 
	 * @param event
	 * @author jyf
	 */
	public void onEventMainThread(EventWifiAuto event) {
		if (null == event || !isCanAcceptMsg) {
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
		finish();
	}

	public void onEventMainThread(EventBinding event) {
		if (null == event) {
			return;
		}
		if (EventConfig.BINDING == event.getCode()) {
			isCanAcceptMsg = event.getBinding();
		}
	}

	private void click_Edit() {
		if (mListAdapter.mEditState) {
			mListAdapter.mEditState = false;
			mEditBtn.setText(this.getResources().getString(R.string.edit_text));// 编辑
			if (mHeadData != null) {
				mHeadData.golukDelIcon.setVisibility(View.GONE);
				mHeadData.golukPointgreyIcon.setVisibility(View.VISIBLE);
			}
		} else {
			mListAdapter.mEditState = true;
			mEditBtn.setText(this.getResources().getString(R.string.short_input_ok));// 完成
			if (mHeadData != null) {
				mHeadData.golukDelIcon.setVisibility(View.VISIBLE);
				mHeadData.golukPointgreyIcon.setVisibility(View.GONE);
			}
		}
		mListAdapter.notifyDataSetChanged();
	}

	/**
	 * 进入选择设备类型界面
	 * 
	 * @author jyf
	 */
	private void click_AddIpc() {
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
