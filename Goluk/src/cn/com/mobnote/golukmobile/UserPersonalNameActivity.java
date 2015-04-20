package cn.com.mobnote.golukmobile;

import cn.com.mobnote.util.console;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 编辑昵称
 * @author mobnote
 *
 */
public class UserPersonalNameActivity extends Activity implements OnClickListener{

	//title
	Button btnBack;
	TextView mTextTitle,mTextOk;
	//body
	EditText mEditName;
	ImageView mImageNameRight;
	private String nameText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_edit_name);
		
		initView();
		//title
		mTextTitle.setText("编辑昵称");
		mTextOk.setText("确认");
		
	}
	//初始化控件
	public void initView(){
		btnBack = (Button) findViewById(R.id.back_btn);
		mTextOk = (TextView) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mEditName = (EditText) findViewById(R.id.user_personal_name_edit);
		mImageNameRight = (ImageView) findViewById(R.id.user_personal_name_image);
		
		/**
		 * 获取从编辑界面传来的姓名
		 */
		Intent it = getIntent();
		if(null!=it.getStringExtra("intentNameText")){
			Bundle bundle = it.getExtras();
			nameText = bundle.getString("intentNameText");
		}
		mEditName.setText(nameText);
		
		/**
		 * 监听
		 */
		btnBack.setOnClickListener(this);
		mTextOk.setOnClickListener(this);
		mImageNameRight.setOnClickListener(this);
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
			String name = mEditName.getText().toString();
			Intent it = new Intent(UserPersonalNameActivity.this,UserPersonalEditActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("itName", name);
			it.putExtras(bundle);
			this.setResult(1, it);
			this.finish();
			break;
		//
		case R.id.user_personal_name_image:
			//点击晴空
			mEditName.setText("");
			break;

		default:
			break;
		}
	}

}
