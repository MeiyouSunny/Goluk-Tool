package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 编辑签名
 * @author mobnote
 *
 */
public class UserPersonalSignActivity extends Activity implements OnClickListener{

	//title
	Button btnBack,btnOk;
	TextView mTextTitle;
	//body
	EditText mEditBody;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_edit_sign);
		
		initView();
		//title
		mTextTitle.setText("编辑签名");
		
	}
	//初始化控件
	public void initView(){
		btnBack = (Button) findViewById(R.id.back_btn);
		btnOk = (Button) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mEditBody = (EditText) findViewById(R.id.user_personal_edit_sign_body);
		/**
		 * 监听
		 */
		btnBack.setOnClickListener(this);
		btnOk.setOnClickListener(this);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		//返回
		case R.id.back_btn:
			finish();
			break;
		//que认
		case R.id.user_title_right:

			break;

		default:
			break;
		}
	}
}
