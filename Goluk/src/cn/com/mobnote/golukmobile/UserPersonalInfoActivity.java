package cn.com.mobnote.golukmobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
public class UserPersonalInfoActivity extends Activity implements OnClickListener{

	//title
	Button backBtn,rightBtn;
	TextView centerTitle;
	//姓名、id、头像、分享按钮
	ImageView mImageHead,mImageShare;
	TextView mTextName,mTextId;
	//个性签名，性别，新浪微博
	LinearLayout mLinearSignAll;
	TextView mTextSignDetail,mTextSex,mTextSinaName;
	ImageView mImageSinaHead;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_personal_info);
		
		intiView();
		//设置title
		centerTitle.setText("个人资料");
		
	}
	//初始化控件
	public void intiView(){
		//title——返回按钮、中间标题、右边提示部分
		backBtn = (Button) findViewById(R.id.back_btn);
		rightBtn = (Button) findViewById(R.id.user_title_text);
		centerTitle = (TextView) findViewById(R.id.user_title_right);
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
				
				break;

		default:
			break;
		}
	}
	
}
