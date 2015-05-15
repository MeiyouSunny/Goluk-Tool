package cn.com.mobnote.golukmobile;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UnbindActivity extends BaseActivity implements OnClickListener{

	//title
	private Button mBackBtn = null;
	private TextView mTextTitle = null;
	//body
	private RelativeLayout mHaveipcLayout = null;
	private RelativeLayout mNoipcLayout = null;
	private TextView mTextCameraName = null;
	private Button mUnbindBtn = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.unbind_layout);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		initView();
		
		
	}
	
	//初始化
	public void initView(){
		//title
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		//body
		mHaveipcLayout = (RelativeLayout) findViewById(R.id.unbind_layout_haveipc);
		mNoipcLayout = (RelativeLayout) findViewById(R.id.unbind_layout_noipc);
		mTextCameraName = (TextView) findViewById(R.id.unbind_camera_name);
		mUnbindBtn = (Button) findViewById(R.id.unbind_layout_btn);
		
		mTextTitle.setText("摄像头管理");
		mUnbindBtn.setText("解除摄像头绑定");
		
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
			
			break;

		default:
			break;
		}
	}
	
	// 是否綁定过 Goluk
			private boolean isBindSucess() {
				SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind",
						MODE_PRIVATE);
				// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
				return preferences.getBoolean("isbind", false);
			}
			
			//解绑
			// 是否綁定过 Goluk
			private void isBindSucess1() {
				SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind",
						MODE_PRIVATE);
				// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
			preferences.edit().putBoolean("isbind", false);
			}
	
}
