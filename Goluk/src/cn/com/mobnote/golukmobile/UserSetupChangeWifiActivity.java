package cn.com.mobnote.golukmobile;

import cn.com.mobnote.user.UserUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 功能：设置页中修改极路客WIFI密码
 * @author mobnote
 *
 */
public class UserSetupChangeWifiActivity extends BaseActivity implements OnClickListener,OnTouchListener{

	/**title部分**/
	private ImageButton mBtnBack = null;//返回
	private TextView mTextTitle = null;//title
	private Button mBtnSave = null;//保存
	/**body**/
	private EditText mEditText = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_setup_changewifi_password);
		
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mTextTitle.setText("极路客WiFi密码");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		initView();
	}
	
	//初始化
	public void initView(){
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mBtnSave = (Button) findViewById(R.id.user_title_right);
		mEditText = (EditText) findViewById(R.id.changewifi_password_editText);
		
		/**
		 * 获取摄像头管理页面传来的WIFI密码
		 */
		Intent it = getIntent();
		String password = it.getStringExtra("wifiPwd").toString();
		GolukDebugUtils.i("lily", password+"------ChangeWiFiPassword----");
		if(!"".equals(password)){
			mEditText.setText(password);
		}else{
			mEditText.setText("");
		}
		
		//监听
		mBtnBack.setOnClickListener(this);
		mBtnSave.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		//返回
		case R.id.back_btn:
			this.finish();
			break;
		//保存
		case R.id.user_title_right:
			//点击保存按钮隐藏软件盘
			UserUtils.hideSoftMethod(UserSetupChangeWifiActivity.this);
			break;
		default:
			break;
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_title_right:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mBtnSave.setTextColor(Color.rgb(0, 197, 176));
				break;
			case MotionEvent.ACTION_UP:
				mBtnSave.setTextColor(Color.WHITE);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return false;
	}
}
