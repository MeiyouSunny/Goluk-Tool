package cn.com.mobnote.golukmobile;

import android.os.Bundle;
import android.view.Window;

public class UnbindActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.unbind_layout);
		
	}
}
