package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 编辑资料
 * @author mobnote
 *
 */
public class UserPersonalEditActivity extends Activity implements OnClickListener{

	//title
	Button btnBack;
	TextView mTextTitle;
	LinearLayout mLayoutInto;
	ImageView mImageRight;
	TextView mTextRight;
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
	//点击每一项
	RelativeLayout mLayoutHead,mLayoutName,mLayoutSex,mLayoutSign;
		
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
		mLayoutInto = (LinearLayout) findViewById(R.id.user_personal_edit_title_layout);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mImageRight = (ImageView) findViewById(R.id.user_personal_edit_title_image);
		mTextRight = (TextView) findViewById(R.id.user_personal_edit_title_text);
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
		//点击每一项
		mLayoutHead = (RelativeLayout) findViewById(R.id.user_personal_edit_layout1);
		mLayoutName = (RelativeLayout) findViewById(R.id.user_personal_edit_layout2);
		mLayoutSex = (RelativeLayout) findViewById(R.id.user_personal_edit_layout3);
		mLayoutSign = (RelativeLayout) findViewById(R.id.user_personal_edit_layout4);
				
		/**
		 * 监听
		 */
		//title
		btnBack.setOnClickListener(this);
		mLayoutInto.setOnClickListener(this);
		//头像
		mLayoutHead.setOnClickListener(this);
		//昵称
		mLayoutName.setOnClickListener(this);
		//性别
		mLayoutSex.setOnClickListener(this);
		//个性签名
		mLayoutSign.setOnClickListener(this);
		
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
		case R.id.user_personal_edit_title_layout:
			//点击保存将修改的数据存储
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
		/**
		 * 点击每一项
		 */
			//点击头像
		case R.id.user_personal_edit_layout1:
			
			break;
		//点击昵称
		case R.id.user_personal_edit_layout2:
			Intent itName = new Intent(UserPersonalEditActivity.this,UserPersonalNameActivity.class);
			startActivity(itName);
			break;
		//点击性别
		case R.id.user_personal_edit_layout3:
			
			break;
		//点击个性签名
		case R.id.user_personal_edit_layout4:
			Intent itSign = new Intent(UserPersonalEditActivity.this,UserPersonalSignActivity.class);
			startActivity(itSign);
			break;
		default:
			break;
		}
	}
}
