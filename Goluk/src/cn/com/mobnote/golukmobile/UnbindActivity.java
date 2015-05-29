package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.tiros.debug.GolukDebugUtils;

public class UnbindActivity extends Activity implements OnClickListener{

	//title
	private ImageButton mBackBtn = null;
	private TextView mTextTitle = null;
	//body
	private RelativeLayout mHaveipcLayout = null;
	private RelativeLayout mNoipcLayout = null;
	private TextView mTextCameraName = null;
	private Button mUnbindBtn = null;
	
	private GolukApplication mApplication = null;
	private Context mContext = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.unbind_layout);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "Unbind");
		
		initView();
		
	}
	
	//初始化
	public void initView(){
		//title
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		//body
		mHaveipcLayout = (RelativeLayout) findViewById(R.id.unbind_layout_haveipc);
		mNoipcLayout = (RelativeLayout) findViewById(R.id.unbind_layout_noipc);
		mTextCameraName = (TextView) findViewById(R.id.unbind_camera_name);
		mUnbindBtn = (Button) findViewById(R.id.unbind_layout_btn);
		
		mTextTitle.setText("摄像头管理");
		mUnbindBtn.setText("解除摄像头绑定连接");
		
		/**
		 * 判断是否是否绑定
		 * true  绑定  显示解绑UI
		 * false  未绑定  显示未绑定UI
		 */
		boolean b = this.isBindSucess();
		GolukDebugUtils.e("bind", "====isBindSuccess===="+b);
		if(b){
			mHaveipcLayout.setVisibility(View.VISIBLE);
			mNoipcLayout.setVisibility(View.GONE);
			String ipcName = this.ipcName();
			mTextCameraName.setText(ipcName);
		}else{
			mHaveipcLayout.setVisibility(View.GONE);
			mNoipcLayout.setVisibility(View.VISIBLE);
		}
		
		/**
		 * 监听
		 */
		mBackBtn.setOnClickListener(this);
		mUnbindBtn.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back_btn:
			this.finish();
			break;
		case R.id.unbind_layout_btn:
			new AlertDialog.Builder(this)
			.setMessage("解除摄像头绑定连接？")
			.setNegativeButton("取消", null)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					toUnbind();
					mHaveipcLayout.setVisibility(View.GONE);
					mNoipcLayout.setVisibility(View.VISIBLE);
					mApplication.mIPCControlManager.setIPCWifiState(false, null);
					mApplication.setIpcLoginOut();
					
				}
			}).create().show();
			break;

		default:
			break;
		}
	}
	
	public String ipcName() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind",MODE_PRIVATE);
		return preferences.getString("ipc_bind_name", "");
		
	}
	// 是否綁定过 Goluk  true为绑定
	public boolean isBindSucess() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind",MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		return preferences.getBoolean("isbind", false);
	}
			
	// 解绑
	public void toUnbind(){
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind",MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		Editor mEditor = preferences.edit();
		mEditor.putBoolean("isbind", false);
		mEditor.commit();
	}
	
}
