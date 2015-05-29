package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.user.UserUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 我的主页
 * @author mobnote
 *
 */
public class UserPersonalHomeActivity extends BaseActivity implements OnClickListener{

	//title
	private ImageButton btnBack;
	private TextView mTextTitle;
	//个人信息
	private ImageView mImageHead,mImageSex,mImageArrow;
	private TextView mTextName;
//	private TextView mTextShare;
	//适配器
//	private UserPersonalHomeAdapter adapter;
//	private ListView lv;
	private RelativeLayout mLayoutInto;
	
	//application
	private GolukApplication mApplication = null;
	// context
	private Context mContext = null;
	
	private String head;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_homepage);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserPersonalHome");
		
		initView();
		//title
		mTextTitle.setText("我的主页");
		//需要解析出来的数据
//		adapter = new UserPersonalHomeAdapter(this, list);
//		lv.setAdapter(adapter);
		
		initData();
	}
	//初始化控件
	public void initView(){
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mImageHead = (ImageView) findViewById(R.id.user_personal_homepage_head);
		mImageSex = (ImageView) findViewById(R.id.user_personal_homepage_sex);
		mImageArrow = (ImageView) findViewById(R.id.user_personal_homepage_arrow);
		mTextName = (TextView) findViewById(R.id.user_personal_homepage_name);
//		mTextShare = (TextView) findViewById(R.id.user_personal_homepage_share);
//		lv = (ListView) findViewById(R.id.user_personal_homepage_listview);
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
		switch (arg0.getId()) {
		//返回
		case R.id.back_btn:
			finish();
			break;
		//进入个人中心
		case R.id.user_personal_homepage_detail_layout:
			Intent it = new Intent(UserPersonalHomeActivity.this,UserPersonalInfoActivity.class);
			startActivity(it);
			break;

		default:
			break;
		}
	}
	
	/**
	 * 个人资料信息
	 */
	public void initData(){
		GolukDebugUtils.i("lily", mApplication.mGoluk+"===");
		String info = mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try{
			JSONObject json = new JSONObject(info);
			head = json.getString("head");
			String name = json.getString("nickname");
			String sex = json.getString("sex");
			GolukDebugUtils.i("lily", "-----UserPersonalHomeActivity-----"+json.toString());
	
			mTextName.setText(name);
			UserUtils.focusHead(head, mImageHead);
			if(sex.equals("1")){
				mImageSex.setImageResource(R.drawable.more_man);
			}else if(sex.equals("2")){
				mImageSex.setImageResource(R.drawable.more_girl);
			}else if(sex.equals("0")){
				mImageSex.setImageResource(R.drawable.more_no_log_in_icon);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
