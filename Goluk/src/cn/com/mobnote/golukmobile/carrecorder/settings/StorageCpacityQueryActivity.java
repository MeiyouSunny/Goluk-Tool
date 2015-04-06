package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.BaseActivity;

public class StorageCpacityQueryActivity extends BaseActivity{
	private TextView mTotalSize=null;
	private TextView mUsedSize=null;
	
	private TextView mCycleSize=null;
	private TextView mWonderfulSize=null;
	private TextView mEmergencySize=null;
	
	private TextView mOtherSize=null;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_storage_cpacity_query, null)); 
		setTitle("容量查询");
		
		initView();
	}
	
	private void initView(){
		mTotalSize = (TextView)findViewById(R.id.mTotalSize);
		mUsedSize = (TextView)findViewById(R.id.mUsedSize);
		mCycleSize = (TextView)findViewById(R.id.mCycleSize);
		mWonderfulSize = (TextView)findViewById(R.id.mWonderfulSize);
		mEmergencySize = (TextView)findViewById(R.id.mEmergencySize);
		mOtherSize = (TextView)findViewById(R.id.mOtherSize);
		
		
		mTotalSize.setText("32.00GB");
		mUsedSize.setText("3.15MB");
		mCycleSize.setText("32.00GB");
		mWonderfulSize.setText("3.15MB");
		mEmergencySize.setText("3.15MB");
		mOtherSize.setText("3.15MB");
		
	}

}
