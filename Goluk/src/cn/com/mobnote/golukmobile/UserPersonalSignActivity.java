package cn.com.mobnote.golukmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 编辑签名
 * @author mobnote
 *
 */
public class UserPersonalSignActivity extends BaseActivity implements OnClickListener{

	/**itle**/
	private ImageButton btnBack;
	private TextView mTextTitle,mTextOk;
	/**body**/
	private EditText mEditBody;
	private String signText;
	/**最大字数限制**/
	private TextView mTextCount = null;
	private static final int MAX_COUNT = 50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_edit_sign);
		
		initView();
		//title
		mTextTitle.setText("编辑签名");
		mTextOk.setText("确认");
		//
		int count = mEditBody.getText().toString().length();
		mTextCount.setText("（"+(MAX_COUNT-count)+"/"+MAX_COUNT+"）");
		
	}
	//初始化控件
	public void initView(){
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextOk = (TextView) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mEditBody = (EditText) findViewById(R.id.user_personal_edit_sign_body);
		mTextCount = (TextView) findViewById(R.id.number_count);
		
		/**
		 * 获取从编辑界面传来的信息
		 * 
		 */
		Intent it = this.getIntent();
		if(null!= it.getStringExtra("intentSignText")){
			Bundle bundle = it.getExtras();
			signText = bundle.getString("intentSignText");
		}
		mEditBody.setText(signText);
		
		/**
		 * 监听
		 */
		btnBack.setOnClickListener(this);
		mTextOk.setOnClickListener(this);
		mEditBody.addTextChangedListener(mTextWatcher);
	}
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		//返回
		case R.id.back_btn:
			finish();
			break;
		//que认
		case R.id.user_title_right:
			String body = mEditBody.getText().toString();
			Intent it = new Intent(UserPersonalSignActivity.this,UserPersonalInfoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("itSign", body);
			it.putExtras(bundle);
			this.setResult(2, it);
			this.finish();
			break;

		default:
			break;
		}
	}
	
TextWatcher mTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			int num = arg0.length();
			int number = MAX_COUNT - num;
			if(number < 0){
				number = 0;
			}
			mTextCount.setText("（"+number + "/"+MAX_COUNT+"）");
		}
	};
}
