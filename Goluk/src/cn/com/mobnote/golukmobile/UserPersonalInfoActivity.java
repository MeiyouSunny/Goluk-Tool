package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.user.UserUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 个人资料
 * @author mobnote
 *
 */
public class UserPersonalInfoActivity extends Activity implements OnClickListener,OnTouchListener{

	//title
	private Button backBtn,rightBtn;
	private TextView centerTitle;
	//姓名、id、头像、分享按钮
	private ImageView mImageHead,mImageShare;
	private TextView mTextName,mTextId;
	//个性签名，性别，新浪微博
	private LinearLayout mLinearSignAll;
	private TextView mTextSignDetail,mTextSex,mTextSinaName;
	private ImageView mImageSinaHead;
	
	// application
	private GolukApplication mApplication = null;
	// context
	private Context mContext = null;
	//详细信息界面的head编号
	private String head;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_info);
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserPersonalEdit");
		
		intiView();
		//设置title
		centerTitle.setText("个人资料");
				
		initData();
		
	}
	//初始化控件
	public void intiView(){
		//title——返回按钮、中间标题、右边提示部分
		backBtn = (Button) findViewById(R.id.back_btn);
		rightBtn = (Button) findViewById(R.id.user_title_right);
		centerTitle = (TextView) findViewById(R.id.user_title_text);
		//body
		mImageHead = (ImageView) findViewById(R.id.user_personal_info_image);
		mImageShare = (ImageView) findViewById(R.id.user_personal_info_share);
		mTextName = (TextView) findViewById(R.id.user_personal_info_name);
		mTextId = (TextView) findViewById(R.id.user_personal_info_id);
		//个性签名、性别、新浪微博
		mLinearSignAll = (LinearLayout) findViewById(R.id.user_personal_info_all);
		mTextSignDetail = (TextView) findViewById(R.id.user_personal_info_sing_text);
		mTextSex = (TextView) findViewById(R.id.user_personal_info_sex_text);
		mTextSinaName = (TextView) findViewById(R.id.user_personal_info_weibo_name);
		mImageSinaHead = (ImageView) findViewById(R.id.user_personal_info_weibo_image);
				
		
		/**
		 * 点击事件
		 */
		backBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		rightBtn.setOnTouchListener(this);
	}
	
	/**
	 * 个人资料信息
	 */
	public void initData(){
		/**
		 * 修改用户信息成功后，获得用户信息
		 */
		Intent intentSave = getIntent();
		if(null != intentSave.getStringExtra("saveName")){
			String name = intentSave.getStringExtra("saveName").toString();
			mTextName.setText(name);
		}
		if(null != intentSave.getStringExtra("saveHead")){
			head = intentSave.getStringExtra("saveHead").toString();
			Log.i("nnn", "&&&&&&----"+head);
			//设置头像==================================
			UserUtils.userHeadChange(mImageHead, head, mTextSex);
		}
		if(null != intentSave.getStringExtra("saveSex")){
			String sex = intentSave.getStringExtra("saveSex").toString();
			mTextSex.setText(sex);
		}
		
		String info = mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try{
			JSONObject json = new JSONObject(info);
			String desc = json.getString("desc");
			head = json.getString("head");
			String name = json.getString("nickname");
			String sex = json.getString("sex");
			String aId = json.getString("aid");
	
			mTextName.setText(name);
			mTextId.setText(aId);
			mTextSignDetail.setText(desc);
			Log.i("mmm", head);
			UserUtils.userHeadChange(mImageHead, head,mTextSex);
			if(sex.equals("1")){
				mTextSex.setText("男");
			}else if(sex.equals("2")){
				mTextSex.setText("女");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	//点击效果
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_title_right:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				rightBtn.setTextColor(Color.rgb(0, 197, 176));
				break;
			case MotionEvent.ACTION_UP:
				rightBtn.setTextColor(Color.WHITE);
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
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		//title部分返回按钮
		case R.id.back_btn:
			finish();
			break;
		//title部分右边编辑按钮
		case R.id.user_title_right:
			String nickName = mTextName.getText().toString();
			String nickId = mTextId.getText().toString();
			String nickDesc = mTextSignDetail.getText().toString();
			String nickSex = mTextSex.getText().toString();
			Intent it = new Intent(UserPersonalInfoActivity.this,UserPersonalEditActivity.class);
			it.putExtra("intentInfoName", nickName);
//			it.putExtra("intentInfoId", nickId);
			it.putExtra("intentInfoHead", head);
			it.putExtra("intentInfoDesc", nickDesc);
			it.putExtra("intentInfoSex", nickSex);
			startActivity(it);
				break;

		default:
			break;
		}
	}
	
}
