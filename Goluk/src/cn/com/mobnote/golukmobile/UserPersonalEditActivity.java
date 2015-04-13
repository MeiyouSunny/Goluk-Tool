package cn.com.mobnote.golukmobile;

import java.net.URLEncoder;

import org.json.JSONObject;

import com.baidu.location.ac;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.console;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 编辑资料
 * @author mobnote
 *
 */
public class UserPersonalEditActivity extends Activity implements OnClickListener,OnTouchListener{

	//title
	private Button btnBack,btnRight;
	private TextView mTextTitle;
	//头像
	private ImageView mImageHead;
	//昵称
	private TextView mTextName;
	//性别
	private TextView mTextSex;
	//个性签名
	private TextView mTextSign;
	//点击每一项
	private RelativeLayout mLayoutHead,mLayoutName,mLayoutSex,mLayoutSign;
	//application
	private GolukApplication mApplication = null;
	//context
	private Context mContext = null;
	//修改用户信息
	private String name;
	private String head;
	private String sex;
	private String desc;
	private String nameOk,descOk;
	//UserPersonalHeadActivity传来的head编号
	private String index;
	private String editHead;
	//从UserPersonalInfoActivity中传来的头像编号
	private String infoHead;
	//获取文本框中的数据
	private String infoName,infoSex,infoDesc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_edit);
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserPersonalEdit");
		
		initView();
		//title
		mTextTitle.setText("编辑资料");
				
	}
	//初始化控件
	public void initView(){
		//title
		btnBack = (Button) findViewById(R.id.back_btn);
		btnRight = (Button) findViewById(R.id.user_title_right);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		//头像
		mImageHead = (ImageView) findViewById(R.id.user_person_edit_head_image);
		//昵称
		mTextName = (TextView) findViewById(R.id.user_personal_name_text);
		//性别
		mTextSex = (TextView) findViewById(R.id.user_personal_sex_text);
		//个性签名
		mTextSign = (TextView) findViewById(R.id.user_personal_sign_text);
		//点击每一项
		mLayoutHead = (RelativeLayout) findViewById(R.id.user_personal_edit_layout1);
		mLayoutName = (RelativeLayout) findViewById(R.id.user_personal_edit_layout2);
		mLayoutSex = (RelativeLayout) findViewById(R.id.user_personal_edit_layout3);
		mLayoutSign = (RelativeLayout) findViewById(R.id.user_personal_edit_layout4);
				
		/**
		 *从UserPersonalInfoActivity传来的信息 
		 *
		 */		
		Intent intentInfo = getIntent();
		if(null!=intentInfo.getStringExtra("intentInfoName")){
			infoName = intentInfo.getStringExtra("intentInfoName").toString();
			mTextName.setText(infoName);
		}
		if(null!=intentInfo.getStringExtra("intentInfoDesc")){
			infoDesc = intentInfo.getStringExtra("intentInfoDesc").toString();
			mTextSign.setText(infoDesc);
		}
		if(null!=intentInfo.getStringExtra("intentInfoSex")){
			infoSex = intentInfo.getStringExtra("intentInfoSex").toString();
			mTextSex.setText(infoSex);
		}
		if(null !=intentInfo.getStringExtra("intentInfoHead")){
			infoHead = intentInfo.getStringExtra("intentInfoHead").toString();
			Log.i("edit", infoHead+"=====infoHead====");
			UserUtils.userHeadChange(mImageHead, infoHead,mTextSex);
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
			if(null !=infoHead){
				Bundle bundle = new Bundle();
				bundle.putString("intentHeadText", infoHead);
				itHead.putExtras(bundle);
				startActivityForResult(itHead, 3);
			}
			break;
		//点击昵称
		case R.id.user_personal_edit_layout2:
			String nameText = mTextName.getText().toString();
			Intent itName = new Intent(UserPersonalEditActivity.this,UserPersonalNameActivity.class);
			if(null!=nameText){
				Bundle bundle = new Bundle();
				bundle.putString("intentNameText", nameText);
				itName.putExtras(bundle);
				startActivityForResult(itName, 1);
			}
			break;
		//点击性别
		case R.id.user_personal_edit_layout3:
			
			break;
		//点击个性签名
		case R.id.user_personal_edit_layout4:
			String signText = mTextSign.getText().toString();
			Intent itSign = new Intent(UserPersonalEditActivity.this,UserPersonalSignActivity.class);
			if(null != signText){
				Bundle bundle = new Bundle();
				bundle.putString("intentSignText", signText);
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
		name = mTextName.getText().toString();
		head = editHead;
		sex = mTextSex.getText().toString();
		desc = mTextSign.getText().toString();
		try{
			nameOk = URLEncoder.encode(name, "utf-8");
			descOk = URLEncoder.encode(desc, "utf-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		//{NickName：“昵称”，UserHead:”1”，UserSex:”1”,Desc:""}
		String isSave = "{\"NickName\":\"" + nameOk + "\",\"UserHead\":\""+ head +  "\",\"UserSex\":\""+sex+"\",\"Desc\":\""+descOk+"\"}";
		boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_ModifyUserInfo, isSave);
		if(b){
			Log.i("edit", "====UserPersonalEditActivity()====="+b);
		}else{
			
		}
	}
	/**
	 * 修改用户信息回调
	 */
	public void saveInfoCallBack(int success,Object obj){
		console.log("修改用户信息回调---saveInfoCallBack---" + success + "---" + obj);
		if(1 == success){
			try{
				String data = (String)obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				//解析修改后数据
				JSONObject json2 = json.getJSONObject("data");
				
				Log.i("edit", json2+"");
				String sex = json2.getString("sex");
				String desc = json2.getString("desc");
				String name = json2.getString("nickname");
				String head = json2.getString("head");
				Log.i("edit", head+"======head+++saveInfoCallBack()=====");
				Intent itSave = new Intent(UserPersonalEditActivity.this,UserPersonalInfoActivity.class);
				
				Log.i("edit", infoHead+"infoHead====VS====head"+head);
				Log.i("edit", infoDesc+"infoDesc====VS====desc"+desc);
				Log.i("edit", infoName+"infoName====VS====name"+name);
				/*if(infoDesc.equals(desc) &&  infoName.equals(name) && infoHead.equals(head)){
					this.finish();
					console.toast("没有修改数据", mContext);
				}else{*/
					switch (code) {
					// 如果修改用户信息返回信息成功的话，携带参数跳转页面
					case 200:
						console.toast("数据修改成功", mContext);
						itSave.putExtra("saveName", name);
						itSave.putExtra("saveHead", head);
						itSave.putExtra("saveSex", sex);
						startActivity(itSave);
						break;
					case 405:
						console.toast("该用户未注册", mContext);
						break;

					case 500:
						console.toast("服务器异常", mContext);
						break;

					default:
						break;
					}
			//}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(success == 0){
			console.toast("没有修改数据", mContext);
			this.finish();
		}else{
			//success不等于1
			console.toast("数据修改失败,请重试", mContext);
		}
	}
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
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
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		//修改昵称
		case 1:
			Bundle bundle = data.getExtras();
			String editName = bundle.getString("itName");
			mTextName.setText(editName);
			break;
		//修改个性签名
		case 2:
			Bundle bundle2 = data.getExtras();
			String editSign = bundle2.getString("itSign");
			mTextSign.setText(editSign);
			break;
		//修改头像
		case 3:
			Bundle bundle3 = data.getExtras();
			editHead = bundle3.getString("intentSevenHead");
//			infoHead = editHead;
			UserUtils.userHeadChange(mImageHead, editHead, mTextSex);
			break;

		default:
			break;
		}
	}
}
