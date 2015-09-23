package cn.com.mobnote.golukmobile.wifibind;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.wifibind.WiFiListManage.WiFiListData;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:wifi列表数据适配
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("InflateParams")
public class WiFiListAdapter extends BaseAdapter {
	private Context mContext = null;
	private ArrayList<WiFiListData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 记录上一次点击的id */
	private int resIndex = -1;
	/** 当前已链接的wifi */
	private int linkIndex = -1;

	public WiFiListAdapter(Context context, ArrayList<WiFiListData> data) {
		mContext = context;
		mDataList = data;
		mLayoutInflater = LayoutInflater.from(context);
	}

	/**
	 * 修改wifi连接状态
	 * 
	 * @return
	 */
	public void changeWiFiStatus() {
		if (resIndex > -1) {
			WiFiListData data = (WiFiListData) getItem(resIndex);
			data.wifiStatus = true;
			if (linkIndex > -1) {
				WiFiListData data2 = (WiFiListData) getItem(linkIndex);
				data2.wifiStatus = false;
				linkIndex = resIndex;
			}
			this.notifyDataSetChanged();
		}
	}

	public void refreshConnectState(String name, String mac) {
		if (null == mDataList || mDataList.size() <= 0) {
			return;
		}
		final int size = mDataList.size();
		for (int i = 0; i < size; i++) {
			WiFiListData temp = mDataList.get(i);
			if (temp.wifiName.equals(name) && temp.mac.equals(mac)) {
				temp.wifiRealState = true;
			} else {
				temp.wifiRealState = false;
			}
		}

		this.notifyDataSetChanged();

	}

	/**
	 * 获取是否已连接ipc热点
	 * 
	 * @return
	 */
	public int getLinkIndex() {
		return linkIndex;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList == null ? null : mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.wifi_link_list_item, null);
			holder.wifiName = (TextView) convertView.findViewById(R.id.wifi_name);
			holder.wifiStatus = (ImageView) convertView.findViewById(R.id.wifi_status);
			holder.pwdStatus = (ImageView) convertView.findViewById(R.id.wifi_pwd_status);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		WiFiListData data = (WiFiListData) mDataList.get(position);

		if (data.wifiRealState) {
			linkIndex = position;
			holder.wifiStatus.setBackgroundResource(R.drawable.connect_wifi_icon);
			holder.pwdStatus.setBackgroundResource(R.drawable.connect_lock_icon);
		} else {
			holder.wifiStatus.setBackgroundResource(R.drawable.connect_wifi_icon_ash);
			holder.pwdStatus.setBackgroundResource(R.drawable.connect_lock_icon_ash);
		}
		if (!data.hasPwd) {
			// 有密码返回false
			holder.pwdStatus.setVisibility(View.VISIBLE);
		} else {
			holder.pwdStatus.setVisibility(View.GONE);
		}
		holder.wifiName.setText(data.wifiName);

		convertView.setOnClickListener(new onclick(position));
		return convertView;
	}

	class ViewHolder {
		TextView wifiName = null;
		ImageView wifiStatus = null;
		ImageView pwdStatus = null;
	}

	class onclick implements OnClickListener {
		private int index;
		private String wifiName;
		private String mac;

		public onclick(int index) {
			this.index = index;
		}

		/**
		 * wifi链接系统输入框
		 * 
		 * @param data
		 */
		private void inputTitleDialog(WiFiListData data) {
			final EditText inputServer = new EditText(mContext);
			// inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
			inputServer.setFocusable(true);

			wifiName = data.wifiName.toString();
			mac = data.mac.toString();
			
			final AlertDialog dialog = new AlertDialog.Builder(mContext,R.style.CustomDialog).create();
			dialog.show();
			dialog.getWindow().setContentView(R.layout.live_wifi_dialog);
			TextView view = (TextView) dialog.getWindow().findViewById(R.id.wifiname);
			view.setText(wifiName);
			final EditText et =  (EditText) dialog.getWindow().findViewById(R.id.wifipwd);
			et.setFocusable(true);
			dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			dialog.getWindow().findViewById(R.id.wifi_exit).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			
			dialog.getWindow().findViewById(R.id.wifi_connection).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String pwd = et.getText().toString();
					if(pwd == null || "".equals(pwd)){
						GolukUtils.showToast(mContext, "请输入WiFi密码");
					}else{
						((WiFiLinkListActivity)mContext).connectWiFi(wifiName,mac,pwd);
						dialog.dismiss();
					}
				}
			});
			

			/*
			 * AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			 * builder
			 * .setTitle(wifiName).setView(inputServer).setNegativeButton("取消",
			 * null); builder.setPositiveButton("连接",new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int which) { String pwd =
			 * inputServer.getText().toString(); if(!"".equals(pwd)){
			 * GolukDebugUtils.e("","wifi---pwd---" + pwd);
			 * ((WiFiLinkListActivity)mContext).connectWiFi(wifiName,mac,pwd); }
			 * else{ GolukUtils.showToast(mContext, "请输入WiFi密码"); } } });
			 */
			
			// AlertDialog ad = builder.create();
			// 后来我在show()方法调用之前，用setView(new EditText())添加一个空的EditText，
			// 由于是自定义的AlertDialog，有我们指定的布局，所以这个空的
		}

		/**
		 * 滤镜列表类别点击事件
		 */
		@Override
		public void onClick(View v) {
			resIndex = index;
			WiFiListData data = (WiFiListData) getItem(index);
			if (!data.wifiRealState) {
				// 判断wifi有没有密码,没有密码直接连接
				boolean hasPwd = data.hasPwd;
				if (!hasPwd) {
					inputTitleDialog(data);
				} else {
					// 直接连接wifi
					String wifiName = data.wifiName.toString();
					String mac = data.mac.toString();
					((WiFiLinkListActivity) mContext).connectWiFi(wifiName, mac);
				}
			} else {
				GolukUtils.showToast(mContext, "已连接" + data.wifiName + "....");
				// ((WiFiLinkListActivity)mContext).sendLogicLinkIpc();
			}
			// ((VideoEditMusicActivity)mContext).mMusicListAdapter.notifyDataSetChanged();
			// ((VideoEditMusicActivity)mContext).changeNoMusicStatus(false,data.filePath);
		}
	}
}