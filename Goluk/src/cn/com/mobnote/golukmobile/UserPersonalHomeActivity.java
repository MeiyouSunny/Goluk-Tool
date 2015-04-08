package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 我的主页
 * @author mobnote
 *
 */
public class UserPersonalHomeActivity extends Activity implements OnClickListener{

	//title
	Button btnBack;
	TextView mTextTitle;
	//个人信息
	ImageView mImageHead,mImageSex,mImageArrow;
	TextView mTextName,mTextShare;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_homepage);
		
		
	}
	//初始化控件
	public void initView(){
		btnBack = (Button) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mImageHead = (ImageView) findViewById(R.id.user_personal_homepage_head);
		mImageSex = (ImageView) findViewById(R.id.user_personal_homepage_sex);
		mImageArrow = (ImageView) findViewById(R.id.user_personal_homepage_arrow);
		mTextName = (TextView) findViewById(R.id.user_personal_homepage_name);
		mTextShare = (TextView) findViewById(R.id.user_personal_homepage_share);
		/**
		 * 监听
		 */
		btnBack.setOnClickListener(this);
		mImageArrow.setOnClickListener(this);
		
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		//返回
		case R.id.back_btn:
			finish();
			break;
		//进入个人中心
		case R.id.user_personal_homepage_arrow:

			break;

		default:
			break;
		}
	}
}
