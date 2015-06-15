package cn.com.mobnote.golukmobile;

import java.net.URLEncoder;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 编辑资料
 * @author mobnote
 *
 */
public class UserPersonalEditActivity extends BaseActivity implements OnClickListener,OnTouchListener{

	//title
	private ImageButton btnBack;
	private Button btnRight;
	private TextView mTextTitle;
	//头像
	private ImageView mImageHead;
	//昵称
	private TextView mTextName;
	//性别
//	private TextView mTextSex;
	//个性签名
	private TextView mTextSign;
	//点击每一项
	private RelativeLayout mLayoutHead,mLayoutName,mLayoutSign;
	//application
	private GolukApplication mApplication = null;
	//context
	private Context mContext = null;
	
	/**传值**/
	private String head = null;
	private String name = null;
	private String sex = null;
	private String sign = null;
	//utf-8
	private String newName = null;
	private String newSign = null;
	// info传来的信息
	private String intentHead = null;
	private String intentName = null;
	private String intentSex = null;
	private String intentSign = null;
	//保存数据的loading
	private CustomLoadingDialog mCustomProgressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_edit);
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserPersonalEdit");
		
		if(null == mCustomProgressDialog){
			mCustomProgressDialog = new CustomLoadingDialog(mContext,"保存中，请稍候……");
		}
		initView();
		//title
		mTextTitle.setText("编辑资料");
				
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	//初始化控件
	public void initView(){
		//title
		btnBack = (ImageButton) findViewById(R.id.back_btn);
		btnRight = (Button) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		//头像
		mImageHead = (ImageView) findViewById(R.id.user_person_edit_head_image);
		//昵称
		mTextName = (TextView) findViewById(R.id.user_personal_name_text);
		//性别
//		mTextSex = (TextView) findViewById(R.id.user_personal_sex_text);
		//个性签名
		mTextSign = (TextView) findViewById(R.id.user_personal_sign_text);
		//点击每一项
		mLayoutHead = (RelativeLayout) findViewById(R.id.user_personal_edit_layout1);
		mLayoutName = (RelativeLayout) findViewById(R.id.user_personal_edit_layout2);
		mLayoutSign = (RelativeLayout) findViewById(R.id.user_personal_edit_layout4);
			
		/**
		 * 从UserPersonalInfoActivity传来的用户信息
		 */
		Intent it = getIntent();
		if(null != it.getStringExtra("infoHead")){
			intentHead = it.getStringExtra("infoHead").toString();
			UserUtils.focusHead(intentHead, mImageHead);
		}
		if(null != it.getStringExtra("infoName")){
			intentName = it.getStringExtra("infoName").toString();
			mTextName.setText(intentName);
		}
		if(null != it.getStringExtra("infoSex")){
			intentSex = it.getStringExtra("infoSex").toString();
		}
		if(null != it.getStringExtra("infoSign")){
			intentSign = it.getStringExtra("infoSign").toString();
			mTextSign.setText(intentSign);
		}
		GolukDebugUtils.i("lily", "------head--------"+intentHead+"-----name---"+intentName+"----sex----"+intentSex+"-----sign----"+intentSign);
		if(head == null || name == null || sex == null || sign == null){
			head = intentHead;
			name = intentName;
			sex = intentSex;
			sign = intentSign;
		}
		
		/**
		 * 监听
		 */
		//title
		btnBack.setOnClickListener(this);
		btnRight.setOnClickListener(this);
		btnRight.setOnTouchListener(this);
		//头像
		mLayoutHead.setOnClickListener(this);
		//昵称
		mLayoutName.setOnClickListener(this);
		//个性签名
		mLayoutSign.setOnClickListener(this);
		
	}
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		//title返回
		case R.id.back_btn:
			this.finish();
			break;
		//保存
		case R.id.user_title_right:
			//点击保存将修改的数据存储
			saveInfo();
			break;
		/**
		 * 点击每一项
		 */
			//点击头像
		case R.id.user_personal_edit_layout1:
			Intent itHead = new Intent(UserPersonalEditActivity.this,UserPersonalHeadActivity.class);
			if(null !=head){
				Bundle bundle = new Bundle();
				bundle.putString("intentHeadText", head);
				itHead.putExtras(bundle);
				startActivityForResult(itHead, 3);
			}
			break;
		//点击昵称
		case R.id.user_personal_edit_layout2:
			Intent itName = new Intent(UserPersonalEditActivity.this,UserPersonalNameActivity.class);
			if(null!=name){
				Bundle bundle = new Bundle();
				bundle.putString("intentNameText", name);
				itName.putExtras(bundle);
				startActivityForResult(itName, 1);
			}
			break;
		//点击个性签名
		case R.id.user_personal_edit_layout4:
			Intent itSign = new Intent(UserPersonalEditActivity.this,UserPersonalSignActivity.class);
			if(null != sign){
				Bundle bundle = new Bundle();
				bundle.putString("intentSignText", sign);
				itSign.putExtras(bundle);
				startActivityForResult(itSign, 2);
			}
			break;
		default:
			break;
		}
	}
	/**
	 * 修改用户信息
	 */
	public void saveInfo(){
		try{
			newName = URLEncoder.encode(name, "utf-8");
			newSign = URLEncoder.encode(sign,"utf-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		if(newName.trim().isEmpty() || name.trim().isEmpty()){
			UserUtils.showDialog(mContext, "数据修改失败，昵称不能为空");
		}else{
			if(intentName.equals(name) && intentHead.equals(head) && intentSign.equals(sign)){
				this.finish();
			}else{
				//{NickName：“昵称”，UserHead:”1”，UserSex:”1”,Desc:""}
				String isSave = "{\"NickName\":\"" + newName + "\",\"UserHead\":\""+ head +  "\",\"UserSex\":\""+sex+"\",\"Desc\":\""+newSign+"\"}";
				boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_ModifyUserInfo, isSave);
				if(b){
					//保存中
					mCustomProgressDialog.show();
					btnBack.setEnabled(false);
					btnRight.setEnabled(false);
					mLayoutHead.setEnabled(false);
					mLayoutName.setEnabled(false);
					mLayoutSign.setEnabled(false);
				}
			}
		}
	}
	/**
	 * 修改用户信息回调
	 */
	
	public void saveInfoCallBack(int success,Object obj){
		GolukDebugUtils.e("","修改用户信息回调---saveInfoCallBack---" + success + "---" + obj);
		if(1 == success){
			try{
				String data = (String)obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				
				closeProgressDialog();
				switch (code) {
				case 200:
					GolukUtils.showToast(mContext, "数据修改成功");
					this.finish();
					break;
				case 405:
					GolukUtils.showToast(mContext, "该用户未注册");
					closeProgressDialog();
					btnBack.setEnabled(true);
					btnRight.setEnabled(true);
					mLayoutHead.setEnabled(true);
					mLayoutName.setEnabled(true);
					mLayoutSign.setEnabled(true);
					break;

				case 500:
					GolukUtils.showToast(mContext, "服务器异常");
					closeProgressDialog();
					btnBack.setEnabled(true);
					btnRight.setEnabled(true);
					mLayoutHead.setEnabled(true);
					mLayoutName.setEnabled(true);
					mLayoutSign.setEnabled(true);
					break;

				default:
					break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			//success不等于1
			GolukUtils.showToast(mContext, "数据修改失败,请重试");
			closeProgressDialog();
			btnBack.setEnabled(true);
			btnRight.setEnabled(true);
			mLayoutHead.setEnabled(true);
			mLayoutName.setEnabled(true);
			mLayoutSign.setEnabled(true);
		}
	}
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_title_right:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				btnRight.setTextColor(Color.rgb(0, 197, 176));
				break;
			case MotionEvent.ACTION_UP:
				btnRight.setTextColor(Color.WHITE);
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
	
	/**
	 * 分别获取从修改昵称、修改个性签名和修改头像页面的数据
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		//修改昵称
		case 1:
			Bundle bundle = data.getExtras();
			name = bundle.getString("itName");
			mTextName.setText(name);
			break;
		//修改个性签名
		case 2:
			Bundle bundle2 = data.getExtras();
			sign = bundle2.getString("itSign");
			mTextSign.setText(sign);
			break;
		//修改头像
		case 3:
			Bundle bundle3 = data.getExtras();
			head = bundle3.getString("intentSevenHead");
			UserUtils.focusHead(head, mImageHead);
			break;

		default:
			break;
		}
	}
	
	/**
	 * 关闭加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}
	
}
