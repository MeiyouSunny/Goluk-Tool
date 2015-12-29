package cn.com.mobnote.golukmobile.adas;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.alibaba.fastjson.JSON;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;


import android.widget.ListView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventAdasConfigStatus;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.GolukUtils;
import de.greenrobot.event.EventBus;

public class AdasSeletedVehicleTypeActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
	private static final String TAG = "AdasSeletedVehicleTypeActivity";
	private static final String CONFIG_FILE_NAME = "vehicleConfig";
	public  static final String FROM = "from";
	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;
	private ListView mListView = null;
	private CarTypeAdapter mCarTypeAdapter = null;
	private int mFromType = 0; /**向导跳转：0，  配置页跳转：1**/
	private Button mNextButton;
	private AdasConfigParamterBean mAdasConfigParamter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adasselected_vehicle_type);
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, TAG);
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			mFromType = intent.getIntExtra(FROM, 0);
			mAdasConfigParamter = (AdasConfigParamterBean) intent.getSerializableExtra(AdasVerificationActivity.ADASCONFIGDATA);
		} else {
			mFromType = savedInstanceState.getInt(FROM);
			mAdasConfigParamter = (AdasConfigParamterBean) savedInstanceState.getSerializable(AdasVerificationActivity.ADASCONFIGDATA);
		}
		EventBus.getDefault().register(this);
		initView();
		loadData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt(FROM, mFromType);
		if (mAdasConfigParamter != null) {
			outState.putSerializable(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
		}
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
		mNextButton = (Button) findViewById(R.id.button_selected_complete);
		mNextButton.setOnClickListener(this);
		if (mFromType != 0) {
			mNextButton.setText(R.string.short_input_ok);
		}
		mListView = (ListView) findViewById(R.id.listview_car_type);
		mCarTypeAdapter = new CarTypeAdapter(this);
		mListView.setAdapter(mCarTypeAdapter);
		mListView.setOnItemClickListener(this);
		
	}

	private void loadData() {
		String listStr = getDataFromAssets(CONFIG_FILE_NAME);
		if (listStr != null) {
			ArrayList<VehicleParamterBean> data = (ArrayList<VehicleParamterBean>) JSON.parseArray(listStr, VehicleParamterBean.class);
			mCarTypeAdapter.setData(data);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.imagebutton_back:
			// 返回
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			finish();
			break;
		case R.id.button_selected_complete:
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			if (mFromType == 0) {
				Intent intent = new Intent(AdasSeletedVehicleTypeActivity.this, AdasVerificationActivity.class);
				intent.putExtra(AdasVerificationActivity.FROM, 0);
				intent.putExtra(AdasVerificationActivity.ADASCONFIGDATA, mAdasConfigParamter);
				startActivity(intent);
			} else {
				
			}
			break;
		default:
			Log.e(TAG, "id = " + id);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		mCarTypeAdapter.setSelectedId(position);
	}

	public String getDataFromAssets(String fileName) {
		InputStreamReader inputReader = null;
		try {
			inputReader = new InputStreamReader(getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String result = "";
			while ((line = bufReader.readLine()) != null) {
				result += line;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputReader != null) {
				try {
					inputReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(EventAdasConfigStatus event) {
		finish();
	}
}
