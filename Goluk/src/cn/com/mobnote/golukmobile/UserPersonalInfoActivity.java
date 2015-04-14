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
	
	// application
	private GolukApplication mApplication = null;
	// context
	private Context mContext = null;
	//title
	private Button backBtn,rightBtn;
	private TextView mTextCenter;
	//头像
	private ImageView mImageHead;
	private TextView mTextName,mTextId;
	//个性签名
	private TextView mTextSign;
	//性别
	private TextView mTextSex;
	//xinxi
	private String head = null;
	private String name = null;
	private String sex = null;
	private String sign = null;
	
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
		mTextCenter.setText("个人资料");
		
		initData();
		
	}
	
	public void intiView(){
		//title
		backBtn = (Button) findViewById(R.id.back_btn);
		rightBtn = (Button) findViewById(R.id.user_title_right);
		mTextCenter = (TextView) findViewById(R.id.user_title_text);
		//头像
		mImageHead = (ImageView) findViewById(R.id.user_personal_info_image);
		mTextName = (TextView) findViewById(R.id.user_personal_info_name);
		mTextId = (TextView) findViewById(R.id.user_personal_info_id);
		//个性签名
		mTextSign = (TextView) findViewById(R.id.user_personal_info_sing_text);
		//性别
		mTextSex = (TextView) findViewById(R.id.user_personal_info_sex_text);
		
		/**
		 * 监听
		 */
		backBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		rightBtn.setOnTouchListener(this);
		
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
			case R.id.back_btn:
				finish();
				break;
			case R.id.user_title_right:
				Intent itEdit = new Intent(UserPersonalInfoActivity.this,UserPersonalEditActivity.class);
				itEdit.putExtra("infoHead", head);
				itEdit.putExtra("infoName", name);
				itEdit.putExtra("infoSex", sex);
				itEdit.putExtra("infoSign", sign);
				Log.i("info", head+name+sex+sign);
				startActivity(itEdit);
				break;
			default:
				break;
			}
		}
	/**
	 * 初始化用户信息
	 */
	public void initData(){
		String info = mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try{
			JSONObject json = new JSONObject(info);
			
			Log.i("info", "====json===="+json);
			head = json.getString("head");
			name = json.getString("nickname");
			String id = json.getString("aid");
			sex = json.getString("sex");
			sign = json.getString("desc");
	
			mTextName.setText(name);
			UserUtils.userHeadChange(mImageHead, head, mTextSex);
			mTextId.setText(id);
			mTextSign.setText(sign);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
