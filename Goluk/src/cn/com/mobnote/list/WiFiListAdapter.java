package cn.com.mobnote.list;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.WiFiLinkListActivity;
import cn.com.mobnote.list.WiFiListManage.WiFiListData;
import cn.com.mobnote.util.console;
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
public class WiFiListAdapter extends BaseAdapter{
	private Context mContext = null;
	private ArrayList<WiFiListData> mDataList = null;
	private LayoutInflater mLayoutInflater = null;
	/** 记录上一次点击的id */
	private int resIndex = 0;
	/** 当前已链接的wifi */
	private int linkIndex = -1;
	
	public WiFiListAdapter(Context context, ArrayList<WiFiListData> data) {
		mContext = context;
		mDataList = data;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	/**
	 * 
	 * @return
	 */
	public void changeWiFiStatus(){
		WiFiListData data = (WiFiListData)getItem(resIndex);
		data.wifiStatus = true;
		if(linkIndex > -1){
			WiFiListData data2 = (WiFiListData)getItem(linkIndex);
			data2.wifiStatus = false;
			linkIndex = resIndex;
		}
		this.notifyDataSetChanged();
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
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		WiFiListData data = (WiFiListData)mDataList.get(position);
		
		if(data.wifiStatus){
			linkIndex = position;
			holder.wifiStatus.setBackgroundResource(R.drawable.wifi_linked);
		}else{
			holder.wifiStatus.setBackgroundResource(R.drawable.wifi_no_link);
		}
		holder.wifiName.setText(data.wifiName);
		
		convertView.setOnClickListener(new onclick(position));
		return convertView;
	}
	
	
	
	class ViewHolder {
		TextView wifiName = null;
		ImageView wifiStatus = null;
	}
	
	
	class onclick implements OnClickListener{
		private int index;
		private String wifiName;
		public onclick(int index){
			this.index = index;
		}
		
		/**
		 * wifi链接系统输入框
		 * @param data
		 */
		private void inputTitleDialog(WiFiListData data) {
			final EditText inputServer = new EditText(mContext);
			//inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
			inputServer.setFocusable(true);
			
			wifiName = data.wifiName.toString();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(wifiName).setView(inputServer).setNegativeButton("取消", null);
			builder.setPositiveButton("连接",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String pwd = inputServer.getText().toString();
					if(!"".equals(pwd)){
						console.log("wifi---pwd---" + pwd);
						((WiFiLinkListActivity)mContext).connectWiFi(wifiName,pwd);
					}
					else{
						console.toast("请求输入WiFi密码", mContext);
					}
				}
			});
			
			builder.show();
			//AlertDialog ad = builder.create();
			//后来我在show()方法调用之前，用setView(new EditText())添加一个空的EditText，
			//由于是自定义的AlertDialog，有我们指定的布局，所以这个空的
		}
		
		/**
		 * 滤镜列表类别点击事件
		 */
		@Override
		public void onClick(View v) {
			resIndex = index;
			WiFiListData data = (WiFiListData)getItem(index);
			if(!data.wifiStatus){
				//判断wifi有没有密码,没有密码直接连接
				boolean hasPwd = true;
				if(hasPwd){
					inputTitleDialog(data);
				}
				else{
					//直接连接wifi
					String wifiName = data.wifiName.toString();
					((WiFiLinkListActivity)mContext).connectWiFi(wifiName);
				}
			}
			else{
				console.toast("已连接" + data.wifiName, mContext);
			}
//			((VideoEditMusicActivity)mContext).mMusicListAdapter.notifyDataSetChanged();
//			((VideoEditMusicActivity)mContext).changeNoMusicStatus(false,data.filePath);
		}
	}
}

