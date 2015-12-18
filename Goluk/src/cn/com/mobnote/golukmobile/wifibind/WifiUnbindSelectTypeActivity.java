package cn.com.mobnote.golukmobile.wifibind;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WifiUnbindSelectTypeActivity extends BaseActivity {
	
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
	}
	
	
	private void initView(){
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
		mT1Layout = (RelativeLayout) findViewById(R.id.goluk_t1_layout);
		mG2Layout = (RelativeLayout) findViewById(R.id.goluk_g2_layout);
		mG1Layout = (RelativeLayout) findViewById(R.id.goluk_g1_layout);
	}

}
