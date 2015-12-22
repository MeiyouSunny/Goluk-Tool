package cn.com.mobnote.golukmobile.wifibind;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WifiUnbindSelectTypeActivity extends BaseActivity implements OnClickListener {
	
	/**关闭按钮**/
	private ImageView mCloseBtn;
	
	private RelativeLayout mT1Layout;
	private RelativeLayout mG2Layout;
	private RelativeLayout mG1Layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unbind_type_layout);
		
		initView();
		initLisenner();
	}
	
	/**
	 * 初始化view
	 */
	private void initView(){
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
		mT1Layout = (RelativeLayout) findViewById(R.id.goluk_t1_layout);
		mG2Layout = (RelativeLayout) findViewById(R.id.goluk_g2_layout);
		mG1Layout = (RelativeLayout) findViewById(R.id.goluk_g1_layout);
	}
	
	/**
	 * 初始化view的监听
	 */
	private void initLisenner(){
		mCloseBtn.setOnClickListener(this);
		mT1Layout.setOnClickListener(this);
		mG2Layout.setOnClickListener(this);
		mG1Layout.setOnClickListener(this);
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close_btn:
			this.finish();
			break;
		case R.id.goluk_t1_layout:
		case R.id.goluk_g2_layout:
		case R.id.goluk_g1_layout:
			Intent intent = new Intent(this, WifiUnbindSelectListActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

}
