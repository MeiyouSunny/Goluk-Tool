package cn.com.mobnote.golukmobile;

import cn.com.mobnote.user.UserPersonalHomeAdapter;
import cn.com.mobnote.util.console;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
	//适配器
	UserPersonalHomeAdapter adapter;
	ListView lv;
	RelativeLayout mLayoutInto;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_homepage);
		
		initView();
		//title
		mTextTitle.setText("我的主页");
		//需要解析出来的数据
//		adapter = new UserPersonalHomeAdapter(this, list);
//		lv.setAdapter(adapter);
		
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
		lv = (ListView) findViewById(R.id.user_personal_homepage_listview);
		mLayoutInto = (RelativeLayout) findViewById(R.id.user_personal_homepage_detail_layout);
		/**
		 * 监听
		 */
		btnBack.setOnClickListener(this);
		mImageArrow.setOnClickListener(this);
		mLayoutInto.setOnClickListener(this);
		
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
		case R.id.user_personal_homepage_detail_layout:
			console.log("++++++++++");
			Intent it = new Intent(UserPersonalHomeActivity.this,UserPersonalInfoActivity.class);
			startActivity(it);
			break;

		default:
			break;
		}
	}
}
