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
 * 编辑资料
 * @author mobnote
 *
 */
public class UserPersonalEditActivity extends Activity implements OnClickListener{

	//title
	Button btnBack,btnSave;
	TextView mTextTitle;
	//头像
	ImageView mImageHead,mImageHeadArrow;
	//昵称
	TextView mTextName;
	ImageView mImageNameArrow;
	//性别
	TextView mTextSex;
	ImageView mImageSexArrow;
	//个性签名
	TextView mTextSign;
	ImageView mImageSignArrow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_edit);
		
		initView();
		//title
		mTextTitle.setText("编辑资料");
		
	}
	//初始化控件
	public void initView(){
		//title
		btnBack = (Button) findViewById(R.id.back_btn);
		btnSave = (Button) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		//头像
		mImageHead = (ImageView) findViewById(R.id.user_person_edit_head_image);
		mImageHeadArrow = (ImageView) findViewById(R.id.user_personal_head_arrow);
		//昵称
		mTextName = (TextView) findViewById(R.id.user_personal_name_text);
		mImageNameArrow = (ImageView) findViewById(R.id.user_personal_name_arrow);
		//性别
		mTextSex = (TextView) findViewById(R.id.user_personal_sex_text);
		mImageSexArrow = (ImageView) findViewById(R.id.user_personal_sex_arrow);
		//个性签名
		mTextSign = (TextView) findViewById(R.id.user_personal_sign_text);
		mImageSignArrow = (ImageView) findViewById(R.id.user_personal_sign_arrow);
		
		/**
		 * 监听
		 */
		//title
		btnBack.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		//头像
		mImageHeadArrow.setOnClickListener(this);
		//昵称
		mImageNameArrow.setOnClickListener(this);
		//性别
		mImageSexArrow.setOnClickListener(this);
		//个性签名
		mImageSignArrow.setOnClickListener(this);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		//title返回
		case R.id.back_btn:
			finish();
			break;
		//保存
		case R.id.user_title_right:

			break;
		//头像into
		case R.id.user_personal_head_arrow:

			break;
		//昵称into
		case R.id.user_personal_name_arrow:

			break;
		//性别into
		case R.id.user_personal_sex_arrow:

			break;
		//个性签名into
		case R.id.user_personal_sign_arrow:

			break;
			
		default:
			break;
		}
	}
}
