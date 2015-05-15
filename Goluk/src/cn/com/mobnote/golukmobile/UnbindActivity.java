package cn.com.mobnote.golukmobile;

import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UnbindActivity extends BaseActivity {

	//title
	private Button mBackBtn = null;
	private TextView mTextTitle = null;
	//body
	private RelativeLayout mHaveipcLayout = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.unbind_layout);
		
	}
}
